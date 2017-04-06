package planner;

import java.util.*;
import gridpanel.GridPanelCell;
import graph.AbstractGraph;
import graph.GenGraph;
import robot.Robot;

/** Abstract base class for path planning algorithms */
public abstract class AbstractPlanner
{
    /** Callback object */
    public static class Callback {
        /** called when node {@code u} changes. */
        public void nodeUpdate(int u){}
        /** called when robot is told to move from {@code u} to {@code v}. */
        public void move(int u, int v){}
        /** called when optimal path changes. */
        public void pathUpdate(){}
        /** called when bulk updates are made to the planner. */
        public void fullUpdate(){}
    }

    protected int goal;
    /** Distance travelled till now by the robot. */
    protected double distance;
    protected Callback callback;

    protected AbstractPlanner() {
        this.callback = new Callback();
        this.distance = 0;
    }

    public int getGoal() {return goal;}
    /** Distance travelled till now by the robot. */
    public double getDistance() {
        return this.distance;
    }

    public Callback getCallback() {return callback;}
    public void setCallback(Callback callback) {
        if(callback == null) {
            this.callback = new AbstractPlanner.Callback();
        }
        else {
            this.callback = callback;
        }
    }

    public abstract Robot getRobot();
    public abstract void resetRobot(Robot robot);
    /** Get the best node which a robot should move to if the robot is at node {@code u}. */
    public abstract int getNext(int u);

    /** Reset planner to the way it was right after the constructor was called. */
    public abstract void reset();
    /** Replan best path. */
    public abstract long replan();

    /** Examine set of changed nodes reported by robot. */
    protected abstract void examineUpdates(Set<Integer> updatedNodes);

    /** Return a visual representation of node {@code u}.
        @param onPath whether u is supposed to be drawn with the assumption
            that it is on the optimal path. */
    public abstract GridPanelCell getGridPanelCell(int u, boolean onPath);
    /** Return a visual representation of node {@code u}. */
    public GridPanelCell getGridPanelCell(int u) {
        return getGridPanelCell(u, false);
    }

    /** Get a list of nodes which form the optimal path from node {@code u} onwards. */
    public List<Integer> getPath(int u) {
        List<Integer> l = new ArrayList<Integer>();
        if(getNext(u) == -1)
            return l;
        while(u != goal) {
            l.add(u);
            u = getNext(u);
        }
        l.add(goal);
        return l;
    }

    /** Try to move one step towards the goal.
        @param radius Radius of sensor used to detect changes in environment. */
    synchronized public long move(int radius) {
        int u = getRobot().getPosition();
        if(u == goal)
            throw new RuntimeException("Already at goal");

        Set<Integer> updatedNodes = getRobot().getUpdatedNodes(radius);
        boolean changed = (updatedNodes.size() > 0);
        examineUpdates(updatedNodes);

        long time_taken = 0;
        if(changed)
            time_taken = replan();
        int v = getNext(u);
        if(v == -1)
            throw new RuntimeException("No path to destination");

        getRobot().moveTo(v);
        // moveTo might not actually move to v
        int v2 = getRobot().getPosition();
        distance += getRobot().getGraph().getWeight(u, v2);
        callback.move(u, v);
        return time_taken;
    }
    public long move() {return this.move(0);}
}
