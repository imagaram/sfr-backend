/**
 * SFR Learning API SDK - Evaluations Client
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 */

import { ApiClient } from './api-client';
import {
    EvaluationDto,
    EvaluationResponse,
    PaginatedResponse
} from '../types';

/**
 * 評価・レビュー管理クライアント
 * コンテンツの評価投稿、取得、更新、削除機能を提供
 */
export class EvaluationsClient {
    constructor(private apiClient: ApiClient) { }

    /**
     * 評価投稿
     * @param evaluationDto 評価データ
     * @returns 投稿された評価
     */
    async submitEvaluation(evaluationDto: EvaluationDto): Promise<EvaluationResponse> {
        return this.apiClient.post<EvaluationResponse>('/evaluations', evaluationDto);
    }

    /**
     * コンテンツの評価一覧取得
     * @param contentId コンテンツID
     * @param options 取得オプション
     * @returns 評価一覧
     */
    async getEvaluations(
        contentId: number,
        options?: {
            page?: number;
            size?: number;
            sortBy?: 'createdAt' | 'rating';
            sortOrder?: 'asc' | 'desc';
        }
    ): Promise<PaginatedResponse<EvaluationResponse>> {
        const params = new URLSearchParams();
        params.append('contentId', contentId.toString());

        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());
        if (options?.sortBy) params.append('sortBy', options.sortBy);
        if (options?.sortOrder) params.append('sortOrder', options.sortOrder);

        const queryString = params.toString();

        return this.apiClient.get<PaginatedResponse<EvaluationResponse>>(
            `/evaluations?${queryString}`
        );
    }

    /**
     * 特定の評価取得
     * @param evaluationId 評価ID
     * @returns 評価詳細
     */
    async getEvaluation(evaluationId: number): Promise<EvaluationResponse> {
        return this.apiClient.get<EvaluationResponse>(`/evaluations/${evaluationId}`);
    }

    /**
     * 評価更新
     * @param evaluationId 評価ID
     * @param evaluationDto 更新データ
     * @returns 更新された評価
     */
    async updateEvaluation(
        evaluationId: number,
        evaluationDto: Partial<EvaluationDto>
    ): Promise<EvaluationResponse> {
        return this.apiClient.put<EvaluationResponse>(
            `/evaluations/${evaluationId}`,
            evaluationDto
        );
    }

    /**
     * 評価削除
     * @param evaluationId 評価ID
     */
    async deleteEvaluation(evaluationId: number): Promise<void> {
        await this.apiClient.delete(`/evaluations/${evaluationId}`);
    }

    /**
     * ユーザーの評価一覧取得
     * @param userId ユーザーID（未指定の場合は認証済みユーザー）
     * @param options 取得オプション
     * @returns ユーザーの評価一覧
     */
    async getUserEvaluations(
        userId?: string,
        options?: {
            page?: number;
            size?: number;
            contentId?: number;
        }
    ): Promise<PaginatedResponse<EvaluationResponse>> {
        const params = new URLSearchParams();

        if (userId) params.append('userId', userId);
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());
        if (options?.contentId) params.append('contentId', options.contentId.toString());

        const queryString = params.toString();
        const url = userId
            ? `/evaluations/user?${queryString}`
            : `/evaluations/me?${queryString}`;

        return this.apiClient.get<PaginatedResponse<EvaluationResponse>>(url);
    }

    /**
     * コンテンツの評価統計取得
     * @param contentId コンテンツID
     * @returns 評価統計
     */
    async getEvaluationStats(contentId: number): Promise<{
        averageRating: number;
        totalEvaluations: number;
        ratingDistribution: Record<string, number>;
    }> {
        return this.apiClient.get(`/evaluations/stats?contentId=${contentId}`);
    }

    /**
     * キャラクター別評価一覧取得
     * @param characterId キャラクターID
     * @param options 取得オプション
     * @returns キャラクター別評価一覧
     */
    async getCharacterEvaluations(
        characterId: string,
        options?: {
            page?: number;
            size?: number;
            contentId?: number;
        }
    ): Promise<PaginatedResponse<EvaluationResponse>> {
        const params = new URLSearchParams();
        params.append('characterId', characterId);

        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());
        if (options?.contentId) params.append('contentId', options.contentId.toString());

        const queryString = params.toString();

        return this.apiClient.get<PaginatedResponse<EvaluationResponse>>(
            `/evaluations/character?${queryString}`
        );
    }

    /**
     * 評価の有用性投票
     * @param evaluationId 評価ID
     * @param helpful 有用かどうか
     * @returns 投票結果
     */
    async voteEvaluationHelpfulness(
        evaluationId: number,
        helpful: boolean
    ): Promise<{
        helpfulVotes: number;
        totalVotes: number;
    }> {
        return this.apiClient.post(`/evaluations/${evaluationId}/vote`, { helpful });
    }

    /**
     * 評価への返信
     * @param evaluationId 評価ID
     * @param reply 返信内容
     * @returns 返信結果
     */
    async replyToEvaluation(
        evaluationId: number,
        reply: string
    ): Promise<{
        id: number;
        reply: string;
        authorId: string;
        createdAt: string;
    }> {
        return this.apiClient.post(`/evaluations/${evaluationId}/reply`, { reply });
    }

    /**
     * 評価の報告
     * @param evaluationId 評価ID
     * @param reason 報告理由
     * @returns 報告結果
     */
    async reportEvaluation(
        evaluationId: number,
        reason: 'spam' | 'inappropriate' | 'harassment' | 'other',
        details?: string
    ): Promise<{
        reportId: number;
        status: 'submitted' | 'reviewing' | 'resolved';
    }> {
        return this.apiClient.post(`/evaluations/${evaluationId}/report`, {
            reason,
            details
        });
    }

    /**
     * 高評価のコンテンツ取得
     * @param spaceId 学習空間ID（オプション）
     * @param options 取得オプション
     * @returns 高評価コンテンツ一覧
     */
    async getHighlyRatedContent(
        spaceId?: number,
        options?: {
            minRating?: number;
            minEvaluations?: number;
            page?: number;
            size?: number;
        }
    ): Promise<PaginatedResponse<{
        contentId: number;
        contentTitle: string;
        averageRating: number;
        totalEvaluations: number;
    }>> {
        const params = new URLSearchParams();

        if (spaceId) params.append('spaceId', spaceId.toString());
        if (options?.minRating) params.append('minRating', options.minRating.toString());
        if (options?.minEvaluations) params.append('minEvaluations', options.minEvaluations.toString());
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();

        return this.apiClient.get<PaginatedResponse<any>>(
            `/evaluations/top-rated?${queryString}`
        );
    }
}
