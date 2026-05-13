import client from './client'

export const login = async (email: string, password: string) => {
  const res = await client.post('/user-service/v1/auth/login', { email, password })
  return res.data.data ?? res.data
}

export const logout = async () => {
  await client.post('/user-service/v1/auth/logout')
}

export const register = async (data: {
  email: string; password: string; name: string; nickname: string; phoneNumber: string
}) => {
  const res = await client.post('/user-service/v1/users', data)
  return res.data.data ?? res.data
}

export const getMe = async () => {
  const res = await client.get('/user-service/v1/users/me')
  return res.data.data ?? res.data
}

export const updateMe = async (data: {
  name: string
  nickname: string
  phoneNumber: string
}) => {
  const res = await client.patch('/user-service/v1/users/me', data)
  return res.data.data ?? res.data
}
