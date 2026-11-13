export type Track = {
  id: string;
  title: string;
  artist: string;
  album: string;
  genre: string;
  releaseYear: number;
  durationSec: number;
  bpm: number;
  energy: number;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PlaylistSummary = {
  id: string;
  name: string;
  description?: string | null;
  trackCount: number;
  updatedAt?: string | null;
};

export type PlaylistItem = {
  itemId: string;
  position: number;
  track: Track;
};

export type PlaylistDetail = {
  id: string;
  name: string;
  description?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  items: PlaylistItem[];
};

export type Suggestion = Track & {
  score: number;
  reason: string;
};
