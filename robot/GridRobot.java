package robot;

import java.util.*;

import robot.Robot;
import graph.GridGraph;

/** A {@link Robot} whose environment is a grid. */
public interface GridRobot extends Robot {
    public GridGraph getGraph();
}
