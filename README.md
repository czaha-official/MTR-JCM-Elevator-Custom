# MTR JCM Elevator Custom for MJX

Minecraft **1.20.1 / Forge 47.x / Java 17** 用エレベーター表示・音声カスタマイズMODです。
MTR 4.0.5 と YTE (Yunzhu Transit Extension) のインストールを想定しています。

## 主な機能

- MTRのLift Refresherの画面に **Media Settings** ボタンを追加
- リフトごとに上昇/下降矢印を個別選択（YTEの候補または任意のPNG）
- 現在階の表示ON/OFF
- 現在階の表示フォントを個別選択（YTEのTTF/OTFまたは任意のTTF/OTF）
- 上昇/下降の到着チャイムを個別選択（YTEのサウンドイベントまたは任意のVorbis OGG）
- 移動中のみ矢印を表示、停止時は矢印を消す
- リフトの移動方向がUP/DOWNからNONEに変化したとき、近くで到着チャイムを1回再生
- 設定JSONとMinecraft 1.20.1用リソースパックを自動生成
- 小さいGUIスケールに対応するようレイアウトを調整
- 異常な画像サイズ、未対応OGGコーデック、破損したJSON、不正なパスの防御

## 仕組み

**矢印と階数の描画はMTRの `RenderLifts.renderLiftDisplay` にMixinで連携しています。**
設定がないリフトでは元のMTR描画を変更しません。設定済みリフトのみ矢印と階数を差し替えます。
JCMが独自スクリプト制御によってMTR標準モデルの描画自体を停止するLiftスタイルの場合は、
MTR標準表示も呼ばれないため、この方式の描画対象外です。

到着音はクライアントのLiftDirectionを監視します。初回ロード時には鳴らさず、
移動終了（UP/DOWNからNONEへの遷移）時に近距離のクライアントだけで再生します。
**到着音と画像・フォント設定はクライアントごとに保存され、マルチプレイサーバーへ自動同期しません。**

## 使用方法

1. Forge 1.20.1、MTR 4.0.5、YTEを導入します。
2. Lift Refresherで対象リフトの設定画面を開き、「Media Settings」を押します。
3. Arrows / Numbers / Chimesのタブで設定を変更します。
4. 任意ファイルを指定する場合はそのファイルの絶対パスを入力します。
5. **Apply media pack / reload** を押して生成パックを有効化・再読み込みします。

設定は `.minecraft/config/mtr_jcm_elevator_media.json`、
生成リソースパックは `.minecraft/resourcepacks/MTR_JCM_Elevator_Custom/` に保存されます。

### 入力ファイルと制限

- 矢印：PNG（最大4096×4096画素）
- フォント：TTF / OTF
- チャイム：**Ogg Vorbis**（Opus形式のOGGはMinecraft標準音声エンジンでは非対応）
- 各ファイル：最大32 MiB

読み込み時にマジックバイトを検査しますが、あらゆる画像や音声の完全なデコード検証は行いません。

## リソースパックの保存先

- `assets/mtr_jcm_ev/textures/lift/<ID>/up.png`
- `assets/mtr_jcm_ev/textures/lift/<ID>/down.png`
- `assets/mtr_jcm_ev/font/<ID>/digits.ttf`（またはotf）
- `assets/mtr_jcm_ev/font/lift_<ID>.json`（使用フォント定義）
- `assets/mtr_jcm_ev/sounds/lift/<ID>/chime_up.ogg`
- `assets/mtr_jcm_ev/sounds/lift/<ID>/chime_down.ogg`
- `assets/mtr_jcm_ev/sounds.json`

生成パックのリロードが必要なのは、外部ファイルを変更した場合、およびMinecraftの
テクスチャ・フォント・サウンド定義を再取得する場合です。
すでに読み込まれているYTE画像・フォント・音声の選択変更は設定ファイルへの書き込み後に参照されます。

## ビルド

```bash
./gradlew :forge:build
```

ビルド環境と依存MODの解決が必要です。MTRの画面APIとRenderLiftsのシグネチャは
対象バージョンと一致していることが前提です。

## 現時点で未検証の事項

- Forge環境での最終的なビルド成功、実機起動、Minecraftログ上のMixin適用
- YTEの全テクスチャ候補の実在と、全音声候補の音量・距離特性
- GUIの全スケールでの重なり、フォントの見え方、音量
- JCMのスクリプト制御Liftスタイルと他の表示差し替えMODの競合
- モーション終端でLiftDirectionがNONEに切り替わるタイミング

**CodeQL解析の成功のみでは実機動作を保証できません。**

## ライセンス

MIT（LICENSE参照）。YTEの素材を配布せず、インストール済みYTEのリソースIDを参照します。
