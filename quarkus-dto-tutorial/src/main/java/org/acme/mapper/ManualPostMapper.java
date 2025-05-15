package org.acme.mapper;

import org.acme.domain.Post;
import org.acme.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ManualPostMapper {

    public static PostDto toDto(Post post) {
        PostDto dto = new PostDto();
        dto.id = post.id;
        dto.title = post.title;
        dto.content = post.content;
        dto.authorEmail = post.authorEmail;
        dto.creationDate = post.creationDate;
        dto.lastModifiedDate = post.lastModifiedDate;
        return dto;
    }

    public static Post toEntity(CreatePostDto dto) {
        return new Post(dto.title, dto.content, dto.authorEmail);
    }

    public static void updateEntity(UpdatePostDto dto, Post post) {
        if (dto.title != null)
            post.title = dto.title;
        if (dto.content != null)
            post.content = dto.content;
        post.lastModifiedDate = LocalDateTime.now();
    }

    public static List<PostDto> toDtoList(List<Post> posts) {
        return posts.stream().map(ManualPostMapper::toDto).collect(Collectors.toList());
    }
}
