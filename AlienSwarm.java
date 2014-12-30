import processing.core.*;
import java.util.*;
import java.awt.Color;
import L3D.*;
import ddf.minim.*;

public class AlienSwarm {
  private List<Pair<Alien, PVector>> aliensAndOffsets;
  private int spacing, margin, sizeDeadzone;
  
  PVector pos;
  int xDir, yDir, zDir;
  private int w, h, depth;
  
  private int initialSpeed;
  private int lastMoveTime;
  
  private Minim minim;
  
  public AlienSwarm(Minim _minim, int _spacing, int margin, int sizeDeadzone, int speed) {
    spacing = _spacing;
    initialSpeed = speed;
    
    minim = _minim;
    
    w = (int)(Math.ceil((8-margin) / spacing) - 1) * spacing + 1;
    depth = w;
    h = 7 - sizeDeadzone;
    
    pos = new PVector(margin, 0, margin);
   
    xDir = 1; yDir = 1; zDir = 1;
    
    aliensAndOffsets = new ArrayList<Pair<Alien, PVector>>();
    layoutAliens();
  }
  
  public void layoutAliens() {
    int alienXZSpacing = spacing;
    int alienYSpacing = 2;
    
    int i = 0;
    for(int z=0; z < depth; z += alienXZSpacing) {
      for(int y=0; y < h; y += alienYSpacing) {
        for(int x=0; x < w; x += alienXZSpacing) {
          if(aliensAndOffsets.size() - 1 < i) {
            Alien a = new Alien(minim, x + (int)pos.x, y + (int)pos.y, z + (int)pos.z);
            PVector offset = new PVector(x, y, z);
            aliensAndOffsets.add(new Pair<Alien, PVector>(a, offset));
          } else {
            Pair p = aliensAndOffsets.get(i);
            Alien a = (Alien)p.x;
            PVector offset = (PVector)p.y;
            
            offset.set(x, y, z);
          }
          
          i++;
        }
      }
    }
    
    moveAliens();
  }
  
  public void moveAliens() {
    for(Pair p: aliensAndOffsets) {
      Alien a = (Alien)p.x;
      PVector offset = (PVector)p.y;
      
      a.setPosition((int)(pos.x + offset.x), (int)(pos.y + offset.y), (int)(pos.z + offset.z));
    }
  }
  
  public void render(L3D cube) {
    for(Pair p: aliensAndOffsets) {
      Alien a = (Alien)p.x;
      a.render(cube);
    }
  }
  
  public void update(int time, List<Shot> shots) {
    int speed = (int)(initialSpeed - PApplet.map(pos.y, 0, 7, 0, initialSpeed));
    
    if(time - lastMoveTime >= speed) {
      boolean xCollide = false;
      boolean zCollide = false;
      
      pos.x += xDir;
      
      if(pos.x + w > 8) {
        pos.x = 8 - w;
        xCollide = true;
      }
      
      if(pos.x < 0) {
        pos.x = 0;
        xCollide = true;
      }
      
      if(xCollide) {
        xDir *= -1;
        
        pos.z += zDir;
        
        if(pos.z + depth > 8) {
          pos.z = 8 - depth;
          zCollide = true;
        }
        
        if(pos.z < 0) {
          pos.z = 0;
          zCollide = true;
        }
        
        if(zCollide) {
          zDir *= -1;
          
          pos.y += yDir;
          
          if(pos.y + h > 7) {
            // TODO lose
          }
        }
      }
      
      moveAliens();
      
      lastMoveTime = time;
    }
    
    for(Pair p: aliensAndOffsets) {
      Alien a = (Alien)p.x;
      a.update(time, shots);
    }
    
    // bury the dead
    Iterator<Pair<Alien, PVector>> pairItr = aliensAndOffsets.iterator();
    while(pairItr.hasNext()) {
      Alien a = pairItr.next().x;
      if(!a.isAlive())
        pairItr.remove();
    }
  }
  
  public boolean reachedGoal() {
    return pos.y + h > 7;
  }
  
  public boolean isAlive() {
    return aliensAndOffsets.size() > 0;
  }
}

class Alien {
  private static final float SHOOT_LIKELIHOOD = 0.005f;
  
  private int myColor;
  private Shot myShot;
  
  private PVector pos;
  private boolean alive;
  
  private AudioPlayer sfxShoot;
  
  private Random rand;
  
  public Alien(Minim minim, int _x, int _y, int _z) {
    this(minim, _x, _y, _z, 30);
  }
  
  public Alien(Minim minim, int _x, int _y, int _z, int _speed) {
    pos = new PVector(_x, _y, _z);
    alive = true;
    
    rand = new Random();
    
    sfxShoot = minim.loadFile("alien-shoot.wav");
    
    myColor = (new Color(255, 255, 255)).getRGB();
  }
  
  public void setPosition(int x, int y, int z) {
    pos.x = x;
    pos.y = y;
    pos.z = z;
  }
  
  public void update(int time, List<Shot> shots) {
    for(Shot s: shots) {
      if(s instanceof Ship.ShipShot) {
        if((int)pos.x == s.getX() && (int)pos.y == s.getY() && (int)pos.z == s.getZ()) {
          alive = false;
          s.kill();
        }
      }
    }
    
    if(alive) {
      if(rand.nextFloat() < SHOOT_LIKELIHOOD && myShot == null) {
        myShot = new AlienShot((int)pos.x, (int)(pos.y+1), (int)pos.z);
        shots.add(myShot);
        sfxShoot.rewind();
        sfxShoot.play();
      }
    }
    
    if(myShot != null && !myShot.isAlive())
      myShot = null;
  }
  
  public void render(L3D cube) {
    cube.setVoxel(pos, myColor);
  }
  
  public boolean isAlive() {
    return alive;
  }
  
  class AlienShot extends Shot {
    public AlienShot(int x, int y, int z) {
      super(x, y, z, 1);
      myColor = (new Color(255, 0, 0)).getRGB();
    }
  }
}
