import java.util.*;
import processing.serial.*; 
 
Serial myPort;    // The serial port
PFont myFont;     // The display font
String inString;  // Input string from serial port
int lf = 10;      // ASCII linefeed 
 
void setup() { 
  size(500,500);
  printArray(Serial.list()); 
  myPort = new Serial(this, Serial.list()[0], 9600); 
  myPort.bufferUntil(lf);
} 
void foto(){
  PImage foto;
  foto = loadImage("playa.jpg");
  image(foto, 0, 0);
}
void serialEvent(Serial p) { 
  inString = p.readString(); 
} 
void barco(int x){
  //Base
  line(250-x, 270-x, 100-x, 240-x);
  line(250-x, 270-x, 400-x, 240-x);
  //Laterales
  line(100-x, 240-x, 100-x, 180-x);
  line(400-x, 240-x, 400-x, 180-x);
  //Coraza
  line(100-x, 180-x, 250-x, 150-x);
  line(400-x, 180-x, 250-x, 150-x);
  //Central
  line(250-x, 270-x, 250-x, 150-x);
}

void draw() {
  background(255);
  foto();
  translate(250, 250);
  //line(0, 325, 400, 325);
  //text("angulo: " + inString, 10, -30); 
  if (inString !=null) {
    int[] valores=int(split(inString, " "));
    text("angulo: " + -valores[0], -50, -130); 
    textSize(20);
    fill(50);
    pushMatrix();
    //translate(250 ,250);
    rotate(radians(valores[0]));
    barco(250);    
    popMatrix();
  }
}