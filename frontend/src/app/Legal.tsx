import React, { ReactNode } from 'react'
import { ArrowLeft } from 'lucide-react'

interface LegalPageProps {
  title: string
  eyebrow: string
  children: ReactNode
}

export function LegalLinks() {
  return (
    <nav className="flex items-center justify-center gap-6 text-xs text-zinc-500 pt-6">
      <a href="/privacy-policy" className="hover:text-zinc-300 transition-colors">Privacy Policy</a>
      <a href="/terms-of-service" className="hover:text-zinc-300 transition-colors">Terms of Service</a>
    </nav>
  )
}

export function LegalPage({ title, eyebrow, children }: LegalPageProps) {
  return (
    <main className="min-h-screen bg-zinc-950 text-zinc-100 p-6 md:p-12 flex justify-center">
      <article className="max-w-2xl w-full space-y-6">
        <a href="/" className="inline-flex items-center gap-2 text-xs font-semibold text-zinc-400 hover:text-zinc-200 transition-colors">
          <ArrowLeft className="w-4 h-4" /> Back to Dashboard
        </a>
        <div>
          <span className="text-xs font-mono font-bold tracking-wider text-zinc-500 uppercase">{eyebrow}</span>
          <h1 className="text-3xl font-extrabold tracking-tight mt-1">{title}</h1>
          <p className="text-xs text-zinc-500 mt-2">Last updated: September 2026</p>
        </div>
        <div className="space-y-4 text-sm leading-relaxed text-zinc-300 border-t border-white/[0.08] pt-6">
          {children}
        </div>
        <LegalLinks />
      </article>
    </main>
  )
}

export function PrivacyPolicy() {
  return (
    <LegalPage title="Privacy Policy" eyebrow="LEGAL COMPLIANCE">
      <p>Job Matcher processes user preferences, uploaded resumes, and search credentials purely to generate relevant AI match evaluations.</p>
      <h3 className="text-base font-semibold text-zinc-100 pt-2">Data Protection</h3>
      <p>Uploaded documents are parsed solely for text extraction and matching pipelines. Personal files are never sold or leveraged for unapproved telemetry.</p>
    </LegalPage>
  )
}

export function TermsOfService() {
  return (
    <LegalPage title="Terms of Service" eyebrow="LEGAL COMPLIANCE">
      <p>By operating the Job Matcher platform, you agree to access live target APIs in accordance with security terms.</p>
      <h3 className="text-base font-semibold text-zinc-100 pt-2">Service Availability</h3>
      <p>Public provider integrations depend on target sitemap and JSON endpoint response limits.</p>
    </LegalPage>
  )
}
