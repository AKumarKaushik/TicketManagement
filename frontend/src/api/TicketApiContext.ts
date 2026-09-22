import { createContext, useContext } from "react";
import type { TicketApi } from "./types";

const TicketApiContext = createContext<TicketApi | null>(null);

export const TicketApiProvider = TicketApiContext.Provider;

export function useTicketApi(): TicketApi {
  const api = useContext(TicketApiContext);
  if (!api) {
    throw new Error("TicketApiProvider is required");
  }
  return api;
}
