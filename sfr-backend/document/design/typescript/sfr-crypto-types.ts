/**
 * SFR暗号資産システム TypeScript型定義・DTOモジュール
 * 
 * @description SFR暗号資産APIとの通信で使用する型定義とデータ転送オブジェクト
 * @version 1.0.0
 * @date 2025-08-20
 */

// =============================================================================
// 基底型定義
// =============================================================================

/**
 * UUIDv4文字列型
 */
export type UUID = string;

/**
 * SFR金額型（文字列形式、8桁小数まで）
 */
export type SFRAmount = string;

/**
 * 日付文字列型（ISO 8601 YYYY-MM-DD形式）
 */
export type DateString = string;

/**
 * 日時文字列型（ISO 8601 YYYY-MM-DDTHH:mm:ss形式）
 */
export type DateTimeString = string;

/**
 * レスポンスステータス
 */
export enum ResponseStatus {
    SUCCESS = 'SUCCESS',
    PARTIAL_SUCCESS = 'PARTIAL_SUCCESS',
    WARNING = 'WARNING',
    ERROR = 'ERROR'
}

/**
 * 基底レスポンスDTO
 */
export interface BaseResponseDto {
    /** 処理タイムスタンプ */
    timestamp: DateTimeString;
    /** レスポンスID（トレーサビリティ用） */
    responseId: string;
    /** 処理ステータス */
    status: ResponseStatus;
}

/**
 * エラーレスポンスDTO
 */
export interface ErrorResponseDto extends BaseResponseDto {
    /** エラーコード */
    error: string;
    /** エラーメッセージ */
    message: string;
    /** エラー詳細情報 */
    details?: Record<string, any>;
    /** リクエストパス */
    path: string;
    /** エラー固有ID */
    errorId?: string;
}

/**
 * ページネーション情報DTO
 */
export interface PaginationDto {
    /** 現在ページ番号（1ベース） */
    page: number;
    /** 1ページあたりのアイテム数 */
    limit: number;
    /** 総ページ数 */
    totalPages: number;
    /** 総アイテム数 */
    totalCount: number;
    /** 次ページ存在フラグ */
    hasNext: boolean;
    /** 前ページ存在フラグ */
    hasPrevious: boolean;
}

/**
 * ページネーション対応レスポンスDTO
 */
export interface PagedResponseDto<T> extends BaseResponseDto {
    /** データリスト */
    data: T[];
    /** ページネーション情報 */
    pagination: PaginationDto;
}

// =============================================================================
// Enum定義
// =============================================================================

/**
 * トランザクション種別
 */
export enum TransactionType {
    /** 報酬獲得 */
    EARN = 'EARN',
    /** 使用・支払い */
    SPEND = 'SPEND',
    /** 徴収 */
    COLLECT = 'COLLECT',
    /** バーン（焼却） */
    BURN = 'BURN',
    /** 送金・転送 */
    TRANSFER = 'TRANSFER'
}

/**
 * 徴収先区分
 */
export enum CollectionDestination {
    /** バーン（焼却） */
    BURN = 'BURN',
    /** リザーブ（準備金） */
    RESERVE = 'RESERVE',
    /** 再分配 */
    REDISTRIBUTE = 'REDISTRIBUTE'
}

/**
 * AIバーン判断結果
 */
export enum BurnDecisionResult {
    /** バーン実行 */
    BURN = 'BURN',
    /** リザーブ保管 */
    RESERVE = 'RESERVE'
}

/**
 * ガバナンス提案種別
 */
export enum ProposalType {
    /** ポリシー提案 */
    POLICY = 'POLICY',
    /** パラメータ提案 */
    PARAMETER = 'PARAMETER',
    /** 機能提案 */
    FEATURE = 'FEATURE',
    /** ガバナンス提案 */
    GOVERNANCE = 'GOVERNANCE'
}

/**
 * 提案ステータス
 */
export enum ProposalStatus {
    /** 下書き */
    DRAFT = 'DRAFT',
    /** 投票中 */
    VOTING = 'VOTING',
    /** 可決 */
    PASSED = 'PASSED',
    /** 否決 */
    REJECTED = 'REJECTED',
    /** 期限切れ */
    EXPIRED = 'EXPIRED'
}

/**
 * 投票選択
 */
export enum VoteChoice {
    /** 賛成 */
    YES = 'YES',
    /** 反対 */
    NO = 'NO',
    /** 棄権 */
    ABSTAIN = 'ABSTAIN'
}

/**
 * 評議員ステータス
 */
export enum CouncilStatus {
    /** 活動中 */
    ACTIVE = 'ACTIVE',
    /** 任期満了 */
    COMPLETED = 'COMPLETED',
    /** 辞任 */
    RESIGNED = 'RESIGNED',
    /** 罷免 */
    REMOVED = 'REMOVED'
}

/**
 * 統計集計期間
 */
export enum StatsPeriod {
    /** 日次 */
    DAILY = 'DAILY',
    /** 週次 */
    WEEKLY = 'WEEKLY',
    /** 月次 */
    MONTHLY = 'MONTHLY'
}

/**
 * Oracle外部データ種別
 */
export enum OracleDataType {
    /** 価格データ */
    PRICE = 'PRICE',
    /** 取引量データ */
    VOLUME = 'VOLUME',
    /** 流動性データ */
    LIQUIDITY = 'LIQUIDITY',
    /** レートデータ */
    RATE = 'RATE'
}

/**
 * システムパラメータ種別
 */
export enum ParameterType {
    /** 文字列型 */
    STRING = 'STRING',
    /** 数値型 */
    NUMBER = 'NUMBER',
    /** 真偽値型 */
    BOOLEAN = 'BOOLEAN',
    /** JSON型 */
    JSON = 'JSON'
}

/**
 * パラメータ調整トリガー種別
 */
export enum TriggerType {
    /** 手動調整 */
    MANUAL = 'MANUAL',
    /** AI自動調整 */
    AUTO_AI = 'AUTO_AI',
    /** Oracle連動調整 */
    ORACLE = 'ORACLE',
    /** ガバナンス決定 */
    GOVERNANCE = 'GOVERNANCE'
}

/**
 * ユーザー評価種別
 */
export enum EvaluationType {
    /** 学習活動評価 */
    LEARNING = 'LEARNING',
    /** 創作活動評価 */
    CREATION = 'CREATION',
    /** コミュニティ貢献評価 */
    CONTRIBUTION = 'CONTRIBUTION',
    /** 一般評価 */
    GENERAL = 'GENERAL'
}

// =============================================================================
// Token Management DTO
// =============================================================================

/**
 * ユーザー残高DTO
 */
export interface UserBalanceDto {
    /** ユーザーID */
    userId: UUID;
    /** 現在残高（8桁小数） */
    currentBalance: SFRAmount;
    /** 累計獲得額 */
    totalEarned: SFRAmount;
    /** 累計使用額 */
    totalSpent: SFRAmount;
    /** 累計徴収額 */
    totalCollected: SFRAmount;
    /** 最終徴収日 */
    lastCollectionDate?: DateString;
    /** 徴収免除フラグ */
    collectionExempt: boolean;
    /** 残高凍結フラグ */
    frozen: boolean;
    /** 更新日時 */
    updatedAt: DateTimeString;
}

/**
 * 残高変動履歴DTO
 */
export interface BalanceHistoryDto {
    /** 履歴ID */
    historyId: string;
    /** ユーザーID */
    userId: UUID;
    /** トランザクション種別 */
    transactionType: TransactionType;
    /** 変動金額 */
    amount: SFRAmount;
    /** 変動前残高 */
    balanceBefore: SFRAmount;
    /** 変動後残高 */
    balanceAfter: SFRAmount;
    /** 変動理由 */
    reason: string;
    /** 関連トランザクションID */
    referenceId?: string;
    /** 作成日時 */
    createdAt: DateTimeString;
}

/**
 * SFR送金リクエストDTO
 */
export interface TransferRequestDto {
    /** 送金元ユーザーID */
    fromUserId: UUID;
    /** 送金先ユーザーID */
    toUserId: UUID;
    /** 送金金額 */
    amount: SFRAmount;
    /** 送金理由 */
    reason: string;
    /** 備考 */
    note?: string;
}

/**
 * SFR送金レスポンスDTO
 */
export interface TransferResponseDto extends BaseResponseDto {
    /** 送金ID */
    transferId: string;
    /** 送金元ユーザーID */
    fromUserId: UUID;
    /** 送金先ユーザーID */
    toUserId: UUID;
    /** 送金金額 */
    amount: SFRAmount;
    /** 送金後の送金元残高 */
    fromBalanceAfter: SFRAmount;
    /** 送金後の送金先残高 */
    toBalanceAfter: SFRAmount;
    /** 処理完了日時 */
    processedAt: DateTimeString;
}

// =============================================================================
// Rewards System DTO
// =============================================================================

/**
 * 報酬発行リクエストDTO
 */
export interface RewardIssueRequestDto {
    /** ユーザーID */
    userId: UUID;
    /** 活動スコア（0-100） */
    activityScore: number;
    /** 評価スコア（1.0-5.0） */
    evaluationScore: number;
    /** 報酬理由 */
    rewardReason: string;
    /** 強制発行フラグ（プール不足でも発行） */
    forceIssue?: boolean;
}

/**
 * 報酬発行レスポンスDTO
 */
export interface RewardIssueResponseDto extends BaseResponseDto {
    /** 報酬ID */
    rewardId: string;
    /** ユーザーID */
    userId: UUID;
    /** 発行金額 */
    rewardAmount: SFRAmount;
    /** 対象プール日 */
    poolDate: DateString;
    /** 複合スコア（0.6*評価 + 0.4*活動） */
    combinedScore: number;
    /** 当日の全体スコア合計 */
    totalPoolScore: number;
    /** 計算詳細 */
    calculationDetails: Record<string, any>;
    /** 発行日時 */
    issuedAt: DateTimeString;
}

/**
 * 報酬計算リクエストDTO
 */
export interface RewardCalculateRequestDto {
    /** ユーザーID */
    userId: UUID;
    /** 活動スコア（0-100） */
    activityScore: number;
    /** 評価スコア（1.0-5.0） */
    evaluationScore: number;
    /** 計算対象日（指定なしは当日） */
    targetDate?: DateString;
}

/**
 * 報酬計算レスポンスDTO
 */
export interface RewardCalculateResponseDto {
    /** ユーザーID */
    userId: UUID;
    /** 推定報酬額 */
    estimatedReward: SFRAmount;
    /** 複合スコア */
    combinedScore: number;
    /** 現在のプール総額 */
    currentPoolTotal: SFRAmount;
    /** 現在のプール残額 */
    currentPoolRemaining: SFRAmount;
    /** 参加者総数 */
    totalParticipants: number;
    /** 計算詳細 */
    calculationDetails: Record<string, any>;
}

/**
 * 日次報酬分配リクエストDTO
 */
export interface DailyDistributionRequestDto {
    /** 対象日 */
    targetDate: DateString;
    /** ドライランフラグ（実行前テスト） */
    dryRun?: boolean;
    /** 再分配フラグ（既に分配済みでも再実行） */
    forceRedistribution?: boolean;
}

/**
 * 分配詳細DTO
 */
export interface DistributionDetailDto {
    /** ユーザーID */
    userId: UUID;
    /** 報酬金額 */
    rewardAmount: SFRAmount;
    /** 複合スコア */
    combinedScore: number;
}

/**
 * 日次報酬分配レスポンスDTO
 */
export interface DailyDistributionResponseDto extends BaseResponseDto {
    /** 対象日 */
    targetDate: DateString;
    /** 参加者総数 */
    totalParticipants: number;
    /** 分配総額 */
    totalDistributed: SFRAmount;
    /** 平均報酬額 */
    averageReward: SFRAmount;
    /** 分配詳細リスト */
    distributionDetails: DistributionDetailDto[];
    /** 処理完了日時 */
    processedAt: DateTimeString;
}

/**
 * 報酬履歴DTO
 */
export interface RewardHistoryDto {
    /** 報酬ID */
    rewardId: string;
    /** プール日 */
    poolDate: DateString;
    /** 報酬金額 */
    rewardAmount: SFRAmount;
    /** 活動スコア */
    activityScore: number;
    /** 評価スコア */
    evaluationScore: number;
    /** 複合スコア */
    combinedScore: number;
    /** 報酬理由 */
    rewardReason: string;
    /** 作成日時 */
    createdAt: DateTimeString;
}

// =============================================================================
// Collections System DTO
// =============================================================================

/**
 * トークン徴収リクエストDTO
 */
export interface CollectionRequestDto {
    /** ユーザーID */
    userId: UUID;
    /** 強制徴収フラグ（条件無視） */
    forceCollection?: boolean;
    /** 徴収率（0.0001-1.0） */
    collectionRate?: number;
    /** 徴収理由 */
    collectionReason?: string;
}

/**
 * トークン徴収レスポンスDTO
 */
export interface CollectionResponseDto extends BaseResponseDto {
    /** 徴収ID */
    collectionId: string;
    /** ユーザーID */
    userId: UUID;
    /** 徴収前残高 */
    balanceBefore: SFRAmount;
    /** 徴収金額 */
    collectionAmount: SFRAmount;
    /** 徴収率 */
    collectionRate: number;
    /** 徴収先（BURN/RESERVE/REDISTRIBUTE） */
    destination: CollectionDestination;
    /** AI判断ID */
    aiDecisionId?: string;
    /** 処理日時 */
    processedAt: DateTimeString;
}

/**
 * 月次一括徴収リクエストDTO
 */
export interface MonthlyCollectionRequestDto {
    /** 対象月（YYYY-MM形式） */
    targetMonth: string;
    /** ドライランフラグ */
    dryRun?: boolean;
    /** 徴収最低残高（指定なしはデフォルト） */
    collectionThreshold?: SFRAmount;
}

/**
 * 月次徴収詳細DTO
 */
export interface MonthlyCollectionDetailDto {
    /** ユーザーID */
    userId: UUID;
    /** 徴収金額 */
    collectionAmount: SFRAmount;
    /** 徴収先 */
    destination: CollectionDestination;
}

/**
 * 月次一括徴収レスポンスDTO
 */
export interface MonthlyCollectionResponseDto extends BaseResponseDto {
    /** 対象月 */
    targetMonth: string;
    /** 徴収総額 */
    totalCollected: SFRAmount;
    /** バーン総額 */
    totalBurned: SFRAmount;
    /** リザーブ総額 */
    totalReserved: SFRAmount;
    /** 影響ユーザー数 */
    affectedUsers: number;
    /** 徴収詳細リスト */
    collectionDetails: MonthlyCollectionDetailDto[];
    /** 処理完了日時 */
    processedAt: DateTimeString;
}

/**
 * 市場データDTO
 */
export interface MarketDataDto {
    /** 価格 */
    price?: number;
    /** 取引量 */
    volume?: number;
    /** 流動性 */
    liquidity?: number;
}

/**
 * AIバーン判断リクエストDTO
 */
export interface BurnDecisionRequestDto {
    /** 判断トリガーソース */
    triggerSource: string;
    /** 市場データ */
    marketData?: MarketDataDto;
}

/**
 * AIバーン判断レスポンスDTO
 */
export interface BurnDecisionResponseDto extends BaseResponseDto {
    /** 判断ID */
    decisionId: string;
    /** 判断日 */
    decisionDate: DateString;
    /** 総流通量 */
    totalCirculation: SFRAmount;
    /** 総発行量 */
    totalIssued: SFRAmount;
    /** AI信頼度（0.0-1.0） */
    aiConfidence: number;
    /** 判断結果 */
    decisionResult: BurnDecisionResult;
    /** バーン金額 */
    burnedAmount: SFRAmount;
    /** リザーブ金額 */
    reservedAmount: SFRAmount;
    /** AI判断理由 */
    reasoning: string;
    /** トリガー情報 */
    triggeredBy: string;
    /** 作成日時 */
    createdAt: DateTimeString;
}

/**
 * 徴収履歴DTO
 */
export interface CollectionHistoryDto {
    /** 徴収ID */
    collectionId: string;
    /** ユーザーID */
    userId: UUID;
    /** 徴収日 */
    collectionDate: DateString;
    /** 徴収金額 */
    collectionAmount: SFRAmount;
    /** 徴収先 */
    destination: CollectionDestination;
    /** 徴収理由 */
    collectionReason?: string;
    /** 作成日時 */
    createdAt: DateTimeString;
}

// =============================================================================
// Governance DTO
// =============================================================================

/**
 * 評議員情報DTO
 */
export interface CouncilMemberDto {
    /** 任期ID */
    termId: string;
    /** ユーザーID */
    userId: UUID;
    /** 開始日 */
    startDate: DateString;
    /** 終了日 */
    endDate: DateString;
    /** ステータス */
    status: CouncilStatus;
    /** 投票権力 */
    votingPower: number;
    /** 評価実施数 */
    evaluationCount: number;
    /** 提案数 */
    proposalCount: number;
}

/**
 * 評議員リストレスポンスDTO
 */
export interface CouncilMembersResponseDto {
    /** 評議員データ */
    data: CouncilMemberDto[];
    /** 総メンバー数 */
    totalMembers: number;
}

/**
 * 評議員任命リクエストDTO
 */
export interface CouncilAppointRequestDto {
    /** ユーザーID */
    userId: UUID;
    /** 開始日 */
    startDate: DateString;
    /** 終了日 */
    endDate: DateString;
    /** 投票権力 */
    votingPower?: number;
    /** 任命理由 */
    appointmentReason: string;
}

/**
 * 評議員任命レスポンスDTO
 */
export interface CouncilAppointResponseDto extends BaseResponseDto {
    /** 任期ID */
    termId: string;
    /** ユーザーID */
    userId: UUID;
    /** 開始日 */
    startDate: DateString;
    /** 終了日 */
    endDate: DateString;
    /** 投票権力 */
    votingPower: number;
    /** 評議員ステータス */
    councilStatus: CouncilStatus;
    /** 任命日時 */
    appointedAt: DateTimeString;
}

/**
 * 提案作成リクエストDTO
 */
export interface CreateProposalRequestDto {
    /** 提案タイトル */
    title: string;
    /** 提案詳細 */
    description: string;
    /** 提案種別 */
    proposalType: ProposalType;
    /** 投票期間（時間） */
    votingDurationHours?: number;
    /** 必要定足数 */
    quorumRequired?: number;
    /** 可決閾値（0.5-1.0） */
    approvalThreshold?: number;
}

/**
 * 提案作成レスポンスDTO
 */
export interface CreateProposalResponseDto extends BaseResponseDto {
    /** 提案ID */
    proposalId: UUID;
    /** 提案タイトル */
    title: string;
    /** 提案ステータス */
    proposalStatus: ProposalStatus;
    /** 投票開始日時 */
    votingStart: DateTimeString;
    /** 投票終了日時 */
    votingEnd: DateTimeString;
    /** 作成日時 */
    createdAt: DateTimeString;
}

/**
 * 投票詳細DTO
 */
export interface VoteDetailDto {
    /** ユーザーID */
    userId: UUID;
    /** 投票選択 */
    voteChoice: VoteChoice;
    /** 投票権力 */
    votingPower: number;
    /** 投票日時 */
    castAt: DateTimeString;
}

/**
 * 提案DTO
 */
export interface ProposalDto {
    /** 提案ID */
    proposalId: UUID;
    /** 提案タイトル */
    title: string;
    /** 提案種別 */
    proposalType: ProposalType;
    /** 作成者ID */
    createdBy: UUID;
    /** ステータス */
    status: ProposalStatus;
    /** 投票開始日時 */
    votingStart: DateTimeString;
    /** 投票終了日時 */
    votingEnd: DateTimeString;
    /** 総投票数 */
    totalVotes: number;
    /** 賛成票数 */
    yesVotes: number;
    /** 反対票数 */
    noVotes: number;
    /** 作成日時 */
    createdAt: DateTimeString;
}

/**
 * 提案詳細DTO
 */
export interface ProposalDetailDto extends ProposalDto {
    /** 提案詳細 */
    description: string;
    /** 必要定足数 */
    quorumRequired: number;
    /** 可決閾値 */
    approvalThreshold: number;
    /** 棄権票数 */
    abstainVotes: number;
    /** 現在の可決率 */
    currentApprovalRate: number;
    /** 定足数達成フラグ */
    isQuorumMet: boolean;
    /** 投票詳細リスト */
    votesDetail: VoteDetailDto[];
    /** 更新日時 */
    updatedAt: DateTimeString;
}

/**
 * 投票リクエストDTO
 */
export interface VoteRequestDto {
    /** 投票選択 */
    voteChoice: VoteChoice;
    /** 投票コメント */
    comment?: string;
}

/**
 * 投票レスポンスDTO
 */
export interface VoteResponseDto extends BaseResponseDto {
    /** 投票ID */
    voteId: string;
    /** 提案ID */
    proposalId: UUID;
    /** ユーザーID */
    userId: UUID;
    /** 投票選択 */
    voteChoice: VoteChoice;
    /** 投票権力 */
    votingPower: number;
    /** 投票重み */
    voteWeight: number;
    /** 投票日時 */
    castAt: DateTimeString;
    /** 投票後の提案ステータス */
    proposalStatusAfter: ProposalStatus;
}

/**
 * ユーザー投票履歴DTO
 */
export interface UserVoteDto {
    /** 投票ID */
    voteId: string;
    /** 提案ID */
    proposalId: UUID;
    /** 提案タイトル */
    proposalTitle: string;
    /** 投票選択 */
    voteChoice: VoteChoice;
    /** 投票権力 */
    votingPower: number;
    /** 投票コメント */
    comment?: string;
    /** 投票日時 */
    castAt: DateTimeString;
}

// =============================================================================
// Statistics DTO
// =============================================================================

/**
 * SFR統計概要DTO
 */
export interface StatsOverviewDto {
    /** 総流通量 */
    totalCirculation: SFRAmount;
    /** 総発行量 */
    totalIssued: SFRAmount;
    /** 総バーン量 */
    totalBurned: SFRAmount;
    /** ホルダー総数 */
    totalHolders: number;
    /** アクティブホルダー数 */
    activeHolders: number;
    /** 平均残高 */
    averageBalance: SFRAmount;
    /** 中央値残高 */
    medianBalance: SFRAmount;
    /** 日次発行量 */
    dailyIssuance: SFRAmount;
    /** 日次徴収量 */
    dailyCollection: SFRAmount;
    /** バーン率 */
    burnRate: number;
    /** 最終更新日時 */
    lastUpdated: DateTimeString;
}

/**
 * 流通量データDTO
 */
export interface CirculationDataDto {
    /** 対象日 */
    date: DateString;
    /** 流通量 */
    circulation: SFRAmount;
    /** 発行量 */
    issued: SFRAmount;
    /** バーン量 */
    burned: SFRAmount;
    /** 徴収量 */
    collected: SFRAmount;
    /** ホルダー数 */
    holders: number;
}

/**
 * 流通量サマリーDTO
 */
export interface CirculationSummaryDto {
    /** 総変動量 */
    totalChange: SFRAmount;
    /** 平均日次発行量 */
    averageDailyIssuance: SFRAmount;
    /** 平均日次バーン量 */
    averageDailyBurn: SFRAmount;
    /** 成長率 */
    growthRate: number;
}

/**
 * 流通量統計DTO
 */
export interface CirculationStatsDto {
    /** 統計期間 */
    period: StatsPeriod;
    /** 統計データリスト */
    data: CirculationDataDto[];
    /** サマリー情報 */
    summary: CirculationSummaryDto;
}

/**
 * 報酬統計データDTO
 */
export interface RewardStatsDataDto {
    /** 期間 */
    period: DateString;
    /** 総報酬額 */
    totalRewards: SFRAmount;
    /** 受給者数 */
    recipientCount: number;
    /** 平均報酬額 */
    averageReward: SFRAmount;
    /** 最大報酬額 */
    maxReward: SFRAmount;
    /** 最小報酬額 */
    minReward: SFRAmount;
}

/**
 * 報酬統計サマリーDTO
 */
export interface RewardStatsSummaryDto {
    /** 期間総報酬額 */
    totalPeriodRewards: SFRAmount;
    /** 総受給者数 */
    totalRecipients: number;
    /** 全体平均 */
    overallAverage: SFRAmount;
}

/**
 * 報酬統計DTO
 */
export interface RewardStatsDto {
    /** 開始日 */
    fromDate: DateString;
    /** 終了日 */
    toDate: DateString;
    /** グループ化 */
    groupBy: 'day' | 'week' | 'month';
    /** 統計データリスト */
    data: RewardStatsDataDto[];
    /** サマリー情報 */
    summary: RewardStatsSummaryDto;
}

/**
 * 上位ホルダーDTO
 */
export interface TopHolderDto {
    /** ランク */
    rank: number;
    /** ユーザーID */
    userId: UUID;
    /** 現在残高 */
    currentBalance: SFRAmount;
    /** 全体に占める割合 */
    percentageOfTotal: number;
    /** 最終活動日時 */
    lastActivity?: DateTimeString;
}

/**
 * 上位ホルダーレスポンスDTO
 */
export interface TopHoldersResponseDto {
    /** ホルダーデータ */
    data: TopHolderDto[];
    /** 表示対象総額 */
    totalRepresented: SFRAmount;
    /** 流通量に占める割合 */
    percentageOfCirculation: number;
    /** 生成日時 */
    generatedAt: DateTimeString;
}

// =============================================================================
// Oracle & Audit DTO
// =============================================================================

/**
 * OracleフィードデータDTO
 */
export interface OracleFeedDto {
    /** フィードID */
    feedId: string;
    /** データソース */
    source: string;
    /** データ種別 */
    dataType: OracleDataType;
    /** 値 */
    value: SFRAmount;
    /** 信頼度 */
    confidence: number;
    /** タイムスタンプ */
    timestamp: DateTimeString;
    /** メタデータ */
    metadata?: Record<string, any>;
}

/**
 * Oracleフィード一覧レスポンスDTO
 */
export interface OracleFeedsResponseDto {
    /** フィードデータ */
    data: OracleFeedDto[];
    /** 最新更新日時 */
    latestUpdate: DateTimeString;
}

/**
 * Oracleフィード更新リクエストDTO
 */
export interface OracleFeedUpdateRequestDto {
    /** データソース */
    source: string;
    /** データ種別 */
    dataType: OracleDataType;
    /** 値 */
    value: SFRAmount;
    /** 信頼度 */
    confidence?: number;
    /** メタデータ */
    metadata?: Record<string, any>;
}

/**
 * Oracleフィード更新レスポンスDTO
 */
export interface OracleFeedUpdateResponseDto extends BaseResponseDto {
    /** フィードID */
    feedId: string;
    /** データソース */
    source: string;
    /** データ種別 */
    dataType: OracleDataType;
    /** 値 */
    value: SFRAmount;
    /** 信頼度 */
    confidence: number;
    /** 作成日時 */
    createdAt: DateTimeString;
    /** フィードステータス */
    feedStatus: 'ACCEPTED' | 'REJECTED' | 'PENDING';
}

/**
 * システムパラメータDTO
 */
export interface SystemParameterDto {
    /** パラメータID */
    parameterId: string;
    /** パラメータ名 */
    parameterName: string;
    /** パラメータ値 */
    parameterValue: string;
    /** パラメータ型 */
    parameterType: ParameterType;
    /** 説明 */
    description?: string;
    /** 最終更新者 */
    lastUpdatedBy?: UUID;
    /** 更新日時 */
    updatedAt: DateTimeString;
}

/**
 * システムパラメータ一覧レスポンスDTO
 */
export interface SystemParametersResponseDto {
    /** パラメータデータ */
    data: SystemParameterDto[];
    /** 総パラメータ数 */
    totalParameters: number;
}

/**
 * パラメータ更新リクエストDTO
 */
export interface ParameterUpdateRequestDto {
    /** パラメータ値 */
    parameterValue: string;
    /** 更新理由 */
    updateReason: string;
    /** 強制更新フラグ */
    forceUpdate?: boolean;
}

/**
 * パラメータ更新レスポンスDTO
 */
export interface ParameterUpdateResponseDto extends BaseResponseDto {
    /** パラメータID */
    parameterId: string;
    /** パラメータ名 */
    parameterName: string;
    /** 旧値 */
    oldValue: string;
    /** 新値 */
    newValue: string;
    /** 更新者 */
    updatedBy: UUID;
    /** 更新理由 */
    updateReason: string;
    /** 更新日時 */
    updatedAt: DateTimeString;
    /** バリデーション通過フラグ */
    validationPassed: boolean;
}

/**
 * 調整ログDTO
 */
export interface AdjustmentLogDto {
    /** 調整ID */
    adjustmentId: string;
    /** パラメータ名 */
    parameterName: string;
    /** 旧値 */
    oldValue?: string;
    /** 新値 */
    newValue: string;
    /** 調整理由 */
    adjustmentReason?: string;
    /** 調整者 */
    adjustedBy?: UUID;
    /** トリガー種別 */
    triggerType: TriggerType;
    /** 作成日時 */
    createdAt: DateTimeString;
}

// =============================================================================
// API クライアント用ヘルパー型
// =============================================================================

/**
 * APIレスポンス型（成功時）
 */
export type ApiResponse<T> = T & BaseResponseDto;

/**
 * APIレスポンス型（ページネーション対応）
 */
export type ApiPagedResponse<T> = PagedResponseDto<T>;

/**
 * APIエラーレスポンス型
 */
export type ApiError = ErrorResponseDto;

/**
 * APIリクエスト設定
 */
export interface ApiRequestConfig {
    /** ベースURL */
    baseURL?: string;
    /** タイムアウト（ミリ秒） */
    timeout?: number;
    /** 認証トークン */
    token?: string;
    /** 追加ヘッダー */
    headers?: Record<string, string>;
}

/**
 * クエリパラメータ型（共通）
 */
export interface CommonQueryParams {
    /** ページ番号 */
    page?: number;
    /** 表示件数 */
    limit?: number;
    /** 開始日 */
    fromDate?: DateString;
    /** 終了日 */
    toDate?: DateString;
}

/**
 * 残高履歴クエリパラメータ
 */
export interface BalanceHistoryQueryParams extends CommonQueryParams {
    /** トランザクション種別 */
    transactionType?: TransactionType;
}

/**
 * 徴収履歴クエリパラメータ
 */
export interface CollectionHistoryQueryParams extends CommonQueryParams {
    /** 徴収先 */
    destination?: CollectionDestination;
}

/**
 * 提案クエリパラメータ
 */
export interface ProposalsQueryParams extends CommonQueryParams {
    /** ステータス */
    status?: ProposalStatus;
    /** 提案種別 */
    proposalType?: ProposalType;
}

/**
 * 調整ログクエリパラメータ
 */
export interface AdjustmentLogsQueryParams extends CommonQueryParams {
    /** パラメータ名 */
    parameterName?: string;
    /** トリガー種別 */
    triggerType?: TriggerType;
}

// =============================================================================
// デフォルトエクスポート
// =============================================================================

export default {
    // 型定義
    ResponseStatus,
    TransactionType,
    CollectionDestination,
    BurnDecisionResult,
    ProposalType,
    ProposalStatus,
    VoteChoice,
    CouncilStatus,
    StatsPeriod,
    OracleDataType,
    ParameterType,
    TriggerType,
    EvaluationType,
};
