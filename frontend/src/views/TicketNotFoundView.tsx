interface TicketNotFoundViewProps {
  onBack: () => void;
}

export function TicketNotFoundView({ onBack }: TicketNotFoundViewProps) {
  return (
    <section>
      <h1>Ticket not found</h1>
      <p role="status">The requested ticket does not exist. This is not an empty ticket.</p>
      <button type="button" onClick={onBack}>
        Back to list
      </button>
    </section>
  );
}
