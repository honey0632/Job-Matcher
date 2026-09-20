import { Match, Preferences } from '../features/jobs/types'

export type View = 'home' | 'profile' | 'resume' | 'search' | 'matches' | 'saved'

export const COMPANIES = [
  { id: 'GOOGLE_CAREERS', name: 'Google Careers', icon: '🔵', summary: 'Search Google roles' },
  { id: 'AMAZON', name: 'Amazon Jobs', icon: '🟧', summary: 'Search Amazon roles' },
  { id: 'WELLS_FARGO', name: 'Wells Fargo', icon: '🔴', summary: 'Search Wells Fargo roles' },
  { id: 'NVIDIA', name: 'NVIDIA Jobs', icon: '🟢', summary: 'Search NVIDIA roles' },
  { id: 'ADOBE', name: 'Adobe Careers', icon: '🔺', summary: 'Search Adobe roles' },
  { id: 'META', name: 'Meta Careers', icon: '🔷', summary: 'Search Meta roles' },
  { id: 'APPLE', name: 'Apple Jobs', icon: '🍎', summary: 'Search Apple roles' },
  { id: 'UBER', name: 'Uber Jobs', icon: '⚫', summary: 'Search Uber roles' },
  { id: 'INFOSYS', name: 'Infosys Careers', icon: '🔷', summary: 'Search Infosys roles' },
  { id: 'WIPRO', name: 'Wipro Careers', icon: '🟣', summary: 'Search Wipro roles' },
  { id: 'SALESFORCE', name: 'Salesforce Jobs', icon: '☁️', summary: 'Search Salesforce roles' },
  { id: 'ATLASSIAN', name: 'Atlassian Jobs', icon: '🔷', summary: 'Search Atlassian roles' },
] as const

export const emptyPreferences: Preferences = {
  country: '',
  experienceYears: 0,
  desiredRole: '',
}
