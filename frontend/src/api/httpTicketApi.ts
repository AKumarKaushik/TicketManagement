import {
  TicketApi,
  TicketApiError,
  type AddCommentRequest,
  type ApiErrorBody,
  type Comment,
  type CreateTicketRequest,
  type ErrorCode,
  type Ticket,
  type TicketDetails,
  type TicketStatus,
  type UpdateTicketRequest,
} from "./types";

function unexpected(message = "Something went wrong. Please try again."): TicketApiError {
  return new TicketApiError({
    status: 500,
    errorCode: "UNEXPECTED",
    message,
  });
}

function parseError(status: number, text: string): TicketApiError {
  try {
    const body = JSON.parse(text) as Partial<ApiErrorBody>;
    const errorCode = (body.errorCode ?? "UNEXPECTED") as ErrorCode;
    return new TicketApiError({
      status: body.status ?? status,
      errorCode,
      message: body.message ?? "The request could not be completed.",
      fields: body.fields,
    });
  } catch {
    return unexpected();
  }
}

export function createHttpTicketApi(baseUrl = ""): TicketApi {
  async function request<T>(path: string, init?: RequestInit): Promise<T> {
    let response: Response;
    try {
      response = await fetch(`${baseUrl}${path}`, {
        ...init,
        headers: {
          Accept: "application/json",
          ...(init?.body ? { "Content-Type": "application/json" } : {}),
          ...init?.headers,
        },
      });
    } catch {
      throw unexpected("The ticket service is not available.");
    }

    const text = await response.text();
    if (!response.ok) {
      throw parseError(response.status, text);
    }
    if (!text) {
      throw unexpected();
    }
    return JSON.parse(text) as T;
  }

  return {
    listTickets: () => request<Ticket[]>("/tickets"),
    listByStatus: (status: TicketStatus) =>
      request<Ticket[]>(`/tickets?status=${encodeURIComponent(status)}`),
    searchByKeyword: (keyword: string) =>
      request<Ticket[]>(`/tickets?keyword=${encodeURIComponent(keyword)}`),
    getTicket: (id: string) => request<TicketDetails>(`/tickets/${encodeURIComponent(id)}`),
    createTicket: (body: CreateTicketRequest) =>
      request<Ticket>("/tickets", { method: "POST", body: JSON.stringify(body) }),
    updateTicket: (id: string, body: UpdateTicketRequest) =>
      request<Ticket>(`/tickets/${encodeURIComponent(id)}`, {
        method: "PATCH",
        body: JSON.stringify(body),
      }),
    addComment: (id: string, body: AddCommentRequest) =>
      request<Comment>(`/tickets/${encodeURIComponent(id)}/comments`, {
        method: "POST",
        body: JSON.stringify(body),
      }),
    changeStatus: (id: string, status: TicketStatus) =>
      request<Ticket>(`/tickets/${encodeURIComponent(id)}/status`, {
        method: "POST",
        body: JSON.stringify({ status }),
      }),
  };
}
