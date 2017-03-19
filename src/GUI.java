import com.ede1998.genalg.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by 152863eh on 15.03.2017.
 */
public class GUI extends JFrame {
    private static final int NODE_RADIUS = 25;
    Graphics graphics;

    public GUI(String s) {
        super(s);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        graphics = getGraphics();
    }

    public void clear() {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0,0, getWidth(), getHeight());
    }
    public void drawCreature(Creature creature) {
        for (Muscle m : creature.getMuscles()) {
            ArrayList<Node> nodes = creature.getConnections().getNodes(m);
            int x1 = convertToScreenX(nodes.get(0).getPositionX()), x2 = convertToScreenX(nodes.get(1).getPositionX()),
                y1 = convertToScreenY(nodes.get(0).getPositionY()), y2 = convertToScreenY(nodes.get(1).getPositionY());
            graphics.drawLine(x1,y1,x2,y2);
        }

        for (Node n : creature.getNodes()) {
            graphics.setColor(makeColor(n.getFriction()));
            graphics.fillOval( convertToScreenX(n.getPositionX()) - NODE_RADIUS, convertToScreenY(n.getPositionY()) - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
        }

        graphics.setColor(Color.BLACK);
        graphics.drawOval(convertToScreenX(creature.getPositionX()) - 5, convertToScreenY(creature.getPositionY()) - 5, 10, 10);
    }

    private Color makeColor(double percentage) {
        if ((percentage > 1) || (percentage < 0)) throw new IllegalArgumentException("Color must be between 0 and 1.");
        int c = (int) (percentage * 0xFFFFFF);
        return new Color((c & 0xFF0000) >> 16 , (c & 0x00FF00) >> 8, (c & 0x0000FF));
    }

    private int convertToScreenY(double posY) {
        return getHeight() - (int) (posY * 10);
    }

    private int convertToScreenX(double posX) {
        return (int) (posX * 100);
    }
}
