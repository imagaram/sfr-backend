/**
 * SFR暗号資産システム バリデーション・ユーティリティモジュール
 * 
 * @description データバリデーション、React Hooks、各種ヘルパー関数を提供
 * @version 1.0.0
 * @date 2025-08-20
 */

import {
    // 基底型
    SfrCryptoApiClient,
    SfrApiError,
    ApiRequestState,
    UseSfrApiConfig,
} from './sfr-crypto-client';

import {
    UUID,
    SFRAmount,
    DateString,
    DateTimeString,
    TransactionType,
    ProposalStatus,
    VoteChoice,
    StatsPeriod,
    ApiResponse,
    ApiPagedResponse,
} from './sfr-crypto-types';

// =============================================================================
// バリデーション関数
// =============================================================================

/**
 * バリデーション結果
 */
export interface ValidationResult {
    /** バリデーション通過フラグ */
    isValid: boolean;
    /** エラーメッセージ */
    errors: string[];
}

/**
 * バリデーションエラー
 */
export class ValidationError extends Error {
    constructor(public readonly errors: string[]) {
        super(`Validation failed: ${errors.join(', ')}`);
        this.name = 'ValidationError';
    }
}

/**
 * SFR金額のバリデーション
 */
export const validateSFRAmount = (amount: string): ValidationResult => {
    const errors: string[] = [];

    // 必須チェック
    if (!amount || amount.trim() === '') {
        errors.push('金額は必須です');
        return { isValid: false, errors };
    }

    // 数値形式チェック
    const regex = /^\d+(\.\d{1,8})?$/;
    if (!regex.test(amount)) {
        errors.push('金額は数値で、小数点以下8桁まで入力可能です');
    }

    // 値の範囲チェック
    const numValue = parseFloat(amount);
    if (isNaN(numValue)) {
        errors.push('金額が無効な数値です');
    } else if (numValue < 0) {
        errors.push('金額は0以上である必要があります');
    } else if (numValue > 1000000000) {
        errors.push('金額が上限を超えています');
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * UUIDのバリデーション
 */
export const validateUUID = (uuid: string): ValidationResult => {
    const errors: string[] = [];

    if (!uuid || uuid.trim() === '') {
        errors.push('IDは必須です');
        return { isValid: false, errors };
    }

    const regex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    if (!regex.test(uuid)) {
        errors.push('有効なUUID形式ではありません');
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * 日付文字列のバリデーション
 */
export const validateDateString = (date: string): ValidationResult => {
    const errors: string[] = [];

    if (!date || date.trim() === '') {
        errors.push('日付は必須です');
        return { isValid: false, errors };
    }

    const regex = /^\d{4}-\d{2}-\d{2}$/;
    if (!regex.test(date)) {
        errors.push('日付はYYYY-MM-DD形式で入力してください');
    } else {
        const parsedDate = new Date(date);
        if (isNaN(parsedDate.getTime())) {
            errors.push('有効な日付ではありません');
        }
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * 日時文字列のバリデーション
 */
export const validateDateTimeString = (datetime: string): ValidationResult => {
    const errors: string[] = [];

    if (!datetime || datetime.trim() === '') {
        errors.push('日時は必須です');
        return { isValid: false, errors };
    }

    const regex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/;
    if (!regex.test(datetime)) {
        errors.push('日時はYYYY-MM-DDTHH:mm:ss形式で入力してください');
    } else {
        const parsedDate = new Date(datetime);
        if (isNaN(parsedDate.getTime())) {
            errors.push('有効な日時ではありません');
        }
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * メールアドレスのバリデーション
 */
export const validateEmail = (email: string): ValidationResult => {
    const errors: string[] = [];

    if (!email || email.trim() === '') {
        errors.push('メールアドレスは必須です');
        return { isValid: false, errors };
    }

    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regex.test(email)) {
        errors.push('有効なメールアドレス形式ではありません');
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * スコアのバリデーション（0-100）
 */
export const validateScore = (score: number, min: number = 0, max: number = 100): ValidationResult => {
    const errors: string[] = [];

    if (score === null || score === undefined) {
        errors.push('スコアは必須です');
        return { isValid: false, errors };
    }

    if (isNaN(score)) {
        errors.push('スコアは数値である必要があります');
    } else if (score < min) {
        errors.push(`スコアは${min}以上である必要があります`);
    } else if (score > max) {
        errors.push(`スコアは${max}以下である必要があります`);
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * 評価スコアのバリデーション（1.0-5.0）
 */
export const validateEvaluationScore = (score: number): ValidationResult => {
    return validateScore(score, 1.0, 5.0);
};

/**
 * パスワードのバリデーション
 */
export const validatePassword = (password: string): ValidationResult => {
    const errors: string[] = [];

    if (!password || password.trim() === '') {
        errors.push('パスワードは必須です');
        return { isValid: false, errors };
    }

    if (password.length < 8) {
        errors.push('パスワードは8文字以上である必要があります');
    }

    if (password.length > 128) {
        errors.push('パスワードは128文字以下である必要があります');
    }

    if (!/[a-z]/.test(password)) {
        errors.push('パスワードには小文字を含める必要があります');
    }

    if (!/[A-Z]/.test(password)) {
        errors.push('パスワードには大文字を含める必要があります');
    }

    if (!/\d/.test(password)) {
        errors.push('パスワードには数字を含める必要があります');
    }

    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
        errors.push('パスワードには特殊文字を含める必要があります');
    }

    return { isValid: errors.length === 0, errors };
};

/**
 * 複数のバリデーション結果をマージ
 */
export const mergeValidationResults = (...results: ValidationResult[]): ValidationResult => {
    const allErrors = results.flatMap(result => result.errors);
    return {
        isValid: allErrors.length === 0,
        errors: allErrors,
    };
};

/**
 * バリデーション結果をスロー
 */
export const throwIfInvalid = (result: ValidationResult): void => {
    if (!result.isValid) {
        throw new ValidationError(result.errors);
    }
};

// =============================================================================
// フォーマット関数
// =============================================================================

/**
 * SFR金額フォーマット（表示用）
 */
export const formatSFRAmount = (
    amount: SFRAmount,
    options: {
        decimals?: number;
        thousandsSeparator?: boolean;
        currency?: boolean;
    } = {}
): string => {
    const {
        decimals = 8,
        thousandsSeparator = true,
        currency = true
    } = options;

    const numValue = parseFloat(amount);
    if (isNaN(numValue)) {
        return '0';
    }

    let formatted = numValue.toFixed(decimals);

    // 末尾の0を削除
    formatted = formatted.replace(/\.?0+$/, '');

    // 千の位区切り
    if (thousandsSeparator) {
        const parts = formatted.split('.');
        if (parts[0]) {
            parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        }
        formatted = parts.join('.');
    }

    // 通貨記号
    if (currency) {
        formatted = `${formatted} SFR`;
    }

    return formatted;
};

/**
 * 日付フォーマット（日本語表示）
 */
export const formatDate = (
    date: DateString | DateTimeString,
    options: {
        format?: 'short' | 'medium' | 'long' | 'full';
        locale?: string;
    } = {}
): string => {
    const {
        format = 'medium',
        locale = 'ja-JP'
    } = options;

    const dateObj = new Date(date);
    if (isNaN(dateObj.getTime())) {
        return '無効な日付';
    }

    const formatOptions: Record<string, Intl.DateTimeFormatOptions> = {
        short: { year: 'numeric', month: 'numeric', day: 'numeric' },
        medium: { year: 'numeric', month: 'long', day: 'numeric' },
        long: { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' },
        full: { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', era: 'long' },
    };

    return dateObj.toLocaleDateString(locale, formatOptions[format]);
};

/**
 * 日時フォーマット（日本語表示）
 */
export const formatDateTime = (
    datetime: DateTimeString,
    options: {
        dateFormat?: 'short' | 'medium' | 'long';
        timeFormat?: 'short' | 'medium' | 'long';
        locale?: string;
    } = {}
): string => {
    const {
        dateFormat = 'medium',
        timeFormat = 'medium',
        locale = 'ja-JP'
    } = options;

    const dateObj = new Date(datetime);
    if (isNaN(dateObj.getTime())) {
        return '無効な日時';
    }

    const formatOptions: Intl.DateTimeFormatOptions = {
        year: 'numeric',
        month: dateFormat === 'short' ? 'numeric' : 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric',
        second: timeFormat === 'long' ? 'numeric' : undefined,
    };

    return dateObj.toLocaleString(locale, formatOptions);
};

/**
 * 相対時間フォーマット（例: 3分前、1時間前）
 */
export const formatRelativeTime = (
    datetime: DateTimeString,
    options: {
        locale?: string;
        style?: 'long' | 'short' | 'narrow';
    } = {}
): string => {
    const {
        locale = 'ja-JP',
        style = 'long'
    } = options;

    const dateObj = new Date(datetime);
    const now = new Date();
    const diffMs = now.getTime() - dateObj.getTime();

    if (isNaN(dateObj.getTime())) {
        return '無効な日時';
    }

    const rtf = new Intl.RelativeTimeFormat(locale, { style });

    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffDays > 0) {
        return rtf.format(-diffDays, 'day');
    } else if (diffHours > 0) {
        return rtf.format(-diffHours, 'hour');
    } else if (diffMinutes > 0) {
        return rtf.format(-diffMinutes, 'minute');
    } else {
        return rtf.format(-diffSeconds, 'second');
    }
};

/**
 * パーセント表示フォーマット
 */
export const formatPercentage = (
    value: number,
    options: {
        decimals?: number;
        showSign?: boolean;
    } = {}
): string => {
    const {
        decimals = 2,
        showSign = false
    } = options;

    const percentage = (value * 100).toFixed(decimals);
    const sign = showSign && value > 0 ? '+' : '';
    return `${sign}${percentage}%`;
};

// =============================================================================
// Enum表示名マッピング
// =============================================================================

/**
 * TransactionType表示名
 */
export const getTransactionTypeDisplayName = (type: TransactionType): string => {
    const names: Record<TransactionType, string> = {
        [TransactionType.EARN]: '報酬獲得',
        [TransactionType.SPEND]: '使用・支払い',
        [TransactionType.COLLECT]: '徴収',
        [TransactionType.BURN]: 'バーン（焼却）',
        [TransactionType.TRANSFER]: '送金・転送',
    };
    return names[type] || type;
};

/**
 * ProposalStatus表示名
 */
export const getProposalStatusDisplayName = (status: ProposalStatus): string => {
    const names: Record<ProposalStatus, string> = {
        [ProposalStatus.DRAFT]: '下書き',
        [ProposalStatus.VOTING]: '投票中',
        [ProposalStatus.PASSED]: '可決',
        [ProposalStatus.REJECTED]: '否決',
        [ProposalStatus.EXPIRED]: '期限切れ',
    };
    return names[status] || status;
};

/**
 * VoteChoice表示名
 */
export const getVoteChoiceDisplayName = (choice: VoteChoice): string => {
    const names: Record<VoteChoice, string> = {
        [VoteChoice.YES]: '賛成',
        [VoteChoice.NO]: '反対',
        [VoteChoice.ABSTAIN]: '棄権',
    };
    return names[choice] || choice;
};

/**
 * StatsPeriod表示名
 */
export const getStatsPeriodDisplayName = (period: StatsPeriod): string => {
    const names: Record<StatsPeriod, string> = {
        [StatsPeriod.DAILY]: '日次',
        [StatsPeriod.WEEKLY]: '週次',
        [StatsPeriod.MONTHLY]: '月次',
    };
    return names[period] || period;
};

// =============================================================================
// React Hooks（React使用時のみ）
// =============================================================================

// NOTE: この部分はReactを使用する場合のみ有効です
// React未使用の場合は無視してください

/**
 * SFR APIクライアントを使用するためのReact Hook
 */
declare function useSfrApiClient(config?: UseSfrApiConfig): SfrCryptoApiClient;

/**
 * 非同期データフェッチ用React Hook
 */
declare function useApiRequest<T>(
    requestFn: () => Promise<T>,
    dependencies?: any[]
): ApiRequestState<T>;

/**
 * ユーザー残高取得Hook
 */
declare function useUserBalance(userId: UUID): ApiRequestState<ApiResponse<any>>;

/**
 * 提案一覧取得Hook
 */
declare function useProposals(params?: any): ApiRequestState<ApiPagedResponse<any>>;

/**
 * SFR統計概要取得Hook
 */
declare function useStatsOverview(): ApiRequestState<ApiResponse<any>>;

// React Hooks の実装例（実際にはReactライブラリが必要）
/*
import { useState, useEffect, useCallback, useMemo } from 'react';

export const useSfrApiClient = (config: UseSfrApiConfig = {}): SfrCryptoApiClient => {
  return useMemo(() => createSfrCryptoApiClient(config), [config]);
};

export const useApiRequest = <T>(
  requestFn: () => Promise<T>,
  dependencies: any[] = []
): ApiRequestState<T> => {
  const [state, setState] = useState<ApiRequestState<T>>({
    data: null,
    loading: false,
    error: null,
    retry: () => {},
  });

  const executeRequest = useCallback(async () => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    
    try {
      const data = await requestFn();
      setState(prev => ({ ...prev, data, loading: false }));
    } catch (error) {
      const apiError = handleApiError(error);
      setState(prev => ({ ...prev, error: apiError, loading: false }));
    }
  }, dependencies);

  useEffect(() => {
    executeRequest();
  }, [executeRequest]);

  const retry = useCallback(() => {
    executeRequest();
  }, [executeRequest]);

  return { ...state, retry };
};

export const useUserBalance = (userId: UUID): ApiRequestState<ApiResponse<any>> => {
  const client = useSfrApiClient();
  
  return useApiRequest(
    () => client.getUserBalance(userId),
    [userId]
  );
};

export const useProposals = (params: any = {}): ApiRequestState<ApiPagedResponse<any>> => {
  const client = useSfrApiClient();
  
  return useApiRequest(
    () => client.getProposals(params),
    [JSON.stringify(params)]
  );
};

export const useStatsOverview = (): ApiRequestState<ApiResponse<any>> => {
  const client = useSfrApiClient();
  
  return useApiRequest(
    () => client.getStatsOverview(),
    []
  );
};
*/

// =============================================================================
// ユーティリティヘルパー
// =============================================================================

/**
 * APIエラーメッセージの日本語化
 */
export const getLocalizedErrorMessage = (error: SfrApiError): string => {
    const errorMessages: Record<string, string> = {
        // 一般的なエラー
        'NETWORK_ERROR': 'ネットワークエラーが発生しました',
        'TIMEOUT_ERROR': 'リクエストがタイムアウトしました',
        'UNKNOWN_ERROR': '予期しないエラーが発生しました',

        // 認証・認可エラー
        'UNAUTHORIZED': '認証が必要です',
        'FORBIDDEN': 'この操作を行う権限がありません',
        'TOKEN_EXPIRED': '認証トークンが期限切れです',

        // バリデーションエラー
        'VALIDATION_ERROR': '入力内容に誤りがあります',
        'INVALID_AMOUNT': '金額が無効です',
        'INVALID_USER_ID': 'ユーザーIDが無効です',
        'INVALID_PROPOSAL_ID': '提案IDが無効です',

        // ビジネスロジックエラー
        'INSUFFICIENT_BALANCE': '残高が不足しています',
        'USER_NOT_FOUND': 'ユーザーが見つかりません',
        'PROPOSAL_NOT_FOUND': '提案が見つかりません',
        'VOTING_CLOSED': '投票期間が終了しています',
        'ALREADY_VOTED': '既に投票済みです',
        'COLLECTION_NOT_ALLOWED': '徴収できません',
        'REWARD_CALCULATION_FAILED': '報酬計算に失敗しました',

        // システムエラー
        'INTERNAL_SERVER_ERROR': 'サーバー内部エラーが発生しました',
        'SERVICE_UNAVAILABLE': 'サービスが一時的に利用できません',
        'RATE_LIMIT_EXCEEDED': 'リクエスト制限を超過しました',
        'MAINTENANCE_MODE': 'メンテナンス中です',
    };

    return errorMessages[error.error.error] || error.error.message || 'エラーが発生しました';
};

/**
 * 安全な数値変換
 */
export const safeParseFloat = (value: string, defaultValue: number = 0): number => {
    const parsed = parseFloat(value);
    return isNaN(parsed) ? defaultValue : parsed;
};

/**
 * 安全な整数変換
 */
export const safeParseInt = (value: string, defaultValue: number = 0): number => {
    const parsed = parseInt(value, 10);
    return isNaN(parsed) ? defaultValue : parsed;
};

/**
 * 深い複製
 */
export const deepClone = <T>(obj: T): T => {
    if (obj === null || typeof obj !== 'object') {
        return obj;
    }

    if (obj instanceof Date) {
        return new Date(obj.getTime()) as unknown as T;
    }

    if (Array.isArray(obj)) {
        return obj.map(deepClone) as unknown as T;
    }

    const cloned = {} as T;
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
            cloned[key] = deepClone(obj[key]);
        }
    }

    return cloned;
};

/**
 * デバウンス関数
 */
export const debounce = <T extends (...args: any[]) => any>(
    func: T,
    delay: number
): ((...args: Parameters<T>) => void) => {
    let timeoutId: number | undefined;

    return (...args: Parameters<T>) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = window.setTimeout(() => func(...args), delay);
    };
};

/**
 * スロットル関数
 */
export const throttle = <T extends (...args: any[]) => any>(
    func: T,
    delay: number
): ((...args: Parameters<T>) => void) => {
    let lastCall = 0;

    return (...args: Parameters<T>) => {
        const now = Date.now();
        if (now - lastCall >= delay) {
            lastCall = now;
            func(...args);
        }
    };
};

// =============================================================================
// デフォルトエクスポート
// =============================================================================

export default {
    // バリデーション
    validateSFRAmount,
    validateUUID,
    validateDateString,
    validateDateTimeString,
    validateEmail,
    validateScore,
    validateEvaluationScore,
    validatePassword,
    mergeValidationResults,
    throwIfInvalid,
    ValidationError,

    // フォーマット
    formatSFRAmount,
    formatDate,
    formatDateTime,
    formatRelativeTime,
    formatPercentage,

    // Enum表示名
    getTransactionTypeDisplayName,
    getProposalStatusDisplayName,
    getVoteChoiceDisplayName,
    getStatsPeriodDisplayName,

    // ユーティリティ
    getLocalizedErrorMessage,
    safeParseFloat,
    safeParseInt,
    deepClone,
    debounce,
    throttle,
};
