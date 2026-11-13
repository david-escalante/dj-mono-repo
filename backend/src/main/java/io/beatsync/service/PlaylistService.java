package io.beatsync.service;

import io.beatsync.domain.Playlist;
import io.beatsync.domain.PlaylistItem;
import io.beatsync.domain.Track;
import io.beatsync.repo.PlaylistRepository;
import io.beatsync.repo.TrackRepository;
import io.beatsync.web.BadRequestException;
import io.beatsync.web.NotFoundException;
import io.beatsync.web.dto.PlaylistDtos.AddItemRequest;
import io.beatsync.web.dto.PlaylistDtos.CreatePlaylistRequest;
import io.beatsync.web.dto.PlaylistDtos.MoveItemRequest;
import io.beatsync.web.dto.PlaylistDtos.PlaylistDetail;
import io.beatsync.web.dto.PlaylistDtos.PlaylistSummary;
import io.beatsync.web.dto.PlaylistDtos.UpdatePlaylistRequest;
import io.beatsync.web.dto.SuggestionResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PlaylistService {

    private final PlaylistRepository playlists;
    private final TrackRepository tracks;

    public PlaylistService(PlaylistRepository playlists, TrackRepository tracks) {
        this.playlists = playlists;
        this.tracks = tracks;
    }

    public List<PlaylistSummary> list() {
        return playlists.findAll().stream()
                .sorted(Comparator.comparing(Playlist::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(PlaylistSummary::from)
                .toList();
    }

    public PlaylistDetail get(String id) {
        return toDetail(requirePlaylist(id));
    }

    public PlaylistDetail create(CreatePlaylistRequest request) {
        Instant now = Instant.now();
        Playlist playlist = new Playlist();
        playlist.setName(request.name().trim());
        playlist.setDescription(blankToNull(request.description()));
        playlist.setItems(new ArrayList<>());
        playlist.setCreatedAt(now);
        playlist.setUpdatedAt(now);
        return toDetail(playlists.save(playlist));
    }

    public PlaylistDetail update(String id, UpdatePlaylistRequest request) {
        Playlist playlist = requirePlaylist(id);
        if (request.name() != null && !request.name().isBlank()) {
            playlist.setName(request.name().trim());
        }
        if (request.description() != null) {
            playlist.setDescription(blankToNull(request.description()));
        }
        touch(playlist);
        return toDetail(playlists.save(playlist));
    }

    public void delete(String id) {
        if (!playlists.existsById(id)) {
            throw new NotFoundException("Playlist not found: " + id);
        }
        playlists.deleteById(id);
    }

    public PlaylistDetail addItem(String playlistId, AddItemRequest request) {
        Playlist playlist = requirePlaylist(playlistId);
        Track track = tracks.findById(request.trackId())
                .orElseThrow(() -> new NotFoundException("Track not found: " + request.trackId()));
        List<PlaylistItem> items = sorted(playlist);
        int insertAt = items.size();
        if (request.afterItemId() != null && !request.afterItemId().isBlank()) {
            insertAt = indexOf(items, request.afterItemId()) + 1;
        }
        items.add(insertAt, new PlaylistItem(UUID.randomUUID().toString(), track.getId(), insertAt));
        reindex(items);
        playlist.setItems(items);
        touch(playlist);
        return toDetail(playlists.save(playlist));
    }

    public PlaylistDetail removeItem(String playlistId, String itemId) {
        Playlist playlist = requirePlaylist(playlistId);
        List<PlaylistItem> items = sorted(playlist);
        int index = indexOf(items, itemId);
        items.remove(index);
        reindex(items);
        playlist.setItems(items);
        touch(playlist);
        return toDetail(playlists.save(playlist));
    }

    public PlaylistDetail moveItem(String playlistId, String itemId, MoveItemRequest request) {
        Playlist playlist = requirePlaylist(playlistId);
        List<PlaylistItem> items = sorted(playlist);
        int from = indexOf(items, itemId);
        PlaylistItem moving = items.remove(from);
        int to;
        if (request.position() != null) {
            to = Math.max(0, Math.min(request.position(), items.size()));
        } else if (request.afterItemId() != null && !request.afterItemId().isBlank()) {
            to = indexOf(items, request.afterItemId()) + 1;
        } else {
            to = 0;
        }
        items.add(to, moving);
        reindex(items);
        playlist.setItems(items);
        touch(playlist);
        return toDetail(playlists.save(playlist));
    }

    public List<SuggestionResponse> suggestions(String playlistId, String currentTrackId, int limit) {
        Playlist playlist = requirePlaylist(playlistId);
        Map<String, Track> byId = tracksById(playlist);
        List<Track> playlistTracks = playlist.getItems().stream()
                .map(item -> byId.get(item.getTrackId()))
                .filter(t -> t != null)
                .toList();
        Track current = null;
        if (currentTrackId != null && !currentTrackId.isBlank()) {
            current = tracks.findById(currentTrackId)
                    .orElseThrow(() -> new NotFoundException("Track not found: " + currentTrackId));
        } else if (!playlistTracks.isEmpty()) {
            current = playlistTracks.getLast();
        }
        List<Track> catalog = tracks.findAll();
        return BeatSyncService.suggest(current, playlistTracks, catalog, limit);
    }

    public PlaylistDetail smoothOrder(String playlistId) {
        Playlist playlist = requirePlaylist(playlistId);
        Map<String, Track> byId = tracksById(playlist);
        playlist.setItems(BeatSyncService.smoothOrder(sorted(playlist), byId));
        touch(playlist);
        return toDetail(playlists.save(playlist));
    }

    private PlaylistDetail toDetail(Playlist playlist) {
        return PlaylistDetail.from(playlist, tracksById(playlist));
    }

    private Map<String, Track> tracksById(Playlist playlist) {
        List<String> ids = playlist.getItems().stream().map(PlaylistItem::getTrackId).distinct().toList();
        Map<String, Track> map = new HashMap<>();
        tracks.findAllById(ids).forEach(track -> map.put(track.getId(), track));
        return map;
    }

    private Playlist requirePlaylist(String id) {
        return playlists.findById(id).orElseThrow(() -> new NotFoundException("Playlist not found: " + id));
    }

    private static List<PlaylistItem> sorted(Playlist playlist) {
        List<PlaylistItem> items = new ArrayList<>(playlist.getItems() == null ? List.of() : playlist.getItems());
        items.sort(Comparator.comparingInt(PlaylistItem::getPosition));
        return items;
    }

    private static int indexOf(List<PlaylistItem> items, String itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemId().equals(itemId)) {
                return i;
            }
        }
        throw new BadRequestException("Playlist item not found: " + itemId);
    }

    private static void reindex(List<PlaylistItem> items) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i);
        }
    }

    private static void touch(Playlist playlist) {
        playlist.setUpdatedAt(Instant.now());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
