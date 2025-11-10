package com.example;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import io.smallrye.mutiny.Multi;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@Path("/api/music")
@Produces(MediaType.APPLICATION_JSON)
public class MusicResource {

    @ConfigProperty(name = "music.directory", defaultValue = "/path/to/your/music")
    String musicDir;

    @Inject
    Vertx vertx;

    @GET
    @Path("/list")
    public List<String> listMusic() {
        try {
            return Files.list(new File(musicDir).toPath())
                    .filter(path -> path.toString().endsWith(".mp3"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebApplicationException("Failed to list music files", e, 500);
        }
    }

    @GET
    @Path("/stream/{filename}")
    @Produces("audio/mpeg")
    public Multi<byte[]> streamMusic(@PathParam("filename") String filename) {
        if (filename.contains("..")) {
            throw new WebApplicationException("Invalid filename", 400);
        }

        File file = new File(musicDir, filename);
        if (!file.exists()) {
            throw new WebApplicationException("File not found", 404);
        }

        return vertx.fileSystem().open(file.getAbsolutePath(), new OpenOptions().setRead(true))
                .onItem().transformToMulti(asyncFile -> asyncFile.toMulti()
                        .onItem().transform(buffer -> buffer.getBytes())
                        .onCompletion().invoke(asyncFile::close));
    }

    @GET
    @Path("/metadata/{filename}")
    public MusicMetadata getMetadata(@PathParam("filename") String filename) {
        if (filename.contains("..")) {
            throw new WebApplicationException("Invalid filename", 400);
        }

        File file = new File(musicDir, filename);
        if (!file.exists()) {
            throw new WebApplicationException("File not found", 404);
        }

        try {
            MusicMetadata metadata = new MusicMetadata(filename);
            metadata.setFileSize(file.length());

            Mp3File mp3File = new Mp3File(file);
            metadata.setDuration(mp3File.getLengthInSeconds());

            if (mp3File.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                metadata.setTitle(cleanString(id3v2Tag.getTitle()));
                metadata.setArtist(cleanString(id3v2Tag.getArtist()));
                metadata.setAlbum(cleanString(id3v2Tag.getAlbum()));
                metadata.setYear(cleanString(id3v2Tag.getYear()));
                metadata.setGenre(cleanString(id3v2Tag.getGenreDescription()));
            } else if (mp3File.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3File.getId3v1Tag();
                metadata.setTitle(cleanString(id3v1Tag.getTitle()));
                metadata.setArtist(cleanString(id3v1Tag.getArtist()));
                metadata.setAlbum(cleanString(id3v1Tag.getAlbum()));
                metadata.setYear(cleanString(id3v1Tag.getYear()));
                metadata.setGenre(cleanString(id3v1Tag.getGenreDescription()));
            }

            return metadata;
        } catch (Exception e) {
            // If metadata reading fails, return basic info
            MusicMetadata metadata = new MusicMetadata(filename);
            metadata.setFileSize(file.length());
            return metadata;
        }
    }

    private String cleanString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}