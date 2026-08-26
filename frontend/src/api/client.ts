import type {
  AuthProvidersResponse,
  CsrfTokenResponse,
  FriendshipResponse,
  LoginRequest,
  ProblemDetails,
  RegisterUserRequest,
  UserResponse,
} from './contracts'

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, problem: ProblemDetails) {
    super(problem.detail || problem.title || `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = problem.errors ?? {}
  }
}

let csrfToken: CsrfTokenResponse | null = null

async function loadCsrfToken(): Promise<CsrfTokenResponse> {
  if (csrfToken) return csrfToken
  const response = await fetch('/api/csrf', { credentials: 'same-origin' })
  if (!response.ok) throw await toApiError(response)
  csrfToken = await response.json() as CsrfTokenResponse
  return csrfToken
}

async function toApiError(response: Response): Promise<ApiError> {
  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('json')) {
    return new ApiError(response.status, await response.json() as ProblemDetails)
  }
  const detail = await response.text()
  return new ApiError(response.status, { detail })
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = (init.method ?? 'GET').toUpperCase()
  const headers = new Headers(init.headers)
  if (init.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const csrf = await loadCsrfToken()
    headers.set(csrf.headerName, csrf.token)
  }

  const response = await fetch(path, {
    ...init,
    method,
    headers,
    credentials: 'same-origin',
  })
  if (!response.ok) {
    if (response.status === 403) resetCsrfToken()
    throw await toApiError(response)
  }
  if (response.status === 204) return undefined as T
  return await response.json() as T
}

export function resetCsrfToken(): void {
  csrfToken = null
}

export const authApi = {
  providers: () => apiRequest<AuthProvidersResponse>('/api/auth/providers'),
  currentUser: () => apiRequest<UserResponse>('/api/users/me'),
  login: (request: LoginRequest) => apiRequest<UserResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  }),
  register: (request: RegisterUserRequest) => apiRequest<UserResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(request),
  }),
  logout: () => apiRequest<void>('/api/auth/logout', { method: 'POST' }),
}

export const friendshipApi = {
  list: () => apiRequest<FriendshipResponse[]>('/api/friendships'),
  send: (recipientEmail: string) => apiRequest<FriendshipResponse>('/api/friendships', {
    method: 'POST',
    body: JSON.stringify({ recipientEmail }),
  }),
  accept: (id: number) => apiRequest<FriendshipResponse>(`/api/friendships/${id}/accept`, {
    method: 'PATCH',
  }),
  remove: (id: number) => apiRequest<void>(`/api/friendships/${id}`, {
    method: 'DELETE',
  }),
}
