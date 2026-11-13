package io.beatsync.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tracks")
public class Track {

    @Id
    private String id;

    @Indexed
    private String title;

    @Indexed
    private String artist;

    private String album;

    @Indexed
    private String genre;

    private int releaseYear;
    private int durationSec;

    @Indexed
    private int bpm;

    private int energy;

    public Track() {
    }

    public Track(
            String title,
            String artist,
            String album,
            String genre,
            int releaseYear,
            int durationSec,
            int bpm,
            int energy
    ) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.durationSec = durationSec;
        this.bpm = bpm;
        this.energy = energy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
}
