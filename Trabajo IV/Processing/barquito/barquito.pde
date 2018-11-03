void setup(){
  size(500,500);
  background(255);
  foto();
  barco();
  texto(180); /*El '180' es una demostración. El método se encarga exclusivamente de
            imprimir en pantalla los grados.*/ 
}

void foto(){
  PImage foto;
  foto = loadImage("playa.jpg");
  image(foto, 0, 0);
}

void barco(){
  //Base
  line(250, 270, 100, 240);
  line(250, 270, 400, 240);
  //Laterales
  line(100, 240, 100, 180);
  line(400, 240, 400, 180);
  //Coraza
  line(100, 180, 250, 150);
  line(400, 180, 250, 150);
  //Central
  line(250, 270, 250, 150);
}

void texto(int x){
  textSize(15);
  text("Escora: ", 300, 100);
  text(x, 360, 100); 
  text("°", 390, 100);
}

void draw(){
  line(0, 250, 500, 250);
}