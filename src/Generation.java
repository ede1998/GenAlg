/**
 * Created by 152863eh on 15.03.2017.
 */

import java.util.ArrayList;
import java.util.Collections;

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

    public Generation(int walkersToMake) {
        createFromScratch(walkersToMake);
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

    private void createFromScratch(int walkersToMake) {
        walkers = new ArrayList<Creature>(walkersToMake);
        Muscle[] muscles;
        Node[] nodes;
       ConnectionList connections = new ConnectionList();
        for (int i = 0; i < walkersToMake; i++) {
            nodes = new Node[RandomNumberGenerator.randInt(MAX_NODES)];
            muscles = new Muscle[RandomNumberGenerator.randInt(MAX_MUSCLES)];
            for (int j = 0; j < nodes.length; j++){
                nodes[j] = new Node(RandomNumberGenerator.random(), RandomNumberGenerator.random(0,Node.MAX_POS_X),
                        RandomNumberGenerator.random(0, Node.MAX_POS_Y));
            }
            for (int k = 0; k < muscles.length; k++) {
                int n1 = 0, n2 = 0;
                while (n1 == n2) {
                    n1 = RandomNumberGenerator.randInt(nodes.length);
                    n2 = RandomNumberGenerator.randInt(nodes.length);
                }
                final double length = nodes[n1].getDistance(nodes[n2]);
                muscles[k] = new Muscle(length - RandomNumberGenerator.random(length),
                        length + RandomNumberGenerator.random(Muscle.MAX_MUSCLE_LENGTH - length),
                        RandomNumberGenerator.random(), RandomNumberGenerator.random());
                connections.add(nodes[n1], muscles[k]);
                connections.add(nodes[n2], muscles[k]);
            }
            walkers.add(new Creature(nodes, muscles, connections));
        }
    }

    private void live() {
       walkers.forEach((w)-> w.move(TIME));
    }

    private void naturalSelect() {
        int deathCount = 0;
        final int totalDead = (int) Math.round(DEATH_PERCENTAGE * walkers.size());
        //kill some random creatures
        for (int i = 0; i < walkers.size(); i++) {
            if (RandomNumberGenerator.randBool(RANDOM_DEATH_PROBABILITY)){
                deathCount++;
                walkers.remove(i);
                if (deathCount >= totalDead)
                    break;
            }
        }
        //kill worst creatures
        for (int i = walkers.size() - 1; deathCount <= totalDead; i--) {
            deathCount++;
            walkers.remove(i);
        }
    }

    private void mutate() {
        final int totalDead = (int) Math.round(DEATH_PERCENTAGE * walkers.size());
        Creature c;
        for (int i = 0; i <= totalDead; i++) {
            c = walkers.get(i).clone();
            c.mutate();
            walkers.add(c);
        }
    }
}
