import { FormEvent, useState } from "react";
import { ErrorBanner, fieldMessage } from "../components/ErrorBanner";
import { useTicketApi } from "../api/TicketApiContext";
import { TicketApiError } from "../api/types";

interface TicketCreateViewProps {
  onCancel: () => void;
  onCreated: (id: string) => void;
}

export function TicketCreateView({ onCancel, onCreated }: TicketCreateViewProps) {
  const api = useTicketApi();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState("");
  const [assignee, setAssignee] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<TicketApiError | string | null>(null);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!title.trim()) {
      setError("Title must be present and not blank.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const body: { title: string; description?: string; priority?: string; assignee?: string } = {
        title: title.trim(),
      };
      if (description.trim()) {
        body.description = description;
      }
      if (priority.trim()) {
        body.priority = priority.trim();
      }
      if (assignee.trim()) {
        body.assignee = assignee.trim();
      }
      const created = await api.createTicket(body);
      onCreated(created.id);
    } catch (err) {
      setError(err instanceof TicketApiError ? err : "The ticket could not be created.");
    } finally {
      setSubmitting(false);
    }
  }

  const fields = error instanceof TicketApiError ? error.fields : [];

  return (
    <section>
      <header className="page-header">
        <h1>Create ticket</h1>
        <button type="button" onClick={onCancel} disabled={submitting}>
          Back to list
        </button>
      </header>

      {error && <ErrorBanner title="Could not create ticket" error={error} />}

      <form onSubmit={onSubmit} noValidate>
        <div className="field">
          <label htmlFor="title">Title</label>
          <input
            id="title"
            name="title"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            aria-invalid={Boolean(fieldMessage(fields, "title"))}
            aria-describedby={fieldMessage(fields, "title") ? "title-error" : undefined}
          />
          {fieldMessage(fields, "title") && (
            <p id="title-error" className="field-error">
              {fieldMessage(fields, "title")}
            </p>
          )}
        </div>

        <div className="field">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
          <p className="hint">Whether description is required is not specified yet.</p>
        </div>

        <div className="field">
          <label htmlFor="priority">Priority</label>
          <input
            id="priority"
            name="priority"
            value={priority}
            onChange={(event) => setPriority(event.target.value)}
            aria-describedby="priority-help"
          />
          <p id="priority-help" className="hint">
            Recognized priority values are not specified yet. This is free text, not a status.
          </p>
        </div>

        <div className="field">
          <label htmlFor="assignee">Assignee</label>
          <input
            id="assignee"
            name="assignee"
            value={assignee}
            onChange={(event) => setAssignee(event.target.value)}
          />
        </div>

        <div className="actions">
          <button type="submit" disabled={submitting}>
            {submitting ? "Creating…" : "Create ticket"}
          </button>
        </div>
      </form>
    </section>
  );
}
