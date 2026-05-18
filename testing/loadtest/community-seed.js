/**
 * community-service 부하테스트용 시드 데이터 생성기
 *
 * 실행 방법:
 *   node loadtest/seed/community-seed.js
 *
 * 환경변수 (선택):
 *   COMMUNITY_URL    기본값 http://localhost:9001
 *   POST_COUNT       생성할 게시글 수, 기본 1000
 *   USER_COUNT       사용할 사용자 수 (상위 N명), 기본 50
 *   CATEGORY_COUNT   생성할 카테고리 수, 기본 3
 *   BASE_SEED_FILE   기존 사용자 ID 가 담긴 파일, 기본 testing/data/loadtest-ids.json
 *
 * 결과: loadtest/seed/seed_data.json 에 저장
 */

const fs   = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');

// ── 설정 ─────────────────────────────────────────────────────────────────────
const COMMUNITY_URL    = process.env.COMMUNITY_URL  || 'http://localhost:9001';
const POST_COUNT       = parseInt(process.env.POST_COUNT      || '1000', 10);
const USER_COUNT       = parseInt(process.env.USER_COUNT      || '50',   10);
const CATEGORY_COUNT   = parseInt(process.env.CATEGORY_COUNT  || '3',    10);
const BASE_SEED_FILE   = process.env.BASE_SEED_FILE ||
    path.resolve(__dirname, '../../testing/data/loadtest-ids.json');
const OUT_FILE         = path.resolve(__dirname, 'seed_data.json');

// ── 유틸 ─────────────────────────────────────────────────────────────────────
function request(method, url, body, headers = {}) {
    return new Promise((resolve, reject) => {
        const parsed = new URL(url);
        const lib    = parsed.protocol === 'https:' ? https : http;
        const data   = body ? JSON.stringify(body) : null;

        const options = {
            hostname: parsed.hostname,
            port:     parsed.port || (parsed.protocol === 'https:' ? 443 : 80),
            path:     parsed.pathname + parsed.search,
            method,
            headers: {
                'Content-Type': 'application/json',
                ...(data ? { 'Content-Length': Buffer.byteLength(data) } : {}),
                ...headers,
            },
        };

        const req = lib.request(options, (res) => {
            let raw = '';
            res.on('data', chunk => raw += chunk);
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, body: JSON.parse(raw) });
                } catch {
                    resolve({ status: res.statusCode, body: raw });
                }
            });
        });

        req.on('error', reject);
        if (data) req.write(data);
        req.end();
    });
}

/** X-User-Id / X-User-Role 헤더 — Gateway 없이 서비스 직접 호출 시 사용 */
function authHeaders(userId, role = 'USER') {
    return { 'X-User-Id': userId, 'X-User-Role': role };
}

function sleep(ms) {
    return new Promise(r => setTimeout(r, ms));
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

// ── 메인 ─────────────────────────────────────────────────────────────────────
async function main() {
    console.log('=== Community 시드 데이터 생성 시작 ===');
    console.log(`  서버:       ${COMMUNITY_URL}`);
    console.log(`  사용자 수:  ${USER_COUNT}명`);
    console.log(`  카테고리:   ${CATEGORY_COUNT}개`);
    console.log(`  게시글 수:  ${POST_COUNT}개`);

    // 1. 기존 사용자 목록 로드
    if (!fs.existsSync(BASE_SEED_FILE)) {
        console.error(`[ERROR] 베이스 시드 파일 없음: ${BASE_SEED_FILE}`);
        console.error('       testing/data/loadtest-ids.json 을 먼저 생성하세요.');
        process.exit(1);
    }
    const base     = JSON.parse(fs.readFileSync(BASE_SEED_FILE, 'utf-8'));
    const adminId  = base.adminUserId || '00000000-0000-0000-0000-000000000001';

    // USER_COUNT 만큼만 사용 (상위 N명)
    const allUsers = base.users || [];
    if (!allUsers.length) {
        console.error('[ERROR] 사용자 데이터가 없습니다.');
        process.exit(1);
    }
    const users = allUsers.slice(0, USER_COUNT);
    console.log(`  사용자 ${users.length}명 선택 (전체 ${allUsers.length}명 중)`);

    // 2. 커뮤니티 카테고리 생성 (ADMIN) — CATEGORY_COUNT 개만 생성
    const allCategoryNames = ['자유게시판', '질문게시판', '후기게시판', '정보공유', '이벤트'];
    const categoryNames    = allCategoryNames.slice(0, CATEGORY_COUNT);
    const communityCategories = [];

    console.log('\n[1/3] 커뮤니티 카테고리 생성...');
    for (const name of categoryNames) {
        try {
            const res = await request(
                'POST',
                `${COMMUNITY_URL}/v1/community-categories`,
                { name },
                authHeaders(adminId, 'ADMIN')
            );
            if (res.status === 201 && res.body?.data?.id) {
                communityCategories.push({ name, id: res.body.data.id });
                console.log(`  ✓ ${name} → ${res.body.data.id}`);
            } else if (res.status === 409) {
                console.log(`  - ${name} 이미 존재, 스킵`);
            } else {
                console.warn(`  ! ${name} 생성 실패 (${res.status}):`, JSON.stringify(res.body).slice(0, 100));
            }
        } catch (e) {
            console.warn(`  ! ${name} 요청 오류:`, e.message);
        }
        await sleep(50);
    }

    // 카테고리가 하나도 없으면 기존 것 조회 시도
    if (!communityCategories.length) {
        console.log('  카테고리 직접 생성 실패 — 기존 목록 조회 시도...');
        try {
            const res = await request('GET', `${COMMUNITY_URL}/v1/community-categories?size=10`);
            const content = res.body?.data?.content || [];
            content.forEach(c => communityCategories.push({ name: c.name, id: c.id }));
            console.log(`  기존 카테고리 ${communityCategories.length}개 사용`);
        } catch (e) {
            console.error('  카테고리 조회도 실패:', e.message);
        }
    }

    if (!communityCategories.length) {
        console.error('[ERROR] 사용 가능한 카테고리가 없습니다. 서비스가 실행 중인지 확인하세요.');
        process.exit(1);
    }

    // 3. 게시글 생성
    console.log(`\n[2/3] 게시글 ${POST_COUNT}개 생성...`);
    const posts        = [];
    const batchSize    = 10;
    let   successCount = 0;

    for (let i = 0; i < POST_COUNT; i++) {
        const user     = pick(users);
        const category = pick(communityCategories);

        try {
            const res = await request(
                'POST',
                `${COMMUNITY_URL}/v1/posts`,
                {
                    categoryId: category.id,
                    title:      `부하테스트 게시글 ${i + 1}`,
                    content:    `이 게시글은 부하테스트를 위해 자동 생성되었습니다. (${i + 1}/${POST_COUNT})`,
                },
                authHeaders(user.userId, 'USER')
            );

            if (res.status === 201 && res.body?.data?.id) {
                posts.push({
                    postId:     res.body.data.id,
                    categoryId: category.id,
                    userId:     user.userId,
                });
                successCount++;

                if (successCount % 20 === 0) {
                    process.stdout.write(`\r  생성 중... ${successCount}/${POST_COUNT}`);
                }
            }
        } catch (e) {
            // 개별 실패는 무시하고 계속
        }

        // 너무 빠른 요청으로 서버 부하 방지
        if (i % batchSize === batchSize - 1) {
            await sleep(100);
        }
    }
    console.log(`\r  ✓ 게시글 ${successCount}개 생성 완료`);

    if (!posts.length) {
        console.error('[ERROR] 게시글 생성에 모두 실패했습니다.');
        process.exit(1);
    }

    // 4. 시드 파일 저장
    console.log('\n[3/3] seed_data.json 저장...');
    const seedData = {
        generatedAt:         new Date().toISOString(),
        communityUrl:        COMMUNITY_URL,
        adminUserId:         adminId,
        users:               users.map(u => ({ userId: u.userId, role: u.role || 'USER' })),
        communityCategories,
        posts,
        // 핫스팟 테스트용: 상위 5개 게시글을 집중적으로 조회
        hotPosts: posts.slice(0, Math.min(5, posts.length)),
    };

    fs.writeFileSync(OUT_FILE, JSON.stringify(seedData, null, 2));
    console.log(`  ✓ 저장 완료: ${OUT_FILE}`);
    console.log('\n=== 완료 ===');
    console.log(`  카테고리: ${communityCategories.length}개`);
    console.log(`  게시글:   ${posts.length}개`);
    console.log(`  핫스팟:   ${seedData.hotPosts.length}개`);
    console.log('\n이제 부하테스트를 실행하세요:');
    console.log('  k6 run --env SCENARIO=stress loadtest/k6/community-loadtest.js');
}

main().catch(err => {
    console.error('[FATAL]', err);
    process.exit(1);
});
