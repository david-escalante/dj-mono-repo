package io.beatsync.web.dto;

import io.beatsync.domain.Playlist;
import io.beatsync.domain.PlaylistItem;
import io.beatsync.domain.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class PlaylistDtos {

    private PlaylistDtos() {
    }

    public record CreatePlaylistRequest(
            @NotBlank String name,
            String description
    ) {
    }

    public record UpdatePlaylistRequest(
            String name,
            String description
    ) {
    }

    public record AddItemRequest(
            @NotBlank String trackId,
            String afterItemId
    ) {
    }

    public record MoveItemRequest(
            String afterItemId,
            Integer position
    ) {
    }

    public record PlaylistSummary(
            String id,
            String name,
            String description,
            int trackCount,
            Instant updatedAt
    ) {
        public static PlaylistSummary from(Playlist playlist) {
            return new PlaylistSummary(
                    playlist.getId(),
                    playlist.getName(),
                    playlist.getDescription(),
                    playlist.getItems() == null ? 0 : playlist.getItems().size(),
                    playlist.getUpdatedAt()
            );
        }
    }

    public record PlaylistItemResponse(
            String itemId,
            int position,
            TrackResponse track
    ) {
    }

    public record PlaylistDetail(
            String id,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt,
            List<PlaylistItemResponse> items
    ) {
        public static PlaylistDetail from(Playlist playlist, Map<String, Track> tracks) {
            List<PlaylistItemResponse> items = playlist.getItems().stream()
                    .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                    .map(item -> new PlaylistItemResponse(
                            item.getItemId(),
                            item.getPosition(),
                            trackOrStub(item, tracks)
                    ))
                    .toList();
            return new PlaylistDetail(
                    playlist.getId(),
                    playlist.getName(),
                    playlist.getDescription(),
                    playlist.getCreatedAt(),
                    playlist.getUpdatedAt(),
                    items
            );
        }

        private static TrackResponse trackOrStub(PlaylistItem item, Map<String, Track> tracks) {
            Track track = tracks.get(item.getTrackId());
            if (track == null) {
                return new TrackResponse(item.getTrackId(), "(missing track)", "", "", "", 0, 0, 0, 0);
            }
            return TrackResponse.from(track);
        }
    }
}
