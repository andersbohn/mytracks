import { useEffect, useRef } from 'react'

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (cfg: object) => void
          renderButton: (el: HTMLElement, opts: object) => void
        }
      }
    }
  }
}

type Props = { googleClientId: string | null; onLogin: () => void }

const isRealClientId = (id: string | null) =>
  id?.includes('.apps.googleusercontent.com') ?? false

export default function Login({ googleClientId, onLogin }: Props) {
  const buttonRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!isRealClientId(googleClientId) || !buttonRef.current) return

    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.onload = () => {
      window.google?.accounts.id.initialize({
        client_id: googleClientId,
        callback: async ({ credential }: { credential: string }) => {
          await fetch('/mytracks/api/auth/google', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ credential }),
          })
          onLogin()
        },
      })
      if (buttonRef.current) {
        window.google?.accounts.id.renderButton(buttonRef.current, {
          theme: 'outline',
          size: 'large',
        })
      }
    }
    document.body.appendChild(script)
    return () => { document.body.removeChild(script) }
  }, [googleClientId, onLogin])

  if (!isRealClientId(googleClientId)) {
    return (
      <div>
        <h1>mytracks</h1>
        <p>Local dev — mock auth active.</p>
      </div>
    )
  }

  return (
    <div>
      <h1>mytracks</h1>
      <p>Sign in to continue</p>
      <div ref={buttonRef} />
    </div>
  )
}
