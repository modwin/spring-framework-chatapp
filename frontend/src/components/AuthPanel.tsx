import { useState, type FormEvent } from 'react'
import type { LoginRequest, RegisterUserRequest } from '../api/contracts'

interface AuthPanelProps {
  busy: boolean
  googleEnabled: boolean
  onLogin: (request: LoginRequest) => Promise<void>
  onRegister: (request: RegisterUserRequest) => Promise<void>
}

export function AuthPanel({ busy, googleEnabled, onLogin, onRegister }: AuthPanelProps) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [username, setUsername] = useState('')
  const [name, setName] = useState('')

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (mode === 'login') {
      await onLogin({ email: email.trim(), password })
    } else {
      await onRegister({ email: email.trim(), username: username.trim(), name: name.trim(), password })
    }
  }

  function changeMode(nextMode: 'login' | 'register') {
    setMode(nextMode)
    setPassword('')
  }

  return (
    <div className="auth-card">
      <div className="auth-tabs" role="tablist" aria-label="Authentication method">
        <button type="button" role="tab" aria-selected={mode === 'login'} className={mode === 'login' ? 'active' : ''} onClick={() => changeMode('login')}>
          Sign in
        </button>
        <button type="button" role="tab" aria-selected={mode === 'register'} className={mode === 'register' ? 'active' : ''} onClick={() => changeMode('register')}>
          Create account
        </button>
      </div>

      <form className="auth-form" onSubmit={submit}>
        {mode === 'register' && (
          <>
            <label>
              Full name
              <input value={name} onChange={(event) => setName(event.target.value)} autoComplete="name" maxLength={100} required disabled={busy} />
            </label>
            <label>
              Username
              <input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" minLength={3} maxLength={20} pattern="[A-Za-z0-9._-]+" required disabled={busy} />
            </label>
          </>
        )}
        <label>
          Email
          <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" required disabled={busy} />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} minLength={8} maxLength={72} required disabled={busy} />
        </label>
        <button type="submit" className="btn btn-primary" disabled={busy}>
          {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
        </button>

        {googleEnabled && (
          <>
            <div className="divider"><span>or</span></div>
            <a className="btn btn-secondary" href="/oauth2/authorization/google">Continue with Google</a>
          </>
        )}
      </form>
    </div>
  )
}
