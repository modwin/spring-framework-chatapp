import { useState, type FormEvent } from 'react'
import type { FriendshipResponse, UserResponse } from '../api/contracts'

interface FriendshipPanelProps {
  busy: boolean
  friendships: FriendshipResponse[]
  user: UserResponse
  onSend: (email: string) => Promise<boolean>
  onAccept: (id: number) => Promise<boolean>
  onRemove: (id: number) => Promise<boolean>
}

export function FriendshipPanel({ busy, friendships, user, onSend, onAccept, onRemove }: FriendshipPanelProps) {
  const [email, setEmail] = useState('')

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (await onSend(email.trim())) setEmail('')
  }

  return (
    <section className="card friendship-card" aria-labelledby="friendships-heading">
      <div>
        <p className="eyebrow">Social graph</p>
        <h2 id="friendships-heading">Friendships</h2>
      </div>

      <form className="friend-form" onSubmit={submit}>
        <label htmlFor="friend-email" className="sr-only">Friend's email</label>
        <input id="friend-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="friend@example.com" autoComplete="email" required disabled={busy} />
        <button type="submit" className="btn btn-primary" disabled={busy}>Send request</button>
      </form>

      {friendships.length === 0 ? (
        <p className="empty-state">No friendship requests yet. Send one using a registered email address.</p>
      ) : (
        <ul className="friendship-list">
          {friendships.map((friendship) => {
            const incoming = friendship.recipient.id === user.id
            const other = incoming ? friendship.requester : friendship.recipient
            return (
              <li key={friendship.id}>
                <div className="avatar" aria-hidden="true">{other.name.charAt(0).toUpperCase()}</div>
                <div className="friendship-copy">
                  <strong>{other.name}</strong>
                  <span>@{other.username} · {friendship.status === 'PENDING' ? incoming ? 'wants to connect' : 'request sent' : 'friend'}</span>
                </div>
                <div className="friendship-actions">
                  {friendship.status === 'PENDING' && incoming && (
                    <button type="button" className="btn btn-small btn-primary" onClick={() => onAccept(friendship.id)} disabled={busy}>Accept</button>
                  )}
                  <button type="button" className="btn btn-small btn-quiet" onClick={() => onRemove(friendship.id)} disabled={busy}>
                    {friendship.status === 'ACCEPTED' ? 'Remove' : incoming ? 'Decline' : 'Cancel'}
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
