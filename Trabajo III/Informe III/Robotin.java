package Batalla3;

//Importar librerías

import robocode.*;
import java.awt.*;
 
/**
 * SuperTracker - a Super Sample Robot by CrazyBassoonist based on the robot Tracker by Mathew Nelson and maintained by Flemming N. Larsen
 * Tomado desde http://robowiki.net/wiki/SuperTracker
 * 
 * 
 * Moebius - a robot by Michael Dorgan
 * Tomado desde http://old.robowiki.net/robowiki?Moebius/
 **/

//Crear Clase Robotin

public class Robotin extends AdvancedRobot{
	
	//Variables 
	
		//Constantes

		static final int BUSC_PROF = 30; // Profundidad de búsqueda
		static final int RANG_MOV = 350; // Rango de movimiento, modificable según el tipo de enemigo al que nos enfrentamos
		static final int VEL_BAL = 11; // Velocidad de la bala
		static final int RANG_MAX = 800; // Rango al que consideramos al enemigo, no es el máximo de lo que el radar puede, ya que sería mas dificil apuntarle
		static final int BUSC_BUFFER = BUSC_PROF + (RANG_MAX/VEL_BAL); // Cantidad de espacio que dejaremos para analizar
												
		// Globales

		static double arcLength[] = new double[100000];
		static StringBuffer patternMatcher = new StringBuffer("\0\3\6\1\4\7\2\5\b" + (char)(-1) + (char)(-4) + (char)(-7) + (char)(-2) + (char)(-5) + (char)(-8) + (char)(-3) + (char)(-6) + "This space filler for end buffer." + "The numbers up top assure a 1 length match every time.  This string must be " + "longer than BUSC_BUFFER. - Mike Dorgan"); //Establece los patrones
		int moveDirection = 1; //Se establece en que dirección moverse, 

//Inicializamos el robot

	public void run(){
		setAdjustRadarForRobotTurn(true); //Activamos el movimiento de radar independiente
		setColors(Color.BLUE, Color.BLUE, Color.BLUE, Color.RED, Color.RED); //Seteamos el color
		setAdjustGunForRobotTurn(true); //Activamos el movimiento de radar independiente
		turnRadarRightRadians(Double.POSITIVE_INFINITY); //Mantiene el radar girando hasta el infinito
	}

//Creamos Eventos

	//Evento onScannedRobot

	public void onScannedRobot(ScannedRobotEvent e){
		if(getOthers() == 1){ //En caso de ser 1v1 activamos un metodo en el cual se ingresa información de posición sobre el tanque enemigo
			doRobotin(0, patternMatcher.length(), e, BUSC_PROF, e.getBearingRadians() + getHeadingRadians()); //Se ingresas los datos requeridos al metodo
		}
		else{ //para el combate en general analizamos el objetivo como sigue
			double absBearing = e.getBearingRadians() + getHeadingRadians(); //Conducta del enemigo (para donde se dirige)
			double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); //Velocidad lateral a nosotros del enemigo
			double gunTurnAmt; //Cuanto giraremos nuestra torreta
			setTurnRadarLeftRadians(getRadarTurnRemainingRadians()); //Bloquea el radar en la dirección del objetivo
			if(Math.random()>0.9){
				setMaxVelocity((12 * Math.random()) + 12); //Aleatoriamente cambia nuestra velocidad
			}
			if(e.getDistance() > 150) { //Si la distancia es mayor que 150...
				gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + (latVel/22)); //Movemos nuestra torreta un poco mas adelante de la posición enemiga
				setTurnGunRightRadians(gunTurnAmt); //La giramos según lo indicado
				setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity())); //Conduce hacia la ubicación predecida del enemigo
				setAhead((e.getDistance() - 140) * moveDirection); //Avanza
				setFire(3); //y dispara
			}
			else{ //Si nos acercamos lo suficiente...
				gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/15); //Movemos nuestra torreta un poco mas que en el caso anterior
				setTurnGunRightRadians(gunTurnAmt); //La giramos según lo indicado
				setTurnLeft(-90 - e.getBearing()); //Giramos perpendicularmente al enemigo
				setAhead((e.getDistance() - 140) * moveDirection); //Y avanzamos
				setFire(3); //Luego disparamos
			}
		}
	}

	//Evento onHitWall

	public void onHitWall(HitWallEvent e){
		moveDirection =- moveDirection; //Se mueve en la otra dirección al golpear una muralla
	}

	//Evento onWin

	public void onWin(WinEvent e){ //Hace una danza nórdica para agradecer a Odin por la victoria obtenida
		setAllColors(Color.RED);
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

//Creamos Metodos

	//Metodo doRobotin

	public void doRobotin(int matchIndex, int historyIndex, ScannedRobotEvent e, int searchDepth, double targetBearing){
		double arcMovement = e.getVelocity() * Math.sin(e.getHeadingRadians() - targetBearing); //Creamos la variable arcMovement aqui para ahorrar un byte con la asignacion de targeteo
		setAhead(Math.cos(historyIndex >> 4) * RANG_MOV * Math.random()); //Se mueve en un patrón oscilador simple, con un poco de aleatoriedad en su movimiento		
		setTurnRightRadians(e.getBearingRadians() + Math.PI/2); //Intenta quedarse a la misma distancia del objetivo
		setFire(getEnergy() - 1); //Seteamos el poder de la bala según la cantidad de energia que tengamos, descontando 1 para no quedar en 0 de energía al disparar
		arcLength[historyIndex+1] = arcLength[historyIndex] + arcMovement; // Longitud de arco que los enemigos trazan respecto a nuestra ubicación. (ArcLength S = Angulo (Radianes) * Radio del Circulo)
		patternMatcher.append((char)(arcMovement)); //Agregamos la variable ArcMovement al encontrador de patrones, para buscar en el buffer. 
		do{ // Ajustamos el buffer del patron de coincidencias
			matchIndex = patternMatcher.lastIndexOf(patternMatcher.substring(historyIndex - --searchDepth), historyIndex - BUSC_BUFFER);
		}while (matchIndex < 0); 	
		matchIndex += searchDepth; //Actualizamos el index para el final de la búsqueda
		setTurnGunRightRadians(Math.sin((arcLength[matchIndex+((int)(e.getDistance()/VEL_BAL))] - (arcLength[matchIndex]))/(e.getDistance()) + targetBearing - getGunHeadingRadians())); //Movemos la torreta preparandola para el siguiente ataque
		setTurnRadarLeftRadians(getRadarTurnRemaining()); //Bloquea el radar para detectarlo de inmediato cuando salgamos del metodo
	}

}