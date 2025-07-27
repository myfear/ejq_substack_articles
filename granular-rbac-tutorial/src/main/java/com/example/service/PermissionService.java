package com.example.service;

import com.example.entity.Document;
import com.example.entity.DocumentRight;
import com.example.entity.User;
import com.example.entity.UserDocumentRight;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PermissionService {

    public boolean hasPermission(Long userId, Long docId, DocumentRight right) {
        if (userId == null || docId == null)
            return false;

        Document doc = Document.findById(docId);
        if (doc == null)
            return false;

        if (doc.owner.id.equals(userId))
            return true; // owner = all rights
        return UserDocumentRight.hasRight(userId, docId, right);
    }

    @Transactional
    public void grantPermission(User target, Document doc,
            DocumentRight right, User granter) {

        if (!hasPermission(granter.id, doc.id, DocumentRight.SHARE))
            throw new SecurityException("Missing SHARE on document " + doc.id);

        if (UserDocumentRight.hasRight(target.id, doc.id, right))
            return;

        UserDocumentRight r = new UserDocumentRight();
        r.user = target;
        r.document = doc;
        r.right = right;
        r.persist();
    }

    @Transactional
    public long revokePermission(User target, Document doc,
            DocumentRight right, User revoker) {

        if (!hasPermission(revoker.id, doc.id, DocumentRight.SHARE))
            throw new SecurityException("Missing SHARE on document " + doc.id);

        if (doc.owner.id.equals(target.id))
            throw new IllegalArgumentException("Cannot revoke owner rights");

        return UserDocumentRight.delete("user=?1 and document=?2 and right=?3",
                target, doc, right);
    }
}