/**
 * SoukLab Backend TypeScript Type Definitions
 * Auto-compatible with Spring Boot 4 / OpenAPI 3.1 contracts.
 * Copy this file into your frontend project at `src/types/souklab.d.ts` or `src/types/api.ts`.
 */

// ==========================================
// 1. Common Response Envelopes
// ==========================================

export interface ApiResponse<T = unknown> {
  success: boolean;
  code: number;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  empty: boolean;
}

export interface ApiErrorResponse {
  success: false;
  code: number;
  errorCode?: string;
  message: string;
  data: null;
  errors?: Record<string, string>;
  timestamp?: string;
  path?: string;
}

// ==========================================
// 2. Authentication & Profile Models
// ==========================================

export type UserRole = 'ADMIN' | 'ARTISAN' | 'CLIENT';

export interface UserSummary {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  avatarUrl?: string | null;
  isEmailVerified: boolean;
  isPremium: boolean;
  isTeacher?: boolean;
}

export interface AuthResponse {
  tokenType: 'Bearer';
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  user: UserSummary;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  role: 'ARTISAN' | 'CLIENT';
  phone?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface VerifyEmailRequest {
  email: string;
  code: string;
}

export interface ResendVerificationRequest {
  email: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface CompleteProfileRequest {
  bio?: string;
  phone?: string;
  address?: string;
  region?: string;
  city?: string;
  subCategoryId?: string;
  materialIds?: string[];
  epoqueIds?: string[];
  techniqueIds?: string[];
  isTeacher?: boolean;
  preferredCategories?: string[];
}

/**
 * RFC 7396 JSON Merge Patch Payload for PATCH /api/v1/auth/me
 * Omitted keys are untouched; explicit null removes optional fields.
 */
export interface ProfilePatchRequest {
  name?: string;
  bio?: string | null;
  phone?: string | null;
  website?: string | null;
  address?: string | null;
  city?: string | null;
  region?: string | null;
  craftSubCategoryId?: string | null;
  materialIds?: string[] | null;
  epoqueIds?: string[] | null;
  techniqueIds?: string[] | null;
  preferredCategories?: string[] | null;
}

export interface ArtisanResponse {
  id: string;
  email: string;
  name: string;
  role: 'ARTISAN';
  isTeacher: boolean;
  avatar?: string | null;
  bio?: string | null;
  phone?: string | null;
  website?: string | null;
  address?: string | null;
  city?: string | null;
  region?: string | null;
  craftCategories: string[];
  materials?: string[];
  epoques?: string[];
  techniques?: string[];
  contactInfoLocked: boolean;
  totalViews?: number;
  averageRating?: number;
  reviewCount?: number;
  createdAt: string;
}

export interface ClientProfileResponse {
  id: string;
  email: string;
  name: string;
  role: 'CLIENT';
  avatar?: string | null;
  phone?: string | null;
  city?: string | null;
  preferredCategories: string[];
  savedArtisanCount: number;
  isPremium: boolean;
  premiumExpiresAt?: string | null;
  createdAt: string;
}

export type CurrentUserProfile = ArtisanResponse | ClientProfileResponse;

// ==========================================
// 3. Artisan Directory & Search
// ==========================================

export interface ArtisanDirectoryCard {
  id: string;
  artisanName: string;
  profilePhotoUrl?: string | null;
  craftCategory?: string | null;
  craftSubCategory?: string | null;
  wilaya?: string | null;
  daira?: string | null;
  ratingAverage: number;
  reviewCount: number;
  isVerified: boolean;
  isTeacher: boolean;
  isPremium: boolean;
  contactInfoLocked: boolean;
  highlightedWorks: string[];
}

export interface DirectorySearchParams {
  keyword?: string;
  craftCategory?: string;
  craftSubCategory?: string;
  wilaya?: string;
  daira?: string;
  minRating?: number;
  verifiedOnly?: boolean;
  page?: number;
  size?: number;
  sort?: 'relevance' | 'rating,desc' | 'views,desc' | 'createdAt,desc';
}

// ==========================================
// 4. User Avatars & Media
// ==========================================

export type AvatarVariantType = 'THUMBNAIL_150' | 'STANDARD_400' | 'ORIGINAL';

export interface AvatarVariant {
  type: AvatarVariantType;
  url: string;
  width: number;
  height: number;
  fileSizeBytes: number;
}

export interface AvatarResponse {
  id: string;
  userId: string;
  isActive: boolean;
  variants: Record<AvatarVariantType, AvatarVariant>;
  uploadedAt: string;
}

// ==========================================
// 5. Community Feed
// ==========================================

export type FeedPostType = 'ACTUALITE' | 'FORMATION' | 'ANNONCE';
export type FeedPostStatus = 'DRAFT' | 'PENDING' | 'PUBLISHED' | 'HIDDEN' | 'REJECTED';

export interface FeedPostMedia {
  id: string;
  url: string;
  contentType: string;
  displayOrder: number;
}

export interface FeedPostCreateRequest {
  type: FeedPostType;
  title: string;
  body: string;
  formationId?: string | null;
}

export interface FeedPostResponse {
  id: string;
  authorId: string;
  authorName: string;
  type: FeedPostType;
  title: string;
  body: string;
  status: FeedPostStatus;
  formationId?: string | null;
  publishedAt?: string | null;
  moderationNote?: string | null;
  media: FeedPostMedia[];
}

// ==========================================
// 6. Messaging & Real-Time Chat
// ==========================================

export interface ConversationParticipant {
  id: string;
  name: string;
  avatarUrl?: string | null;
  role: UserRole;
}

export interface ChatAttachment {
  id: string;
  filename: string;
  url: string;
  contentType: string;
  sizeBytes: number;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  attachments: ChatAttachment[];
  sentAt: string;
  isRead: boolean;
  readAt?: string | null;
}

export interface ConversationSummary {
  id: string;
  recipient: ConversationParticipant;
  lastMessage?: ChatMessage | null;
  unreadCount: number;
  updatedAt: string;
}

export interface SendMessagePayload {
  conversationId?: string;
  recipientId: string;
  content: string;
}

export interface StompChatEvent {
  type: 'NEW_MESSAGE' | 'MESSAGE_READ' | 'TYPING_START' | 'TYPING_STOP';
  conversationId: string;
  message?: ChatMessage;
  senderId: string;
  timestamp: string;
}

// ==========================================
// 7. Subscriptions & Payments
// ==========================================

export type BillingCycle = 'MONTHLY' | 'YEARLY';
export type SubscriptionTier = 'FREE' | 'PRO' | 'PREMIUM';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELED' | 'REFUNDED';

export interface SubscriptionPlan {
  id: string;
  name: string;
  description: string;
  tier: SubscriptionTier;
  priceDZD: number;
  billingCycle: BillingCycle;
  features: string[];
  isActive: boolean;
}

export interface CheckoutSessionResponse {
  checkoutUrl: string;
  invoiceId: string;
  planId: string;
  amount: number;
  currency: 'DZD';
}

export interface UserSubscriptionState {
  hasActiveSubscription: boolean;
  tier: SubscriptionTier;
  planName?: string | null;
  status: 'ACTIVE' | 'EXPIRED' | 'TRIAL' | 'NONE';
  autoRenew: boolean;
  expiresAt?: string | null;
  daysRemaining?: number | null;
}

export interface SubscriptionInvoice {
  id: string;
  planName: string;
  amount: number;
  paymentStatus: PaymentStatus;
  paymentDate: string;
  receiptUrl?: string | null;
}

// ==========================================
// 8. Reviews & Ratings
// ==========================================

export interface ArtisanReview {
  id: string;
  artisanId: string;
  reviewerId: string;
  reviewerName: string;
  reviewerAvatar?: string | null;
  rating: number; // 1.0 - 5.0
  comment: string;
  createdAt: string;
}

export interface ArtisanReviewCreateRequest {
  rating: number;
  comment: string;
}

export interface ArtisanRatingSummary {
  averageRating: number;
  totalReviews: number;
  distribution: {
    1: number;
    2: number;
    3: number;
    4: number;
    5: number;
  };
}

// ==========================================
// 9. Formations & Workshops
// ==========================================

export type FormationStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'REJECTED' | 'COMPLETED' | 'CANCELLED';

export interface FormationFile {
  id: string;
  filename: string;
  sizeBytes: number;
  downloadUrl: string;
}

export interface FormationSummary {
  id: string;
  title: string;
  description: string;
  craftSubCategory: string;
  instructorName: string;
  instructorAvatar?: string | null;
  scheduledAt: string;
  durationHours: number;
  priceDZD: number;
  capacity: number;
  enrolledCount: number;
  thumbnailUrl?: string | null;
  status: FormationStatus;
}

export interface FormationDetail extends FormationSummary {
  syllabusHtml?: string | null;
  requirements?: string | null;
  files: FormationFile[];
  isEnrolled: boolean;
}

// ==========================================
// 10. Notifications & Reports
// ==========================================

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  type: 'SYSTEM' | 'ORDER' | 'CHAT' | 'FORMATION' | 'SUBSCRIPTION';
  isRead: boolean;
  actionUrl?: string | null;
  createdAt: string;
}

export type ReportTargetType = 'USER' | 'POST' | 'REVIEW';

export interface ContentReportRequest {
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
  details?: string;
}

// ==========================================
// 11. Client Favorites
// ==========================================

export interface ClientFavoriteArtisanResponse {
  favoriteId: string;
  artisanId: string;
  /** ISO-8601 timestamp with microsecond fractional seconds without timezone offset, e.g. "2026-09-24T17:56:45.628794" */
  favoritedAt: string;
}

export interface ClientFavoriteArtisanItem {
  /** ISO-8601 timestamp with microsecond fractional seconds without timezone offset, e.g. "2026-09-24T17:56:45.628794" */
  favoritedAt: string;
  artisan: ArtisanDirectoryCard;
}

export interface FavoriteStatusResponse {
  favorited: boolean;
}
