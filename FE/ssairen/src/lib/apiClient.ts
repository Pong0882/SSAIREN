import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
 * Response Interceptor: 에러 처리
 */
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    // 401 에러 처리 (인증 실패)
    if (error.response?.status === 401) {
      console.error('Unauthorized: Please login again');
      // 필요시 로그아웃 처리
      // localStorage.removeItem('auth-storage');
      // window.location.href = '/login';
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
