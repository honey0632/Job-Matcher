import React from 'react'
import { Match } from '../types'
import { JobList } from './JobList'

interface SavedJobsProps {
  jobs: Match[]
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
}

export function SavedJobs({ jobs, savedJobIds, onToggleSave }: SavedJobsProps) {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Bookmarked Roles</h1>
        <p className="text-sm text-zinc-400 mt-2">
          Persistent stored roles synchronized across your local cache and PostgreSQL backend.
        </p>
      </div>

      <JobList
        jobs={jobs}
        emptyMessage="No bookmarked roles found. Click 'Save' on any job card to persist it here."
        savedJobIds={savedJobIds}
        onToggleSave={onToggleSave}
      />
    </div>
  )
}
