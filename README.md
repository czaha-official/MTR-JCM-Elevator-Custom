# MTR JCM Elevator Custom

Minecraft **1.20.1 / Forge 47.x** 向けの、MTRエレベーター単位でメディア設定を管理するクライアントMODです。

## 対応環境

- Minecraft 1.20.1 / Java 17 / Forge 47.x
- MTR 4.0.5
- Lift Customization Screen の追加ボタンを利用するには、対応するMTR版が必要
- YTEの既存テクスチャ・フォント・効果音IDを候補として利用（YTEのアセットは同梱しません）

## 現在実装済みの機能

- Lift Refresher 内に「Elevator Media Settings」を追加
- リフトIDごとに上り/下り矢印、階数表示切替、フォント、上り/下りチャイムを設定
- PNG・TTF・OTF・OGGファイルのパス指定と生成リソースパックへの取り込み
- ファイル形式（ヘッダー）・32 MiBまでのサイズ・リフトIDの検査
- 生成した sounds.json に対応するカスタムチャイムのイベントIDを保存
- 画面幅を考慮したレイアウトとリソースパック再読み込みボタン
- 設定を .minecraft/config/mtr_jcm_elevator_media.json に保存

## 現在の重要な制限

**このリポジトリには、エレベーター本体の表示・音声再生を変更するレンダラー/イベントフックがまだありません。**
現時点では設定UI・ファイル管理・リソースパックの生成までの実装です。
生成した assets/mtr/js/mtr_jcm_ev/media_config.js を読み込むスクリプトの登録もありません。

したがって、矢印・数字・フォント・音声が**ゲーム内のエレベーターへ実際に反映される**
とはまだ保証できません。YTEの素材IDを選択できることと、実際にYTEの表示や
音声を上書きできることは別です。表示・再生ロジックを追加したあと、実機で検証してください。

ファイルの追加・変更をゲームのリソースマネージャーへ反映するには、各タブの
**Apply media pack / reload** を押してください。初めて生成したパックも、この操作で選択して再読み込みします。
media_config.js の変更が自動的に1秒以内に反映されるわけではありません。

## 操作手順

1. MTR 4.0.5と対応する環境でMODを起動します。
2. Lift Refresherからエレベーターのカスタマイズ画面を開き、「Elevator Media Settings」を選びます。
3. Arrows / Numbers / Chimesのタブで設定を変更します。矢印画像や音声を別ファイルにする場合はフルパスを入力します。
4. ファイルを追加したら「Apply media pack / reload」で生成パックを有効化します。
5. resourcepacks/MTR_JCM_Elevator_Custom/ の生成物と config/mtr_jcm_elevator_media.json を確認します。

## 保存構造

- 設定: config/mtr_jcm_elevator_media.json
- 生成パック: resourcepacks/MTR_JCM_Elevator_Custom
- 矢印: assets/mtr_jcm_ev/textures/lift/<liftId>/{up,down}.png
- フォント: assets/mtr_jcm_ev/font/<liftId>/digits.ttf または .otf
- 音声: assets/mtr_jcm_ev/sounds/lift/<liftId>/chime_{up,down}.ogg
- サウンドイベント: mtr_jcm_ev:lift_<liftId>_{up,down}_chime

生成ファイルはゲームクライアント側に保存されます。サーバーや他プレイヤーへの自動同期は行われません。

## ビルド

Java 17環境でルートのGradle Wrapperを使用し、以下を実行してください。

    ./gradlew :forge:build

依存MODはModrinth/Mavenから取得するため、初回はネットワークアクセスが必要です。
ForgeのMOD定義は forge/src/main/resources/META-INF/mods.toml、
Mixinの設定は forge/src/main/resources/mtr_jcm_ev.mixins.json にあります。

## 検証時の注意

- 1.20.1 Forgeで起動してMOD一覧に表示されるか
- Lift Refresherの画面から設定UIが開き、Esc / Done / タブ切替が戻れるか
- 小さなGUIスケールでボタンが重ならないか
- PNG/TTF/OTF/OGGの正常・異常ファイルを選択した結果
- 生成パックが選択されていて、F3+T後にも sounds.json が読み込まれるか
- まだ未実装のレンダラーと音声フックを接続した後、方向転換・停止・階数更新・チャイム再生を検証すること

ライセンス: MIT（詳細は LICENSE）。YTEのアセットは本MODに含みません。
