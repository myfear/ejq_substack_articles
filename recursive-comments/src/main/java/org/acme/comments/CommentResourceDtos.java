package org.acme.comments;

import java.time.OffsetDateTime;
import java.util.List;

public class CommentResourceDtos {

    public record NewCommentRequest(
            String content) {
    }

    public record CommentResponse(
            Long id,
            Long parentId,
            Long threadRootId,
            String content,
            OffsetDateTime createdAt) {
    }

    public record ThreadNodeResponse(
            Long id,
            Long parentId,
            int level,
            String content,
            List<ThreadNodeResponse> replies) {
    }
}