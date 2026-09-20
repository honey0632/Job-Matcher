import React from 'react'

export function JobSkeleton() {
  return (
    <div className="rounded-xl border border-white/[0.08] bg-zinc-900/60 p-5 shimmer">
      <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
        <div className="space-y-3 flex-1">
          <div className="h-5 bg-zinc-800 rounded w-1/3" />
          <div className="flex gap-4">
            <div className="h-4 bg-zinc-800/60 rounded w-1/4" />
            <div className="h-4 bg-zinc-800/60 rounded w-1/4" />
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="h-8 bg-zinc-800/80 rounded w-16" />
          <div className="h-8 bg-zinc-800/80 rounded w-20" />
        </div>
      </div>
    </div>
  )
}
