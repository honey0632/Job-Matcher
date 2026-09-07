// Centralizes typed API calls, CSRF handling, and authenticated request behavior.

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

export const api = {
  me: () => request<User>('/api/auth/me'),
  getPreferences: () => request<Preferences | null>('/api/profile/preferences'),
  savePreferences: (preferences: Preferences) =>
    request<Preferences>('/api/profile/preferences', {
      method: 'PUT',
      body: JSON.stringify(preferences),
    }),
  uploadResume: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request<Resume>('/api/resumes/upload', { method: 'POST', body: form })
  },
  searchJobs: (criteria: Preferences) =>
    request<Match[]>('/api/jobs/search', {
      method: 'POST',
      body: JSON.stringify(criteria),
    }),
  matches: () => request<Match[]>('/api/jobs/matches?threshold=-1'),
}

export type User = {
  id?: number
  displayName?: string
  email?: string
}

export type Preferences = {
  country: string
  experienceYears: number
  desiredRole: string
}

export type Resume = {
  id?: number
  originalFilename?: string
  status?: string
  uploadedAt?: string
}

export type Match = {
  jobId?: number
  externalId?: string
  title?: string
  company?: string
  location?: string
  jobUrl?: string
  score?: number
}
