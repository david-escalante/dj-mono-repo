import { Route, Routes } from "react-router-dom";
import { Layout } from "./components/Layout";
import { LibraryPage } from "./pages/LibraryPage";
import { PlaylistDetailPage } from "./pages/PlaylistDetailPage";
import { PlaylistsPage } from "./pages/PlaylistsPage";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<LibraryPage />} />
        <Route path="/playlists" element={<PlaylistsPage />} />
        <Route path="/playlists/:id" element={<PlaylistDetailPage />} />
      </Route>
    </Routes>
  );
}
