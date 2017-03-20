import com.ede1998.genalg.Creature;

/**
 * Created by 152863eh on 14.03.2017.
 */
public class Main {
    public static void main(String[] args) {
        GUI window = new GUI("Genetic Algorithm");
        window.setSize(1000, 600);
        Creature c = new Creature();
        while (true) {
            window.clear();
            for (int i = 0; i < 10; i++) {
                try {
                Thread.sleep(100);
                }
                catch (InterruptedException a) {}
                window.drawCreature(c);
            }
        //c.move(1);
        }
    }
}
