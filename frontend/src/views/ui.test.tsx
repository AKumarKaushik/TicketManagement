import type { ReactElement } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TicketApiProvider } from "../api/TicketApiContext";
import type { TicketApi, TicketDetails } from "../api/types";
import { TicketApiError } from "../api/types";
import { TicketListView } from "../views/TicketListView";
import { TicketCreateView } from "../views/TicketCreateView";
import { TicketDetailView } from "../views/TicketDetailView";
import { TicketNotFoundView } from "../views/TicketNotFoundView";
import { nextStatuses } from "../domain/statusTransitions";
import App from "../App";

function apiStub(overrides: Partial<TicketApi> = {}): TicketApi {
  return {
    listTickets: vi.fn().mockResolvedValue([]),
    listByStatus: vi.fn().mockResolvedValue([]),
    searchByKeyword: vi.fn().mockResolvedValue([]),
    getTicket: vi.fn(),
    createTicket: vi.fn(),
    updateTicket: vi.fn(),
    addComment: vi.fn(),
    changeStatus: vi.fn(),
    ...overrides,
  };
}

function renderWithApi(ui: ReactElement, api: TicketApi) {
  return render(<TicketApiProvider value={api}>{ui}</TicketApiProvider>);
}

const openTicket: TicketDetails = {
  id: "t-1",
  title: "Network outage",
  description: "VPN down",
  priority: null,
  status: "OPEN",
  assignee: "alice",
  comments: [{ content: "Checking routers", author: "bob", creationTime: "2026-09-21T08:00:00Z" }],
};

describe("status transition options", () => {
  it("offers only valid next statuses", () => {
    expect(nextStatuses("OPEN")).toEqual(["IN_PROGRESS", "CANCELLED"]);
    expect(nextStatuses("IN_PROGRESS")).toEqual(["RESOLVED", "CANCELLED"]);
    expect(nextStatuses("RESOLVED")).toEqual(["CLOSED"]);
    expect(nextStatuses("CLOSED")).toEqual([]);
    expect(nextStatuses("CANCELLED")).toEqual([]);
  });
});

describe("V-LIST", () => {
  it("shows a loading state then an empty list", async () => {
    const api = apiStub({
      listTickets: vi.fn().mockResolvedValue([]),
    });
    renderWithApi(<TicketListView onCreate={() => undefined} onOpen={() => undefined} />, api);
    expect(screen.getByRole("status")).toHaveTextContent("Loading tickets");
    expect(await screen.findByText("No tickets to display.")).toBeInTheDocument();
  });

  it("shows an error without inventing tickets", async () => {
    const api = apiStub({
      listTickets: vi.fn().mockRejectedValue(
        new TicketApiError({
          status: 500,
          errorCode: "UNEXPECTED",
          message: "Temporary failure",
        }),
      ),
    });
    renderWithApi(<TicketListView onCreate={() => undefined} onOpen={() => undefined} />, api);
    expect(await screen.findByRole("alert")).toHaveTextContent("Temporary failure");
    expect(screen.queryByRole("list")).not.toBeInTheDocument();
  });

  it("filters by an approved status only", async () => {
    const user = userEvent.setup();
    const api = apiStub({
      listByStatus: vi.fn().mockResolvedValue([
        { id: "t-1", title: "Network outage", description: null, priority: null, status: "OPEN", assignee: null },
      ]),
    });
    renderWithApi(<TicketListView onCreate={() => undefined} onOpen={() => undefined} />, api);
    await screen.findByRole("status");
    await user.selectOptions(screen.getByLabelText("Status filter"), "OPEN");
    expect(api.listByStatus).toHaveBeenCalledWith("OPEN");
    expect(await screen.findByText("Network outage")).toBeInTheDocument();
  });
});

describe("V-CREATE", () => {
  it("validates a blank title without calling create", async () => {
    const user = userEvent.setup();
    const api = apiStub();
    renderWithApi(<TicketCreateView onCancel={() => undefined} onCreated={() => undefined} />, api);
    await user.click(screen.getByRole("button", { name: "Create ticket" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Title must be present and not blank.");
    expect(api.createTicket).not.toHaveBeenCalled();
    expect(screen.queryByLabelText("Status")).not.toBeInTheDocument();
  });

  it("displays backend validation on title", async () => {
    const user = userEvent.setup();
    const api = apiStub({
      createTicket: vi.fn().mockRejectedValue(
        new TicketApiError({
          status: 400,
          errorCode: "VALIDATION",
          message: "Title is invalid.",
          fields: [{ field: "title", message: "Title must not be blank." }],
        }),
      ),
    });
    renderWithApi(<TicketCreateView onCancel={() => undefined} onCreated={() => undefined} />, api);
    await user.type(screen.getByLabelText("Title"), "Printer jam");
    await user.click(screen.getByRole("button", { name: "Create ticket" }));
    expect(await screen.findByText("Title must not be blank.")).toBeInTheDocument();
    expect(screen.getByLabelText("Title")).toHaveValue("Printer jam");
  });

  it("calls onCreated only after the backend returns the new ticket", async () => {
    const user = userEvent.setup();
    const onCreated = vi.fn();
    let resolveCreate: (ticket: { id: string; title: string; description: string | null; priority: string | null; status: "OPEN"; assignee: string | null }) => void = () => undefined;
    const api = apiStub({
      createTicket: vi.fn().mockImplementation(
        () =>
          new Promise((resolve) => {
            resolveCreate = resolve;
          }),
      ),
    });
    renderWithApi(<TicketCreateView onCancel={() => undefined} onCreated={onCreated} />, api);
    await user.type(screen.getByLabelText("Title"), "Printer jam");
    await user.click(screen.getByRole("button", { name: "Create ticket" }));
    expect(api.createTicket).toHaveBeenCalled();
    expect(onCreated).not.toHaveBeenCalled();
    expect(screen.getByRole("heading", { name: "Create ticket" })).toBeInTheDocument();
    resolveCreate({
      id: "t-new",
      title: "Printer jam",
      description: null,
      priority: null,
      status: "OPEN",
      assignee: null,
    });
    await waitFor(() => expect(onCreated).toHaveBeenCalledWith("t-new"));
  });
});

describe("V-DETAIL", () => {
  it("renders ticket fields and comments", async () => {
    const api = apiStub({
      getTicket: vi.fn().mockResolvedValue(openTicket),
    });
    renderWithApi(
      <TicketDetailView ticketId="t-1" onBack={() => undefined} onNotFound={() => undefined} />,
      api,
    );
    expect(await screen.findByDisplayValue("Network outage")).toBeInTheDocument();
    expect(screen.getByText("OPEN")).toBeInTheDocument();
    expect(screen.getByText("Checking routers")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Change to IN_PROGRESS" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Change to CANCELLED" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Change to RESOLVED" })).not.toBeInTheDocument();
  });

  it("offers no status changes for a terminal ticket", async () => {
    const api = apiStub({
      getTicket: vi.fn().mockResolvedValue({ ...openTicket, status: "CLOSED" }),
    });
    renderWithApi(
      <TicketDetailView ticketId="t-1" onBack={() => undefined} onNotFound={() => undefined} />,
      api,
    );
    expect(await screen.findByText(/terminal/i)).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /Change to/ })).not.toBeInTheDocument();
  });

  it("keeps the previous status after a 409 conflict", async () => {
    const user = userEvent.setup();
    const api = apiStub({
      getTicket: vi.fn().mockResolvedValue(openTicket),
      changeStatus: vi.fn().mockRejectedValue(
        new TicketApiError({
          status: 409,
          errorCode: "ILLEGAL_TRANSITION",
          message: "That status change is not allowed.",
        }),
      ),
    });
    renderWithApi(
      <TicketDetailView ticketId="t-1" onBack={() => undefined} onNotFound={() => undefined} />,
      api,
    );
    await screen.findByText("OPEN");
    await user.click(screen.getByRole("button", { name: "Change to IN_PROGRESS" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("That status change is not allowed.");
    expect(screen.getByText("OPEN")).toBeInTheDocument();
  });

  it("signals not found instead of rendering a blank ticket", async () => {
    const onNotFound = vi.fn();
    const api = apiStub({
      getTicket: vi.fn().mockRejectedValue(
        new TicketApiError({
          status: 404,
          errorCode: "NOT_FOUND",
          message: "Ticket was not found.",
        }),
      ),
    });
    renderWithApi(
      <TicketDetailView ticketId="missing" onBack={() => undefined} onNotFound={onNotFound} />,
      api,
    );
    expect(await screen.findByRole("button", { name: "Back to list" })).toBeInTheDocument();
    expect(onNotFound).toHaveBeenCalled();
    expect(screen.queryByLabelText("Title")).not.toBeInTheDocument();
  });
});

describe("V-NOT-FOUND", () => {
  it("shows a not-found message", () => {
    render(<TicketNotFoundView onBack={() => undefined} />);
    expect(screen.getByRole("heading", { name: "Ticket not found" })).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("does not exist");
  });
});

describe("create to detail navigation", () => {
  it("opens the ticket detail only after create succeeds", async () => {
    const user = userEvent.setup();
    const created = {
      id: "t-new",
      title: "Printer jam",
      description: null,
      priority: null,
      status: "OPEN" as const,
      assignee: null,
    };
    let resolveCreate: (ticket: typeof created) => void = () => undefined;
    const api = apiStub({
      listTickets: vi.fn().mockResolvedValue([]),
      createTicket: vi.fn().mockImplementation(
        () =>
          new Promise((resolve) => {
            resolveCreate = resolve;
          }),
      ),
      getTicket: vi.fn().mockResolvedValue({ ...created, comments: [] }),
    });
    render(<App ticketApi={api} />);
    await screen.findByRole("heading", { name: "Tickets" });
    await user.click(screen.getByRole("button", { name: "Create ticket" }));
    await user.type(screen.getByLabelText("Title"), "Printer jam");
    await user.click(screen.getByRole("button", { name: "Create ticket" }));
    expect(api.createTicket).toHaveBeenCalled();
    expect(api.getTicket).not.toHaveBeenCalled();
    expect(screen.getByRole("heading", { name: "Create ticket" })).toBeInTheDocument();
    resolveCreate(created);
    expect(await screen.findByRole("heading", { name: "Ticket t-new" })).toBeInTheDocument();
    expect(api.getTicket).toHaveBeenCalledWith("t-new");
    expect(screen.getByDisplayValue("Printer jam")).toBeInTheDocument();
  });
});
