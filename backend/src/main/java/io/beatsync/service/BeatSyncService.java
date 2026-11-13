package io.beatsync.service;

import io.beatsync.domain.Playlist;
import io.beatsync.domain.PlaylistItem;
import io.beatsync.domain.Track;
import io.beatsync.web.dto.SuggestionResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BeatSyncService {

    private BeatSyncService() {
    }

    public static List<SuggestionResponse> suggest(
            Track current,
            List<Track> playlistTracks,
            List<Track> catalog,
            int limit
    ) {
        int targetBpm = current != null
                ? current.getBpm()
                : averageBpm(playlistTracks);
        String targetGenre = current != null
                ? current.getGenre()
                : majorityGenre(playlistTracks);
        int targetEnergy = current != null
                ? current.getEnergy()
                : 3;

        Set<String> excluded = playlistTracks.stream()
                .map(Track::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (current != null && current.getId() != null) {
            excluded.add(current.getId());
        }

        return catalog.stream()
                .filter(track -> track.getId() == null || !excluded.contains(track.getId()))
                .map(track -> score(track, targetBpm, targetGenre, targetEnergy))
                .sorted(Comparator.comparingDouble(Scored::score).thenComparing(s -> s.track().getTitle()))
                .limit(Math.max(1, limit))
                .map(s -> new SuggestionResponse(
                        s.track().getId(),
                        s.track().getTitle(),
                        s.track().getArtist(),
                        s.track().getAlbum(),
                        s.track().getGenre(),
                        s.track().getBpm(),
                        s.track().getEnergy(),
                        s.track().getDurationSec(),
                        s.track().getReleaseYear(),
                        round(s.score()),
                        s.reason()
                ))
                .toList();
    }

    public static List<PlaylistItem> smoothOrder(List<PlaylistItem> items, Map<String, Track> tracksById) {
        if (items == null || items.size() < 2) {
            return items == null ? List.of() : new ArrayList<>(items);
        }
        List<PlaylistItem> remaining = new ArrayList<>(items);
        List<PlaylistItem> ordered = new ArrayList<>();
        PlaylistItem cursor = remaining.removeFirst();
        ordered.add(cursor);
        while (!remaining.isEmpty()) {
            int cursorBpm = bpmOf(tracksById.get(cursor.getTrackId()));
            int bestIndex = 0;
            int bestDelta = Integer.MAX_VALUE;
            for (int i = 0; i < remaining.size(); i++) {
                int delta = Math.abs(cursorBpm - bpmOf(tracksById.get(remaining.get(i).getTrackId())));
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestIndex = i;
                }
            }
            cursor = remaining.remove(bestIndex);
            ordered.add(cursor);
        }
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setPosition(i);
        }
        return ordered;
    }

    public static int targetBpm(Playlist playlist, List<Track> playlistTracks, Track current) {
        if (current != null) {
            return current.getBpm();
        }
        return averageBpm(playlistTracks);
    }

    private static Scored score(Track track, int targetBpm, String targetGenre, int targetEnergy) {
        int bpmDelta = Math.abs(track.getBpm() - targetBpm);
        double score = bpmDelta;
        List<String> reasons = new ArrayList<>();
        reasons.add(bpmDelta + " BPM from target " + targetBpm);
        if (targetGenre != null && targetGenre.equalsIgnoreCase(track.getGenre())) {
            score -= 4;
            reasons.add("same genre");
        }
        score += Math.abs(track.getEnergy() - targetEnergy) * 0.4;
        return new Scored(track, score, String.join(" · ", reasons));
    }

    private static int averageBpm(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            return 120;
        }
        return (int) Math.round(tracks.stream().mapToInt(Track::getBpm).average().orElse(120));
    }

    private static String majorityGenre(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        return tracks.stream()
                .collect(Collectors.groupingBy(Track::getGenre, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static int bpmOf(Track track) {
        return track == null ? 120 : track.getBpm();
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record Scored(Track track, double score, String reason) {
    }
}
