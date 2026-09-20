import { Match, Preferences } from '../features/jobs/types'

export type View = 'home' | 'profile' | 'resume' | 'search' | 'matches' | 'saved'

export const COMPANIES = [
  { id: 'GOOGLE_CAREERS', name: 'Google Careers', icon: '🔵', summary: 'Search Google roles' },
  { id: 'AMAZON', name: 'Amazon Jobs', icon: '🟧', summary: 'Search Amazon roles' },
  { id: 'WELLS_FARGO', name: 'Wells Fargo', icon: '🔴', summary: 'Search Wells Fargo roles' },
  { id: 'NVIDIA', name: 'NVIDIA Jobs', icon: '🟢', summary: 'Search NVIDIA roles' },
] as const

export const emptyPreferences: Preferences = {
  country: '',
  experienceYears: 0,
  desiredRole: '',
}
