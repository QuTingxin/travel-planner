import axios from 'axios';
import { TravelPlan, Expense, AuthResponse, LoginRequest, RegisterRequest } from '../types';
import { message } from 'antd';

const API_BASE_URL = 'http://localhost:8080/api';

// 创建 axios 实例
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    } 
    return config;
  },
  (error) => {
    console.error('请求配置错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response.status, response.config.url);
    return response;
  },
  (error) => {

    
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

// API 接口
export const authAPI = {
  login: (data: LoginRequest): Promise<AuthResponse> =>
    api.post('/auth/login', data).then(res => res.data),
  
  register: (data: RegisterRequest): Promise<string> => {
    message.info('注册中，请稍候...' + data.username);
    return api.post('/auth/register', data).then(res => res.data)
  }
    
};

export const travelPlanAPI = {
  getAll: (): Promise<TravelPlan[]> =>
    api.get('/travel-plans').then(res => res.data),
  
  getById: (id: number): Promise<TravelPlan> =>
    api.get(`/travel-plans/${id}`).then(res => res.data),
  
  create: (data: Omit<TravelPlan, 'id'>): Promise<TravelPlan> =>
    api.post('/travel-plans', data).then(res => res.data),
  
  delete: (id: number): Promise<void> =>
    api.delete(`/travel-plans/${id}`),
  
  search: (destination: string): Promise<TravelPlan[]> =>
    api.get(`/travel-plans/search?destination=${destination}`).then(res => res.data),
};

export const voicePlanAPI = {
    generate: (voiceText: string): Promise<any> =>
        api.post('/voice-plan/generate', { voiceText }).then(res => res.data),
};

export const expenseAPI = {
  getByPlan: (planId: number): Promise<Expense[]> =>
    api.get(`/expenses/plan/${planId}`).then(res => res.data),
  
  create: (data: Omit<Expense, 'id'>): Promise<Expense> =>
    api.post('/expenses', data).then(res => res.data),
  
  update: (id: number, data: Partial<Expense>): Promise<Expense> =>
    api.put(`/expenses/${id}`, data).then(res => res.data),
  
  delete: (id: number): Promise<void> =>
    api.delete(`/expenses/${id}`),
};

export default api;