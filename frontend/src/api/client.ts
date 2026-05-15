import axios from 'axios'
import {useAuthStore} from '../store/authStore'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() || ''

const client = axios.create({
    baseURL: apiBaseUrl,
    headers: {'Content-Type': 'application/json'},
})

export const publicClient = axios.create({
    baseURL: apiBaseUrl,
    headers: {'Content-Type': 'application/json'},
})

export const reissueAccessToken = async () => {
    const refreshToken = useAuthStore.getState().refreshToken
    if (!refreshToken) {
        throw new Error('refreshToken is missing')
    }
    const res = await publicClient.post('/user-service/v1/auth/reissue', {refreshToken})
    const tokens = res.data.data ?? res.data
    if (!tokens?.accessToken) {
        throw new Error('accessToken is missing')
    }
    useAuthStore.getState().setAccessToken(tokens.accessToken)
    if (tokens.refreshToken) {
        useAuthStore.getState().setRefreshToken(tokens.refreshToken)
    }
    return tokens.accessToken as string
}

client.interceptors.request.use((config) => {
    const store = useAuthStore.getState()
    store.syncUserFromAccessToken()
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
                const accessToken = await reissueAccessToken()
                const original = error.config as any
                if (original._retry) {
                    return Promise.reject(error)
                }
                original._retry = true
                original.headers = original.headers ?? {}
                original.headers.Authorization = `Bearer ${accessToken}`
                return client(original)
            } catch {
                useAuthStore.getState().logout()
            }
        }
        return Promise.reject(error)
    }
)

export default client
