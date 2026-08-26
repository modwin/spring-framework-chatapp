import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest, resetCsrfToken } from './client'

afterEach(() => {
  vi.restoreAllMocks()
  resetCsrfToken()
})

describe('apiRequest', () => {
  it('loads and sends the CSRF header for mutations', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(new Response(JSON.stringify({
        token: 'csrf-token',
        headerName: 'X-CSRF-TOKEN',
        parameterName: '_csrf',
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ id: 10 }), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      }))

    await apiRequest('/api/friendships', { method: 'POST', body: '{}' })

    const request = fetchMock.mock.calls[1]
    const headers = request?.[1]?.headers as Headers
    expect(headers.get('X-CSRF-TOKEN')).toBe('csrf-token')
    expect(request?.[1]?.credentials).toBe('same-origin')
  })

  it('exposes ProblemDetail field errors', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(new Response(JSON.stringify({
      title: 'Bad Request',
      detail: 'One or more fields are invalid.',
      errors: { email: 'Email must be valid.' },
    }), { status: 400, headers: { 'Content-Type': 'application/problem+json' } }))

    const error = await apiRequest('/api/example').catch((caught: unknown) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).fieldErrors.email).toBe('Email must be valid.')
  })
})
