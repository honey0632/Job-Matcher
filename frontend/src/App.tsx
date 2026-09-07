import { FormEvent, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { api, Match, Preferences, User } from './api'

type View = 'profile' | 'resume' | 'search' | 'matches'
const backendUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

const emptyPreferences: Preferences = {
  country: '',
  experienceYears: 0,
  desiredRole: '',
}

function LegalLinks() {
  return (
    <nav className="legal-links" aria-label="Legal">
      <a href="/privacy-policy">Privacy Policy</a>
      <a href="/terms-of-service">Terms of Service</a>
    </nav>
  )
}

function LegalPage({ title, eyebrow, children }: { title: string; eyebrow: string; children: ReactNode }) {
  return (
    <main className="legal-page">
      <article className="legal-card">
        <a className="back-link" href="/">← Back to Job Matcher</a>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="legal-updated">Last updated: September 6, 2026</p>
        {children}
        <LegalLinks />
      </article>
    </main>
  )
}

function PrivacyPolicy() {
  return (
    <LegalPage title="Privacy Policy" eyebrow="JOB MATCHER">
      <p>Job Matcher helps users discover job opportunities based on their preferences and uploaded resume. This policy explains what information we collect and how we use it.</p>
      <h2>Information we collect</h2>
      <ul>
        <li>Google account information provided through Google OAuth, such as your name, email address, and provider identifier.</li>
        <li>Search preferences, including country, experience, and desired role.</li>
        <li>Resume files and extracted resume text that you choose to upload.</li>
        <li>Job search results and match scores generated for your account.</li>
        <li>Basic technical information needed to operate secure sessions and protect the service.</li>
      </ul>
      <h2>How we use information</h2>
      <p>We use this information to authenticate you, store your profile and preferences, extract resume text, search job listings, calculate job-match scores, and maintain application security. We do not sell your personal information.</p>
      <h2>Google OAuth</h2>
      <p>Sign-in is provided by Google. Job Matcher receives only the account information authorized by Google for this application. We do not receive or store your Google password.</p>
      <h2>Storage and retention</h2>
      <p>Your account data and resume remain stored while your account is active or as needed to provide the service. Contact us to request deletion or correction of your data.</p>
      <h2>Third parties</h2>
      <p>We use Google for authentication and Google Careers data for job discovery. Job links may take you to third-party websites whose privacy practices are governed by their own policies.</p>
      <h2>Contact</h2>
      <p>For privacy questions or deletion requests, contact the Job Matcher administrator through the contact method published on the application website.</p>
    </LegalPage>
  )
}

function TermsOfService() {
  return (
    <LegalPage title="Terms of Service" eyebrow="JOB MATCHER">
      <p>By using Job Matcher, you agree to these terms. If you do not agree, do not use the service.</p>
      <h2>Service description</h2>
      <p>Job Matcher provides job discovery, resume text extraction, and automated matching based on information you provide. Match scores are estimates and are not guarantees of employment, interviews, or recruiter interest.</p>
      <h2>Your responsibilities</h2>
      <ul>
        <li>Provide accurate information and upload only resumes you are authorized to use.</li>
        <li>Keep your Google account secure and do not share access to your session.</li>
        <li>Do not misuse the service, attempt unauthorized access, or upload malicious content.</li>
        <li>Review job details independently before applying or sharing information with an employer.</li>
      </ul>
      <h2>Third-party job listings</h2>
      <p>Job listings and links may come from Google Careers or other third parties. We do not guarantee their accuracy, availability, compensation, or hiring outcome.</p>
      <h2>Intellectual property</h2>
      <p>You retain rights to your uploaded resume. Job Matcher and its software, branding, and service design remain the property of their respective owners.</p>
      <h2>Availability and changes</h2>
      <p>The service is provided on an availability basis and may change or be discontinued. We may update these terms when the service changes.</p>
      <h2>Contact</h2>
      <p>For questions about these terms, contact the Job Matcher administrator through the contact method published on the application website.</p>
    </LegalPage>
  )
}

export default function App() {
  const path = window.location.pathname.replace(/\/+$/, '') || '/'

  if (path === '/privacy-policy') return <PrivacyPolicy />
  if (path === '/terms-of-service') return <TermsOfService />

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
          <LegalLinks />
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
      <h1>Search job matches.</h1>
      <p className="muted">Searches use your country, experience, and desired role. Only scores above 20% are shown.</p>
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
      <h1>Your 20%+ matches.</h1>
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
