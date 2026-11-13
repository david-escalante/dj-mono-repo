import type { PageResponse, PlaylistDetail, PlaylistSummary, Suggestion, Track } from "./types";

const BASE = import.meta.env.VITE_API_BASE_URL ?? "";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
  });
  if (response.status === 204) {
    return undefined as T;
  }
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(body.error ?? `Request failed (${response.status})`);
  }
  return body as T;
}

export const api = {
  tracks: (params: URLSearchParams) => request<PageResponse<Track>>(`/api/tracks?${params}`),
  genres: () => request<string[]>("/api/genres"),
  playlists: () => request<PlaylistSummary[]>("/api/playlists"),
  playlist: (id: string) => request<PlaylistDetail>(`/api/playlists/${id}`),
  createPlaylist: (name: string, description?: string) =>
    request<PlaylistDetail>("/api/playlists", {
      method: "POST",
      body: JSON.stringify({ name, description }),
    }),
  updatePlaylist: (id: string, patch: { name?: string; description?: string }) =>
    request<PlaylistDetail>(`/api/playlists/${id}`, {
      method: "PATCH",
      body: JSON.stringify(patch),
    }),
  deletePlaylist: (id: string) =>
    request<void>(`/api/playlists/${id}`, { method: "DELETE" }),
  addItem: (playlistId: string, trackId: string) =>
    request<PlaylistDetail>(`/api/playlists/${playlistId}/items`, {
      method: "POST",
      body: JSON.stringify({ trackId }),
    }),
  removeItem: (playlistId: string, itemId: string) =>
    request<PlaylistDetail>(`/api/playlists/${playlistId}/items/${itemId}`, { method: "DELETE" }),
  moveItem: (playlistId: string, itemId: string, position: number) =>
    request<PlaylistDetail>(`/api/playlists/${playlistId}/items/${itemId}/move`, {
      method: "POST",
      body: JSON.stringify({ position }),
    }),
  suggestions: (playlistId: string, currentTrackId?: string) => {
    const params = new URLSearchParams({ limit: "8" });
    if (currentTrackId) {
      params.set("currentTrackId", currentTrackId);
    }
    return request<Suggestion[]>(`/api/playlists/${playlistId}/suggestions?${params}`);
  },
  smoothOrder: (playlistId: string) =>
    request<PlaylistDetail>(`/api/playlists/${playlistId}/smooth-order`, { method: "POST" }),
};

export function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, "0")}`;
}
