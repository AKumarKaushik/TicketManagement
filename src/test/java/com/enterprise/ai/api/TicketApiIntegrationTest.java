package com.enterprise.ai.api;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.enterprise.ai.application.TicketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketApiIntegrationTest {

    private static final String UUID_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository tickets;

    @BeforeEach
    void clearStore() {
        tickets.deleteAll();
    }

    @Test
    void create_assignsUuidAndOpenStatus() throws Exception {
        MvcResult result = mockMvc.perform(post("/tickets")
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {"title":"Network outage","description":"VPN down","priority":"unspecified","assignee":"alice"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/tickets/" + UUID_PATTERN)))
                .andExpect(jsonPath("$.id", matchesPattern(UUID_PATTERN)))
                .andExpect(jsonPath("$.title").value("Network outage"))
                .andExpect(jsonPath("$.description").value("VPN down"))
                .andExpect(jsonPath("$.priority").value("unspecified"))
                .andExpect(jsonPath("$.assignee").value("alice"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        String id = idFrom(result);
        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.comments", empty()));
    }

    @Test
    void create_rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/tickets")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields[0].field").value("title"));
    }

    @Test
    void create_rejectsClientAssignedId() throws Exception {
        mockMvc.perform(post("/tickets")
                        .contentType(APPLICATION_JSON)
                        .content(
                                "{\"title\":\"Printer jam\",\"id\":\"11111111-1111-1111-1111-111111111111\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.fields[0].field").value("id"));
    }

    @Test
    void list_returnsEmptyArrayWhenNoTickets() throws Exception {
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void list_andDetail_returnPersistedTickets() throws Exception {
        String first = createTicket("Network outage");
        String second = createTicket("Printer jam");

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", org.hamcrest.Matchers.containsInAnyOrder(first, second)));

        mockMvc.perform(get("/tickets/" + first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Network outage"))
                .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    void get_unknownTicket_returnsNotFound() throws Exception {
        mockMvc.perform(get("/tickets/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.fields").doesNotExist());
    }

    @Test
    void update_replacesSuppliedFieldsOnly() throws Exception {
        String id = createTicket("Old title");

        mockMvc.perform(patch("/tickets/" + id)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {"title":"New title","description":"Details","priority":"high","assignee":"bob"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("Details"))
                .andExpect(jsonPath("$.priority").value("high"))
                .andExpect(jsonPath("$.assignee").value("bob"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.assignee").value("bob"));
    }

    @Test
    void update_rejectsStatusInPatchBody() throws Exception {
        String id = createTicket("Keep open");

        mockMvc.perform(patch("/tickets/" + id)
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Keep open\",\"status\":\"CLOSED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"));
    }

    @Test
    void update_emptyBody_isValidation() throws Exception {
        String id = createTicket("Needs a field");

        mockMvc.perform(patch("/tickets/" + id).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"));
    }

    @Test
    void comment_isStoredOnTicket() throws Exception {
        String id = createTicket("Network outage");

        mockMvc.perform(post("/tickets/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"Checking routers\",\"author\":\"bob\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tickets/" + id))
                .andExpect(jsonPath("$.content").value("Checking routers"))
                .andExpect(jsonPath("$.author").value("bob"))
                .andExpect(jsonPath("$.creationTime").isString())
                .andExpect(jsonPath("$.id").doesNotExist());

        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].content").value("Checking routers"))
                .andExpect(jsonPath("$.comments[0].author").value("bob"));
    }

    @Test
    void comment_rejectsBlankContent() throws Exception {
        String id = createTicket("Network outage");

        mockMvc.perform(post("/tickets/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"  \",\"author\":\"bob\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.fields[0].field").value("content"));
    }

    @Test
    void comment_unknownTicket_isNotFound() throws Exception {
        mockMvc.perform(post("/tickets/11111111-1111-1111-1111-111111111111/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"Checking routers\",\"author\":\"bob\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @ParameterizedTest
    @CsvSource({
        "IN_PROGRESS, OPEN",
        "RESOLVED, IN_PROGRESS",
        "CLOSED, RESOLVED",
        "CANCELLED, OPEN",
        "CANCELLED, IN_PROGRESS"
    })
    void validTransitions_persistNewStatus(String target, String setupFrom) throws Exception {
        String id = ticketAt(setupFrom);

        mockMvc.perform(post("/tickets/" + id + "/status")
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"" + target + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(target));

        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(target));
    }

    @ParameterizedTest
    @CsvSource({
        "OPEN, RESOLVED",
        "OPEN, CLOSED",
        "IN_PROGRESS, OPEN",
        "IN_PROGRESS, CLOSED",
        "RESOLVED, OPEN",
        "RESOLVED, IN_PROGRESS",
        "RESOLVED, CANCELLED",
        "CLOSED, OPEN",
        "CLOSED, IN_PROGRESS",
        "CLOSED, RESOLVED",
        "CLOSED, CANCELLED",
        "CANCELLED, OPEN",
        "CANCELLED, IN_PROGRESS",
        "CANCELLED, RESOLVED",
        "CANCELLED, CLOSED"
    })
    void invalidTransition_returns409AndLeavesStatus(String from, String to) throws Exception {
        String id = ticketAt(from);

        mockMvc.perform(post("/tickets/" + id + "/status")
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"" + to + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ILLEGAL_TRANSITION"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.fields").doesNotExist());

        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(from));
    }

    @ParameterizedTest
    @ValueSource(strings = {"open", "DONE", " ", "INPROGRESS"})
    void changeStatus_unknownToken_isValidation(String token) throws Exception {
        String id = createTicket("Token check");

        mockMvc.perform(post("/tickets/" + id + "/status")
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"" + token + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.fields[0].field").value("status"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED"})
    void filter_matchesEachStatus(String status) throws Exception {
        String id = ticketAt(status);

        mockMvc.perform(get("/tickets").param("status", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].status").value(status));
    }

    @Test
    void filter_unknownStatus_isValidationNotEmptyList() throws Exception {
        createTicket("Should not be listed as success");

        mockMvc.perform(get("/tickets").param("status", "open"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"));
    }

    @Test
    void filter_emptyResult_isEmptyArray() throws Exception {
        createTicket("Only open");

        mockMvc.perform(get("/tickets").param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void search_isNotImplemented() throws Exception {
        createTicket("Network outage");

        mockMvc.perform(get("/tickets").param("keyword", "Network"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("UNEXPECTED"));
    }

    @Test
    void filter_doesNotMutateStoredTickets() throws Exception {
        String id = createTicket("Must stay open");

        mockMvc.perform(get("/tickets").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.title").value("Must stay open"));
    }

    @Test
    void list_doesNotExposeComments_detailDoes() throws Exception {
        String id = createTicket("Network outage");
        mockMvc.perform(post("/tickets/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"Checking routers\",\"author\":\"bob\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].comments").doesNotExist());

        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].content").value("Checking routers"));
    }

    @Test
    void update_unknownTicket_returnsNotFound() throws Exception {
        mockMvc.perform(patch("/tickets/11111111-1111-1111-1111-111111111111")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"New title\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.fields").doesNotExist());
    }

    @Test
    void changeStatus_unknownTicket_returnsNotFound() throws Exception {
        mockMvc.perform(post("/tickets/11111111-1111-1111-1111-111111111111/status")
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void comment_rejectsMissingAuthor() throws Exception {
        String id = createTicket("Network outage");

        mockMvc.perform(post("/tickets/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"Checking routers\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.fields[0].field").value("author"));
    }

    @Test
    void comment_rejectsBlankAuthor() throws Exception {
        String id = createTicket("Network outage");

        mockMvc.perform(post("/tickets/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"Checking routers\",\"author\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.fields[0].field").value("author"));
    }

    @Test
    void comment_rejectsWhitespaceOnlyAuthor() throws Exception {
        String id = createTicket("Network outage");

        mockMvc.perform(post("/tickets/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":\"Checking routers\",\"author\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION"))
                .andExpect(jsonPath("$.fields[0].field").value("author"));
    }

    @Test
    void create_clientSuppliedStatus_doesNotStoreNonOpenStatus() throws Exception {
        // OQ-013 (ignore vs reject of create-body status) is unresolved. This asserts REQ-013 only:
        // a client-supplied status must not become the stored create status.
        MvcResult result = mockMvc.perform(post("/tickets")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Network outage\",\"status\":\"CLOSED\"}"))
                .andReturn();

        int http = result.getResponse().getStatus();
        assertTrue(
                http == 201 || http == 400,
                "Create with extra status must not store a non-OPEN ticket; HTTP was " + http);

        if (http == 201) {
            JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals("OPEN", body.get("status").asText());
            mockMvc.perform(get("/tickets/" + body.get("id").asText()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OPEN"));
        } else {
            mockMvc.perform(get("/tickets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].status", not(hasItem("CLOSED"))));
        }
    }

    @Test
    void create_titleLongerThan255_isPersisted() throws Exception {
        String title = "T".repeat(300);
        String body = objectMapper.writeValueAsString(java.util.Map.of("title", title));

        MvcResult result = mockMvc.perform(post("/tickets").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        mockMvc.perform(get("/tickets/" + idFrom(result)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title));
    }

    private String createTicket(String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/tickets")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private String ticketAt(String status) throws Exception {
        String id = createTicket("Lifecycle " + status);
        if ("OPEN".equals(status)) {
            return id;
        }
        if ("IN_PROGRESS".equals(status) || "RESOLVED".equals(status) || "CLOSED".equals(status)) {
            postStatus(id, "IN_PROGRESS");
        }
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            postStatus(id, "RESOLVED");
        }
        if ("CLOSED".equals(status)) {
            postStatus(id, "CLOSED");
        }
        if ("CANCELLED".equals(status)) {
            postStatus(id, "CANCELLED");
        }
        return id;
    }

    private void postStatus(String id, String status) throws Exception {
        mockMvc.perform(post("/tickets/" + id + "/status")
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    private String idFrom(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asText();
    }
}
