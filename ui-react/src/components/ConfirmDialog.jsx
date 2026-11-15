import { useEffect, useRef } from 'react'

// Modaler Bestätigungsdialog, wird über den Hook useConfirm() verwendet
function ConfirmDialog({ request, onClose }) {
  const dialogRef = useRef(null)

  // <dialog> modal öffnen bzw. schließen (dunkler Hintergrund, Esc, Fokus kommen vom Browser)
  useEffect(() => {
    const dialog = dialogRef.current
    if (request && !dialog.open) {
      dialog.showModal()
    } else if (!request && dialog.open) {
      dialog.close()
    }
  }, [request])

  return (
    <dialog
      ref={dialogRef}
      className="confirm-dialog"
      aria-labelledby="confirm-title"
      onCancel={event => {
        event.preventDefault() // Esc
        onClose(false)
      }}
      onClick={event => {
        if (event.target === dialogRef.current) {
          onClose(false) // Klick auf den dunklen Hintergrund
        }
      }}
    >
      {request && (
        <div className="confirm-content">
          <h3 id="confirm-title">{request.title}</h3>
          <p>{request.message}</p>
          <div className="confirm-actions">
            <button type="button" className="btn btn-secondary" onClick={() => onClose(false)} autoFocus>
              Abbrechen
            </button>
            <button type="button" className="btn btn-danger" onClick={() => onClose(true)}>
              {request.confirmLabel ?? 'Löschen'}
            </button>
          </div>
        </div>
      )}
    </dialog>
  )
}

export default ConfirmDialog
