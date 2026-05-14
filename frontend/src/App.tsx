import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Events from './pages/Events'
import EventDetail from './pages/EventDetail'
import Community from './pages/Community'
import PostDetail from './pages/PostDetail'
import CommunityWrite from './pages/CommunityWrite'
import Calendar from './pages/Calendar'
import Admin from './pages/Admin'
import Login from './pages/Login'
import Register from './pages/Register'
import MyPage from './pages/MyPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 30,
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Home />} />
            <Route path="/events" element={<Events />} />
            <Route path="/events/:eventId" element={<EventDetail />} />
            <Route path="/calendar" element={<Calendar mode="all" />} />
            <Route path="/community" element={<Community />} />
            <Route path="/community/new" element={<CommunityWrite />} />
            <Route path="/community/:postId" element={<PostDetail />} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/my" element={<MyPage />} />
            <Route path="/my/calendars" element={<Calendar mode="mine" />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
