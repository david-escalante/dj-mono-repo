package io.beatsync.service;

public final class TrackAttributes {

    private TrackAttributes() {
    }

    public static int bpm(String title, String artist, String genre) {
        int[] range = rangeFor(genre);
        int span = range[1] - range[0] + 1;
        return range[0] + Math.floorMod(stableHash(title, artist), span);
    }

    public static int energy(String title, String artist) {
        return 1 + Math.floorMod(stableHash(title, artist) / 17, 5);
    }

    static int[] rangeFor(String genre) {
        if (genre == null) {
            return new int[] {90, 130};
        }
        return switch (genre.trim().toLowerCase()) {
            case "classical" -> new int[] {60, 88};
            case "jazz" -> new int[] {70, 110};
            case "folk", "country", "gospel" -> new int[] {78, 118};
            case "r&b", "soul", "rnb" -> new int[] {70, 105};
            case "hip-hop", "hip hop", "rap" -> new int[] {80, 110};
            case "reggae" -> new int[] {70, 95};
            case "latin", "world" -> new int[] {90, 125};
            case "pop" -> new int[] {100, 128};
            case "indie", "alternative" -> new int[] {95, 140};
            case "rock" -> new int[] {110, 150};
            case "funk" -> new int[] {100, 125};
            case "electronic" -> new int[] {118, 136};
            case "punk" -> new int[] {150, 190};
            default -> new int[] {90, 130};
        };
    }

    static int stableHash(String title, String artist) {
        String key = (title == null ? "" : title.strip()) + "|" + (artist == null ? "" : artist.strip());
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = 31 * hash + key.charAt(i);
        }
        return hash;
    }
}
