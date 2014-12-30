import processing.core.*;
import L3D.*;
import java.awt.Color;

public class Base {
  private final int COLOR_DESTROYED = (new Color(0, 0, 0)).getRGB();
  private final int COLOR_CRITICAL = (new Color(255, 0, 0)).getRGB();
  private final int COLOR_HEALTHY = (new Color(0, 255, 0)).getRGB();
  
  private final int distanceFromBottom = 2;
  
  private int[][] health;
  private PVector pos;
  private int w, h;
  
  public Base(int _x, int _y) {
    this(_x, _y, 2, 2);
  }
  
  public Base(int x, int z, int _width, int _height) {
    pos = new PVector(x, 7-distanceFromBottom, z);
    w = _width;
    h = _height;
    
    health = new int[w][h];
    
    for(int offZ=0; offZ < h; offZ++)
      for(int offX=0; offX < w; offX++)
        health[offX][offZ] = 2;
  }
  
  public void render(L3D cube) {
    for(int offZ=0; offZ < h; offZ++) {
      for(int offX=0; offX < w; offX++) {
        int healthColor = healthToColor(health[offX][offZ]);
        
        int drawX = (int)(pos.x + offX);
        int drawY = (int)(pos.y);
        int drawZ = (int)(pos.z + offZ);
        
        cube.setVoxel(new PVector(drawX, drawY, drawZ), healthColor);
      }
    }
  }
  
  private int healthToColor(int health) {
    if(health == 0) {
      return COLOR_DESTROYED;
    } else if(health == 1) {
      return COLOR_CRITICAL;
    } else {
      return COLOR_HEALTHY;
    }
  }
}
