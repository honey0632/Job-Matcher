import React, { useState, useEffect } from 'react'
import { Match } from '../types'
import { JobList } from './JobList'
import { JobSkeleton } from './JobSkeleton'
import { apiClient } from '../../../lib/api-client'

interface MatchesProps {
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
}

export function Matches({ savedJobIds, onToggleSave }: MatchesProps) {
  const [matches, setMatches] = useState<Match[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    apiClient.jobs.matches()
      .then(setMatches)
      .catch(() => setMatches([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">AI Recommended Matches</h1>
        <p className="text-sm text-zinc-400 mt-2">
          High-alignment opportunities surfaced via semantic resume scoring.
        </p>
      </div>

      {loading ? (
        <div className="space-y-3">
          <JobSkeleton />
          <JobSkeleton />
          <JobSkeleton />
        </div>
      ) : (
        <JobList
          jobs={matches}
          emptyMessage="No direct recommendation results available. Ensure your resume is processed and target role is saved."
          savedJobIds={savedJobIds}
          onToggleSave={onToggleSave}
        />
      )}
    </div>
  )
}
