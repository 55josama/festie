/**
 * Community Service 부하테스트
 *
 * 실행 방법:
 *   # 시드 생성 (최초 1회) — 사용자 50명 / 카테고리 3개 / 게시글 1000개
 *   node loadtest/seed/community-seed.js
 *
 *   # 시나리오별 실행
 *   k6 run --env SCENARIO=list    loadtest/k6/community-loadtest.js  # 목록 조회 (인덱스 성능)
 *   k6 run --env SCENARIO=detail  loadtest/k6/community-loadtest.js  # 상세 조회 (Redis 조회수)
 *   k6 run --env SCENARIO=write   loadtest/k6/community-loadtest.js  # 글쓰기 (인증 필요)
 *   k6 run --env SCENARIO=stress  loadtest/k6/community-loadtest.js  # 복합 스트레스 (기존 동일 조건)
 *
 * 환경변수:
 *   SCENARIO      실행할 시나리오 (list|detail|write|stress), 기본 stress
 *   SEED_FILE     시드 파일 경로, 기본 loadtest/seed/seed_data.json
 *   COMMUNITY_URL 서비스 URL, 기본 http://localhost:9001 (Docker 포트 동일)
 *   HOT_MODE      detail 시나리오에서 핫스팟 집중 여부 (true|false)
 *
 * stress 시나리오 VU 플랜 (기존 동일):
 *   0→50 VU  (30s 램프업) → 50→100 VU (1m) → 100→200 VU (1m 정점) → 200→0 VU (30s 다운)
 *   max VUs: 200 / total: 3m / gracefulStop: 30s → 총 3m30s
 */

import http    from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

// ── 시드 데이터 ───────────────────────────────────────────────────────────────
const SEED_FILE = __ENV.SEED_FILE || 'loadtest/seed/seed_data.json';
const seed      = JSON.parse(open(SEED_FILE));

const BASE_URL  = __ENV.COMMUNITY_URL || seed.communityUrl || 'http://localhost:9001';
const SCENARIO  = (__ENV.SCENARIO || 'stress').toLowerCase();
const HOT_MODE  = (__ENV.HOT_MODE || 'false').toLowerCase() === 'true';

const users               = seed.users               || [];
const communityCategories = seed.communityCategories || [];
const posts               = seed.posts               || [];
const hotPosts            = seed.hotPosts            || posts.slice(0, 5);
const adminId             = seed.adminUserId;

// ── 커스텀 메트릭 ─────────────────────────────────────────────────────────────
const listDuration   = new Trend('community_list_duration',   true);
const detailDuration = new Trend('community_detail_duration', true);
const writeDuration  = new Trend('community_write_duration',  true);
const commentDuration= new Trend('community_comment_duration',true);
const likeDuration   = new Trend('community_like_duration',   true);

const successCount   = new Counter('community_success');
const failCount      = new Counter('community_fail');
const authFailRate   = new Rate('community_auth_fail_rate');

// ── 시나리오 정의 ─────────────────────────────────────────────────────────────
const scenarioConfigs = {
    // 목록 조회 — 인덱스 + Page/COUNT 성능 확인
    list: {
        executor: 'ramping-vus',
        startVUs: 1,
        stages: [
            { duration: '30s', target: Number(__ENV.TARGET_VUS || 30) },
            { duration: '1m',  target: Number(__ENV.TARGET_VUS || 30) },
            { duration: '20s', target: 0 },
        ],
        gracefulRampDown: '10s',
        exec: 'listScenario',
    },
    // 상세 조회 — Redis Write-Behind 조회수 부하
    detail: {
        executor: 'ramping-vus',
        startVUs: 1,
        stages: [
            { duration: '30s', target: Number(__ENV.TARGET_VUS || 50) },
            { duration: '2m',  target: Number(__ENV.TARGET_VUS || 50) },
            { duration: '20s', target: 0 },
        ],
        gracefulRampDown: '10s',
        exec: 'detailScenario',
    },
    // 글쓰기 — 인증 + DB 쓰기 부하
    write: {
        executor: 'ramping-vus',
        startVUs: 1,
        stages: [
            { duration: '20s', target: Number(__ENV.TARGET_VUS || 10) },
            { duration: '1m',  target: Number(__ENV.TARGET_VUS || 10) },
            { duration: '20s', target: 0 },
        ],
        gracefulRampDown: '10s',
        exec: 'writeScenario',
    },
    // 복합 스트레스 — 기존 동일 조건
    // 0→50 VU (30s) → 50→100 VU (1m) → 100→200 VU (1m 정점) → 200→0 VU (30s)
    // 총 3m, gracefulStop 30s → k6 표시: 3m30s max duration
    stress: {
        executor: 'ramping-vus',
        startVUs: 1,
        stages: [
            { duration: '30s', target: 50  },   // 램프업
            { duration: '1m',  target: 100 },   // 증가
            { duration: '1m',  target: 200 },   // 정점
            { duration: '30s', target: 0   },   // 램프다운
        ],
        gracefulRampDown: '30s',
        exec: 'stressScenario',
    },
};

export const options = {
    scenarios: {
        [SCENARIO]: scenarioConfigs[SCENARIO] || scenarioConfigs.stress,
    },
    // p(99) 를 포함한 Trend 통계 수집 — 없으면 handleSummary 에서 n/a 로 뜸
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)', 'count'],
    thresholds: {
        http_req_failed:            ['rate<0.01'],
        community_list_duration:    ['p(95)<500'],
        community_detail_duration:  ['p(95)<200'],
        community_write_duration:   ['p(95)<1000'],
        community_auth_fail_rate:   ['rate<0.001'],
    },
};

// ── 헬퍼 ─────────────────────────────────────────────────────────────────────
function safeJson(res) {
    try { return JSON.parse(res.body || ''); } catch { return null; }
}

function unwrap(res) {
    const parsed = safeJson(res);
    return parsed?.data ?? parsed ?? null;
}

/** X-User-Id / X-User-Role 헤더 반환 (Gateway 없이 서비스 직접 호출) */
function authHeader(userId, role = 'USER') {
    return {
        'X-User-Id':   userId,
        'X-User-Role': role,
        'Content-Type': 'application/json',
    };
}

function jsonHeader() {
    return { 'Content-Type': 'application/json' };
}

function pickRandom(arr) {
    if (!arr.length) throw new Error('빈 배열에서 선택 불가');
    return arr[Math.floor(Math.random() * arr.length)];
}

function pickUser() { return pickRandom(users); }

function pickPost() {
    // HOT_MODE: 핫스팟 게시글에 80% 집중 (Redis 한 키에 부하 집중 시나리오)
    if (HOT_MODE && hotPosts.length && Math.random() < 0.8) {
        return pickRandom(hotPosts);
    }
    return pickRandom(posts);
}

function thinkTime() { sleep(Math.random() * 0.3 + 0.1); }

// ── 시나리오 함수 ─────────────────────────────────────────────────────────────

/**
 * 목록 조회 시나리오
 * - 전체 목록 / 카테고리 필터 / 유저 글 목록 혼합
 * - 인덱스 성능 + COUNT(*) 비용 측정
 */
export function listScenario() {
    const roll = Math.random();
    let url;

    if (roll < 0.5) {
        // 50%: 전체 목록
        url = `${BASE_URL}/v1/posts?page=0&size=20&sort=createdAt,desc`;
    } else if (roll < 0.8 && communityCategories.length) {
        // 30%: 카테고리별 필터
        const cat = pickRandom(communityCategories);
        url = `${BASE_URL}/v1/posts?categoryId=${cat.id}&page=0&size=20&sort=createdAt,desc`;
    } else if (users.length) {
        // 20%: 특정 유저의 글 목록
        const user = pickUser();
        url = `${BASE_URL}/v1/posts?userId=${user.userId}&page=0&size=20&sort=createdAt,desc`;
    } else {
        url = `${BASE_URL}/v1/posts?page=0&size=20`;
    }

    const start = Date.now();
    const res   = http.get(url, { tags: { api: 'post_list' } });
    listDuration.add(Date.now() - start);

    const ok = check(res, {
        'list status 200':      r => r.status === 200,
        'list has content':     r => Array.isArray(unwrap(r)?.content),
        'list has totalPages':  r => unwrap(r)?.totalPages !== undefined,
    });
    ok ? successCount.add(1) : failCount.add(1);

    thinkTime();
}

/**
 * 상세 조회 시나리오
 * - Redis INCR 부하 측정
 * - HOT_MODE=true 시 동일 게시글에 집중 → Redis 단일 키 경합 테스트
 */
export function detailScenario() {
    if (!posts.length) return;

    const post = pickPost();
    const start = Date.now();
    const res   = http.get(
        `${BASE_URL}/v1/posts/${post.postId}`,
        { tags: { api: 'post_detail' } }
    );
    detailDuration.add(Date.now() - start);

    const ok = check(res, {
        'detail status 200':    r => r.status === 200,
        'detail has postId':    r => unwrap(r)?.id !== undefined,
        'detail has viewCount': r => typeof unwrap(r)?.viewCount === 'number',
    });
    ok ? successCount.add(1) : failCount.add(1);

    thinkTime();
}

/**
 * 글쓰기 시나리오
 * - 게시글 작성 + 댓글 + 좋아요 순서대로 실행
 * - 인증 헤더 포함 (HeaderAuthenticationFilter 검증)
 */
export function writeScenario() {
    if (!users.length || !communityCategories.length) return;

    const user     = pickUser();
    const category = pickRandom(communityCategories);
    const headers  = authHeader(user.userId, 'USER');

    // 1. 게시글 작성
    const writeStart = Date.now();
    const writeRes = http.post(
        `${BASE_URL}/v1/posts`,
        JSON.stringify({
            categoryId: category.id,
            title:      `k6 테스트 게시글 ${Date.now()}`,
            content:    '부하테스트 중 작성된 게시글입니다.',
        }),
        { headers, tags: { api: 'post_write' } }
    );
    writeDuration.add(Date.now() - writeStart);

    const writeOk = check(writeRes, {
        'write status 201':    r => r.status === 201,
        'write has postId':    r => unwrap(r)?.id !== undefined,
        'write not 401/403':   r => r.status !== 401 && r.status !== 403,
    });
    authFailRate.add(writeRes.status === 401 || writeRes.status === 403 ? 1 : 0);
    if (!writeOk) { failCount.add(1); return; }
    successCount.add(1);

    const newPostId = unwrap(writeRes)?.id;
    if (!newPostId) return;

    thinkTime();

    // 2. 댓글 작성
    const commentStart = Date.now();
    const commentRes = http.post(
        `${BASE_URL}/v1/posts/${newPostId}/comments`,
        JSON.stringify({ content: 'k6 테스트 댓글입니다.' }),
        { headers, tags: { api: 'comment_write' } }
    );
    commentDuration.add(Date.now() - commentStart);

    check(commentRes, {
        'comment status 201': r => r.status === 201,
        'comment not 401':    r => r.status !== 401,
    }) ? successCount.add(1) : failCount.add(1);

    thinkTime();

    // 3. 좋아요 — VU 번호 + iteration 으로 분산하여 중복 최소화
    // posts.find() 는 항상 같은 첫 번째 글을 반환 → 중복 좋아요 폭발
    const otherPosts = posts.filter(p => p.userId !== user.userId);
    if (otherPosts.length) {
        // VU 별로 다른 오프셋 사용 → 동일 글 좋아요 중복 방지
        const likeTarget = otherPosts[(__VU * 7 + __ITER) % otherPosts.length];
        const likeStart = Date.now();
        const likeRes = http.post(
            `${BASE_URL}/v1/posts/${likeTarget.postId}/likes`,
            null,
            { headers, tags: { api: 'post_like' } }
        );
        likeDuration.add(Date.now() - likeStart);

        // 409 = 이미 좋아요 (정상 비즈니스 케이스) — 실패로 집계하지 않음
        check(likeRes, {
            'like not 401/500': r => r.status !== 401 && r.status !== 500,
        }) ? successCount.add(1) : failCount.add(1);
    }

    thinkTime();
}

/**
 * 복합 스트레스 시나리오
 * - 실제 커뮤니티 트래픽 패턴 시뮬레이션
 * - 읽기 70% / 상세 조회 20% / 쓰기 10%
 */
export function stressScenario() {
    const roll = Math.random();

    if (roll < 0.45) {
        // 45%: 목록 조회
        listScenario();
    } else if (roll < 0.70) {
        // 25%: 상세 조회 (Redis 조회수 증가)
        detailScenario();
    } else if (roll < 0.85) {
        // 15%: 목록 → 상세 → 댓글 목록 (실제 사용자 흐름)
        listScenario();
        if (posts.length) {
            const post = pickPost();
            const res  = http.get(
                `${BASE_URL}/v1/posts/${post.postId}`,
                { tags: { api: 'post_detail_flow' } }
            );
            check(res, { 'flow detail 200': r => r.status === 200 })
                ? successCount.add(1) : failCount.add(1);

            thinkTime();

            const commentRes = http.get(
                `${BASE_URL}/v1/posts/${post.postId}/comments?page=0&size=20`,
                { tags: { api: 'comment_list_flow' } }
            );
            check(commentRes, { 'flow comments 200': r => r.status === 200 })
                ? successCount.add(1) : failCount.add(1);
        }
    } else {
        // 15%: 인증 필요한 쓰기 작업
        writeScenario();
    }
}

// ── 커스텀 요약 리포트 ────────────────────────────────────────────────────────
export function handleSummary(data) {
    const m = data.metrics;

    function val(metric, stat) {
        return m[metric]?.values?.[stat] ?? null;
    }
    function ms(v) {
        return v === null ? 'n/a' : v.toFixed(2) + ' ms';
    }
    function pct(v) {
        return v === null ? 'n/a' : (v * 100).toFixed(2) + '%';
    }
    function num(v) {
        return v === null ? 'n/a' : Math.round(v).toString();
    }

    const totalReqs  = val('http_reqs',          'count');
    const failRate   = val('http_req_failed',     'rate');
    const avgDur     = val('http_req_duration',   'avg');
    const p95Dur     = val('http_req_duration',   'p(95)');
    const p99Dur     = val('http_req_duration',   'p(99)');
    const maxRps     = val('http_reqs',           'rate');

    const listP95    = val('community_list_duration',   'p(95)');
    const detailP95  = val('community_detail_duration', 'p(95)');
    // Counter 메트릭은 values.count 가 아니라 values.value 에 있음
    // Trend 의 count 는 summaryTrendStats 에 'count' 추가해야 포함됨
    const writeCount = val('community_write_duration',  'count');
    const likeCount  = val('community_like_duration',   'count');
    // 성공/실패 Counter
    const successTotal = val('community_success', 'count') ?? m['community_success']?.values?.value ?? null;
    const failTotal    = val('community_fail',    'count') ?? m['community_fail']?.values?.value    ?? null;

    const W  = 56; // 박스 너비
    const HR = '─'.repeat(W);
    const title = ` community-service 부하 테스트 결과 [${SCENARIO}] `;
    const pad   = Math.max(0, W - title.length);
    const lp    = Math.floor(pad / 2);
    const rp    = pad - lp;

    function row(label, value) {
        const dots = '.'.repeat(Math.max(1, W - label.length - value.length - 2));
        return `  ${label} ${dots} ${value}`;
    }

    const lines = [
        '',
        '━'.repeat(lp) + title + '━'.repeat(rp),
        row('총 요청 수',       num(totalReqs)   + ' 건'),
        row('실패율',           pct(failRate)),
        row('평균 응답시간',    ms(avgDur)),
        row('p95 응답시간',     ms(p95Dur)),
        row('p99 응답시간',     ms(p99Dur)),
        row('평균 RPS',         maxRps === null ? 'n/a' : maxRps.toFixed(2) + ' req/s'),
        HR,
        row('목록 조회 p95',    ms(listP95)),
        row('상세 조회 p95',    ms(detailP95)),
        row('글쓰기 p95',       ms(val('community_write_duration', 'p(95)'))),
        row('글쓰기 요청 수',   num(writeCount)  + ' 건'),
        row('좋아요 요청 수',   num(likeCount)   + ' 건'),
        row('성공 체크 수',     num(successTotal) + ' 건'),
        '━'.repeat(W),
        '',
    ];

    console.log(lines.join('\n'));

    // 기본 k6 JSON 요약도 파일로 저장 (선택)
    return {
        stdout: '\n',   // 기본 출력은 비움 (위 console.log 로 대체)
    };
}
