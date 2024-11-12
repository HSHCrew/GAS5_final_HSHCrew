import axios from 'axios';

// Axios 인스턴스 생성
const axiosInstance = axios.create({
  // baseURL: import.meta.env.VITE_API_BASE_URL, // 환경 변수 사용
  baseURL: 'http://localhost:8080', // 환경 변수 사용
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: 모든 요청에 JWT 토큰을 헤더에 추가
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터: 토큰 만료 시 처리
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // 토큰 만료 시 로직 (예: 로그아웃 처리, 로그인 페이지로 이동 등)
      localStorage.removeItem('token');
      sessionStorage.removeItem('token');
      window.location.href = '/signin'; // 로그인 페이지로 리다이렉트
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
