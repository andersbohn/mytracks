import { useEffect, useState } from 'react'
import Login from './Login'
import Profile from './Profile'

type UserInfo = { id: string; email: string; displayName: string | null; role: 'GUEST' | 'USER' | 'ADMIN' }

type AuthStatus = {
  authenticated: boolean
  user: UserInfo | null
  loginUrl: string | null
  googleClientId: string | null
}

function App() {
  const [status, setStatus] = useState<AuthStatus | null>(null)

  const fetchStatus = () =>
    fetch('/mytracks/api/auth/status')
      .then((r) => r.json())
      .then(setStatus)

  useEffect(() => { fetchStatus() }, [])

  if (!status) return <p>Loading…</p>

  if (status.authenticated && status.user) {
    return <Profile onLogout={fetchStatus} role={status.user.role} />
  }

  return <Login googleClientId={status.googleClientId} onLogin={fetchStatus} />
}

export default App
