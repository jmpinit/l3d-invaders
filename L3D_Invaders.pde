import L3D.*;
import java.util.*;
import ddf.minim.*;

// Space Invaders for the L3D Cube
// by Owen Trueblood

L3D cube;

Minim minim;

AudioPlayer sfxExplosion, sfxWin;

Ship player;
List<Base> bases;
AlienSwarm swarm;
List<Shot> shots;

boolean playing = true;
boolean winner;

float animSpeed = 0.3; // length of win/lose animation

int time = 0;
int endTime;

void setup() {
  size(displayWidth, displayHeight, P3D);
  frameRate(30);
  
  cube = new L3D(this);
  cube.enableDrawing();  //draw the virtual cube
  cube.enableMulticastStreaming();  //stream the data over UDP to any L3D cubes that are listening on the local network
  cube.enablePoseCube();
  
  minim = new Minim(this);
  
  sfxExplosion = minim.loadFile("explosion.wav");
  sfxWin = minim.loadFile("win.wav");
  
  layoutBases();
  swarm = new AlienSwarm(minim, 3, 2, max(3, cube.side/2), 30, cube.side);
  player = new Ship(minim, 4, 4, cube.side);
  shots = new Vector<Shot>();
}

void layoutBases() {
  int baseWidth = 2;
  int baseDepth = 2;
  
  int margin = 1;
  int gridSpacing = 2;
  
  int originSpacing = baseWidth + gridSpacing;
  
  bases = new ArrayList<Base>();
  
  for(int z=0; z < cube.side + baseDepth; z += originSpacing) {
    for(int x=0; x < cube.side + baseWidth; x += originSpacing) {
      bases.add(new Base(margin + x, margin + z, baseWidth, baseDepth));
    }
  }
}

void update() {
  if(playing) {
    for(Shot s: shots) s.update(time);
    player.update(time, shots);
    swarm.update(time, shots);
    for(Base b: bases) b.update(time, shots);
    
    // bury the dead
    Iterator<Shot> shotItr = shots.iterator();
    while(shotItr.hasNext()) {
      Shot s = shotItr.next();
      if(!s.isAlive())
        shotItr.remove();
    }
    
    // gameover?
    if(!player.isAlive()) {
      lose();
    } else if(swarm.reachedGoal()) {
      println("aliens reached their goal");
      lose();
    } else if(!swarm.isAlive()) {
      win();
    }
  }
  
  time++;
}

void render() {
  background(0);
  cube.background(0);
  
  if(playing) {
    for(Base b: bases) b.render(cube);
    swarm.render(cube);
    player.render(cube);
    for(Shot s: shots) s.render(cube);
  } else {
    int timeSinceEnd = int(animSpeed * (time - endTime));
    
    if(!winner) {
      int pos = (cube.side-1) - min(cube.side-1, timeSinceEnd);
      
      for(int y=cube.side-1; y >= pos; y--) {
        for(int z=0; z < cube.side; z++) {
          for(int x=0; x < cube.side; x++) {
            cube.setVoxel(new PVector(x, y, z), color(255, 0, 0));
          }
        }
      }
    } else {
      int h = int(min(cube.side, timeSinceEnd));
      
      for(int y=0; y < h; y++) {
        for(int z=0; z < cube.side; z++) {
          for(int x=0; x < cube.side; x++) {
            cube.setVoxel(new PVector(x, y, z), color(0, 255, 0));
          }
        }
      }
    }
  }
}

void win() {
  playing = false;
  winner = true;
  endTime = time;
  
  sfxWin.rewind();
  sfxWin.play();
}

void lose() {
  playing = false;
  winner = false;
  endTime = time;
  
  sfxExplosion.rewind();
  sfxExplosion.play();
}

void draw() {
  update();
  render();
}

void keyPressed() {
  if(playing) {
    if(key == CODED) {
      switch(keyCode) {
        case UP:
          player.moveForward();
          break;
        case LEFT:
          player.moveLeft();
          break;
        case DOWN:
          player.moveBack();
          break;
        case RIGHT:
          player.moveRight();
          break;
      }
    } else {
      switch(key) {
        case 'w':
          player.moveForward();
          break;
        case 'a':
          player.moveLeft();
          break;
        case 's':
          player.moveBack();
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
}
