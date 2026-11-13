import PauseIcon from "@mui/icons-material/Pause";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import SkipNextIcon from "@mui/icons-material/SkipNext";
import SkipPreviousIcon from "@mui/icons-material/SkipPrevious";
import { Box, IconButton, Paper, Typography } from "@mui/material";
import type { Track } from "../types";
import { formatDuration } from "../api";

type Props = {
  tracks: Track[];
  index: number;
  playing: boolean;
  onIndex: (index: number) => void;
  onPlaying: (playing: boolean) => void;
};

export function PlayerBar({ tracks, index, playing, onIndex, onPlaying }: Props) {
  if (tracks.length === 0) {
    return (
      <Paper sx={{ p: 2, mt: 2 }}>
        <Typography color="text.secondary">Add tracks to cue a mix. Playback is simulated.</Typography>
      </Paper>
    );
  }
  const current = tracks[Math.min(index, tracks.length - 1)];
  return (
    <Paper sx={{ p: 2, mt: 2, display: "flex", alignItems: "center", gap: 2, flexWrap: "wrap" }}>
      <Box sx={{ flexGrow: 1, minWidth: 180 }}>
        <Typography variant="subtitle1">{current.title}</Typography>
        <Typography variant="body2" color="text.secondary">
          {current.artist} · {current.bpm} BPM · {formatDuration(current.durationSec)}
        </Typography>
      </Box>
      <IconButton
        aria-label="Previous"
        onClick={() => onIndex((index - 1 + tracks.length) % tracks.length)}
      >
        <SkipPreviousIcon />
      </IconButton>
      <IconButton aria-label={playing ? "Pause" : "Play"} color="primary" onClick={() => onPlaying(!playing)}>
        {playing ? <PauseIcon /> : <PlayArrowIcon />}
      </IconButton>
      <IconButton aria-label="Next" onClick={() => onIndex((index + 1) % tracks.length)}>
        <SkipNextIcon />
      </IconButton>
      <Typography variant="caption" color="text.secondary">
        {playing ? "Playing (simulated)" : "Paused"}
      </Typography>
    </Paper>
  );
}
