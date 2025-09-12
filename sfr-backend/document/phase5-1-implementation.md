# Phase 5.1: バックエンドStripe基盤構築実装

## 🎯 Phase 5.1 目標
**Stripe Java SDK統合とPaymentIntent API実装**

## 📋 実装チェックリスト

### ✅ 1. Stripe SDK依存関係追加
- [x] pom.xml にStripe Java SDK追加
- [x] 設定ファイル準備
- [x] 環境変数設定

### 🚧 2. Stripe設定・認証
- [ ] StripeConfiguration クラス作成
- [ ] Stripe API キー管理
- [ ] Webhook署名秘密鍵設定

### 🚧 3. PaymentIntent API実装
- [ ] PaymentIntentService 作成
- [ ] PaymentIntentController 作成
- [ ] PaymentIntent DTO定義

### 🚧 4. Webhook受信処理
- [ ] StripeWebhookController 作成
- [ ] 署名検証実装
- [ ] イベント処理ディスパッチャ

## 🚀 実装開始

### ステップ1: Stripe SDK追加
まず、pom.xmlにStripe Java SDKを追加します。
