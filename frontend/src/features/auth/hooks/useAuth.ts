import { useState, useEffect } from 'react'
import { User } from '../types'
import { apiClient } from '../../../lib/api-client'

interface AuthState {
  user: User | null
  loading: boolean
  error: string | null
}

export function useAuth() {
  const [authState, setAuthState] = useState<AuthState>({ user: null, loading: true, error: null })

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const user = await apiClient.auth.me()
        setAuthState({ user, loading: false, error: null })
      } catch (error) {
        setAuthState({ user: null, loading: false, error: error instanceof Error ? error.message : 'Authentication failed' })
      }
    }

    fetchUser()
  }, [])

  return authState
}
