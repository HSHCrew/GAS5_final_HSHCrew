import axios from 'axios';

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 설정 (액세스 토큰 추가)
apiClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터 설정 (토큰 만료 시 리프레시 토큰으로 새 토큰 요청)
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 403 Forbidden 오류 발생 시 토큰 갱신 시도
    if (error.response && error.response.status === 403 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
      const username = localStorage.getItem('username') || sessionStorage.getItem('username');

      if (!refreshToken || !username) {
        console.error('리프레시 토큰 또는 사용자 이름이 없습니다.');
        return Promise.reject(error);
      }

      try {
        // 리프레시 API 호출
        const response = await axios.post('http://localhost:8080/altari/refresh', {
          refreshToken,
          username,
        });

        const newAccessToken = response.data.accessToken;
        const newRefreshToken = response.data.refreshToken;

        // 새 토큰 저장
        if (newAccessToken) {
          localStorage.setItem('token', newAccessToken);
          sessionStorage.setItem('token', newAccessToken);

          if (newRefreshToken) {
            localStorage.setItem('refreshToken', newRefreshToken);
            sessionStorage.setItem('refreshToken', newRefreshToken);
          }

          // 실패했던 요청에 새 토큰으로 재시도
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return apiClient(originalRequest);
        } else {
          throw new Error('새로운 액세스 토큰을 받지 못했습니다.');
        }
      } catch (refreshError) {
        console.error('리프레시 토큰 갱신 실패:', refreshError);
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = "/signIn"; // 로그인 페이지로 리디렉션
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
