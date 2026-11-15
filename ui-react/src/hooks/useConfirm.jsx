import { useCallback, useState } from 'react'
import ConfirmDialog from '../components/ConfirmDialog'

// Bestätigungsdialog im Stil der App statt window.confirm().
// Verwendung:
//   const [confirm, confirmDialog] = useConfirm()
//   if (!(await confirm({ title: 'Löschen?', message: '…' }))) return
//   ... und {confirmDialog} einmal im JSX ausgeben.
export function useConfirm() {
  const [request, setRequest] = useState(null)

  const confirm = useCallback(
    options => new Promise(resolve => setRequest({ ...options, resolve })),
    []
  )

  function close(result) {
    request?.resolve(result)
    setRequest(null)
  }

  return [confirm, <ConfirmDialog request={request} onClose={close} />]
}
