import { useState, useEffect } from 'react'
import { User } from '../types'
import { apiClient } from '../../../lib/api-client'

export function useAuth() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiClient.auth.me()
      .then((data) => {
        setUser(data)
        setError(null)
      })
      .catch((err) => {
        setUser(null)
        setError(err instanceof Error ? err.message : 'Authentication failed')
      })
      .finally(() => setLoading(false))
  }, [])

  return { user, loading, error }
}
