package com.ede1998.genalg;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Erik on 19.03.2017.
 */
public class ConnectionList extends ArrayList<ConnectionList.NodeMuscleMapping> {

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

    public boolean connected(Node n1, Node n2) {
        ArrayList<Muscle> m1 = getMuscle(n1), m2 = getMuscle(n2);
        for (Muscle m : m1) {
            if (m2.contains(m))
                return true;
        }
        return false;
    }


    static class NodeMuscleMapping {
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
}
