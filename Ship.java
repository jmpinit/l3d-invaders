import processing.core.*;
import L3D.*;
import java.util.*;
import ddf.minim.*;
import java.awt.Color;

public class Ship {
  private final static int MAX_LIVES = 2;
  
  private PVector pos;
  private boolean shoot;
  private boolean alive;
  private int lives;
  
  private int bound;
  
  private Shot myShot;
  
  private AudioPlayer sfxShoot;
  private AudioPlayer sfxHurt;
  private boolean flash;
  
  public Ship(Minim minim, int x, int z, int _bound) {
    pos = new PVector(x, 0, z);
    
    bound = _bound;
    
    alive = true;
    lives = MAX_LIVES;
    shoot = false;
    
    sfxShoot = minim.loadFile("ship-shoot.wav");
    sfxHurt = minim.loadFile("ship-hurt.wav");
  }
  
  public void update(int time, List<Shot> shots) {
    for(Shot s: shots) {
      if(s instanceof Alien.AlienShot) {
        if((int)pos.x == s.getX() && (int)pos.y == s.getY() && (int)pos.z == s.getZ()) {
          if(lives > 0) {
            lives--;
            flash = true;
            sfxHurt.rewind();
            sfxHurt.play();
          } else {
            alive = false;
          }
          s.kill();
        }
      }
    }
    
    if(shoot && myShot == null) {
      myShot = new ShipShot((int)pos.x, (int)(pos.y+1), (int)pos.z, bound);
      shots.add(myShot);
      sfxShoot.rewind();
      sfxShoot.play();
      shoot = false;
    }
    
    if(myShot != null && !myShot.isAlive())
      myShot = null;
  }
  
  public void render(L3D cube) {
    if(flash) {
      PVector fpos = new PVector();
      int fcolor = (new Color(255, 255, 255)).getRGB();
      
      for(int z=0; z < bound; z++) {
        for(int y=0; y < bound; y++) {
          for(int x=0; x < bound; x++) {
            fpos.set(x, y, z);
            cube.setVoxel(fpos, fcolor);
          }
        }
      }
      
      flash = false;
    } else {
      cube.setVoxel(pos, (new Color(0, 0, (int)(255 * (float)(lives + 1)/(MAX_LIVES + 1)))).getRGB());
    }
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
    if(pos.x < bound-1) pos.x++;
  }
  
  public void moveBack() {
    if(!alive) return;
    if(pos.z < bound-1) pos.z++;
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
    public ShipShot(int x, int y, int z, int bound) {
      super(x, y, z, 1, bound);
      myColor = (new Color(255, 255, 0)).getRGB();
    }
  }
}
