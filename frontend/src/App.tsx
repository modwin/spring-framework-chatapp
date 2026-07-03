import { useState, useEffect } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

interface Role {
  id?: number
  name: string
}

interface Friend {
  id?: number
  email: string
  username: string
  name: string
}

interface UserDto {
  email: string
  username: string
  name: string
  roles: Role[]
  friends: Friend[]
}

function App() {
  const [user, setUser] = useState<UserDto | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')
  const [actionLoading, setActionLoading] = useState<boolean>(false)

  // Login form state
  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')

  // Register form state
  const [regEmail, setRegEmail] = useState('')
  const [regUsername, setRegUsername] = useState('')
  const [regName, setRegName] = useState('')
  const [regPassword, setRegPassword] = useState('')

  // Friend form state
  const [addFriendEmail, setAddFriendEmail] = useState('')

  // Notifications
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [validationErrors, setValidationErrors] = useState<{ [key: string]: string }>({})

  // Fetch current user profile on mount
  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      const response = await fetch('/api/users/profile')
      if (response.ok) {
        const data: UserDto = await response.json()
        setUser(data)
      } else {
        setUser(null)
      }
    } catch (err) {
      console.error('Error fetching profile:', err)
      setUser(null)
    } finally {
      setLoading(false)
    }
  }

  // Clear messages
  const clearMessages = () => {
    setErrorMessage(null)
    setSuccessMessage(null)
    setValidationErrors({})
  }

  // Client-side validations
  const validateRegister = () => {
    const errors: { [key: string]: string } = {}
    if (!regEmail) errors.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(regEmail)) errors.email = 'Invalid email format'

    if (!regUsername) errors.username = 'Username is required'
    else if (regUsername.length < 3 || regUsername.length > 20) {
      errors.username = 'Username must be between 3 and 20 characters'
    }

    if (!regName) errors.name = 'Full Name is required'

    if (!regPassword) errors.password = 'Password is required'
    else if (regPassword.length < 8 || regPassword.length > 40) {
      errors.password = 'Password must be between 8 and 40 characters'
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  // Handle local login
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    clearMessages()
    if (!loginEmail || !loginPassword) {
      setErrorMessage('Please fill in all fields')
      return
    }

    setActionLoading(true)
    try {
      const response = await fetch('/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: loginEmail,
          password: loginPassword,
        }),
      })

      if (response.ok) {
        const loggedInUser: UserDto = await response.json()
        setUser(loggedInUser)
        setSuccessMessage('Successfully logged in!')
        setLoginEmail('')
        setLoginPassword('')
        // Refresh to fetch up-to-date roles/friends list
        await fetchProfile()
      } else {
        setErrorMessage('Invalid email or password.')
      }
    } catch (err) {
      setErrorMessage('A network error occurred. Please try again.')
      console.error(err)
    } finally {
      setActionLoading(false)
    }
  }

  // Handle local registration
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    clearMessages()

    if (!validateRegister()) {
      return
    }

    setActionLoading(true)
    try {
      const response = await fetch('/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: regEmail,
          username: regUsername,
          name: regName,
          password: regPassword,
        }),
      })

      if (response.ok) {
        setSuccessMessage('Account registered successfully! Logging you in...')
        setRegEmail('')
        setRegUsername('')
        setRegName('')
        setRegPassword('')
        // The backend automatically logs in the registered user and sets context
        await fetchProfile()
      } else {
        const text = await response.text()
        setErrorMessage(text || 'Registration failed. Please check your details.')
      }
    } catch (err) {
      setErrorMessage('A network error occurred. Please try again.')
      console.error(err)
    } finally {
      setActionLoading(false)
    }
  }

  // Handle Add Friend
  const handleAddFriend = async (e: React.FormEvent) => {
    e.preventDefault()
    clearMessages()

    const email = addFriendEmail.trim()
    if (!email) {
      setErrorMessage('Please enter a friend\'s email address.')
      return
    }

    setActionLoading(true)
    try {
      const response = await fetch('/api/users/addFriend', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
        },
        body: email,
      })

      if (response.ok) {
        setSuccessMessage(`Successfully added friend: ${email}`)
        setAddFriendEmail('')
        await fetchProfile()
      } else {
        const text = await response.text()
        setErrorMessage(text || 'Failed to add friend. Verify they are registered in the system.')
      }
    } catch (err) {
      setErrorMessage('A network error occurred.')
      console.error(err)
    } finally {
      setActionLoading(false)
    }
  }

  // Handle Remove Friend
  const handleRemoveFriend = async (friendEmail: string) => {
    clearMessages()
    if (!confirm(`Are you sure you want to remove ${friendEmail} from your friends list?`)) {
      return
    }

    setActionLoading(true)
    try {
      const response = await fetch('/api/users/removeFriend', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
        },
        body: friendEmail,
      })

      if (response.ok) {
        setSuccessMessage(`Removed friend: ${friendEmail}`)
        await fetchProfile()
      } else {
        setErrorMessage('Failed to remove friend.')
      }
    } catch (err) {
      setErrorMessage('A network error occurred.')
      console.error(err)
    } finally {
      setActionLoading(false)
    }
  }

  // Handle Logout
  const handleLogout = async () => {
    clearMessages()
    setActionLoading(true)
    try {
      const response = await fetch('/api/users/logout', {
        method: 'POST',
      })
      if (response.ok || response.status === 302) {
        setUser(null)
        setSuccessMessage('Successfully logged out.')
      } else {
        // Fallback local state reset
        setUser(null)
      }
    } catch (err) {
      setUser(null)
      console.error(err)
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading application context...</p>
      </div>
    )
  }

  return (
    <>
      {/* Brand Header */}
      <header className="brand-header">
        <div className="brand-container">
          <div className="logo-section">
            <img src={reactLogo} className="logo-spin" alt="Logo" width="32" height="32" />
            <span className="brand-title">Modwin Chat</span>
          </div>
          <div className="status-badge-container">
            {user ? (
              <span className="status-badge logged-in">
                <span className="indicator-dot green"></span> Connected
              </span>
            ) : (
              <span className="status-badge guest">
                <span className="indicator-dot gray"></span> Guest Session
              </span>
            )}
          </div>
        </div>
      </header>

      <section id="center" className="container">
        {/* Banner Alert Messages */}
        {errorMessage && (
          <div className="alert alert-error">
            <svg viewBox="0 0 24 24" className="alert-icon" width="20" height="20">
              <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
            <span>{errorMessage}</span>
            <button className="alert-close" onClick={() => setErrorMessage(null)}>&times;</button>
          </div>
        )}

        {successMessage && (
          <div className="alert alert-success">
            <svg viewBox="0 0 24 24" className="alert-icon" width="20" height="20">
              <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            <span>{successMessage}</span>
            <button className="alert-close" onClick={() => setSuccessMessage(null)}>&times;</button>
          </div>
        )}

        {!user ? (
          /* ================= GUEST HOME SECTION ================= */
          <div className="guest-grid">
            {/* Hero / Intro Section */}
            <div className="hero-section">
              <div className="hero">
                <img src={heroImg} className="base" width="140" height="147" alt="Modwin Hero" />
                <img src={reactLogo} className="framework" alt="React logo" />
                <img src={viteLogo} className="vite" alt="Vite logo" />
              </div>
              <h1 className="hero-heading">Connect Instantly & Securely</h1>
              <p className="hero-subtext">
                A modern, production-grade Spring Boot full-stack chat application. Log in with Google OAuth2 or register a secure local account to get started immediately.
              </p>
              <div className="hero-features">
                <div className="feature-item">
                  <div className="feature-bullet">✓</div>
                  <div><strong>Stateful Spring Security 6+</strong> integration</div>
                </div>
                <div className="feature-item">
                  <div className="feature-bullet">✓</div>
                  <div><strong>OAuth2 / OIDC Social Login</strong> (Google SSO)</div>
                </div>
                <div className="feature-item">
                  <div className="feature-bullet">✓</div>
                  <div><strong>Realtime user interaction</strong> & friends network</div>
                </div>
              </div>
            </div>

            {/* Auth Forms Card */}
            <div className="auth-card">
              <div className="auth-tabs">
                <button
                  type="button"
                  className={`tab-btn ${authMode === 'login' ? 'active' : ''}`}
                  onClick={() => { setAuthMode('login'); clearMessages(); }}
                >
                  Sign In
                </button>
                <button
                  type="button"
                  className={`tab-btn ${authMode === 'register' ? 'active' : ''}`}
                  onClick={() => { setAuthMode('register'); clearMessages(); }}
                >
                  Create Account
                </button>
              </div>

              <div className="auth-form-container">
                {authMode === 'login' ? (
                  /* --- SIGN IN FORM --- */
                  <form onSubmit={handleLogin} className="auth-form">
                    <p className="form-description">Enter your registered email and password to log in.</p>
                    
                    <div className="form-group">
                      <label htmlFor="login-email">Email Address</label>
                      <input
                        id="login-email"
                        type="email"
                        value={loginEmail}
                        onChange={(e) => setLoginEmail(e.target.value)}
                        placeholder="you@example.com"
                        required
                        disabled={actionLoading}
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="login-password">Password</label>
                      <input
                        id="login-password"
                        type="password"
                        value={loginPassword}
                        onChange={(e) => setLoginPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                        disabled={actionLoading}
                      />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                      {actionLoading ? 'Signing in...' : 'Sign In'}
                    </button>

                    {/* Social Login Options */}
                    <div className="divider-container">
                      <span className="divider-line"></span>
                      <span className="divider-text">Or continue with</span>
                      <span className="divider-line"></span>
                    </div>

                    <a
                      href="/oauth2/authorization/google"
                      className="btn btn-google"
                      onClick={() => setActionLoading(true)}
                    >
                      <svg viewBox="0 0 24 24" width="18" height="18" className="google-icon">
                        <path fill="#EA4335" d="M12 5.04c1.66 0 3.2.57 4.38 1.69l3.27-3.3C17.65 1.58 14.99 1 12 1 7.35 1 3.37 3.65 1.39 7.5l3.92 3.04C6.27 7.55 8.93 5.04 12 5.04z"/>
                        <path fill="#4285F4" d="M23.49 12.27c0-.81-.07-1.59-.2-2.36H12v4.47h6.46c-.28 1.48-1.12 2.74-2.38 3.58l3.71 2.88c2.17-2 3.7-4.94 3.7-8.57z"/>
                        <path fill="#FBBC05" d="M5.31 10.54c-.23-.69-.36-1.42-.36-2.18s.13-1.49.36-2.18L1.39 3.14C.5 4.88 0 6.84 0 8.9c0 2.06.5 4.02 1.39 5.76l3.92-3.12z"/>
                        <path fill="#34A853" d="M12 23c3.24 0 5.97-1.07 7.96-2.91l-3.71-2.88c-1.03.69-2.35 1.1-4.25 1.1-3.07 0-5.73-2.51-6.69-5.5l-3.92 3.04C3.37 20.35 7.35 23 12 23z"/>
                      </svg>
                      Sign in with Google
                    </a>
                  </form>
                ) : (
                  /* --- REGISTER FORM --- */
                  <form onSubmit={handleRegister} className="auth-form">
                    <p className="form-description">Fill in the fields below to create a secure local account.</p>

                    <div className="form-group">
                      <label htmlFor="reg-name">Full Name</label>
                      <input
                        id="reg-name"
                        type="text"
                        value={regName}
                        onChange={(e) => setRegName(e.target.value)}
                        placeholder="John Doe"
                        required
                        disabled={actionLoading}
                      />
                      {validationErrors.name && <span className="field-error">{validationErrors.name}</span>}
                    </div>

                    <div className="form-group">
                      <label htmlFor="reg-email">Email Address</label>
                      <input
                        id="reg-email"
                        type="email"
                        value={regEmail}
                        onChange={(e) => setRegEmail(e.target.value)}
                        placeholder="you@example.com"
                        required
                        disabled={actionLoading}
                      />
                      {validationErrors.email && <span className="field-error">{validationErrors.email}</span>}
                    </div>

                    <div className="form-group">
                      <label htmlFor="reg-username">Username</label>
                      <input
                        id="reg-username"
                        type="text"
                        value={regUsername}
                        onChange={(e) => setRegUsername(e.target.value)}
                        placeholder="johndoe12"
                        required
                        disabled={actionLoading}
                      />
                      {validationErrors.username && <span className="field-error">{validationErrors.username}</span>}
                    </div>

                    <div className="form-group">
                      <label htmlFor="reg-password">Password (8-40 characters)</label>
                      <input
                        id="reg-password"
                        type="password"
                        value={regPassword}
                        onChange={(e) => setRegPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                        disabled={actionLoading}
                      />
                      {validationErrors.password && <span className="field-error">{validationErrors.password}</span>}
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                      {actionLoading ? 'Creating Account...' : 'Register Account'}
                    </button>
                  </form>
                )}
              </div>
            </div>
          </div>
        ) : (
          /* ================= AUTHENTICATED PROFILE & FRIEND PANEL ================= */
          <div className="profile-dashboard">
            <div className="dashboard-welcome">
              <span className="user-avatar-large">
                {user.name ? user.name.charAt(0).toUpperCase() : user.username.charAt(0).toUpperCase()}
              </span>
              <h2>Welcome back, {user.name || user.username}!</h2>
              <p className="dashboard-subtext">Manage your account profile and friends network in real time.</p>
            </div>

            <div className="dashboard-grid">
              {/* Profile details */}
              <div className="dashboard-card profile-info-card">
                <h3>Account Information</h3>
                <div className="profile-details-list">
                  <div className="detail-row">
                    <span className="detail-label">Full Name:</span>
                    <span className="detail-value">{user.name}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Username:</span>
                    <span className="detail-value"><code>{user.username}</code></span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Email:</span>
                    <span className="detail-value">{user.email}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Roles:</span>
                    <span className="detail-value">
                      {user.roles && user.roles.map((r, i) => (
                        <span key={i} className="badge-role">ROLE_{r.name}</span>
                      ))}
                    </span>
                  </div>
                </div>
                <div className="card-footer">
                  <button onClick={handleLogout} className="btn btn-danger" disabled={actionLoading}>
                    {actionLoading ? 'Logging out...' : 'Log Out'}
                  </button>
                </div>
              </div>

              {/* Friends Manager */}
              <div className="dashboard-card friends-card">
                <h3>Friends Network</h3>
                
                {/* Add friend form */}
                <form onSubmit={handleAddFriend} className="add-friend-form">
                  <div className="input-group">
                    <input
                      type="email"
                      value={addFriendEmail}
                      onChange={(e) => setAddFriendEmail(e.target.value)}
                      placeholder="Enter friend's email address..."
                      required
                      disabled={actionLoading}
                    />
                    <button type="submit" className="btn btn-accent btn-small" disabled={actionLoading}>
                      Add Friend
                    </button>
                  </div>
                </form>

                {/* Friends List */}
                <div className="friends-list-container">
                  {user.friends && user.friends.length > 0 ? (
                    <ul className="friends-list">
                      {user.friends.map((friend, idx) => (
                        <li key={idx} className="friend-item">
                          <div className="friend-details">
                            <span className="friend-avatar">
                              {friend.name ? friend.name.charAt(0).toUpperCase() : friend.username.charAt(0).toUpperCase()}
                            </span>
                            <div className="friend-meta">
                              <span className="friend-name">{friend.name}</span>
                              <span className="friend-username">@{friend.username} &middot; {friend.email}</span>
                            </div>
                          </div>
                          <button
                            type="button"
                            className="btn-icon-remove"
                            title="Remove Friend"
                            onClick={() => handleRemoveFriend(friend.email)}
                            disabled={actionLoading}
                          >
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                              <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                            </svg>
                          </button>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <div className="empty-friends">
                      <svg viewBox="0 0 24 24" width="48" height="48" className="empty-icon">
                        <path fill="currentColor" d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H2v2h2v3h2v-3h2v-2H6zm9 4c-2.67 0-9 1.34-9 4v2h18v-2c0-2.66-6.33-4-9-4z"/>
                      </svg>
                      <p>You haven't added any friends yet.</p>
                      <p className="empty-subtext">Add a friend above using their registered email address.</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </section>

      <div className="ticks"></div>

      {/* Modern responsive secondary section */}
      <section id="next-steps">
        <div id="docs">
          <h2>Application Guide</h2>
          <p>Learn how this security and database application is structured:</p>
          <ul className="guide-links">
            <li>
              <a href="/h2-console" target="_blank" rel="noreferrer">
                H2 Database Console
              </a>
            </li>
            <li>
              <a href="https://spring.io/projects/spring-security" target="_blank" rel="noreferrer">
                Spring Security 6 Docs
              </a>
            </li>
          </ul>
        </div>
        <div id="social">
          <h2>Project Repositories</h2>
          <p>Inspect code standardizations and build specs:</p>
          <ul>
            <li>
              <a href="https://github.com/" target="_blank" rel="noreferrer">
                Spring Backend
              </a>
            </li>
            <li>
              <a href="https://github.com/" target="_blank" rel="noreferrer">
                React Frontend
              </a>
            </li>
          </ul>
        </div>
      </section>

      <div className="ticks"></div>
      <section id="spacer"></section>
    </>
  )
}

export default App
