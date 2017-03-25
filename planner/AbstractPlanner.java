package planner;

import graph.AbstractGraph;
import graph.Graph;

abstract class AbstractPlanner
{
    protected int goal, curr;
    protected AbstractGraph graphRemote;
    protected Graph graphLocal;
    protected int distance;
    // graphRemote can be (in fact, we sometimes want it to be) modified from outside.

    public int getGoal() {return goal;}
    public int getCurr() {return curr;}
    public String getLocalGraphStr() {return graphLocal.toString();}
    public double getDistance() {return distance;}

    public abstract void reset(int curr);
    public abstract boolean checkAndUpdate() throws Exception;
    public abstract long replan();

    public abstract long move() throws Exception;
}
