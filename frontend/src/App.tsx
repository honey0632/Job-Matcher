import { useEffect, useState } from 'react'
import { User, Match, Preferences } from './features/jobs/types'
import { COMPANIES, View } from './app/constants'
import { useAuth } from './features/auth/hooks/useAuth'
import { usePreferences } from './features/profile/hooks/usePreferences'
import { useSavedJobs } from './features/jobs/hooks/useSavedJobs'
import { Home } from './features/jobs/components/Home'
import { Search } from './features/jobs/components/Search'
import { Matches } from './features/jobs/components/Matches'
import { SavedJobs } from './features/jobs/components/SavedJobs'
import { Profile } from './features/profile/components/Profile'
import { Resume } from './features/resume/components/Resume'
import { PrivacyPolicy, TermsOfService } from './app/Legal'
import { LandingPage } from './features/auth/components/LandingPage'
import { AuthModal } from './features/auth/components/AuthModal'
import './styles.css'

export default function App() {
  const path = window.location.pathname.replace(/\/+$/, '') || '/'
  if (path === '/privacy-policy') return <PrivacyPolicy />
  if (path === '/terms-of-service') return <TermsOfService />

  const { user, loading: authLoading, error: authError } = useAuth()
  const { preferences, setPreferences, savePreferences, saving } = usePreferences()
  const { savedJobs, savedJobIds, toggleSave } = useSavedJobs()

  const [view, setView] = useState<View>('home')
  const [selectedCompany, setSelectedCompany] = useState<string>('GOOGLE_CAREERS')

  if (authLoading) return <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-zinc-500">Loading...</div>
  if (!user) return <LandingPage />

  return (
    <div className="min-h-screen bg-zinc-950 flex text-zinc-100">
      <aside className="w-64 border-r border-white/[0.06] p-6 space-y-8">
        <div className="text-sm font-bold tracking-tight">Job Fetcher</div>
        <nav className="space-y-1">
          {[
            { id: 'home', label: '🏠 Dashboard' },
            { id: 'search', label: '🔍 Search Jobs' },
            { id: 'matches', label: '🎯 Recommendations' },
            { id: 'saved', label: `⭐ Saved Jobs (${savedJobs.length})` },
            { id: 'profile', label: '⚙️ Settings' },
            { id: 'resume', label: '📄 Resume' },
          ].map(item => (
            <button
              key={item.id}
              className={`w-full text-left px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                view === item.id
                  ? 'bg-zinc-800 text-white'
                  : 'text-zinc-400 hover:text-white hover:bg-zinc-900'
              }`}
              onClick={() => setView(item.id as View)}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>

      <main className="flex-1 p-8 md:p-12">
        {view === 'home' && (
          <Home
            user={user}
            preferences={preferences}
            savedCount={savedJobs.length}
            onNavigate={setView}
            onSelectCompany={(cid) => { setSelectedCompany(cid); setView('search') }}
            savedJobIds={savedJobIds}
            onToggleSave={toggleSave}
          />
        )}
        {view === 'search' && (
          <Search
            preferences={preferences}
            selectedCompany={selectedCompany}
            setSelectedCompany={setSelectedCompany}
            savedJobIds={savedJobIds}
            onToggleSave={toggleSave}
          />
        )}
        {view === 'matches' && <Matches savedJobIds={savedJobIds} onToggleSave={toggleSave} />}
        {view === 'saved' && <SavedJobs jobs={savedJobs} savedJobIds={savedJobIds} onToggleSave={toggleSave} />}
        {view === 'profile' && <Profile preferences={preferences} setPreferences={setPreferences} onSave={savePreferences} saving={saving} />}
        {view === 'resume' && <Resume />}
      </main>
    </div>
  )
}
