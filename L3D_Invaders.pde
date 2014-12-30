import L3D.*;
import java.util.*;

// Space Invaders for the L3D Cube
// by Owen Trueblood
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

void update() {
  for(Shot s: shots) s.update(time);
  player.update(time, shots);
  swarm.update(time, shots);
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
    if(!s.isAlive())
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
