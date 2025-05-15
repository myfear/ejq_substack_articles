package org.acme.mapper;

import java.util.List;

import org.acme.domain.Post;
import org.acme.dto.CreatePostDto;
import org.acme.dto.PostDto;
import org.acme.dto.UpdatePostDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMapper {

    PostDto toDto(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastModifiedDate", expression = "java(java.time.LocalDateTime.now())")
    Post toEntity(CreatePostDto dto);

    List<PostDto> toDtoList(List<Post> posts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "authorEmail", ignore = true)
    @Mapping(target = "lastModifiedDate", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromDto(UpdatePostDto dto, @MappingTarget Post post);
}
