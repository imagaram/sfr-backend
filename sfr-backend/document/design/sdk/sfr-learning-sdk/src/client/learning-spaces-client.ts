/**
 * SFR Learning API SDK - Learning Spaces Client
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 */

import { ApiClient } from './api-client';
import {
    LearningSpace,
    LearningSpaceCreateRequest,
    PaginatedResponse,
    LearningMode,
    LearningSpaceStatus
} from '../types';

/**
 * 学習空間管理クライアント
 * 学習空間の作成、取得、更新、削除機能を提供
 */
export class LearningSpacesClient {
    constructor(private apiClient: ApiClient) { }

    /**
     * 学習空間一覧取得
     * @param options フィルタリングオプション
     * @returns 学習空間一覧（ページネーション付き）
     */
    async getCourses(options?: {
        mode?: LearningMode;
        status?: LearningSpaceStatus;
        page?: number;
        size?: number;
    }): Promise<PaginatedResponse<LearningSpace>> {
        const params = new URLSearchParams();

        if (options?.mode) params.append('mode', options.mode);
        if (options?.status) params.append('status', options.status);
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();
        const url = `/spaces${queryString ? `?${queryString}` : ''}`;

        return this.apiClient.get<PaginatedResponse<LearningSpace>>(url);
    }

    /**
     * 学習空間詳細取得
     * @param spaceId 学習空間ID
     * @returns 学習空間詳細情報
     */
    async getCourse(spaceId: number): Promise<LearningSpace> {
        return this.apiClient.get<LearningSpace>(`/spaces/${spaceId}`);
    }

    /**
     * 学習空間作成
     * @param request 学習空間作成リクエスト
     * @returns 作成された学習空間
     */
    async createCourse(request: LearningSpaceCreateRequest): Promise<LearningSpace> {
        return this.apiClient.post<LearningSpace>('/spaces', request);
    }

    /**
     * 学習空間更新
     * @param spaceId 学習空間ID
     * @param request 更新リクエスト
     * @returns 更新された学習空間
     */
    async updateCourse(
        spaceId: number,
        request: Partial<LearningSpaceCreateRequest>
    ): Promise<LearningSpace> {
        return this.apiClient.put<LearningSpace>(`/spaces/${spaceId}`, request);
    }

    /**
     * 学習空間削除
     * @param spaceId 学習空間ID
     */
    async deleteCourse(spaceId: number): Promise<void> {
        await this.apiClient.delete(`/spaces/${spaceId}`);
    }

    /**
     * 学習空間への参加申請/参加
     * @param spaceId 学習空間ID
     * @param characterId キャラクターID（オプション）
     * @returns 参加結果
     */
    async enrollCourse(spaceId: number, characterId?: string): Promise<{
        status: 'JOINED' | 'PENDING_APPROVAL';
        message: string;
        spaceId: number;
    }> {
        const data = characterId ? { characterId } : {};
        return this.apiClient.post(`/spaces/${spaceId}/join`, data);
    }

    /**
     * 学習空間からの退出
     * @param spaceId 学習空間ID
     */
    async leaveCourse(spaceId: number): Promise<void> {
        await this.apiClient.post(`/spaces/${spaceId}/leave`);
    }

    /**
     * 学習空間のメンバー一覧取得
     * @param spaceId 学習空間ID
     * @returns メンバー一覧
     */
    async getCourseMembers(spaceId: number): Promise<any[]> {
        const space = await this.getCourse(spaceId);
        // 詳細レスポンスに含まれるメンバー情報を返す
        return (space as any).members || [];
    }

    /**
     * 学習空間の設定取得
     * @param spaceId 学習空間ID
     * @returns 設定情報
     */
    async getCourseConfig(spaceId: number): Promise<any> {
        return this.apiClient.get(`/spaces/${spaceId}/config`);
    }

    /**
     * 学習空間の設定更新
     * @param spaceId 学習空間ID
     * @param config 設定情報
     * @returns 更新された設定
     */
    async updateCourseConfig(spaceId: number, config: any): Promise<any> {
        return this.apiClient.put(`/spaces/${spaceId}/config`, config);
    }

    /**
     * 公開学習空間の検索
     * @param query 検索クエリ
     * @param options 検索オプション
     * @returns 検索結果
     */
    async searchPublicCourses(
        query: string,
        options?: {
            mode?: LearningMode;
            difficulty?: string;
            page?: number;
            size?: number;
        }
    ): Promise<PaginatedResponse<LearningSpace>> {
        const params = new URLSearchParams();
        params.append('q', query);

        if (options?.mode) params.append('mode', options.mode);
        if (options?.difficulty) params.append('difficulty', options.difficulty);
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();

        return this.apiClient.get<PaginatedResponse<LearningSpace>>(
            `/spaces/search?${queryString}`
        );
    }

    /**
     * 人気の学習空間取得
     * @param limit 取得件数
     * @returns 人気の学習空間一覧
     */
    async getPopularCourses(limit: number = 10): Promise<LearningSpace[]> {
        return this.apiClient.get<LearningSpace[]>(`/spaces/popular?limit=${limit}`);
    }

    /**
     * 推奨学習空間取得
     * @param userId ユーザーID（オプション、未指定の場合は認証済みユーザー）
     * @returns 推奨学習空間一覧
     */
    async getRecommendedCourses(userId?: string): Promise<LearningSpace[]> {
        const url = userId
            ? `/spaces/recommended?userId=${userId}`
            : '/spaces/recommended';

        return this.apiClient.get<LearningSpace[]>(url);
    }
}
