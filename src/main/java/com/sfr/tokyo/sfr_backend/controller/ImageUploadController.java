package com.sfr.tokyo.sfr_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sfr.tokyo.sfr_backend.service.ImageUploadService;

@RestController
@RequestMapping("/api/image") // このコントローラーのベースパス
public class ImageUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    private final ImageUploadService imageUploadService;

    // コンストラクタインジェクションでImageUploadServiceを注入
    @Autowired
    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    /**
     * 画像ファイルをアップロードするエンドポイント
     *
     * @param file アップロードされたファイル
     * @return 成功した場合は画像のURL、失敗した場合はエラーメッセージ
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        // ファイルが空かチェック
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("ファイルが空です。");
        }

        try {
            // ImageUploadServiceのuploadImageメソッドを呼び出してファイルを保存
            String fileUrl = imageUploadService.uploadImage(file);

            if (fileUrl != null) {
                // ファイルの保存が成功した場合、URLを返す
                return ResponseEntity.ok().body("{\"imageUrl\": \"" + fileUrl + "\"}");
            } else {
                // 保存に失敗した場合
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ファイルの保存に失敗しました。");
            }
        } catch (Exception e) {
            // 例外が発生した場合
            logger.error("ファイルのアップロード中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ファイルのアップロード中にエラーが発生しました。");
        }
    }
}
