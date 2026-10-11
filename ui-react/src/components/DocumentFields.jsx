import { MAX_LENGTH } from '../validation'

const CONTENT_TYPES = [
  { value: 'application/pdf', label: 'PDF' },
  { value: 'image/png', label: 'PNG' },
  { value: 'image/jpeg', label: 'JPEG' },
  { value: 'text/plain', label: 'Text' }
]

// Gemeinsame Formularfelder für "Dokument erstellen" (Dashboard) und "Dokument bearbeiten" (Detailseite).
function DocumentFields({ values, errors, onChange }) {
  const knownType = CONTENT_TYPES.some(type => type.value === values.contentType)

  function update(field) {
    return event => onChange({ ...values, [field]: event.target.value })
  }

  return (
    <>
      <Field id="title" label="Titel *" error={errors.title}>
        <input
          id="title"
          value={values.title}
          onChange={update('title')}
          maxLength={MAX_LENGTH}
          placeholder="z. B. Rechnung Oktober"
          className={errors.title ? 'invalid' : undefined}
          aria-invalid={Boolean(errors.title)}
          aria-describedby="title-error"
        />
      </Field>

      <Field id="filename" label="Dateiname *" error={errors.filename}>
        <input
          id="filename"
          value={values.filename}
          onChange={update('filename')}
          maxLength={MAX_LENGTH}
          placeholder="z. B. rechnung-10.pdf"
          className={errors.filename ? 'invalid' : undefined}
          aria-invalid={Boolean(errors.filename)}
          aria-describedby="filename-error"
        />
      </Field>

      <Field id="contentType" label="Dateityp" error={errors.contentType}>
        <select
          id="contentType"
          value={values.contentType}
          onChange={update('contentType')}
          className={errors.contentType ? 'invalid' : undefined}
          aria-invalid={Boolean(errors.contentType)}
          aria-describedby="contentType-error"
        >
          <option value="">– keiner –</option>
          {CONTENT_TYPES.map(type => (
            <option key={type.value} value={type.value}>{type.label}</option>
          ))}
          {values.contentType && !knownType && (
            <option value={values.contentType}>{values.contentType}</option>
          )}
        </select>
      </Field>
    </>
  )
}

export function Field({ id, label, error, children }) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      {children}
      <span className="field-error" id={`${id}-error`}>{error}</span>
    </div>
  )
}

export default DocumentFields
