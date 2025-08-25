package com.sfr.tokyo.sfr_backend.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sfr.tokyo.sfr_backend.dto.PostDto;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;
import com.sfr.tokyo.sfr_backend.repository.PostRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.service.FileStorageService;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// 作品関連のAPIエンドポイントを定義するコントローラー
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

        private final PostRepository postRepository;
        private final UserRepository userRepository;
        private final FileStorageService fileStorageService;

        // 現在認証されているユーザーを取得
        private User getCurrentAuthenticatedUser() {
                String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        }

        // 新しい作品を投稿する
        @PostMapping
        public ResponseEntity<PostDto> createPost(
                        @RequestParam("title") String title,
                        @RequestParam("description") String description,
                        @RequestParam("file") MultipartFile file) {

                User currentUser = getCurrentAuthenticatedUser();

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
                                .fileUrl(fileDownloadUri) // ダウンロードURLを保存
                                .user(currentUser)
                                .build();

                PostEntity savedPost = postRepository.save(newPost);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                PostDto.builder()
                                                .id(savedPost.getId())
                                                .title(savedPost.getTitle())
                                                .description(savedPost.getDescription())
                                                .fileUrl(savedPost.getFileUrl())
                                                .userId(savedPost.getUser().getId())
                                                .build());
        }

        // 認証ユーザーに紐づくすべての作品を取得するエンドポイント
        @GetMapping("/my-posts")
        public ResponseEntity<List<PostDto>> getMyPosts() {
                User currentUser = getCurrentAuthenticatedUser();
                List<PostEntity> posts = postRepository.findByUser_Id(currentUser.getId());
                List<PostDto> postDtos = posts.stream()
                                .map(post -> PostDto.builder()
                                                .id(post.getId())
                                                .title(post.getTitle())
                                                .description(post.getDescription())
                                                .fileUrl(post.getFileUrl())
                                                .userId(post.getUser().getId())
                                                .build())
                                .collect(Collectors.toList());

                return ResponseEntity.ok(postDtos);
        }

        // 特定の作品を削除する
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deletePost(@PathVariable Long id) {
                User currentUser = getCurrentAuthenticatedUser();
                return postRepository.findById(id)
                                .filter(post -> post.getUser().getId().equals(currentUser.getId()))
                                .map(post -> {
                                        postRepository.delete(post);
                                        return ResponseEntity.noContent().<Void>build();
                                })
                                .orElse(ResponseEntity.notFound().<Void>build());
        }

        // アップロードされたファイルをダウンロードするためのエンドポイント
        @GetMapping("/downloadFile/{fileName:.+}")
        public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
                Resource resource = fileStorageService.loadFileAsResource(fileName);
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + resource.getFilename() + "\"")
                                .body(resource);
        }

        // 特定のユーザーの作品をすべて取得するエンドポイント
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<PostDto>> getPostsByUserId(@PathVariable UUID userId) {
                List<PostEntity> posts = postRepository.findByUser_Id(userId);
                List<PostDto> postDtos = posts.stream()
                                .map(post -> PostDto.builder()
                                                .id(post.getId())
                                                .title(post.getTitle())
                                                .description(post.getDescription())
                                                .fileUrl(post.getFileUrl())
                                                .userId(post.getUser().getId())
                                                .build())
                                .collect(Collectors.toList());

                return ResponseEntity.ok(postDtos);
        }
}
