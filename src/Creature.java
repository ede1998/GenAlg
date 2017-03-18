import java.util.ArrayList;
import java.util.Iterator;

/**
 * Contains the creature and its nodes and muscles, also how they are connected
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

    public Creature() {
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

    /**
     * This method calls trytoMove() time times, so it simulates a time span in which the creature can try to walk.
     * @param time number of repetitons of creature movement
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

class Muscle {
    private static final double MAX_MUSCLE_LENGTH = 12;
    // private double strength;
    private double contractedLength;
    private double extendedLength;
    private double timeContractionStart;
    private double timeExtensionStart;
    private double length;

    private Muscle(double length, double contractedLength, double extendedLength, double timeContractionStart, double timeExtensionStart) {

        //this.strength = strength;
        this.contractedLength = contractedLength;
        this.extendedLength = extendedLength;
        if (contractedLength > extendedLength)
            throw new IllegalArgumentException("Extended muscle must be longer than contracted muscle.");
        if ((timeContractionStart < 0) || (timeContractionStart > 1)) {
            throw new IllegalArgumentException("timeContractionStart must be between 0 and 1.");
        }
        if ((timeExtensionStart < 0) || (timeExtensionStart > 1)) {
            throw new IllegalArgumentException(("timeExtensionStart must be between 0 and 1."));
        }
        if (timeContractionStart == timeExtensionStart) {
            throw new IllegalArgumentException("timeExtensionStart must differ from timeContractionStart.");
        }

        this.timeContractionStart = timeContractionStart;
        this.timeExtensionStart = timeExtensionStart;
        this.length = length;
    }

    public Muscle(double length) {
       this(length, length - RandomNumberGenerator.random(length),
                        length + RandomNumberGenerator.random(Muscle.MAX_MUSCLE_LENGTH - length),
                        RandomNumberGenerator.random(), RandomNumberGenerator.random());
    }

    public double getLength() {
        return length;
        //TODO make sure length is initialized before first call
    }

    public void tenseOrRelease(double timeQuotient) {
        length = calcLengthQuotient(timeQuotient) * (extendedLength - contractedLength) + contractedLength;
    }

    private double calcLengthQuotient(double timeQuotient) {
        if ((timeQuotient > 1) || (timeQuotient < 0))
            throw new IllegalArgumentException("timeQuotient must be between 0 and 1.");
        //equal to contraction
        if (timeQuotient == timeContractionStart) {
            return 1;
        }
        // equal to extension
        else if (timeQuotient == timeExtensionStart) {
            return 0;
        }
        //smaller than both
        else if ((timeQuotient < timeExtensionStart) && (timeQuotient < timeContractionStart)) {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart) {
                return (1 - timeContractionStart + timeQuotient) / (1 - (timeContractionStart - timeExtensionStart));
            } else //C < E => it's extension time
            {
                return (1 - timeExtensionStart + timeQuotient) / (1 - (timeExtensionStart - timeContractionStart));
            }
        }
        //larger than both
        else if ((timeQuotient > timeExtensionStart) && (timeQuotient > timeContractionStart)) {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart) {
                return (timeQuotient - timeContractionStart) / (1 - (timeContractionStart - timeExtensionStart));
            } else //C < E => it's extension time
            {
                return (timeQuotient - timeExtensionStart) / (1 - (timeExtensionStart - timeContractionStart));
            }
        }
        //between both
        else {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart) {
                return (timeQuotient - timeExtensionStart) / (timeContractionStart - timeExtensionStart);
            } else //C < E => it's extension time
            {
                return 1 - ((timeQuotient - timeContractionStart) / (timeExtensionStart - timeContractionStart));
            }
        }
    }

    @Override
    public Muscle clone() {
        return new Muscle(length, contractedLength, extendedLength, timeContractionStart, timeExtensionStart);
    }

    public void mutate(double divergence, double startingDist) {
        contractedLength *= RandomNumberGenerator.randG(divergence, 1);
        extendedLength *= RandomNumberGenerator.randG(divergence, 1);
        if (contractedLength > startingDist) contractedLength = startingDist;
        if (extendedLength < startingDist) extendedLength = startingDist;
        length = startingDist;

        timeContractionStart *= RandomNumberGenerator.randG(divergence, 1);
        while (timeContractionStart >= 1) timeContractionStart--;
        while (timeContractionStart < 0) timeContractionStart++;
        timeExtensionStart *= RandomNumberGenerator.randG(divergence, 1);
        if (timeExtensionStart >= 1) timeExtensionStart--;
        if (timeExtensionStart < 0) timeExtensionStart++;
    }
}

class Node {
    private static final double MAX_POS_X = 10;
    private static final double MAX_POS_Y = 10;
    private double friction;
    private double positionX;
    private double positionY;
    private double startPositionX;
    private double startPositionY;
    private double deltaX, deltaY;
    private int connections;


    private Node(double friction, double posX, double posY) throws IllegalArgumentException {
        if ((friction < 0) || (friction > 1))
            throw new IllegalArgumentException("Node friction must be between 0 and 1.");
        this.friction = friction;
        this.positionX = posX;
        this.startPositionX = posX;
        if (posY < 0)
            throw new IllegalArgumentException("Node must be above ground.");
        this.positionY = posY;
        this.startPositionY = posY;
        connections = 0;
    }

    public Node() {
        this(RandomNumberGenerator.random(), RandomNumberGenerator.random(0, MAX_POS_X),
                        RandomNumberGenerator.random(0, MAX_POS_Y));
    }

    public double getFriction() {
        return friction;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void movePosition(double deltaX, double deltaY) {
        this.deltaX += deltaX;
        this.deltaY += deltaY;
    }

    public void forceMovement() {
        positionX += deltaX;
        positionY += deltaY;
        deltaX = 0;
        deltaY = 0;
    }

    public void reset() {
        this.positionX = startPositionX;
        this.positionY = startPositionY;
        deltaX = deltaY = 0;
    }

    @Override
    public Node clone() {
        return new Node(friction, positionX, positionY);
    }

    public double getDistance(Node n2) {
        return Math.sqrt(Math.pow(this.getPositionX() - n2.getPositionX(), 2) + Math.pow(this.getPositionY() - n2.getPositionY(), 2));
    }

    public void mutate(double divergence) {
        startPositionY *= RandomNumberGenerator.randG(divergence, 1);
        if (startPositionY < 0)
            startPositionY = 0;
        startPositionX *= RandomNumberGenerator.randG(divergence, 1);
        positionX = startPositionX;
        positionY = startPositionY;
        friction *= RandomNumberGenerator.randG(divergence, 1);
    }

    public void addConnection() {
        connections++;
    }

    public void removeConnection() {
        if (connections < 2) throw new CrippledCreatureException();
        connections--;
    }

    public int getConnections() {
        return connections;
    }
}

class NodeMuscleMapping {
    private final Node node;
    private final Muscle muscle;

    public NodeMuscleMapping(Node node, Muscle muscle) {
        this.node = node;
        this.muscle = muscle;
    }

    public Node getNode() {
        return node;
    }

    public Muscle getMuscle() {
        return muscle;
    }
}

class ConnectionList extends ArrayList<NodeMuscleMapping> {

    @SuppressWarnings("UnusedReturnValue")
    public boolean add(Node node, Muscle muscle) {
        node.addConnection();
        return super.add(new NodeMuscleMapping(node, muscle));
    }

    public void remove(Node node) {
        forEach((NodeMuscleMapping nmm) -> {if (nmm.getNode() == node) remove(nmm.getMuscle());}); //TODO check if works
    }

    public boolean remove(Muscle muscle) {
        Iterator<NodeMuscleMapping> tmp = iterator();
        NodeMuscleMapping nmm;
        while (tmp.hasNext()) {
            nmm = tmp.next();
            if (nmm.getMuscle() == muscle)
                if (nmm.getNode().getConnections() < 2)
                    return false;
        }
        forEach((NodeMuscleMapping nmm1) -> {
            if (nmm1.getMuscle() == muscle) {
                super.remove(nmm1);
                nmm1.getNode().removeConnection();
            }
        }); //TODO check if works
        return true;
    }

    public ArrayList<Muscle> getMuscle(Node node) {
        ArrayList<Muscle> muscle = new ArrayList<>();
        this.forEach((NodeMuscleMapping m) -> {
            if (m.getNode() == node)
                muscle.add(m.getMuscle());
        });
        return muscle;
    }

    public ArrayList<Node> getNodes(Muscle muscle) {
        ArrayList<Node> nodes = new ArrayList<>();
        this.forEach((NodeMuscleMapping m) -> {
            if (m.getMuscle() == muscle)
                nodes.add(m.getNode());
        });
        if (nodes.size() != 2)
            throw new CrippledCreatureException();
        return nodes;
    }

}

class CrippledCreatureException extends RuntimeException {}