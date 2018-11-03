package Batalla2;

//Importar Librerias

import robocode.*;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

//Crear Clase Robotin

public class Robotin extends AdvancedRobot {

//Declarar Variables

	int count = 0; // Lleva la cuenta de cuantas veces hemos visto al objetivo
	double gunTurnAmt; // Cuanto hay que girar el arma cuando estamos buscando un objetivo
	String trackName; // Nombre del robot que actualmente estamos siguiendo
	boolean peek; // No girar si hay un tanque
	double moveAmount; // Cuanto debe moverse el tanque
	static double arcLength[] = new double[100000];
	static StringBuffer patternMatcher = new StringBuffer(("\0\3\6\1\4\7\2\5\b" + (char)(-1) + (char)(-4) + (char)(-7) + (char)(-2) + (char)(-5) + (char)(-8) + (char)(-3) + (char)(-6)));

//Declarar Constantes

	static final int buscde = 30;	// Increasing this slows down game execution - beware!
	static final int movdis = 150;	// Larger helps on no-aim and nanoLauLectrik - smaller on linear-lead bots
	static final int velbal = 11;	// 3 power bullets travel at this speed.
	static final int rangmax = 800;	
	static final int buscbuf = buscde + rangmax / velbal;

//Iniciar Robot

	public void run() {
		// Se setea el color del tanque
		setColors(Color.blue, Color.blue, Color.blue, Color.red, Color.red);
		while(true){
			if(getOthers() >= 4){
				trackName = null; // Se establece para no seguir a nadie en primera instancia
				setAdjustGunForRobotTurn(true); // Mantiene la direcci�n del ca�on aunque giremos
				gunTurnAmt = 10; // inicializa el giro del arma en 10
				// Gira el ca�on (busca al enemigo)
				turnGunRight(gunTurnAmt);
				// Va contando la cantidad de turnos que hemos seguido al objetivo
				count++;
				// Si no hemos visto al objetivo por dos turnos, mira hacia la izquierda
				if (count > 2) {
					gunTurnAmt = -10;
				}
				// Si a�n no vemos al objetivo despu�s de cinco turnos, miramos a la derecha
				if (count > 5) {
					gunTurnAmt = 10;
				}
				// Si a�n no vemos al objetivo despu�s de 10 turnos, buscamos otro objetivo
				if (count > 11) {
					trackName = null;
				}
			}
			else if (getOthers() > 1 && getOthers() < 4){
				//Desactivamos la torreta con movimiento independiente del cuerpo del tanque.
				setAdjustGunForRobotTurn(false);
				// Ajustamos el ca��n para que se oriente correctamente 
				turnGunLeft(getGunHeading()-getHeading());
				//turnGunRight(90);
				//Comienza a moverse alrededor de las paredes
				// Inicializa la cantidad de movimiento al maximo que permita el mapa.
				moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
				peek = false;
				// Gira a la izquierda para mirar una muralla.
				// getHeading() % 90 significa el resto de los grados de la orientacion del tanque por 90
				turnLeft(getHeading() % 90);
				//Avanza
				ahead(moveAmount);
				peek = true;
				// Gira el ca�on 90 grados a la izquierda.
				turnGunRight(90);
				// Mira antes de que se complete el ahead().
				turnRight(90);
				peek = true;
				// Se mueve hacia arriba de la pared
				ahead(moveAmount);
				// Deja de mirar
				peek = false;
				// Gira a la siguiente pared
				turnRight(90);
			}
			else{
				setAllColors(Color.RED);
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
		}
	}


	public void onHitRobot(HitRobotEvent e) {
		if(getOthers() >= 4){	
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
		else{
			
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if(getOthers() >= 4){
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
				} else {
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
		else{
			doMoebius(0, patternMatcher.length(), e, buscde, e.getBearingRadians() + getHeadingRadians());
		}
	}

	public void onWin(WinEvent e) {
		// Movimiento n�rdico ganador para agradecer a Odin
		for (int i = 0; i < 50; i++) {
			fire(1);
			turnRight(30);
			turnLeft(30);
		}
	}

	 private void doMoebius(int matchIndex, int historyIndex, ScannedRobotEvent e, int searchDepth, double targetBearing)
    {
		// Asinga un movimineto
		double arcMovement = e.getVelocity() * Math.sin(e.getHeadingRadians() - targetBearing);														
		//Mueve en un patron aleatorio
		setAhead(Math.cos(historyIndex>>4) * movdis * Math.random());				
		// Ecuacuines para obtener una distancia
		setTurnRightRadians(e.getBearingRadians() + Math.PI/2);
		//Realizar un disparo
		setFire(getEnergy()-1);
		//Realiza un movimiento radial
		arcLength[historyIndex+1] = arcLength[historyIndex] + arcMovement;
		//Añade un arco de movimiento
		patternMatcher.append((char)(arcMovement));
		do 
		{
			matchIndex = patternMatcher.lastIndexOf(
							patternMatcher.substring(historyIndex - --searchDepth),
							historyIndex-buscbuf);
		}
		while (matchIndex < 0); 
		//Realiza busqueda
		matchIndex += searchDepth;
		//Apuntar al Objetico
		setTurnGunRightRadians(Math.sin( (arcLength[matchIndex+((int)(e.getDistance()/velbal))]-arcLength[matchIndex])/e.getDistance() + targetBearing - getGunHeadingRadians()));
		//Bloquear el radar
		setTurnRadarLeftRadians(getRadarTurnRemaining());
	}

	//Parte de código tomado de Tracker y Wall. Además de unas ideas sobre Moebius (Falta Refinar)

}