import { render, screen } from '@testing-library/react'
import { afterEach, expect, it, vi } from 'vitest'
import App from './App'
import { resetCsrfToken } from './api/client'

afterEach(() => {
  vi.restoreAllMocks()
  resetCsrfToken()
})

it('shows the authentication experience for an anonymous visitor', async () => {
  vi.spyOn(globalThis, 'fetch').mockImplementation(async (input) => {
    const url = String(input)
    if (url === '/api/auth/providers') {
      return new Response(JSON.stringify({ providers: ['LOCAL'] }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    }
    if (url === '/api/users/me') {
      return new Response(JSON.stringify({ title: 'Unauthorized', detail: 'Authentication is required.' }), {
        status: 401,
        headers: { 'Content-Type': 'application/problem+json' },
      })
    }
    throw new Error(`Unexpected request: ${url}`)
  })

  render(<App />)

  expect(await screen.findByRole('heading', { name: 'Build your trusted circle.' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument()
  expect(screen.queryByRole('link', { name: 'Continue with Google' })).not.toBeInTheDocument()
})

it('shows the authenticated profile and incoming friendship requests', async () => {
  vi.spyOn(globalThis, 'fetch').mockImplementation(async (input) => {
    const url = String(input)
    if (url === '/api/auth/providers') {
      return Response.json({ providers: ['LOCAL', 'GOOGLE'] })
    }
    if (url === '/api/users/me') {
      return Response.json({ id: 2, email: 'bob@example.com', username: 'bob', name: 'Bob', roles: ['USER'] })
    }
    if (url === '/api/friendships') {
      return Response.json([{
        id: 10,
        requester: { id: 1, username: 'alice', name: 'Alice' },
        recipient: { id: 2, username: 'bob', name: 'Bob' },
        status: 'PENDING',
      }])
    }
    throw new Error(`Unexpected request: ${url}`)
  })

  render(<App />)

  expect(await screen.findByRole('heading', { name: 'Bob' })).toBeInTheDocument()
  expect(await screen.findByText('@alice · wants to connect')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Accept' })).toBeInTheDocument()
})
