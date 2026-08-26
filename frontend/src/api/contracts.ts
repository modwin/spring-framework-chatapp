export interface UserResponse {
  id: number
  email: string
  username: string
  name: string
  roles: string[]
}

export interface FriendSummary {
  id: number
  username: string
  name: string
}

export interface FriendshipResponse {
  id: number
  requester: FriendSummary
  recipient: FriendSummary
  status: 'PENDING' | 'ACCEPTED'
}

export interface AuthProvidersResponse {
  providers: string[]
}

export interface CsrfTokenResponse {
  token: string
  headerName: string
  parameterName: string
}

export interface ProblemDetails {
  title?: string
  status?: number
  detail?: string
  errors?: Record<string, string>
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterUserRequest extends LoginRequest {
  username: string
  name: string
}
