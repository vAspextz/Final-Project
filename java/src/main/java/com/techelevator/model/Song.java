package com.techelevator.model;

public class Song {
    private int songId;
    private String title;
    private String artist;
    private String spotifyId;
    private int duration;

    public Song(int songId, String title, String artist, String spotifyId, int duration) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.spotifyId = spotifyId;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Song() {

    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
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

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }
}