import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Paper,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, formatDuration } from "../api";
import { PlayerBar } from "../components/PlayerBar";
import type { PlaylistDetail, Suggestion } from "../types";

export function PlaylistDetailPage() {
  const { id } = useParams();
  const [playlist, setPlaylist] = useState<PlaylistDetail | null>(null);
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [index, setIndex] = useState(0);
  const [playing, setPlaying] = useState(false);

  async function load(next?: PlaylistDetail) {
    if (!id) return;
    setError(null);
    const detail = next ?? (await api.playlist(id));
    setPlaylist(detail);
    const current = detail.items[Math.min(index, Math.max(detail.items.length - 1, 0))]?.track.id;
    try {
      setSuggestions(await api.suggestions(id, current));
    } catch {
      setSuggestions([]);
    }
  }

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    load()
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }
  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }
  if (!playlist || !id) {
    return <Alert severity="info">Playlist not found.</Alert>;
  }

  const tracks = playlist.items.map((item) => item.track);

  async function move(itemId: string, position: number) {
    const next = await api.moveItem(id!, itemId, position);
    await load(next);
  }

  return (
    <Box>
      <Typography variant="h5">{playlist.name}</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        {playlist.description || "Queue tracks, then let Beat-Sync suggest the next mix."}
      </Typography>
      <Button
        variant="outlined"
        sx={{ mb: 2 }}
        disabled={playlist.items.length < 2}
        onClick={() => {
          void (async () => {
            const next = await api.smoothOrder(id);
            await load(next);
          })();
        }}
      >
        Smooth mix
      </Button>
      {playlist.items.length === 0 ? (
        <Alert severity="info" sx={{ mb: 2 }}>
          This playlist is empty. Add tracks from the library.
        </Alert>
      ) : (
        <List>
          {playlist.items.map((item, i) => (
            <ListItem
              key={item.itemId}
              sx={{ bgcolor: i === index ? "action.selected" : "transparent" }}
              secondaryAction={
                <Box>
                  <IconButton aria-label="Move up" disabled={i === 0} onClick={() => void move(item.itemId, i - 1)}>
                    <ArrowUpwardIcon />
                  </IconButton>
                  <IconButton
                    aria-label="Move down"
                    disabled={i === playlist.items.length - 1}
                    onClick={() => void move(item.itemId, i + 1)}
                  >
                    <ArrowDownwardIcon />
                  </IconButton>
                  <Button
                    size="small"
                    color="error"
                    onClick={() => {
                      void (async () => {
                        const next = await api.removeItem(id, item.itemId);
                        await load(next);
                      })();
                    }}
                  >
                    Remove
                  </Button>
                </Box>
              }
            >
              <ListItemText
                primary={`${item.position + 1}. ${item.track.title}`}
                secondary={`${item.track.artist} · ${item.track.bpm} BPM · ${item.track.genre} · ${formatDuration(item.track.durationSec)}`}
              />
            </ListItem>
          ))}
        </List>
      )}
      <PlayerBar tracks={tracks} index={index} playing={playing} onIndex={setIndex} onPlaying={setPlaying} />
      <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
        Beat-sync suggestions
      </Typography>
      {suggestions.length === 0 ? (
        <Alert severity="info">Suggestions appear once the catalog is seeded and this playlist has a target track.</Alert>
      ) : (
        <Box sx={{ display: "grid", gap: 1 }}>
          {suggestions.map((s) => (
            <Paper key={s.id} sx={{ p: 2, display: "flex", gap: 2, alignItems: "center", flexWrap: "wrap" }}>
              <Box sx={{ flexGrow: 1 }}>
                <Typography>
                  {s.title} — {s.artist}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {s.reason}
                </Typography>
                <Chip size="small" label={`${s.bpm} BPM`} sx={{ mr: 1, mt: 0.5 }} />
                <Chip size="small" label={s.genre} sx={{ mt: 0.5 }} />
              </Box>
              <Button
                onClick={() => {
                  void (async () => {
                    const next = await api.addItem(id, s.id);
                    await load(next);
                  })();
                }}
              >
                Add
              </Button>
            </Paper>
          ))}
        </Box>
      )}
    </Box>
  );
}
