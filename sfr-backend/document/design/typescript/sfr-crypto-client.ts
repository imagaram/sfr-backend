/**
 * SFR暗号資産システム APIクライアント
 * 
 * @description TypeScript/JavaScriptからSFR暗号資産APIを呼び出すためのクライアントライブラリ
 * @version 1.0.0
 * @date 2025-08-20
 */

import {
    // 基底型
    UUID,
    DateString,

    // Token Management
    UserBalanceDto,
    BalanceHistoryDto,
    TransferRequestDto,
    TransferResponseDto,

    // Rewards System
    RewardIssueRequestDto,
    RewardIssueResponseDto,
    RewardCalculateRequestDto,
    RewardCalculateResponseDto,
    DailyDistributionRequestDto,
    DailyDistributionResponseDto,
    RewardHistoryDto,

    // Collections System
    CollectionRequestDto,
    CollectionResponseDto,
    MonthlyCollectionRequestDto,
    MonthlyCollectionResponseDto,
    BurnDecisionRequestDto,
    BurnDecisionResponseDto,
    CollectionHistoryDto,

    // Governance
    CouncilMembersResponseDto,
    CouncilAppointRequestDto,
    CouncilAppointResponseDto,
    CreateProposalRequestDto,
    CreateProposalResponseDto,
    ProposalDto,
    ProposalDetailDto,
    VoteRequestDto,
    VoteResponseDto,
    UserVoteDto,

    // Statistics
    StatsOverviewDto,
    CirculationStatsDto,
    RewardStatsDto,
    TopHoldersResponseDto,

    // Oracle & Audit
    OracleFeedsResponseDto,
    OracleFeedUpdateRequestDto,
    OracleFeedUpdateResponseDto,
    SystemParametersResponseDto,
    ParameterUpdateRequestDto,
    ParameterUpdateResponseDto,
    AdjustmentLogDto,

    // Query Params
    CommonQueryParams,
    BalanceHistoryQueryParams,
    CollectionHistoryQueryParams,
    ProposalsQueryParams,
    AdjustmentLogsQueryParams,

    // Helper types
    ApiResponse,
    ApiPagedResponse,
    ApiError,
    ApiRequestConfig,

    // Enums
    StatsPeriod,
    OracleDataType,
} from './sfr-crypto-types';

// =============================================================================
// HTTP クライアント基底クラス
// =============================================================================

/**
 * HTTPエラークラス
 */
export class SfrApiError extends Error {
    constructor(
        public readonly status: number,
        public readonly error: ApiError,
        message?: string
    ) {
        super(message || error.message);
        this.name = 'SfrApiError';
    }
}

/**
 * HTTP リクエスト設定
 */
interface RequestConfig {
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
    url: string;
    data?: any;
    params?: Record<string, any> | undefined;
    headers?: Record<string, string>;
}

/**
 * HTTP クライアント基底クラス
 */
export class HttpClient {
    private baseURL: string;
    private timeout: number;
    private defaultHeaders: Record<string, string>;

    constructor(config: ApiRequestConfig = {}) {
        this.baseURL = config.baseURL || '/api/v1';
        this.timeout = config.timeout || 30000;
        this.defaultHeaders = {
            'Content-Type': 'application/json',
            ...config.headers,
        };

        if (config.token) {
            this.defaultHeaders['Authorization'] = `Bearer ${config.token}`;
        }
    }

    /**
     * 認証トークンを設定
     */
    setAuthToken(token: string): void {
        this.defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    /**
     * 認証トークンを削除
     */
    removeAuthToken(): void {
        delete this.defaultHeaders['Authorization'];
    }

    /**
     * HTTPリクエストを実行
     */
    async request<T>(config: RequestConfig): Promise<T> {
        const url = this.buildUrl(config.url, config.params);
        const headers = { ...this.defaultHeaders, ...config.headers };

        const requestInit: RequestInit = {
            method: config.method,
            headers,
            signal: AbortSignal.timeout(this.timeout),
        };

        if (config.data && config.method !== 'GET') {
            requestInit.body = JSON.stringify(config.data);
        }

        try {
            const response = await fetch(url, requestInit);

            if (!response.ok) {
                let errorData: ApiError;
                try {
                    errorData = await response.json();
                } catch {
                    errorData = {
                        timestamp: new Date().toISOString(),
                        responseId: '',
                        status: 'ERROR' as any,
                        error: 'HTTP_ERROR',
                        message: `HTTP ${response.status}: ${response.statusText}`,
                        path: config.url,
                    };
                }
                throw new SfrApiError(response.status, errorData);
            }

            return await response.json();
        } catch (error) {
            if (error instanceof SfrApiError) {
                throw error;
            }

            // ネットワークエラーまたは予期しないエラー
            const apiError: ApiError = {
                timestamp: new Date().toISOString(),
                responseId: '',
                status: 'ERROR' as any,
                error: 'NETWORK_ERROR',
                message: error instanceof Error ? error.message : 'Unknown error',
                path: config.url,
            };
            throw new SfrApiError(0, apiError);
        }
    }

    /**
     * GETリクエスト
     */
    async get<T>(url: string, params?: Record<string, any>): Promise<T> {
        return this.request<T>({ method: 'GET', url, params });
    }

    /**
     * POSTリクエスト
     */
    async post<T>(url: string, data?: any): Promise<T> {
        return this.request<T>({ method: 'POST', url, data });
    }

    /**
     * PUTリクエスト
     */
    async put<T>(url: string, data?: any): Promise<T> {
        return this.request<T>({ method: 'PUT', url, data });
    }

    /**
     * DELETEリクエスト
     */
    async delete<T>(url: string): Promise<T> {
        return this.request<T>({ method: 'DELETE', url });
    }

    /**
     * PATCHリクエスト
     */
    async patch<T>(url: string, data?: any): Promise<T> {
        return this.request<T>({ method: 'PATCH', url, data });
    }

    /**
     * URLとクエリパラメータを結合
     */
    private buildUrl(path: string, params?: Record<string, any>): string {
        const url = new URL(path, this.baseURL);

        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null) {
                    url.searchParams.append(key, String(value));
                }
            });
        }

        return url.toString();
    }
}

// =============================================================================
// SFR 暗号資産 API クライアント
// =============================================================================

/**
 * SFR暗号資産APIクライアント
 */
export class SfrCryptoApiClient {
    private http: HttpClient;

    constructor(config: ApiRequestConfig = {}) {
        this.http = new HttpClient(config);
    }

    /**
     * 認証トークンを設定
     */
    setAuthToken(token: string): void {
        this.http.setAuthToken(token);
    }

    /**
     * 認証トークンを削除
     */
    removeAuthToken(): void {
        this.http.removeAuthToken();
    }

    // ========================================================================
    // Token Management API
    // ========================================================================

    /**
     * ユーザー残高を取得
     */
    async getUserBalance(userId: UUID): Promise<ApiResponse<UserBalanceDto>> {
        return this.http.get<ApiResponse<UserBalanceDto>>(`/tokens/balance/${userId}`);
    }

    /**
     * 残高変動履歴を取得
     */
    async getBalanceHistory(
        userId: UUID,
        params?: BalanceHistoryQueryParams
    ): Promise<ApiPagedResponse<BalanceHistoryDto>> {
        return this.http.get<ApiPagedResponse<BalanceHistoryDto>>(
            `/tokens/balance/${userId}/history`,
            params
        );
    }

    /**
     * SFRトークンを送金
     */
    async transferTokens(request: TransferRequestDto): Promise<TransferResponseDto> {
        return this.http.post<TransferResponseDto>('/tokens/transfer', request);
    }

    // ========================================================================
    // Rewards System API
    // ========================================================================

    /**
     * 報酬を発行
     */
    async issueReward(request: RewardIssueRequestDto): Promise<RewardIssueResponseDto> {
        return this.http.post<RewardIssueResponseDto>('/rewards/issue', request);
    }

    /**
     * 報酬額を計算（発行前の見積もり）
     */
    async calculateReward(request: RewardCalculateRequestDto): Promise<RewardCalculateResponseDto> {
        return this.http.post<RewardCalculateResponseDto>('/rewards/calculate', request);
    }

    /**
     * 日次報酬分配を実行
     */
    async distributeDaily(request: DailyDistributionRequestDto): Promise<DailyDistributionResponseDto> {
        return this.http.post<DailyDistributionResponseDto>('/rewards/distribute', request);
    }

    /**
     * ユーザーの報酬履歴を取得
     */
    async getRewardHistory(
        userId: UUID,
        params?: CommonQueryParams
    ): Promise<ApiPagedResponse<RewardHistoryDto>> {
        return this.http.get<ApiPagedResponse<RewardHistoryDto>>(
            `/rewards/history/${userId}`,
            params
        );
    }

    // ========================================================================
    // Collections System API
    // ========================================================================

    /**
     * トークンを徴収
     */
    async collectTokens(request: CollectionRequestDto): Promise<CollectionResponseDto> {
        return this.http.post<CollectionResponseDto>('/collections/collect', request);
    }

    /**
     * 月次一括徴収を実行
     */
    async monthlyCollection(request: MonthlyCollectionRequestDto): Promise<MonthlyCollectionResponseDto> {
        return this.http.post<MonthlyCollectionResponseDto>('/collections/monthly', request);
    }

    /**
     * AIバーン判断を実行
     */
    async burnDecision(request: BurnDecisionRequestDto): Promise<BurnDecisionResponseDto> {
        return this.http.post<BurnDecisionResponseDto>('/collections/burn-decision', request);
    }

    /**
     * 徴収履歴を取得
     */
    async getCollectionHistory(
        userId: UUID,
        params?: CollectionHistoryQueryParams
    ): Promise<ApiPagedResponse<CollectionHistoryDto>> {
        return this.http.get<ApiPagedResponse<CollectionHistoryDto>>(
            `/collections/history/${userId}`,
            params
        );
    }

    // ========================================================================
    // Governance API
    // ========================================================================

    /**
     * 評議員一覧を取得
     */
    async getCouncilMembers(): Promise<CouncilMembersResponseDto> {
        return this.http.get<CouncilMembersResponseDto>('/governance/council');
    }

    /**
     * 評議員を任命
     */
    async appointCouncilMember(request: CouncilAppointRequestDto): Promise<CouncilAppointResponseDto> {
        return this.http.post<CouncilAppointResponseDto>('/governance/council/appoint', request);
    }

    /**
     * 提案を作成
     */
    async createProposal(request: CreateProposalRequestDto): Promise<CreateProposalResponseDto> {
        return this.http.post<CreateProposalResponseDto>('/governance/proposals', request);
    }

    /**
     * 提案一覧を取得
     */
    async getProposals(params?: ProposalsQueryParams): Promise<ApiPagedResponse<ProposalDto>> {
        return this.http.get<ApiPagedResponse<ProposalDto>>('/governance/proposals', params);
    }

    /**
     * 提案詳細を取得
     */
    async getProposal(proposalId: UUID): Promise<ApiResponse<ProposalDetailDto>> {
        return this.http.get<ApiResponse<ProposalDetailDto>>(`/governance/proposals/${proposalId}`);
    }

    /**
     * 提案に投票
     */
    async vote(proposalId: UUID, request: VoteRequestDto): Promise<VoteResponseDto> {
        return this.http.post<VoteResponseDto>(`/governance/proposals/${proposalId}/vote`, request);
    }

    /**
     * ユーザーの投票履歴を取得
     */
    async getUserVotes(
        userId: UUID,
        params?: CommonQueryParams
    ): Promise<ApiPagedResponse<UserVoteDto>> {
        return this.http.get<ApiPagedResponse<UserVoteDto>>(
            `/governance/votes/${userId}`,
            params
        );
    }

    // ========================================================================
    // Statistics API
    // ========================================================================

    /**
     * SFR統計概要を取得
     */
    async getStatsOverview(): Promise<ApiResponse<StatsOverviewDto>> {
        return this.http.get<ApiResponse<StatsOverviewDto>>('/statistics/overview');
    }

    /**
     * 流通量統計を取得
     */
    async getCirculationStats(
        period: StatsPeriod,
        fromDate: DateString,
        toDate: DateString
    ): Promise<ApiResponse<CirculationStatsDto>> {
        return this.http.get<ApiResponse<CirculationStatsDto>>('/statistics/circulation', {
            period,
            fromDate,
            toDate,
        });
    }

    /**
     * 報酬統計を取得
     */
    async getRewardStats(
        fromDate: DateString,
        toDate: DateString,
        groupBy: 'day' | 'week' | 'month'
    ): Promise<ApiResponse<RewardStatsDto>> {
        return this.http.get<ApiResponse<RewardStatsDto>>('/statistics/rewards', {
            fromDate,
            toDate,
            groupBy,
        });
    }

    /**
     * 上位ホルダー一覧を取得
     */
    async getTopHolders(limit: number = 100): Promise<TopHoldersResponseDto> {
        return this.http.get<TopHoldersResponseDto>('/statistics/top-holders', { limit });
    }

    // ========================================================================
    // Oracle & Audit API
    // ========================================================================

    /**
     * Oracleフィード一覧を取得
     */
    async getOracleFeeds(
        dataType?: OracleDataType,
        source?: string
    ): Promise<OracleFeedsResponseDto> {
        return this.http.get<OracleFeedsResponseDto>('/oracle/feeds', {
            dataType,
            source,
        });
    }

    /**
     * Oracleフィードを更新
     */
    async updateOracleFeed(request: OracleFeedUpdateRequestDto): Promise<OracleFeedUpdateResponseDto> {
        return this.http.post<OracleFeedUpdateResponseDto>('/oracle/feeds', request);
    }

    /**
     * システムパラメータ一覧を取得
     */
    async getSystemParameters(): Promise<SystemParametersResponseDto> {
        return this.http.get<SystemParametersResponseDto>('/audit/parameters');
    }

    /**
     * システムパラメータを更新
     */
    async updateSystemParameter(
        parameterId: string,
        request: ParameterUpdateRequestDto
    ): Promise<ParameterUpdateResponseDto> {
        return this.http.put<ParameterUpdateResponseDto>(
            `/audit/parameters/${parameterId}`,
            request
        );
    }

    /**
     * 調整ログを取得
     */
    async getAdjustmentLogs(params?: AdjustmentLogsQueryParams): Promise<ApiPagedResponse<AdjustmentLogDto>> {
        return this.http.get<ApiPagedResponse<AdjustmentLogDto>>('/audit/adjustments', params);
    }
}

// =============================================================================
// クライアント Factory
// =============================================================================

/**
 * SFR暗号資産APIクライアントのファクトリー関数
 */
export function createSfrCryptoApiClient(config: ApiRequestConfig = {}): SfrCryptoApiClient {
    return new SfrCryptoApiClient(config);
}

/**
 * デフォルト設定でクライアントを作成
 */
export const defaultClient = new SfrCryptoApiClient();

// =============================================================================
// React Hook (Optional)
// =============================================================================

/**
 * React Hook用の型定義（React使用時のみ）
 */
export interface UseSfrApiConfig extends ApiRequestConfig {
    /** エラー時の自動再試行回数 */
    retryCount?: number;
    /** 再試行間隔（ミリ秒） */
    retryDelay?: number;
}

/**
 * APIリクエスト状態
 */
export interface ApiRequestState<T> {
    /** データ */
    data: T | null;
    /** ローディング状態 */
    loading: boolean;
    /** エラー */
    error: SfrApiError | null;
    /** 再試行関数 */
    retry: () => void;
}

// =============================================================================
// ユーティリティ関数
// =============================================================================

/**
 * エラーハンドリング用ヘルパー
 */
export const handleApiError = (error: unknown): SfrApiError => {
    if (error instanceof SfrApiError) {
        return error;
    }

    const apiError: ApiError = {
        timestamp: new Date().toISOString(),
        responseId: '',
        status: 'ERROR' as any,
        error: 'UNKNOWN_ERROR',
        message: error instanceof Error ? error.message : 'Unknown error occurred',
        path: '',
    };

    return new SfrApiError(0, apiError);
};

/**
 * APIレスポンスの成功判定
 */
export const isApiSuccess = <T>(response: ApiResponse<T>): boolean => {
    return response.status === 'SUCCESS' || response.status === 'PARTIAL_SUCCESS';
};

/**
 * ページネーションヘルパー
 */
export const hasNextPage = <T>(response: ApiPagedResponse<T>): boolean => {
    return response.pagination.hasNext;
};

/**
 * ページネーションヘルパー
 */
export const hasPreviousPage = <T>(response: ApiPagedResponse<T>): boolean => {
    return response.pagination.hasPrevious;
};

/**
 * 次ページ番号取得
 */
export const getNextPage = <T>(response: ApiPagedResponse<T>): number | null => {
    return response.pagination.hasNext ? response.pagination.page + 1 : null;
};

/**
 * 前ページ番号取得
 */
export const getPreviousPage = <T>(response: ApiPagedResponse<T>): number | null => {
    return response.pagination.hasPrevious ? response.pagination.page - 1 : null;
};

// =============================================================================
// デフォルトエクスポート
// =============================================================================

export default {
    SfrCryptoApiClient,
    createSfrCryptoApiClient,
    defaultClient,
    SfrApiError,
    HttpClient,
    handleApiError,
    isApiSuccess,
    hasNextPage,
    hasPreviousPage,
    getNextPage,
    getPreviousPage,
};
