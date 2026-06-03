// Entity.java (書き換え・追加部分)
import java.awt.Rectangle;

public class Entity {
    public int x, y;
    public int speed;
    public String direction = "down"; 
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
    public boolean collisionOn = false;

    // --- 【追加】RPGの基本ステータス ---
    public int maxHp;
    public int hp;
    public int attack;
    public int defense;
    public String name; // キャラクターの名前
}