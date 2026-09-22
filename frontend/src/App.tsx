import { useCallback, useMemo, useState } from "react";
import { TicketApiProvider } from "./api/TicketApiContext";
import { createHttpTicketApi } from "./api/httpTicketApi";
import type { TicketApi } from "./api/types";
import { TicketCreateView } from "./views/TicketCreateView";
import { TicketDetailView } from "./views/TicketDetailView";
import { TicketListView } from "./views/TicketListView";
import { TicketNotFoundView } from "./views/TicketNotFoundView";

type Route =
  | { name: "list" }
  | { name: "create" }
  | { name: "detail"; id: string }
  | { name: "not-found" };

interface AppProps {
  ticketApi?: TicketApi;
}

export default function App({ ticketApi }: AppProps = {}) {
  const api = useMemo(
    () => ticketApi ?? createHttpTicketApi(import.meta.env.VITE_API_BASE_URL ?? ""),
    [ticketApi],
  );
  const [route, setRoute] = useState<Route>({ name: "list" });
  const showNotFound = useCallback(() => setRoute({ name: "not-found" }), []);

  return (
    <TicketApiProvider value={api}>
      <main className="app">
        {route.name === "list" && (
          <TicketListView
            onCreate={() => setRoute({ name: "create" })}
            onOpen={(id) => setRoute({ name: "detail", id })}
          />
        )}
        {route.name === "create" && (
          <TicketCreateView
            onCancel={() => setRoute({ name: "list" })}
            onCreated={(id) => setRoute({ name: "detail", id })}
          />
        )}
        {route.name === "detail" && (
          <TicketDetailView
            ticketId={route.id}
            onBack={() => setRoute({ name: "list" })}
            onNotFound={showNotFound}
          />
        )}
        {route.name === "not-found" && (
          <TicketNotFoundView onBack={() => setRoute({ name: "list" })} />
        )}
      </main>
    </TicketApiProvider>
  );
}
