package io.beatsync.service;

public final class DurationParser {

    private DurationParser() {
    }

    public static int toSeconds(String runTime) {
        if (runTime == null || runTime.isBlank()) {
            throw new IllegalArgumentException("Run time is required");
        }
        String[] parts = runTime.trim().split(":");
        try {
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return minutes * 60 + seconds;
            }
            if (parts.length == 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                return hours * 3600 + minutes * 60 + seconds;
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid run time: " + runTime, ex);
        }
        throw new IllegalArgumentException("Invalid run time: " + runTime);
    }
}
