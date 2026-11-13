import LibraryMusicIcon from "@mui/icons-material/LibraryMusic";
import MenuIcon from "@mui/icons-material/Menu";
import QueueMusicIcon from "@mui/icons-material/QueueMusic";
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from "@mui/material";
import { useState } from "react";
import { NavLink, Outlet } from "react-router-dom";

const drawerWidth = 240;

export function Layout() {
  const theme = useTheme();
  const mobile = useMediaQuery(theme.breakpoints.down("md"));
  const [open, setOpen] = useState(false);

  const nav = (
    <List>
      <ListItemButton component={NavLink} to="/" onClick={() => setOpen(false)}>
        <ListItemIcon>
          <LibraryMusicIcon />
        </ListItemIcon>
        <ListItemText primary="Library" />
      </ListItemButton>
      <ListItemButton component={NavLink} to="/playlists" onClick={() => setOpen(false)}>
        <ListItemIcon>
          <QueueMusicIcon />
        </ListItemIcon>
        <ListItemText primary="Playlists" />
      </ListItemButton>
    </List>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar position="fixed" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
        <Toolbar>
          {mobile && (
            <IconButton color="inherit" edge="start" onClick={() => setOpen(true)} sx={{ mr: 1 }}>
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" sx={{ flexGrow: 1, letterSpacing: 0.4 }}>
            Beat-Sync
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Playlist Manager
          </Typography>
        </Toolbar>
      </AppBar>
      <Drawer
        variant={mobile ? "temporary" : "permanent"}
        open={mobile ? open : true}
        onClose={() => setOpen(false)}
        sx={{
          width: drawerWidth,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: "border-box", mt: mobile ? 0 : 0 },
        }}
      >
        <Toolbar />
        {nav}
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 3 }, width: "100%" }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
