export const TICKET_STATUSES = [
  "OPEN",
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
  "CANCELLED",
] as const;

export type TicketStatus = (typeof TICKET_STATUSES)[number];

export interface Ticket {
  id: string;
  title: string;
  description: string | null;
  priority: string | null;
  status: TicketStatus;
  assignee: string | null;
}

export interface Comment {
  content: string;
  author: string;
  creationTime: string;
}

export interface TicketDetails extends Ticket {
  comments: Comment[];
}

export interface CreateTicketRequest {
  title: string;
  description?: string;
  priority?: string;
  assignee?: string;
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  priority?: string;
  assignee?: string;
}

export interface AddCommentRequest {
  content: string;
  author: string;
}

export type ErrorCode =
  | "VALIDATION"
  | "ILLEGAL_TRANSITION"
  | "NOT_FOUND"
  | "UNEXPECTED";

export interface FieldError {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  status: number;
  errorCode: ErrorCode;
  message: string;
  fields?: FieldError[];
}

export class TicketApiError extends Error {
  readonly status: number;
  readonly errorCode: ErrorCode;
  readonly fields: FieldError[];

  constructor(body: ApiErrorBody) {
    super(body.message);
    this.name = "TicketApiError";
    this.status = body.status;
    this.errorCode = body.errorCode;
    this.fields = body.fields ?? [];
  }
}

export interface TicketApi {
  listTickets(): Promise<Ticket[]>;
  listByStatus(status: TicketStatus): Promise<Ticket[]>;
  searchByKeyword(keyword: string): Promise<Ticket[]>;
  getTicket(id: string): Promise<TicketDetails>;
  createTicket(request: CreateTicketRequest): Promise<Ticket>;
  updateTicket(id: string, request: UpdateTicketRequest): Promise<Ticket>;
  addComment(id: string, request: AddCommentRequest): Promise<Comment>;
  changeStatus(id: string, status: TicketStatus): Promise<Ticket>;
}
