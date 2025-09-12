# 📋 SFR.TOKYO データベース最適化計画
# learning_space → space テーブルマイグレーション

## 🎯 目標
- 旧 `learning_space` テーブルから新 `space` テーブルへの統合
- 全関連テーブルのリファクタリング
- ゼロダウンタイム マイグレーション実現

## 📊 影響範囲分析

### 既存テーブル (learning_space参照)
```sql
learning_space             -- 🎯 マイグレーション対象
├── learning_mode_config   -- space_id FK
├── learning_content       -- space_id FK
├── learning_point         -- space_id FK
├── learning_ranking       -- space_id FK
├── learning_shakyo        -- space_id FK
├── learning_survey        -- space_id FK
├── learning_forum         -- space_id FK
└── 他8テーブル            -- space_id FK
```

### 新しい統合構造
```sql
space                      -- 🆕 新統合テーブル
├── space_config          -- 🆕 統合設定
├── space_content         -- 🆕 汎用コンテンツ
├── space_member          -- 🆕 メンバー管理
├── space_activity        -- 🆕 活動ログ
└── 既存learning_*        -- 段階的移行
```

## 🚀 マイグレーション実行計画

### Phase 1: 新Spaceエンティティ作成 (30分)
- [x] SpaceEntity実装
- [x] SpaceRepository作成
- [x] SpaceService基本機能
- [x] SpaceController REST API

### Phase 2: デュアル運用開始 (1時間)
- [ ] データ同期機能実装
- [ ] learning_space → space 自動同期
- [ ] 新APIエンドポイント有効化
- [ ] フロントエンド切り替え準備

### Phase 3: 段階的移行 (2時間)
- [ ] 関連テーブルFK更新スクリプト
- [ ] データ整合性チェック
- [ ] 旧APIの段階的無効化
- [ ] フロントエンド完全移行

### Phase 4: クリーンアップ (30分)
- [ ] 旧learning_space削除
- [ ] 不要なコード削除
- [ ] パフォーマンス最適化

## ⚠️ リスク対策

### データ安全性
```sql
-- バックアップ作成
CREATE TABLE learning_space_backup AS SELECT * FROM learning_space;
-- ロールバック手順準備
-- 整合性チェッククエリ準備
```

### ゼロダウンタイム保証
- デュアル運用期間中は両API併存
- 段階的切り替えでリスク最小化
- 即座ロールバック可能な設計

## 🔧 実装順序

1. **新Spaceエンティティ基盤** ← 📍 現在ここ
2. **データ同期システム**
3. **API移行とテスト**
4. **フロントエンド更新**
5. **旧システム廃止**

## 📈 期待効果

### パフォーマンス改善
- 統合されたインデックス設計
- 不要なJOIN削減
- クエリ最適化

### 保守性向上
- 単一責任テーブル設計
- 型安全なAPI構造
- 一貫したネーミング

### 拡張性確保
- 新機能追加の容易さ
- 他システムとの統合性
- 将来的なスケーリング対応

---

**次のステップ**: 新Spaceエンティティの実装から開始
