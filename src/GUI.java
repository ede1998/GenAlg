import javax.swing.*;
import java.awt.*;

/**
 * Created by 152863eh on 15.03.2017.
 */
public class GUI extends JFrame {

    public GUI(String s) {
        super(s);
    }
    public void Draw() {
        getGraphics().setColor(Color.black);
        getGraphics().drawRect(0,0,100,100);
        getGraphics().fillRect(100,100,200,200);
        getGraphics().fillOval();
    }
}
