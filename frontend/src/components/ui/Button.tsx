import * as React from 'react'
import { cn } from '../../lib/utils'

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg' | 'icon'
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', ...props }, ref) => {
    const baseStyles = 'inline-flex items-center justify-center rounded-lg font-medium transition-all duration-150 active:scale-[0.98] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-400 disabled:pointer-events-none disabled:opacity-50'

    const variants = {
      primary: 'bg-zinc-100 text-zinc-900 hover:bg-white shadow-[inset_0_1px_0_0_rgba(255,255,255,0.2)]',
      secondary: 'bg-zinc-800 text-zinc-100 hover:bg-zinc-700/80 border border-white/[0.06]',
      outline: 'border border-white/[0.12] bg-transparent text-zinc-300 hover:bg-white/[0.04] hover:text-white',
      ghost: 'bg-transparent text-zinc-400 hover:bg-white/[0.04] hover:text-zinc-100',
      danger: 'bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20',
    }

    const sizes = {
      sm: 'h-8 px-3 text-xs gap-1.5',
      md: 'h-9 px-4 text-sm gap-2',
      lg: 'h-11 px-6 text-base gap-2.5',
      icon: 'h-9 w-9 p-0',
    }

    return (
      <button
        ref={ref}
        className={cn(baseStyles, variants[variant], sizes[size], className)}
        {...props}
      />
    )
  }
)
Button.displayName = 'Button'
