/**
 * SFR Learning API SDK - HTTP Client
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import {
    SfrLearningConfig,
    RequestConfig,
    ErrorResponse,
    ApiResponse
} from '../types';

/**
 * APIクライアントのベースクラス
 * 認証、エラーハンドリング、リトライ機能を提供
 */
export class ApiClient {
    private client: AxiosInstance;
    private config: SfrLearningConfig;
    private accessToken?: string;

    constructor(config: SfrLearningConfig) {
        this.config = {
            timeout: 10000,
            retryAttempts: 3,
            debug: false,
            ...config
        };

        // Axiosインスタンスの作成
        this.client = axios.create({
            baseURL: this.config.baseURL,
            timeout: this.config.timeout,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        // リクエストインターセプター
        this.client.interceptors.request.use(
            (config) => {
                // 認証トークンの設定
                if (this.accessToken) {
                    config.headers['Authorization'] = `Bearer ${this.accessToken}`;
                }

                // APIキーの設定
                if (this.config.apiKey) {
                    config.headers['X-API-Key'] = this.config.apiKey;
                }

                // デバッグログ
                if (this.config.debug) {
                    console.log('🚀 API Request:', {
                        method: config.method?.toUpperCase(),
                        url: config.url,
                        headers: config.headers,
                        data: config.data
                    });
                }

                return config;
            },
            (error) => {
                return Promise.reject(error);
            }
        );

        // レスポンスインターセプター
        this.client.interceptors.response.use(
            (response) => {
                // デバッグログ
                if (this.config.debug) {
                    console.log('✅ API Response:', {
                        status: response.status,
                        data: response.data,
                        headers: response.headers
                    });
                }

                return response;
            },
            (error) => {
                // エラーログ
                if (this.config.debug) {
                    console.error('❌ API Error:', {
                        status: error.response?.status,
                        data: error.response?.data,
                        message: error.message
                    });
                }

                // エラーレスポンスの正規化
                if (error.response?.data) {
                    const errorResponse: ErrorResponse = error.response.data;
                    error.message = errorResponse.message || error.message;
                }

                return Promise.reject(error);
            }
        );
    }

    /**
     * アクセストークンを設定
     * @param token JWTアクセストークン
     */
    setAccessToken(token: string): void {
        this.accessToken = token;
    }

    /**
     * アクセストークンをクリア
     */
    clearAccessToken(): void {
        this.accessToken = undefined;
    }

    /**
     * GET リクエスト
     */
    async get<T>(url: string, config?: RequestConfig): Promise<T> {
        return this.request<T>('GET', url, undefined, config);
    }

    /**
     * POST リクエスト
     */
    async post<T>(url: string, data?: any, config?: RequestConfig): Promise<T> {
        return this.request<T>('POST', url, data, config);
    }

    /**
     * PUT リクエスト
     */
    async put<T>(url: string, data?: any, config?: RequestConfig): Promise<T> {
        return this.request<T>('PUT', url, data, config);
    }

    /**
     * DELETE リクエスト
     */
    async delete<T>(url: string, config?: RequestConfig): Promise<T> {
        return this.request<T>('DELETE', url, undefined, config);
    }

    /**
     * PATCH リクエスト
     */
    async patch<T>(url: string, data?: any, config?: RequestConfig): Promise<T> {
        return this.request<T>('PATCH', url, data, config);
    }

    /**
     * マルチパートフォームデータ リクエスト
     */
    async postMultipart<T>(
        url: string,
        formData: FormData,
        config?: RequestConfig
    ): Promise<T> {
        const multipartConfig: RequestConfig = {
            ...config,
            headers: {
                ...config?.headers,
                'Content-Type': 'multipart/form-data'
            }
        };
        return this.request<T>('POST', url, formData, multipartConfig);
    }

    /**
     * 汎用リクエストメソッド（リトライ機能付き）
     */
    private async request<T>(
        method: string,
        url: string,
        data?: any,
        config?: RequestConfig
    ): Promise<T> {
        const requestConfig: AxiosRequestConfig = {
            method,
            url,
            data,
            headers: config?.headers,
            timeout: config?.timeout || this.config.timeout
        };

        let lastError: any;
        const maxRetries = config?.retries ?? this.config.retryAttempts ?? 3;

        for (let attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                const response: AxiosResponse<T> = await this.client.request(requestConfig);
                return response.data;
            } catch (error: any) {
                lastError = error;

                // リトライ対象外のエラー
                if (
                    attempt === maxRetries ||
                    error.response?.status < 500 ||
                    error.response?.status === 401 ||
                    error.response?.status === 403 ||
                    error.response?.status === 404
                ) {
                    break;
                }

                // リトライ前の待機時間（指数バックオフ）
                const delayMs = Math.min(1000 * Math.pow(2, attempt), 5000);
                await new Promise(resolve => setTimeout(resolve, delayMs));

                if (this.config.debug) {
                    console.warn(`🔄 Retrying request (attempt ${attempt + 1}/${maxRetries + 1}):`, {
                        method,
                        url,
                        error: error.message
                    });
                }
            }
        }

        throw lastError;
    }

    /**
     * ヘルスチェック
     */
    async healthCheck(): Promise<{ status: string; timestamp: string }> {
        try {
            const response = await this.get<{ status: string; timestamp: string }>('/health');
            return response;
        } catch (error) {
            throw new Error('API health check failed');
        }
    }

    /**
     * 設定の取得
     */
    getConfig(): SfrLearningConfig {
        return { ...this.config };
    }

    /**
     * Axiosインスタンスの取得（上級者向け）
     */
    getAxiosInstance(): AxiosInstance {
        return this.client;
    }
}
