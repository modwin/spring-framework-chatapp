import type { FriendshipResponse, UserResponse } from '../api/contracts'
import { FriendshipPanel } from './FriendshipPanel'

interface DashboardProps {
  busy: boolean
  friendships: FriendshipResponse[]
  user: UserResponse
  onAccept: (id: number) => Promise<boolean>
  onLogout: () => Promise<boolean>
  onRemove: (id: number) => Promise<boolean>
  onSend: (email: string) => Promise<boolean>
}

export function Dashboard(props: DashboardProps) {
  const { busy, user, onLogout } = props
  return (
    <div className="dashboard-grid">
      <section className="card profile-card" aria-labelledby="profile-heading">
        <div className="profile-avatar" aria-hidden="true">{user.name.charAt(0).toUpperCase()}</div>
        <p className="eyebrow">Signed in</p>
        <h1 id="profile-heading">{user.name}</h1>
        <dl>
          <div><dt>Username</dt><dd>@{user.username}</dd></div>
          <div><dt>Email</dt><dd>{user.email}</dd></div>
          <div><dt>Roles</dt><dd>{user.roles.join(', ')}</dd></div>
        </dl>
        <button type="button" className="btn btn-secondary" onClick={onLogout} disabled={busy}>Sign out</button>
      </section>
      <FriendshipPanel {...props} />
    </div>
  )
}
