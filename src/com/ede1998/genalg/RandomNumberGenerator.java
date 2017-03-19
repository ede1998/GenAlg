package com.ede1998.genalg; /**
 * Created by 152863eh on 15.03.2017.
 */
import java.util.Random;
public class RandomNumberGenerator {
    private static Random rng = new Random();

    public static int randInt(int min, int max){
        if (max < min) throw new IllegalArgumentException("Max must be larger than min.");
        return (int) (Math.random() * (max - min + 1) + min);
    }

    /**
     * Pseudo-randomly generates an integer between 0 and max (including both) using Math.random.
     * @param max maximum value
     * @return pseudo-random number
     */
    public static int randInt(int max){
        return randInt(0, max);
    }
    public static double random(){
        return Math.random();
    }
    public static double random(double max) {
        return random(0, max);
    }
    public static double random(double min, double max) {
        if (max < min) throw new IllegalArgumentException("Max must be larger than min.");
        return Math.random() * (max - min) + min;
    }

    public static boolean randBool(double probability) {
        return random() < probability;
    }

    public static double randG() {
        return rng.nextGaussian();
    }

    public static double randG(double stdDev, double shift) {
        return randG() * stdDev + shift;
    }

    public static boolean randBool() {
        return rng.nextBoolean();
    }
}
