#!/usr/bin/env bash
# 8강: 켜 둔 서버(./gradlew bootTestRun, http://localhost:8080)에 구매 흐름을 순서대로 요청한다.
#   ./scripts/walkthrough.sh        (user1 이 4번 유료 글을 산다)
#   ./scripts/walkthrough.sh 5      (5번 글)
# curl 만 쓴다. 응답 JSON 은 그대로 보여 주고 HTTP 상태코드를 뒤에 붙인다.
set -u
POST=${1:-4}
BASE=${BASE:-http://localhost:8080}
TOKEN=""

call() {   # call <METHOD> <path> [json body]
  local method=$1 path=$2 body=${3:-}
  local -a opts=(-s -w '\n(HTTP %{http_code})' -X "$method" -H 'Content-Type: application/json')
  [ -n "$TOKEN" ] && opts+=(-H "Authorization: Bearer $TOKEN")
  [ -n "$body" ] && opts+=(-d "$body")
  curl "${opts[@]}" "$BASE$path"
  echo; echo
}

echo "== 1. 로그인 (user1)"
login=$(call POST /api/v1/members/login '{"username":"user1","password":"1234"}')
TOKEN=$(sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p' <<< "$login")
echo "$login" | sed 's/"accessToken":"[^"]*"/"accessToken":"…"/; s/"apiKey":"[^"]*"/"apiKey":"…"/'   # 토큰은 가린다
echo

echo "== 2. 내 지갑 (구매 전)";        call GET /api/v1/wallets/me
echo "== 3. $POST 번 글 (구매 전)";   call GET "/api/v1/posts/$POST"

echo "== 4. 주문 생성 (대기)"
created=$(call POST "/api/v1/posts/$POST/orders")
echo "$created"; echo
ORDER=$(sed -n 's/.*"orderDto":{"id":\([0-9]*\).*/\1/p' <<< "$created")

echo "== 5. 주문 확정 (주문 $ORDER)"; call POST "/api/v1/orders/$ORDER/confirm"
echo "== 6. $POST 번 글 (구매 후)";  call GET "/api/v1/posts/$POST"
echo "== 7. 내 주문 목록";           call GET /api/v1/orders
echo "== 8. 내 지갑 (구매 후)";      call GET /api/v1/wallets/me
