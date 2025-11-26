package org.acme.comments;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thread_root_id", nullable = false)
    private Long threadRootId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // JPA requires a no-arg constructor
    protected Comment() {
    }

    public Comment(Long threadRootId, Long parentId, String content, OffsetDateTime createdAt) {
        this.threadRootId = threadRootId;
        this.parentId = parentId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getThreadRootId() {
        return threadRootId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setThreadRootId(Long threadRootId) {
        this.threadRootId = threadRootId;
    }
}