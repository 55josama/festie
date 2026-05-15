import { type ReactNode, useMemo, useState } from 'react'
import {Link} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {getCategories, getPosts} from '../api/community'
import PostCard from '../components/PostCard'
import { useAuthStore } from '../store/authStore'

const CATEGORY_ADMIN_LINK = '/admin?tab=requests&panel=categories'

export default function Community() {
    const { user } = useAuthStore()
    const [categoryId, setCategoryId] = useState<string | undefined>()
    const [sort, setSort] = useState<'latest' | 'popular'>('latest')
    const isAdmin = !!user && /ADMIN/.test(user.role)
    const {data: categories = []} = useQuery({queryKey: ['categories'], queryFn: getCategories})
    const {data: rawPosts = []} = useQuery({
        queryKey: ['posts', categoryId, sort],
        queryFn: () => getPosts({categoryId, sort: sort === 'popular' ? 'likeCount,desc' : 'createdAt,desc', size: 100}),
    })

    const categoryNameById = useMemo(() => {
        return new Map(categories.map((category: any) => [category.id, category.name]))
    }, [categories])

    const posts = useMemo(() => {
        if (sort === 'latest') return rawPosts
        const formatter = new Intl.DateTimeFormat('ko-KR', { timeZone: 'Asia/Seoul' })
        const today = formatter.format(new Date())
        const todayPosts = [...rawPosts]
            .filter((post: any) => {
                const createdAt = new Date(post.createdAt)
                if (Number.isNaN(createdAt.getTime())) return false
                return formatter.format(createdAt) === today
            })
            .sort((a: any, b: any) => b.likeCount - a.likeCount)
        return todayPosts.length ? todayPosts : [...rawPosts].sort((a: any, b: any) => b.likeCount - a.likeCount)
    }, [rawPosts, sort])
    return (
        <div className="space-y-5 px-5 py-5 md:px-8 md:py-7">
            <section
                className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
                <div className="max-w-3xl">
                <div
                        className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
                        커뮤니티
                    </div>
                    <h1 className="mt-3 text-[22px] font-black tracking-tight text-slate-950 md:text-[28px]">
                        다녀온 사람의 경험이 가장 정확한 정보
                    </h1>
                    <p className="mt-2 hidden max-w-2xl text-sm leading-6 text-slate-600 md:block">
                        후기, 꿀팁, 자유 글을 하나의 피드로 읽고 행사별 이야기까지 빠르게 확인하세요.
                    </p>
                </div>
            </section>

            <section
                className="rounded-[24px] border border-[var(--line)] bg-white p-3 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-4">
                <div className="flex flex-col gap-2.5 lg:flex-row lg:items-center lg:justify-between">
                    <div className="flex flex-wrap items-center gap-1.5">
                        <span className="text-[10px] font-semibold text-slate-500">카테고리</span>
                        <FilterChip active={!categoryId} tone="violet"
                                    onClick={() => setCategoryId(undefined)}>전체</FilterChip>
                        {categories.map((category: any) => (
                            <FilterChip
                                key={category.id}
                                active={categoryId === category.id}
                                tone={categoryTone(category.name)}
                                onClick={() => setCategoryId(category.id)}
                            >
                                {category.name}
                            </FilterChip>
                        ))}
                        {isAdmin && (
                            <Link
                                to={CATEGORY_ADMIN_LINK}
                                aria-label="카테고리 관리로 이동"
                                className="inline-flex items-center justify-center rounded-full border border-[var(--line)] bg-white px-2 py-1 text-[10px] font-semibold text-slate-500 transition-colors hover:bg-slate-50 md:px-2.5 md:py-1.5 md:text-[11px]"
                            >
                                +
                            </Link>
                        )}
                    </div>

                    <div className="flex flex-wrap items-center gap-1.5">
                        <span className="text-[10px] font-semibold text-slate-500">정렬</span>
                        <FilterChip active={sort === 'latest'} tone="sky"
                                    onClick={() => setSort('latest')}>최신글</FilterChip>
                        <FilterChip active={sort === 'popular'} tone="sky"
                                    onClick={() => setSort('popular')}>인기글</FilterChip>
                    </div>
                </div>
            </section>

            <section
                className="space-y-3 rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
                <div className="flex items-center justify-between gap-3">
                    <div>
                        <h2 className="text-[18px] font-black tracking-tight text-slate-950">{sort === 'popular' ? '오늘 인기글' : '최신글'}</h2>
                        <p className="mt-1 hidden text-xs text-slate-500 md:block">
                            {sort === 'popular'
                                ? '오늘 올라온 글 가운데 좋아요가 많은 글을 보고 있어요.'
                                : `${categoryId ? `${categoryNameById.get(categoryId) ?? '선택한'} 카테고리` : '모든 카테고리'} 글을 보고 있어요.`}
                        </p>
                    </div>
                    <Link
                        to="/community/new"
                        className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] hover:bg-white"
                    >
                        글쓰기
                    </Link>
                </div>

                <div className="space-y-3">
                    {posts.map((post: any) => (
                        <PostCard
                          key={post.id}
                          post={post}
                          categoryLabel={categoryNameById.get(post.categoryId) ?? post.categoryName}
                        />
                    ))}
                </div>

            </section>
        </div>
    )
}

function FilterChip({active, tone = 'violet', onClick, children}: {
    active?: boolean;
    tone?: 'violet' | 'sky' | 'emerald' | 'rose' | 'slate';
    onClick: () => void;
    children: ReactNode
}) {
    const toneClass = {
        violet: 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)]',
        sky: 'border-sky-200 bg-sky-50 text-sky-700',
        emerald: 'border-emerald-200 bg-emerald-50 text-emerald-700',
        rose: 'border-rose-200 bg-rose-50 text-rose-700',
        slate: 'border-slate-200 bg-slate-50 text-slate-700',
    }[tone]

    return (
        <button
            onClick={onClick}
            className={`rounded-full border px-2 py-1 text-[10px] font-semibold transition-colors md:px-2.5 md:py-1.5 md:text-[11px] ${
                active ? toneClass : 'border-[var(--line)] bg-white text-slate-600 hover:bg-slate-50'
            }`}
        >
            {children}
        </button>
    )
}

function categoryTone(name: string) {
    return (
        {
            review: 'violet',
            tip: 'sky',
            free: 'emerald',
            request: 'rose',
        }[normalizeCategoryKey(name)] ?? 'violet'
    ) as 'violet' | 'sky' | 'emerald' | 'rose' | 'slate'
}

function normalizeCategoryKey(name: string) {
    const normalized = String(name ?? '').trim().toLowerCase()
    if (normalized === '후기' || normalized === 'review') return 'review'
    if (normalized === '꿀팁' || normalized === 'tip') return 'tip'
    if (normalized === '자유' || normalized === 'free') return 'free'
    if (normalized === '요청' || normalized === 'request') return 'request'
    return normalized
}
