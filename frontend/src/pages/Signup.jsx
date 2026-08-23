import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function Signup() {
  const [form, setForm] = useState({ username: '', email: '', password: '', age: '', gender: 'MALE' })
  const [error, setError] = useState(null)
  const { login } = useAuth()
  const navigate = useNavigate()

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    try {
      const response = await api.register({
        ...form,
        age: Number(form.age)
      })
      const token = response.headers.get('Authorization')
      login(token)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Create account</h1>

        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Username
            <input value={form.username} onChange={(e) => update('username', e.target.value)} required />
          </label>
          <label>
            Email
            <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />
          </label>
          <label>
            Password
            <input type="password" value={form.password} onChange={(e) => update('password', e.target.value)} required />
          </label>
          <label>
            Age
            <input type="number" min="1" value={form.age} onChange={(e) => update('age', e.target.value)} required />
          </label>
          <label>
            Gender
            <select value={form.gender} onChange={(e) => update('gender', e.target.value)}>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
            </select>
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit">Sign up</button>
        </form>

        <p className="switch-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}