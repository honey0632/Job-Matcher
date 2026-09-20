import React from 'react'
import { Bookmark, ExternalLink, ChevronDown, ChevronUp, Sparkles, Building2, MapPin } from 'lucide-react'
import { Match } from '../types'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Badge } from '../../../components/ui/Badge'
import { cn } from '../../../lib/utils'

export interface JobCardProps {
  job: Match
  isSaved: boolean
  onToggleSave: (job: Match) => void
}

export function JobCard({ job, isSaved, onToggleSave }: JobCardProps) {
  const [showDescription, setShowDescription] = React.useState(false)
  const jobId = job.jobId ?? job.externalId

  return (
    <Card className="group relative overflow-hidden transition-all hover:border-white/[0.15] hover:bg-zinc-900/80 hover:shadow-card-hover">
      <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
        <div className="space-y-2 flex-1">
          <div className="flex items-center gap-2 flex-wrap">
            <h3 className="text-base font-semibold text-zinc-100 group-hover:text-white">
              {job.title || 'Untitled role'}
            </h3>
            {job.source && (
              <Badge variant="outline" className="text-[10px] font-mono uppercase tracking-wider">
                {job.source}
              </Badge>
            )}
          </div>

          <div className="flex items-center gap-4 text-sm text-zinc-400">
            <span className="inline-flex items-center gap-1.5 text-zinc-300 font-medium">
              <Building2 className="w-4 h-4 text-zinc-500" />
              {job.company || 'Company'}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-zinc-500" />
              {job.location || 'Location'}
            </span>
          </div>
        </div>

        <div className="flex items-center justify-between md:justify-end gap-3 pt-2 md:pt-0 border-t md:border-t-0 border-white/[0.06]">
          {job.score !== undefined && (
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-mono font-semibold tabular-nums">
              <Sparkles className="w-3.5 h-3.5" />
              {job.score}% match
            </div>
          )}

          <div className="flex items-center gap-2">
            {job.jobId && (
              <Button
                variant={isSaved ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => onToggleSave(job)}
                className={cn(isSaved && 'bg-amber-500/20 text-amber-300 border border-amber-500/30 hover:bg-amber-500/30')}
              >
                <Bookmark className={cn("w-3.5 h-3.5", isSaved && "fill-amber-300")} />
                {isSaved ? 'Saved' : 'Save'}
              </Button>
            )}

            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowDescription(!showDescription)}
            >
              Details
              {showDescription ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
            </Button>

            {job.jobUrl && (
              <Button
                variant="primary"
                size="sm"
                onClick={() => window.open(job.jobUrl, '_blank', 'noreferrer')}
                className="inline-flex items-center gap-1.5"
              >
                Apply <ExternalLink className="w-3.5 h-3.5" />
              </Button>
            )}
          </div>
        </div>
      </div>

      {showDescription && (
        <div className="mt-4 pt-4 border-t border-white/[0.06] animate-in fade-in duration-200">
          <p className="text-xs font-mono text-zinc-500 uppercase tracking-wider mb-2">Job Description</p>
          <div className="text-sm text-zinc-300 leading-relaxed whitespace-pre-wrap bg-zinc-950/50 p-4 rounded-lg border border-white/[0.04] max-h-80 overflow-y-auto">
            {job.description || 'No detailed job description provided.'}
          </div>
        </div>
      )}
    </Card>
  )
}
