import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "dark",
    primary: { main: "#7ee0c8" },
    secondary: { main: "#c9a8ff" },
    background: { default: "#0e1116", paper: "#171c24" },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"IBM Plex Sans", "Segoe UI", sans-serif',
    h5: { fontWeight: 700 },
  },
});
