import processing.core.*;
import L3D.*;
import java.util.*;
import java.awt.Color;

public class Ship {
  private final int COLOR = (new Color(0, 0, 255)).getRGB();
  
  private PVector pos;
  private boolean shoot;
  private boolean alive;
  
  public Ship(int x, int z) {
    pos = new PVector(x, 7, z);
    
    alive = true;
    shoot = false;
  }
  
  public void update(int time, List<Shot> shots) {
    for(Shot s: shots) {
      if(s instanceof Alien.AlienShot) {
        if((int)pos.x == s.getX() && (int)pos.y == s.getY() && (int)pos.z == s.getZ()) {
          alive = false;
          s.kill();
        }
      }
    }
    
    if(shoot) {
      shots.add(new ShipShot((int)pos.x, (int)(pos.y-1), (int)pos.z));
      shoot = false;
    }
  }
  
  public void render(L3D cube) {
    cube.setVoxel(pos, COLOR);
  }
  
  public boolean isAlive() {
    return alive;
  }
  
  public void moveLeft() {
    if(!alive) return;
    if(pos.x > 0) pos.x--;
  }
  
  public void moveRight() {
    if(!alive) return;
    if(pos.x < 7) pos.x++;
  }
  
  public void moveBack() {
    if(!alive) return;
    if(pos.z < 7) pos.z++;
  }
  
  public void moveForward() {
    if(!alive) return;
    if(pos.z > 0) pos.z--;
  }
  
  public void shoot() {
    if(!alive) return;
    shoot = true;
  }
   
  class ShipShot extends Shot {
    public ShipShot(int x, int y, int z) {
      super(x, y, z, -1);
      myColor = (new Color(255, 255, 0)).getRGB();
    }
  }
}
