package com.sfr.tokyo.sfr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sfr.tokyo.sfr_backend.dto.CommentDto;
import com.sfr.tokyo.sfr_backend.entity.Comment;
import com.sfr.tokyo.sfr_backend.entity.Post;
import com.sfr.tokyo.sfr_backend.repository.CommentRepository;
import com.sfr.tokyo.sfr_backend.repository.PostRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// コメント関連のAPIエンドポイントを定義するコントローラー
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 現在認証されているユーザーを取得
    private User getCurrentAuthenticatedUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    // 特定の投稿に紐づくすべてのコメントを取得する
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> CommentDto.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .postId(comment.getPost().getId())
                        .userId(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentDtos);
    }

    // 新しいコメントを作成する
    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDto commentDto) {

        User currentUser = getCurrentAuthenticatedUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found."));

        Comment newComment = Comment.builder()
                .content(commentDto.getContent())
                .post(post)
                .user(currentUser)
                .build();

        Comment savedComment = commentRepository.save(newComment);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommentDto.builder()
                        .id(savedComment.getId())
                        .content(savedComment.getContent())
                        .postId(savedComment.getPost().getId())
                        .userId(savedComment.getUser().getId())
                        .username(savedComment.getUser().getUsername())
                        .build()
        );
    }

    // 特定のコメントを削除する
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        return commentRepository.findById(commentId)
                .filter(comment ->
                        // 投稿者、またはコメントの作成者のみが削除可能
                        comment.getUser().getId().equals(currentUser.getId()) ||
                        comment.getPost().getUser().getId().equals(currentUser.getId())
                )
                .map(comment -> {
                    commentRepository.delete(comment);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().<Void>build());
    }
}
