<!-- この設計書はGitHub Copilot Proによるコード生成を目的としています -->

スペース機能 設計書（sfr.tokyo）

1️⃣ 機能のイメージ

コンセプト： 「学校」「オンラインサロン」「公式ファンクラブ」の3モードを切り替えられる、教育・交流・エンタメが融合したスペース。

モード切り替え： 管理画面からモードを選択 → UI・機能セットが自動最適化。

主なユースケース：

動画・教材の投稿と視聴

カリキュラムの進捗管理とゲーミフィケーション

オンライン授業の開催とフィードバック収集

写経・クイズなどのインタラクティブ教材

ディスカッションフォーラムによる交流

AIチューターによる学習支援と運営補助

モバイルアプリ[SFRM]へのPR投稿

2️⃣ DB定義（抜粋）

space

id: BIGINT, PK

name: VARCHAR

mode: ENUM(SCHOOL, SALON, FANCLUB)

space_mode_config

id: BIGINT, PK

space_id: BIGINT, FK → space.id

ui_config: JSON

feature_flags: JSON

space_content

id: BIGINT, PK

space_id: BIGINT, FK

title: VARCHAR

type: ENUM(VIDEO, MATERIAL)

url: TEXT

space_section

id: BIGINT, PK

content_id: BIGINT, FK

title: VARCHAR

order: INT

space_material

id: BIGINT, PK

section_id: BIGINT, FK

text: TEXT

media_url: TEXT

space_progress

id: BIGINT, PK

user_id: BIGINT

content_id: BIGINT

progress_percent: DECIMAL

space_badge

id: BIGINT, PK

name: VARCHAR

description: TEXT

space_point

id: BIGINT, PK

user_id: BIGINT

points: INT

space_ranking

id: BIGINT, PK

space_id: BIGINT

user_id: BIGINT

rank: INT

space_live_session

id: BIGINT, PK

owner_id: BIGINT

title: VARCHAR

scheduled_at: DATETIME

max_participants: INT

space_live_feedback

id: BIGINT, PK

session_id: BIGINT, FK

user_id: BIGINT

feedback: TEXT

space_live_qna

id: BIGINT, PK

session_id: BIGINT, FK

question: TEXT

answer: TEXT

space_quiz

id: BIGINT, PK

space_id: BIGINT

title: VARCHAR

questions: JSON

space_simulation

id: BIGINT, PK

space_id: BIGINT

scenario: TEXT

space_shakyo

id: BIGINT, PK

source_text: TEXT

category: VARCHAR

space_forum

id: BIGINT, PK

space_id: BIGINT

title: VARCHAR

space_comment

id: BIGINT, PK

forum_id: BIGINT, FK

user_id: BIGINT

text: TEXT

space_topic

id: BIGINT, PK

forum_id: BIGINT, FK

title: VARCHAR

space_ai_log

id: BIGINT, PK

user_id: BIGINT

query: TEXT

response: TEXT

space_ai_report

id: BIGINT, PK

space_id: BIGINT

summary: TEXT

space_ai_faq

id: BIGINT, PK

question: TEXT

answer: TEXT

space_pr_post

id: BIGINT, PK

space_id: BIGINT

title: VARCHAR

content: TEXT

space_subscription_plan

id: BIGINT, PK

space_id: BIGINT

monthly_fee: DECIMAL

currency: VARCHAR

space_course_fee

id: BIGINT, PK

content_id: BIGINT

price: DECIMAL

currency: VARCHAR

space_reward_log

id: BIGINT, PK

instructor_id: BIGINT

content_id: BIGINT

reward_type: ENUM(SUBSCRIPTION, COURSE_FEE, CRYPTO_GRANT)

amount: DECIMAL

granted_at: DATETIME

crypto_asset_transaction

id: BIGINT, PK

reward_log_id: BIGINT, FK

asset_type: VARCHAR

amount: DECIMAL

status: ENUM(PENDING, COMPLETED)

3️⃣ 機能群と対応DB設計（概要）

機能カテゴリ

主なテーブル案

モード管理

space, space_mode_config

コンテンツ管理

space_content, space_section, space_material

進捗・ゲーミフィケーション

space_progress, space_badge, space_point, space_ranking

オンライン授業

space_live_session, space_live_feedback, space_live_qna

インタラクティブ教材

space_quiz, space_simulation, space_shakyo

コミュニティ

space_forum, space_comment, space_topic

AIアシスタント

space_ai_log, space_ai_report, space_ai_faq

PR連携

space_pr_post（→ SFRM連携）

報酬設計

space_subscription_plan, space_course_fee, space_reward_log, crypto_asset_transaction

4️⃣ API設計（詳細）

メソッド

パス

説明

リクエストDTO

レスポンス

POST

/spaces

スペースの作成（モード指定）

SpaceCreateDto

spaceId

GET

/spaces/{id}/config

モード別設定取得

―

SpaceModeConfig

PUT

/spaces/{id}/config

モード別設定更新

SpaceModeConfigDto

success

POST

/spaces/contents

動画・教材投稿

SpaceContentDto

contentId

GET

/spaces/contents/{id}

コンテンツ詳細取得

―

SpaceContentDetail

POST

/spaces/progress

学習進捗登録

SpaceProgressDto

progressId

GET

/spaces/progress/{userId}

ユーザー進捗取得

―

List<SpaceProgress>

POST

/spaces/live/sessions

オンライン授業作成

SpaceLiveSessionDto

sessionId

GET

/spaces/live/sessions/{id}

授業詳細取得

―

SpaceLiveSessionDetail

POST

/spaces/quizzes

クイズ作成

SpaceQuizDto

quizId

GET

/spaces/quizzes/{id}

クイズ詳細取得

―

SpaceQuizDetail

POST

/spaces/shakyo

写経教材作成

SpaceShakyoDto

shakyoId

GET

/spaces/shakyo/{id}

写経教材取得

―

SpaceShakyoDetail

POST

/spaces/ai/query

AIチューターへの質問

SpaceAiQueryDto

responseText

GET

/spaces/ai/report

AIによる活動レポート取得

―

SpaceAiReport

POST

/spaces/subscription/plans

月額プランの登録

SpaceSubscriptionPlanDto

planId

GET

/spaces/subscription/plans/{id}

プラン詳細取得

―

SpaceSubscriptionPlanDetail

POST

/spaces/course/fee

買い切り講座の価格設定

SpaceCourseFeeDto

feeId

GET

/spaces/course/fee/{id}

講座価格取得

―

SpaceCourseFeeDetail

GET

/spaces/rewards/{instructorId}

報酬履歴取得

―

List<SpaceRewardLog>

POST

/spaces/rewards/crypto

無料講座提供者への暗号資産付与（運営権限）

CryptoRewardRequestDto

transactionId

5️⃣ DTO定義と責務コメント

// Zod型生成例

// 使用API: POST /spaces
// 関連Entity: space
export const SpaceCreateSchema = z.object({
  name: z.string().min(1).max(100),
  mode: z.enum(['SCHOOL', 'SALON', 'FANCLUB'])
});

// 使用API: POST /spaces/contents
// 関連Entity: space_content
export const SpaceContentSchema = z.object({
  title: z.string().min(1).max(100),
  type: z.enum(['VIDEO', 'MATERIAL']),
  url: z.string().url()
});

// 使用API: POST /spaces/progress
// 関連Entity: space_progress
export const SpaceProgressSchema = z.object({
  content_id: z.number().int(),
  progress_percent: z.number().min(0).max(100)
});

// 使用API: POST /spaces/live/sessions
// 関連Entity: space_live_session
export const SpaceLiveSessionSchema = z.object({
  title: z.string().min(1).max(100),
  scheduled_at: z.string().refine(isFutureISODate),
  max_participants: z.number().int().min(1)
});

// 使用API: POST /spaces/rewards/crypto
// 関連Entity: crypto_asset_transaction
export const CryptoRewardRequestSchema = z.object({
  reward_log_id: z.number().int(),
  asset_type: z.enum(['BTC', 'ETH', 'SFR']),
  amount: z.number().min(0)
});

6️⃣ レスポンス型定義と責務コメント

export type SpaceCreateResponse = {
  spaceId: number;
};

export type SpaceModeConfig = {
  ui_config: Record<string, unknown>;
  feature_flags: Record<string, boolean>;
};

export type SpaceContentDetail = {
  title: string;
  type: 'VIDEO' | 'MATERIAL';
  url: string;
  sections: Array<{
    title: string;
    order: number;
    materials: Array<{ text: string; media_url?: string }>;
  }>;
};

export type SpaceProgress = {
  content_id: number;
  progress_percent: number;
};

export type SpaceLiveSessionDetail = {
  title: string;
  scheduled_at: string;
  max_participants: number;
  feedbacks: Array<{ user_id: number; feedback: string }>;
  qna: Array<{ question: string; answer: string }>;
};

export type SpaceQuizDetail = {
  title: string;
  questions: Array<{ question: string; options: string[]; answer: string }>;
};

export type SpaceShakyoDetail = {
  source_text: string;
  category: string;
};

export type SpaceAiReport = {
  summary: string;
};

export type SpaceSubscriptionPlanDetail = {
  monthly_fee: number;
  currency: string;
};

export type SpaceCourseFeeDetail = {
  price: number;
  currency: string;
};

7️⃣ バリデーションルールとエラーレスポンス標準化

必須項目の未入力時は400エラー

型不一致時は422エラー

存在しないID指定時は404エラー

権限不足時は403エラー

8️⃣ テストケース設計（例）

スペース作成時の正常系・異常系

コンテンツ投稿時のURLバリデーション

進捗登録時の境界値テスト（0%, 100%）

ライブ授業の最大参加数制限テスト

クイズ作成時の選択肢数チェック

9️⃣ OpenAPI連携（方針）

openapi.yamlをGitHub管理

CIで型生成とSDKビルドを自動化

バージョン管理とCHANGELOG整備

🔟 SDK設計（SFRM連携）

目的

SFRMモバイルアプリや運営ツールからのAPI呼び出しを簡素化

型安全な開発体験の提供

設計方針

TypeScriptベースのSDKを提供

OpenAPIから自動生成された型とZodスキーマを活用

AxiosベースのHTTPクライアントを内包

機能例

import axios from 'axios';
import { SpaceCreateSchema } from './schemas';

export async function createSpace(data: unknown): Promise<number> {
  const parsed = SpaceCreateSchema.parse(data);
  const res = await axios.post('/spaces', parsed);
  return res.data.spaceId;
}

export async function getSpaceConfig(spaceId: number): Promise<SpaceModeConfig> {
  const res = await axios.get(`/spaces/${spaceId}/config`);
  return res.data;
}

1️⃣1️⃣ CI/CD連携とSDK利用ガイド（ドラフト）

CI/CD連携方針

GitHub Actionsを用いたSDKの自動ビルド・テスト・公開

openapi.yamlの更新をトリガーに型生成とSDK再ビルド

npmパッケージとして @sfr/sdk を公開

SDK利用ガイド（ドラフト）

インストール

npm install @sfr/sdk

初期化

import { createSpace } from '@sfr/sdk';

const spaceId = await createSpace({
  name: 'SFRスペース',
  mode: 'SCHOOL'
});

エラーハンドリング

try {
  await createSpace({ name: '', mode: 'UNKNOWN' });
} catch (e) {
  if (e instanceof ZodError) {
    console.error('バリデーションエラー:', e.errors);
  }
}

API一覧と型補完

VSCodeでの型補完対応

openapi.yaml由来の型定義により、引数・戻り値の安全性を確保

今後の展望

SDKのバージョン管理とCHANGELOG整備

SFRMアプリとの統合テスト

SDK利用者向けドキュメントサイト構築

1️⃣2️⃣ 用語集・命名規則・設計思想

用語集

SFR：Soundtrack For Revolution（プロジェクト名）

SFRM：SFRのモバイルアプリ

DTO：Data Transfer Object（API入出力用の型）

Zod：TypeScript用のスキーマバリデーションライブラリ

OpenAPI：API仕様記述フォーマット

命名規則

テーブル名は space_ プレフィックスで統一

DTO名は SpaceXxxDto 形式

スキーマ名は SpaceXxxSchema 形式

APIパスは /spaces/ で始める

設計思想

モード切替によるUI・機能の柔軟な最適化

学習・交流・報酬の統合設計

AI・ゲーミフィケーション・PR連携による拡張性

型安全・自動化・SDK化による開発効率の最大化