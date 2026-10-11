// Client-seitige Validierung. Das Backend prüft zusätzlich mit @NotBlank / @Size(max = 255).
export const MAX_LENGTH = 255
const FILENAME_PATTERN = /^[^\\/:*?"<>|]+\.[A-Za-z0-9]{1,10}$/
const CONTENT_TYPE_PATTERN = /^[\w.+-]+\/[\w.+-]+$/

export function normalizeDocument(form) {
  return {
    title: form.title.trim(),
    filename: form.filename.trim(),
    contentType: form.contentType.trim() || null
  }
}

export function validateDocument(form) {
  const { title, filename, contentType } = normalizeDocument(form)
  const errors = {}

  if (!title) {
    errors.title = 'Titel ist ein Pflichtfeld.'
  } else if (title.length > MAX_LENGTH) {
    errors.title = `Titel darf höchstens ${MAX_LENGTH} Zeichen lang sein.`
  }

  if (!filename) {
    errors.filename = 'Dateiname ist ein Pflichtfeld.'
  } else if (filename.length > MAX_LENGTH) {
    errors.filename = `Dateiname darf höchstens ${MAX_LENGTH} Zeichen lang sein.`
  } else if (!FILENAME_PATTERN.test(filename)) {
    errors.filename = 'Dateiname braucht eine Endung (z. B. rechnung.pdf) und darf keines der Zeichen \\ / : * ? " < > | enthalten.'
  }

  if (contentType && !CONTENT_TYPE_PATTERN.test(contentType)) {
    errors.contentType = 'Dateityp muss die Form typ/subtyp haben (z. B. application/pdf).'
  }

  return errors
}

export function validateComment(text) {
  const trimmed = text.trim()
  const errors = {}
  if (!trimmed) {
    errors.text = 'Kommentar darf nicht leer sein.'
  } else if (trimmed.length > MAX_LENGTH) {
    errors.text = `Kommentar darf höchstens ${MAX_LENGTH} Zeichen lang sein.`
  }
  return errors
}

export function hasErrors(errors) {
  return Object.keys(errors).length > 0
}
