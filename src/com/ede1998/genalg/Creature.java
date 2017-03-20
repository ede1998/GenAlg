package com.ede1998.genalg;

import java.util.ArrayList;

/**
 * Created by 152863eh on 14.03.2017.
 */

public class Creature implements Comparable<Creature> {
    private static final int RESOLUTION = 15000;
    private static final double RANDOM_MUTATION_PROBABILITY = 0.03;
    private static final double MUTATION_DIVERGENCE = 0.2;
    private static final int MAX_NODES = 10;
    private static final int MAX_MUSCLES = 10;
    private static final double MORE_NODES_PROBABILITY = 0.4;
    private static final double GRAVITY = 9.81;
    private Node[] nodes;
    private Muscle[] muscles;
    private ConnectionList connections;


    private Creature(Node[] nodes, Muscle[] muscles, ConnectionList connections) {
        this.nodes = nodes;
        this.muscles = muscles;
        this.connections = connections;
    }

    public Creature() {
        connections = new ConnectionList();
        nodes = new Node[RandomNumberGenerator.randInt(MAX_NODES - 2) + 2];
        muscles = new Muscle[nodes.length + RandomNumberGenerator.randInt(MAX_MUSCLES)];
        makeCreature(0,0 , nodes.length, null);
        for (int k = muscles.length - 1; k >= 0; k--) {
            if (muscles[k] != null) break;
            int n1 = 0, n2 = 0;
            while (connections.connected(nodes[n1], nodes[n2])) { //TODO prevent infinite loop if nodes.length << muscles.length
                n1 = RandomNumberGenerator.randInt(nodes.length - 1);
                n2 = RandomNumberGenerator.randInt(nodes.length - 1);
            }
            muscles[k] = new Muscle(nodes[n1].getDistance(nodes[n2]));
            connections.add(nodes[n1], muscles[k]);
            connections.add(nodes[n2], muscles[k]);
        }
    }

    private int makeCreature(int posN, int posM, int len, Node prevNode) {
        nodes[posN] = new Node();
        if (prevNode != null) {
            muscles[posM] = new Muscle(prevNode.getDistance(nodes[posN]));
            connections.add(nodes[posN], muscles[posM]);
            connections.add(prevNode, muscles[posM]);
            posM++;
        }
        if (posN < len - 1) {
            do {
                posN = makeCreature(posN + 1, posM, len, nodes[posN]);
            }
            while (RandomNumberGenerator.randBool(MORE_NODES_PROBABILITY) && (posN < len - 1));
        }
        return posN;
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
            run();
    }

    public void reset() {
        for (Node n : nodes)
            n.reset();
    }

    private void run() {
        for (int clock = 0; clock < RESOLUTION; clock++) {
            singleStep((double) clock / RESOLUTION);
        }
    }

    private void singleStep(double timeQuotient) {
        ArrayList<Node> conN;
        for (Node n : nodes) {
            //apply effect of gravity
            n.movePosition(0, GRAVITY * timeQuotient);

            //Calculate new position for each node
            ArrayList<Muscle> conM = connections.getMuscle(n);
            double totalStrength = 0;
            for (Muscle m : conM)
                totalStrength += m.getStrength();
            for (Muscle m : conM) {
                conN = connections.getNodes(m);
                double deltaLength = (m.getTargetLength(timeQuotient) - m.getLength()) * m.getStrength() / totalStrength;
                ;
                double angle = Math.asin(Math.abs(conN.get(0).getPositionY() - conN.get(1).getPositionY()) / m.getLength());
                n.movePosition(deltaLength * Math.cos(angle), deltaLength * Math.sin(angle));
            }

            //push nodes in ground up to surface
            double newPositionY = n.getNewPositionY();
            if (newPositionY < 0) {
                n.movePosition(0, -newPositionY);
                for (Muscle m : conM) {
                    Node tmp;
                    conN = connections.getNodes(m);
                    if (conN.get(0) == n)
                        tmp = conN.get(1);
                    else
                        tmp = conN.get(0);
                    tmp.movePosition(0, -newPositionY);

                }
            }
        }
        for (Node n : nodes)
            n.forceMovement();
        //Update muscle lengths
        for (Muscle m : muscles) {
            conN = connections.getNodes(m);
            m.changeLength(conN.get(0), conN.get(1));
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

    public String toString() {
        String s = "";
        for (int i = 0; i < nodes.length; i++) {
            s += "Node " + i + ":\n";
            s += nodes[i].toString();
        }
        for (int i = 0; i < muscles.length; i++) {
            s += "Muscle " + i + ":\n";
            s += muscles[i].toString();
        }
        s += connections.toString();
        return s;
    }
}

class CrippledCreatureException extends RuntimeException {}