import React from 'react'
import { Inbox } from 'lucide-react'
import { Match } from '../types'
import { JobCard } from './JobCard'

interface JobListProps {
  jobs: Match[]
  emptyMessage?: string
  savedJobIds: Set<number>
  onToggleSave: (job: Match) => void
}

export function JobList({
  jobs,
  emptyMessage = 'No job matches found.',
  savedJobIds,
  onToggleSave,
}: JobListProps) {
  if (!jobs.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 px-4 rounded-xl border border-white/[0.06] bg-zinc-900/20 text-center">
        <div className="w-12 h-12 rounded-full bg-zinc-900 border border-white/[0.08] flex items-center justify-center mb-4 text-zinc-500">
          <Inbox className="w-6 h-6" />
        </div>
        <h3 className="text-sm font-semibold text-zinc-300 mb-1">Nothing here yet</h3>
        <p className="text-sm text-zinc-500 max-w-sm">{emptyMessage}</p>
      </div>
    )
  }

  return (
    <div className="grid gap-3">
      {jobs.map((job, index) => {
        const key = job.jobId ?? job.externalId ?? index
        const isSaved = job.jobId ? savedJobIds.has(job.jobId) : false
        return (
          <JobCard
            key={key}
            job={job}
            isSaved={isSaved}
            onToggleSave={onToggleSave}
          />
        )
      })}
    </div>
  )
}
