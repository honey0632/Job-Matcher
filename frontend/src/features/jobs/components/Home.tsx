import React, { useEffect, useState } from 'react'
import { Sparkles, ArrowRight, ArrowUpRight, Award, Compass, Star } from 'lucide-react'
import { User, Preferences, Match } from '../types'
import { COMPANIES, View } from '../../../app/constants'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Badge } from '../../../components/ui/Badge'
import { JobList } from './JobList'
import { JobSkeleton } from './JobSkeleton'
import { apiClient } from '../../../lib/api-client'

interface HomeProps {
  user: User
  preferences: Preferences
  savedCount: number
  onNavigate: (view: View) => void
  onSelectCompany: (companyId: string) => void
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
  onMatchesLoaded?: () => void
}

export function Home({
  user,
  preferences,
  savedCount,
  onNavigate,
  onSelectCompany,
  savedJobIds,
  onToggleSave,
  onMatchesLoaded,
}: HomeProps) {
  const [topMatches, setTopMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    apiClient.jobs.matches()
      .then(res => {
        setTopMatches(res.slice(0, 3))
        onMatchesLoaded?.()
      })
      .catch(() => setTopMatches([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-10">
      <div>
        <span className="text-xs font-bold tracking-wider text-zinc-400 uppercase">workspace</span>
        <h1 className="text-3xl font-extrabold tracking-tight mt-1">
          Welcome back, {user.displayName || user.email?.split('@')[0]} 👋
        </h1>
        <p className="text-sm text-zinc-400 mt-1">
          Your personalized career matching hub and job fetcher operations panel.
        </p>
      </div>

      {/* Hero Accent Card */}
      <div className="relative overflow-hidden rounded-2xl border border-white/[0.08] bg-gradient-to-br from-zinc-900 via-zinc-900/40 to-zinc-950 p-6 md:p-8 shadow-[inset_0_1px_0_0_rgba(255,255,255,0.04)]">
        <div className="absolute top-0 right-0 w-64 h-64 bg-zinc-100/[0.02] rounded-full blur-3xl pointer-events-none" />
        <div className="grid md:grid-template-cols-1 md:grid-cols-[1.5fr_1fr] gap-8 items-center">
          <div className="space-y-4">
            <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-zinc-100/5 text-zinc-300 text-xs font-semibold tracking-wide border border-white/[0.06]">
              <Sparkles className="w-3.5 h-3.5 text-zinc-300" />
              Next-Gen Job Matching
            </span>
            <h2 className="text-2xl font-bold text-zinc-100 tracking-tight leading-tight">
              Optimize your next job hunt with precision matching.
            </h2>
            <p className="text-sm text-zinc-400 leading-relaxed max-w-lg">
              Upload your resume and dynamically search top employer sitemaps, JSON endpoints, and index APIs. Highly aligned matches are surfaced instantly.
            </p>
            <div className="flex flex-wrap gap-2.5 pt-2">
              <Button onClick={() => onNavigate('search')}>
                Explore Jobs
                <ArrowRight className="w-4 h-4" />
              </Button>
              <Button variant="secondary" onClick={() => onNavigate('saved')}>
                Saved Roles
              </Button>
            </div>
          </div>

          <div className="rounded-xl border border-white/[0.06] bg-zinc-950/40 p-5 divide-y divide-white/[0.04] backdrop-blur shadow-inner">
            <div className="pb-3.5">
              <p className="text-[10px] font-mono text-zinc-500 uppercase tracking-wider">Desired Role</p>
              <strong className="text-base text-zinc-100 block mt-1 tracking-tight font-medium">
                {preferences.desiredRole || 'Not specified'}
              </strong>
            </div>
            <div className="py-3.5">
              <p className="text-[10px] font-mono text-zinc-500 uppercase tracking-wider">Geography</p>
              <strong className="text-base text-zinc-100 block mt-1 tracking-tight font-medium">
                {preferences.country || 'Global / Remote'}
              </strong>
            </div>
            <div className="pt-3.5">
              <p className="text-[10px] font-mono text-zinc-500 uppercase tracking-wider">Experience Threshold</p>
              <strong className="text-base text-zinc-100 block mt-1 tracking-tight font-medium font-mono">
                {preferences.experienceYears ? `${preferences.experienceYears} Years` : 'None specified'}
              </strong>
            </div>
          </div>
        </div>
      </div>

      {/* Grid of stats */}
      <div className="grid sm:grid-cols-3 gap-4">
        <Card className="stat-card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-medium text-zinc-400">Match Profile</h3>
            <Award className="w-4 h-4 text-zinc-500" />
          </div>
          <div>
            <div className="text-xl font-bold tracking-tight text-zinc-100">
              {preferences.desiredRole || 'Inactive'}
            </div>
            <p className="text-xs text-zinc-500 mt-1">
              {preferences.desiredRole ? `${preferences.country || 'Global'} · ${preferences.experienceYears} Years` : 'Setup search preferences'}
            </p>
          </div>
          <Button variant="secondary" size="sm" className="w-full text-xs" onClick={() => onNavigate('profile')}>
            Edit Profile
          </Button>
        </Card>

        <Card className="stat-card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-medium text-zinc-400">Bookmarked</h3>
            <Star className="w-4 h-4 text-zinc-500" />
          </div>
          <div>
            <div className="text-xl font-extrabold tracking-tight text-zinc-100 tabular-nums">
              {savedCount} Roles
            </div>
            <p className="text-xs text-zinc-500 mt-1">Preserved items in local cache & server</p>
          </div>
          <Button variant="secondary" size="sm" className="w-full text-xs" onClick={() => onNavigate('saved')}>
            View Bookmarks
          </Button>
        </Card>

        <Card className="stat-card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-medium text-zinc-400">Active Handlers</h3>
            <Compass className="w-4 h-4 text-zinc-500" />
          </div>
          <div>
            <div className="text-xl font-bold tracking-tight text-zinc-100">
              4 Providers
            </div>
            <p className="text-xs text-zinc-500 mt-1">18 company career channels available</p>
          </div>
          <Button variant="secondary" size="sm" className="w-full text-xs" onClick={() => onNavigate('search')}>
            Run Pipeline
          </Button>
        </Card>
      </div>

      {/* Target Focus Group */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-zinc-100 tracking-tight">Focus Channels</h2>
        <p className="text-sm text-zinc-400 leading-relaxed">
          Focusing your query triggers direct and immediate updates from that individual portal API, optimizing performance.
        </p>
        <div className="grid sm:grid-cols-4 gap-4">
          {COMPANIES.map(comp => (
            <button
              key={comp.id}
              onClick={() => onSelectCompany(comp.id)}
              className="flex flex-col items-start text-left p-5 rounded-xl border border-white/[0.08] bg-zinc-900/50 hover:bg-zinc-900/80 hover:border-white/[0.15] hover:shadow-card-hover transition-all duration-200 active:scale-[0.98] group"
            >
              <span className="text-2xl mb-4 group-hover:scale-110 transition-transform duration-150">{comp.icon}</span>
              <strong className="text-sm text-zinc-100 font-bold block mb-1">{comp.name}</strong>
              <span className="text-xs text-zinc-500 leading-relaxed mb-4">{comp.summary}</span>
              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-zinc-300 group-hover:text-white mt-auto pt-2">
                Open Channel
                <ArrowUpRight className="w-3.5 h-3.5" />
              </span>
            </button>
          ))}
        </div>
      </div>

      {/* Matches Overview */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-zinc-100 tracking-tight">Curated Top Recommendations</h2>
        <p className="text-sm text-zinc-400 leading-relaxed">
          The highest-aligned match recommendations retrieved based on semantic similarity to your parsed resume.
        </p>
        {loading ? (
          <div className="space-y-3">
            <JobSkeleton />
            <JobSkeleton />
          </div>
        ) : (
          <JobList
            jobs={topMatches}
            emptyMessage="No direct recommendation results available. Ensure your resume is processed and target role is saved."
            savedJobIds={savedJobIds}
            onToggleSave={onToggleSave}
          />
        )}
      </div>
    </div>
  )
}
