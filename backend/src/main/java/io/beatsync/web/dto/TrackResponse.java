package io.beatsync.web.dto;

import io.beatsync.domain.Track;

public record TrackResponse(
        String id,
        String title,
        String artist,
        String album,
        String genre,
        int releaseYear,
        int durationSec,
        int bpm,
        int energy
) {
    public static TrackResponse from(Track track) {
        return new TrackResponse(
                track.getId(),
                track.getTitle(),
                track.getArtist(),
                track.getAlbum(),
                track.getGenre(),
                track.getReleaseYear(),
                track.getDurationSec(),
                track.getBpm(),
                track.getEnergy()
        );
    }
}
