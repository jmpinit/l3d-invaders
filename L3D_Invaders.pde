import L3D.*;
import java.util.*;

// Space Invaders for the L3D Cube
// by Owen Trueblood
// TODO Split classes into files
// TODO PVectors for all positions

L3D cube;

Ship player;
List<Base> bases;
AlienSwarm swarm;
List<Shot> shots;

int time = 0;

void setup() {
  size(640, 480, P3D);
  frameRate(30);
  
  cube=new L3D(this);
  cube.enableDrawing();  //draw the virtual cube
  cube.enableMulticastStreaming();  //stream the data over UDP to any L3D cubes that are listening on the local network
  cube.enablePoseCube();
  
  layoutBases();
  swarm = new AlienSwarm(3, 2, 3, 30);
  player = new Ship(4, 4);
  shots = new Vector<Shot>();
}

void layoutBases() {
  int baseWidth = 2;
  int baseHeight = 2;
  
  int baseGridWidth = 3;
  int baseGridHeight = 3;
  int baseGridSpacing = 1;
  
  int originSpacing = baseWidth + baseGridSpacing;
  
  bases = new ArrayList<Base>(baseGridWidth * baseGridHeight);
  
  for(int y=0; y < baseGridHeight; y++) {
    for(int x=0; x < baseGridWidth; x++) {
      bases.add(new Base(x * originSpacing, y * originSpacing, baseWidth, baseHeight));
    }
  }
}

abstract class Shot {
  private PVector pos;
  private int vy;
  private boolean alive;
  
  private final static int SPEED = 1;
  protected int myColor;
  private int lastMoveTime;
  
  public Shot(int x, int y, int z, int _vy) {
    pos = new PVector(x, y, z);
    vy = _vy;
    lastMoveTime = 0;
    alive = true;
  }
  
  public void update(int time) {
    if(time - lastMoveTime > SPEED) {
      pos.y += vy;
      
      if(pos.y < 0 || pos.y > 7)
        alive = false;
      
      lastMoveTime = time;
    }
  }
  
  public void render(L3D cube) {
    cube.setVoxel(pos, myColor);
  }
}

class AlienSwarm {
  private List<Alien> aliens;
  private int spacing, margin, sizeDeadzone;
  
  PVector pos;
  int xDir, yDir, zDir;
  private int w, h, depth;
  
  private int initialSpeed;
  private int lastMoveTime;
  
  public AlienSwarm(int _spacing, int margin, int sizeDeadzone, int speed) {
    spacing = _spacing;
    initialSpeed = speed;
    
    w = (ceil((8-margin) / spacing) - 1) * spacing + 1;
    depth = w;
    h = 7 - sizeDeadzone;
    
    pos = new PVector(margin, 0, margin);
   
    xDir = 1; yDir = 1; zDir = 1;
   
    aliens = new ArrayList<Alien>();
    layoutAliens();
  }
  
  public void layoutAliens() {
    int alienXZSpacing = spacing;
    int alienYSpacing = 2;
    
    int i = 0;
    for(int z=int(pos.z); z < pos.z + depth; z += alienXZSpacing) {
      for(int y=int(pos.y); y < pos.y + h; y += alienYSpacing) {
        for(int x=int(pos.x); x < pos.x + w; x += alienXZSpacing) {
          if(aliens.size() - 1 < i)
            aliens.add(new Alien(x, y, z));
          else
            aliens.get(i).setPosition(x, y, z);
            
          i++;
        }
      }
    }
  }
  
  public void render(L3D cube) {
    for(Alien a: aliens)
      a.render(cube);
  }
  
  public void update(int time) {
    int speed = int(initialSpeed - map(pos.y, 0, 7, 0, initialSpeed));
    
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
      
      layoutAliens();
      
      lastMoveTime = time;
    }
  }
}

class Alien {
  private int myColor;
  
  PVector pos;
  boolean alive;
  
  public Alien(int _x, int _y, int _z) {
    this(_x, _y, _z, 30);
  }
  
  public Alien(int _x, int _y, int _z, int _speed) {
    pos = new PVector(_x, _y, _z);
    alive = true;
    
    myColor = color(255);
  }
  
  public void setPosition(int x, int y, int z) {
    pos.x = x;
    pos.y = y;
    pos.z = z;
  }
  
  public void render(L3D cube) {
    if(alive) cube.setVoxel(pos, myColor);
  }
  
  class AlienShot extends Shot {
    public AlienShot(int x, int y, int z) {
      super(x, y, z, 1);
      myColor = color(255, 255, 0);
    }
  }
}

class Ship {
  private final int COLOR = color(0, 0, 255);
  
  private int x, z;
  private final int y = 7;
  private boolean shoot;
  
  public Ship(int _x, int _z) {
    x = _x;
    z = _z;
    
    shoot = false;
  }
  
  public void update(int time) {
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
      myColor = color(255, 255, 0);
    }
  }
}

class Base {
  private final int COLOR_DESTROYED = color(0, 0, 0);
  private final int COLOR_CRITICAL = color(255, 0, 0);
  private final int COLOR_HEALTHY = color(0, 255, 0);
  
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

void update() {
  for(Shot s: shots) s.update(time);
  player.update(time);
  swarm.update(time);
  time++;
}

void render() {
  background(0);
  cube.background(0);
  
  for(Base b: bases) b.render(cube);
  swarm.render(cube);
  player.render(cube);
  for(Shot s: shots) s.render(cube);
  
  // bury the dead
  Iterator<Shot> shotItr = shots.iterator();
  while(shotItr.hasNext()) {
    Shot s = shotItr.next();
    if(!s.alive)
      shotItr.remove();
  }
}

void draw() {
  update();
  render();
}

void keyPressed() {
  if(key == CODED) {
    switch(keyCode) {
      case UP:
        player.moveBack();
        break;
      case LEFT:
        player.moveLeft();
        break;
      case DOWN:
        player.moveForward();
        break;
      case RIGHT:
        player.moveRight();
        break;
    }
  } else {
    switch(key) {
      case 'w':
        player.moveBack();
        break;
      case 'a':
        player.moveLeft();
        break;
      case 's':
        player.moveForward();
        break;
      case 'z':
        player.moveRight();
        break;
      case ' ':
        player.shoot();
        break;
    }
  }
}
