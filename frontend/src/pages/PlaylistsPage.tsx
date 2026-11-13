import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { Link as RouterLink } from "react-router-dom";
import { api } from "../api";
import type { PlaylistSummary } from "../types";

export function PlaylistsPage() {
  const [items, setItems] = useState<PlaylistSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setItems(await api.playlists());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load playlists");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function create() {
    await api.createPlaylist(name, description);
    setOpen(false);
    setName("");
    setDescription("");
    await load();
  }

  async function remove(id: string) {
    await api.deletePlaylist(id);
    await load();
  }

  return (
    <Box>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2, gap: 2, flexWrap: "wrap" }}>
        <div>
          <Typography variant="h5">Playlists</Typography>
          <Typography color="text.secondary">Build a set, then open it to mix by BPM.</Typography>
        </div>
        <Button variant="contained" onClick={() => setOpen(true)}>
          New playlist
        </Button>
      </Box>
      {error && <Alert severity="error">{error}</Alert>}
      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
          <CircularProgress />
        </Box>
      ) : items.length === 0 ? (
        <Alert severity="info">No playlists yet. Create one to start a mix.</Alert>
      ) : (
        <Grid container spacing={2}>
          {items.map((playlist) => (
            <Grid key={playlist.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <Card>
                <CardContent>
                  <Typography variant="h6">{playlist.name}</Typography>
                  <Typography color="text.secondary" sx={{ minHeight: 48 }}>
                    {playlist.description || "No description"}
                  </Typography>
                  <Typography variant="body2">{playlist.trackCount} tracks</Typography>
                </CardContent>
                <CardActions>
                  <Button component={RouterLink} to={`/playlists/${playlist.id}`}>
                    Open
                  </Button>
                  <Button color="error" onClick={() => void remove(playlist.id)}>
                    Delete
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth>
        <DialogTitle>New playlist</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Name"
            fullWidth
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <TextField
            margin="dense"
            label="Description"
            fullWidth
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button disabled={!name.trim()} onClick={() => void create()}>
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
