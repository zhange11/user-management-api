import { createContext, useContext, useEffect, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('jwt'))

  useEffect(() => {
    if (token) localStorage.setItem('jwt', token)
    else localStorage.removeItem('jwt')
  }, [token])

  const login = (jwt) => setToken(jwt)
  const logout = () => setToken(null)

  return (
    <AuthContext.Provider value={{ token, isAuthenticated: !!token, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}