この文書は暗号資産SFRのユーザーへの報酬設計について記述します。暗号資産SFR全体の設計についてはsfr-backend\document\design\sfr_sfrcrypto_design.mdを参照してください。GitHub Copilot Proがsfr-backendの開発支援を行う際に、DBスキーマ設計やAPI設計の参照元として活用できるよう、構造化された定義・ロジック・制度的背景を含めて記述しています。

SFRの価格安定性と高値維持を目的とする場合、ユーザーへの報酬設計は単なるインセンティブではなく、市場構造そのものに影響を与える経済設計になります。以下に、目的に沿った報酬設計の考え方を整理してみます。

🎯 目的：SFRの円建て価格を安定的に高値で維持する

この目的を達成するには、報酬設計が以下の3つの要素をバランスよく満たす必要があります：

供給の抑制（インフレ回避）

需要の促進（ユースケースと流動性の確保）

市場参加者の行動誘導（売り圧を減らし、保有・利用を促す）

🧮 報酬量の計算式：貢献度 × 市場状況 × 保有インセンティブ

以下のような多変数モデルが有効です：

SFR報酬量 = B × C × M × H

変数

意味

調整方法

B

基本報酬係数

貢献タイプ別に設定（例：開発、流動性提供、ガバナンス投票など）

C

貢献度スコア

定量評価（例：コード量、取引量、提案採択率など）

M

市場状況係数

SFR/JPY価格が目標価格より低い場合は報酬増、高い場合は報酬減

H

保有インセンティブ係数

長期保有者やステーキング参加者に報酬倍率を追加

📈 市場状況係数 M の設計例

SFR/JPY価格が目標価格（例：¥150）を基準にして、以下のように報酬倍率を調整：

SFR/JPY価格

M係数

¥120未満

1.5〜2.0（報酬増）

¥120〜¥150未満

1.0（通常報酬）

¥150〜¥170未満

0.8〜0.5（報酬減）

¥170以上

0.3以下（報酬抑制）

これにより、価格が下落傾向にあるときは報酬を増やして参加を促し、価格が過熱しているときは報酬を抑えて供給過多を防ぎます。

🛡️ 保有インセンティブ係数 H の設計例

1. 設計方針（価格支持力ベース）

H係数は「売り圧を抑え、買い支え・保有を促す」行動に報酬を与えるため、保有期間だけでなく、市場価格に対する保有者の貢献度を加味する。

H = 1.0 + α × log₁₀(保有日数) + β × (平均保有時価格 ÷ 現在価格)

α：保有期間に対する報酬係数（例：0.05）

β：価格支持力に対する報酬係数（例：0.2）

平均保有時価格 ÷ 現在価格：価格支持力（高値圏で保有しているほど値が大きくなる）

この式により、長期保有かつ高値圏での保有者ほど報酬倍率が高くなる。

2. ステーキング・SFR決済利用との組み合わせ

このH係数に、以下の加算ロジックを組み合わせることで、保有行動の多様性に応じた報酬強化が可能：

ステーキング3ヶ月：× 1.1

ステーキング6ヶ月：× 1.3

SFR決済利用（API/NFT等）：+ 0.1

3. 複合保有行動による報酬最適化

行動タイプ

評価軸

報酬倍率への影響

ステーキング

期間（月）・ロック量

乗算係数（例：×1.1〜×1.5）

決済利用

回数・金額・カテゴリ

加算係数（例：+0.1〜+0.3）

保有期間

日数

対数加算（例：log₁₀ベース）

保有価格

平均取得価格

高値圏ほど加算（例：β × ratio）

🧠 応用：ダイナミック報酬モデルの導入

報酬量をリアルタイムで市場価格や流動性に連動させることで、SFRの価格安定性を支える「経済的フィードバックループ」が形成されます。これは、トークン設計における**Reflexivity（反射性）**の応用です。

💡補足：報酬の原資とインフレ管理

報酬原資はDAOトレジャリーやステーキングプールから供給

トークン発行量に上限を設け、報酬量が一定閾値を超えた場合はバーンやロックアップを活用

🧩 ユースケースに基づく報酬・購入・使用モデル

暗号資産SFRが付与されるとき

学習空間機能で無料講座を開講し、卒業生の評価が高かった時

ショップから"限定商品"を購入し、それを転売やオークション販売したときに、販売代金の10％が限定商品制作販売チームに寄付される機能を利用したとき

ショップやオークションから商品を購入したとき

学習空間機能で開かれた有料教室（公式ファンクラブ/オンラインサロン/講座）を受講したとき

コミュニティを立ち上げ管理したときに、多くの支持を集め、評価の高いコミュニティとして認知されたとき（指標はフォロー数とフォロワーからの評価）

暗号資産SFRを購入するとき

運営の公式ショップから購入

暗号資産SFRを使用するとき

モバイルアプリSFRMに広告を掲載するとき

運営の公式ショップで限定商品を購入するとき

ショップやオークションで購入する商品が”SFRでの支払い”が指定されているとき

🧱 B係数の設計方針

以下の観点で整理すると、設計が明確になります：

1. 貢献タイプの分類

貢献タイプ

内容例

B係数（案）

開発貢献

コード提出、バグ修正、機能提案

1.2〜1.5

流動性提供

LP参加、AMMへの資金供給

1.0〜1.3

ガバナンス

提案提出、投票参加、議論貢献

0.8〜1.2

教育・普及

講座開設、コミュニティ運営

1.0〜1.4

商用利用

SFR決済導入、商品販売

1.0〜1.3

UX改善

フィードバック提出、UI提案

0.8〜1.1

※係数は仮値。実際にはC（貢献度）とのバランスで調整。

2. 報酬原資との整合性

DAOトレジャリーやステーキングプールからの供給に対して、B係数が高すぎるとインフレリスクがあるため、月次上限やカテゴリ別予算枠を設けると良いです。

3. 動的調整の可能性

B係数を固定せず、以下のような条件で動的に調整することも可能です：

貢献数が過多なカテゴリは係数を徐々に下げる（希少性重視）

市場価格が低迷しているときは「商用利用」や「教育普及」系の係数を上げる

ガバナンス参加率が低いときは「ガバナンス」係数を一時的に上げる

🧑‍💻 B係数の実装ロジック（コードベース）

SFR報酬量の計算式 SFR報酬量 = B × C × M × H において、B係数は貢献タイプごとの基本倍率です。以下はTypeScriptベースでの定義例です。

1. 貢献タイプの定義とB係数のマッピング

type ContributionType =
  | 'development'
  | 'liquidity'
  | 'governance'
  | 'education'
  | 'commerce'
  | 'ux'

const baseRewardFactors: Record<ContributionType, number> = {
  development: 1.4,
  liquidity: 1.3,
  governance: 1.0,
  education: 1.3,
  commerce: 1.2,
  ux: 0.9
}

2. 報酬計算関数の定義

function calculateSFRReward(
  contributionType: ContributionType,
  contributionScore: number, // C
  marketFactor: number,       // M
  holdingFactor: number       // H
): number {
  const B = baseRewardFactors[contributionType]
  return B * contributionScore * marketFactor * holdingFactor
}

この関数により、貢献タイプに応じた報酬が自動的に算出されます。

3. B係数の動的調整（希少性ベース）

function adjustBaseRewardFactor(
  contributionType: ContributionType,
  recentActivityVolume: number,
  threshold: number
): number {
  const base = baseRewardFactors[contributionType]
  if (recentActivityVolume > threshold) {
    return base * 0.9 // 過剰なら係数を下げる
  } else {
    return base * 1.1 // 希少なら係数を上げる
  }
}

このようにすれば、貢献の希少性や市場状況に応じてB係数を微調整できます。

このロジックは、報酬配布モジュールやAPIレスポンス生成にも組み込み可能です。 次は、C（貢献度スコア）の定量評価方法や、H（保有インセンティブ）の算出ロジックもコード化していくと、報酬設計全体が実装レベルで一貫性を持ちます。

📊 C係数（貢献度スコア）の設計方針

1. 貢献タイプ別の評価軸

貢献タイプ

評価指標（例）

スコア範囲（案）

開発

コミット数、PRのマージ率、Issue対応数

0.1〜5.0

流動性提供

LP提供量、提供期間、取引量

0.1〜3.0

ガバナンス

提案数、投票参加率、議論への貢献度

0.1〜2.0

教育・普及

講座受講者数、評価、継続率

0.1〜4.0

商用利用

SFR決済回数、売上高、継続利用率

0.1〜3.5

UX改善

フィードバック件数、採用率

0.1〜2.5

2. 実装例（TypeScript）

type ContributionMetrics = {
  commits?: number
  mergedPRs?: number
  issuesClosed?: number
  lpAmount?: number
  lpDurationDays?: number
  votesCast?: number
  proposalsSubmitted?: number
  feedbackSubmitted?: number
  feedbackAccepted?: number
}

function calculateContributionScore(metrics: ContributionMetrics): number {
  let score = 0

  if (metrics.commits) score += metrics.commits * 0.05
  if (metrics.mergedPRs) score += metrics.mergedPRs * 0.2
  if (metrics.issuesClosed) score += metrics.issuesClosed * 0.1
  if (metrics.lpAmount && metrics.lpDurationDays)
    score += (metrics.lpAmount / 1000) * (metrics.lpDurationDays / 30)

  if (metrics.votesCast) score += metrics.votesCast * 0.05
  if (metrics.proposalsSubmitted) score += metrics.proposalsSubmitted * 0.3

  if (metrics.feedbackSubmitted)
    score += metrics.feedbackSubmitted * 0.05
  if (metrics.feedbackAccepted)
    score += metrics.feedbackAccepted * 0.2

  return Math.min(score, 5.0) // 上限を設定
}

3. OSS化に関する注釈

このC係数の算出ロジックは、透明性と信頼性を高めるために部分的にオープンソース化することが推奨されます。

評価軸と重み付けの構造は公開

閾値や係数の調整ロジックは非公開または抽象化

GitHub上で改善提案を受け付けることで、DAO的進化の余地を残す

この方針により、中央運営型のsfr.tokyoでも制度の信頼性と柔軟性を両立できます。