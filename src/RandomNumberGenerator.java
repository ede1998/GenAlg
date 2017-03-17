/**
 * Created by 152863eh on 15.03.2017.
 */
import java.util.Random;
public class RandomNumberGenerator {
    private static Random rng = new Random();

    public static int randint(int min, int max){
        if (max < min) throw new IllegalArgumentException("Max must be larger than min.");
        return (int) (Math.random() * (max - min + 1) + min);
    }
    public static int randint(int max){
        return randint(0, max);
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

    public static boolean randbool(double probability) {
        return random() < probability;
    }

    public static double randG() {
        return rng.nextGaussian();
    }

    public static double randG(double stddev, double shift) {
        return randG() * stddev + shift;
    }

    public static boolean randbool() {
        return rng.nextBoolean();
    }
}
