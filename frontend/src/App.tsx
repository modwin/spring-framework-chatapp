import { useEffect, useState } from 'react'
import { ApiError, authApi, friendshipApi, resetCsrfToken } from './api/client'
import type { FriendshipResponse, LoginRequest, RegisterUserRequest, UserResponse } from './api/contracts'
import { AlertBanner } from './components/AlertBanner'
import { AuthPanel } from './components/AuthPanel'
import { Dashboard } from './components/Dashboard'
import './App.css'

interface Notice {
  kind: 'error' | 'success'
  message: string
}

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    const fieldMessages = Object.values(error.fieldErrors)
    return fieldMessages.length > 0 ? fieldMessages.join(' ') : error.message
  }
  return 'The service could not be reached. Please try again.'
}

function App() {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [friendships, setFriendships] = useState<FriendshipResponse[]>([])
  const [googleEnabled, setGoogleEnabled] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<Notice | null>(null)

  useEffect(() => {
    let active = true
    Promise.all([
      authApi.providers(),
      authApi.currentUser().catch((error: unknown) => {
        if (error instanceof ApiError && error.status === 401) return null
        throw error
      }),
    ])
      .then(([providers, currentUser]) => {
        if (!active) return
        setGoogleEnabled(providers.providers.includes('GOOGLE'))
        setUser(currentUser)
      })
      .catch((error: unknown) => active && setNotice({ kind: 'error', message: errorMessage(error) }))
      .finally(() => active && setLoading(false))
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!user) {
      setFriendships([])
      return
    }
    friendshipApi.list()
      .then(setFriendships)
      .catch((error: unknown) => setNotice({ kind: 'error', message: errorMessage(error) }))
  }, [user])

  async function runAction(action: () => Promise<void>): Promise<boolean> {
    setBusy(true)
    setNotice(null)
    try {
      await action()
      return true
    } catch (error) {
      setNotice({ kind: 'error', message: errorMessage(error) })
      return false
    } finally {
      setBusy(false)
    }
  }

  async function login(request: LoginRequest) {
    await runAction(async () => {
      setUser(await authApi.login(request))
      setNotice({ kind: 'success', message: 'Welcome back.' })
    })
  }

  async function register(request: RegisterUserRequest) {
    await runAction(async () => {
      setUser(await authApi.register(request))
      setNotice({ kind: 'success', message: 'Your account is ready.' })
    })
  }

  async function reloadFriendships() {
    setFriendships(await friendshipApi.list())
  }

  if (loading) {
    return <main className="loading" aria-busy="true"><div className="spinner" /><p>Loading Modwin Chat…</p></main>
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Modwin Chat home"><span>MC</span> Modwin Chat</a>
        <span className={`connection ${user ? 'online' : ''}`}>{user ? 'Signed in' : 'Guest'}</span>
      </header>

      <main>
        {notice && <AlertBanner {...notice} onDismiss={() => setNotice(null)} />}
        {user ? (
          <Dashboard
            busy={busy}
            user={user}
            friendships={friendships}
            onLogout={() => runAction(async () => {
              await authApi.logout()
              resetCsrfToken()
              setUser(null)
              setNotice({ kind: 'success', message: 'You are signed out.' })
            })}
            onSend={(email) => runAction(async () => {
              await friendshipApi.send(email)
              await reloadFriendships()
              setNotice({ kind: 'success', message: 'Friend request sent.' })
            })}
            onAccept={(id) => runAction(async () => {
              await friendshipApi.accept(id)
              await reloadFriendships()
              setNotice({ kind: 'success', message: 'Friend request accepted.' })
            })}
            onRemove={(id) => runAction(async () => {
              await friendshipApi.remove(id)
              await reloadFriendships()
              setNotice({ kind: 'success', message: 'Friendship updated.' })
            })}
          />
        ) : (
          <div className="guest-grid">
            <section className="hero-copy">
              <p className="eyebrow">Accounts and friendships</p>
              <h1>Build your trusted circle.</h1>
              <p>Sign in securely, connect with registered users, and manage pending friendship requests. Messaging is the next planned milestone.</p>
              <ul>
                <li>Session-based Spring Security</li>
                <li>Local accounts and optional Google sign-in</li>
                <li>Explicit, consent-based friendships</li>
              </ul>
            </section>
            <AuthPanel busy={busy} googleEnabled={googleEnabled} onLogin={login} onRegister={register} />
          </div>
        )}
      </main>

      <footer>Modwin Chat · Authentication and friendship foundation</footer>
    </div>
  )
}

export default App
