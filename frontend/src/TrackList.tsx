import { useEffect, useRef, useState } from 'react'
import TrackDetail from './TrackDetail'
import { fmtDate, fmtDist, fmtDuration, fmtAscent, fmtHR } from './format'

export type Track = {
  id: string
  trackName: string
  source: string
  sourceId: string | null
  activityType: string | null
  uploadTimestamp: string
  notes: string | null
  startTime: string | null
  durationSeconds: number | null
  movingTimeSeconds: number | null
  distanceMeters: number | null
  ascentMeters: number | null
  descentMeters: number | null
  avgHeartRate: number | null
  maxHeartRate: number | null
  avgSpeedMs: number | null
  maxSpeedMs: number | null
  calories: number | null
  avgPowerWatts: number | null
  normalizedPowerWatts: number | null
  avgCadence: number | null
  sport: string | null
  subSport: string | null
}

type TrackPage = {
  content: Track[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

const PAGE_SIZE = 50

export default function TrackList() {
  const [trackPage, setTrackPage] = useState<TrackPage | null>(null)
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const topRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setTrackPage(null)
    fetch(`/mytracks/api/tracks?page=${page}&size=${PAGE_SIZE}`)
      .then((r) => r.json())
      .then(setTrackPage)
  }, [page])

  const goTo = (p: number) => {
    setPage(p)
    setSelectedId(null)
    topRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  if (!trackPage) return <p>Loading tracks…</p>
  if (trackPage.totalElements === 0) return <p>No tracks yet.</p>

  const selected = trackPage.content.find((t) => t.id === selectedId) ?? null

  return (
    <div ref={topRef}>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Name</th>
            <th>Sport</th>
            <th>Distance</th>
            <th>Duration</th>
            <th>Ascent</th>
            <th>Avg HR</th>
            <th>Calories</th>
            <th>Activity</th>
          </tr>
        </thead>
        <tbody>
          {trackPage.content.map((t) => (
            <tr
              key={t.id}
              onClick={() => setSelectedId(t.id === selectedId ? null : t.id)}
              style={{ cursor: 'pointer', fontWeight: t.id === selectedId ? 'bold' : undefined }}
            >
              <td>{fmtDate(t.startTime ?? t.uploadTimestamp)}</td>
              <td>{t.trackName}</td>
              <td>{t.sport ?? t.activityType ?? ''}</td>
              <td>{fmtDist(t.distanceMeters)}</td>
              <td>{fmtDuration(t.durationSeconds)}</td>
              <td>{fmtAscent(t.ascentMeters)}</td>
              <td>{fmtHR(t.avgHeartRate)}</td>
              <td>{t.calories ?? ''}</td>
              <td onClick={(e) => e.stopPropagation()}>
                {t.sourceId && t.source === 'fit-upload' ? (
                  <a
                    href={`https://connect.garmin.com/app/activity/${t.sourceId}`}
                    target="_blank"
                    rel="noreferrer"
                  >
                    {t.sourceId}
                  </a>
                ) : (
                  t.sourceId ?? ''
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selected && <TrackDetail track={selected} onClose={() => setSelectedId(null)} />}

      <div>
        <button onClick={() => goTo(page - 1)} disabled={page === 0}>
          Prev
        </button>
        <span>
          {' '}
          Page {page + 1} of {trackPage.totalPages} · {trackPage.totalElements} tracks{' '}
        </span>
        <button onClick={() => goTo(page + 1)} disabled={page >= trackPage.totalPages - 1}>
          Next
        </button>
      </div>
    </div>
  )
}
