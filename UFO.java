import processing.core.*;
import L3D.*;
import java.util.*;
import ddf.minim.*;
import java.awt.Color;

public class UFO {
  public static final boolean HORIZONTAL = false;
  public static final boolean VERTICAL = true;
  
  private final int speed = 10;
  private final int COLOR = (new Color(255, 20, 0)).getRGB();
  
  private PVector pos;
  private boolean dir;
  private boolean alive;
  
  private boolean announced;
  
  private int bound;
  private int lastMoveTime;
  
  private AudioPlayer sfxUFO, sfxDeath;
  
  public UFO(Minim minim, int x, int z, boolean _dir, int _bound) {
    dir = _dir;
    bound = _bound;
    pos = new PVector(x, bound-1, z);
    
    alive = true;
    announced = false;
    
    sfxUFO = minim.loadFile("ufo-long.wav");
    sfxDeath = minim.loadFile("ufo-death.wav");
  }
  
  public void update(int time, List<Shot> shots) {
    if(!announced) {
      sfxUFO.rewind();
      sfxUFO.play();
      announced = true;
    }
    
    for(Shot s: shots) {
      if(s instanceof Ship.ShipShot) {
        for(int z=0; z < 2; z++) {
          for(int x=0; x < 2; x++) {
            if((int)(pos.x+x) == s.getX() && (int)pos.y == s.getY() && (int)(pos.z+z) == s.getZ()) {
              sfxDeath.rewind();
              sfxDeath.play();
              sfxUFO.pause();
              alive = false;
              s.kill();
            }
          }
        }
      }
    }
    
    if(time - lastMoveTime >= speed) {
      if(dir == HORIZONTAL) {
        pos.x += 1;
      } else {
        pos.z += 1;
      }
      
      // die upon leaving the cube
      if(pos.x <= -2 || pos.y <= -2 || pos.x > bound || pos.y > bound) {
        alive = false;
      }
      
      lastMoveTime = time;
    }
  }
  
  public void render(L3D cube) {
    PVector offset = new PVector();
    
    for(int z=0; z < 2; z++) {
      for(int x=0; x < 2; x++) {
        offset.set(pos.x+x, pos.y, pos.z+z);
        cube.setVoxel(offset, COLOR);
      }
    }
  }
  
  public boolean isAlive() {
    return alive;
  }
}
