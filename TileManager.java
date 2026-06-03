import java.awt.Color;
import java.awt.Graphics2D;

public class TileManager {

    GamePanel gp;
    
    // マップのデータ（0: 草地、1: 壁）
    // 横14マス、縦10マスの2次元配列です
    int mapTileNum[][];

    // TileManager.java の上部メンバ変数に追加
    public Tile[] tile;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        mapTileNum = new int[gp.maxScreenCol][gp.maxScreenRow];

        // TileManager のコンストラクタ（public TileManager...）の中に以下を追加
        tile = new Tile[10]; // とりあえず10種類分のタイルの部屋を用意
        tile[0] = new Tile(); // 草地
        tile[0].collision = false;

        tile[1] = new Tile(); // 壁
        tile[1].collision = true; // ★壁は衝突フラグをtrueにする
        
        // マップの初期化
        initMap();
    }

    public void initMap() {
        // テスト用のマップデータ
        // 周りを1（壁）で囲み、中を0（草地）にしています
        int testMap[][] = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,1,1,0,0,0,0,0,0,0,1}, // 真ん中にちょっと壁を配置
            {1,0,0,1,0,1,0,0,0,0,0,0,0,1},
            {1,0,0,1,1,1,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };

        // 配列データを入れ替えて保持する（行と列のインデックスに注意）
        for(int row = 0; row < gp.maxScreenRow; row++) {
            for(int col = 0; col < gp.maxScreenCol; col++) {
                mapTileNum[col][row] = testMap[row][col];
            }
        }
    }

    // マップを描画するメソッド
    public void draw(Graphics2D g2) {
        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        // 画面全体をループして、マス目ごとにタイルを描く
        while(col < gp.maxScreenCol && row < gp.maxScreenRow) {
            
            int tileType = mapTileNum[col][row];

            // 今は画像がないので、色で区別します
            if(tileType == 0) {
                g2.setColor(new Color(34, 139, 34)); // 森っぽい緑色（草地）
                //g2.setColor(new Color(255, 255, 255)); // 背景：白い(デバッグ用)
            } else if(tileType == 1) {
                g2.setColor(Color.GRAY); // 灰色（壁）
            }

            // タイルのサイズ（48x48）に合わせて四角を描く
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
            
            // 隣のマスへ移動
            col++;
            x += gp.tileSize;

            // 右端まで行ったら、次の行（下の段）へ
            if(col == gp.maxScreenCol) {
                col = 0;
                x = 0;
                row++;
                y += gp.tileSize;
            }
        }
    }
}