// utils/apiRequest.js

import axios from 'axios';

async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
  const username = localStorage.getItem('username') || sessionStorage.getItem('username');

  try {
    const response = await axios.post('/api/v1/token/refresh', { 
      refreshToken, 
      username 
    });
    const newAccessToken = response.data.accessToken;

    if (newAccessToken) {
      localStorage.setItem('token', newAccessToken);
      sessionStorage.setItem('token', newAccessToken);
      return newAccessToken;
    } else {
      throw new Error('새로운 액세스 토큰을 받지 못했습니다.');
    }
  } catch (error) {
    console.error("토큰 갱신 실패:", error);
    window.location.href = "/login";
  }
}

async function apiRequest(url, options = {}) {
  let accessToken = localStorage.getItem('token') || sessionStorage.getItem('token');
  const config = {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`
    }
  };

  try {
    const response = await axios(url, config);
    return response;
  } catch (error) {
    if (error.response?.status === 401) {
      // 401 에러가 발생하면 토큰 갱신 시도
      const newToken = await refreshAccessToken();
      if (newToken) {
        config.headers['Authorization'] = `Bearer ${newToken}`;
        return await axios(url, config);
      }
    }
    throw error; // 다른 에러가 발생할 경우 그대로 던짐
  }
}

export default apiRequest;
