package com.ede1998.genalg; /**
 * Created by 152863eh on 15.03.2017.
 */

import java.util.ArrayList;
import java.util.Collections;

public class Generation {
    private ArrayList<Creature> creatures;
    private static final int TIME = 15;
    private static final double DEATH_PERCENTAGE = 0.2;
    private static final double RANDOM_DEATH_PROBABILITY = 0.02;

    /**
     * This constructor should be called for all generations except the first.
     * It takes as parameter the previous generation and uses a copy of its creatures to find the best creatures and mutate them. Bad creatures die. 
     * Afterwards it also processes and evaluates this new generation (i.e. itself).
     *
     * @param prevGen The previous generation of creatures.
     */
    public Generation(Generation prevGen) {
        if (prevGen == null) throw new IllegalArgumentException("Null pointer generation.");
        if (prevGen == this) throw new IllegalArgumentException("Previous generation is the current gen.");
        creatures = new ArrayList<>(prevGen.creatures.size());
        prevGen.creatures.forEach( (Creature w) -> creatures.add(w.clone()));
        naturalSelect();
        mutate();
        doGeneration();
    }

    /**
     *  This constructor should be called for the first generation only as it creates all creatures randomly. Then these creatures are processed and evaluated.
     * @param creaturesToMake Specifies the number of creatures the generation has.
     */
    public Generation(int creaturesToMake) {
        createFromScratch(creaturesToMake);
        doGeneration();
    }

    /**
     * This method processes all creatures and afterwards evaluates them sorting the creatures by their respective travel distance.
     */
    private void doGeneration() {
        creatures.forEach((Creature w) -> w.reset());
        live();
        Collections.sort(creatures);
    }
    
    
    public Creature getCreatureAt(int index) {
        return creatures.get(index);
    }

    /**
     * A new generation is randomly generated from void. A true creation ex nihilo.
     * @param walkersToMake Specifies the number of creatures the generation has.
     */
    private void createFromScratch(int walkersToMake) {
        creatures = new ArrayList<Creature>(walkersToMake);
        creatures.forEach((Creature c) -> c = new Creature());
    }

    private void live() {//TODO add threads
       creatures.forEach((w)-> w.move(TIME));
    }

    /**
     *  This method kills a percentage of creatures specified by com.ede1998.genalg.Generation.DEATH_PERCENTAGE.
     *  Mostly, the worst performing creatures are killed but a couple of creatures also die randomly. This behaviour is controlled by the com.ede1998.genalg.Generation.RANDOM_DEATH_PROBABILITY constant.
     */
    private void naturalSelect() {
        int deathCount = 0;
        final int totalDead = (int) Math.round(DEATH_PERCENTAGE * creatures.size());
        //kill some random creatures
        for (int i = 0; i < creatures.size(); i++) {
            if (RandomNumberGenerator.randBool(RANDOM_DEATH_PROBABILITY)){
                deathCount++;
                creatures.remove(i);
                if (deathCount >= totalDead)
                    break;
            }
        }
        //kill worst creatures
        for (int i = creatures.size() - 1; deathCount <= totalDead; i--) {
            deathCount++;
            creatures.remove(i);
        }
    }

    /**
     * For each creature that died from naturalSelect(), a new creature is created. It a clone of another already existing creature but its parameters are slightly altered.
     */
    private void mutate() {
        final int totalDead = (int) Math.round(DEATH_PERCENTAGE * creatures.size());
        Creature c;
        for (int i = 0; i <= totalDead; i++) {
            c = creatures.get(i).clone();
            c.mutate();
            creatures.add(c);
        }
    }
}
