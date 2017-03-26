package driver;

import util.CmdUtil;
import graph.*;
import planner.*;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AdjDriver
{
    public static void main(String[] args) throws IOException, GridGraphCreateException
    {
        String usage = "usage: java driver.AdjacentDriver [file]";
        BufferedReader inbr = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader fbr = CmdUtil.getBrFromArgs(args, usage, true);

        System.out.print("Enter graph class: ");
        String gtype = inbr.readLine();

        System.out.print("Enter planner type (0 for Dijkstra): ");
        String ptype = inbr.readLine();

        System.out.print("Enter start vertex (-1 for last vertex): ");
        int start_v = Integer.parseInt(inbr.readLine());
        System.out.print("Enter goal vertex (-1 for last vertex): ");
        int goal_v = Integer.parseInt(inbr.readLine());

        AbstractGraph graph;
        if(gtype.equals("Graph"))
            graph = new Graph(fbr);
        else if(gtype.equals("GridGraph"))
            graph = new GridGraph(fbr);
        else
            throw new RuntimeException("Invalid graph class");
        if(start_v == -1)
            start_v = graph.size() - 1;
        if(goal_v == -1)
            goal_v = graph.size() - 1;

        AbstractAdjacentPlanner planner;
        if(ptype.equals("0"))
            planner = new DijkstraPlanner(start_v, goal_v, graph);
        else
            throw new RuntimeException("Invalid planner");

        System.out.println("Path: " + planner.getPath(planner.getCurr()));
        long total_replan_time = 0;

        while(planner.getCurr() != planner.getGoal()) {
            System.out.print("> ");
            System.out.flush();
            String[] words = inbr.readLine().split(" ");
            if(words.length > 0) {
                if(words[0].equals("move")) {
                    long time_taken = planner.move();
                    System.out.println("Replanning time: " + time_taken);
                    System.out.println("Path: " + planner.getPath(planner.getCurr()));
                    total_replan_time += time_taken;
                }
                else if(words[0].equals("local")) {
                    System.out.println("Local graph: " + planner.getLocalGraphStr());
                }
                else if(words[0].equals("update") && (graph instanceof Graph)) {
                    int u = Integer.parseInt(words[1]);
                    int v = Integer.parseInt(words[2]);
                    double w = Double.parseDouble(words[3]);
                    ((Graph)graph).update(new Edge(u, v, w), true);
                }
                else if(words[0].equals("update") && (graph instanceof GridGraph)) {
                    int i = Integer.parseInt(words[1]);
                    int j = Integer.parseInt(words[2]);
                    int t = Integer.parseInt(words[3]);
                    ((GridGraph)graph).update(i, j, new GridGraphNode(t, 1));
                }
                else if(words[0].equals("break") && (graph instanceof Graph)) {
                    int u = Integer.parseInt(words[1]);
                    int v = Integer.parseInt(words[2]);
                    ((Graph)graph).breakEdge(u, v);
                }
                else {
                    System.out.println("Invalid command");
                }
            }
        }
        System.out.println("Goal reached!");
        System.out.println("Distance travelled: " + planner.getDistance());
        System.out.println("Replanning time: " + total_replan_time);
    }
}
