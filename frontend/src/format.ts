export function fmtDate(iso: string | null | undefined): string {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString()
}

export function fmtDist(meters: number | null | undefined): string {
  if (meters == null) return ''
  return (meters / 1000).toFixed(1) + ' km'
}

export function fmtDuration(seconds: number | null | undefined): string {
  if (seconds == null) return ''
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  return `${m}:${String(s).padStart(2, '0')}`
}

export function fmtAscent(meters: number | null | undefined): string {
  if (meters == null) return ''
  return Math.round(meters) + ' m'
}

export function fmtHR(bpm: number | null | undefined): string {
  if (bpm == null) return ''
  return bpm + ' bpm'
}

export function fmtPower(watts: number | null | undefined): string {
  if (watts == null) return ''
  return watts + ' W'
}

export function fmtSpeed(mps: number | null | undefined): string {
  if (mps == null) return ''
  return (mps * 3.6).toFixed(1) + ' km/h'
}
