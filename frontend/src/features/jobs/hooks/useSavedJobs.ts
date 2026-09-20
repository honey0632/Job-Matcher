import { useState, useEffect } from 'react'
import { Match } from '../types'
import { apiClient } from '../../../lib/api-client'

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

export function useSavedJobs() {
  const [savedJobs, setSavedJobs] = useState<Match[]>(readStoredSavedJobs())
  const [savedJobIds, setSavedJobIds] = useState<Set<number>>(new Set(readStoredSavedJobs().map(j => j.jobId).filter((id): id is number => typeof id === 'number')))

  useEffect(() => {
    apiClient.jobs.getSaved()
      .then(jobs => {
        setSavedJobs(jobs)
        setSavedJobIds(new Set(jobs.map(j => j.jobId).filter((id): id is number => typeof id === 'number')))
        localStorage.setItem(SAVED_JOBS_STORAGE_KEY, JSON.stringify(jobs))
      })
      .catch(() => undefined)
  }, [])

  const toggleSave = async (job: Match) => {
    if (!job.jobId) return
    const isSaved = savedJobIds.has(job.jobId)

    try {
      if (isSaved) {
        await apiClient.jobs.unsave(job.jobId)
        const nextJobs = savedJobs.filter(j => j.jobId !== job.jobId)
        setSavedJobs(nextJobs)
        setSavedJobIds(new Set(nextJobs.map(j => j.jobId).filter((id): id is number => typeof id === 'number')))
        localStorage.setItem(SAVED_JOBS_STORAGE_KEY, JSON.stringify(nextJobs))
      } else {
        const saved = await apiClient.jobs.save(job.jobId, job.score)
        const nextJobs = [saved, ...savedJobs.filter(j => j.jobId !== job.jobId)]
        setSavedJobs(nextJobs)
        setSavedJobIds(new Set(nextJobs.map(j => j.jobId).filter((id): id is number => typeof id === 'number')))
        localStorage.setItem(SAVED_JOBS_STORAGE_KEY, JSON.stringify(nextJobs))
      }
    } catch {
      throw new Error('Failed to update saved job.')
    }
  }

  return { savedJobs, savedJobIds, toggleSave }
}
