import { useEffect, useState } from 'react'

type Track = {
  id: string
  trackName: string
  source: string
  sourceId: string | null
  activityType: string | null
  uploadTimestamp: string
  notes: string | null
}

export default function TrackList() {
  const [tracks, setTracks] = useState<Track[] | null>(null)

  useEffect(() => {
    fetch('/mytracks/api/tracks')
      .then((r) => r.json())
      .then(setTracks)
  }, [])

  if (!tracks) return <p>Loading tracks…</p>
  if (tracks.length === 0) return <p>No tracks yet.</p>

  return (
    <ul>
      {tracks.map((t) => (
        <li key={t.id}>
          <strong>{t.trackName}</strong>
          {t.activityType && <span> · {t.activityType}</span>}
          <span> · {new Date(t.uploadTimestamp).toLocaleDateString()}</span>
          {t.sourceId && <span> · <small>{t.source}:{t.sourceId}</small></span>}
        </li>
      ))}
    </ul>
  )
}
