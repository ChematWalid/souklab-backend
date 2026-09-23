/**
 * SoukLab Production HTTP & WebSocket Client Template
 *
 * Solves the classic frontend 401 race condition bug during JWT rotation.
 * Compatible with React, Next.js, Vue, and Vite.
 *
 * Dependencies:
 *   npm install axios @stomp/stompjs
 */

import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { Client as StompClient } from '@stomp/stompjs';
import type { ApiResponse, AuthResponse, RefreshTokenRequest } from './types';

// ==========================================
// 1. Environment & Token Storage Abstraction
// ==========================================

const API_BASE_URL =
  (typeof process !== 'undefined' && process.env.NEXT_PUBLIC_API_URL) ||
  'http://localhost:8080';

const TOKEN_KEYS = {
  ACCESS: 'souklab_access_token',
  REFRESH: 'souklab_refresh_token',
};

export const tokenStorage = {
  getAccessToken: (): string | null => {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(TOKEN_KEYS.ACCESS);
  },
  getRefreshToken: (): string | null => {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(TOKEN_KEYS.REFRESH);
  },
  setTokens: (accessToken: string, refreshToken: string): void => {
    if (typeof window === 'undefined') return;
    localStorage.setItem(TOKEN_KEYS.ACCESS, accessToken);
    localStorage.setItem(TOKEN_KEYS.REFRESH, refreshToken);
  },
  clearTokens: (): void => {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(TOKEN_KEYS.ACCESS);
    localStorage.removeItem(TOKEN_KEYS.REFRESH);
  },
};

// ==========================================
// 2. Axios Instance Setup
// ==========================================

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// ==========================================
// 3. Request Interceptor: Attach Bearer JWT
// ==========================================

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ==========================================
// 4. Response Interceptor: 401 Refresh Queue
// ==========================================

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Don't intercept auth login/refresh requests themselves
    const isAuthRoute =
      originalRequest?.url?.includes('/api/v1/auth/login') ||
      originalRequest?.url?.includes('/api/v1/auth/refresh');

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthRoute) {
      if (isRefreshing) {
        // Enqueue request while another refresh is already in flight
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = tokenStorage.getRefreshToken();
      if (!refreshToken) {
        tokenStorage.clearTokens();
        isRefreshing = false;
        if (typeof window !== 'undefined') window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const refreshResponse = await axios.post<ApiResponse<AuthResponse>>(
          `${API_BASE_URL}/api/v1/auth/refresh`,
          { refreshToken } as RefreshTokenRequest
        );

        const newAuth = refreshResponse.data.data;
        tokenStorage.setTokens(newAuth.accessToken, newAuth.refreshToken);

        processQueue(null, newAuth.accessToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAuth.accessToken}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        tokenStorage.clearTokens();
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// ==========================================
// 5. STOMP / WebSocket Client Helper
// ==========================================

export interface CreateStompClientOptions {
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: unknown) => void;
}

/**
 * Creates an authenticated STOMP client connected to SoukLab RabbitMQ relay.
 */
export function createSouklabWebSocket(options: CreateStompClientOptions = {}): StompClient {
  const wsUrl = API_BASE_URL.replace(/^http/, 'ws') + '/ws/chat';
  const token = tokenStorage.getAccessToken();

  const stompClient = new StompClient({
    brokerURL: wsUrl,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    debug: (str) => {
      if (process.env.NODE_ENV === 'development') {
        console.debug('[STOMP]', str);
      }
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });

  stompClient.onConnect = () => {
    options.onConnect?.();
  };

  stompClient.onStompError = (frame) => {
    console.error('[STOMP Broker Error]', frame.headers['message'], frame.body);
    options.onError?.(frame);
  };

  stompClient.onWebSocketClose = () => {
    options.onDisconnect?.();
  };

  return stompClient;
}
