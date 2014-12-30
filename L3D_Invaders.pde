import L3D.*;
import java.util.*;
import ddf.minim.*;

// Space Invaders for the L3D Cube
// by Owen Trueblood
// TODO PVectors for all positions

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
  size(640, 480, P3D);
  frameRate(30);
  
  cube=new L3D(this);
  cube.enableDrawing();  //draw the virtual cube
  cube.enableMulticastStreaming();  //stream the data over UDP to any L3D cubes that are listening on the local network
  cube.enablePoseCube();
  
  minim = new Minim(this);
  
  sfxExplosion = minim.loadFile("explosion.wav");
  sfxWin = minim.loadFile("win.wav");
  
  layoutBases();
  swarm = new AlienSwarm(minim, 3, 2, 3, 30);
  player = new Ship(minim, 4, 4);
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
    
    if(winner) {
      int pos = 7 - min(7, timeSinceEnd);
      
      for(int y=7; y >= pos; y--) {
        for(int z=0; z < 8; z++) {
          for(int x=0; x < 8; x++) {
            cube.setVoxel(new PVector(x, y, z), color(0, 255, 0));
          }
        }
      }
    } else {
      int pos = int(min(7, timeSinceEnd));
      
      for(int y=0; y < pos; y++) {
        for(int z=0; z < 8; z++) {
          for(int x=0; x < 8; x++) {
            cube.setVoxel(new PVector(x, y, z), color(255, 0, 0));
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
}
