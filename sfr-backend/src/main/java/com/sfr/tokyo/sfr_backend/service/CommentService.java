package com.sfr.tokyo.sfr_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sfr.tokyo.sfr_backend.dto.CommentDto;
import com.sfr.tokyo.sfr_backend.entity.Comment;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;
import com.sfr.tokyo.sfr_backend.mapper.CommentMapper;
import com.sfr.tokyo.sfr_backend.repository.CommentRepository;
import com.sfr.tokyo.sfr_backend.repository.PostRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    /**
     * 投稿のコメント一覧取得
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPost_Id(postId);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * コメント作成
     */
    @Transactional
    public CommentDto createComment(Long postId, CommentDto commentDto, UUID userId) {
        // 投稿の存在確認
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        // ユーザーの存在確認
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // DTOからエンティティに変換
        Comment comment = commentMapper.toEntity(commentDto);
        comment.setPost(post);
        comment.setUser(user);

        // 保存
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }

    /**
     * コメント削除（投稿者または投稿の作成者のみ可能）
     */
    @Transactional
    public boolean deleteComment(Long commentId, UUID userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            return false;
        }

        Comment comment = commentOpt.get();

        // コメント投稿者または投稿の作成者のみ削除可能
        boolean canDelete = comment.getUser().getId().equals(userId) ||
                comment.getPost().getUser().getId().equals(userId);

        if (canDelete) {
            commentRepository.delete(comment);
            return true;
        }

        return false;
    }
}
