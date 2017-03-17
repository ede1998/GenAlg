/**
 * Created by 152863eh on 15.03.2017.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Generation {
    private ArrayList<Creature> walkers;
    private static final int TIME = 15;
    private static final int MAX_NODES = 10;
    private static final int MAX_MUSCLES = 10;
    private static final double DEATH_PERCENTAGE = 0.2;
    private static final double RANDOM_DEATH_PROBABILITY = 0.02;

    public Generation(Generation prevGen) {
        if (prevGen == null) throw new IllegalArgumentException("Null pointer generation.");
        if (prevGen == this) throw new IllegalArgumentException("Previous generation is the current gen.");
        //TODO copy and keep prev gen
        walkers = prevGen.walkers;
        prevGen.walkers = null;
        naturalSelect();
        mutate();
        doGeneration();
    }

    public Generation(int walkerstomake) {
        createFromScratch(walkerstomake);
        doGeneration();
    }

    private void doGeneration() {
        walkers.forEach((w)->w.reset());
        live();
        Collections.sort(walkers);
    }

    public Creature getWalkerAt(int index) {
        return walkers.get(index);
    }

    private void createFromScratch(int walkerstomake) {
        walkers = new ArrayList<Creature>(walkerstomake);
        Muscle[] muscles;
        Node[] nodes;
       ConnectionList connections = new ConnectionList();
        for (int i = 0; i < walkerstomake; i++) {
            nodes = new Node[RandomNumberGenerator.randint(MAX_NODES)];
            muscles = new Muscle[RandomNumberGenerator.randint(MAX_MUSCLES)];
            for (int j = 0; j < nodes.length; j++){
                nodes[j] = new Node(RandomNumberGenerator.random(), RandomNumberGenerator.random(0,Node.MAX_POS_X),
                        RandomNumberGenerator.random(0, Node.MAX_POS_Y));
            }//TODO connections init
            for (int k = 0; k < muscles.length; k++) {
                int n1 = 0, n2 = 0;
                while (n1 == n2) {
                    n1 = RandomNumberGenerator.randint(nodes.length);
                    n2 = RandomNumberGenerator.randint(nodes.length);
                }
                final double length = nodes[n1].getDistance(nodes[n2]);
                muscles[k] = new Muscle(length - RandomNumberGenerator.random(length),
                        length + RandomNumberGenerator.random(Muscle.MAX_MUSCLE_LENGTH - length),
                        RandomNumberGenerator.random(), RandomNumberGenerator.random(), n1, n2);
            }
            walkers.add(new Creature(nodes, muscles, connections));
        }
    }

    private void live() {
       walkers.forEach((w)-> w.move(TIME));
    }

    private void naturalSelect() {
        int deathcount = 0;
        final int totaldead = (int) Math.round(DEATH_PERCENTAGE * walkers.size());
        //kill some random creatures
        for (int i = 0; i < walkers.size(); i++) {
            if (RandomNumberGenerator.randbool(RANDOM_DEATH_PROBABILITY)){
                deathcount++;
                walkers.remove(i);
                if (deathcount >= totaldead)
                    break;
            }
        }
        //kill worst creatures
        for (int i = walkers.size() - 1; deathcount <= totaldead; i--) {
            deathcount++;
            walkers.remove(i);
        }
    }

    private void mutate() {
        final int totaldead = (int) Math.round(DEATH_PERCENTAGE * walkers.size());
        Creature c;
        for (int i = 0; i <= totaldead; i++) {
            c = walkers.get(i).clone();
            c.mutate();
            walkers.add(c);
        }
    }
}
