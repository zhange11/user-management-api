const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function getToken() {
  return localStorage.getItem('jwt')
}

async function request(path, options = {}) {
  const token = getToken()
  const headers = {
    ...(options.body && !(options.body instanceof FormData)
      ? { 'Content-Type': 'application/json' }
      : {}),
    ...(token ? { Authorization: token } : {}),
    ...options.headers
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`
    try {
      const body = await response.json()
      message = body.message || message
    } catch {
      // no JSON body
    }
    throw new Error(message)
  }

  return response
}

export const api = {
  login: (email, password) =>
    request('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    }),

  register: (payload) =>
    request('/api/v1/customers', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),

  uploadProfileImage: (customerId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request(`/api/v1/customers/${customerId}/profile-image`, {
      method: 'POST',
      body: formData
    })
  },

  getProfileImageUrl: (customerId) =>
    `${API_BASE_URL}/api/v1/customers/${customerId}/profile-image`,

  oauthRedirectUrl: (provider) => `${API_BASE_URL}/oauth2/authorization/${provider}`
}

export { API_BASE_URL, getToken }