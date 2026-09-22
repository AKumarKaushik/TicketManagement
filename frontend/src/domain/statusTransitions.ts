import type { TicketStatus } from "../api/types";

const NEXT: Record<TicketStatus, readonly TicketStatus[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
  CANCELLED: [],
};

export function nextStatuses(current: TicketStatus): readonly TicketStatus[] {
  return NEXT[current];
}

export function isTerminal(status: TicketStatus): boolean {
  return status === "CLOSED" || status === "CANCELLED";
}
