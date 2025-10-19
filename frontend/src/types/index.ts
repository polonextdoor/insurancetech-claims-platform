// Type definitions for InsuranceTech Claims Platform

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: 'CUSTOMER' | 'AGENT' | 'ADJUSTER' | 'ADMIN';
  isActive: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface Policy {
  id: number;
  policyNumber: string;
  userId: number;
  customerName: string;
  policyType: 'AUTO' | 'HOME' | 'HEALTH' | 'LIFE' | 'BUSINESS';
  coverageAmount: number;
  deductible: number;
  premiumAmount: number;
  startDate: string;
  endDate: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Claim {
  id: number;
  claimNumber: string;
  policyId: number;
  policyNumber: string;
  policyType: string;
  userId: number;
  customerName: string;
  assignedAdjusterId?: number;
  assignedAdjusterName?: string;
  incidentDate: string;
  reportedDate: string;
  incidentDescription: string;
  incidentLocation?: string;
  claimedAmount: number;
  approvedAmount?: number;
  deductibleAmount?: number;
  status: 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'INVESTIGATING' | 'APPROVED' | 'DENIED' | 'CLOSED';
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  riskScore: number;
  fraudFlag: boolean;
  fraudScore: number;
  submittedAt?: string;
  reviewedAt?: string;
  closedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateClaimRequest {
  policyId: number;
  incidentDate: string;
  incidentDescription: string;
  incidentLocation?: string;
  claimedAmount: number;
}

export interface UpdateClaimStatusRequest {
  status: string;
  notes?: string;
  approvedAmount?: number;
  assignedAdjusterId?: number;
}

export interface CreatePolicyRequest {
  userId: number;
  policyType: string;
  coverageAmount: number;
  deductible: number;
  premiumAmount: number;
  startDate: string;
  endDate: string;
}