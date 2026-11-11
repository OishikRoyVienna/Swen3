
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router'
import { api, formatDate } from '../api'
import { hasErrors, validateDocument } from '../validation'
import DocumentFields from '../components/DocumentFields'

const EMPTY_FORM = {
  title: '',
  filename: '',
  contentType: 'application/pdf'
}

function Dashboard() {
  const [documents, setDocuments] = useState([])
  const [loading, setLoading] = useState(true)
  const [listError, setListError] = useState('')
  const [search, setSearch] = useState('')

  const [form, setForm] = useState(EMPTY_FORM)
  const [submitted, setSubmitted] = useState(false)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)

  const [selectedFile, setSelectedFile] = useState(null)
  const [fileInputKey, setFileInputKey] = useState(0)

  const errors = submitted ? validateDocument(form) : {}

  const loadDocuments = useCallback(() =>
      api.getDocuments()
          .then(data => {
            setDocuments(data)
            setListError('')
          })
          .catch(err =>
              setListError(
                  `Dokumente konnten nicht geladen werden: ${err.message}`
              )
          )
          .finally(() => setLoading(false)), [])

  useEffect(() => {
    loadDocuments()
  }, [loadDocuments])

  function handleFileChange(event) {
    const file = event.target.files?.[0] ?? null

    setSelectedFile(file)
    setMessage(null)

    if (file) {
      setForm(previous => ({
        ...previous,
        filename: file.name,
        contentType: 'application/pdf',
        title: previous.title || file.name.replace(/\.pdf$/i, '')
      }))
    }
  }

  async function handleCreate(event) {
    event.preventDefault()
    setSubmitted(true)
    setMessage(null)

    if (!selectedFile) {
      setMessage({
        type: 'error',
        text: 'Bitte wähle eine PDF-Datei aus.'
      })
      return
    }

    if (
        selectedFile.type !== 'application/pdf' &&
        !selectedFile.name.toLowerCase().endsWith('.pdf')
    ) {
      setMessage({
        type: 'error',
        text: 'Bitte wähle eine gültige PDF-Datei aus.'
      })
      return
    }

    if (selectedFile.size > 20 * 1024 * 1024) {
      setMessage({
        type: 'error',
        text: 'Die PDF darf maximal 20 MB groß sein.'
      })
      return
    }

    if (hasErrors(validateDocument(form))) {
      return
    }

    setSaving(true)

    try {
      await api.uploadDocument(selectedFile, form.title)

      setMessage({
        type: 'success',
        text: 'Dokument erfolgreich erstellt und hochgeladen. OCR-Verarbeitung wurde gestartet.'
      })

      setForm(EMPTY_FORM)
      setSelectedFile(null)
      setSubmitted(false)
      setFileInputKey(key => key + 1)

      await loadDocuments()
    } catch (err) {
      setMessage({
        type: 'error',
        text: `Upload fehlgeschlagen: ${err.message}`
      })
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(doc) {
    if (!window.confirm(
        `Möchtest du "${doc.title}" inklusive Kommentaren wirklich löschen?`
    )) {
      return
    }

    try {
      await api.deleteDocument(doc.id)
      await loadDocuments()
    } catch (err) {
      setListError(
          `Dokument konnte nicht gelöscht werden: ${err.message}`
      )
    }
  }

  const filter = search.trim().toLowerCase()

  const filteredDocuments = documents.filter(doc =>
      !filter
      || (doc.title || '').toLowerCase().includes(filter)
      || (doc.filename || '').toLowerCase().includes(filter)
  )

  return (
      <main className="page">
        <h1>Paperless Dashboard</h1>

        <p className="subtitle">
          SWEN3 – Dokumentenverwaltung mit React
        </p>

        <section className="card">
          <h2>Neues Dokument erstellen</h2>

          <form
              className="form"
              onSubmit={handleCreate}
              noValidate
          >
            <label htmlFor="pdf-file">
              PDF-Datei auswählen *
            </label>

            <input
                key={fileInputKey}
                id="pdf-file"
                type="file"
                accept=".pdf,application/pdf"
                onChange={handleFileChange}
            />

            {selectedFile && (
                <p className="muted">
                  Ausgewählt: {selectedFile.name}
                </p>
            )}

            <DocumentFields
                values={form}
                errors={errors}
                onChange={setForm}
            />

            <button
                type="submit"
                className="btn"
                disabled={saving}
            >
              {saving
                  ? 'Dokument wird hochgeladen...'
                  : 'Dokument erstellen & PDF hochladen'}
            </button>
          </form>

          {message && (
              <p
                  className={`message ${message.type}`}
                  role="status"
              >
                {message.text}
              </p>
          )}
        </section>

        <section>
          <div className="section-header">
            <h2>Meine Dokumente</h2>

            <input
                type="search"
                className="search"
                placeholder="Nach Titel oder Dateiname suchen..."
                aria-label="Dokumente filtern"
                value={search}
                onChange={event => setSearch(event.target.value)}
            />
          </div>

          {loading && (
              <p>Dokumente werden geladen...</p>
          )}

          {listError && (
              <p className="message error" role="alert">
                {listError}
              </p>
          )}

          {!loading && !listError && (
              filteredDocuments.length === 0 ? (
                  <p className="muted">
                    {documents.length === 0
                        ? 'Noch keine Dokumente vorhanden.'
                        : 'Keine Dokumente passen zum Filter.'}
                  </p>
              ) : (
                  <div className="documents">
                    {filteredDocuments.map(doc => (
                        <article
                            className="document-card"
                            key={doc.id}
                        >
                          <h3>
                            <Link to={`/documents/${doc.id}`}>
                              {doc.title}
                            </Link>
                          </h3>

                          <dl className="meta">
                            <dt>Dateiname</dt>
                            <dd>{doc.filename}</dd>

                            <dt>Dateityp</dt>
                            <dd>{doc.contentType || '–'}</dd>

                            <dt>Hochgeladen</dt>
                            <dd>{formatDate(doc.uploadedAt)}</dd>

                            <dt>Kommentare</dt>
                            <dd>{doc.comments?.length ?? 0}</dd>
                          </dl>

                          <div className="actions">
                            <Link
                                className="btn"
                                to={`/documents/${doc.id}`}
                            >
                              Details
                            </Link>

                            <button
                                className="btn btn-danger"
                                onClick={() => handleDelete(doc)}
                            >
                              Löschen
                            </button>
                          </div>
                        </article>
                    ))}
                  </div>
              )
          )}
        </section>
      </main>
  )
}

export default Dashboard
