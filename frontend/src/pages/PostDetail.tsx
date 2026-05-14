import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createComment, deleteComment, deletePost, getComments, getPost } from '../api/community'
import ReportButton from '../components/ReportButton'
import { useAuthStore } from '../store/authStore'
import { formatRelativeTime } from '../lib/format'

export default function PostDetail() {
  const { postId = '' } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, isLoggedIn } = useAuthStore()
  const [content, setContent] = useState('')

  const { data: post } = useQuery({
    queryKey: ['post', postId],
    queryFn: () => getPost(postId),
    enabled: !!postId,
  })

  const { data: comments = [] } = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => getComments(postId),
    enabled: !!postId,
  })

  const mutation = useMutation({
    mutationFn: (text: string) => createComment(postId, text),
    onSuccess: async () => {
      setContent('')
      await queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => deletePost(postId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['posts'] })
      await queryClient.invalidateQueries({ queryKey: ['post', postId] })
      navigate('/community')
    },
  })

  if (!post) {
    return <div className="px-5 py-10 text-slate-500">게시글을 불러오는 중입니다.</div>
  }

  const canDelete = user?.userId === post.userId || user?.role === 'ADMIN'

  return (
    <div className="px-5 py-6 md:px-8 md:py-8">
      <div className="mb-4 flex items-center gap-2 text-sm text-slate-500">
        <Link to="/community" className="hover:text-slate-900">커뮤니티</Link>
        <span>/</span>
        <span className="text-slate-900">게시글 상세</span>
      </div>

      <div className="grid gap-6 xl:grid-cols-1">
        <section className="space-y-6 rounded-[30px] border border-[var(--line)] bg-white p-6 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-8">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`rounded-full px-3 py-1 text-xs font-semibold ${categoryBadgeClass(post.categoryName)}`}>{post.categoryName}</span>
            {post.visibility === 'PRIVATE' && (
              <span className="rounded-full bg-slate-200 px-3 py-1 text-xs font-semibold text-slate-600">비공개</span>
            )}
            {post.eventName && <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600">{post.eventName}</span>}
          </div>

          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0 flex-1">
              <h1 className="text-[26px] font-black tracking-tight text-slate-950">{post.title}</h1>
              <div className="mt-3 flex flex-wrap items-center gap-2 text-sm text-slate-500">
                <span>{post.authorNickname ?? '익명'}</span>
                <span>·</span>
                <span>{formatRelativeTime(post.createdAt)}</span>
                <span>·</span>
                <span>조회 {post.viewCount}</span>
                <span>좋아요 {post.likeCount}</span>
                <span>댓글 {post.commentCount}</span>
              </div>
            </div>

            <div className="flex shrink-0 flex-wrap items-center gap-2">
              <ReportButton targetType="POST" targetId={post.id} />
            </div>
          </div>

          {canDelete && (
            <div className="flex justify-end">
              <button
                onClick={() => deleteMutation.mutate()}
                disabled={deleteMutation.isPending}
                className="rounded-full border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-600 transition-colors hover:bg-rose-100 disabled:opacity-70"
              >
                {deleteMutation.isPending ? '삭제 중...' : '게시글 삭제'}
              </button>
            </div>
          )}

          <div className="rounded-[24px] p-5 text-[15px] leading-7 text-slate-700">
            {post.content}
          </div>

          <div className="rounded-[24px] border border-[var(--line)] bg-slate-50 p-5">
            <div className="text-sm font-semibold text-slate-500">댓글</div>
            <div className="mt-4 space-y-4">
              {comments.map((comment: any) => (
                <CommentItem key={comment.id} comment={comment} postId={postId} currentUser={user} />
              ))}
            </div>
          </div>

          <div className="rounded-[24px] border border-[var(--line)] bg-white p-5">
            {isLoggedIn() ? (
              <div className="space-y-3">
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="댓글을 남겨보세요"
                  className="min-h-28 w-full rounded-[20px] border border-[var(--line)] bg-slate-50 p-4 text-sm outline-none"
                />
                <div className="flex justify-end">
                  <button
                    onClick={() => content.trim() && mutation.mutate(content)}
                    className="rounded-full bg-[var(--accent)] px-5 py-3 text-sm font-semibold text-white"
                  >
                    댓글 등록
                  </button>
                </div>
              </div>
            ) : (
              <div className="text-sm text-slate-500">
                <Link to="/login" className="font-semibold text-[var(--accent)]">로그인</Link> 후 댓글을 작성할 수 있어요.
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}

function categoryBadgeClass(name: string) {
  return (
    {
      review: 'bg-violet-100 text-violet-700',
      tip: 'bg-sky-100 text-sky-700',
      free: 'bg-emerald-100 text-emerald-700',
      request: 'bg-rose-100 text-rose-700',
    }[normalizePostCategoryKey(name)] ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'
  )
}

function CommentItem({ comment, postId, currentUser }: { comment: any; postId: string; currentUser: any }) {
  const queryClient = useQueryClient()
  const canDelete = currentUser?.userId === comment.userId || currentUser?.role === 'ADMIN'
  const deleteMutation = useMutation({
    mutationFn: () => deleteComment(comment.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    },
  })

  return (
    <div className="rounded-[20px] bg-white p-4 ring-1 ring-[var(--line)]">
      <div className="flex items-center justify-between gap-3">
        <div className="text-sm font-semibold text-slate-900">{comment.userId}</div>
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <span>{formatRelativeTime(comment.createdAt)}</span>
          {currentUser && (
            <ReportButton targetType="COMMENT" targetId={comment.id} label="신고" className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-medium text-slate-600" />
          )}
          {canDelete && (
            <button
              type="button"
              onClick={() => deleteMutation.mutate()}
              disabled={deleteMutation.isPending}
              className="rounded-full border border-rose-200 bg-rose-50 px-3 py-1.5 text-[11px] font-semibold text-rose-600 disabled:opacity-70"
            >
              {deleteMutation.isPending ? '삭제 중...' : '삭제'}
            </button>
          )}
        </div>
      </div>
      <div className="mt-2 text-sm leading-6 text-slate-700">{comment.content}</div>
      {comment.replies?.length ? (
        <div className="mt-4 space-y-3 border-l border-slate-200 pl-4">
          {comment.replies.map((reply: any) => (
            <CommentItem key={reply.id} comment={reply} postId={postId} currentUser={currentUser} />
          ))}
        </div>
      ) : null}
    </div>
  )
}

function normalizePostCategoryKey(name: string) {
  if (name === '\uD6C4\uAE30') return 'review'
  if (name === '\uAFC0\uD301') return 'tip'
  if (name === '\uC790\uC720') return 'free'
  if (name === '\uC694\uCCAD') return 'request'
  return name.toLowerCase()
}
