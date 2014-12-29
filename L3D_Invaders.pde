import L3D.*;

L3D cube;
//float offset=0;
//float fade=0.8;
//float m=0;
//float minc=1;

void setup() {
  size(displayWidth, displayHeight, P3D);
  cube=new L3D(this);
  cube.enableDrawing();  //draw the virtual cube
  cube.enableMulticastStreaming();  //stream the data over UDP to any L3D cubes that are listening on the local network
}

void draw() {
  background(0);
  cube.background(0);
  
  translate(width/2, height/2);  //move to the cube.center of the display
  rotateX(PI/8);  //rotate to a nice angle for vieweing the cube
  rotateY(-PI/8);
}

