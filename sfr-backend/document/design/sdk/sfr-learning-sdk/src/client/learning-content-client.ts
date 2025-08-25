/**
 * SFR Learning API SDK - Learning Content Client
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 */

import { ApiClient } from './api-client';
import {
    LearningContent,
    LearningContentCreateRequest,
    PaginatedResponse,
    ContentType,
    ContentDifficulty,
    LearningProgress,
    ProgressRecordRequest
} from '../types';

/**
 * 学習コンテンツ管理クライアント
 * コンテンツの取得、作成、更新、削除、進捗管理機能を提供
 */
export class LearningContentClient {
    constructor(private apiClient: ApiClient) { }

    /**
     * 学習コンテンツ一覧取得
     * @param spaceId 学習空間ID
     * @param options フィルタリングオプション
     * @returns 学習コンテンツ一覧
     */
    async getContent(
        spaceId: number,
        options?: {
            contentType?: ContentType;
            published?: boolean;
            page?: number;
            size?: number;
        }
    ): Promise<PaginatedResponse<LearningContent>> {
        const params = new URLSearchParams();

        if (options?.contentType) params.append('contentType', options.contentType);
        if (options?.published !== undefined) params.append('published', options.published.toString());
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();
        const url = `/spaces/${spaceId}/content${queryString ? `?${queryString}` : ''}`;

        return this.apiClient.get<PaginatedResponse<LearningContent>>(url);
    }

    /**
     * 学習コンテンツ詳細取得
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     * @returns 学習コンテンツ詳細
     */
    async getContentById(spaceId: number, contentId: number): Promise<LearningContent> {
        return this.apiClient.get<LearningContent>(`/spaces/${spaceId}/content/${contentId}`);
    }

    /**
     * 学習コンテンツ作成
     * @param spaceId 学習空間ID
     * @param request 作成リクエスト
     * @returns 作成されたコンテンツ
     */
    async createContent(
        spaceId: number,
        request: LearningContentCreateRequest
    ): Promise<LearningContent> {
        // ファイルアップロードが含まれる場合はマルチパートフォーム
        if (request.file) {
            const formData = new FormData();
            formData.append('title', request.title);
            if (request.description) formData.append('description', request.description);
            formData.append('contentType', request.contentType);
            if (request.content) formData.append('content', request.content);
            formData.append('file', request.file);
            if (request.duration !== undefined) formData.append('duration', request.duration.toString());
            formData.append('difficulty', request.difficulty);
            if (request.tags) formData.append('tags', JSON.stringify(request.tags));
            if (request.isPublished !== undefined) formData.append('isPublished', request.isPublished.toString());
            if (request.order !== undefined) formData.append('order', request.order.toString());

            return this.apiClient.postMultipart<LearningContent>(
                `/spaces/${spaceId}/content`,
                formData
            );
        } else {
            return this.apiClient.post<LearningContent>(`/spaces/${spaceId}/content`, request);
        }
    }

    /**
     * 学習コンテンツ更新
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     * @param request 更新リクエスト
     * @returns 更新されたコンテンツ
     */
    async updateContent(
        spaceId: number,
        contentId: number,
        request: Partial<LearningContentCreateRequest>
    ): Promise<LearningContent> {
        // ファイルアップロードが含まれる場合はマルチパートフォーム
        if (request.file) {
            const formData = new FormData();
            if (request.title) formData.append('title', request.title);
            if (request.description) formData.append('description', request.description);
            if (request.content) formData.append('content', request.content);
            formData.append('file', request.file);
            if (request.duration !== undefined) formData.append('duration', request.duration.toString());
            if (request.difficulty) formData.append('difficulty', request.difficulty);
            if (request.tags) formData.append('tags', JSON.stringify(request.tags));
            if (request.isPublished !== undefined) formData.append('isPublished', request.isPublished.toString());
            if (request.order !== undefined) formData.append('order', request.order.toString());

            return this.apiClient.postMultipart<LearningContent>(
                `/spaces/${spaceId}/content/${contentId}`,
                formData
            );
        } else {
            return this.apiClient.put<LearningContent>(
                `/spaces/${spaceId}/content/${contentId}`,
                request
            );
        }
    }

    /**
     * 学習コンテンツ削除
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     */
    async deleteContent(spaceId: number, contentId: number): Promise<void> {
        await this.apiClient.delete(`/spaces/${spaceId}/content/${contentId}`);
    }

    /**
     * 学習進捗取得
     * @param spaceId 学習空間ID
     * @returns 学習進捗情報
     */
    async getProgress(spaceId: number): Promise<LearningProgress> {
        return this.apiClient.get<LearningProgress>(`/spaces/${spaceId}/progress`);
    }

    /**
     * 学習進捗記録
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     * @param request 進捗記録リクエスト
     * @returns 進捗記録結果
     */
    async recordProgress(
        spaceId: number,
        contentId: number,
        request: ProgressRecordRequest
    ): Promise<any> {
        return this.apiClient.post(
            `/spaces/${spaceId}/content/${contentId}/progress`,
            request
        );
    }

    /**
     * コンテンツの検索
     * @param spaceId 学習空間ID
     * @param query 検索クエリ
     * @param options 検索オプション
     * @returns 検索結果
     */
    async searchContent(
        spaceId: number,
        query: string,
        options?: {
            contentType?: ContentType;
            difficulty?: ContentDifficulty;
            tags?: string[];
            page?: number;
            size?: number;
        }
    ): Promise<PaginatedResponse<LearningContent>> {
        const params = new URLSearchParams();
        params.append('q', query);

        if (options?.contentType) params.append('contentType', options.contentType);
        if (options?.difficulty) params.append('difficulty', options.difficulty);
        if (options?.tags) params.append('tags', options.tags.join(','));
        if (options?.page !== undefined) params.append('page', options.page.toString());
        if (options?.size !== undefined) params.append('size', options.size.toString());

        const queryString = params.toString();

        return this.apiClient.get<PaginatedResponse<LearningContent>>(
            `/spaces/${spaceId}/content/search?${queryString}`
        );
    }

    /**
     * 次に学習すべきコンテンツの取得
     * @param spaceId 学習空間ID
     * @returns 次のコンテンツ
     */
    async getNextContent(spaceId: number): Promise<LearningContent | null> {
        try {
            return await this.apiClient.get<LearningContent>(`/spaces/${spaceId}/content/next`);
        } catch (error: any) {
            if (error.response?.status === 404) {
                return null; // 次のコンテンツがない場合
            }
            throw error;
        }
    }

    /**
     * コンテンツの前提条件チェック
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     * @returns 前提条件チェック結果
     */
    async checkPrerequisites(spaceId: number, contentId: number): Promise<{
        canAccess: boolean;
        missingPrerequisites: LearningContent[];
    }> {
        return this.apiClient.get(
            `/spaces/${spaceId}/content/${contentId}/prerequisites`
        );
    }

    /**
     * コンテンツのダウンロード
     * @param spaceId 学習空間ID
     * @param contentId コンテンツID
     * @returns ダウンロードURL
     */
    async downloadContent(spaceId: number, contentId: number): Promise<string> {
        const response = await this.apiClient.get<{ downloadUrl: string }>(
            `/spaces/${spaceId}/content/${contentId}/download`
        );
        return response.downloadUrl;
    }
}
