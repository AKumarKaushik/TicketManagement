import { FormEvent, useEffect, useState } from "react";
import { ErrorBanner, fieldMessage } from "../components/ErrorBanner";
import { useTicketApi } from "../api/TicketApiContext";
import { TicketApiError, type TicketDetails, type TicketStatus } from "../api/types";
import { nextStatuses } from "../domain/statusTransitions";

interface TicketDetailViewProps {
  ticketId: string;
  onBack: () => void;
  onNotFound: () => void;
}

export function TicketDetailView({ ticketId, onBack, onNotFound }: TicketDetailViewProps) {
  const api = useTicketApi();
  const [ticket, setTicket] = useState<TicketDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<TicketApiError | string | null>(null);
  const [lifecycleError, setLifecycleError] = useState<string | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState("");
  const [assignee, setAssignee] = useState("");
  const [content, setContent] = useState("");
  const [author, setAuthor] = useState("");
  const [saving, setSaving] = useState(false);
  const [commenting, setCommenting] = useState(false);
  const [changingStatus, setChangingStatus] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    setLifecycleError(null);
    api
      .getTicket(ticketId)
      .then((result) => {
        if (cancelled) {
          return;
        }
        setTicket(result);
        setTitle(result.title);
        setDescription(result.description ?? "");
        setPriority(result.priority ?? "");
        setAssignee(result.assignee ?? "");
        setLoading(false);
      })
      .catch((err: unknown) => {
        if (cancelled) {
          return;
        }
        setLoading(false);
        if (err instanceof TicketApiError && err.errorCode === "NOT_FOUND") {
          onNotFound();
          return;
        }
        setError(err instanceof TicketApiError ? err : "The ticket could not be loaded.");
      });
    return () => {
      cancelled = true;
    };
  }, [api, ticketId, onNotFound]);

  function applyTicket(updated: { title: string; description: string | null; priority: string | null; assignee: string | null; status: TicketStatus }) {
    setTicket((current) =>
      current
        ? {
            ...current,
            title: updated.title,
            description: updated.description,
            priority: updated.priority,
            assignee: updated.assignee,
            status: updated.status,
          }
        : current,
    );
    setTitle(updated.title);
    setDescription(updated.description ?? "");
    setPriority(updated.priority ?? "");
    setAssignee(updated.assignee ?? "");
  }

  async function saveFields(event: FormEvent) {
    event.preventDefault();
    if (!ticket) {
      return;
    }
    if (!title.trim()) {
      setError("Title must be present and not blank.");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const updated = await api.updateTicket(ticket.id, {
        title: title.trim(),
        description,
        priority,
        assignee,
      });
      applyTicket(updated);
    } catch (err) {
      if (err instanceof TicketApiError && err.errorCode === "NOT_FOUND") {
        onNotFound();
        return;
      }
      setError(err instanceof TicketApiError ? err : "The ticket could not be updated.");
      if (ticket) {
        setTitle(ticket.title);
        setDescription(ticket.description ?? "");
        setPriority(ticket.priority ?? "");
        setAssignee(ticket.assignee ?? "");
      }
    } finally {
      setSaving(false);
    }
  }

  async function saveComment(event: FormEvent) {
    event.preventDefault();
    if (!ticket) {
      return;
    }
    setCommenting(true);
    setError(null);
    try {
      const comment = await api.addComment(ticket.id, { content, author });
      setTicket({ ...ticket, comments: [...ticket.comments, comment] });
      setContent("");
      setAuthor("");
    } catch (err) {
      if (err instanceof TicketApiError && err.errorCode === "NOT_FOUND") {
        onNotFound();
        return;
      }
      setError(err instanceof TicketApiError ? err : "The comment could not be added.");
    } finally {
      setCommenting(false);
    }
  }

  async function changeStatus(target: TicketStatus) {
    if (!ticket) {
      return;
    }
    setChangingStatus(true);
    setError(null);
    setLifecycleError(null);
    try {
      const updated = await api.changeStatus(ticket.id, target);
      applyTicket(updated);
    } catch (err) {
      if (err instanceof TicketApiError && err.errorCode === "NOT_FOUND") {
        onNotFound();
        return;
      }
      if (err instanceof TicketApiError && err.errorCode === "ILLEGAL_TRANSITION") {
        setLifecycleError(err.message);
        return;
      }
      setError(err instanceof TicketApiError ? err : "The status could not be changed.");
    } finally {
      setChangingStatus(false);
    }
  }

  if (loading) {
    return <p role="status">Loading ticket…</p>;
  }

  if (!ticket) {
    return (
      <section>
        {error && <ErrorBanner title="Could not load ticket" error={error} />}
        <button type="button" onClick={onBack}>
          Back to list
        </button>
      </section>
    );
  }

  const offered = nextStatuses(ticket.status);
  const fields = error instanceof TicketApiError ? error.fields : [];

  return (
    <section>
      <header className="page-header">
        <h1>Ticket {ticket.id}</h1>
        <button type="button" onClick={onBack}>
          Back to list
        </button>
      </header>

      {error && <ErrorBanner title="Request failed" error={error} />}
      {lifecycleError && (
        <div role="alert" className="banner banner-lifecycle">
          <p>
            <strong>Status could not be changed:</strong> {lifecycleError}
          </p>
        </div>
      )}

      <dl className="ticket-meta">
        <dt>Identity</dt>
        <dd>{ticket.id}</dd>
        <dt>Status</dt>
        <dd>{ticket.status}</dd>
      </dl>

      <form onSubmit={saveFields} noValidate>
        <div className="field">
          <label htmlFor="edit-title">Title</label>
          <input
            id="edit-title"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
          />
          {fieldMessage(fields, "title") && (
            <p className="field-error">{fieldMessage(fields, "title")}</p>
          )}
        </div>
        <div className="field">
          <label htmlFor="edit-description">Description</label>
          <textarea
            id="edit-description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="edit-priority">Priority</label>
          <input
            id="edit-priority"
            value={priority}
            onChange={(event) => setPriority(event.target.value)}
            aria-describedby="edit-priority-help"
          />
          <p id="edit-priority-help" className="hint">
            Recognized priority values are not specified yet.
          </p>
        </div>
        <div className="field">
          <label htmlFor="edit-assignee">Assignee</label>
          <input
            id="edit-assignee"
            value={assignee}
            onChange={(event) => setAssignee(event.target.value)}
          />
        </div>
        <button type="submit" disabled={saving}>
          {saving ? "Saving…" : "Save fields"}
        </button>
      </form>

      <section className="status-actions">
        <h2>Change status</h2>
        {offered.length === 0 ? (
          <p>This ticket is terminal. No further status changes are offered.</p>
        ) : (
          <div className="actions">
            {offered.map((status) => (
              <button
                key={status}
                type="button"
                disabled={changingStatus}
                onClick={() => changeStatus(status)}
              >
                Change to {status}
              </button>
            ))}
          </div>
        )}
      </section>

      <section>
        <h2>Comments</h2>
        {ticket.comments.length === 0 ? (
          <p>No comments yet.</p>
        ) : (
          <ul className="comments">
            {ticket.comments.map((comment, index) => (
              <li key={`${comment.creationTime}-${index}`}>
                <p>{comment.content}</p>
                <p className="meta">
                  {comment.author} · {comment.creationTime}
                </p>
              </li>
            ))}
          </ul>
        )}

        <form onSubmit={saveComment} noValidate>
          <div className="field">
            <label htmlFor="comment-content">Comment</label>
            <textarea
              id="comment-content"
              value={content}
              onChange={(event) => setContent(event.target.value)}
            />
            {fieldMessage(fields, "content") && (
              <p className="field-error">{fieldMessage(fields, "content")}</p>
            )}
          </div>
          <div className="field">
            <label htmlFor="comment-author">Author</label>
            <input
              id="comment-author"
              value={author}
              onChange={(event) => setAuthor(event.target.value)}
            />
            {fieldMessage(fields, "author") && (
              <p className="field-error">{fieldMessage(fields, "author")}</p>
            )}
          </div>
          <button type="submit" disabled={commenting}>
            {commenting ? "Adding…" : "Add comment"}
          </button>
        </form>
      </section>
    </section>
  );
}
