import { User, Preferences, Resume, Match, AuthCredentials, RegistrationCredentials } from '../features/jobs/types'

const baseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')
let csrfToken: string | undefined

async function getCsrfToken(): Promise<string> {
  if (csrfToken) return csrfToken

  const response = await fetch(`${baseUrl}/api/auth/csrf`, {
    credentials: 'include',
  })
  if (!response.ok) throw new Error('Unable to initialize secure requests')

  const token = (await response.json()).token as string
  if (!token) throw new Error('Secure request token was empty')
  csrfToken = token
  return csrfToken
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase()
  const headers = new Headers(options.headers)

  if (!(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    headers.set('X-XSRF-TOKEN', await getCsrfToken())
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    credentials: 'include',
    headers,
  })

  if (!response.ok) {
    throw new Error((await response.text()) || `Request failed (${response.status})`)
  }
  return response.status === 204 ? (undefined as T) : response.json()
}

export const apiClient = {
  auth: {
    me: () => request<User>('/api/auth/me'),
    login: (credentials: AuthCredentials) =>
      request<User>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
      }),
    register: (credentials: RegistrationCredentials) =>
      request<User>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(credentials),
      }),
  },
  profile: {
    getPreferences: () => request<Preferences | null>('/api/profile/preferences'),
    savePreferences: (preferences: Preferences) =>
      request<Preferences>('/api/profile/preferences', {
        method: 'PUT',
        body: JSON.stringify(preferences),
      }),
  },
  resume: {
    upload: (file: File) => {
      const form = new FormData()
      form.append('file', file)
      return request<Resume>('/api/resumes/upload', { method: 'POST', body: form })
    },
  },
  jobs: {
    search: (criteria: Preferences) =>
      request<Match[]>('/api/jobs/search', {
        method: 'POST',
        body: JSON.stringify(criteria),
      }),
    matches: () => request<Match[]>('/api/jobs/matches'),
    getSaved: () => request<Match[]>('/api/jobs/saved'),
    getSavedIds: () => request<number[]>('/api/jobs/saved/ids'),
    save: (jobId: number, score?: number) =>
      request<Match>(`/api/jobs/saved/${jobId}${score !== undefined ? `?score=${score}` : ''}`, {
        method: 'POST',
      }),
    unsave: (jobId: number) =>
      request<void>(`/api/jobs/saved/${jobId}`, {
        method: 'DELETE',
      }),
  },
}
