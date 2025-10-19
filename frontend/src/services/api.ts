import axios from 'axios';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  Claim,
  Policy,
  CreateClaimRequest,
  UpdateClaimStatusRequest,
  CreatePolicyRequest
} from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth API
export const authAPI = {
  register: (data: RegisterRequest): Promise<AuthResponse> =>
    api.post('/auth/register', data).then(res => res.data),
  
  login: (data: LoginRequest): Promise<AuthResponse> =>
    api.post('/auth/login', data).then(res => res.data),
  
  testAuth: (): Promise<any> =>
    api.get('/auth/test').then(res => res.data),
};

// Claims API
export const claimsAPI = {
  getMyClaims: (): Promise<Claim[]> =>
    api.get('/claims/my-claims').then(res => res.data),
  
  getAllClaims: (): Promise<Claim[]> =>
    api.get('/claims').then(res => res.data),
  
  getClaimById: (id: number): Promise<Claim> =>
    api.get(`/claims/${id}`).then(res => res.data),
  
  createClaim: (data: CreateClaimRequest): Promise<Claim> =>
    api.post('/claims', data).then(res => res.data),
  
  updateClaimStatus: (id: number, data: UpdateClaimStatusRequest): Promise<Claim> =>
    api.put(`/claims/${id}/status`, data).then(res => res.data),
  
  deleteClaim: (id: number): Promise<void> =>
    api.delete(`/claims/${id}`).then(res => res.data),
  
  getClaimsByStatus: (status: string): Promise<Claim[]> =>
    api.get(`/claims/status/${status}`).then(res => res.data),
};

// Policies API
export const policiesAPI = {
  getMyPolicies: (): Promise<Policy[]> =>
    api.get('/policies/my-policies').then(res => res.data),
  
  getMyActivePolicies: (): Promise<Policy[]> =>
    api.get('/policies/my-policies/active').then(res => res.data),
  
  getAllPolicies: (): Promise<Policy[]> =>
    api.get('/policies').then(res => res.data),
  
  getPolicyById: (id: number): Promise<Policy> =>
    api.get(`/policies/${id}`).then(res => res.data),
  
  createPolicy: (data: CreatePolicyRequest): Promise<Policy> =>
    api.post('/policies', data).then(res => res.data),
  
  deactivatePolicy: (id: number): Promise<Policy> =>
    api.put(`/policies/${id}/deactivate`).then(res => res.data),
  
  deletePolicy: (id: number): Promise<void> =>
    api.delete(`/policies/${id}`).then(res => res.data),
};

export default api;