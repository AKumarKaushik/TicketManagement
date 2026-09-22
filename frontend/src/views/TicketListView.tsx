import { FormEvent, useEffect, useState } from "react";
import { ErrorBanner } from "../components/ErrorBanner";
import { useTicketApi } from "../api/TicketApiContext";
import { TICKET_STATUSES, TicketApiError, type Ticket, type TicketStatus } from "../api/types";

interface TicketListViewProps {
  onCreate: () => void;
  onOpen: (id: string) => void;
}

type ListMode = { kind: "all" } | { kind: "status"; status: TicketStatus } | { kind: "keyword"; keyword: string };

export function TicketListView({ onCreate, onOpen }: TicketListViewProps) {
  const api = useTicketApi();
  const [mode, setMode] = useState<ListMode>({ kind: "all" });
  const [keyword, setKeyword] = useState("");
  const [tickets, setTickets] = useState<Ticket[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<TicketApiError | string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    const load =
      mode.kind === "status"
        ? api.listByStatus(mode.status)
        : mode.kind === "keyword"
          ? api.searchByKeyword(mode.keyword)
          : api.listTickets();
    load
      .then((result) => {
        if (!cancelled) {
          setTickets(result);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setTickets(null);
          setError(err instanceof TicketApiError ? err : "The ticket list could not be loaded.");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [api, mode]);

  function applySearch(event: FormEvent) {
    event.preventDefault();
    const value = keyword.trim();
    if (!value) {
      return;
    }
    setMode({ kind: "keyword", keyword: value });
  }

  function applyFilter(value: string) {
    if (value === "") {
      setMode({ kind: "all" });
      return;
    }
    setMode({ kind: "status", status: value as TicketStatus });
  }

  return (
    <section>
      <header className="page-header">
        <h1>Tickets</h1>
        <button type="button" onClick={onCreate}>
          Create ticket
        </button>
      </header>

      <div className="toolbar">
        <form onSubmit={applySearch} className="search-form">
          <label htmlFor="keyword">Keyword</label>
          <input
            id="keyword"
            name="keyword"
            type="search"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            aria-describedby="keyword-help"
          />
          <button type="submit" disabled={!keyword.trim()}>
            Search
          </button>
          <p id="keyword-help" className="hint">
            Matching rules are not specified yet. A blank search is not submitted.
          </p>
        </form>

        <div>
          <label htmlFor="status-filter">Status filter</label>
          <select
            id="status-filter"
            value={mode.kind === "status" ? mode.status : ""}
            onChange={(event) => applyFilter(event.target.value)}
          >
            <option value="">All statuses</option>
            {TICKET_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading && <p role="status">Loading tickets…</p>}
      {error && <ErrorBanner title="Could not load tickets" error={error} />}
      {!loading && !error && tickets && tickets.length === 0 && (
        <p role="status">No tickets to display.</p>
      )}
      {!loading && !error && tickets && tickets.length > 0 && (
        <ul className="ticket-list">
          {tickets.map((ticket) => (
            <li key={ticket.id}>
              <button type="button" className="ticket-link" onClick={() => onOpen(ticket.id)}>
                <span>{ticket.title}</span>
                <span className="meta">
                  {ticket.id} · {ticket.status}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
