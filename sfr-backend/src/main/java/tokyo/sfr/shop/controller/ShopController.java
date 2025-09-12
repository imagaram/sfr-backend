package tokyo.sfr.shop.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.util.Map;
import tokyo.sfr.shop.model.ShopItem;
import tokyo.sfr.shop.model.ShopOrder;
import tokyo.sfr.shop.model.ShopDelivery;
import tokyo.sfr.shop.model.ShopDeliveryToken;
import tokyo.sfr.shop.service.ShopService;
import tokyo.sfr.shop.service.PoALogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class ShopController {
    private final ShopService shopService;
    private final PoALogService poaLogService;
    
    public ShopController(ShopService shopService, PoALogService poaLogService) {
        this.shopService = shopService;
        this.poaLogService = poaLogService;
    }

    // 商品画像アップロード
    @PostMapping("/items/{id}/upload")
    public ResponseEntity<?> uploadItemImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, Principal principal, HttpServletRequest request) {
        String userId = principal != null ? principal.getName() : "anonymous";
        String ip = request.getRemoteAddr();
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("errorCode", "EMPTY_FILE", "message", "ファイルが選択されていません"));
            }
            
            // ウイルスチェック
            if (!callClamAV(file)) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("[image-upload] virus detected: itemId={}, userId={}, filename={}", id, userId, file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorCode", "VIRUS_DETECTED", "message", "危険なファイルが検出されました"));
            }
            
            // 画像保存処理（shopService.saveItemImage等で実装）
            String imageUrl = shopService.saveItemImage(id, file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
            
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("[image-upload] error: itemId={}, userId={}, msg={}", id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorCode", "SERVER_ERROR", "message", "画像アップロード失敗: " + e.getMessage()));
        }
    }

    // ClamAVウイルスチェックAPI呼び出し（Flask REST API連携）
    private boolean callClamAV(MultipartFile file) {
        try {
            // ClamAV Flask REST APIエンドポイント（今回実装したもの）
            String clamAvUrl = "http://localhost:5000/scan";
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(clamAvUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            // multipart/form-data形式で送信
            String boundary = "----" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            try (java.io.OutputStream os = conn.getOutputStream()) {
                String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
                
                // multipart形式でファイルを送信
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes());
                os.write(("Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes());
                os.write(file.getBytes());
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
            }
            
            int code = conn.getResponseCode();
            if (code == 200) {
                try (java.io.InputStream is = conn.getInputStream()) {
                    String result = new String(is.readAllBytes());
                    // JSONレスポンスを解析
                    return !result.contains("\"is_infected\":true") && !result.contains("FOUND");
                }
            }
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("[clamav] HTTP error: code={}", code);
            return false;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("[clamav] error: {}", e.getMessage());
            // エラー時は安全側（false）
            return false;
        }
    }

    // PoA提出API（POST /items/{id}/poa）
    @PostMapping("/items/{id}/poa")
    public ResponseEntity<?> submitPoA(@PathVariable Long id, @RequestBody PoARequest poa, Principal principal, HttpServletRequest request) {
        String userId = principal != null ? principal.getName() : "anonymous";
        String ip = request.getRemoteAddr();
        try {
            // PoA保存処理（shopService.submitPoA(id, poa)等）
            shopService.submitPoA(id, poa);
            // 操作ログ記録
            poaLogService.saveLog(userId, id, poa, ip, System.currentTimeMillis());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("[poa-submit] error: itemId={}, userId={}, msg={}", id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorCode", "SERVER_ERROR", "message", "PoA提出失敗: " + e.getMessage()));
        }
    }

    // 商品一覧取得
    @GetMapping("/items")
    public List<ShopItem> getItems() { return shopService.getAllItems(); }

    // 商品登録
    @PostMapping("/items")
    public void addItem(@RequestBody ShopItem item) { shopService.addItem(item); }

    // 商品詳細取得
    @GetMapping("/items/{id}")
    public ShopItem getItem(@PathVariable Long id) { return shopService.getItem(id); }

    // 商品編集
    @PutMapping("/items/{id}")
    public void updateItem(@PathVariable Long id, @RequestBody ShopItem item) { item.setId(id); shopService.updateItem(item); }

    // 商品削除
    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable Long id) { shopService.deleteItem(id); }

    // 注文作成
    @PostMapping("/orders")
    public void createOrder(@RequestBody ShopOrder order) { shopService.createOrder(order); }

    // 注文一覧取得
    @GetMapping("/orders")
    public List<ShopOrder> getOrders(@RequestParam Long buyerId) { return shopService.getOrdersByBuyer(buyerId); }

    // 注文詳細取得
    @GetMapping("/orders/{id}")
    public ShopOrder getOrder(@PathVariable Long id) { return shopService.getOrder(id); }

    // 配送ラベル作成
    @PostMapping("/delivery/{carrier}/createLabel")
    public ShopDelivery createDeliveryLabel(@PathVariable String carrier, @RequestParam Long orderId, @RequestParam String recipientInfo, @RequestParam String poaToken) {
        return shopService.createDeliveryLabel(orderId, carrier, recipientInfo, poaToken);
    }

    // 配送追跡
    @GetMapping("/delivery/{carrier}/trackShipment")
    public ShopDelivery trackDelivery(@PathVariable String carrier, @RequestParam String trackingNumber) {
        return shopService.trackDelivery(trackingNumber, carrier);
    }

    // 配送トークン発行
    @PostMapping("/delivery/token")
    public ShopDeliveryToken issueDeliveryToken(@RequestParam Long deliveryId, @RequestParam String poaToken) {
        return shopService.issueDeliveryToken(deliveryId, poaToken);
    }

    // DTO
    public static class PoARequest {
        public String text;
        public String url;
    }
}
