import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { SharedArray } from 'k6/data';
import { randomIntBetween, randomString, uuidv4 } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// ─── 시드 데이터 로드 (init 단계, 1회) ────────────────────────
//   seed_community.py 가 만든 JSON 을 읽어서 user_ids / category_ids 풀로 활용.
//   파일 없으면 단일 랜덤 UUID fallback (smoke 용).
const SEED_FILE = __ENV.SEED_FILE || '../seed/seed_data.json';
let seedData = null;
try {
    seedData = JSON.parse(open(SEED_FILE));
    console.log(`[init] 시드 로드 완료 → users=${seedData.user_ids.length}, categories=${seedData.category_ids.length}, posts=${seedData.post_count}`);
} catch (e) {
    console.warn(`[init] 시드 파일(${SEED_FILE}) 없음 — fallback 모드 (사용자/카테고리 단일값).`);
}

// SharedArray: VU 간 메모리 공유 (대용량일 때 메모리 절약)
const userIdPool = new SharedArray('user_ids', () => seedData?.user_ids || []);
const categoryIdPool = new SharedArray('category_ids', () => seedData?.category_ids || []);

function pickRandom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

// ─── 환경 변수 / 엔드포인트 설정 ───────────────────────────────
const BASE_URL = __ENV.BASE_URL || 'http://localhost:9001';      // community-service
const AUTH_URL = __ENV.AUTH_URL || 'http://localhost:8081';      // user-service (JWT 발급)
const SCENARIO = __ENV.SCENARIO || 'load';
const POST_RATIO = Number(__ENV.POST_RATIO ?? 0.1);              // POST /v1/posts 트리거 확률 (smoke 디버깅 시 1.0 로)
const LIKE_RATIO = Number(__ENV.LIKE_RATIO ?? 0.3);              // POST /v1/posts/{id}/likes 트리거 확률
const DETAIL_RATIO = Number(__ENV.DETAIL_RATIO ?? 0.5);          // GET /v1/posts/{id} 트리거 확률 (N+1 의심 케이스)

// ─── 사용자 정의 메트릭 (Grafana 에서 따로 그래프 그릴 수 있음) ──
const errorRate         = new Rate('custom_errors');
const postCreated       = new Counter('post_created_total');
const postLiked         = new Counter('post_liked_total');
const postListLatency   = new Trend('post_list_latency_ms', true);
const postDetailLatency = new Trend('post_detail_latency_ms', true);

// ─── 시나리오 프리셋 ─────────────────────────────────────────
//  smoke  : 기능 검증 (1명, 30초)
//  load   : 일반 부하 (50명까지 ramp-up)
//  stress : 한계 탐색 (200명까지)
//  spike  : 순간 폭주 (10 → 300 → 10)
const scenarios = {
    smoke: {
        executor: 'constant-vus',
        vus: 1,
        duration: '30s',
    },
    load: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
            { duration: '30s', target: 20 },   // ramp-up
            { duration: '1m',  target: 50 },   // 유지
            { duration: '30s', target: 0  },   // ramp-down
        ],
        gracefulRampDown: '10s',
    },
    stress: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
            { duration: '30s', target: 50  },
            { duration: '1m',  target: 100 },
            { duration: '1m',  target: 200 },
            { duration: '30s', target: 0   },
        ],
    },
    spike: {
        executor: 'ramping-arrival-rate',
        startRate: 10,
        timeUnit: '1s',
        preAllocatedVUs: 50,
        maxVUs: 500,
        stages: [
            { duration: '30s', target: 10  },
            { duration: '10s', target: 300 },  // 스파이크!
            { duration: '1m',  target: 300 },
            { duration: '10s', target: 10  },
        ],
    },
};

export const options = {
    scenarios: { [SCENARIO]: scenarios[SCENARIO] },

    // ── Pass/Fail 기준 (Threshold) — 미달 시 exit code != 0 → CI 실패 처리 가능
    thresholds: {
        http_req_failed:    ['rate<0.01'],                  // 에러율 1% 미만
        http_req_duration:  ['p(95)<500', 'p(99)<1000'],    // p95 < 500ms, p99 < 1s
        custom_errors:      ['rate<0.05'],
        post_list_latency_ms: ['p(95)<300'],
    },

    // ── tag 로 메트릭 분류 (Grafana 에서 service 별 보기 좋음)
    tags: { service: 'community-service', testType: SCENARIO },
};

// ─── 셋업: 한 번만 실행. 토큰 발급 + 카테고리 ID 확보 ────────────
export function setup() {
    // setup 안의 요청들은 부하 측정 메트릭에서 빼고 싶음.
    // → expectedStatuses 로 4xx 도 "예상된 응답" 으로 마킹하면 http_req_failed 에 안 잡힘.
    const setupRespOk = http.expectedStatuses({ min: 200, max: 499 });

    // 1) user-service 에서 JWT 받아오기 (테스트용 계정 — 없으면 토큰 없이 진행)
    const loginRes = http.post(
        `${AUTH_URL}/v1/auth/login`,
        JSON.stringify({
            email: __ENV.TEST_USER_EMAIL || 'loadtest@test.com',
            password: __ENV.TEST_USER_PASSWORD || 'Test1234!',
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'setup_login' },
            responseCallback: setupRespOk,   // ← 4xx 도 setup 단계에선 실패로 안 침
        },
    );

    let token = null;
    if (loginRes.status === 200) {
        try {
            token = loginRes.json('data.accessToken') || loginRes.json('accessToken');
        } catch (e) { /* 응답 스키마 다르면 null 그대로 */ }
    } else {
        console.warn(`[setup] 로그인 실패 (${loginRes.status}). 토큰 없이 진행. community 가 permitAll 이면 OK.`);
    }

    // 2) 카테고리 / 사용자 풀 결정 (seed 우선, 없으면 fallback)
    let useSeedPool = userIdPool.length > 0 && categoryIdPool.length > 0;
    let fallbackCategoryId = __ENV.CATEGORY_ID || null;
    let fallbackUserId = __ENV.TEST_USER_ID || null;

    if (useSeedPool) {
        console.log(`[setup] 시드 풀 사용: users=${userIdPool.length}, categories=${categoryIdPool.length}`);
    } else {
        // seed 없음 → fallback
        if (!fallbackCategoryId) {
            const catRes = http.get(`${BASE_URL}/v1/community-categories`, {
                headers: { 'Content-Type': 'application/json' },
                tags: { name: 'setup_categories' },
                responseCallback: setupRespOk,
            });
            console.log(`[setup] GET /v1/community-categories → status=${catRes.status}, body=${(catRes.body || '').toString().slice(0, 200)}`);
            if (catRes.status === 200) {
                try {
                    const body = catRes.json();
                    let list = [];
                    if (Array.isArray(body)) list = body;
                    else if (Array.isArray(body?.data)) list = body.data;
                    else if (Array.isArray(body?.data?.content)) list = body.data.content;
                    else if (Array.isArray(body?.content)) list = body.content;
                    if (list.length > 0) fallbackCategoryId = list[0].id || list[0].categoryId;
                } catch (e) { /* parsing 실패 */ }
            }
            if (!fallbackCategoryId) {
                console.warn('[setup] 카테고리 ID 확보 실패 → POST /v1/posts 시나리오는 스킵됩니다.');
            }
        }
        if (!fallbackUserId) {
            fallbackUserId = uuidv4();
            console.warn(`[setup] TEST_USER_ID env 없음 → 랜덤 UUID(${fallbackUserId}) 사용.`);
        }
    }

    return { token, useSeedPool, fallbackCategoryId, fallbackUserId };
}

// ─── 메인 시나리오 ───────────────────────────────────────────
export default function (data) {
    // 매 iteration 마다 사용자/카테고리 풀에서 랜덤 픽 (현실적인 분포)
    const userId = data.useSeedPool ? pickRandom(userIdPool) : data.fallbackUserId;
    const categoryId = data.useSeedPool ? pickRandom(categoryIdPool) : data.fallbackCategoryId;

    const headers = {
        'Content-Type': 'application/json',
        // 게이트웨이가 JWT 검증 후 전달하는 X-User-Id 헤더를 직접 세팅 (POST 엔드포인트 필수)
        'X-User-Id': userId,
        ...(data.token ? { Authorization: `Bearer ${data.token}` } : {}),
    };

    // 목록 응답에서 글 ID 한 개 추출 → 다음 시나리오에서 활용
    let pickedPostId = null;

    // ──── 1) 게시글 목록 조회 (가장 트래픽 많은 케이스)
    group('GET /v1/posts (목록)', () => {
        // 페이지를 다양하게 (0~9) — 깊은 페이지 latency 도 보기 위함
        const page = randomIntBetween(0, 9);
        const res = http.get(`${BASE_URL}/v1/posts?page=${page}&size=20`, {
            headers,
            tags: { name: 'list_posts' },
        });
        postListLatency.add(res.timings.duration);
        const ok = check(res, {
            '목록 200 OK': (r) => r.status === 200,
            '응답시간 < 500ms': (r) => r.timings.duration < 500,
        });
        errorRate.add(!ok);

        // 응답에서 글 ID 추출
        if (res.status === 200) {
            try {
                const body = res.json();
                const list = body?.data?.content || body?.content || (Array.isArray(body?.data) ? body.data : []);
                if (list.length > 0) {
                    const item = list[randomIntBetween(0, list.length - 1)];
                    pickedPostId = item.id || item.postId;
                }
            } catch (e) { /* skip */ }
        }
    });

    sleep(randomIntBetween(1, 3));   // 실제 유저 행동 사이의 think time

    // ──── 2) 게시글 상세조회 (N+1 의심 케이스 — DETAIL_RATIO 확률)
    if (pickedPostId && Math.random() < DETAIL_RATIO) {
        group('GET /v1/posts/{id} (상세)', () => {
            const res = http.get(`${BASE_URL}/v1/posts/${pickedPostId}`, {
                headers,
                tags: { name: 'detail_post' },
            });
            postDetailLatency.add(res.timings.duration);
            check(res, { '상세 200 OK': (r) => r.status === 200 });
        });
    }

    sleep(1);

    // ──── 3) 카테고리 조회
    group('GET /v1/community-categories', () => {
        const res = http.get(`${BASE_URL}/v1/community-categories`, {
            headers,
            tags: { name: 'list_categories' },
        });
        check(res, { '카테고리 200': (r) => r.status === 200 });
    });

    sleep(1);

    // ──── 4) 좋아요 (동시성 의심 케이스 — LIKE_RATIO 확률)
    if (pickedPostId && Math.random() < LIKE_RATIO) {
        group('POST /v1/posts/{id}/likes (좋아요)', () => {
            const res = http.post(`${BASE_URL}/v1/posts/${pickedPostId}/likes`, null, {
                headers,
                tags: { name: 'like_post' },
                // 좋아요 중복(409)은 비즈니스적으로 정상 응답이라 http_req_failed 메트릭에서 제외
                responseCallback: http.expectedStatuses({ min: 200, max: 201 }, 409),
            });
            const ok = check(res, {
                '좋아요 정상응답': (r) => r.status === 200 || r.status === 201 || r.status === 409,
            });
            if (ok && (res.status === 200 || res.status === 201)) postLiked.add(1);
            if (!ok) errorRate.add(1);
        });
    }

    sleep(randomIntBetween(1, 2));

    // ──── 5) 게시글 작성 (POST_RATIO 확률로, categoryId 가 확보된 경우만)
    if (categoryId && Math.random() < POST_RATIO) {
        group('POST /v1/posts (작성)', () => {
            const payload = JSON.stringify({
                categoryId: categoryId,
                title: `loadtest-${randomString(8)}`,
                content: `automated load test content ${randomString(40)}`,
            });
            const res = http.post(`${BASE_URL}/v1/posts`, payload, {
                headers,
                tags: { name: 'create_post' },
            });
            const created = check(res, {
                '작성 200/201': (r) => r.status === 200 || r.status === 201,
            });
            if (created) postCreated.add(1);
            else {
                errorRate.add(1);
                console.warn(`[create_post] 실패 status=${res.status}, body=${(res.body || '').toString().slice(0, 200)}`);
            }
        });
    }

    // ──── 6) 헬스 (모니터링 통과 확인용 — 부하 자체엔 비중 ↓)
    if (Math.random() < 0.02) {
        http.get(`${BASE_URL}/actuator/health`, { tags: { name: 'health' } });
    }
}

// ─── 종료 시 콘솔 요약 출력 ──────────────────────────────────
export function handleSummary(data) {
    const m = data.metrics;
    const fmt = (n) => (n ? n.toFixed(2) : 'n/a');

    const summary = `
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  community-service 부하 테스트 결과 [${SCENARIO}]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  총 요청 수         : ${m.http_reqs?.values.count ?? 0}
  실패율             : ${fmt((m.http_req_failed?.values.rate ?? 0) * 100)}%
  평균 응답시간      : ${fmt(m.http_req_duration?.values.avg)} ms
  p95 응답시간       : ${fmt(m.http_req_duration?.values['p(95)'])} ms
  p99 응답시간       : ${fmt(m.http_req_duration?.values['p(99)'])} ms
  최대 RPS           : ${fmt(m.http_reqs?.values.rate)}  req/s
  ─────────────────────────────────────────────
  목록 p95           : ${fmt(m.post_list_latency_ms?.values['p(95)'])} ms
  상세 p95           : ${fmt(m.post_detail_latency_ms?.values['p(95)'])} ms
  생성된 글 수       : ${m.post_created_total?.values.count ?? 0}
  눌러진 좋아요 수   : ${m.post_liked_total?.values.count ?? 0}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
`;
    console.log(summary);
    return {
        'stdout': summary,
        'summary.json': JSON.stringify(data, null, 2),
    };
}
