import javax.swing.*;

/**
 * Created by 152863eh on 14.03.2017.
 */
public class Main {
    public static void main(String[] args) {
        GUI window = new GUI("Genetic Algorithm");
        window.setSize(1000, 600);
        window.setVisible(true);
        while (true) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException a) {};
            window.Draw();
        }
    }
}
