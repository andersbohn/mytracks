import { useRef, useState } from 'react'

export default function UploadPanel({ onUploaded }: { onUploaded: () => void }) {
  return (
    <div>
      <GpxUpload onUploaded={onUploaded} />
      <hr />
      <FitUpload onUploaded={onUploaded} />
    </div>
  )
}

function GpxUpload({ onUploaded }: { onUploaded: () => void }) {
  const fileRef = useRef<HTMLInputElement>(null)
  const [trackName, setTrackName] = useState('')
  const [activityType, setActivityType] = useState('')
  const [sourceId, setSourceId] = useState('')
  const [status, setStatus] = useState<'idle' | 'loading' | 'error'>('idle')
  const [error, setError] = useState<string | null>(null)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const file = fileRef.current?.files?.[0]
    if (!file) return

    const body = new FormData()
    body.append('file', file)
    if (trackName.trim()) body.append('trackName', trackName.trim())
    if (activityType.trim()) body.append('activityType', activityType.trim())
    if (sourceId.trim()) body.append('sourceId', sourceId.trim())

    setStatus('loading')
    setError(null)

    const res = await fetch('/mytracks/api/tracks/upload', { method: 'POST', body })
    if (res.ok) {
      setStatus('idle')
      setTrackName('')
      setActivityType('')
      setSourceId('')
      if (fileRef.current) fileRef.current.value = ''
      onUploaded()
    } else {
      setStatus('error')
      setError(`Upload failed (${res.status})`)
    }
  }

  return (
    <form onSubmit={submit}>
      <h2>Upload GPX</h2>
      <div>
        <input ref={fileRef} type="file" accept=".gpx" required />
      </div>
      <div>
        <input
          type="text"
          placeholder="Track name (auto-detected from GPX)"
          value={trackName}
          onChange={(e) => setTrackName(e.target.value)}
        />
      </div>
      <div>
        <input
          type="text"
          placeholder="Activity type (optional)"
          value={activityType}
          onChange={(e) => setActivityType(e.target.value)}
        />
      </div>
      <div>
        <input
          type="text"
          placeholder="Garmin activity ID (optional)"
          value={sourceId}
          onChange={(e) => setSourceId(e.target.value)}
        />
      </div>
      <button type="submit" disabled={status === 'loading'}>
        {status === 'loading' ? 'Uploading…' : 'Upload'}
      </button>
      {error && <p>{error}</p>}
    </form>
  )
}

function FitUpload({ onUploaded }: { onUploaded: () => void }) {
  const fileRef = useRef<HTMLInputElement>(null)
  const [activityId, setActivityId] = useState('')
  const [status, setStatus] = useState<'idle' | 'loading' | 'error'>('idle')
  const [error, setError] = useState<string | null>(null)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    const file = fileRef.current?.files?.[0]
    if (!file || !activityId.trim()) return

    const body = new FormData()
    body.append('file', file)

    setStatus('loading')
    setError(null)

    const res = await fetch(`/mytracks/api/tracks/fit/${encodeURIComponent(activityId.trim())}`, {
      method: 'PUT',
      body,
    })
    if (res.ok) {
      setStatus('idle')
      setActivityId('')
      if (fileRef.current) fileRef.current.value = ''
      onUploaded()
    } else {
      setStatus('error')
      setError(`Upload failed (${res.status})`)
    }
  }

  return (
    <form onSubmit={submit}>
      <h2>Upload Garmin FIT</h2>
      <div>
        <input ref={fileRef} type="file" accept=".fit" required />
      </div>
      <div>
        <input
          type="text"
          placeholder="Garmin activity ID"
          value={activityId}
          onChange={(e) => setActivityId(e.target.value)}
          required
        />
      </div>
      <button type="submit" disabled={status === 'loading' || !activityId.trim()}>
        {status === 'loading' ? 'Uploading…' : 'Upload'}
      </button>
      {error && <p>{error}</p>}
    </form>
  )
}
