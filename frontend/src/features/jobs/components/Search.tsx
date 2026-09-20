import React, { useState } from 'react'
import { ArrowRight, Inbox, Search as SearchIcon } from 'lucide-react'
import { Match, Preferences } from '../types'
import { COMPANIES } from '../../../app/constants'
import { Button } from '../../../components/ui/Button'
import { JobList } from './JobList'
import { JobSkeleton } from './JobSkeleton'
import { apiClient } from '../../../lib/api-client'

interface SearchProps {
  preferences: Preferences
  selectedCompany: string
  setSelectedCompany: (company: string) => void
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
}

export function Search({
  preferences,
  selectedCompany,
  setSelectedCompany,
  savedJobIds,
  onToggleSave,
}: SearchProps) {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function performSearch() {
    setLoading(true)
    setError(null)
    try {
      const criteria: Preferences = {
        ...preferences,
        source: selectedCompany,
      }
      const results = await apiClient.jobs.search(criteria)
      setMatches(results)
    } catch (err) {
      setMatches([])
      setError(err instanceof Error ? err.message : 'Unable to fetch jobs from this company')
    } finally {
      setLoading(false)
    }
  }

  const selectedCompObj = COMPANIES.find(c => c.id === selectedCompany) || COMPANIES[0]

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Search & Match</h1>
        <p className="text-sm text-zinc-400 mt-2">
          Target a single provider channel to run an focused, optimized matching pipeline.
        </p>
      </div>

      <div className="grid grid-cols-[240px_1fr] gap-8">
        <aside className="space-y-6">
          <div className="rounded-xl border border-white/[0.08] bg-zinc-900/60 p-4 space-y-2">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-zinc-500 mb-4 px-2">Provider Channels</h4>
            {COMPANIES.map(comp => (
              <button
                key={comp.id}
                onClick={() => setSelectedCompany(comp.id)}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 ${
                  selectedCompany === comp.id
                    ? 'bg-zinc-800 text-white shadow-[inset_0_1px_0_0_rgba(255,255,255,0.06)]'
                    : 'text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-200'
                }`}
              >
                <span className="text-lg">{comp.icon}</span>
                {comp.name}
              </button>
            ))}
          </div>
        </aside>

        <main className="space-y-6">
          <div className="flex items-center gap-4 p-4 rounded-xl border border-white/[0.08] bg-zinc-900/40">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-zinc-800 border border-white/[0.06] flex items-center justify-center text-xl">
                {selectedCompObj.icon}
              </div>
              <div>
                <p className="text-[10px] font-mono text-zinc-500 uppercase tracking-wider">Active Channel</p>
                <strong className="text-sm font-semibold text-zinc-100">{selectedCompObj.name}</strong>
              </div>
            </div>
            <Button
              className="ml-auto"
              disabled={loading}
              onClick={performSearch}
            >
              {loading ? (
                <>Matching...</>
              ) : (
                <>
                  <SearchIcon className="w-4 h-4 mr-2" />
                  Run Matching Pipeline
                </>
              )}
            </Button>
          </div>

          {error && (
            <div className="rounded-xl border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-300">
              {error}
            </div>
          )}

          {loading ? (
            <div className="space-y-3">
              <JobSkeleton />
              <JobSkeleton />
            </div>
          ) : (
            <JobList
              jobs={matches}
              emptyMessage={`No roles matched for ${selectedCompObj.name}. Try adjusting your criteria or resume.`}
              savedJobIds={savedJobIds}
              onToggleSave={onToggleSave}
            />
          )}
        </main>
      </div>
    </div>
  )
}
