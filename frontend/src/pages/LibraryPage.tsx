import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { api, formatDuration } from "../api";
import type { PlaylistSummary, Track } from "../types";

export function LibraryPage() {
  const [q, setQ] = useState("");
  const [debounced, setDebounced] = useState("");
  const [genre, setGenre] = useState("");
  const [genres, setGenres] = useState<string[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(25);
  const [rows, setRows] = useState<Track[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [playlists, setPlaylists] = useState<PlaylistSummary[]>([]);
  const [adding, setAdding] = useState<Track | null>(null);
  const [playlistId, setPlaylistId] = useState("");
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(q), 300);
    return () => window.clearTimeout(t);
  }, [q]);

  useEffect(() => {
    api.genres().then(setGenres).catch(() => setGenres([]));
    api.playlists().then(setPlaylists).catch(() => setPlaylists([]));
  }, []);

  const params = useMemo(() => {
    const p = new URLSearchParams({ page: String(page), size: String(size) });
    if (debounced) p.set("q", debounced);
    if (genre) p.set("genre", genre);
    return p;
  }, [debounced, genre, page, size]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    api
      .tracks(params)
      .then((result) => {
        if (cancelled) return;
        setRows(result.content);
        setTotal(result.totalElements);
      })
      .catch((err: Error) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [params]);

  async function confirmAdd() {
    if (!adding || !playlistId) return;
    await api.addItem(playlistId, adding.id);
    setToast(`Added “${adding.title}” to playlist`);
    setAdding(null);
    const next = await api.playlists();
    setPlaylists(next);
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Library
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Search the seeded catalog, then drop a track onto a playlist.
      </Typography>
      <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap", mb: 2 }}>
        <TextField
          label="Search title, artist, album"
          value={q}
          onChange={(e) => {
            setPage(0);
            setQ(e.target.value);
          }}
          sx={{ minWidth: 260, flexGrow: 1 }}
        />
        <FormControl sx={{ minWidth: 180 }}>
          <InputLabel>Genre</InputLabel>
          <Select
            label="Genre"
            value={genre}
            onChange={(e) => {
              setPage(0);
              setGenre(e.target.value);
            }}
          >
            <MenuItem value="">All genres</MenuItem>
            {genres.map((g) => (
              <MenuItem key={g} value={g}>
                {g}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
          <CircularProgress />
        </Box>
      ) : rows.length === 0 ? (
        <Alert severity="info">No tracks match those filters.</Alert>
      ) : (
        <>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Title</TableCell>
                <TableCell>Artist</TableCell>
                <TableCell>Album</TableCell>
                <TableCell>Genre</TableCell>
                <TableCell>Year</TableCell>
                <TableCell>BPM</TableCell>
                <TableCell>Length</TableCell>
                <TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((track) => (
                <TableRow key={track.id} hover>
                  <TableCell>{track.title}</TableCell>
                  <TableCell>{track.artist}</TableCell>
                  <TableCell>{track.album}</TableCell>
                  <TableCell>{track.genre}</TableCell>
                  <TableCell>{track.releaseYear}</TableCell>
                  <TableCell>{track.bpm}</TableCell>
                  <TableCell>{formatDuration(track.durationSec)}</TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => setAdding(track)}>
                      Add
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div"
            count={total}
            page={page}
            onPageChange={(_, next) => setPage(next)}
            rowsPerPage={size}
            onRowsPerPageChange={(e) => {
              setSize(parseInt(e.target.value, 10));
              setPage(0);
            }}
            rowsPerPageOptions={[10, 25, 50]}
          />
        </>
      )}
      <Dialog open={Boolean(adding)} onClose={() => setAdding(null)} fullWidth maxWidth="sm">
        <DialogTitle>Add to playlist</DialogTitle>
        <DialogContent>
          {playlists.length === 0 ? (
            <Alert severity="info" sx={{ mt: 1 }}>
              Create a playlist first from the Playlists page.
            </Alert>
          ) : (
            <FormControl fullWidth sx={{ mt: 1 }}>
              <InputLabel>Playlist</InputLabel>
              <Select label="Playlist" value={playlistId} onChange={(e) => setPlaylistId(e.target.value)}>
                {playlists.map((p) => (
                  <MenuItem key={p.id} value={p.id}>
                    {p.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAdding(null)}>Cancel</Button>
          <Button disabled={!playlistId || playlists.length === 0} onClick={() => void confirmAdd()}>
            Add track
          </Button>
        </DialogActions>
      </Dialog>
      <Snackbar open={Boolean(toast)} autoHideDuration={3000} onClose={() => setToast(null)} message={toast} />
    </Box>
  );
}
