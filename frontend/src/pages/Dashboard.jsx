import { useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { api } from '../api/client.js'

export default function Dashboard() {
  const { logout } = useAuth()
  const [customerId, setCustomerId] = useState('')
  const [file, setFile] = useState(null)
  const [status, setStatus] = useState(null)

  async function handleUpload(e) {
    e.preventDefault()
    setStatus(null)
    if (!file || !customerId) {
      setStatus({ type: 'error', message: 'Enter a customer ID and pick a file first.' })
      return
    }
    try {
      await api.uploadProfileImage(customerId, file)
      setStatus({ type: 'success', message: 'Uploaded to S3.' })
    } catch (err) {
      setStatus({ type: 'error', message: err.message })
    }
  }

  return (
    <div className="dashboard">
      <header>
        <h1>Dashboard</h1>
        <button onClick={logout}>Sign out</button>
      </header>

      <section className="upload-card">
        <h2>Upload profile image</h2>
        <form onSubmit={handleUpload}>
          <label>
            Customer ID
            <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} required />
          </label>
          <label>
            File
            <input type="file" accept="image/*" onChange={(e) => setFile(e.target.files[0])} required />
          </label>
          <button type="submit">Upload</button>
        </form>
        {status && <p className={status.type}>{status.message}</p>}

        {customerId && (
          <img
            className="preview"
            src={api.getProfileImageUrl(customerId)}
            alt="Current profile"
            onError={(e) => (e.target.style.display = 'none')}
          />
        )}
      </section>
    </div>
  )
}