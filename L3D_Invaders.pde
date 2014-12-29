import L3D.*;
import java.util.*;

L3D cube;

Ship player;
List<Base> bases;
List<Alien> aliens;

void setup() {
  size(displayWidth, displayHeight, P3D);
  cube=new L3D(this);
  cube.enableDrawing();  //draw the virtual cube
  cube.enableMulticastStreaming();  //stream the data over UDP to any L3D cubes that are listening on the local network
  cube.enablePoseCube();
  
  layoutBases();
  layoutAliens();
  
  player = new Ship(4, 4);
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

void layoutAliens() {
  aliens = new ArrayList<Alien>();
  
  int alienXZSpacing = 3;
  int alienYSpacing = 2;
  int alienXZMargin = 2;
  int alienYMargin = 0;
  int sizeDeadzone = 3; // empty space between aliens and cube bottom
  
  for(int z=alienXZMargin; z < 7; z += alienXZSpacing) {
    for(int y=alienYMargin; y < 7 - sizeDeadzone; y += alienYSpacing) {
      for(int x=alienXZMargin; x < 7; x += alienXZSpacing) {
        aliens.add(new Alien(x, y, z));
      }
    }
  }
}

class Alien {
  private int myColor;
  
  PVector pos;
  boolean alive;
  
  public Alien(int _x, int _y, int _z) {
    pos = new PVector(_x, _y, _z);
    alive = true;
    
    myColor = color(255);
  }
  
  public void render(L3D cube) {
    if(alive) cube.setVoxel(pos, myColor);
  }
  
  public void act() {
    
  }
}

class Ship {
  private final int COLOR = color(0, 0, 255);
  
  int x, z;
  final int y = 7;
  
  public Ship(int _x, int _z) {
    x = _x;
    z = _z;
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
    println("pow pow");
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

void draw() {
  background(0);
  cube.background(0);
  
  for(Alien a: aliens) a.render(cube);
  for(Base b: bases)   b.render(cube);
  
  player.render(cube);
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
    }
  }
}
