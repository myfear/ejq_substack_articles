package com.acme.batch;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.acme.domain.Invoice;
import com.acme.service.PdfService;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Named("com.acme.batch.InvoiceWriter")
public class InvoiceWriter extends AbstractItemWriter {

    @Inject
    EntityManager em;
    @Inject
    PdfService pdfService;
    @Inject
    JobContext jobCtx;

    private boolean dryRun;
    private Path outDir;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        dryRun = Boolean.parseBoolean(jobCtx.getProperties().getProperty("dryRun", "false"));
        outDir = Path.of(jobCtx.getProperties().getProperty("billing.pdf.output-dir", "target/invoices"));
        Files.createDirectories(outDir);
    }

    @Override
    @Transactional
    public void writeItems(List<Object> items) throws Exception {
        if (!dryRun) {
            for (Object o : items) {
                Invoice inv = (Invoice) o;
                em.persist(inv);
                em.flush();

                var pdf = pdfService.render(inv);
                inv.pdfPath = pdf.toString();

                em.merge(inv);
                em.flush();
            }
        }
    }
}
