import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';
import { useAuthStore } from '@/features/auth/store/authStore';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// 토큰 갱신 상태 관리
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (error?: any) => void;
}> = [];

/**
 * 대기열에 있는 요청들을 처리
 */
const processQueue = (error: Error | null = null) => {
  failedQueue.forEach(promise => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve();
    }
  });

  failedQueue = [];
};

/**
 * Axios 인스턴스 생성
 */
const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

/**
 * Request Interceptor: access token을 자동으로 헤더에 추가
 */
axiosInstance.interceptors.request.use(
  (config) => {
    // requiresAuth가 false가 아닌 경우에만 토큰 추가
    const requiresAuth = (config as any).requiresAuth !== false;

    if (requiresAuth) {
      const authStorage = localStorage.getItem('auth-storage');
      if (authStorage) {
        try {
          const parsedStorage = JSON.parse(authStorage);
          const accessToken = parsedStorage?.state?.accessToken;

          if (accessToken && config.headers) {
            config.headers.Authorization = `Bearer ${accessToken}`;
          }
        } catch (error) {
          console.error('Failed to parse auth storage:', error);
        }
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor: 에러 처리 및 토큰 갱신
 */
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // 401 에러 처리 (인증 실패)
    if (error.response?.status === 401 && originalRequest) {
      // refresh API 자체가 실패한 경우 무한 루프 방지
      if (originalRequest.url?.includes('/api/auth/refresh')) {
        console.error('Refresh token expired: Please login again');
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        return Promise.reject(new Error('토큰이 만료되었습니다. 다시 로그인해주세요.'));
      }

      // 이미 재시도한 요청이면 무한 루프 방지
      if (originalRequest._retry) {
        console.error('Token refresh failed: Please login again');
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        return Promise.reject(new Error('인증에 실패했습니다. 다시 로그인해주세요.'));
      }

      // 토큰 갱신 중이면 대기열에 추가
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => {
            // 토큰 갱신 성공 후 원래 요청 재시도
            const authStorage = localStorage.getItem('auth-storage');
            if (authStorage) {
              try {
                const parsedStorage = JSON.parse(authStorage);
                const accessToken = parsedStorage?.state?.accessToken;
                if (accessToken && originalRequest.headers) {
                  originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                }
              } catch (error) {
                console.error('Failed to parse auth storage:', error);
              }
            }
            return axiosInstance(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      // 토큰 갱신 시작
      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // auth-storage에서 refreshToken 가져오기
        const authStorage = localStorage.getItem('auth-storage');
        if (!authStorage) {
          throw new Error('No auth storage available');
        }

        const parsedStorage = JSON.parse(authStorage);
        const refreshToken = parsedStorage?.state?.refreshToken;

        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // refreshTokenApi를 직접 import하면 순환 참조가 발생할 수 있으므로
        // axios를 직접 사용해서 refresh 요청
        const response = await axios.post(
          `${API_URL}/api/auth/refresh`,
          { refreshToken },
          {
            headers: {
              'Content-Type': 'application/json',
            },
            withCredentials: true,
          }
        );

        const apiResponse = response.data;
        if (!apiResponse.success || !apiResponse.data) {
          throw new Error(apiResponse.message || '토큰 갱신에 실패했습니다.');
        }

        const tokenData = apiResponse.data;

        // zustand store 업데이트 (메모리상의 store 동기화)
        useAuthStore.getState().updateTokens(
          tokenData.accessToken,
          tokenData.refreshToken
        );

        // 원래 요청에 새 토큰 적용
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${tokenData.accessToken}`;
        }

        // 대기열의 모든 요청 재시도
        processQueue(null);
        isRefreshing = false;

        // 원래 요청 재시도
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        // 토큰 갱신 실패
        processQueue(refreshError as Error);
        isRefreshing = false;

        console.error('Token refresh failed:', refreshError);

        // zustand store 정리
        useAuthStore.getState().clearAuth();

        window.location.href = '/login';

        return Promise.reject(new Error('토큰 갱신에 실패했습니다. 다시 로그인해주세요.'));
      }
    }

    // 에러 메시지 추출
    const errorMessage =
      (error.response?.data as any)?.message ||
      error.message ||
      'An error occurred';

    return Promise.reject(new Error(errorMessage));
  }
);

export interface ApiClientOptions extends AxiosRequestConfig {
  requiresAuth?: boolean;
}

export default axiosInstance;
