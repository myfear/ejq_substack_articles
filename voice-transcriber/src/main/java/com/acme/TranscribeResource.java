package com.acme;

import java.util.List;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/transcribe")
public class TranscribeResource {

    @Inject
    WhisperService whisper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String transcribe(List<Float> audioData) {
        float[] pcm = new float[audioData.size()];
        for (int i = 0; i < audioData.size(); i++) {
            pcm[i] = audioData.get(i);
        }

        Log.infof("Received " + pcm.length + " samples");
        return whisper.transcribe(pcm);
    }
}