// MON_Slime.java
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

// --- 【追加】画像読み込み用のインポート ---
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MON_Slime extends Entity {
    GamePanel gp;
    private int actionLockCounter = 0; // 行動を切り替えるためのタイマー
    
    // --- スライムの画像保持用変数 ---
    public BufferedImage slimeImage;

    public MON_Slime(GamePanel gp) {
        this.gp = gp;

        speed = 1; // プレイヤーより少し遅め
        x = gp.tileSize * 5; // 初期位置：5マス目
        y = gp.tileSize * 5; // 初期位置：5マス目

        name = "スライム";
        speed = 1;
        x = gp.tileSize * 5;
        y = gp.tileSize * 5;

        // --- 【追加】スライムのステータス ---
        maxHp = 30;
        hp = maxHp;
        attack = 10;   // 攻撃力
        defense = 2;   // 防御力

        // --- 【追加】起動時に画像を読み込む ---
        getSlimeImage();
    }

// --- 【追加】画像を切り出すメソッド ---
    public void getSlimeImage() {
        try {
            // 画像シートを読み込む
            BufferedImage sheet = ImageIO.read(getClass().getResourceAsStream("/res/slime_sheet.png"));
            
            // 左側の「IDLE」という文字を飛び越えるためのX座標
            // ※男の子の時と同様、ズレる場合はこの数値を調整します
            // 左にずれる場合はstartXの値を小さく、右にずれる場合はstartXの値を大きくする
            int startX = 250; // 初期：140
            int startY = 135;  // 1段目のスライムの頭の上の位置 初期：30
            
            // スライム1コマの切り出しサイズ
            int spriteWidth = 150;  // 初期：100
            int spriteHeight = 150;  // 初期：80
            
            // 1段目（IDLE）の1コマ目を切り抜く
            slimeImage = sheet.getSubimage(startX, startY, spriteWidth, spriteHeight);
            
        } catch (IOException e) {
            System.out.println("スライムの画像読み込みに失敗しました。");
            e.printStackTrace();
        }
    }

    // 敵の行動AI（1秒間に60回呼ばれます）
    public void setAction() {
        actionLockCounter++;

        // 120フレーム（2秒）ごとに移動方向をランダムに決定
        if (actionLockCounter == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1; // 1〜100の乱数

            if (i <= 25) { direction = "up"; }
            if (i > 25 && i <= 50) { direction = "down"; }
            if (i > 50 && i <= 75) { direction = "left"; }
            if (i > 75 && i <= 100) { direction = "right"; }

            actionLockCounter = 0; // カウンターをリセット
        }
    }

    public void update() {
        setAction(); // 方向を決める

        // 1. 衝突フラグをリセットし、衝突判定を実行
        collisionOn = false;
        gp.cChecker.checkTile(this);

        // 決まった方向へ進む
        if (direction.equals("up")) { y -= speed; }
        if (direction.equals("down")) { y += speed; }
        if (direction.equals("left")) { x -= speed; }
        if (direction.equals("right")) { x += speed; }

        // 【簡易的な画面外・壁抜け防止】
        if (x < gp.tileSize) x = gp.tileSize;
        if (x > gp.screenWidth - gp.tileSize * 2) x = gp.screenWidth - gp.tileSize * 2;
        if (y < gp.tileSize) y = gp.tileSize;
        if (y > gp.screenHeight - gp.tileSize * 2) y = gp.screenHeight - gp.tileSize * 2;
    }

    /*
    スライムの描画用メソッド
    */
    public void draw(Graphics2D g2) {

        // --- 【変更】赤い四角形から、ドット絵画像を描画するように変更 ---
        if (slimeImage != null) {

            // 右に進んでいる、または上下に進んでいるときはそのままの向き（右向き）
            if (direction.equals("right") || direction.equals("up") || direction.equals("down")) {
                // マップ上ではプレイヤーと同じtileSize(48x48)に縮小して描画します
                g2.drawImage(slimeImage, x, y, gp.tileSize, gp.tileSize, null);
            }else if(direction.equals("left")){
                // X座標（x）に横幅（gp.tileSize）を足した位置から描き始め、
                // 横幅の指定をマイナス（-gp.tileSize）にすることで左右反転します
                g2.drawImage(slimeImage, x + gp.tileSize, y, -gp.tileSize, gp.tileSize, null);
            }
        } else {
            // 画像が読めこめなかった用
            // スライムっぽく赤色の四角形で描画
            g2.setColor(Color.RED);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
}