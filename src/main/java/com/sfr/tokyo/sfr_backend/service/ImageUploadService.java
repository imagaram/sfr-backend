package com.sfr.tokyo.sfr_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 画像アップロード機能を扱うサービス
@Service
public class ImageUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);

    // application.propertiesで設定されたアップロードディレクトリのパスをインジェクション
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 画像ファイルをアップロードするメソッド
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            // ファイルが空の場合はエラーをログに出力
            logger.error("Failed to store empty file.");
            return null;
        }

        try {
            // アップロードディレクトリが存在しない場合は作成
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 元のファイル名から拡張子を取得
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // UUIDを生成して一意なファイル名を作成
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);

            // ファイルを保存
            Files.copy(file.getInputStream(), filePath);

            // 保存したファイルの相対パス（URLとしてアクセス可能なパス）を返す
            return "/images/" + fileName;

        } catch (IOException e) {
            // ファイル保存中にエラーが発生した場合
            logger.error("Failed to store file.", e);
            return null;
        }
    }
}
