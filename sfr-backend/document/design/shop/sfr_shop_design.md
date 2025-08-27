📄 SFR.TOKYO ショップ機能 設計書ひな型

1. 概要

本ドキュメントは、SFR.TOKYOにおけるショップ機能の制度設計および技術設計を体系的に記述したものである。GitHub Copilot Proによるsfr-backend開発支援時に、DBスキーマ設計やAPI設計の参照元として活用できるよう、構造化された定義・ロジック・制度的背景を含む。

2. 制度的背景

2.1 特定商取引法対応

開示方式：購入者から請求があった場合のみ販売者情報を開示

法令準拠：特商法第11条「主務省令で定める方法」に準拠

判断プロセス：AI＋評議員による二段階判断（消費者保護／販売者保護の両立）

2.2 限定商品制度

販売範囲：SFR.TOKYO内限定

購入制限：ユーザー単位で購入個数制限

所有権管理：PoAチェーンに記録

二次流通時の寄付・報酬：

販売代金の10％を制作販売チームに寄付

中古販売者に暗号資産SFRを付与（付与額は下記ロジック）

SFR付与額ロジック（実装済み）

$$ SFR報酬量 = B \times C \times M \times H $$

変数

意味

調整方法

B

基本報酬係数

貢献タイプ別に設定（例：開発、流動性提供、ガバナンス投票）

C

貢献度スコア

定量評価（例：コード量、取引量、提案採択率）

M

市場状況係数

SFR/JPY価格が目標価格より低い場合は報酬増、高い場合は報酬減

H

保有インセンティブ係数

長期保有者やステーキング参加者に報酬倍率を追加

3. 機能要件

3.1 ユーザー階層構造

ユーザーがキャラクターを作成

キャラクターがチームを作成

チームがショップを作成

3.2 商品種別

物理商品（匿名配送対応）

デジタル商品（作品収集庫格納、PoA記録）

限定商品（購入制限・二次流通寄付）

3.3 決済

現金決済：Stripe（手数料10％、内3.5％はStripe利用料）

暗号資産SFR決済：Stripe利用料不要

3.4 入金処理

成約後30日後にチーム出納帳へ入金

分配規律がある場合はキャラクター出納帳へ自動分配

クーリングオフ時の振込手数料はチーム負担

3.5 配送

匿名配送（メルカリ型）

配送システム利用料はチーム負担

3.6 モバイルアプリSFRM連携

商品掲載（フォロワー表示／CPM課金で非フォロワー表示）

CPM：1,000円／1,000インプレッション

SFR決済時はStripe利用料不要

3.7 オークション

毎月1日告知／25日開催

取引は暗号資産SFR

4. 非機能要件

セキュリティ：PoAチェーンによる所有権証明、個人情報暗号化

可用性：決済・配送APIの冗長化

透明性：評議員判断履歴のPoA記録

5. DBスキーマ案（主要テーブル）

テーブル

主なカラム

関連

users

id, wallet, profile

characters

characters

id, user_id, team_id

teams

teams

id, ledger, rules

shops

shops

id, team_id, stripe_id

items

items

id, shop_id, type, owner_id, poa_hash

transactions

transactions

id, buyer_id, payment_type, status

items

auctions

id, item_id, start_date, bids

transactions

poa_records

id, item_id, owner_id, tx_hash

items

6. API設計案（例）

POST /characters：キャラクター作成

POST /teams：チーム作成

POST /shops：ショップ作成

POST /items：商品登録

POST /transactions：購入処理

POST /auctions/:id/bid：入札

POST /disclosure-requests：販売者情報開示請求

POST /resale-detection：二次流通検知→寄付・SFR付与

7. 運用フロー例

商品登録

販売・決済

PoA記録更新

入金処理（30日後）

返品対応（必要時）

二次流通検知→寄付・SFR付与

評議員判断履歴のPoA記録