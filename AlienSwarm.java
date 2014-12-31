import processing.core.*;
import java.util.*;
import java.awt.Color;
import L3D.*;
import ddf.minim.*;

public class AlienSwarm {
  private List<Pair<Alien, PVector>> aliensAndOffsets;
  private int spacing, margin, sizeDeadzone;
  private final static float SHOOT_LIKELIHOOD = 0.03f;
  
  PVector pos;
  int xDir, yDir, zDir;
  private int w, h, depth;
  private int bound;
  
  private int initialSpeed;
  private int lastMoveTime;
  
  private Minim minim;
  private AudioPlayer music[];
  private int musicIndex = 0;
  
  public AlienSwarm(Minim _minim, int _spacing, int margin, int sizeDeadzone, int speed, int _bound) {
    spacing = _spacing;
    initialSpeed = speed;
    bound = _bound;
    
    minim = _minim;
    
    music = new AudioPlayer[4];
    music[0] = minim.loadFile("music-1.mp3");
    music[1] = minim.loadFile("music-2.mp3");
    music[2] = minim.loadFile("music-3.mp3");
    music[3] = minim.loadFile("music-4.mp3");
    musicIndex = 0;
    
    w = (int)(Math.ceil((bound-margin) / spacing) - 1) * spacing + 1;
    depth = w;
    h = bound - sizeDeadzone;
    
    pos = new PVector(margin, bound - 1, margin);
   
    xDir = 1; yDir = -1; zDir = 1;
    
    aliensAndOffsets = new ArrayList<Pair<Alien, PVector>>();
    layoutAliens();
    
    float shootLikelihood = SHOOT_LIKELIHOOD * 1.0f / aliensAndOffsets.size();
    for(Pair p: aliensAndOffsets) {
      Alien a = (Alien)p.x;
      a.setShootLikelihood(shootLikelihood);
    }
  }
  
  public void layoutAliens() {
    int alienXZSpacing = spacing;
    int alienYSpacing = 2;
    
    int i = 0;
    for(int z=0; z < depth; z += alienXZSpacing) {
      for(int y=0; y > -h; y -= alienYSpacing) {
        for(int x=0; x < w; x += alienXZSpacing) {
          if(aliensAndOffsets.size() - 1 < i) {
            Alien a = new Alien(minim, x + (int)pos.x, y + (int)pos.y, z + (int)pos.z, bound);
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
    int speed = (int)PApplet.map(pos.y, 0, bound-1, 0, initialSpeed);
    
    if(time - lastMoveTime >= speed) {
      boolean xCollide = false;
      boolean zCollide = false;
      
      pos.x += xDir;
      
      if(pos.x + w > bound) {
        pos.x = bound - w;
        xCollide = true;
      }
      
      if(pos.x < 0) {
        pos.x = 0;
        xCollide = true;
      }
      
      if(xCollide) {
        xDir *= -1;
        
        pos.z += zDir;
        
        if(pos.z + depth > bound) {
          pos.z = bound - depth;
          zCollide = true;
        }
        
        if(pos.z < 0) {
          pos.z = 0;
          zCollide = true;
        }
        
        if(zCollide) {
          zDir *= -1;
          
          pos.y += yDir;
        }
      }
      
      moveAliens();
      
      music[musicIndex].rewind();
      music[musicIndex].play();
      musicIndex++;
      musicIndex = musicIndex % music.length;
      
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
    return pos.y < h;
  }
  
  public boolean isAlive() {
    return aliensAndOffsets.size() > 0;
  }
}

class Alien {
  private float shootLikelihood;
  
  private int myColor;
  private Shot myShot;
  
  private PVector pos;
  private boolean alive;
  
  private AudioPlayer sfxShoot;
  private AudioPlayer sfxDie;
  
  private Random rand;
  private int bound;
  
  public Alien(Minim minim, int _x, int _y, int _z, int _bound) {
    this(minim, _x, _y, _z, 30, _bound);
  }
  
  public Alien(Minim minim, int _x, int _y, int _z, int _speed, int _bound) {
    pos = new PVector(_x, _y, _z);
    bound = _bound;
    
    rand = new Random();
    
    sfxShoot = minim.loadFile("alien-shoot.wav");
    sfxDie = minim.loadFile("alien-death.wav");
    
    myColor = (new Color(255, 255, 255)).getRGB();
    alive = true;
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
          sfxDie.rewind();
          sfxDie.play();
        }
      }
    }
    
    if(alive) {
      if(rand.nextFloat() < shootLikelihood && myShot == null) {
        myShot = new AlienShot((int)pos.x, (int)(pos.y-1), (int)pos.z);
        shots.add(myShot);
        sfxShoot.rewind();
        sfxShoot.play();
      }
    }
    
    if(myShot != null && !myShot.isAlive())
      myShot = null;
  }
  
  public void setShootLikelihood(float likelihood) {
    if(likelihood >= 0 && likelihood <= 1.0) {
      shootLikelihood = likelihood;
    } else {
      throw new RuntimeException("Shoot likelihood must be between 0 and 1: " + likelihood);
    }
  }
  
  public void render(L3D cube) {
    cube.setVoxel(pos, myColor);
  }
  
  public boolean isAlive() {
    return alive;
  }
  
  class AlienShot extends Shot {
    public AlienShot(int x, int y, int z) {
      super(x, y, z, -1, bound);
      myColor = (new Color(255, 0, 0)).getRGB();
    }
  }
}
