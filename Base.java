import processing.core.*;
import L3D.*;
import java.awt.Color;

public class Base {
  private final int COLOR_DESTROYED = (new Color(0, 0, 0)).getRGB();
  private final int COLOR_CRITICAL = (new Color(255, 0, 0)).getRGB();
  private final int COLOR_HEALTHY = (new Color(0, 255, 0)).getRGB();
  
  int[][] health;
  int x, z;
  final int y = 7-2;
  int w, h;
  
  public Base(int _x, int _y) {
    this(_x, _y, 2, 2);
  }
  
  public Base(int _x, int _z, int _width, int _height) {
    x = _x;
    z = _z;
    w = _width;
    h = _height;
    
    health = new int[w][h];
    
    for(int z=0; z < h; z++)
      for(int x=0; x < w; x++)
        health[x][z] = 2;
  }
  
  public void render(L3D cube) {
    for(int offZ=0; offZ < h; offZ++) {
      for(int offX=0; offX < w; offX++) {
        int healthColor = healthToColor(health[offX][offZ]);
        
        int drawX = x + offX;
        int drawY = y;
        int drawZ = z + offZ;
        
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
