import * as React from 'react'
import { cn } from '../../lib/utils'

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'outline' | 'success' | 'warning' | 'info'
}

export const Badge = React.forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className, variant = 'default', ...props }, ref) => {
    const baseStyles = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold tracking-wide transition-colors border'

    const variants = {
      default: 'bg-zinc-800 text-zinc-300 border-white/[0.04]',
      outline: 'bg-transparent text-zinc-400 border-white/[0.12]',
      success: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      warning: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      info: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
    }

    return (
      <span
        ref={ref}
        className={cn(baseStyles, variants[variant], className)}
        {...props}
      />
    )
  }
)
Badge.displayName = 'Badge'
