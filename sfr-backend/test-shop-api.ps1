# ショップAPI テストスクリプト（cURL）

## 基本設定
$baseUrl = "http://localhost:8080/api/shop"
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer your-jwt-token-here"
}

Write-Host "=== ショップAPI統合テスト ==="

## 1. 商品一覧取得テスト
Write-Host "`n1. 商品一覧取得テスト"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/items" -Method GET -Headers $headers
    Write-Host "✅ 商品一覧取得成功: $($response.Count) 件"
} catch {
    Write-Host "❌ 商品一覧取得失敗: $($_.Exception.Message)"
}

## 2. 商品登録テスト
Write-Host "`n2. 商品登録テスト"
$testItem = @{
    name = "テスト商品"
    description = "テスト用の商品です"
    price = 100.00
    stock = 10
    ownerId = 1
    category = "test"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/items" -Method POST -Body $testItem -Headers $headers
    Write-Host "✅ 商品登録成功"
} catch {
    Write-Host "❌ 商品登録失敗: $($_.Exception.Message)"
}

## 3. 商品詳細取得テスト
Write-Host "`n3. 商品詳細取得テスト (ID: 1)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/items/1" -Method GET -Headers $headers
    Write-Host "✅ 商品詳細取得成功: $($response.name)"
} catch {
    Write-Host "❌ 商品詳細取得失敗: $($_.Exception.Message)"
}

## 4. 注文作成テスト
Write-Host "`n4. 注文作成テスト"
$testOrder = @{
    itemId = 1
    buyerId = 2
    quantity = 2
    totalPrice = 200.00
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/orders" -Method POST -Body $testOrder -Headers $headers
    Write-Host "✅ 注文作成成功"
} catch {
    Write-Host "❌ 注文作成失敗: $($_.Exception.Message)"
}

## 5. 注文一覧取得テスト
Write-Host "`n5. 注文一覧取得テスト (buyerId: 2)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/orders?buyerId=2" -Method GET -Headers $headers
    Write-Host "✅ 注文一覧取得成功: $($response.Count) 件"
} catch {
    Write-Host "❌ 注文一覧取得失敗: $($_.Exception.Message)"
}

## 6. PoA提出テスト
Write-Host "`n6. PoA提出テスト"
$testPoa = @{
    text = "テストPoA"
    url = "https://example.com/poa"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/items/1/poa" -Method POST -Body $testPoa -Headers $headers
    Write-Host "✅ PoA提出成功"
} catch {
    Write-Host "❌ PoA提出失敗: $($_.Exception.Message)"
}

## 7. 開発用 - データクリア
Write-Host "`n7. 開発用データクリア"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/dev/clear" -Method POST -Headers $headers
    Write-Host "✅ データクリア成功"
} catch {
    Write-Host "❌ データクリア失敗: $($_.Exception.Message)"
}

Write-Host "`n=== テスト完了 ==="
