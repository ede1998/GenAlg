import java.util.ArrayList;
import java.util.Map;

/**
 * Created by 152863eh on 14.03.2017.
 */

public class Creature implements Comparable<Creature>{
    private Node[] nodes;
    private Muscle[] muscles;
    private ConnectionList connections;
    private static final int RESOLUTION = 15000;
    private static final double RANDOM_MUTATION_PROBABILITY = 0.03;
    private static final double MUTATION_DIVERGENCE = 0.2;


    public Creature(Node[] nodes, Muscle[] muscles, ConnectionList connections) {
        this.nodes = nodes;
        this.muscles = muscles;
        this.connections = connections;
    }

    public double getPositionX() {
        double pos = 0;
        for (int i = 0; i < nodes.length; i++)
            pos += nodes[i].getPositionX();
        return pos / nodes.length;
    }

    public double getPositionY() {
        double pos = 0;
        for (int i = 0; i < nodes.length; i++)
            pos += nodes[i].getPositionY();
        return pos / nodes.length;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Muscle[] getMuscles() {
        return muscles;
    }

    public void move(int time){
        for (int i = 0; i < time; i++)
            tryToMove();
    }

    public void reset() {
        for (Node n: nodes)
            n.reset();
    }

    private void tryToMove() {
        //TODO hope that works
        if (RESOLUTION <= 0) throw new IllegalArgumentException("Time to try must be greater than 0.");
        double n1px = 0, n1py = 0, n2py = 0, n2px = 0;
        for (int clock = 0; clock < RESOLUTION; clock++)
        {
            for (Muscle muscle: muscles) {
                double prevlen = muscle.getLength();
                muscle.tenseOrRelease(clock/ RESOLUTION);
                double deltalen = muscle.getLength() - prevlen;
                double angle = Math.asin(Math.abs(nodes[muscle.getNode1()].getPositionY() - nodes[muscle.getNode2()].getPositionY()) /prevlen);
                if (nodes[muscle.getNode1()].getPositionY() == 0) {
                    if (nodes[muscle.getNode2()].getPositionY() == 0) { //both on ground
                        //depends on friction, only x, angle = 0
                        n2px = nodes[muscle.getNode2()].getFriction() / (nodes[muscle.getNode2()].getFriction() + nodes[muscle.getNode1()].getFriction())
                                * deltalen;
                        n1px = nodes[muscle.getNode1()].getFriction() / (nodes[muscle.getNode2()].getFriction() + nodes[muscle.getNode1()].getFriction())
                                * deltalen;

                    } else { //node 1 on ground, node 2 in air
                        //node2 moves in x, node1 not; both in y
                        n2px = Math.sin(angle) * deltalen;
                        n2py = Math.cos(angle) * deltalen / 2;
                        n1py = n2py;
                    }
                }
                else {
                    if (nodes[muscle.getNode2()].getPositionY() == 0) { //node1 in air, node2 on ground
                        //node1 moves in x, node2 not; both in y
                        n1px = Math.sin(angle) * deltalen;
                        n2py = Math.cos(angle) * deltalen / 2;
                        n1py = n2py;
                    }
                    else { //both in air
                        //both move in x and y
                        n2px = Math.sin(angle) * deltalen / 2;
                        n1px = n2px;
                        n2py = Math.cos(angle) * deltalen / 2;
                        n1py = n2py;
                    }
                }
                nodes[muscle.getNode1()].movePosition(n1px, n1py);
                nodes[muscle.getNode2()].movePosition(n2px, n2py);
                //TODO prevent form slipping in ground y < 0!
            }
            for (Node n: nodes) {
                n.forceMovement();
            }
        }
    }

    @Override
    public Creature clone() {
        Node[] n = new Node[nodes.length];
        Muscle[] m = new Muscle[muscles.length];
        ConnectionList c =  new ConnectionList();

        c.addAll(connections);
        for (int i = 0; i < nodes.length; i++)
            n[i] = nodes[i].clone();
        for (int i = 0; i < muscles.length; i++)
            m[i] = muscles[i].clone();
        return new Creature(n,m, c);
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
        for (int i = 0; i < nodes.length; i++)
            nodes[i].mutate(MUTATION_DIVERGENCE);
        for (int i = 0; i < muscles.length; i++)
            muscles[i].mutate(MUTATION_DIVERGENCE, nodes[muscles[i].getNode1()]);//TODO Update index
        if (RandomNumberGenerator.randbool(RANDOM_MUTATION_PROBABILITY))
            switch (RandomNumberGenerator.randint(1)) {
                case 0: //Node
                    nodeMutation(RandomNumberGenerator.randint(1));
                    break;
                case 1: //Muscle
                    muscleMutation(RandomNumberGenerator.randint(1));
                    break;
            }
    }

    private void nodeMutation(int doAdd) {
        Node[] tmpnode;
         switch (doAdd) {
             case 0:
                 tmpnode = new Node[nodes.length + 1];
                 for (int i = 0; i < nodes.length; i++) {
                     tmpnode[i] = nodes[i];
                 }
                 tmpnode[nodes.length] = new Node(RandomNumberGenerator.random(), RandomNumberGenerator.random(0,Node.MAX_POS_X),
                         RandomNumberGenerator.random(0, Node.MAX_POS_Y));
                 break;
             default:
                 tmpnode = new Node[nodes.length - 1];
                 final int todelete = RandomNumberGenerator.randint(nodes.length - 1);
                 for (int i = 0; i < todelete; i++) {
                     tmpnode[i] = nodes[i];
                 }
                 for (int i = todelete + 1; i < tmpnode.length; i++) {
                     tmpnode[i] = nodes[i - 1];
                 }
                 break;
        }
        nodes = tmpnode;
    }

    private void muscleMutation(int doAdd) {
        Muscle[] tmpmuscle;
        switch (doAdd) {
            case 0:
                tmpmuscle = new Muscle[muscles.length + 1];
                for (int i = 0; i < muscles.length; i++) {
                    tmpmuscle[i] = muscles[i];
                }
                int n1 = 0, n2 = 0;
                while (n1 == n2) {
                    n1 = RandomNumberGenerator.randint(nodes.length);
                    n2 = RandomNumberGenerator.randint(nodes.length);
                }
                final double length = nodes[n1].getDistance(nodes[n2]);
                tmpmuscle[muscles.length] = new Muscle(length - RandomNumberGenerator.random(length),
                        length + RandomNumberGenerator.random(Muscle.MAX_MUSCLE_LENGTH - length),
                        RandomNumberGenerator.random(), RandomNumberGenerator.random(), n1, n2);
                //todo
                break;
            default:
                tmpmuscle = new Muscle[muscles.length - 1];
                final int todelete = RandomNumberGenerator.randint(muscles.length - 1);
                for (int i = 0; i < todelete; i++) {
                    tmpmuscle[i] = muscles[i];
                }
                for (int i = todelete + 1; i < tmpmuscle.length; i++) {
                    tmpmuscle[i] = muscles[i - 1];
                }
                break;
        }
        muscles = tmpmuscle;
    }
}

class Muscle {
   // private double strength;
    private double contractedLength;
    private double extendedLength;
    private double timeContractionStart;
    private double timeExtensionStart;
    private double length;
    public static final double MAX_MUSCLE_LENGTH = 12;

    public Muscle(double contractedLength, double extendedLength, double timeContractionStart, double timeExtensionStart) {

        //this.strength = strength;
        this.contractedLength = contractedLength;
        this.extendedLength = extendedLength;
        if (contractedLength > extendedLength) throw new IllegalArgumentException("Extended muscle must be longer than contracted muscle.");
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
    }

    public double getLength() {
        return length;
    }

    public void tenseOrRelease(double timeQuotient){
        length = calcLengthQuotient(timeQuotient) * (extendedLength - contractedLength) + contractedLength;
    }

    private double calcLengthQuotient(double timeQuotient) {
        if ((timeQuotient > 1) || (timeQuotient < 0)) throw new IllegalArgumentException("timeQuotient must be between 0 and 1.");
        //equal to contraction
        if (timeQuotient == timeContractionStart) {
            return 1;
        }
        // equal to extension
        else if (timeQuotient == timeExtensionStart){
            return 0;
        }
        //smaller than both
        else if ((timeQuotient < timeExtensionStart) && (timeQuotient < timeContractionStart)) {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart)
            {
                return (1 - timeContractionStart + timeQuotient)/(1-(timeContractionStart-timeExtensionStart));
            }
            else //C < E => it's extension time
            {
                return (1 - timeExtensionStart + timeQuotient)/(1-(timeExtensionStart-timeContractionStart));
            }
        }
        //larger than both
        else if ((timeQuotient > timeExtensionStart) && (timeQuotient > timeContractionStart)) {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart)
            {
                return (timeQuotient - timeContractionStart)/(1-(timeContractionStart-timeExtensionStart));
            }
            else //C < E => it's extension time
            {
                return (timeQuotient - timeExtensionStart)/(1-(timeExtensionStart-timeContractionStart));
            }
        }
        //between both
        else
        {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart)
            {
                return (timeQuotient - timeExtensionStart) / (timeContractionStart - timeExtensionStart);
            }
            else //C < E => it's extension time
            {
                return 1 - ((timeQuotient - timeContractionStart) / (timeExtensionStart - timeContractionStart));
            }
        }
    }

    public int findConnectedNode(int otherNode) {
        if (otherNode == node1) return node2;
        if (otherNode == node2) return node1;
        throw new IllegalArgumentException("No connection found.");
    }

    @Override
    public Muscle clone() {
        return new Muscle(contractedLength, extendedLength, timeContractionStart, timeExtensionStart);
    }

    public void mutate(double divergence, double startingdist) {
        contractedLength *= RandomNumberGenerator.randG(divergence, 1);
        extendedLength *= RandomNumberGenerator.randG(divergence, 1);
        if (contractedLength > startingdist) contractedLength = startingdist;
        if (extendedLength < startingdist) extendedLength = startingdist;
        length = startingdist;

        timeContractionStart *= RandomNumberGenerator.randG(divergence, 1);
        while (timeContractionStart >= 1) timeContractionStart--;
        while (timeContractionStart < 0) timeContractionStart++;
        timeExtensionStart *= RandomNumberGenerator.randG(divergence, 1);
        if (timeExtensionStart >= 1) timeExtensionStart--;
        if (timeExtensionStart < 0) timeExtensionStart++;
    }
}

class Node {
    private double friction;
    private double positionX;
    private double positionY;
    private double startPositionX;
    private double startPositionY;
    private double deltaX, deltaY;
    public static final double MAX_POS_X = 10;
    public static final double MAX_POS_Y = 10;


    public Node(double friction, double posx, double posy) throws IllegalArgumentException {
        if ((friction < 0) || (friction > 1))
            throw new IllegalArgumentException("Node friction must be between 0 and 1.");
        this.friction = friction;
        this.positionX = posx;
        this.startPositionX = posx;
        if (posy < 0)
            throw new IllegalArgumentException("Node must be above ground.");
        this.positionY = posy;
        this.startPositionY  = posy;
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
        return Math.sqrt(Math.pow(this.getPositionX() - n2.getPositionX(),2) + Math.pow(this.getPositionY() - n2.getPositionY(),2));
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
}

class NodeMuscleMapping {
    private Node node;
    private Muscle muscle;

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

    public boolean add(Node node, Muscle muscle) {
        return super.add(new NodeMuscleMapping(node, muscle));
    }

    public ArrayList<Muscle> getMuscle(Node node) {
        ArrayList<Muscle> muscle = new ArrayList<Muscle>();
        this.forEach((NodeMuscleMapping m) -> {
            if (m.getNode() == node)
                muscle.add(m.getMuscle());
        });
        return muscle;
    }

    public ArrayList<Node> getNode(Muscle muscle) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        this.forEach((NodeMuscleMapping m) -> {
            if (m.getMuscle() == muscle)
                nodes.add(m.getNode());
        });
        return nodes;
    }
}