import type { FieldError, TicketApiError } from "../api/types";

interface ErrorBannerProps {
  title: string;
  error: TicketApiError | string;
}

export function ErrorBanner({ title, error }: ErrorBannerProps) {
  const message = typeof error === "string" ? error : error.message;
  const fields: FieldError[] = typeof error === "string" ? [] : error.fields;
  return (
    <div role="alert" className="banner banner-error">
      <p>
        <strong>{title}:</strong> {message}
      </p>
      {fields.length > 0 && (
        <ul>
          {fields.map((field) => (
            <li key={field.field}>
              {field.field}: {field.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function fieldMessage(fields: FieldError[], name: string): string | undefined {
  return fields.find((field) => field.field === name)?.message;
}
