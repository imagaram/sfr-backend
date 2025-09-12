/**
 * SFR Learning API SDK - Main Entry Point
 * @author SFR.TOKYO Development Team
 * @version 2.0.0
 */

import { ApiClient } from './client/api-client';
import { SpacesClient } from './client/spaces-client'; // 新しいスペースクライアント
import { LearningSpacesClient } from './client/learning-spaces-client'; // 旧クライアント（非推奨）
import { LearningContentClient } from './client/learning-content-client';
import { EvaluationsClient } from './client/evaluations-client';
import { QuizClient } from './client/quiz-client';
import { SfrLearningConfig } from './types';

/**
 * SFR Learning API メインSDKクライアント
 * 全ての学習機能へのアクセスポイントを提供
 */
export class SfrLearningSDK {
    private apiClient: ApiClient;

    // 各機能のクライアント
    public readonly spaces: SpacesClient; // 新しいスペースクライアント
    public readonly content: LearningContentClient;
    public readonly evaluations: EvaluationsClient;
    public readonly quiz: QuizClient;
    
    /**
     * @deprecated LearningSpacesClient は非推奨です。spaces プロパティを使用してください。
     */
    public readonly learningSpaces: LearningSpacesClient; // 後方互換性のため

    constructor(config: SfrLearningConfig) {
        this.apiClient = new ApiClient(config);

        // 各機能クライアントの初期化
        this.spaces = new SpacesClient(this.apiClient); // 新しいスペースクライアント
        this.learningSpaces = new LearningSpacesClient(this.apiClient); // 後方互換性
        this.content = new LearningContentClient(this.apiClient);
        this.evaluations = new EvaluationsClient(this.apiClient);
        this.quiz = new QuizClient(this.apiClient);
    }

    /**
     * 認証トークンを設定
     * @param token JWTアクセストークン
     */
    setAccessToken(token: string): void {
        this.apiClient.setAccessToken(token);
    }

    /**
     * 認証トークンをクリア
     */
    clearAccessToken(): void {
        this.apiClient.clearAccessToken();
    }

    /**
     * APIの疎通確認
     * @returns ヘルスチェック結果
     */
    async healthCheck(): Promise<{ status: string; timestamp: string }> {
        return this.apiClient.healthCheck();
    }

    /**
     * SDK設定を取得
     * @returns 現在の設定
     */
    getConfig(): SfrLearningConfig {
        return this.apiClient.getConfig();
    }

    // ======================================
    // 🎓 便利メソッド（よく使われる操作）
    // ======================================

    /**
     * スペース一覧取得（簡易版）
     * @param options 取得オプション
     * @returns スペース一覧
     */
    async getCourses(options?: {
        mode?: 'SCHOOL' | 'SALON' | 'FANCLUB';
        page?: number;
        size?: number;
    }) {
        return this.spaces.getSpaces(options);
    }

    /**
     * スペース参加（簡易版）
     * @param courseId スペースID（数値または文字列）
     * @param characterId キャラクターID
     * @returns 参加結果
     */
    async enrollCourse(courseId: string | number, characterId?: string) {
        const spaceId = typeof courseId === 'string' ? parseInt(courseId) : courseId;
        return this.spaces.joinSpace(spaceId, characterId);
    }

    /**
     * 評価投稿（簡易版）
     * @param evaluationDto 評価データ
     * @returns 投稿結果
     */
    async submitEvaluation(evaluationDto: {
        contentId: number;
        rating: number;
        comment?: string;
        characterId?: string;
    }) {
        return this.evaluations.submitEvaluation(evaluationDto);
    }

    /**
     * 学習進捗の記録
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     * @param progressData 進捗データ
     * @returns 記録結果
     */
    async recordProgress(
        spaceId: number,
        contentId: number,
        progressData: {
            progressType: 'STARTED' | 'IN_PROGRESS' | 'COMPLETED';
            timeSpent?: number;
            rating?: number;
            notes?: string;
        }
    ) {
        return this.content.recordProgress(spaceId, contentId, progressData);
    }

    /**
     * 次の学習コンテンツを取得
     * @param spaceId 学習空間ID
     * @returns 次のコンテンツ
     */
    async getNextContent(spaceId: number) {
        return this.content.getNextContent(spaceId);
    }

    /**
     * ユーザーの学習統計取得
     * @param spaceId 学習空間ID
     * @returns 学習統計
     */
    async getLearningStats(spaceId: number) {
        const progress = await this.content.getProgress(spaceId);
        const quizStats = await this.quiz.getQuizStats(spaceId);

        return {
            progress,
            quizStats,
            completionRate: progress.overallProgress,
            totalTimeSpent: progress.totalTimeSpent,
            achievements: progress.achievements
        };
    }

    /**
     * スペースの完全な情報取得
     * @param spaceId スペースID
     * @returns スペースの詳細情報
     */
    async getCompleteSpaceInfo(spaceId: number) {
        const [space, contentList, progress, quizzes] = await Promise.all([
            this.spaces.getSpace(spaceId),
            this.content.getContent(spaceId),
            this.content.getProgress(spaceId),
            this.quiz.getQuizzes(spaceId)
        ]);

        return {
            space,
            content: contentList.content,
            progress,
            quizzes: quizzes.quizzes,
            stats: {
                contentCount: contentList.totalElements,
                quizCount: quizzes.totalCount,
                completionRate: progress.overallProgress
            }
        };
    }
}

// ======================================
// 🔄 エクスポート
// ======================================

// メインクラス
export default SfrLearningSDK;

// 型定義
export * from './types';

// 個別クライアント（高度な使用向け）
export {
    ApiClient,
    LearningSpacesClient,
    LearningContentClient,
    EvaluationsClient,
    QuizClient
};

// ======================================
// 🚀 ファクトリー関数
// ======================================

/**
 * SDKの簡単な初期化
 * @param baseURL APIのベースURL
 * @param options 追加オプション
 * @returns 初期化されたSDKインスタンス
 */
export function createSfrLearningSDK(
    baseURL: string,
    options?: {
        apiKey?: string;
        timeout?: number;
        debug?: boolean;
    }
): SfrLearningSDK {
    return new SfrLearningSDK({
        baseURL,
        ...options
    });
}

/**
 * 開発環境用のSDK初期化
 * @param token 認証トークン（オプション）
 * @returns 開発環境向けSDKインスタンス
 */
export function createDevSDK(token?: string): SfrLearningSDK {
    const sdk = new SfrLearningSDK({
        baseURL: 'http://localhost:8080/api/learning',
        debug: true,
        timeout: 15000
    });

    if (token) {
        sdk.setAccessToken(token);
    }

    return sdk;
}

/**
 * 本番環境用のSDK初期化
 * @param token 認証トークン
 * @param apiKey APIキー（オプション）
 * @returns 本番環境向けSDKインスタンス
 */
export function createProdSDK(token: string, apiKey?: string): SfrLearningSDK {
    const sdk = new SfrLearningSDK({
        baseURL: 'https://api.sfr.tokyo/api/learning',
        apiKey,
        debug: false,
        timeout: 10000,
        retryAttempts: 3
    });

    sdk.setAccessToken(token);

    return sdk;
}