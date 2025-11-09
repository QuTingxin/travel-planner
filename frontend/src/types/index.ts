export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  createdAt: string;
}

export interface TravelPlan {
  id?: number;
  destination: string;
  startDate: string;
  endDate: string;
  budget: number;
  travelerCount: number;
  preferences: string;
  itinerary?: string;
  createdAt?: string;
  user?: User;
  expenses?: Expense[];
}

export interface Expense {
  id?: number;
  category: string;
  description: string;
  amount: number;
  expenseDate: string;
  travelPlan?: TravelPlan;
}

export interface AuthResponse {
  token: string;
  username: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phone?: string;
}