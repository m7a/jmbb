package ma.tools2.util;

/**
 * @author Linux-Fan, Ma_Sys.ma
 * @version 1.0.2.1
 * @since Tools 2
 */
public class MathUtils {

	// Static
	private MathUtils() {
		super();
	}
	
	/**
	 * Kürzt den Bruch mit Hilfe der {@link #ggt(long, long)} Funktion
	 * und gibt die passendne Zähler und Nenner zurück.
	 * 
	 * @param zaehler Eingabezähler (obere Zahl im Bruch)
	 * @param nenner Eingabenenner (untere Zahl)
	 * @return
	 * 	Array mit Index 1: Zähler; Index 2: Nenner
	 * 	(ret[0] zähler, ret[1] nenner)
	 */
	public static long[] kuerzen(long zaehler, long nenner) {
		long divideBy = ggt(zaehler, nenner)[2];
		long[] ret = { zaehler / divideBy, nenner / divideBy };
		return ret;
	}

	/**
	 * Euklidischer Algorithmus, um den größten gemeinsamen Teiler zu
	 * bestimmen.
	 * 
	 * @param n1i Eingabezahl 1
	 * @param n2i Eingabezahl 2
	 * @return An index 3 (ret[2]) den größten gemeinsamen Teiler
	 */
	public static long[] ggt(long n1i, long n2i) {
		long n1 = n1i, n2 = n2i;
		long[] ret = { 1, 0, 0 };
		long u = 0, v = 1;
		while(n2 != 0) {
			long bruch = n1 / n2;
			long modulo = n1 - bruch * n2; // n1 % n2
			n1 = n2;
			n2 = modulo;
			long diff = ret[0] - bruch * u; 
			ret[0] = u;
			u = diff;
			diff = ret[1] - bruch * v;
			ret[1] = v;
			v = diff;
		}
		ret[2] = n1;
		return ret;
	}
	
}
