import React, { useState } from 'react'
import { LogIn, Sparkles, Database, ShieldCheck, Cpu } from 'lucide-react'
import { Card } from '../../../components/ui/Card'
import { Button } from '../../../components/ui/Button'
import { LegalLinks } from '../../../app/Legal'

const backendUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export function LandingPage() {
  const [showLogin, setShowLogin] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Handle login logic here
    console.log('Login attempt with', email, password)
  }

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100 flex flex-col justify-between selection:bg-white/10">
      {/* Header */}
      <header className="border-b border-white/[0.04] backdrop-blur bg-zinc-950/80 sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-zinc-100 flex items-center justify-center text-zinc-900 font-extrabold text-sm">
              JF
            </div>
            <strong className="text-sm font-bold tracking-tight text-zinc-100">Job Fetcher</strong>
          </div>
          <Button variant="primary" size="sm" onClick={() => setShowLogin(!showLogin)}>
            {showLogin ? 'Back to Landing' : 'Sign In'}
          </Button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-6xl mx-auto px-6 py-12 md:py-24 space-y-24">
        {showLogin ? (
          <div className="flex items-center justify-center">
            <Card className="w-full max-w-md p-8 space-y-6">
              <div className="space-y-2 text-center">
                <h2 className="text-2xl font-bold">Sign In</h2>
                <p className="text-sm text-zinc-400">Enter your credentials to access your account</p>
              </div>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <label htmlFor="email" className="text-sm font-medium">Email</label>
                  <input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full px-3 py-2 bg-zinc-800 border border-white/[0.08] rounded-lg focus:outline-none focus:ring-2 focus:ring-zinc-400"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <label htmlFor="password" className="text-sm font-medium">Password</label>
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="text-sm text-zinc-400 hover:text-zinc-300 focus:outline-none"
                    >
                      {showPassword ? 'Hide' : 'Show'}
                    </button>
                  </div>
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full px-3 py-2 bg-zinc-800 border border-white/[0.08] rounded-lg focus:outline-none focus:ring-2 focus:ring-zinc-400"
                    required
                  />
                </div>
                <Button type="submit" className="w-full">
                  Sign In
                </Button>
              </form>
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t border-white/[0.08]"></span>
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-zinc-950 px-2 text-zinc-400">Or continue with</span>
                </div>
              </div>
              <Button variant="secondary" className="w-full" onClick={() => window.location.href = `${backendUrl}/oauth2/authorization/google`}>
                <LogIn className="w-4 h-4 mr-2" />
                Continue with Google
              </Button>
            </Card>
          </div>
        ) : (
          <>
            {/* Hero Section */}
            <section className="text-center max-w-3xl mx-auto space-y-6">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/[0.04] border border-white/[0.08] text-xs font-semibold tracking-wider uppercase text-zinc-400">
                <Sparkles className="w-3.5 h-3.5 text-zinc-300 animate-pulse" /> AI-Driven Recruitment
              </span>
              <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight leading-none bg-gradient-to-b from-white to-zinc-400 bg-clip-text text-transparent">
                Your Resume, Mapped Directly to Top Careers.
              </h1>
              <p className="text-base md:text-lg text-zinc-400 leading-relaxed">
                Ingest live public sitemaps and API feeds from Google, Amazon, Wells Fargo, and NVIDIA. Align your background with semantic AI-scored precision.
              </p>
              <div className="flex items-center justify-center gap-3 pt-4">
                <Button size="lg" onClick={() => setShowLogin(true)}>
                  Continue with Email
                </Button>
                <Button variant="secondary" size="lg" onClick={() => window.location.href = `${backendUrl}/oauth2/authorization/google`}>
                  Continue with Google
                </Button>
              </div>
            </section>

            {/* Features Section */}
            <section className="grid sm:grid-cols-3 gap-6">
              <Card className="p-6 space-y-4">
                <div className="w-10 h-10 rounded-lg bg-zinc-800 border border-white/[0.06] flex items-center justify-center text-zinc-300">
                  <Database className="w-5 h-5" />
                </div>
                <h3 className="text-lg font-bold">Robust Feeds Ingestion</h3>
                <p className="text-xs md:text-sm text-zinc-400 leading-relaxed">
                  Fetches records through highly safety-bounded public channels: Google script payloads, Amazon JSON batches, sitemaps, and XML feeds.
                </p>
              </Card>

              <Card className="p-6 space-y-4">
                <div className="w-10 h-10 rounded-lg bg-zinc-800 border border-white/[0.06] flex items-center justify-center text-zinc-300">
                  <Cpu className="w-5 h-5" />
                </div>
                <h3 className="text-lg font-bold">Dual Matching Engines</h3>
                <p className="text-xs md:text-sm text-zinc-400 leading-relaxed">
                  Toggle between offline title-weighted keyword overlap or LLM-semantic scoring with Gemini 3.6 for high-end contextual matches.
                </p>
              </Card>

              <Card className="p-6 space-y-4">
                <div className="w-10 h-10 rounded-lg bg-zinc-800 border border-white/[0.06] flex items-center justify-center text-zinc-300">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <h3 className="text-lg font-bold">Tactile Security Shell</h3>
                <p className="text-xs md:text-sm text-zinc-400 leading-relaxed">
                  Google OIDC account provisioning combined with HttpOnly session cookies, CSRF protection, and local caching.
                </p>
              </Card>
            </section>
          </>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-white/[0.04] bg-zinc-950/20 py-8">
        <div className="max-w-6xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4 text-xs text-zinc-500">
          <div className="flex items-center gap-2">
            <strong>💼 Job Fetcher</strong>
            <span>© 2026. Fully open-source pipelines.</span>
          </div>
          <LegalLinks />
        </div>
      </footer>
    </div>
  )
}
