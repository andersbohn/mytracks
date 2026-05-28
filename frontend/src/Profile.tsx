import { useEffect, useState } from 'react'
import TrackList from './TrackList'
import UploadPanel from './UploadPanel'

type User = { id: string; email: string; displayName: string | null }

export default function Profile({ onLogout }: { onLogout: () => void }) {
  const [user, setUser] = useState<User | null>(null)
  const [trackRefreshKey, setTrackRefreshKey] = useState(0)

  useEffect(() => {
    fetch('/mytracks/api/me')
      .then((r) => r.json())
      .then(setUser)
  }, [])

  const logout = async () => {
    await fetch('/mytracks/api/auth/logout', { method: 'POST' })
    onLogout()
  }

  if (!user) return <p>Loading…</p>

  return (
    <div>
      <h1>Welcome, {user.displayName ?? user.email}</h1>
      <p>{user.email}</p>
      <button onClick={logout}>Sign out</button>
      <UploadPanel onUploaded={() => setTrackRefreshKey((k) => k + 1)} />
      <h2>Tracks</h2>
      <TrackList key={trackRefreshKey} />
    </div>
  )
}
