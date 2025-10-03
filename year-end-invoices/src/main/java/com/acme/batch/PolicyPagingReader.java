package com.acme.batch;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import com.acme.domain.Policy;

import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Named("com.acme.batch.PolicyPagingReader")
public class PolicyPagingReader extends AbstractItemReader {

    @Inject
    JobContext jobCtx;
    @Inject
    StepContext stepCtx;
    @PersistenceContext
    EntityManager em;

    private Iterator<Policy> buffer;
    private int page = 0;
    private int pageSize = 200;

    @Override
    public void open(Serializable checkpoint) {
        Object cs = jobCtx.getProperties().get("chunkSize");
        if (cs != null)
            pageSize = Integer.parseInt(cs.toString());
    }

    @Override
    public Object readItem() {
        if (buffer == null || !buffer.hasNext()) {
            List<Policy> next = fetchPage(page++, pageSize);
            if (next.isEmpty())
                return null;
            buffer = next.iterator();
        }
        return buffer.next();
    }

    private List<Policy> fetchPage(int page, int size) {
        int year = Integer.parseInt(jobCtx.getProperties().getProperty("year"));
        String bundesland = jobCtx.getProperties().getProperty("bundesland");
        LocalDate day = LocalDate.of(year, 12, 31);

        String base = "SELECT p FROM Policy p WHERE p.validFrom <= :day AND p.validTo >= :day AND p.cancelled=false";
        if (bundesland != null && !bundesland.isBlank())
            base += " AND p.bundesland = :bl";

        var q = em.createQuery(base, Policy.class)
                .setParameter("day", day)
                .setFirstResult(page * size)
                .setMaxResults(size);

        if (bundesland != null && !bundesland.isBlank())
            q.setParameter("bl", bundesland);
        
        return q.getResultList();
    }
}
