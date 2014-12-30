import processing.core.*;
import L3D.*;
import java.util.*;
import java.awt.Color;

public class Ship {
  private final int COLOR = (new Color(0, 0, 255)).getRGB();
  
  private int x, z;
  private final int y = 7;
  private boolean shoot;
  
  public Ship(int _x, int _z) {
    x = _x;
    z = _z;
    
    shoot = false;
  }
  
  public void update(int time, List<Shot> shots) {
    if(shoot) {
      shots.add(new ShipShot(x, y-1, z));
      shoot = false;
    }
  }
  
  public void render(L3D cube) {
    cube.setVoxel(new PVector(x, y, z), COLOR);
  }
  
  public void moveLeft() {
    if(x > 0) x--;
  }
  
  public void moveRight() {
    if(x < 7) x++;
  }
  
  public void moveBack() {
    if(z < 7) z++;
  }
  
  public void moveForward() {
    if(z > 0) z--;
  }
  
  public void shoot() {
    shoot = true;
  }
   
  class ShipShot extends Shot {
    public ShipShot(int x, int y, int z) {
      super(x, y, z, -1);
      myColor = (new Color(255, 255, 0)).getRGB();
    }
  }
}
