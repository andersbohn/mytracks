import type { Track } from './TrackList'
import { fmtDate, fmtDist, fmtDuration, fmtAscent, fmtHR, fmtPower, fmtSpeed } from './format'

function Row({ label, value }: { label: string; value: string | number | null | undefined }) {
  if (value == null || value === '') return null
  return (
    <tr>
      <td>{label}</td>
      <td>{value}</td>
    </tr>
  )
}

export default function TrackDetail({ track, onClose }: { track: Track; onClose: () => void }) {
  return (
    <div>
      <button onClick={onClose}>Close</button>
      <table>
        <tbody>
          <Row label="Name" value={track.trackName} />
          <Row label="Sport" value={track.sport} />
          <Row label="Sub-sport" value={track.subSport} />
          <Row label="Date" value={fmtDate(track.startTime ?? track.uploadTimestamp)} />
          <Row label="Distance" value={fmtDist(track.distanceMeters)} />
          <Row label="Duration" value={fmtDuration(track.durationSeconds)} />
          <Row label="Moving time" value={fmtDuration(track.movingTimeSeconds)} />
          <Row label="Ascent" value={fmtAscent(track.ascentMeters)} />
          <Row label="Descent" value={fmtAscent(track.descentMeters)} />
          <Row label="Avg HR" value={fmtHR(track.avgHeartRate)} />
          <Row label="Max HR" value={fmtHR(track.maxHeartRate)} />
          <Row label="Avg speed" value={fmtSpeed(track.avgSpeedMs)} />
          <Row label="Max speed" value={fmtSpeed(track.maxSpeedMs)} />
          <Row label="Calories" value={track.calories ?? ''} />
          <Row label="Avg power" value={fmtPower(track.avgPowerWatts)} />
          <Row label="Norm power" value={fmtPower(track.normalizedPowerWatts)} />
          <Row label="Avg cadence" value={track.avgCadence != null ? track.avgCadence + ' rpm' : ''} />
          <Row label="Source" value={track.source} />
          <Row label="Source ID" value={track.sourceId} />
          <Row label="Uploaded" value={fmtDate(track.uploadTimestamp)} />
          <Row label="Notes" value={track.notes} />
        </tbody>
      </table>
    </div>
  )
}
