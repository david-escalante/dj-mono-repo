package io.beatsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.beatsync.domain.PlaylistItem;
import io.beatsync.domain.Track;
import io.beatsync.web.dto.SuggestionResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BeatSyncServiceTest {

    @Test
    void prefersCloserBpmAndSameGenre() {
        Track current = track("1", "Now", "A", "Electronic", 128, 4);
        Track closeSame = track("2", "Close Mix", "B", "Electronic", 129, 4);
        Track farOther = track("3", "Ballad", "C", "Folk", 72, 2);
        List<SuggestionResponse> suggestions = BeatSyncService.suggest(
                current,
                List.of(current),
                List.of(closeSame, farOther, current),
                5
        );
        assertThat(suggestions).extracting(SuggestionResponse::id).containsExactly("2", "3");
        assertThat(suggestions.getFirst().reason()).contains("same genre");
    }

    @Test
    void smoothOrderWalksNearestBpm() {
        Track a = track("a", "A", "x", "Pop", 100, 3);
        Track b = track("b", "B", "x", "Pop", 180, 3);
        Track c = track("c", "C", "x", "Pop", 102, 3);
        List<PlaylistItem> items = List.of(
                new PlaylistItem("i1", "a", 0),
                new PlaylistItem("i2", "b", 1),
                new PlaylistItem("i3", "c", 2)
        );
        List<PlaylistItem> ordered = BeatSyncService.smoothOrder(items, Map.of("a", a, "b", b, "c", c));
        assertThat(ordered).extracting(PlaylistItem::getTrackId).containsExactly("a", "c", "b");
        assertThat(ordered).extracting(PlaylistItem::getPosition).containsExactly(0, 1, 2);
    }

    private static Track track(String id, String title, String artist, String genre, int bpm, int energy) {
        Track track = new Track(title, artist, "Album", genre, 2000, 180, bpm, energy);
        track.setId(id);
        return track;
    }
}
