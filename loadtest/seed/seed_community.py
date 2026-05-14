#!/usr/bin/env python3
"""
festie 부하 테스트용 시드 데이터 생성기

사용법:
    pip install requests
    python loadtest/seed/seed_community.py                          # 기본: 사용자 50, 글 1000
    python loadtest/seed/seed_community.py --users 100 --posts 5000 # 대량
    python loadtest/seed/seed_community.py --skip-users             # user-service 없을 때

생성:
    - user-service: 사용자 N명
    - community-service: 카테고리 3개 + 게시글 K개 (랜덤 사용자/카테고리 분포)

결과:
    loadtest/seed/seed_data.json 에 user_ids / category_ids 저장
    → k6 스크립트가 이 파일을 읽어서 부하 분산
"""

import argparse
import json
import random
import sys
import time
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

try:
    import requests
except ImportError:
    print(
        "requests 모듈이 없습니다. 다음 중 한 가지 방법으로 설치하세요:\n"
        "  1) pip3 install requests\n"
        "  2) pip3 install requests --break-system-packages   (PEP 668 보호 정책 거부 시)\n"
        "  3) 가상환경 사용:\n"
        "     python3 -m venv loadtest/.venv && source loadtest/.venv/bin/activate && pip install requests",
        file=sys.stderr,
    )
    sys.exit(1)

# ─── 기본 설정 (환경변수로 override 가능) ────────────────────
USER_BASE_URL = "http://localhost:8081"
COMMUNITY_BASE_URL = "http://localhost:9001"
SEED_OUTPUT = Path(__file__).parent / "seed_data.json"

# ─── 더미 데이터 풀 ──────────────────────────────────────
KOREAN_FIRST = ["민준", "서연", "도윤", "지우", "예준", "서윤", "주원", "하은", "지호", "수아", "건우", "지유", "현우", "다은", "우진"]
KOREAN_LAST  = ["김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "신", "권", "황"]
CATEGORIES   = ["잡담", "후기", "정보공유"]
POST_TITLES  = [
    "오늘 다녀온 페스티벌 후기",
    "처음 가본 사람도 즐길 수 있을까요?",
    "주차 정보 공유합니다",
    "다음 페스티벌 같이 가실 분",
    "굿즈 정보 모음",
    "라인업 분석",
    "음향 너무 좋았어요",
    "교통편 추천드림",
]
POST_CONTENTS_TEMPLATE = (
    "이 글은 부하 테스트 시드로 자동 생성된 글입니다.\n"
    "현실적인 데이터 분포를 만들기 위해 다양한 사용자/카테고리에서 작성됩니다.\n"
    "(seed run_id={run_id}, post_idx={idx})\n"
    "본문 본문 본문 본문 본문 본문 본문 본문 본문 본문\n"
)


def random_name() -> str:
    return random.choice(KOREAN_LAST) + random.choice(KOREAN_FIRST)


def random_phone() -> str:
    return f"010{random.randint(10000000, 99999999)}"


# ─── API 호출 헬퍼 ───────────────────────────────────────
def create_user(idx: int, run_id: str) -> str | None:
    """user-service 에 회원가입. 성공 시 userId(UUID) 반환."""
    email = f"loadtest+{run_id}+{idx}@test.com"
    payload = {
        "email": email,
        "password": "Test1234!",
        "nickname": f"부하테스트{idx:04d}",
        "name": random_name(),
        "phoneNumber": random_phone(),
    }
    try:
        r = requests.post(f"{USER_BASE_URL}/v1/users", json=payload, timeout=10)
        if r.status_code in (200, 201):
            body = r.json()
            data = body.get("data", body)
            return data.get("userId") or data.get("id")
        if idx < 3:  # 처음 몇 개만 로그
            print(f"  [user {idx}] {r.status_code}: {r.text[:120]}", file=sys.stderr)
        return None
    except Exception as e:
        if idx < 3:
            print(f"  [user {idx}] exception: {e}", file=sys.stderr)
        return None


def create_category(name: str) -> str | None:
    """community-service 에 카테고리 생성."""
    try:
        r = requests.post(
            f"{COMMUNITY_BASE_URL}/v1/community-categories",
            json={"name": name},
            timeout=10,
        )
        if r.status_code in (200, 201):
            body = r.json()
            data = body.get("data", body)
            return data.get("id") or data.get("categoryId")
        print(f"  [category {name}] {r.status_code}: {r.text[:120]}", file=sys.stderr)
        return None
    except Exception as e:
        print(f"  [category {name}] exception: {e}", file=sys.stderr)
        return None


def create_post(user_id: str, category_id: str, idx: int, run_id: str) -> bool:
    """community-service 에 글 생성."""
    payload = {
        "categoryId": category_id,
        "title": f"{random.choice(POST_TITLES)} #{idx}",
        "content": POST_CONTENTS_TEMPLATE.format(run_id=run_id, idx=idx),
    }
    try:
        r = requests.post(
            f"{COMMUNITY_BASE_URL}/v1/posts",
            json=payload,
            headers={"X-User-Id": user_id, "Content-Type": "application/json"},
            timeout=10,
        )
        if r.status_code in (200, 201):
            return True
        if idx < 3:
            print(f"  [post {idx}] {r.status_code}: {r.text[:120]}", file=sys.stderr)
        return False
    except Exception as e:
        if idx < 3:
            print(f"  [post {idx}] exception: {e}", file=sys.stderr)
        return False


# ─── 메인 ────────────────────────────────────────────────
def main() -> None:
    parser = argparse.ArgumentParser(description="festie 부하 테스트 시드 데이터 생성")
    parser.add_argument("--users",   type=int, default=50,   help="생성할 사용자 수")
    parser.add_argument("--posts",   type=int, default=1000, help="생성할 글 수")
    parser.add_argument("--workers", type=int, default=10,   help="동시 워커 수")
    parser.add_argument("--skip-users", action="store_true",
                        help="user-service 호출 안 함 (랜덤 UUID 로 대체, 검증 우회되면 사용)")
    args = parser.parse_args()

    run_id = str(int(time.time()))

    print("━" * 50)
    print(f" festie 부하 테스트 시드 (run_id={run_id})")
    print(f" 사용자: {args.users}명, 카테고리: {len(CATEGORIES)}개, 글: {args.posts}개")
    print(f" 동시 워커: {args.workers}")
    if args.skip_users:
        print(" ⚠️  --skip-users : 사용자 API 호출 건너뜀 (랜덤 UUID 사용)")
    print("━" * 50)

    # 1. 사용자
    print(f"\n[1/3] 사용자 {args.users}명 생성...")
    t0 = time.time()
    user_ids: list[str] = []
    if args.skip_users:
        user_ids = [str(uuid.uuid4()) for _ in range(args.users)]
        print(f"  → 랜덤 UUID {len(user_ids)}개 생성")
    else:
        with ThreadPoolExecutor(max_workers=args.workers) as ex:
            futures = [ex.submit(create_user, i, run_id) for i in range(args.users)]
            for f in as_completed(futures):
                uid = f.result()
                if uid:
                    user_ids.append(uid)
        print(f"  → 성공: {len(user_ids)}/{args.users} ({time.time()-t0:.1f}s)")

    if not user_ids:
        print("\n✗ 사용자 0명. user-service 가 떠 있는지 확인하거나 --skip-users 옵션 사용.",
              file=sys.stderr)
        sys.exit(1)

    # 2. 카테고리
    print(f"\n[2/3] 카테고리 {len(CATEGORIES)}개 생성...")
    t0 = time.time()
    category_ids: list[str] = []
    for name in CATEGORIES:
        cid = create_category(f"{name}-{run_id}")
        if cid:
            category_ids.append(cid)
    print(f"  → 성공: {len(category_ids)}/{len(CATEGORIES)} ({time.time()-t0:.1f}s)")

    if not category_ids:
        print("\n✗ 카테고리 0개. community-service 가 떠 있는지 확인.", file=sys.stderr)
        sys.exit(1)

    # 3. 글
    print(f"\n[3/3] 글 {args.posts}개 생성...")
    t0 = time.time()
    success = 0
    fail = 0
    with ThreadPoolExecutor(max_workers=args.workers) as ex:
        futures = [
            ex.submit(
                create_post,
                random.choice(user_ids),
                random.choice(category_ids),
                i,
                run_id,
            )
            for i in range(args.posts)
        ]
        done = 0
        for f in as_completed(futures):
            done += 1
            if f.result():
                success += 1
            else:
                fail += 1
            if done % 100 == 0:
                print(f"  → 진행: {done}/{args.posts} (성공 {success}, 실패 {fail})")
    print(f"  → 성공: {success}/{args.posts} ({time.time()-t0:.1f}s)")

    # 4. 결과 저장
    SEED_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    SEED_OUTPUT.write_text(
        json.dumps(
            {
                "run_id": run_id,
                "created_at": int(time.time()),
                "user_count": len(user_ids),
                "category_count": len(category_ids),
                "post_count": success,
                "user_ids": user_ids,
                "category_ids": category_ids,
            },
            indent=2,
            ensure_ascii=False,
        ),
        encoding="utf-8",
    )

    print(f"\n✓ 완료! → {SEED_OUTPUT}")
    print("\n다음 단계 — k6 부하 테스트 실행:")
    print(f"  k6 run --env SCENARIO=load \\")
    print(f"         --env SEED_FILE={SEED_OUTPUT.relative_to(Path.cwd()) if SEED_OUTPUT.is_relative_to(Path.cwd()) else SEED_OUTPUT} \\")
    print(f"         loadtest/k6/community-loadtest.js")


if __name__ == "__main__":
    main()
