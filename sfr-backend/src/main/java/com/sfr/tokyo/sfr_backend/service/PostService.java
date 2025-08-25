package com.sfr.tokyo.sfr_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sfr.tokyo.sfr_backend.dto.PostDto;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;
import com.sfr.tokyo.sfr_backend.mapper.PostMapper;
import com.sfr.tokyo.sfr_backend.repository.PostRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PostMapper postMapper;

    // Mapperを使用してDTOとEntityの変換を行う

    /**
     * 投稿作成
     */
    @Transactional
    public PostDto createPost(String title, String description, MultipartFile file, UUID userId) {
        // ユーザーの存在確認
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // ファイルを保存し、保存されたファイル名を取得
        String fileName = fileStorageService.storeFile(file);

        // ファイルのダウンロードURLを作成
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/posts/downloadFile/")
                .path(fileName)
                .toUriString();

        PostEntity newPost = PostEntity.builder()
                .title(title)
                .description(description)
                .fileUrl(fileDownloadUri)
                .user(user)
                .build();

        PostEntity savedPost = postRepository.save(newPost);

        return postMapper.toDto(savedPost);
    }

    /**
     * 投稿作成（DTOベース）
     */
    @Transactional
    public PostDto createPost(PostDto postDto, UUID userId) {
        // ユーザーの存在確認
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // DTOからエンティティに変換
        PostEntity post = postMapper.toEntity(postDto);
        post.setUser(user);

        // 保存
        PostEntity savedPost = postRepository.save(post);

        return postMapper.toDto(savedPost);
    }

    /**
     * ユーザーの投稿一覧取得
     */
    @Transactional(readOnly = true)
    public List<PostDto> getPostsByUserId(UUID userId) {
        List<PostEntity> posts = postRepository.findByUser_Id(userId);
        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 投稿詳細取得
     */
    @Transactional(readOnly = true)
    public Optional<PostDto> getPostById(Long postId) {
        return postRepository.findById(postId)
                .map(postMapper::toDto);
    }

    /**
     * 投稿更新
     */
    @Transactional
    public Optional<PostDto> updatePost(Long postId, UUID userId, PostDto postDto) {
        return postRepository.findById(postId)
                .filter(post -> post.getUser().getId().equals(userId))
                .map(post -> {
                    post.setTitle(postDto.getTitle());
                    post.setDescription(postDto.getDescription());
                    if (postDto.getFileUrl() != null) {
                        post.setFileUrl(postDto.getFileUrl());
                    }

                    PostEntity updatedPost = postRepository.save(post);
                    return postMapper.toDto(updatedPost);
                });
    }

    /**
     * 投稿削除
     */
    @Transactional
    public boolean deletePost(Long postId, UUID userId) {
        Optional<PostEntity> postOpt = postRepository.findById(postId);

        if (postOpt.isEmpty()) {
            return false;
        }

        PostEntity post = postOpt.get();

        // 投稿者のみ削除可能
        if (post.getUser().getId().equals(userId)) {
            // 関連ファイルも削除
            if (post.getFileUrl() != null) {
                try {
                    fileStorageService.deleteFile(post.getFileUrl());
                } catch (Exception e) {
                    // ファイル削除エラーはログに記録するが処理は継続
                    // ログ処理は実装に応じて追加
                }
            }

            postRepository.delete(post);
            return true;
        }

        return false;
    }

    /**
     * 全投稿一覧取得（公開投稿）
     */
    @Transactional(readOnly = true)
    public List<PostDto> getAllPosts() {
        List<PostEntity> posts = postRepository.findAll();
        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }
}
