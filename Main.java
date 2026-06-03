import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("2D RPG Base");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // パネルの推奨サイズ（640x480）にウィンドウを合わせる

        window.setLocationRelativeTo(null); // 画面の中央に表示
        window.setVisible(true);

        gamePanel.startGameThread(); // ゲームループ開始！
    }
}