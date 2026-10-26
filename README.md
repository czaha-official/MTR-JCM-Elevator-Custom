# MTR JCM Elevator Custom for MJX

Minecraft **1.20.1 / Forge 47.x / Java 17** 用エレベーター表示・音声カスタマイズMODです。
MTR 4.0.5、JCM（Joban Client Mod 2.x / MOD ID: jsblock）、YTE (Yunzhu Transit Extension) のインストールを想定しています。

## 主な機能

- MTRのLift Refresherの画面に **Media Settings** ボタンを追加
- リフトごとに上昇/下降矢印を個別選択（YTEの候補または任意のPNG）
- 現在階の表示ON/OFF
- 現在階の表示フォントを個別選択（YTEのTTF/OTFまたは任意のTTF/OTF）
- 上昇/下降の到着チャイムを個別選択（YTEのサウンドイベントまたは任意のVorbis OGG）
- 移動中のみ矢印を表示、停止時は矢印を消す
- JCMの到着判定と各階のチャイム許可設定に従い、選択した方向だけチャイムを置換
- 設定JSONとMinecraft 1.20.1用リソースパックを自動生成
- 小さいGUIスケールに対応するようレイアウトを調整
- 異常な画像サイズ、未対応OGGコーデック、破損したJSON、不正なパスの防御

## 仕組み

**矢印と階数の描画はMTRの `RenderLifts.renderLiftDisplay` にMixinで連携しています。**
設定がないリフトでは元のMTR描画を変更しません。設定済みリフトのみ矢印と階数を差し替えます。
JCMのスクリプト制御Liftは、標準MTRの表示位置・表示サイズと異なることがあります。**そのモデルにMTRの階数表示を強制的に重ねると、黒い画面に赤い「0」が浮いたりモデル表示が崩れたりします。** このため、スクリプト制御Liftへの追加描画は初期状態ではOFFです。

設定画面の **Script** タブから「Scripted arrow overlay」を明示的にONにすると、JCMスクリプト式のかごに矢印のみを重ね描画できます。**モデル固有の画面位置への自動追従ではなく、標準MTR基準位置を使うため、見た目が合わない場合はOFFにしてください。** スクリプト式モデルの階数・背景・内蔵矢印はJCM側スクリプトが所有しており、本MODから汎用的に安全に差し替えることはできません。

到着音はJCM本体の `JCMPatchForMTR.liftJustArrived` の判定に連携します。
音声設定があるリフトでは、**指定した方向だけ**JCMの標準チャイムを選択音に置き換えます（対象フロアのチャイム許可設定が有効な場合）。移動方向を特定できないときや、選択音を読み込めないときはJCM標準音を維持します。
YTE側の到着チャイムはYTEのサウンドイベントを参照し、取り込みOGGは自動生成リソースパックから再生します。
**到着音と画像・フォント設定はクライアントごとに保存され、マルチプレイサーバーへ自動同期しません。**

## 使用方法

1. Forge 1.20.1、MTR 4.0.5、JCM 2.x、YTEを導入します。
2. Lift Refresherで対象リフトの設定画面を開き、「Media Settings」を押します。
3. Arrows / Numbers / Chimesで設定を変更します。**Scriptタブの重ね描画は通常OFFを推奨**します。
4. 任意ファイルを指定する場合はそのファイルの絶対パスを入力します。
5. **Apply media pack / reload** を押して生成パックを有効化・再読み込みします。
6. Chimesタブの **Test selected sounds** を開き、**Test selected UP / DOWN** で音源を直接確認できます。どのGUIスケールでも試聴ページを利用できます。
7. ArrowsのYTE矢印を選ぶと、以前指定したカスタムPNGの優先設定を自動解除します。NumbersのYTEフォントを選ぶと、以前指定したTTF/OTFが解除されます。「MTR default font」で標準MTRフォントに戻せます。
8. `Sound not loaded` が表示される場合、リソースパックとYTEの導入・有効化を確認してください。読み込めない音源が選択されているとJCM標準音にフォールバックします。

設定は `.minecraft/config/mtr_jcm_elevator_media.json`、
生成リソースパックは `.minecraft/resourcepacks/MTR_JCM_Elevator_Custom/` に保存されます。

### 入力ファイルと制限

- 矢印：PNG（入力は最大4096×4096画素、保存する画像は最大512pxに自動縮小）
- フォント：TTF / OTF
- チャイム：**Ogg Vorbis**（Opus形式のOGGはMinecraft標準音声エンジンでは非対応）
- 各ファイル：最大32 MiB

PNGは画像としてデコード可能か確認し、大きな画像を縮小します。音声とフォントは基本ヘッダー検証のみで、全ファイルの完全なデコード検証は行いません。

## Minecraft向け軽量化

- 描画ごとのJSON設定照会・ロック・テクスチャIDの解析をやめ、設定変更時に参照用のデータを準備します。
- 階数表示のフォント整形を直近の階数についてキャッシュし、同じ階を連続描画する際の生成処理を減らします。
- JCM本体の到着判定を再利用するため、毎tickの独立したエレベーター巡回とドア状態の監視を廃止しています。
- 初回設定の読み込みはクライアントtickで実行し、描画処理中に設定ファイルを読み込みません。
- 同じ内容の生成パックファイルは再書き込みしません。使われなくなった自動生成フォント定義も削除します。
- **新しく取り込むPNGは、縦横の長辺が512pxを超える場合のみ縮小**します。既存の大きな画像は自動では変更されないため、縮小したい場合は再取り込みしてください。
- Mixinの詳細デバッグ出力を通常は無効にしています。

数値上のFPS改善は実機測定が必要です。これらの変更はCPU負荷・不要な割り当て・
テクスチャ使用メモリを抑える設計上の最適化であり、FPS向上を保証するものではありません。

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

- 実機起動、Minecraftログ上のMixin適用とJCMとの注入順序
- YTE音声候補の実機再生時の音量・距離特性（リソースIDはYTEソースと照合済み）
- GUIの全スケールでの重なり、フォントの見え方、音量
- JCMスクリプト制御Liftに明示的に重ね描画したときのモデルごとの座標調整と、他の表示差し替えMODとの競合
- 実機のJCM到着判定タイミング、方向判定と、選択音への実際の置換

GitHub ActionsのForgeビルドとJAR内容スモークテストを行っています。**コンパイル成功でも実機動作・Mixinの正常適用は保証できません。**

## ライセンス

MIT（LICENSE参照）。YTEの素材を配布せず、インストール済みYTEのリソースIDを参照します。
