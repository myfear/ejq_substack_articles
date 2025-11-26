package org.acme.comments;

import java.util.ArrayList;
import java.util.List;

public record CommentThreadNode(
        Long id,
        Long parentId,
        String content,
        int level,
        List<CommentThreadNode> replies) {
    public CommentThreadNode {
        // Always ensure replies is mutable
        if (replies == null) {
            replies = new ArrayList<>();
        }
    }
}