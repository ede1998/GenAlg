package com.ede1998.genalg;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by 152863eh on 14.03.2017.
 */

public class Creature implements Comparable<Creature> {
    private static final int RESOLUTION = 15000;
    private static final double RANDOM_MUTATION_PROBABILITY = 0.03;
    private static final double MUTATION_DIVERGENCE = 0.2;
    private static final int MAX_NODES = 10;
    private static final int MAX_MUSCLES = 10;
    private Node[] nodes;
    private Muscle[] muscles;
    private ConnectionList connections;


    private Creature(Node[] nodes, Muscle[] muscles, ConnectionList connections) {
        this.nodes = nodes;
        this.muscles = muscles;
        this.connections = connections;
    }

    public Creature() { //TODO creature must be completely connected
        connections = new ConnectionList();
        nodes = new Node[RandomNumberGenerator.randInt(MAX_NODES)];
        muscles = new Muscle[RandomNumberGenerator.randInt(MAX_MUSCLES)];
        for (int j = 0; j < nodes.length; j++) {
            nodes[j] = new Node();
        }
        for (int k = 0; k < muscles.length; k++) {
            int n1 = 0, n2 = 0;
            while (n1 == n2) {
                n1 = RandomNumberGenerator.randInt(nodes.length - 1);
                n2 = RandomNumberGenerator.randInt(nodes.length - 1);
            }
            muscles[k] = new Muscle(nodes[n1].getDistance(nodes[n2]));
            connections.add(nodes[n1], muscles[k]);
            connections.add(nodes[n2], muscles[k]);
        }
    }

    public double getPositionX() {
        double pos = 0;
        for (Node node : nodes) pos += node.getPositionX();
        return pos / nodes.length;
    }

    public double getPositionY() {
        double pos = 0;
        for (Node node : nodes) pos += node.getPositionY();
        return pos / nodes.length;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Muscle[] getMuscles() {
        return muscles;
    }

    public ConnectionList getConnections() {
        return connections;
    }

    /**
     * This method calls tryToMove() time times, so it simulates a time span in which the creature can try to walk.
     * @param time number of repetitions of creature movement
     */
    public void move(int time) {
        if (time <= 0) throw new IllegalArgumentException("Time to try must be greater than 0.");
        for (int i = 0; i < time; i++)
            tryToMove();
    }

    public void reset() {
        for (Node n : nodes)
            n.reset();
    }

    private void tryToMove() {
        //TODO hope that works
        double n1px = 0, n1py = 0, n2py = 0, n2px = 0;
        for (int clock = 0; clock < RESOLUTION; clock++) {
            for (Muscle muscle : muscles) {
                ArrayList<Node> con = connections.getNodes(muscle);
                double prevLen = muscle.getLength();
                muscle.tenseOrRelease(clock / RESOLUTION);
                double deltaLen = muscle.getLength() - prevLen;
                double angle = Math.asin(Math.abs(con.get(0).getPositionY() - con.get(1).getPositionY()) / prevLen);
                if (con.get(0).getPositionY() == 0) {
                    if (con.get(1).getPositionY() == 0) { //both on ground
                        //depends on friction, only x, angle = 0
                        n2px = con.get(1).getFriction() / (con.get(1).getFriction() + con.get(0).getFriction())
                                * deltaLen;
                        n1px = con.get(0).getFriction() / (con.get(1).getFriction() + con.get(0).getFriction())
                                * deltaLen;

                    } else { //node 1 on ground, node 2 in air
                        //node2 moves in x, node1 not; both in y
                        n2px = Math.sin(angle) * deltaLen;
                        n2py = Math.cos(angle) * deltaLen / 2;
                        n1py = n2py;
                    }
                } else {
                    if (con.get(1).getPositionY() == 0) { //node1 in air, node2 on ground
                        //node1 moves in x, node2 not; both in y
                        n1px = Math.sin(angle) * deltaLen;
                        n2py = Math.cos(angle) * deltaLen / 2;
                        n1py = n2py;
                    } else { //both in air
                        //both move in x and y
                        n2px = Math.sin(angle) * deltaLen / 2;
                        n1px = n2px;
                        n2py = Math.cos(angle) * deltaLen / 2;
                        n1py = n2py;
                    }
                }
                con.get(0).movePosition(n1px, n1py);
                con.get(1).movePosition(n2px, n2py);
                //TODO prevent form slipping in ground y < 0!
            }
            for (Node n : nodes) {
                n.forceMovement();
            }
        }
    }

    @Override
    public Creature clone() {
        Node[] n = new Node[nodes.length];
        Muscle[] m = new Muscle[muscles.length];
        ConnectionList c = new ConnectionList();

        c.addAll(connections);
        for (int i = 0; i < nodes.length; i++)
            n[i] = nodes[i].clone();
        for (int i = 0; i < muscles.length; i++)
            m[i] = muscles[i].clone();
        return new Creature(n, m, c);
    }

    @Override //this<other => -1
    public int compareTo(Creature otherCreature) {
        double x1 = this.getPositionX();
        double x2 = otherCreature.getPositionX();
        if (x1 > x2)
            return 1;
        if (x1 < x2)
            return -1;
        return 0;
    }

    public void mutate() {
        for (Node n : nodes)
            n.mutate(MUTATION_DIVERGENCE);
        for (Muscle m : muscles) {
            ArrayList<Node> con = connections.getNodes(m);
            m.mutate(MUTATION_DIVERGENCE, con.get(0).getDistance(con.get(1)));
        }
        if (RandomNumberGenerator.randBool(RANDOM_MUTATION_PROBABILITY))
            switch (RandomNumberGenerator.randInt(3)) {
                case 0:
                    mutationAddNode();
                    break;
                case 1:
                    mutationAddMuscle();
                    break;
                case 2:
                    mutationRemoveMuscle();
                    break;
                case 3:
                    mutationRemoveNode();
                    break;
            }
    }

    private Node createNewNode() {
        Node[] tmpNodes = new Node[nodes.length + 1];
        System.arraycopy(nodes, 0, tmpNodes, 0, nodes.length);
        tmpNodes[nodes.length] = new Node();
        nodes = tmpNodes;
        return nodes[nodes.length - 1];
    }

    private Muscle createNewMuscle(double length) {
        Muscle[] tmpMuscles = new Muscle[muscles.length + 1];
        System.arraycopy(muscles, 0, tmpMuscles, 0, muscles.length);
        tmpMuscles[muscles.length] = new Muscle(length);
        muscles = tmpMuscles;
        return muscles[muscles.length - 1];
    }

    private void mutationAddNode() {
        //Create new Node
        final Node newNode = createNewNode();
        Node conNode = null;
        while (newNode == conNode)
            conNode = nodes[RandomNumberGenerator.randInt(nodes.length - 1)];
        //Create new muscle to connect node with
        Muscle newMuscle = createNewMuscle(newNode.getDistance(conNode));

        connections.add(conNode, newMuscle);
        connections.add(newNode, newMuscle);

    }

    private void mutationRemoveNode() {
        Node[] tmpNode = new Node[nodes.length - 1];
        final int toDelete = RandomNumberGenerator.randInt(nodes.length - 1);
        connections.remove(nodes[toDelete]);
        System.arraycopy(nodes, 0, tmpNode, 0, toDelete);
        System.arraycopy(nodes, toDelete + 1 - 1, tmpNode, toDelete + 1, tmpNode.length - (toDelete + 1)); //TODO check if this works
    }

    private void mutationAddMuscle() {
        int n1 = 0, n2 = 0;
        while (n1 == n2) {
            n1 = RandomNumberGenerator.randInt(nodes.length - 1);
            n2 = RandomNumberGenerator.randInt(nodes.length - 1);
        }
        Muscle newMuscle = createNewMuscle(nodes[n1].getDistance(nodes[n2]));
        connections.add(nodes[n1], newMuscle);
        connections.add(nodes[n2], newMuscle);
    }

    private void mutationRemoveMuscle() {
        int toDelete;
        boolean removedMuscle;
        Muscle[] tmpMuscles = new Muscle[nodes.length - 1];
        do {
            toDelete = RandomNumberGenerator.randInt(nodes.length - 1);
            removedMuscle = connections.remove(muscles[toDelete]);
        } while (!removedMuscle);

        System.arraycopy(muscles, 0, tmpMuscles, 0, toDelete);
        System.arraycopy(muscles, toDelete + 1 - 1, tmpMuscles, toDelete + 1, tmpMuscles.length - (toDelete + 1)); //TODO check if this works
        muscles = tmpMuscles;
    }
}

class CrippledCreatureException extends RuntimeException {}