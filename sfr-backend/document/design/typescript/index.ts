/**
 * SFR暗号資産システム TypeScript SDK
 * 
 * @description SFR暗号資産APIとの通信とデータ処理のための包括的なTypeScript SDK
 * @version 1.0.0
 * @date 2025-08-20
 * @author SFR Development Team
 * 
 * @example
 * ```typescript
 * import { SfrCryptoApiClient, validateSFRAmount, formatSFRAmount } from '@sfr/crypto-sdk';
 * 
 * // APIクライアントの作成
 * const client = new SfrCryptoApiClient({
 *   baseURL: 'https://api.sfr.example.com/v1',
 *   token: 'your-jwt-token'
 * });
 * 
 * // ユーザー残高の取得
 * const balance = await client.getUserBalance('user-uuid');
 * 
 * // 金額のバリデーションとフォーマット
 * const isValid = validateSFRAmount('123.45678901');
 * const formatted = formatSFRAmount('123.45678901');
 * ```
 */

// =============================================================================
// 型定義とDTO（Data Transfer Objects）
// =============================================================================

export * from './sfr-crypto-types';

// =============================================================================
// APIクライアント
// =============================================================================

export * from './sfr-crypto-client';

// =============================================================================
// バリデーション・ユーティリティ
// =============================================================================

export * from './sfr-crypto-utils';

// =============================================================================
// 便利なエイリアス
// =============================================================================

import {
    SfrCryptoApiClient,
    createSfrCryptoApiClient,
    SfrApiError,
    HttpClient,
} from './sfr-crypto-client';

import {
    validateSFRAmount,
    validateUUID,
    formatSFRAmount,
    formatDate,
    formatDateTime,
    getTransactionTypeDisplayName,
    getProposalStatusDisplayName,
    getLocalizedErrorMessage,
} from './sfr-crypto-utils';

// 便利なエイリアス
export const SFR = {
    // クライアント
    Client: SfrCryptoApiClient,
    createClient: createSfrCryptoApiClient,
    ApiError: SfrApiError,
    HttpClient,

    // バリデーション
    validate: {
        amount: validateSFRAmount,
        uuid: validateUUID,
    },

    // フォーマット
    format: {
        amount: formatSFRAmount,
        date: formatDate,
        datetime: formatDateTime,
    },

    // 表示名
    display: {
        transactionType: getTransactionTypeDisplayName,
        proposalStatus: getProposalStatusDisplayName,
        errorMessage: getLocalizedErrorMessage,
    },
};

// =============================================================================
// パッケージ情報
// =============================================================================

export const SDK_INFO = {
    name: '@sfr/crypto-sdk',
    version: '1.0.0',
    description: 'SFR暗号資産システム TypeScript SDK',
    author: 'SFR Development Team',
    license: 'MIT',
    repository: 'https://github.com/sfr-project/crypto-sdk',
    homepage: 'https://sfr-project.github.io/crypto-sdk',
    documentation: 'https://docs.sfr-project.com/sdk',
    apiReference: 'https://api.sfr-project.com/docs',
};

// =============================================================================
// デフォルトエクスポート
// =============================================================================

export default SFR;
