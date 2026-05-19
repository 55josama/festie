import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createComment, deleteComment, deletePost, getCategories, getComments, getPost, likePost, unlikePost } from '../api/community'
import ReportButton from '../components/ReportButton'
import { useAuthStore } from '../store/authStore'
import { formatRelativeTime } from '../lib/format'

export default function PostDetail() {
  const { postId = '' } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, isLoggedIn } = useAuthStore()
  const [content, setContent] = useState('')
  const [liked, setLiked] = useState(false)

  const postQuery = useQuery({
    queryKey: ['post', postId],
    queryFn: () => getPost(postId),
    enabled: !!postId,
  })

  const commentsQuery = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => getComments(postId),
    enabled: !!postId,
  })
  const categoriesQuery = useQuery({
    queryKey: ['categories', 'post-detail'],
    queryFn: getCategories,
  })
  const post = postQuery.data
  const comments = commentsQuery.data ?? []
  const categories = categoriesQuery.data ?? []
  const categoryNameById = new Map(categories.map((category) => [category.id, category.name]))
  const categoryLabel = categoryNameById.get(post?.categoryId ?? '') ?? post?.categoryName ?? '카테고리'
  const authorLabel =
    post?.authorNickname ??
    (user?.userId === post?.userId ? user?.nickname : undefined) ??
    '작성자'
  const parsedRequest = parseRequestContent(post?.content ?? '')
  const showRequestPreview =
    normalizePostCategoryKey(categoryLabel) === 'request' || parsedRequest.hasFields

  useEffect(() => {
    setLiked(Boolean(post?.liked))
  }, [post?.liked, post?.id])

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

  const likeMutation = useMutation({
    mutationFn: () => likePost(postId),
    onSuccess: async () => {
      setLiked(true)
      await queryClient.invalidateQueries({ queryKey: ['post', postId] })
      await queryClient.invalidateQueries({ queryKey: ['posts'] })
    },
  })

  const unlikeMutation = useMutation({
    mutationFn: () => unlikePost(postId),
    onSuccess: async () => {
      setLiked(false)
      await queryClient.invalidateQueries({ queryKey: ['post', postId] })
      await queryClient.invalidateQueries({ queryKey: ['posts'] })
    },
  })

  if (postQuery.isLoading) {
    return <div className="px-5 py-10 text-slate-500">게시글을 불러오는 중입니다.</div>
  }

  if (postQuery.isError || !post) {
    return (
      <div className="px-5 py-10">
        <div className="rounded-[24px] border border-[var(--line)] bg-white p-6 text-center shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="text-lg font-black text-slate-950">게시글을 찾을 수 없습니다</div>
          <p className="mt-2 text-sm text-slate-500">
            해당 글이 삭제됐거나, 현재 환경의 데이터와 맞지 않을 수 있어요.
          </p>
          <Link to="/community" className="mt-5 inline-flex rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white">
            커뮤니티로 돌아가기
          </Link>
        </div>
      </div>
    )
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
        <section className="space-y-6 rounded-[30px] border border-[var(--line)] bg-white p-6 md:p-8">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`rounded-full px-3.5 py-1.5 text-sm font-semibold ${categoryBadgeClass(categoryLabel)}`}>{categoryLabel}</span>
            {post.visibility === 'PRIVATE' && (
              <span className="rounded-full bg-slate-200 px-3.5 py-1.5 text-sm font-semibold text-slate-600">비공개</span>
            )}
            {post.eventName && <span className="rounded-full bg-slate-100 px-3.5 py-1.5 text-sm font-medium text-slate-600">{post.eventName}</span>}
          </div>

          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0 flex-1">
              <h1 className="text-[26px] font-black tracking-tight text-slate-950">{post.title}</h1>
              <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-slate-500">
                <span>{authorLabel}</span>
                <span>·</span>
                <span>{formatRelativeTime(post.createdAt)}</span>
                <span>·</span>
                <span>조회 {post.viewCount}</span>
                <span>좋아요 {post.likeCount}</span>
                <span>댓글 {post.commentCount}</span>
              </div>
            </div>
            <div className="flex shrink-0 flex-wrap items-center gap-2">
              <ReportButton targetType="POST" targetId={post.id} targetContent={post.content} />
              {canDelete && (
                <>
                  <Link
                    to={`/community/${post.id}/edit`}
                    className="inline-flex h-10 w-[64px] items-center justify-center rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
                  >
                    수정
                  </Link>
                  <button
                    onClick={() => deleteMutation.mutate()}
                    disabled={deleteMutation.isPending}
                    className="inline-flex h-10 w-[64px] items-center justify-center rounded-full border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-600 transition-colors hover:bg-rose-100 disabled:opacity-70"
                  >
                    {deleteMutation.isPending ? '삭제 중...' : '삭제'}
                  </button>
                </>
              )}
            </div>
          </div>

          <div className="border-b border-dashed border-slate-200 pb-5" />

          <div className="space-y-5">
            {showRequestPreview ? (
              <div className="space-y-4">
                {parsedRequest.intro && (
                  <div className="text-[16px] leading-7 text-slate-700 whitespace-pre-wrap break-words">
                    {parsedRequest.intro}
                  </div>
                )}
                <div className="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
                  <div className="grid gap-3 md:grid-cols-2">
                    {parsedRequest.eventName && <RequestField label="행사명" value={parsedRequest.eventName} />}
                    {parsedRequest.eventDate && <RequestField label="일정" value={parsedRequest.eventDate} />}
                    {parsedRequest.eventPlace && <RequestField label="장소" value={parsedRequest.eventPlace} />}
                    {parsedRequest.eventLink && (
                      <RequestField
                        label="링크"
                        value={parsedRequest.eventLink}
                        isLink
                      />
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-[16px] leading-7 text-slate-700 whitespace-pre-wrap break-words">
                {post.content}
              </div>
            )}

            <div className="flex justify-center py-1">
              <button
                type="button"
                onClick={() => {
                  if (!isLoggedIn()) return
                  if (liked) {
                    unlikeMutation.mutate()
                  } else {
                    likeMutation.mutate()
                  }
                }}
                disabled={likeMutation.isPending || unlikeMutation.isPending}
                className={`inline-flex items-center gap-2 rounded-full border px-4 py-2 text-sm font-semibold transition-colors ${
                  liked
                    ? 'border-rose-200 bg-rose-50 text-rose-600 hover:bg-rose-100'
                    : 'border-[var(--line)] bg-white text-slate-600 hover:bg-slate-50'
                }`}
                >
                <span>{liked ? '♥' : '♡'}</span>
                <span>좋아요</span>
                <span className="text-xs text-slate-400">{post.likeCount}</span>
              </button>
            </div>

            <div className="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
              <div className="text-sm font-semibold text-slate-500">댓글</div>
              <div className="mt-4 space-y-4">
                {comments.length ? (
                  comments.map((comment: any) => (
                    <CommentItem key={comment.id} comment={comment} postId={postId} currentUser={user} />
                  ))
                ) : (
                  <div className="text-sm text-slate-500">댓글이 없습니다</div>
                )}
              </div>

              <div className="mt-5 rounded-[22px] border border-slate-200 bg-white p-4">
                {isLoggedIn() ? (
                  <div className="space-y-3">
                    <textarea
                      value={content}
                      onChange={(e) => setContent(e.target.value)}
                      placeholder="댓글을 남겨보세요"
                      className="min-h-28 w-full rounded-[18px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none placeholder:text-slate-400"
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
            </div>
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

function CommentItem({
  comment,
  postId,
  currentUser,
  depth = 0,
}: {
  comment: any
  postId: string
  currentUser: any
  depth?: number
}) {
  const queryClient = useQueryClient()
  const [replyOpen, setReplyOpen] = useState(false)
  const [replyContent, setReplyContent] = useState('')
  const canDelete = currentUser?.userId === comment.userId || currentUser?.role === 'ADMIN'
  const writerLabel = currentUser?.userId === comment.userId ? currentUser?.nickname ?? '작성자' : '작성자'
  const canReply = depth === 0
  const deleteMutation = useMutation({
    mutationFn: () => deleteComment(comment.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    },
  })
  const replyMutation = useMutation({
    mutationFn: (content: string) => createComment(postId, content, comment.id),
    onSuccess: async () => {
      setReplyContent('')
      setReplyOpen(false)
      await queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    },
  })

  return (
    <div className="rounded-[20px] border border-slate-200 bg-white p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="text-sm font-semibold text-slate-900">{writerLabel}</div>
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <span>{formatRelativeTime(comment.createdAt)}</span>
          {currentUser && canReply && (
            <button
              type="button"
              onClick={() => setReplyOpen((prev) => !prev)}
              className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-medium text-slate-600"
            >
              답글
            </button>
          )}
          {currentUser && (
            <ReportButton targetType="COMMENT" targetId={comment.id} targetContent={comment.content} label="신고" className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-medium text-slate-600" />
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
      {replyOpen && currentUser && (
        <div className="mt-4 rounded-[18px] border border-slate-200 bg-slate-50 p-3">
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="답글을 남겨보세요"
            className="min-h-24 w-full rounded-[16px] border border-slate-200 bg-white p-3 text-sm outline-none placeholder:text-slate-400"
          />
          <div className="mt-3 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => {
                setReplyOpen(false)
                setReplyContent('')
              }}
              className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-medium text-slate-600"
            >
              취소
            </button>
            <button
              type="button"
              disabled={!replyContent.trim() || replyMutation.isPending}
              onClick={() => replyContent.trim() && replyMutation.mutate(replyContent.trim())}
              className="rounded-full bg-[var(--accent)] px-3 py-1.5 text-[11px] font-semibold text-white disabled:opacity-70"
            >
              답글 등록
            </button>
          </div>
        </div>
      )}
      {comment.replies?.length ? (
        <div className="mt-4 space-y-3 border-l border-slate-200 pl-4">
          {comment.replies.map((reply: any) => (
            <CommentItem key={reply.id} comment={reply} postId={postId} currentUser={currentUser} depth={depth + 1} />
          ))}
        </div>
      ) : null}
    </div>
  )
}

function normalizePostCategoryKey(name: string) {
  const normalized = String(name ?? '').trim().toLowerCase()
  if (!normalized) return ''
  if (normalized === '\uD6C4\uAE30' || normalized === 'review') return 'review'
  if (normalized === '\uAFC0\uD301' || normalized === 'tip') return 'tip'
  if (normalized === '\uC790\uC720' || normalized === 'free') return 'free'
  if (normalized === '\uC694\uCCAD' || normalized === 'request') return 'request'
  return normalized
}

function parseRequestContent(content: string) {
  const lines = String(content ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)

  const fields = {
    eventName: '',
    eventDate: '',
    eventPlace: '',
    eventLink: '',
  }

  const introLines: string[] = []

  for (const line of lines) {
    const [label, ...rest] = line.split(':')
    const value = rest.join(':').trim()
    const normalizedLabel = label.trim()

    if (!value) {
      introLines.push(line)
      continue
    }

    if (normalizedLabel === '행사명') {
      fields.eventName = value
      continue
    }
    if (normalizedLabel === '일정') {
      fields.eventDate = value
      continue
    }
    if (normalizedLabel === '장소') {
      fields.eventPlace = value
      continue
    }
    if (normalizedLabel === '링크') {
      fields.eventLink = value
      continue
    }

    introLines.push(line)
  }

  return {
    ...fields,
    intro: introLines.join('\n'),
    hasFields: Boolean(fields.eventName || fields.eventDate || fields.eventPlace || fields.eventLink),
  }
}

function RequestField({ label, value, isLink = false }: { label: string; value: string; isLink?: boolean }) {
  return (
    <div className="rounded-[18px] border border-slate-200 bg-white px-4 py-3">
      <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-400">{label}</div>
      {isLink ? (
        <a href={value} target="_blank" rel="noreferrer" className="mt-1 block break-all text-sm text-[var(--accent)] underline decoration-[var(--accent-soft)] decoration-2 underline-offset-4">
          {value}
        </a>
      ) : (
        <div className="mt-1 break-words text-sm leading-6 text-slate-700">{value}</div>
      )}
    </div>
  )
}
