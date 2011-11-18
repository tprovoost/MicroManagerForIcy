package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools;

import icy.plugin.interface_.PluginLibrary;

import java.awt.geom.Point2D;

public abstract class MathTools implements PluginLibrary {

	/**
	 * @param a : first value
	 * @param b : second value
	 * @param allowedDifference : allowed difference between a & b.
	 * @return
	 * Returns a nearly equals comparison.
	 */
	public static boolean nearlyEquals(double a, double b, double allowedDifference) {
		return (Math.abs(a - b) < allowedDifference);
	}
	
	/**
	 * @param a : first value
	 * @param b : second value
	 * @param allowedDifference : allowed percentage difference between a & b.
	 * @return
	 * Returns a nearly equals comparison.
	 */
	public static boolean nearlyEqualsWithPercentage(double a, double b, int allowedPercentageDifference) {
		double difference = b * allowedPercentageDifference/100d;
		return (a >= b - difference && a <= b + difference);
	}
	
	/**
	 * a <= x <= b
	 * @param x : value between
	 * @param a : lower bound
	 * @param b : higher bound
	 */
	public static boolean isBetween(double x, double a, double b) {
		return (a <= x && x <= b);
	}
	
	/**
	 * a < x < b
	 * @param x : value between
	 * @param a : lower bound
	 * @param b : higher bound
	 */
	public static boolean isBetweenStrict(double x, double a, double b) {
		return (a < x && x < b);
	}
	
	/**
	 * Works with nanoprecision
	 * @param timemillis
	 */
	public static void waitFor(final long timemillis) {
		try {
			Thread.sleep(timemillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static Point2D vectorAB(Point2D A, Point2D B) {
		return new Point2D.Double(B.getX()-A.getX(), B.getY()-A.getY());
	}
}
