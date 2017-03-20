package com.ede1998.genalg;

/**
 * Created by Erik on 19.03.2017.
 */
public class Node {
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

    /**
     * This method adds the given parameters to an internal shift variable. To apply the shift, call forceMovement().
     * @param deltaX shift in X direction
     * @param deltaY shift in Y direction
     */
    void movePosition(double deltaX, double deltaY) {
        this.deltaX += deltaX;
        this.deltaY += deltaY;
    }

    double getNewPositionY() {
        return positionY + deltaY;
    }

    double getNewPositionX() {
        return positionX + deltaX;
    }

    void forceMovement() {
        positionX += deltaX;
        positionY += deltaY;
        deltaX = 0;
        deltaY = 0;
    }

    void reset() {
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

    public String toString() {
        String s = "";
        s += "  Friction: " + friction + "\n";
        s += "  Position: (" + positionX + "|" + positionY + ")\n";
        return s;
    }
}
