package com.sfr.tokyo.sfr_backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.client.RestClient;

/**
 * アプリ起動時に OpenAPI 定義を JSON / YAML として書き出す簡易エクスポータ。
 * 実行例:
 * mvn spring-boot:run -Dspring-boot.run.arguments=--app.openapi.export=true
 * 追加指定 (出力先変更):
 * -Dspring-boot.run.jvmArguments="-Dapp.openapi.outDir=custom/path" など
 */
@Configuration
@ConditionalOnProperty(prefix = "app.openapi", name = "export", havingValue = "true")
public class OpenApiExportConfig {

    private final ObjectProvider<OpenAPI> openApiProvider;
    @Value("${app.openapi.outDir:#{null}}")
    private String outDirPropFromSpring;
    @Value("${app.openapi.export.exit:false}")
    private boolean exitAfterExportFlag;

    public OpenApiExportConfig(ObjectProvider<OpenAPI> openApiProvider) {
        this.openApiProvider = openApiProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) throws Exception {
        // HTTP 経由で springdoc が構築した完全な spec を取得
        OpenAPI openAPI = null;
        String rawJson = null;
        try {
            int port = 8080;
            if (event.getApplicationContext() instanceof ServletWebServerApplicationContext ctx) {
                port = ctx.getWebServer().getPort();
            }
            RestClient client = RestClient.create();
            rawJson = client.get().uri("http://localhost:" + port + "/v3/api-docs").retrieve().body(String.class);
            if (rawJson != null) {
                System.out.println("[OpenAPI Export] HTTP fetch success /v3/api-docs size=" + rawJson.length());
            } else {
                System.out.println("[OpenAPI Export] HTTP fetch returned null body");
            }
        } catch (Exception ex) {
            // フォールバック: 直接 OpenAPI ビーン (未構築の場合は最小限)
            System.err.println("[OpenAPI Export] Failed HTTP fetch /v3/api-docs: " + ex.getClass().getSimpleName()
                    + " - " + ex.getMessage());
            openAPI = openApiProvider
                    .getIfAvailable(() -> new OpenAPI().info(new Info().title("SFR Backend API").version("v1")));
            System.out.println("[OpenAPI Export] Using fallback OpenAPI bean (likely incomplete)");
        }
        export(openAPI, rawJson);
    }

    private void export(OpenAPI openAPI, String rawJson) throws IOException {
        String outDirProp = System.getProperty("app.openapi.outDir");
        if (!StringUtils.hasText(outDirProp)) {
            outDirProp = outDirPropFromSpring; // spring property fallback
        }
        Path outDir = Path.of(StringUtils.hasText(outDirProp) ? outDirProp : "target/openapi");
        Files.createDirectories(outDir);
        // 将来差分管理するなら timestamp 付与版も出してアーカイブ化可能
        // String timestamp = LocalDateTime.now().toString().replace(":", "-");

        ObjectMapper jsonMapper = new ObjectMapper();
        Path jsonFile = outDir.resolve("openapi.json");
        if (rawJson != null) {
            // そのまま整形して書き込み
            Object tree = jsonMapper.readValue(rawJson, Object.class);
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), tree);
            // YAML 変換
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            Path yamlFile = outDir.resolve("openapi.yaml");
            yamlMapper.writeValue(yamlFile.toFile(), tree);
        } else {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), openAPI);
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            Path yamlFile = outDir.resolve("openapi.yaml");
            yamlMapper.writeValue(yamlFile.toFile(), openAPI);
        }

        System.out.println("[OpenAPI Export] Wrote files under: " + outDir.toAbsolutePath());

        if (exitAfterExportFlag || "true".equalsIgnoreCase(System.getProperty("app.openapi.export.exit"))) {
            System.out.println("[OpenAPI Export] auto-exit enabled (app.openapi.export.exit=true)");
            // 非CIの通常起動に影響しないよう System.exit は明示プロパティ指定時のみ。
            System.exit(0);
        }
    }
}
