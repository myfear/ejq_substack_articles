package org.acme.diary;

import java.time.LocalDate;
import java.util.List;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class DiaryResource {

    @Inject
    CryptoService crypto;
    @Inject
    Template diary;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String view(@QueryParam("date") LocalDate date) {
        List<DiaryEntry> entries = date != null
                ? DiaryEntry.list("entryDate = ?1 and archived = false order by entryDate desc", date)
                : DiaryEntry.list("archived = false order by entryDate desc");

        entries.forEach(e -> e.encryptedContent = crypto.decrypt(e.encryptedContent));
        return diary.data("entries", entries).render();
    }

    @POST
    @Path("/entries")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void add(@FormParam("entryDate") LocalDate date, @FormParam("content") String content) {
        var entry = new DiaryEntry();
        entry.entryDate = date;
        entry.encryptedContent = crypto.encrypt(content);
        entry.persist();
    }

    @POST
    @Path("/entries/{id}/archive")
    @Transactional
    public void archive(@PathParam("id") Long id) {
        DiaryEntry entry = DiaryEntry.findById(id);
        if (entry != null) {
            entry.archived = true;
            entry.persist();
        }
    }
}
