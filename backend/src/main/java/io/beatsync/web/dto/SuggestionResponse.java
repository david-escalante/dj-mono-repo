package io.beatsync.web.dto;

public record SuggestionResponse(
        String id,
        String title,
        String artist,
        String album,
        String genre,
        int bpm,
        int energy,
        int durationSec,
        int releaseYear,
        double score,
        String reason
) {
}
