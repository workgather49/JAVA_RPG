import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

// --- 【追加】主人公の画像管理用の変数 ---
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GamePanel extends JPanel implements Runnable {

    /*
    // 画面サイズの設定
    final int screenWidth = 640;
    final int screenHeight = 480;
    */

    // --- 【追加・変更】タイルの基本設定 ---
    public final int tileSize = 48; // 48x48ピクセルのタイル
    public final int maxScreenCol = 14; // 横に14マス (14 * 48 = 672)
    public final int maxScreenRow = 10; // 縦に10マス (10 * 48 = 480)

    public final int titleState = 0; // 今回は使いませんが、拡張用に
    public final int playState = 1;  // マップ移動画面
    public final int combatState = 2;// 戦闘画面

    // 画面サイズをマスの数から自動計算するように変更
    final int screenWidth = tileSize * maxScreenCol; // 672ピクセル
    final int screenHeight = tileSize * maxScreenRow; // 480ピクセル

    public int gameState;             // マップ画面か戦闘画面の状態管理用
    public String combatMessage = ""; // 画面に表示するメッセージ

    // 戦闘の進行状態を管理するフラグ
    // 0: コマンド選択中, 1: メッセージ表示中（ボタン待ち）
    public int combatPhase = 0;

    // 戦闘画面のコマンド選択用変数
    public int commandNum = 0; // 0: たたかう, 1: にげる

    /*
    勇者の画像設定
    */
    public BufferedImage playerSheet;
    public BufferedImage playerImage; // 切り抜いた1コマを保存する変数
    
    // GamePanelのメンバ変数に追加（連続衝突を防ぐ無敵時間タイマー）
    int damageCooldown = 0;

    // プレイヤーの当たり判定用のエリア（四角形）
    // 今回はプレイヤーのサイズ（48x48）と同じ大きさの判定を持たせます
    // 当たり判定で使うため、プレイヤー自身のSolidAreaサイズを明示
    public java.awt.Rectangle playerSolidArea = new java.awt.Rectangle(0, 0, 48, 48);

    // --- 【追加】タイルマネージャーのインスタンス ---
    TileManager tileM = new TileManager(this);

    // --- 【追加】キーハンドラーのインスタンスを作成 ---
    KeyHandler keyH = new KeyHandler();

    // --- 【追加】衝突したかどうかのインスタンス ---
    CollisionChecker cChecker = new CollisionChecker(this);

    // GamePanel.java のメンバ変数（tileM や keyH の近く）に追加
    MON_Slime slime = new MON_Slime(this);

    // ゲームのメインスレッド（これがループを回します）
    Thread gameThread;

    // プレイヤーの座標
    int playerX = 200;
    int playerY = 300;

    int playerSpeed = 4; // 1フレームに動くピクセル数

    public String playerName = "勇者";
    public int playerMaxHp = 100;
    public int playerHp = 100;
    public int playerAttack = 20;
    public int playerDefense = 5;
    public String direction = "right";

    /* コンストラクタ */
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // 画面のチラつきを抑える設定

        // --- 【追加】パネルにキー入力を受け付ける設定をする ---
        this.addKeyListener(keyH); // 作成したキーハンドラーを登録
        this.setFocusable(true);    // パネルにフォーカスを当てて入力を有効にする
    
        // --- 【追加】ゲーム開始前に画像を読み込む ---
        getPlayerImage();

        // 最初はマップ移動画面からスタート
        gameState = playState;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start(); // これにより run() メソッドが呼ばれます
    }

    @Override
    public void run() {
        // 1秒間に60回（60FPS）ループを回すための計算
        double drawInterval = 1000000000 / 60.0; // ナノ秒単位
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            
            // 1. データの更新（キャラクターの位置など）
            update();
            
            // 2. 画面の再描画
            repaint(); // これを呼ぶと paintComponent が実行されます

            // 60FPSを維持するための待機（ウェイト）処理
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000; // ミリ秒に変換

                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ゲーム内のデータ更新処理（ここにキャラクター移動などを書く）
    public void update() {
        // テスト用にコンソールに出力（確認できたら消してOKです）
        //System.out.println("ゲームループが動いています...");

        if (gameState == playState) {
            // 1. まず現在のキー入力から「次のマスが壁かどうか」をチェック
            // 壁だった場合は、cCheckerの中で該当する方向のキーフラグが強制的にfalseにされます
            cChecker.checkTile(this);

            // --- 【変更】キーの状態に応じて座標を更新する ---
            if (keyH.upPressed) {
                playerY -= playerSpeed; // 上に行くとY座標は減る
            }
            if (keyH.downPressed) {
                playerY += playerSpeed; // 下に行くとY座標は増える
            }
            if (keyH.leftPressed) {
                playerX -= playerSpeed; // 左に行くとX座標は減る
                direction = "left"; // ★左向き
            }
            if (keyH.rightPressed) {
                playerX += playerSpeed; // 右に行くとX座標は増える
                direction = "right"; // ★右向き
            }

            // 2. 【追加】画面外へのハミ出しをチェックして補正する
            // 四角形のサイズ（paintComponentで指定している 48x48）

            // 左端のチェック
            if (playerX < 0) {
                playerX = 0;
            }
            // 右端のチェック（画面幅 640 からプレイヤーの幅 48 を引いた位置が限界）
            if (playerX > screenWidth - tileSize) {
                playerX = screenWidth - tileSize;
            }
            // 上端のチェック
            if (playerY < 0) {
                playerY = 0;
            }
            // 下端のチェック（画面高 480 からプレイヤーの高さ 48 を引いた位置が限界）
            if (playerY > screenHeight - tileSize) {
                playerY = screenHeight - tileSize;
            }

            // --- 【追加】敵のデータを更新 ---
            slime.update();

            // --- 【追加】プレイヤーとモンスターの衝突・戦闘チェック ---
            checkMonsterCollision();

        } else if (gameState == combatState) {
            // --- 戦闘画面の更新 ---

            // 【フェーズ0: コマンド選択中】
            if (combatPhase == 0) {
                // ここではキー入力を直接チェックしてコマンドを上下させます
                if (keyH.upPressed) {
                    commandNum--;
                    if (commandNum < 0) commandNum = 1; // ループさせる
                    keyH.upPressed = false; // 連打防止のためにフラグを折る
                }
                if (keyH.downPressed) {
                    commandNum++;
                    if (commandNum > 1) commandNum = 0;
                    keyH.downPressed = false;
                }
                
                // 決定キー（今回は仮に D キー、またはお好みのキーで）が押されたら実行
                //if (keyH.rightPressed) { // 右キーかDキーを決定ボタン代わりに
                if (keyH.enterPressed) { // Enterキーが決定ボタン代わりに
                    keyH.enterPressed = false;
                    
                    if (commandNum == 0) {
                        // 【たたかう】を選んだ場合
                        //System.out.println("たたかう を選択：敵にダメージ！");
                        int damage = playerAttack - slime.defense;
                        if (damage < 1) damage = 1;
                        slime.hp -= damage;

                        //攻撃受けたスライムのHPがマイナスではなく、0とする。
                        if(slime.hp < 0) slime.hp = 0;
                        
                        // 画面に表示するメッセージをセット
                        combatMessage = playerName + " の攻撃！ " + slime.name + " に " + damage + " のダメージ！";
                        combatPhase = 1; // メッセージ表示フェーズへ移行

                    } else if (commandNum == 1) {
                        // 【にげる】を選んだ場合
                        combatMessage = "勇者は逃げ出した！";
                        combatPhase = 1; // メッセージ表示フェーズへ移行
                    }
                }
            // 【フェーズ1: メッセージ表示中（決定キー待ち）】
            } else if (combatPhase == 1) {
                if (keyH.enterPressed) {
                    keyH.enterPressed = false;

                    // 逃げるメッセージの後に決定を押した場合
                    if (commandNum == 1) {
                        damageCooldown = 60;
                        gameState = playState;
                        combatPhase = 0; // フェーズを元に戻す
                        combatMessage = "";
                    }
                    // たたかうメッセージの後に決定を押した場合
                    else if (commandNum == 0) {
                        if (slime.hp <= 0) {
                            // 敵を倒していたらマップへ戻る
                            System.out.println("敵を倒した！");
                            slime.x = tileSize * 8;
                            slime.y = tileSize * 2;
                            slime.hp = slime.maxHp;
                            damageCooldown = 90;
                            gameState = playState;
                            combatPhase = 0;
                            combatMessage = "";
                        } else {
                            // 敵が生きていたら、本来はここで敵の反撃フェーズに移りますが、
                            // 今回は簡易的にコマンド選択（プレイヤーのターン）に戻します
                            combatPhase = 0;
                            combatMessage = "";
                        }
                    }
                }
            }
        }
    }

    // 画面の描画処理
    @Override
    protected void paintComponent(Graphics g) {
        // コンストラクタ
        super.paintComponent(g);
        // Graphics2Dは、通常のGraphicsより細かい描画コントロールが可能です
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == playState) {
            // ここに描画の処理を書いていきます
            // --- 【超重要】描画の順番：背景が先、プレイヤーが後！ ---
            // 1. マップを描く
            tileM.draw(g2);

            // --- 【追加】2. 敵を描く(スライム) ---
            slime.draw(g2);

            // プレイヤーを描く
            // --- 【変更】プレイヤーを白い四角形からドット絵画像に変更 ---
            if(playerImage != null) {

                if (direction.equals("right")) { // ★ keyH. を取る
                    // drawImage(画像, X座標, Y座標, 描画する横幅, 描画する高さ, null)
                    // 元の画像が大きくても、ここで tileSize(48) を指定すれば自動で48x48に縮小されて綺麗に収まります
                    g2.drawImage(playerImage, playerX, playerY, tileSize, tileSize, null);
                } else if (direction.equals("left")) { // ★ keyH. を取る
                    // 左右反転して描画（左向き）
                    g2.drawImage(playerImage, playerX + tileSize, playerY, -tileSize, tileSize, null);
                }

            }else{
                // 万が一画像がない場合のバックアップとして四角形を残しておく
                g2.setColor(Color.WHITE);
                g2.fillRect(playerX, playerY, tileSize, tileSize); // 白い四角形
            }
        
        } else if (gameState == combatState) {
            // --- 戦闘画面の描画 ---
            // 1. 背景を真っ黒にする
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);
            
            // --- 【変更】2. 敵を中央に大きく表示（赤四角からスライム画像へ） ---
            if (slime.slimeImage != null) {
                // 中央（screenWidth/2 - 48, screenHeight/3）に、横96, 縦96の大サイズで描画
                g2.drawImage(slime.slimeImage, screenWidth/2 - 48, screenHeight/3, 96, 96, null);
            }
            else{
                // 2. 敵を中央に大きく表示してみる
                g2.setColor(Color.RED);
                g2.fillRect(screenWidth/2 - 48, screenHeight/3, 96, 96);
            }

            g2.setColor(Color.WHITE);
            g2.drawString(slime.name + " (HP: " + slime.hp + ")", screenWidth/2 - 40, screenHeight/3 - 10);

            // 3. コマンドウィンドウの枠線を描く
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRect(50, screenHeight - 150, screenWidth - 100, 110);
            
            // 4. コマンド文字を描く
            g2.setFont(new java.awt.Font("MS Gothic", java.awt.Font.BOLD, 24));
            
            if (combatPhase == 0) {
                g2.drawString("たたかう", 100, screenHeight - 110);
                g2.drawString("にげる", 100, screenHeight - 60);
                
                // 5. 選択しているコマンドの横に「持ち手（カーソル）」となる＞を描く
                if (commandNum == 0) {
                    g2.drawString("＞", 70, screenHeight - 110);
                } else if (commandNum == 1) {
                    g2.drawString("＞", 70, screenHeight - 60);
                }
            // 【フェーズ1なら、選択肢を消してメッセージと「▼（次に進む合図）」を描画】
            } else if (combatPhase == 1) {
                // 設定されたメッセージをウィンドウ内に表示
                g2.drawString(combatMessage, 80, screenHeight - 90);
        
                // 右下に「次のページへ」を意味する小さな下向きの矢印（▼）を点滅、または表示させる
                g2.setFont(new java.awt.Font("MS Gothic", java.awt.Font.BOLD, 16));
                g2.drawString("▼", screenWidth - 80, screenHeight - 60);
            }
        }
        g2.dispose(); // メモリ解放
    }

    // GamePanelクラス内（updateメソッドの下など）に新しいメソッドを追加
    public void checkMonsterCollision() {
        
        // プレイヤーの現在の当たり判定四角形を作成
        java.awt.Rectangle pRect = new java.awt.Rectangle(playerX + playerSolidArea.x, playerY + playerSolidArea.y, playerSolidArea.width, playerSolidArea.height);
        
        // 敵の現在の当たり判定四角形を作成
        java.awt.Rectangle mRect = new java.awt.Rectangle(slime.x + slime.solidArea.x, slime.y + slime.solidArea.y, slime.solidArea.width, slime.solidArea.height);

        // クールダウン（無敵時間）のカウントを進める
        if (damageCooldown > 0) {
            damageCooldown--;
        }

        // ぶつかったら戦闘画面へ遷移
        if (pRect.intersects(mRect) && damageCooldown == 0) {
            gameState = combatState; // ★戦闘画面に切り替え！
            commandNum = 0;          // コマンドの選択位置を初期化
        }
    }

    public void getPlayerImage() {
        try {
            // resフォルダから画像を読み込む
            playerSheet = ImageIO.read(getClass().getResourceAsStream("/res/player_sheet.png"));
            
            // --- 【修正】切り抜き位置の微調整 ---
            // 左側の「JUMP」「RUN」などの文字を飛び越えるためのX座標（ピクセル単位）
            // ※もしこれでもズレる場合は、この170という数値を150や190に調整してみてください
            // 左にずれる場合はstartXの値を小さく、右にずれる場合はstartXの値を大きくする
            int startX = 260;
            int runRowY = 60; // 2段目のY座標(初期化：128) 小さくすると(上)、大きくすると(下)

            // RUN（走る）は上から2段目。
            // 1コマのサイズが 128x128 だと仮定すると：
            // 1コマ目の左上座標は X=0, Y=128 になります。
            int spriteWidth = 150;
            int spriteHeight = 150;
            
            // getSubimage(X座標, Y座標, 横幅, 高さ) で走るポーズの1コマ目を切り抜く
            playerImage = playerSheet.getSubimage(startX, runRowY, spriteWidth, spriteHeight);
            
        } catch (IOException e) {
            System.out.println("画像の読み込みに失敗しました。パスを確認してください。");
            e.printStackTrace();
        }
    }
}