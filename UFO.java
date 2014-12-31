import processing.core.*;
import L3D.*;
import java.util.*;
import ddf.minim.*;
import java.awt.Color;

public class UFO {
  private static int deaths;
  public static final boolean HORIZONTAL = false;
  public static final boolean VERTICAL = true;
  
  private final int speed = 10;
  private final int COLOR = (new Color(255, 20, 0)).getRGB();
  
  private PVector pos;
  private boolean dir;
  private boolean alive;
  
  private int deathAnimTimer;
  
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
    
    if(alive) {
      for(Shot s: shots) {
        if(s instanceof Ship.ShipShot) {
          for(int z=0; z < 2; z++) {
            for(int x=0; x < 2; x++) {
              if((int)(pos.x+x) == s.getX() && (int)pos.y == s.getY() && (int)(pos.z+z) == s.getZ()) {
                sfxDeath.rewind();
                sfxDeath.play();
                sfxUFO.pause();
                
                deathAnimTimer = 16;
                alive = false;
                s.kill();
                
                UFO.deaths++;
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
        
        lastMoveTime = time;
      }
      
      // die upon leaving the cube
      if(pos.x <= -2 || pos.z <= -2 || pos.x > bound || pos.z > bound) {
        alive = false;
      }
    }
  }
  
  public void render(L3D cube) {
    PVector offset = new PVector();
    
    if(alive) {
      for(int z=0; z < 2; z++) {
        for(int x=0; x < 2; x++) {
          offset.set(pos.x+x, pos.y, pos.z+z);
          cube.setVoxel(offset, COLOR);
        }
      }
    } else { 
      for(int z=0; z < bound; z++) {
        for(int x=0; x < bound; x++) {
          offset.set(x, pos.y, z);
          float d = (float)Math.sqrt(Math.pow(x-pos.x, 2) + Math.pow(z-pos.z, 2));
          float phase = deathAnimTimer / 1.0f;
          float v = (float)((1.0 + Math.sin(d+phase))/2.0);
          float b = 1.0f;
          
          cube.setVoxel(offset, Color.HSBtoRGB(1.0f, v, b));
        }
      }
      
      deathAnimTimer--;
    }
  }
  
  public boolean isAlive() {
    return !(deathAnimTimer == 0 && !alive);
  }
  
  public static int deadCount() {
    return UFO.deaths;
  }
}
