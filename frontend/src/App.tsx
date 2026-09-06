import { FormEvent, useEffect, useState } from 'react'
import { api, Match, Preferences, User } from './api'

type View = 'profile' | 'resume' | 'search' | 'matches'
const backendUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

const emptyPreferences: Preferences = {
  country: '',
  experienceYears: 0,
  desiredRole: '',
}

export default function App() {
  const [user, setUser] = useState<User | null>(null)
  const [view, setView] = useState<View>('profile')
  const [preferences, setPreferences] = useState(emptyPreferences)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    api.me().then(setUser).catch(() => setUser(null))
  }, [])

  useEffect(() => {
    if (!user) return
    api.getPreferences().then(value => value && setPreferences(value)).catch(() => undefined)
  }, [user])

  if (!user) {
    return (
      <main className="login-page">
        <section className="card login-card">
          <span className="eyebrow">JOB FETCHER</span>
          <h1>Find work that fits.</h1>
          <p>Sign in, upload your resume, and discover roles matched to your experience.</p>
          <a className="button primary" href={`${backendUrl}/oauth2/authorization/google`}>Continue with Google</a>
        </section>
      </main>
    )
  }

  async function action(work: () => Promise<unknown>, success: string) {
    setError('')
    setMessage('')
    try {
      await work()
      setMessage(success)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Something went wrong')
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <strong>Job Fetcher</strong>
        <span>{user.displayName || user.email}</span>
      </header>
      <div className="layout">
        <aside className="sidebar">
          <p className="eyebrow">WORKSPACE</p>
          {(['profile', 'resume', 'search', 'matches'] as View[]).map(item => (
            <button
              className={view === item ? 'nav-item active' : 'nav-item'}
              onClick={() => setView(item)}
              key={item}
            >
              {item === 'profile' ? 'Profile & preferences' : item === 'resume' ? 'Resume' : item === 'search' ? 'Search jobs' : 'My matches'}
            </button>
          ))}
        </aside>
        <main className="content">
          {message && <div className="notice success">{message}</div>}
          {error && <div className="notice error">{error}</div>}
          {view === 'profile' && (
            <Profile
              preferences={preferences}
              setPreferences={setPreferences}
              onSave={() => action(() => api.savePreferences(preferences), 'Preferences saved.')}
            />
          )}
          {view === 'resume' && (
            <Resume onUpload={file => action(() => api.uploadResume(file), 'Resume uploaded and processed.')} />
          )}
          {view === 'search' && <Search preferences={preferences} />}
          {view === 'matches' && <Matches />}
        </main>
      </div>
    </div>
  )
}

function Profile({
  preferences,
  setPreferences,
  onSave,
}: {
  preferences: Preferences
  setPreferences: (preferences: Preferences) => void
  onSave: () => void
}) {
  return (
    <section>
      <p className="eyebrow">PROFILE</p>
      <h1>Tell us what you want next.</h1>
      <p className="muted">These criteria drive the Google Careers search.</p>
      <form className="card form-grid" onSubmit={(event) => { event.preventDefault(); onSave() }}>
        <label>
          Country
          <input value={preferences.country} onChange={event => setPreferences({ ...preferences, country: event.target.value })} placeholder="e.g. India" required />
        </label>
        <label>
          Experience in years
          <input type="number" min="0" value={preferences.experienceYears} onChange={event => setPreferences({ ...preferences, experienceYears: Number(event.target.value) })} required />
        </label>
        <label>
          Desired role
          <input value={preferences.desiredRole} onChange={event => setPreferences({ ...preferences, desiredRole: event.target.value })} placeholder="e.g. Java Backend Engineer" required />
        </label>
        <button className="button primary" type="submit">Save preferences</button>
      </form>
    </section>
  )
}

function Resume({ onUpload }: { onUpload: (file: File) => void }) {
  const [file, setFile] = useState<File>()
  return (
    <section>
      <p className="eyebrow">RESUME</p>
      <h1>Put your experience to work.</h1>
      <p className="muted">Upload a PDF or DOCX resume. Text extraction runs automatically.</p>
      <div className="card upload-card">
        <input type="file" accept=".pdf,.docx" onChange={event => setFile(event.target.files?.[0])} />
        <button className="button primary" disabled={!file} onClick={() => file && onUpload(file)}>Upload resume</button>
      </div>
    </section>
  )
}

function Search({ preferences }: { preferences: Preferences }) {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    try {
      setMatches(await api.searchJobs(preferences))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section>
      <p className="eyebrow">DISCOVER</p>
      <h1>Search high-confidence matches.</h1>
      <p className="muted">Searches use your country, experience, and desired role. Only scores above 80% are shown.</p>
      <form className="searchbar" onSubmit={submit}>
        <button className="button primary">{loading ? 'Searching…' : 'Search jobs'}</button>
      </form>
      <JobList jobs={matches} empty="Run a search after uploading your resume." />
    </section>
  )
}

function Matches() {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.matches().then(setMatches).catch(() => setMatches([])).finally(() => setLoading(false))
  }, [])

  return (
    <section>
      <p className="eyebrow">RECOMMENDED</p>
      <h1>Your 80%+ matches.</h1>
      {loading ? <p className="muted">Loading matches…</p> : <JobList jobs={matches} empty="Upload a resume and run a search to see matches." />}
    </section>
  )
}

function JobList({ jobs, empty }: { jobs: Match[]; empty: string }) {
  if (!jobs.length) return <div className="card empty">{empty}</div>

  return (
    <div className="job-list">
      {jobs.map((job, index) => (
        <article className="card job" key={job.jobId ?? job.externalId ?? index}>
          <div>
            <h3>{job.title || 'Untitled role'}</h3>
            <p>{job.company || 'Company'} · {job.location || 'Location'}</p>
          </div>
          <div>
            <strong>{job.score}% match</strong>
            {job.jobUrl && <a href={job.jobUrl} target="_blank" rel="noreferrer">View role ↗</a>}
          </div>
        </article>
      ))}
    </div>
  )
}
