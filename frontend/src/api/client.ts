import axios from 'axios'
import {useAuthStore} from '../store/authStore'

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    headers: {'Content-Type': 'application/json'},
})

client.interceptors.request.use((config) => {
    const {accessToken, user} = useAuthStore.getState()
    const headers = config.headers as any
    if (accessToken) headers.Authorization = `Bearer ${accessToken}`
    if (user?.userId) headers['X-User-Id'] = user.userId
    if (user?.role) headers['X-User-Role'] = user.role
    return config
})

client.interceptors.response.use(
    (res) => res,
    async (error) => {
        if (error.response?.status === 401) {
            try {
                const res = await axios.post('/user-service/v1/auth/reissue')
                useAuthStore.getState().setAccessToken(res.data.accessToken)
                const original = error.config as any
                if (original._retry) {
                    return Promise.reject(error)
                }
                original._retry = true
                original.headers = original.headers ?? {}
                original.headers.Authorization = `Bearer ${res.data.accessToken}`
                return client(original)
            } catch {
                useAuthStore.getState().logout()
            }
        }
        return Promise.reject(error)
    }
)

export default client
