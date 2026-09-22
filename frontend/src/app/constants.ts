import { Match, Preferences } from '../features/jobs/types'

export type View = 'home' | 'profile' | 'resume' | 'search' | 'matches' | 'saved'

export const COMPANIES = [
  { id: 'GOOGLE_CAREERS', name: 'Google Careers', icon: '🔵', summary: 'Search Google roles' },
  { id: 'AMAZON', name: 'Amazon Jobs', icon: '🟧', summary: 'Search Amazon roles' },
  { id: 'WELLS_FARGO', name: 'Wells Fargo', icon: '🔴', summary: 'Search Wells Fargo roles' },
  { id: 'NVIDIA', name: 'NVIDIA Jobs', icon: '🟢', summary: 'Search NVIDIA roles' },
  { id: 'META', name: 'Meta Careers', icon: '🔷', summary: 'Search Meta roles' },
  { id: 'MICROSOFT', name: 'Microsoft Careers', icon: '🟦', summary: 'Search Microsoft roles' },
  { id: 'APPLE', name: 'Apple Jobs', icon: '🍎', summary: 'Search Apple roles' },
  { id: 'NETFLIX', name: 'Netflix Jobs', icon: '🔴', summary: 'Search Netflix roles' },
  { id: 'UBER', name: 'Uber Jobs', icon: '⚫', summary: 'Search Uber roles' },
  { id: 'INFOSYS', name: 'Infosys Careers', icon: '🔷', summary: 'Search Infosys roles' },
  { id: 'TCS', name: 'TCS Careers', icon: '🔷', summary: 'Search TCS roles' },
  { id: 'WIPRO', name: 'Wipro Careers', icon: '🟣', summary: 'Search Wipro roles' },
  { id: 'ADOBE', name: 'Adobe Careers', icon: '🔺', summary: 'Search Adobe roles' },
  { id: 'SALESFORCE', name: 'Salesforce Jobs', icon: '☁️', summary: 'Search Salesforce roles' },
  { id: 'ATLASSIAN', name: 'Atlassian Jobs', icon: '🔷', summary: 'Search Atlassian roles' },
  { id: 'INTUIT', name: 'Intuit Jobs', icon: '🟦', summary: 'Search Intuit roles' },
  { id: 'SERVICENOW', name: 'ServiceNow Jobs', icon: '🟢', summary: 'Search ServiceNow roles' },
  { id: 'ORACLE', name: 'Oracle Jobs', icon: '🔴', summary: 'Search Oracle roles' },
  { id: 'CISCO', name: 'Cisco Jobs', icon: '🔵', summary: 'Search Cisco roles' },
  { id: 'DATABRICKS', name: 'Databricks Jobs', icon: '🟪', summary: 'Search Databricks roles' },
  { id: 'SNOWFLAKE', name: 'Snowflake Jobs', icon: '🟦', summary: 'Search Snowflake roles' },
] as const

export const emptyPreferences: Preferences = {
  country: '',
  experienceYears: 0,
  desiredRole: '',
}
