package org.acme.comments;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CommentService {

    @Inject
    CommentRepository repository;

    @Transactional
    public Comment addRootComment(String content) {
        var now = OffsetDateTime.now();
        // Insert with temporary thread_root_id value (1) to satisfy NOT NULL constraint
        var comment = new Comment(1L, null, content, now);
        Comment inserted = repository.insert(comment);

        // Update thread_root_id to the comment's own ID using save()
        inserted.setThreadRootId(inserted.getId());
        return repository.save(inserted);
    }

    @Transactional
    public Comment addReply(Long parentId, String content) {
        Comment parent = repository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found: " + parentId));

        var now = OffsetDateTime.now();
        var reply = new Comment(parent.getThreadRootId(), parentId, content, now);
        return repository.insert(reply);
    }

    public CommentThreadNode getThread(long rootId) {
        List<CommentRepository.ThreadRow> rows = repository.loadThread(rootId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Thread not found for rootId " + rootId);
        }

        Map<Long, CommentThreadNode> byId = new LinkedHashMap<>();
        CommentThreadNode root = null;

        // First pass: create nodes
        for (CommentRepository.ThreadRow row : rows) {
            CommentThreadNode node = new CommentThreadNode(
                    row.id(),
                    row.parentId(),
                    row.content(),
                    row.level(),
                    new ArrayList<>());
            byId.put(row.id(), node);
            if (row.parentId() == null) {
                root = node;
            }
        }

        // Second pass: attach children
        for (CommentThreadNode node : byId.values()) {
            Long parentId = node.parentId();
            if (parentId != null) {
                CommentThreadNode parent = byId.get(parentId);
                if (parent != null) {
                    parent.replies().add(node);
                }
            }
        }

        return root;
    }
}