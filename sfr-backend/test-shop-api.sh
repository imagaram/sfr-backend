#!/bin/bash
# ショップAPI テストスクリプト（curl）

BASE_URL="http://localhost:8080/api/shop"
HEADER_CT="Content-Type: application/json"
HEADER_AUTH="Authorization: Bearer your-jwt-token-here"

echo "=== ショップAPI統合テスト ==="

# 1. 商品一覧取得テスト
echo -e "\n1. 商品一覧取得テスト"
response=$(curl -s -w "%{http_code}" -H "$HEADER_CT" -H "$HEADER_AUTH" "$BASE_URL/items")
http_code="${response: -3}"
if [ "$http_code" -eq 200 ]; then
    echo "✅ 商品一覧取得成功 (HTTP $http_code)"
else
    echo "❌ 商品一覧取得失敗 (HTTP $http_code)"
fi

# 2. 商品登録テスト
echo -e "\n2. 商品登録テスト"
test_item='{
    "name": "テスト商品",
    "description": "テスト用の商品です",
    "price": 100.00,
    "stock": 10,
    "ownerId": 1,
    "category": "test"
}'

response=$(curl -s -w "%{http_code}" -X POST -H "$HEADER_CT" -H "$HEADER_AUTH" -d "$test_item" "$BASE_URL/items")
http_code="${response: -3}"
if [ "$http_code" -eq 200 ]; then
    echo "✅ 商品登録成功 (HTTP $http_code)"
else
    echo "❌ 商品登録失敗 (HTTP $http_code)"
fi

# 3. 商品詳細取得テスト
echo -e "\n3. 商品詳細取得テスト (ID: 1)"
response=$(curl -s -w "%{http_code}" -H "$HEADER_CT" -H "$HEADER_AUTH" "$BASE_URL/items/1")
http_code="${response: -3}"
if [ "$http_code" -eq 200 ]; then
    echo "✅ 商品詳細取得成功 (HTTP $http_code)"
else
    echo "❌ 商品詳細取得失敗 (HTTP $http_code)"
fi

# 4. 注文作成テスト
echo -e "\n4. 注文作成テスト"
test_order='{
    "itemId": 1,
    "buyerId": 2,
    "quantity": 2,
    "totalPrice": 200.00
}'

response=$(curl -s -w "%{http_code}" -X POST -H "$HEADER_CT" -H "$HEADER_AUTH" -d "$test_order" "$BASE_URL/orders")
http_code="${response: -3}"
if [ "$http_code" -eq 200 ]; then
    echo "✅ 注文作成成功 (HTTP $http_code)"
else
    echo "❌ 注文作成失敗 (HTTP $http_code)"
fi

# 5. 注文一覧取得テスト
echo -e "\n5. 注文一覧取得テスト (buyerId: 2)"
response=$(curl -s -w "%{http_code}" -H "$HEADER_CT" -H "$HEADER_AUTH" "$BASE_URL/orders?buyerId=2")
http_code="${response: -3}"
if [ "$http_code" -eq 200 ]; then
    echo "✅ 注文一覧取得成功 (HTTP $http_code)"
else
    echo "❌ 注文一覧取得失敗 (HTTP $http_code)"
fi

# 6. PoA提出テスト
echo -e "\n6. PoA提出テスト"
test_poa='{
    "text": "テストPoA",
    "url": "https://example.com/poa"
}'

response=$(curl -s -w "%{http_code}" -X POST -H "$HEADER_CT" -H "$HEADER_AUTH" -d "$test_poa" "$BASE_URL/items/1/poa")
http_code="${response: -3}"
if [ "$http_code" -eq 200 ]; then
    echo "✅ PoA提出成功 (HTTP $http_code)"
else
    echo "❌ PoA提出失敗 (HTTP $http_code)"
fi

# 7. ファイルアップロードテスト（テスト画像が必要）
echo -e "\n7. ファイルアップロードテスト"
if [ -f "test-image.jpg" ]; then
    response=$(curl -s -w "%{http_code}" -X POST -H "$HEADER_AUTH" -F "file=@test-image.jpg" "$BASE_URL/items/1/upload")
    http_code="${response: -3}"
    if [ "$http_code" -eq 200 ]; then
        echo "✅ ファイルアップロード成功 (HTTP $http_code)"
    else
        echo "❌ ファイルアップロード失敗 (HTTP $http_code)"
    fi
else
    echo "⚠️  テスト画像ファイル (test-image.jpg) が見つかりません"
fi

echo -e "\n=== テスト完了 ==="
