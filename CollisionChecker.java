// CollisionChecker.java
public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(GamePanel gp) {
        // プレイヤーの現在の世界座標（当たり判定の枠を含めた位置）
        int playerLeftX = gp.playerX + gp.playerSolidArea.x;
        int playerRightX = gp.playerX + gp.playerSolidArea.x + gp.playerSolidArea.width;
        int playerTopY = gp.playerY + gp.playerSolidArea.y;
        int playerBottomY = gp.playerY + gp.playerSolidArea.y + gp.playerSolidArea.height;

        // プレイヤーの四辺がどのマス（行列）に位置しているかを計算
        int playerLeftCol = playerLeftX / gp.tileSize;
        int playerRightCol = playerRightX / gp.tileSize;
        int playerTopRow = playerTopY / gp.tileSize;
        int playerBottomRow = playerBottomY / gp.tileSize;

        int tileNum1, tileNum2;

        // プレイヤーが押している方向キーごとに、一歩先のマスを予測チェックする
        if (gp.keyH.upPressed) {
            // 上に動いた場合の予測Y座標から、調べるマスの行を割り出す
            int nextTopY = playerTopY - gp.playerSpeed;
            playerTopRow = nextTopY / gp.tileSize;
            
            // プレイヤーの左上の角と、右上の角が触れているタイルの番号を取得
            tileNum1 = gp.tileM.mapTileNum[playerLeftCol][playerTopRow];
            tileNum2 = gp.tileM.mapTileNum[playerRightCol][playerTopRow];
            
            // どちらかのタイルが「衝突あり(collision = true)」なら、移動フラグを折る
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.keyH.upPressed = false; // キーが押されていないことにして移動を相殺
            }
        }
        if (gp.keyH.downPressed) {
            int nextBottomY = playerBottomY + gp.playerSpeed;
            playerBottomRow = nextBottomY / gp.tileSize;
            
            tileNum1 = gp.tileM.mapTileNum[playerLeftCol][playerBottomRow];
            tileNum2 = gp.tileM.mapTileNum[playerRightCol][playerBottomRow];
            
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.keyH.downPressed = false;
            }
        }
        if (gp.keyH.leftPressed) {
            int nextLeftX = playerLeftX - gp.playerSpeed;
            playerLeftCol = nextLeftX / gp.tileSize;
            
            tileNum1 = gp.tileM.mapTileNum[playerLeftCol][playerTopRow];
            tileNum2 = gp.tileM.mapTileNum[playerLeftCol][playerBottomRow];
            
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.keyH.leftPressed = false;
            }
        }
        if (gp.keyH.rightPressed) {
            int nextRightX = playerRightX + gp.playerSpeed;
            playerRightCol = nextRightX / gp.tileSize;
            
            tileNum1 = gp.tileM.mapTileNum[playerRightCol][playerTopRow];
            tileNum2 = gp.tileM.mapTileNum[playerRightCol][playerBottomRow];
            
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.keyH.rightPressed = false;
            }
        }
    }
}