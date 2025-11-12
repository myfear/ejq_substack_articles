package com.example.repository;

import java.util.List;

import com.example.entity.Post;
import com.example.model.PostStatus;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {

    public List<Post> findByStatus(PostStatus status) {
        return list("status", status);
    }

    public List<Post> findPendingPosts() {
        return findByStatus(PostStatus.PENDING);
    }
}