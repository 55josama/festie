import client from './client'
import { unwrap, unwrapPage } from '../lib/api'
import type { Category, Comment, Post } from '../types'

export const getPosts = async (params: {
  categoryId?: string
  eventId?: string
  sort?: string
  page?: number
  size?: number
}) => {
  const res = await client.get('/community-service/v1/posts', { params })
  return unwrapPage<Post>(res.data)
}

export const getPost = async (postId: string) => {
  const res = await client.get(`/community-service/v1/posts/${postId}`)
  return unwrap<Post>(res.data)
}

export const createPost = async (data: {
  title: string
  content: string
  categoryId: string
  eventId?: string
  visibility?: 'PUBLIC' | 'PRIVATE'
}) => {
  const res = await client.post('/community-service/v1/posts', data)
  return unwrap<Post>(res.data)
}

export const getCategories = async () => {
  const res = await client.get('/community-service/v1/community-categories')
  return unwrapPage<Category>(res.data)
}

export const getComments = async (postId: string) => {
  const res = await client.get(`/community-service/v1/posts/${postId}/comments`)
  return unwrapPage<Comment>(res.data)
}

export const createComment = async (postId: string, content: string, parentId?: string | null) => {
  const res = await client.post(`/community-service/v1/posts/${postId}/comments`, { content, parentId })
  return unwrap<Comment>(res.data)
}

export const deleteComment = async (commentId: string) => {
  const res = await client.delete(`/community-service/v1/comments/${commentId}`)
  return unwrap<void>(res.data)
}

export const deletePost = async (postId: string) => {
  const res = await client.delete(`/community-service/v1/posts/${postId}`)
  return unwrap<void>(res.data)
}
