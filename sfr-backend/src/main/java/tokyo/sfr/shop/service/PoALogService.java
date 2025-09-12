package tokyo.sfr.shop.service;

import tokyo.sfr.shop.controller.ShopController;
import org.springframework.stereotype.Service;

@Service
public class PoALogService {
    
    public void saveLog(String userId, Long itemId, ShopController.PoARequest poa, String ip, long timestamp) {
        // TODO: PoAログ保存処理の実装が必要
        // データベースにPoA提出ログを保存
    }
}
