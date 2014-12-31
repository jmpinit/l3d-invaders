import processing.core.*;
import java.util.*;
import L3D.*;
import java.awt.Color;

public class Base {
  private final int COLOR_DESTROYED = (new Color(0, 0, 0)).getRGB();
  private final int COLOR_CRITICAL = (new Color(255, 0, 0)).getRGB();
  private final int COLOR_HEALTHY = (new Color(0, 255, 0)).getRGB();
  
  private final int distanceFromBottom = 2;
  
  private int[][] health;
  private PVector pos;
  private int width, depth;
  
  public Base(int _x, int _y) {
    this(_x, _y, 2, 2);
  }
  
  public Base(int x, int z, int _width, int _depth) {
    pos = new PVector(x, distanceFromBottom, z);
    width = _width;
    depth = _depth;
    
    health = new int[width][depth];
    
    for(int offZ=0; offZ < depth; offZ++)
      for(int offX=0; offX < width; offX++)
        health[offX][offZ] = 2;
  }
  
  public void render(L3D cube) {
    for(int offZ=0; offZ < depth; offZ++) {
      for(int offX=0; offX < width; offX++) {
        int healthColor = healthToColor(health[offX][offZ]);
        
        int drawX = (int)(pos.x + offX);
        int drawY = (int)(pos.y);
        int drawZ = (int)(pos.z + offZ);
        
        cube.setVoxel(new PVector(drawX, drawY, drawZ), healthColor);
      }
    }
  }
  
  public void update(int time, List<Shot> shots) {
    for(Shot s: shots) {
      for(int z=0; z < depth; z++) {
        for(int x=0; x < width; x++) {
          if((int)(pos.x + x) == s.getX()
           && (int)(pos.y) == s.getY()
           && (int)(pos.z + z) == s.getZ()) {
            if(health[x][z] > 0) {
              health[x][z]--;
              s.kill();
            }
          }
        }
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
