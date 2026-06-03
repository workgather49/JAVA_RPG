import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// KeyListenerインターフェースを実装（implements）します
public class KeyHandler implements KeyListener {

    // どのキーが押されているかを保持するフラグ（true = 押されている）
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // エンターキーが押されたかどうかのフラグ
     public boolean enterPressed;

    @Override
    public void keyTyped(KeyEvent e) {
        // 今回は使いませんが、KeyListenerの実装に必須なため空のまま残します
    }

    // キーが押された瞬間に呼ばれるメソッド
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // 押されたキーの識別番号を取得

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = true; // Wキー または 上矢印キー
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = true; // Sキー または 下矢印キー
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = true; // Aキー または 左矢印キー
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = true; // Dキー または 右矢印キー
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true; // Enterキー
        }
    }

    // キーが離された瞬間に呼ばれるメソッド
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = false; 
        }
    }
}