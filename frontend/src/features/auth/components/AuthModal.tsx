import React, { useState } from 'react'
import { LogIn, Eye, EyeOff } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { apiClient } from '../../../lib/api-client'
import { AuthCredentials, RegistrationCredentials } from '../../jobs/types'

interface AuthModalProps {
  onClose: () => void
}

export function AuthModal({ onClose }: AuthModalProps) {
  const [isLogin, setIsLogin] = useState(true)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      if (isLogin) {
        const credentials: AuthCredentials = { email, password }
        await apiClient.auth.login(credentials)
      } else {
        const credentials: RegistrationCredentials = { email, password, displayName }
        await apiClient.auth.register(credentials)
      }
      window.location.reload()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Authentication failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card className="w-full max-w-md p-6 space-y-6">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold">{isLogin ? 'Sign In' : 'Create Account'}</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-white">
            ×
          </button>
        </div>

        {error && (
          <div className="p-3 bg-red-500/10 text-red-400 border border-red-500/20 rounded-lg text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <label htmlFor="email" className="text-sm font-medium">Email</label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          {!isLogin && (
            <div className="space-y-2">
              <label htmlFor="displayName" className="text-sm font-medium">Display Name</label>
              <Input
                id="displayName"
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                required
              />
            </div>
          )}

          <div className="space-y-2">
            <div className="flex justify-between">
              <label htmlFor="password" className="text-sm font-medium">Password</label>
              {isLogin && (
                <button
                  type="button"
                  onClick={() => setIsLogin(false)}
                  className="text-sm text-zinc-400 hover:text-white"
                >
                  Forgot password?
                </button>
              )}
            </div>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-white"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? (
              <>Processing...</>
            ) : (
              <>
                <LogIn className="w-4 h-4 mr-2" />
                {isLogin ? 'Sign In' : 'Create Account'}
              </>
            )}
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

        <Button
          variant="secondary"
          className="w-full"
          onClick={() => window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`}
        >
          Continue with Google
        </Button>

        <div className="text-center text-sm text-zinc-400">
          {isLogin ? (
            <>Don't have an account? <button type="button" onClick={() => setIsLogin(false)} className="text-white hover:underline">Sign up</button></>
          ) : (
            <>Already have an account? <button type="button" onClick={() => setIsLogin(true)} className="text-white hover:underline">Sign in</button></>
          )}
        </div>
      </Card>
    </div>
  )
}
