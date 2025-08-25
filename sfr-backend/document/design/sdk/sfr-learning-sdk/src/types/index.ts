/**
 * SFR Learning API SDK - Type Definitions
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 */

// ======================================
// 🎓 学習空間関連タイプ
// ======================================

export interface LearningSpace {
    id: number;
    name: string;
    description?: string;
    mode: LearningMode;
    status: LearningSpaceStatus;
    isPublic: boolean;
    maxMembers: number;
    memberCount: number;
    ownerId: string;
    createdAt: string;
    updatedAt: string;
}

export interface LearningSpaceCreateRequest {
    name: string;
    description?: string;
    mode: LearningMode;
    isPublic?: boolean;
    maxMembers?: number;
    settings?: LearningSpaceSettings;
}

export interface LearningSpaceSettings {
    allowComments?: boolean;
    allowFileUpload?: boolean;
    moderationRequired?: boolean;
    autoProgressTracking?: boolean;
    notificationSettings?: NotificationSettings;
}

export interface NotificationSettings {
    newContentNotification?: boolean;
    assignmentDueNotification?: boolean;
    discussionNotification?: boolean;
}

// ======================================
// 📚 学習コンテンツ関連タイプ
// ======================================

export interface LearningContent {
    id: number;
    title: string;
    description?: string;
    contentType: ContentType;
    contentUrl?: string;
    duration?: number;
    difficulty: ContentDifficulty;
    tags: string[];
    isPublished: boolean;
    order: number;
    authorId: string;
    createdAt: string;
    updatedAt: string;
}

export interface LearningContentCreateRequest {
    title: string;
    description?: string;
    contentType: ContentType;
    content?: string;
    file?: File;
    duration?: number;
    difficulty: ContentDifficulty;
    tags?: string[];
    isPublished?: boolean;
    order?: number;
}

export interface ContentProgress {
    isCompleted: boolean;
    completedAt?: string;
    timeSpent: number;
    lastAccessedAt?: string;
    rating?: number;
}

// ======================================
// 📈 学習進捗関連タイプ
// ======================================

export interface LearningProgress {
    spaceId: number;
    userId: string;
    overallProgress: number;
    completedContentCount: number;
    totalContentCount: number;
    totalTimeSpent: number;
    lastActivity?: string;
    contentProgress: ContentProgressItem[];
    achievements: Achievement[];
}

export interface ContentProgressItem {
    contentId: number;
    contentTitle: string;
    progress: ContentProgress;
}

export interface ProgressRecordRequest {
    progressType: 'STARTED' | 'IN_PROGRESS' | 'COMPLETED';
    timeSpent?: number;
    rating?: number;
    notes?: string;
}

export interface Achievement {
    id: number;
    title: string;
    description: string;
    badgeUrl?: string;
    earnedAt: string;
}

// ======================================
// ❓ クイズ関連タイプ
// ======================================

export interface Quiz {
    id: number;
    title: string;
    description?: string;
    difficulty: QuizDifficulty;
    timeLimit: number;
    passingScore: number;
    questionCount: number;
    authorId: string;
    createdAt: string;
    userAttempts: number;
    bestScore?: number;
}

export interface QuizCreateRequest {
    title: string;
    description?: string;
    difficulty: QuizDifficulty;
    timeLimit?: number;
    passingScore?: number;
    questions: QuizQuestion[];
}

export interface QuizQuestion {
    question: string;
    questionType: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'TEXT_INPUT';
    options: string[];
    correctAnswer: string;
    explanation?: string;
    points?: number;
}

export interface QuizAnswerRequest {
    answers: QuizAnswer[];
}

export interface QuizAnswer {
    questionIndex: number;
    answer: string;
}

export interface QuizResult {
    quizId: number;
    score: number;
    totalQuestions: number;
    correctAnswers: number;
    passed: boolean;
    timeSpent: number;
    submittedAt: string;
    questionResults: QuestionResult[];
}

export interface QuestionResult {
    questionIndex: number;
    isCorrect: boolean;
    userAnswer: string;
    correctAnswer: string;
    explanation?: string;
}

// ======================================
// 📊 評価関連タイプ
// ======================================

export interface EvaluationDto {
    contentId: number;
    rating: number;
    comment?: string;
    characterId?: string;
}

export interface EvaluationResponse {
    id: number;
    contentId: number;
    rating: number;
    comment?: string;
    characterId?: string;
    userId: string;
    createdAt: string;
}

// ======================================
// 🔄 API レスポンスタイプ
// ======================================

export interface ApiResponse<T> {
    data: T;
    message?: string;
    timestamp: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface ErrorResponse {
    error: string;
    message: string;
    details?: ErrorDetail[];
    timestamp: string;
    path: string;
}

export interface ErrorDetail {
    field: string;
    message: string;
}

// ======================================
// 📊 列挙型
// ======================================

export type LearningMode = 'SCHOOL' | 'SALON' | 'FANCLUB';

export type LearningSpaceStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ARCHIVED';

export type LearningSpaceRole = 'OWNER' | 'INSTRUCTOR' | 'MEMBER' | 'GUEST';

export type ContentType = 'TEXT' | 'VIDEO' | 'AUDIO' | 'DOCUMENT' | 'INTERACTIVE';

export type ContentDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export type QuizDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export type QuizStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

// ======================================
// ⚙️ SDK設定タイプ
// ======================================

export interface SfrLearningConfig {
    baseURL: string;
    apiKey?: string;
    timeout?: number;
    retryAttempts?: number;
    debug?: boolean;
}

export interface RequestConfig {
    headers?: Record<string, string>;
    timeout?: number;
    retries?: number;
}
