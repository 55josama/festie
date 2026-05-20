import client, { publicClient } from './client'
import { unwrap, unwrapPage, unwrapPageResponse } from '../lib/api'
import type { Category, Comment, Post } from '../types'

export const getPosts = async (params: {
  categoryId?: string
  eventId?: string
  sort?: string
  page?: number
  size?: number
  mine?: boolean
}) => {
  const { mine, ...query } = params
  const res = mine
    ? await client.get('/community-service/v1/posts', { params: query })
    : await publicClient.get('/community-service/v1/posts', { params: query })
  return unwrapPage<Post>(res.data)
}

export const getPostsPage = async (params: {
  categoryId?: string
  eventId?: string
  sort?: string
  page?: number
  size?: number
  mine?: boolean
}) => {
  const { mine, ...query } = params
  const res = mine
    ? await client.get('/community-service/v1/posts', { params: query })
    : await publicClient.get('/community-service/v1/posts', { params: query })
  return unwrapPageResponse<Post>(res.data)
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

export const updatePost = async (
  postId: string,
  data: {
    title: string
    content: string
    categoryId: string
    visibility?: 'PUBLIC' | 'PRIVATE'
  }
) => {
  const res = await client.patch(`/community-service/v1/posts/${postId}`, data)
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

export const likePost = async (postId: string) => {
  const res = await client.post(`/community-service/v1/posts/${postId}/likes`)
  return unwrap<void>(res.data)
}

export const unlikePost = async (postId: string) => {
  const res = await client.delete(`/community-service/v1/posts/${postId}/likes`)
  return unwrap<void>(res.data)
}
