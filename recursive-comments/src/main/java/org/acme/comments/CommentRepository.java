package org.acme.comments;

import java.util.List;

import org.hibernate.annotations.processing.SQL;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {

    /**
     * Load an entire comment thread in one DB round-trip using a recursive CTE.
     *
     * The SQL:
     * - Starts from the root comment (id = :rootId)
     * - Joins children by parent_id
     * - Computes a "level" and "path" for stable ordering
     */
    @SQL("""
            with recursive thread as (
                select c.id,
                       c.parent_id,
                       c.content,
                       0 as level,
                       lpad(c.id::text, 10, '0') as path
                from comments c
                where c.id = :rootId

                union all

                select child.id,
                       child.parent_id,
                       child.content,
                       parent.level + 1 as level,
                       parent.path || '.' || lpad(child.id::text, 10, '0') as path
                from comments child
                join thread parent on child.parent_id = parent.id
            )
            select id, parent_id, content, level, path
            from thread
            order by path
            """)
    List<ThreadRow> loadThread(long rootId);

    /**
     * Flat projection of the recursive CTE result.
     */
    record ThreadRow(
            Long id,
            Long parentId,
            String content,
            int level,
            String path) {
    }

}
