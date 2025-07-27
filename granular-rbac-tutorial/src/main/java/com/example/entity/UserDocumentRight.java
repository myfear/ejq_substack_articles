package com.example.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_document_rights", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "document_id",
        "right_type" }))
public class UserDocumentRight extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    public Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "right_type", nullable = false)
    public DocumentRight right;

    public static boolean hasRight(Long userId, Long docId, DocumentRight r) {
        return count("user.id = ?1 and document.id = ?2 and right = ?3",
                userId, docId, r) > 0;
    }
}