package org.ithub.postservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.dto.PostRequestDto;
import org.ithub.postservice.dto.PostResponseDto;
import org.ithub.postservice.enums.PostStatus;
import org.ithub.postservice.service.PostService;
import org.ithub.postservice.service.SocialPostPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final SocialPostPublisher publisher;

    @PostMapping
    @Operation(summary = "Создание нового поста")
    public ResponseEntity<PostResponseDto> createPost(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid PostRequestDto requestDto) {
        log.info("Creating post for user {}", userId);
        return new ResponseEntity<>(postService.createPost(userId, requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/count/{userId}")
    @Operation(summary = "Получение количества постов пользователя")
    public ResponseEntity<Long> getUserPostsCount(@PathVariable Long userId) {
        log.info("Getting post count for user {}", userId);
        return ResponseEntity.ok(postService.getPostsCountByUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение поста по ID")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        log.info("Getting post with id {}", id);
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping
    @Operation(summary = "Получение списка постов пользователя")
    public ResponseEntity<Page<PostResponseDto>> getUserPosts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Getting posts for user {} with status {}", userId, status);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(postService.getPostsByUser(userId, status, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление поста")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequestDto requestDto) {
        log.info("Updating post with id {}", id);
        return ResponseEntity.ok(postService.updatePost(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление поста")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("Deleting post with id {}", id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Публикация поста")
    public ResponseEntity<PostResponseDto> publishPost(@PathVariable Long id) {
        log.info("Publishing post with id {}", id);
        return ResponseEntity.ok(publisher.publishPost(id));
    }
}
