# MTR JCM Elevator Custom

Minecraft 1.20.1 Forge、MTR 4.0.5、JCM 2.3、YTE (Yunzhu Transit Extension) 向けのエレベーター表示カスタムMODです。YTEの矢印テクスチャ、フォント、上り下りランタン音、Orange Liftスタイルを直接参照します。

## 機能

- Lift Refresherに設定画面を追加し、「矢印」「階数表示」「チャイム」のタブでLiftごとに設定
- 上下方向はMTRのLiftDirectionから取得。YTEと同じ移動方向を表示し、停止中は矢印を消す
- YTEの矢印テクスチャと数字フォントを選択可能。各方向のPNG、独自TTF/OTFで個別に上書き可能
- 現在階の表示を切り替え可能
- YTEの上り・下りランタン音を個別に選択可能。方向は最後に検出した移動方向に連動
- 上り・下りチャイムを別々のOGGで上書き可能
- 設定は1秒以内に反映し、矢印・フォント・階数表示の変更ではリソース再読み込み不要
- 指定したPNG/TTF/OTFはスクリプトから直接読み込む。YTEサウンドの選択は即時反映し、OGG上書きだけはサウンド定義の再構築が必要なため「Apply chime files」を一度押す

YTEはMITライセンスです。YTEのアセットを同梱せず、インストール済みYTEのリソースIDを参照します。

## 設定方法

1. クライアントにMinecraft 1.20.1 Forge、MTR 4.0.5、JCM 2.3、YTEを導入します。
2. MTRのLift Refresherで対象エレベーターを開き、追加された`Elevator Media Settings`を選びます。
3. タブから矢印画像・フォント・階数表示・上下チャイムを設定します。ファイル指定はゲーム画面にパスを入力します。
4. Lift Refresherでは`JCM Elevator Custom`またはYTEの`Orange Lift`を選びます。どちらも通常のLift表示を残したまま、矢印と階数をスクリプトで重ねます。
5. 矢印・フォントの設定は自動で反映されます。OGGを指定した場合だけ、チャイムタブで上下の音を設定した後に`Apply chime files`を押します。この操作で生成パックを有効にしてサウンドを一度再読み込みします。

選択内容は`.minecraft/config/mtr_jcm_elevator_media.json`に保存されます。カスタム画像、フォント、音声と`media_config.js`、`sounds.json`は`.minecraft/resourcepacks/MTR_JCM_Elevator_Custom`以下に生成されます。

