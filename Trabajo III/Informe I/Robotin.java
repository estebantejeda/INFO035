package Batalla1;

//Importar Librerias

import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.*;
import java.awt.*;

//Crear Clase Robotin

public class Robotin extends AdvancedRobot{

//Declarar Variables

	int count = 0; // Lleva la cuenta de cuantas veces hemos visto al objetivo
	int turnDirection = 1;
	double gunTurnAmt; // Cuanto hay que girar el arma cuando estamos buscando un objetivo
	String trackName; // Nombre del robot que actualmente estamos siguiendo
	boolean peek; // No girar si hay un tanque
	double moveAmount; // Cuanto debe moverse el tanque	

//Iniciar Robot

	public void run(){
		setColors(Color.blue, Color.blue, Color.blue, Color.red, Color.red);
		while(true){
			if(getOthers() >= 4 || getOthers() == 1){ //4 o más robots enemigos
				if (getOthers() == 1){
					setAllColors(Color.red);
				}
				trackName = null; // Se establece para no seguir a nadie en primera instancia
				setAdjustGunForRobotTurn(true); // Mantiene la dirección del cañon aunque giremos
				gunTurnAmt = 10; // inicializa el giro del arma en 10
				turnGunRight(gunTurnAmt); // Gira el cañon (busca al enemigo)
				count++; // Va contando la cantidad de turnos que hemos seguido al objetivo
				if (count > 2) { // Si no hemos visto al objetivo por dos turnos, mira hacia la izquierda
					gunTurnAmt = -10;
				}
				if (count > 5) { // Si aún no vemos al objetivo después de cinco turnos, miramos a la derecha
					gunTurnAmt = 10;
				}
				if (count > 11) { // Si aún no vemos al objetivo después de 10 turnos, buscamos otro objetivo
					trackName = null;
				}
			}
			else if (getOthers() > 1 && getOthers() < 4){ //Entre 2 y 3 robots enemigos
				moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
				peek = false;
				// Gira a la izquierda para mirar una muralla.
				// getHeading() % 90 significa el resto de los grados de la orientacion del tanque por 90
				turnLeft(getHeading() % 90);
				ahead(moveAmount);
				// Gira el ca�on 90 grados a la izquierda.
				peek = true;
				// Ajustamos el ca��n para que se oriente correctamente 
				turnGunRight(90-getGunHeading());
				turnRight(90);
				// Mira antes de que se complete el ahead().
				peek = true;
				// Se mueve hacia arriba de la pared
				ahead(moveAmount);
				// Deja de mirar
				peek = false;
				// Gira a la siguiente pared
				turnGunRight(90);
				turnRight(90);
			}
			/*else{ //1 vs 1
				setAllColors(Color.RED);
			}*/
		}
	}

//Realizar Eventos

	//Evento onHitRobot

	public void onHitRobot(HitRobotEvent e) {
		if(getOthers() >= 4 || getOthers() == 1){	
			// Solo se imprime el mensaje si actualmente no es nuestro objetivo.
			if (trackName != null && !trackName.equals(e.getName())) {
				out.println("Siguiendo a " + e.getName() + " debido a un choque");
			}
			// Mira el objetivo
			trackName = e.getName();
			// Regresa un poco.
			gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
			turnGunRight(gunTurnAmt);
			fire(3);
			back(50);
		}
		else if (getOthers() > 1 && getOthers() < 4){
			// Si est� delante de nosotros, nos devolvemos un poco.
			if (e.getBearing() > -90 && e.getBearing() < 90) {
				back(100);
			} // Si est� detras de nosotros, avanzamos un poco.
			else {
				ahead(100);
			}
		}
		/*else{
			if (e.getBearing() >=0){
				turnDirection = 1;
			}
			else{
				turnDirection = -1;
			}
			turnRight(e.getBearing());
			if(e.getEnergy() > 16){
				fire(3);
			}
			else if(e.getEnergy() > 10){
				fire (2);
			}
			else if( e.getEnergy() > 4){
				fire(1);
			}
		}*/
	}

	//Evento onScannerRobot

	public void onScannedRobot(ScannedRobotEvent e) {
		if(getOthers() >= 4 || getOthers() == 1){
			// Si tenemos un objetivo, y no es el que ya tenemos marcado, terminamos la excepcion
			// asi podemos volver a dejar libre el onScannedRobot
			if (trackName != null && !e.getName().equals(trackName)) {
				return;
			}
			// Si no tenemos marcados un objetivo, marcamos al detectado
			if (trackName == null) {
				trackName = e.getName();
				out.println("Tracking " + trackName);
			}
			// Como este es un nuevo objetivo, reseteamos el contador
			count = 0;
			// Si el objetivo esta lejos, nos movemos hacia el.
			if (e.getDistance() > 150) {
				gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
	
				turnGunRight(gunTurnAmt); 
				turnRight(e.getBearing());
				ahead(e.getDistance() - 140);
				return;
			}
			// Nuestro objetivo esta demasiado cerca
			gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
			turnGunRight(gunTurnAmt);
			fire(3);
			// Regresamos si el objetivo esta muy cerca
			if (e.getDistance() < 100) {
				if (e.getBearing() > -90 && e.getBearing() <= 90) {
					back(40);
				} 
				else {
					ahead(40);
				}
			}
			scan();
		}
		else if (getOthers() > 1 && getOthers() < 4){
			// disparamos a todo lo que detectemos con el radar
			fire(2);
			if (peek) {
				scan();
			}
		}
		/*else{
			if (e.getBearing() >= 0){
				turnDirection = 1;
			}
			else{
				turnDirection = -1;
			}
			turnRight(e.getBearing());
			ahead(e.getDistance() + 5);
			scan();
		}*/
	}

	//Evento onWin

		public void onWin(WinEvent e) {
		// Movimiento n�rdico ganador para agradecer a Odin
		for (int i = 0; i < 50; i++) {
			fire(1);
			turnRight(30);
			turnLeft(30);
		}
	}


}