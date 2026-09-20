import React, { useState, ChangeEvent } from 'react'
import { Upload, FileText, CheckCircle2 } from 'lucide-react'
import { Card } from '../../../components/ui/Card'
import { Button } from '../../../components/ui/Button'
import { apiClient } from '../../../lib/api-client'

export function Resume() {
  const [file, setFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)
  const [success, setSuccess] = useState(false)

  const handleUpload = async () => {
    if (!file) return
    setUploading(true)
    setSuccess(false)
    try {
      await apiClient.resume.upload(file)
      setSuccess(true)
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="space-y-8 max-w-2xl">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Resume Extraction</h1>
        <p className="text-sm text-zinc-400 mt-2">
          Upload your PDF or DOCX file to automatically drive semantic match scoring.
        </p>
      </div>

      <Card className="p-8 border-dashed border-white/[0.12] bg-zinc-900/30 text-center space-y-4">
        <div className="w-12 h-12 rounded-full bg-zinc-800 border border-white/[0.08] flex items-center justify-center mx-auto text-zinc-400">
          <Upload className="w-6 h-6" />
        </div>
        <div className="space-y-1">
          <p className="text-sm font-medium text-zinc-200">Select a PDF or DOCX file from your device</p>
          <p className="text-xs text-zinc-500">Maximum file size 10MB</p>
        </div>

        <input
          type="file"
          id="resume-file"
          accept=".pdf,.docx"
          className="hidden"
          onChange={(e: ChangeEvent<HTMLInputElement>) => setFile(e.target.files?.[0] || null)}
        />

        <div className="pt-2">
          <Button variant="outline" onClick={() => document.getElementById('resume-file')?.click()}>
            Browse Files
          </Button>
        </div>

        {file && (
          <div className="flex items-center justify-center gap-2 text-xs font-mono text-zinc-300 pt-2">
            <FileText className="w-4 h-4 text-zinc-400" />
            {file.name}
          </div>
        )}

        <Button
          disabled={!file || uploading}
          onClick={handleUpload}
          className="w-full mt-4"
        >
          {uploading ? 'Extracting Text...' : 'Upload & Process'}
        </Button>

        {success && (
          <div className="flex items-center justify-center gap-2 text-sm text-emerald-400 font-medium pt-2">
            <CheckCircle2 className="w-4 h-4" />
            Resume processed successfully. Semantic matching updated!
          </div>
        )}
      </Card>
    </div>
  )
}
