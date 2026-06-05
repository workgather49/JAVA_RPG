// MON_Slime.java
import java.awt.Color;
import java.awt.Graphics2D;

// --- 【追加】画像読み込み用のインポート ---
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

import java.awt.Rectangle;

public class Player extends Entity{
    KeyHandler keyH;
    GamePanel gp;

    /*
    勇者の画像設定
    */
    public BufferedImage playerSheet;
    public BufferedImage playerImage; // 切り抜いた1コマを保存する変数

    public Player(GamePanel gp, KeyHandler keyH) {
        this.keyH = keyH;
        this.gp = gp;

        // 主人公の初期位置や初期ステータスを設定
        x = gp.tileSize * 2; // 例: 2マス目の位置
        y = gp.tileSize * 3;
        speed = 4;
        direction = "down";

        // ステータス設定など...
        name = "勇者";
        maxHp = 100;
        hp = maxHp;
        attack = 20;
        defense = 5;

        // 主人公用の当たり判定エリア（必要に応じてサイズを調整してください）
        // 例: 48x48のタイルの中で、少し小さめの四角形にする場合
        solidArea = new Rectangle(8, 16, 32, 32);

        // player画像を読み込む
        getPlayerImage();
    }

    public void update() {
        // 何かしらの方向キーが押されているときだけ処理する（そうしないと、何も押していないのに常に衝突判定が走ってしまいます）
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
        
            // 1. キー入力に応じて、現在の方向（direction）を決定する
            if (keyH.upPressed)    { direction = "up"; }
            if (keyH.downPressed)  { direction = "down"; }
            if (keyH.leftPressed)  { direction = "left"; }
            if (keyH.rightPressed) { direction = "right"; }

            // 2. 衝突フラグをリセットし、自分自身（this = プレイヤー）を渡してチェック！
            collisionOn = false;
            gp.cChecker.checkTile(this); // ★リファクタリングしたメソッドが美しくハマります！

            // 3. 衝突していなければ、その方向へ座標を更新する
            if (collisionOn == false) {
                switch (direction) {
                    case "up":    y -= speed; break;
                    case "down":  y += speed; break;
                    case "left":  x -= speed; break;
                    case "right": x += speed; break;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        // プレイヤーを描く
        // --- 【変更】プレイヤーを白い四角形からドット絵画像に変更 ---
        if(playerImage != null) {
            //画像都合により、そのままの向き（右向き）
            if (direction.equals("right") || direction.equals("up") || direction.equals("down")){ // ★ keyH. を取る
                // drawImage(画像, X座標, Y座標, 描画する横幅, 描画する高さ, null)
                // 元の画像が大きくても、ここで tileSize(48) を指定すれば自動で48x48に縮小されて綺麗に収まります
                //g2.drawImage(playerImage, playerX, playerY, tileSize, tileSize, null);
                g2.drawImage(playerImage, x, y, gp.tileSize, gp.tileSize, null);
            } else if (direction.equals("left")) { // ★ keyH. を取る
                // 左右反転して描画（左向き）
                // g2.drawImage(playerImage, playerX + tileSize, playerY, -tileSize, tileSize, null);
                g2.drawImage(playerImage, x + gp.tileSize, y, -gp.tileSize, gp.tileSize, null);
            }
        }else{
            // 万が一画像がない場合のバックアップとして四角形を残しておく
            g2.setColor(Color.WHITE);
            //g2.fillRect(playerX, playerY, tileSize, tileSize); // 白い四角形
            g2.fillRect(x, y, gp.tileSize, gp.tileSize); // 白い四角形
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
            //System.out.println("デバッグ用：playerImageの値：" + playerImage);
            
        } catch (IOException e) {
            System.out.println("画像の読み込みに失敗しました。パスを確認してください。");
            e.printStackTrace();
        }
    }
}
