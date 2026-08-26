interface AlertBannerProps {
  kind: 'error' | 'success'
  message: string
  onDismiss: () => void
}

export function AlertBanner({ kind, message, onDismiss }: AlertBannerProps) {
  return (
    <div className={`alert alert-${kind}`} role={kind === 'error' ? 'alert' : 'status'} aria-live="polite">
      <span aria-hidden="true">{kind === 'error' ? '!' : '✓'}</span>
      <span>{message}</span>
      <button type="button" className="alert-close" onClick={onDismiss} aria-label="Dismiss notification">
        ×
      </button>
    </div>
  )
}
