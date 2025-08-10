package com.sfr.tokyo.sfr_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.propertiesからファイル保存関連の設定を読み込むクラス
@ConfigurationProperties(prefix = "file") // file.upload-dirなどの設定を読み込む
public class FileStorageProperties {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
