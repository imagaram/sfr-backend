/**
 * SFR Learning API SDK - Quiz Client
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 */

import { ApiClient } from './api-client';
import {
    Quiz,
    QuizCreateRequest,
    QuizAnswerRequest,
    QuizResult,
    QuizDifficulty,
    QuizStatus
} from '../types';

/**
 * クイズ・テスト管理クライアント
 * クイズの作成、取得、回答、結果管理機能を提供
 */
export class QuizClient {
    constructor(private apiClient: ApiClient) { }

    /**
     * クイズ一覧取得
     * @param spaceId 学習空間ID
     * @param options フィルタリングオプション
     * @returns クイズ一覧
     */
    async getQuizzes(
        spaceId: number,
        options?: {
            difficulty?: QuizDifficulty;
            status?: QuizStatus;
            page?: number;
            size?: number;
        }
    ): Promise<{ quizzes: Quiz[]; totalCount: number }> {
        const params = new URLSearchParams();

        if (options?.difficulty) params.append('difficulty', options.difficulty);
        if (options?.status) params.append('status', options.status);
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();
        const url = `/spaces/${spaceId}/quizzes${queryString ? `?${queryString}` : ''}`;

        return this.apiClient.get<{ quizzes: Quiz[]; totalCount: number }>(url);
    }

    /**
     * クイズ詳細取得
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @returns クイズ詳細（問題含む）
     */
    async getQuiz(spaceId: number, quizId: number): Promise<Quiz & { questions: any[] }> {
        return this.apiClient.get<Quiz & { questions: any[] }>(
            `/spaces/${spaceId}/quizzes/${quizId}`
        );
    }

    /**
     * クイズ作成
     * @param spaceId 学習空間ID
     * @param request クイズ作成リクエスト
     * @returns 作成されたクイズ
     */
    async createQuiz(spaceId: number, request: QuizCreateRequest): Promise<Quiz> {
        return this.apiClient.post<Quiz>(`/spaces/${spaceId}/quizzes`, request);
    }

    /**
     * クイズ更新
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @param request 更新リクエスト
     * @returns 更新されたクイズ
     */
    async updateQuiz(
        spaceId: number,
        quizId: number,
        request: Partial<QuizCreateRequest>
    ): Promise<Quiz> {
        return this.apiClient.put<Quiz>(`/spaces/${spaceId}/quizzes/${quizId}`, request);
    }

    /**
     * クイズ削除
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     */
    async deleteQuiz(spaceId: number, quizId: number): Promise<void> {
        await this.apiClient.delete(`/spaces/${spaceId}/quizzes/${quizId}`);
    }

    /**
     * クイズ回答提出
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @param answers 回答データ
     * @returns クイズ結果
     */
    async submitQuizAnswer(
        spaceId: number,
        quizId: number,
        answers: QuizAnswerRequest
    ): Promise<QuizResult> {
        return this.apiClient.post<QuizResult>(
            `/spaces/${spaceId}/quizzes/${quizId}/attempt`,
            answers
        );
    }

    /**
     * クイズ結果取得
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @param attemptId 受験ID（オプション、未指定の場合は最新）
     * @returns クイズ結果
     */
    async getQuizResult(
        spaceId: number,
        quizId: number,
        attemptId?: number
    ): Promise<QuizResult> {
        const url = attemptId
            ? `/spaces/${spaceId}/quizzes/${quizId}/results/${attemptId}`
            : `/spaces/${spaceId}/quizzes/${quizId}/results/latest`;

        return this.apiClient.get<QuizResult>(url);
    }

    /**
     * クイズ受験履歴取得
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @returns 受験履歴一覧
     */
    async getQuizAttempts(spaceId: number, quizId: number): Promise<QuizResult[]> {
        return this.apiClient.get<QuizResult[]>(
            `/spaces/${spaceId}/quizzes/${quizId}/attempts`
        );
    }

    /**
     * ユーザーのクイズ統計取得
     * @param spaceId 学習空間ID
     * @param userId ユーザーID（未指定の場合は認証済みユーザー）
     * @returns クイズ統計
     */
    async getQuizStats(spaceId: number, userId?: string): Promise<{
        totalQuizzes: number;
        completedQuizzes: number;
        averageScore: number;
        totalTimeSpent: number;
        bestScores: Array<{
            quizId: number;
            quizTitle: string;
            score: number;
            attemptedAt: string;
        }>;
    }> {
        const url = userId
            ? `/spaces/${spaceId}/quizzes/stats?userId=${userId}`
            : `/spaces/${spaceId}/quizzes/stats`;

        return this.apiClient.get(url);
    }

    /**
     * クイズランキング取得
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @param limit 取得件数
     * @returns ランキング
     */
    async getQuizLeaderboard(
        spaceId: number,
        quizId: number,
        limit: number = 10
    ): Promise<Array<{
        rank: number;
        userId: string;
        username: string;
        score: number;
        timeSpent: number;
        completedAt: string;
    }>> {
        return this.apiClient.get(
            `/spaces/${spaceId}/quizzes/${quizId}/leaderboard?limit=${limit}`
        );
    }

    /**
     * クイズの練習モード開始
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @returns 練習セッション情報
     */
    async startPracticeMode(spaceId: number, quizId: number): Promise<{
        sessionId: string;
        questions: any[];
        timeLimit?: number;
    }> {
        return this.apiClient.post(`/spaces/${spaceId}/quizzes/${quizId}/practice`);
    }

    /**
     * 練習モードの回答提出
     * @param spaceId 学習空間ID
     * @param quizId クイズID
     * @param sessionId セッションID
     * @param answers 回答データ
     * @returns 即座のフィードバック
     */
    async submitPracticeAnswer(
        spaceId: number,
        quizId: number,
        sessionId: string,
        answers: QuizAnswerRequest
    ): Promise<{
        correctAnswers: number;
        totalQuestions: number;
        feedback: Array<{
            questionIndex: number;
            isCorrect: boolean;
            explanation: string;
        }>;
    }> {
        return this.apiClient.post(
            `/spaces/${spaceId}/quizzes/${quizId}/practice/${sessionId}/submit`,
            answers
        );
    }

    /**
     * 難易度別クイズ取得
     * @param spaceId 学習空間ID
     * @param difficulty 難易度
     * @returns 指定難易度のクイズ一覧
     */
    async getQuizzesByDifficulty(
        spaceId: number,
        difficulty: QuizDifficulty
    ): Promise<Quiz[]> {
        return this.apiClient.get<Quiz[]>(
            `/spaces/${spaceId}/quizzes?difficulty=${difficulty}`
        );
    }

    /**
     * 推奨クイズ取得
     * @param spaceId 学習空間ID
     * @returns ユーザーに推奨されるクイズ一覧
     */
    async getRecommendedQuizzes(spaceId: number): Promise<Quiz[]> {
        return this.apiClient.get<Quiz[]>(`/spaces/${spaceId}/quizzes/recommended`);
    }
}
