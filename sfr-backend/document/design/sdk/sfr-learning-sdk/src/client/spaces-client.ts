/**
 * SFR Learning API SDK - Spaces Client
 * @author SFR.TOKYO Development Team
 * @version 2.0.0
 * @deprecated learning-spaces-client.ts は非推奨です。新しい spaces-client.ts を使用してください。
 */

import { ApiClient } from './api-client';
import {
    Space,
    SpaceCreateRequest,
    PaginatedResponse,
    SpaceMode,
    SpaceStatus
} from '../types';

/**
 * スペース管理クライアント
 * スペースの作成、取得、更新、削除機能を提供
 */
export class SpacesClient {
    constructor(private apiClient: ApiClient) { }

    /**
     * スペース一覧取得
     * @param options フィルタリングオプション
     * @returns スペース一覧（ページネーション付き）
     */
    async getSpaces(options?: {
        mode?: SpaceMode;
        status?: SpaceStatus;
        page?: number;
        size?: number;
    }): Promise<PaginatedResponse<Space>> {
        const params = new URLSearchParams();

        if (options?.mode) params.append('mode', options.mode);
        if (options?.status) params.append('status', options.status);
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();
        const url = `/spaces${queryString ? `?${queryString}` : ''}`;

        return this.apiClient.get<PaginatedResponse<Space>>(url);
    }

    /**
     * スペース詳細取得
     * @param spaceId スペースID
     * @returns スペース詳細情報
     */
    async getSpace(spaceId: number): Promise<Space> {
        return this.apiClient.get<Space>(`/spaces/${spaceId}`);
    }

    /**
     * スペース作成
     * @param request スペース作成リクエスト
     * @returns 作成されたスペース
     */
    async createSpace(request: SpaceCreateRequest): Promise<Space> {
        return this.apiClient.post<Space>('/spaces', request);
    }

    /**
     * スペース更新
     * @param spaceId スペースID
     * @param request 更新リクエスト
     * @returns 更新されたスペース
     */
    async updateSpace(spaceId: number, request: Partial<SpaceCreateRequest>): Promise<Space> {
        return this.apiClient.put<Space>(`/spaces/${spaceId}`, request);
    }

    /**
     * スペース削除
     * @param spaceId スペースID
     */
    async deleteSpace(spaceId: number): Promise<void> {
        await this.apiClient.delete(`/spaces/${spaceId}`);
    }

    /**
     * スペースへの参加申請/参加
     * @param spaceId スペースID
     * @param characterId キャラクターID（オプション）
     * @returns 参加結果
     */
    async joinSpace(spaceId: number, characterId?: string): Promise<{
        status: 'JOINED' | 'PENDING_APPROVAL';
        message: string;
        spaceId: number;
    }> {
        const data = characterId ? { characterId } : {};
        return this.apiClient.post(`/spaces/${spaceId}/join`, data);
    }

    /**
     * スペースからの退出
     * @param spaceId スペースID
     */
    async leaveSpace(spaceId: number): Promise<void> {
        await this.apiClient.post(`/spaces/${spaceId}/leave`);
    }

    /**
     * スペースのメンバー一覧取得
     * @param spaceId スペースID
     * @returns メンバー一覧
     */
    async getSpaceMembers(spaceId: number): Promise<any[]> {
        const space = await this.getSpace(spaceId);
        // 詳細レスポンスに含まれるメンバー情報を返す
        return (space as any).members || [];
    }

    /**
     * スペースの設定取得
     * @param spaceId スペースID
     * @returns 設定情報
     */
    async getSpaceConfig(spaceId: number): Promise<any> {
        return this.apiClient.get(`/spaces/${spaceId}/config`);
    }

    /**
     * スペースの設定更新
     * @param spaceId スペースID
     * @param config 設定情報
     * @returns 更新された設定
     */
    async updateSpaceConfig(spaceId: number, config: any): Promise<any> {
        return this.apiClient.put(`/spaces/${spaceId}/config`, config);
    }

    /**
     * 公開スペースの検索
     * @param query 検索クエリ
     * @param options 検索オプション
     * @returns 検索結果
     */
    async searchPublicSpaces(
        query: string,
        options?: {
            mode?: SpaceMode;
            difficulty?: string;
            page?: number;
            size?: number;
        }
    ): Promise<PaginatedResponse<Space>> {
        const params = new URLSearchParams();
        
        params.append('query', query);
        if (options?.mode) params.append('mode', options.mode);
        if (options?.difficulty) params.append('difficulty', options.difficulty);
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();
        return this.apiClient.get<PaginatedResponse<Space>>(`/spaces/search?${queryString}`);
    }

    /**
     * 人気のスペース取得
     * @param limit 取得件数
     * @returns 人気のスペース一覧
     */
    async getPopularSpaces(limit: number = 10): Promise<Space[]> {
        return this.apiClient.get<Space[]>(`/spaces/popular?limit=${limit}`);
    }

    /**
     * 推奨スペース取得
     * @param userId ユーザーID（オプション、未指定の場合は認証済みユーザー）
     * @returns 推奨スペース一覧
     */
    async getRecommendedSpaces(userId?: string): Promise<Space[]> {
        const url = userId ? `/spaces/recommended?userId=${userId}` : '/spaces/recommended';
        return this.apiClient.get<Space[]>(url);
    }
}
