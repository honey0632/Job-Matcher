export type User = {
  id?: number
  displayName?: string
  email?: string
}

export type Preferences = {
  country: string
  experienceYears: number
  desiredRole: string
  source?: string
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
  description?: string
  jobUrl?: string
  source?: string
  score?: number
}

export type AuthCredentials = {
  email: string
  password: string
}

export type RegistrationCredentials = {
  email: string
  password: string
  displayName: string
}
