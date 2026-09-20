import React, { useState, FormEvent } from 'react'
import { Preferences } from '../../jobs/types'
import { Card } from '../../../components/ui/Card'
import { Input } from '../../../components/ui/Input'
import { Button } from '../../../components/ui/Button'

interface ProfileProps {
  preferences: Preferences
  setPreferences: (prefs: Preferences) => void
  onSave: (prefs: Preferences) => void
  saving?: boolean
}

export function Profile({ preferences, setPreferences, onSave, saving }: ProfileProps) {
  const [formData, setFormData] = useState<Preferences>(preferences)

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    onSave(formData)
  }

  return (
    <div className="space-y-8 max-w-2xl">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Search Preferences</h1>
        <p className="text-sm text-zinc-400 mt-2">
          Fine-tune the parameters that direct your automated cross-provider job extraction.
        </p>
      </div>

      <Card className="p-6">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-zinc-400">Target Country / Region</label>
            <Input
              value={formData.country}
              onChange={(e) => setFormData({ ...formData, country: e.target.value })}
              placeholder="e.g. India, United States"
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-zinc-400">Years of Experience</label>
            <Input
              type="number"
              min="0"
              value={formData.experienceYears}
              onChange={(e) => setFormData({ ...formData, experienceYears: Number(e.target.value) })}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-zinc-400">Desired Target Role</label>
            <Input
              value={formData.desiredRole}
              onChange={(e) => setFormData({ ...formData, desiredRole: e.target.value })}
              placeholder="e.g. Senior Software Engineer"
              required
            />
          </div>

          <Button type="submit" disabled={saving} className="w-full">
            {saving ? 'Saving...' : 'Save Parameters'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
