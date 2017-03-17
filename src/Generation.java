/**
 * Created by 152863eh on 15.03.2017.
 */

import java.util.ArrayList;
import java.util.Collections;

public class Generation {
    private ArrayList<Creature> walkers;
    private static final int TIME = 15;
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
        walkers.forEach((Creature c) -> c = new Creature());
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
