// Defines the main application views and user workflows.

import { FormEvent, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { api, Match, Preferences, User } from './api'

type View = 'home' | 'profile' | 'resume' | 'search' | 'matches' | 'saved'
const backendUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

const emptyPreferences: Preferences = {
  country: '',
  experienceYears: 0,
  desiredRole: '',
}

const COMPANIES = [
  // Temporarily disabled for testing. Re-enable this block only when we intentionally want
  // a cross-company / "all companies" match run.
  // { id: 'ALL', name: 'All Companies', icon: '🌐', summary: 'Everything' },
  { id: 'GOOGLE_CAREERS', name: 'Google Careers', icon: '🔵', summary: 'Search Google roles' },
  { id: 'AMAZON', name: 'Amazon Jobs', icon: '🟧', summary: 'Search Amazon roles' },
  { id: 'WELLS_FARGO', name: 'Wells Fargo', icon: '🔴', summary: 'Search Wells Fargo roles' },
  { id: 'NVIDIA', name: 'NVIDIA Jobs', icon: '🟢', summary: 'Search NVIDIA roles' },
] as const

const SAVED_JOBS_STORAGE_KEY = 'job-fetcher.saved-jobs.v1'

function readStoredSavedJobs(): Match[] {
  try {
    const stored = localStorage.getItem(SAVED_JOBS_STORAGE_KEY)
    if (!stored) return []
    const parsed = JSON.parse(stored) as Match[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function writeStoredSavedJobs(jobs: Match[]) {
  localStorage.setItem(SAVED_JOBS_STORAGE_KEY, JSON.stringify(jobs))
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
  const [view, setView] = useState<View>('home')
  const [preferences, setPreferences] = useState(emptyPreferences)
  const [savedJobs, setSavedJobs] = useState<Match[]>(() => readStoredSavedJobs())
  const [savedJobIds, setSavedJobIds] = useState<Set<number>>(() => new Set(readStoredSavedJobs().map(job => job.jobId).filter((id): id is number => typeof id === 'number')))
  const [selectedCompany, setSelectedCompany] = useState<string>('GOOGLE_CAREERS')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function syncSavedJobs(nextJobs: Match[]) {
    setSavedJobs(nextJobs)
    setSavedJobIds(new Set(nextJobs.map(job => job.jobId).filter((id): id is number => typeof id === 'number')))
    writeStoredSavedJobs(nextJobs)
  }

  useEffect(() => {
    api.me().then(setUser).catch(() => setUser(null))
  }, [])

  useEffect(() => {
    if (!user) return
    api.getPreferences().then(value => value && setPreferences(value)).catch(() => undefined)
    loadSavedJobs()
  }, [user])

  function loadSavedJobs() {
    api.getSavedJobs().then(jobs => {
      syncSavedJobs(jobs)
    }).catch(() => {
      const fallbackJobs = readStoredSavedJobs()
      if (fallbackJobs.length) {
        syncSavedJobs(fallbackJobs)
      }
    })
  }

  async function toggleSaveJob(job: Match) {
    if (!job.jobId) return
    try {
      const isSaved = savedJobIds.has(job.jobId)
      if (isSaved) {
        await api.unsaveJob(job.jobId)
        const nextJobs = savedJobs.filter(j => j.jobId !== job.jobId)
        syncSavedJobs(nextJobs)
        setMessage('Job removed from saved.')
      } else {
        const saved = await api.saveJob(job.jobId, job.score)
        const nextJobs = [saved, ...savedJobs.filter(j => j.jobId !== job.jobId)]
        syncSavedJobs(nextJobs)
        setMessage('Job saved successfully!')
      }
    } catch {
      setError('Failed to update saved job.')
    }
  }

  if (!user) {
    return (
      <div className="landing-page">
        <header className="public-navbar">
          <div className="public-nav-container">
            <div className="public-brand">
              <span className="brand-logo">💼</span>
              <strong>Job Matcher</strong>
            </div>
            <nav className="public-nav-links">
              <a href="#features">Features</a>
              <a href="#how-it-works">How It Works</a>
              <a href="#companies">Providers</a>
              <a href="/privacy-policy">Privacy Policy</a>
              <a href="/terms-of-service">Terms of Service</a>
            </nav>
            <a className="button primary" href={`${backendUrl}/oauth2/authorization/google`}>
              Sign in with Google
            </a>
          </div>
        </header>

        <main className="landing-content">
          <section className="hero-section">
            <span className="eyebrow">AI-POWERED CAREER DISCOVERY</span>
            <h1>Discover & Match Jobs That Fit Your Resume</h1>
            <p className="hero-subtitle">
              Job Matcher analyzes your resume and automatically evaluates live openings from top employers like Google, Amazon, Wells Fargo, and NVIDIA. High-match roles (&gt;80%) are automatically bookmarked for you.
            </p>
            <div className="hero-ctas">
              <a className="button primary hero-btn" href={`${backendUrl}/oauth2/authorization/google`}>
                Continue with Google
              </a>
              <a className="button secondary hero-btn" href="#features">
                Learn More ↓
              </a>
            </div>
          </section>

          <section id="companies" className="landing-section">
            <p className="eyebrow">SUPPORTED EMPLOYERS</p>
            <h2>Search Across Top Tech Employers</h2>
            <p className="muted">Direct integration with live job portals for real-time opportunity discovery.</p>
            <div className="landing-company-grid">
              <div className="landing-company-card">
                <span className="company-icon">🔵</span>
                <h3>Google Careers</h3>
                <p>Global engineering, cloud, AI, and product management opportunities.</p>
              </div>
              <div className="landing-company-card">
                <span className="company-icon">🟧</span>
                <h3>Amazon Jobs</h3>
                <p>Software development, AWS infrastructure, operations, and solutions roles.</p>
              </div>
              <div className="landing-company-card">
                <span className="company-icon">🔴</span>
                <h3>Wells Fargo</h3>
                <p>Financial technology, cybersecurity, software engineering, and data analytics.</p>
              </div>
              <div className="landing-company-card">
                <span className="company-icon">🟢</span>
                <h3>NVIDIA Jobs</h3>
                <p>AI, deep learning, GPU architecture, hardware, and software platforms.</p>
              </div>
            </div>
          </section>

          <section id="features" className="landing-section alt-bg">
            <p className="eyebrow">CORE FEATURES</p>
            <h2>Everything You Need for Targeted Job Discovery</h2>
            <div className="features-grid">
              <div className="feature-card">
                <div className="feature-icon">📄</div>
                <h3>AI Resume Parsing</h3>
                <p>Upload your PDF or DOCX resume. Our parser extracts your key skills, technical stack, and target roles automatically.</p>
              </div>
              <div className="feature-card">
                <div className="feature-icon">🎯</div>
                <h3>Deep Match Scoring</h3>
                <p>Using Google Gemini AI, your background is scored against real-time job requirements for precise semantic relevancy.</p>
              </div>
              <div className="feature-card">
                <div className="feature-icon">⭐</div>
                <h3>Auto-Save Top Matches</h3>
                <p>Any job matching your resume with a score higher than 80% is automatically bookmarked to your profile for easy tracking.</p>
              </div>
              <div className="feature-card">
                <div className="feature-icon">🌍</div>
                <h3>Country & Experience Filtering</h3>
                <p>Filter opportunities by target country and experience level so you only focus on relevant positions.</p>
              </div>
            </div>
          </section>

          <section id="how-it-works" className="landing-section">
            <p className="eyebrow">HOW IT WORKS</p>
            <h2>Get Started in 3 Simple Steps</h2>
            <div className="steps-grid">
              <div className="step-card">
                <span className="step-number">1</span>
                <h3>Sign in with Google</h3>
                <p>Fast, secure login using your existing Google account. No extra passwords needed.</p>
              </div>
              <div className="step-card">
                <span className="step-number">2</span>
                <h3>Upload Resume & Preferences</h3>
                <p>Provide your desired role, country, and upload your resume for instant AI analysis.</p>
              </div>
              <div className="step-card">
                <span className="step-number">3</span>
                <h3>Review & Apply</h3>
                <p>Explore AI-scored matches, view auto-saved top picks (&gt;80%), and jump directly to official application portals.</p>
              </div>
            </div>
          </section>

          <section className="landing-cta-banner">
            <h2>Ready to find your next role?</h2>
            <p>Sign in now with Google and discover matched job opportunities in seconds.</p>
            <a className="button primary hero-btn" href={`${backendUrl}/oauth2/authorization/google`}>
              Get Started with Google
            </a>
          </section>
        </main>

        <footer className="public-footer">
          <div className="public-footer-container">
            <div className="footer-info">
              <strong>💼 Job Matcher</strong>
              <p>AI-powered career matching & job discovery platform.</p>
            </div>
            <LegalLinks />
            <p className="copyright">© 2026 Job Matcher. All rights reserved.</p>
          </div>
        </footer>
      </div>
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
        <strong>Job Matcher</strong>
        <span>{user.displayName || user.email}</span>
      </header>
      <div className="layout">
        <aside className="sidebar">
          <p className="eyebrow">WORKSPACE</p>
          {[
            { id: 'home', label: '🏠 Dashboard' },
            { id: 'search', label: '🔍 Search Jobs' },
            { id: 'matches', label: '🎯 Recommended Matches' },
            { id: 'saved', label: `⭐ Saved Jobs (${savedJobs.length})` },
            { id: 'profile', label: '⚙️ Profile & Preferences' },
            { id: 'resume', label: '📄 Resume' },
          ].map(item => (
            <button
              className={view === item.id ? 'nav-item active' : 'nav-item'}
              onClick={() => setView(item.id as View)}
              key={item.id}
            >
              {item.label}
            </button>
          ))}
        </aside>
        <main className="content">
          {message && <div className="notice success">{message}</div>}
          {error && <div className="notice error">{error}</div>}

          {view === 'home' && (
            <Home
              user={user}
              preferences={preferences}
              savedCount={savedJobs.length}
              onNavigate={(v) => setView(v)}
              onSelectCompany={(companyId) => {
                setSelectedCompany(companyId)
                setView('search')
              }}
              savedJobIds={savedJobIds}
              onToggleSave={toggleSaveJob}
              onMatchesLoaded={loadSavedJobs}
            />
          )}

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

          {view === 'search' && (
            <Search
              preferences={preferences}
              selectedCompany={selectedCompany}
              setSelectedCompany={setSelectedCompany}
              savedJobIds={savedJobIds}
              onToggleSave={toggleSaveJob}
              onMatchesLoaded={loadSavedJobs}
            />
          )}

          {view === 'matches' && (
            <Matches
              savedJobIds={savedJobIds}
              onToggleSave={toggleSaveJob}
              onMatchesLoaded={loadSavedJobs}
            />
          )}

          {view === 'saved' && (
            <SavedJobs
              jobs={savedJobs}
              savedJobIds={savedJobIds}
              onToggleSave={toggleSaveJob}
            />
          )}
        </main>
      </div>
    </div>
  )
}

function Home({
  user,
  preferences,
  savedCount,
  onNavigate,
  onSelectCompany,
  savedJobIds,
  onToggleSave,
  onMatchesLoaded,
}: {
  user: User
  preferences: Preferences
  savedCount: number
  onNavigate: (view: View) => void
  onSelectCompany: (companyId: string) => void
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
  onMatchesLoaded?: () => void
}) {
  const [topMatches, setTopMatches] = useState<Match[]>([])
  const [loadingMatches, setLoadingMatches] = useState(false)

  useEffect(() => {
    setLoadingMatches(true)
    api.matches()
      .then(res => {
        setTopMatches(res.slice(0, 5))
        onMatchesLoaded?.()
      })
      .catch(() => setTopMatches([]))
      .finally(() => setLoadingMatches(false))
  }, [])

  return (
    <section>
      <p className="eyebrow">DASHBOARD</p>
      <h1>Welcome back, {user.displayName || user.email?.split('@')[0]}! 👋</h1>
      <p className="muted">Your personalized job fetcher & AI matching center.</p>

      <section className="homepage-hero card">
        <div>
          <p className="eyebrow">JOB MATCHER</p>
          <h1>Find your next role faster.</h1>
          <p className="muted">Track the right employers, keep matching against your resume, and save only the opportunities worth revisiting.</p>
          <div className="quick-actions">
            <button className="button primary" onClick={() => onNavigate('search')}>Search jobs</button>
            <button className="button secondary" onClick={() => onNavigate('saved')}>Saved roles ({savedCount})</button>
            <button className="button secondary" onClick={() => onNavigate('profile')}>Update profile</button>
          </div>
        </div>
        <div className="hero-summary">
          <div>
            <span className="label">Role</span>
            <strong>{preferences.desiredRole || 'Not set yet'}</strong>
          </div>
          <div>
            <span className="label">Location</span>
            <strong>{preferences.country || 'Choose a country'}</strong>
          </div>
          <div>
            <span className="label">Experience</span>
            <strong>{preferences.experienceYears ? `${preferences.experienceYears} years` : 'Add experience'}</strong>
          </div>
        </div>
      </section>

      <div className="dashboard-grid">
        <div className="card stat-card">
          <h3>Target Role</h3>
          <div className="stat-value">{preferences.desiredRole || 'Not Set'}</div>
          <div className="stat-sub">{preferences.country ? `${preferences.country} · ${preferences.experienceYears} yrs exp` : 'Update profile preferences'}</div>
          <button className="button secondary" onClick={() => onNavigate('profile')}>Edit Profile</button>
        </div>

        <div className="card stat-card">
          <h3>Saved Jobs</h3>
          <div className="stat-value">{savedCount} Roles</div>
          <div className="stat-sub">Jobs you have bookmarked</div>
          <button className="button secondary" onClick={() => onNavigate('saved')}>View Saved</button>
        </div>

        <div className="card stat-card">
          <h3>Approved Sources</h3>
          <div className="stat-value">4 Companies</div>
          <div className="stat-sub">Google, Amazon, Wells Fargo, NVIDIA</div>
          <button className="button secondary" onClick={() => onNavigate('search')}>Search Jobs</button>
        </div>
      </div>

      <div className="company-section">
        <h2>Company Focus Panel</h2>
        <p className="muted">Open one company and only that source will run a match. Save the roles you want to revisit later.</p>
        <div className="company-grid">
          {COMPANIES.map(comp => (
            <div key={comp.id} className="company-card">
              <button type="button" className="company-card-button" onClick={() => onSelectCompany(comp.id)}>
                <span className="company-icon">{comp.icon}</span>
                <span className="company-name">{comp.name}</span>
                <span className="company-summary">{comp.summary}</span>
                <span className="button primary company-trigger">Search {comp.name.split(' ')[0]}</span>
              </button>
            </div>
          ))}
        </div>
      </div>

      <div style={{ marginTop: '36px' }}>
        <h2>Top Recommended Roles</h2>
        <p className="muted">Highest matching job opportunities based on your uploaded resume.</p>
        {loadingMatches ? (
          <p className="muted">Finding top matches for you…</p>
        ) : (
          <JobList
            jobs={topMatches}
            empty="No recommended matches yet. Please make sure your resume is uploaded and preferences are set."
            savedJobIds={savedJobIds}
            onToggleSave={onToggleSave}
          />
        )}
      </div>
    </section>
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
      <p className="muted">These criteria drive job search & AI matching across all approved sources.</p>
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
          <input value={preferences.desiredRole} onChange={event => setPreferences({ ...preferences, desiredRole: event.target.value })} placeholder="e.g. Software Engineer" required />
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

function Search({
  preferences,
  selectedCompany,
  setSelectedCompany,
  savedJobIds,
  onToggleSave,
  onMatchesLoaded,
}: {
  preferences: Preferences
  selectedCompany: string
  setSelectedCompany: (company: string) => void
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
  onMatchesLoaded?: () => void
}) {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(false)

  async function submit(event?: FormEvent) {
    if (event) event.preventDefault()
    setLoading(true)
    try {
      const criteria: Preferences = {
        ...preferences,
        source: selectedCompany,
      }
      const results = await api.searchJobs(criteria)
      setMatches(results)
      onMatchesLoaded?.()
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // The "All Companies" mode is intentionally disabled for now. We only run single-company
    // search passes while confirming providers such as Amazon, Wells Fargo, and NVIDIA.
    void submit()
  }, [selectedCompany])

  const selectedCompObj = COMPANIES.find(c => c.id === selectedCompany) || COMPANIES[0]

  return (
    <section>
      <p className="eyebrow">DISCOVER</p>
      <h1>Search & Match Jobs</h1>
      <p className="muted">Choose one company at a time. Only the open company triggers its search and match pass.</p>

      <div className="search-layout">
        <aside className="company-side-panel">
          <h4>Company Panel</h4>
          {COMPANIES.map(comp => (
            <button
              key={comp.id}
              className={selectedCompany === comp.id ? 'company-btn active' : 'company-btn'}
              onClick={() => setSelectedCompany(comp.id)}
            >
              <span>{comp.icon}</span>
              <span>{comp.name}</span>
            </button>
          ))}
        </aside>

        <div>
          <form className="searchbar" onSubmit={submit}>
            <button className="button primary" style={{ width: '100%' }}>
              {loading ? `Matching ${selectedCompObj.name}…` : `Search & Match ${selectedCompObj.name}`}
            </button>
          </form>
          <div className="company-status">
            <span className="badge-company">Current source</span>
            <strong>{selectedCompObj.name}</strong>
          </div>
          <JobList
            jobs={matches}
            empty={selectedCompany === 'ALL' ? 'Select a company to start a focused match run.' : `No roles matched for ${selectedCompObj.name} yet. Try another company or update your profile.`}
            savedJobIds={savedJobIds}
            onToggleSave={onToggleSave}
          />
        </div>
      </div>
    </section>
  )
}

function Matches({
  savedJobIds,
  onToggleSave,
  onMatchesLoaded,
}: {
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
  onMatchesLoaded?: () => void
}) {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.matches()
      .then(res => {
        setMatches(res)
        onMatchesLoaded?.()
      })
      .catch(() => setMatches([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section>
      <p className="eyebrow">RECOMMENDED</p>
      <h1>Your matches (60%+).</h1>
      {loading ? <p className="muted">Loading matches…</p> : (
        <JobList
          jobs={matches}
          empty="Upload a resume and run a search to see matches."
          savedJobIds={savedJobIds}
          onToggleSave={onToggleSave}
        />
      )}
    </section>
  )
}

function SavedJobs({
  jobs,
  savedJobIds,
  onToggleSave,
}: {
  jobs: Match[]
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
}) {
  return (
    <section>
      <p className="eyebrow">BOOKMARKS</p>
      <h1>Saved Jobs ⭐</h1>
      <p className="muted">All jobs you have bookmarked for quick reference. They will never disappear.</p>
      <JobList
        jobs={jobs}
        empty="You haven't saved any jobs yet. Click 'Save Job' on any job card to bookmark it."
        savedJobIds={savedJobIds}
        onToggleSave={onToggleSave}
      />
    </section>
  )
}

function JobList({
  jobs,
  empty,
  savedJobIds,
  onToggleSave,
}: {
  jobs: Match[]
  empty: string
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
}) {
  const [descriptionJobId, setDescriptionJobId] = useState<number | string>()

  if (!jobs.length) return <div className="card empty">{empty}</div>

  return (
    <div className="job-list">
      {jobs.map((job, index) => {
        const jobId = job.jobId ?? job.externalId
        const isSaved = job.jobId ? savedJobIds.has(job.jobId) : false
        return (
          <article className="card job" key={jobId ?? index}>
            <div>
              <h3>{job.title || 'Untitled role'}</h3>
              <p>
                <strong>{job.company || 'Company'}</strong> · {job.location || 'Location'}
                {job.source && <span className="badge-company" style={{ marginLeft: '8px' }}>{job.source}</span>}
              </p>
            </div>
            <div className="job-actions">
              {job.score !== undefined && <strong>{job.score}% match</strong>}
              <div className="job-buttons">
                {job.jobId && (
                  <button
                    className={isSaved ? 'button saved-btn active' : 'button saved-btn'}
                    type="button"
                    onClick={() => onToggleSave(job)}
                  >
                    {isSaved ? 'Saved ⭐' : 'Save Job ☆'}
                  </button>
                )}
                <button
                  className="button secondary"
                  type="button"
                  onClick={() => setDescriptionJobId(
                    descriptionJobId === jobId ? undefined : jobId
                  )}
                >
                  Job Description
                </button>
                {job.jobUrl && (
                  <>
                    <a className="button secondary" href={job.jobUrl} target="_blank" rel="noreferrer">
                      Portal ↗
                    </a>
                    <a className="button primary" href={job.jobUrl} target="_blank" rel="noreferrer">
                      Apply ↗
                    </a>
                  </>
                )}
              </div>
              {descriptionJobId === jobId && (
                <p className="job-description">
                  {job.description || 'No job description is available.'}
                </p>
              )}
            </div>
          </article>
        )
      })}
    </div>
  )
}
