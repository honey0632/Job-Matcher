import { useState, useEffect } from 'react'
import { Preferences } from '../../jobs/types'
import { apiClient } from '../../../lib/api-client'

export function usePreferences() {
  const [preferences, setPreferences] = useState<Preferences>({
    country: '',
    experienceYears: 0,
    desiredRole: '',
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiClient.profile.getPreferences()
      .then((data) => {
        if (data) setPreferences(data)
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to fetch preferences'))
      .finally(() => setLoading(false))
  }, [])

  const savePreferences = async (newPrefs: Preferences) => {
    setSaving(true)
    setError(null)
    try {
      const data = await apiClient.profile.savePreferences(newPrefs)
      setPreferences(data)
      return data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save preferences')
      throw err;
    } finally {
      setSaving(false)
    }
  }

  return { preferences, setPreferences, savePreferences, loading, saving, error }
}
