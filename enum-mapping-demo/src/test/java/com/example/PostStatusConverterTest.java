package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.entity.Post;
import com.example.model.PostStatus;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@QuarkusTest
public class PostStatusConverterTest {

    @Inject
    EntityManager entityManager;

    @Test
    @Transactional
    void shouldMapCustomEnumValuesCorrectly() {
        Post post = new Post();
        post.title = "Pending Approval";
        post.status = PostStatus.PENDING;

        post.persist();
        entityManager.flush();
        entityManager.clear();

        Post reloaded = Post.findById(1);
        assertEquals(PostStatus.PENDING, reloaded.status);
    }
}
