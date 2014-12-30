import processing.core.*;
import L3D.*;

public abstract class Shot {
  private PVector pos;
  private int vy;
  private boolean alive;
  
  private final static int SPEED = 1;
  protected int myColor;
  private int lastMoveTime;
  
  public Shot(int x, int y, int z, int _vy) {
    pos = new PVector(x, y, z);
    vy = _vy;
    lastMoveTime = 0;
    alive = true;
  }
  
  public void update(int time) {
    if(time - lastMoveTime > SPEED) {
      pos.y += vy;
      
      if(pos.y < 0 || pos.y > 7)
        alive = false;
      
      lastMoveTime = time;
    }
  }
  
  public void render(L3D cube) {
    cube.setVoxel(pos, myColor);
  }
  
  public boolean isAlive() {
    return alive;
  }
  
  public void kill() {
    alive = false;
  }
  
  public int getX() { return (int)pos.x; }
  public int getY() { return (int)pos.y; }
  public int getZ() { return (int)pos.z; }
}
