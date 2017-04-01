package graph;

import java.util.*;
import java.lang.Math;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import graph.AbstractGraph;
import util.CmdUtil;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class GridGraph extends AbstractGraph
{
    public static class Node
    {
        private int type;
        private int occ;    // occupancy

        public int getType() {return type;}
        public int getOcc() {return occ;}

        public Node(int type, int occ) {
            this.type = type;
            this.occ = occ;
        }
        public Node() {
            this(0, 1);
        }
        public Node(Node n) {
            this(n.getType(), n.getOcc());
        }
        public boolean isSame(Node n) {
            return (type == n.getType()) && (occ == n.getOcc());
        }

        public String toString() {
            return "GridGraph.Node(" + type + ", " + occ + ")";
        }
    }

    public static class CreateException extends Exception
    {
        public CreateException(String s) {super(s);}
    }

    public static abstract class UpdateCallback {
        public abstract void run(int i, int j);
    }

    private int rows, cols;
    private int _size;
    private Node[] grid;
    private UpdateCallback updateCallback;

    public int getRows() {return rows;}
    public int getCols() {return cols;}
    public int size() {return _size;}
    public Node getNode(int i, int j) {return grid[i * cols + j];}
    public Node getNode(int u) {return grid[u];}

    public UpdateCallback getCallback() {
        return updateCallback;
    }
    public void setCallback(UpdateCallback callback) {
        this.updateCallback = callback;
    }

    synchronized public long norm(int src, int dst) {
        int si = src / cols, sj = src % cols;
        int di = dst / cols, dj = dst % cols;
        long norm = (long)(si-di)*(si-di) + (long)(sj-dj)*(sj-dj);
        if(grid[src].getType() == grid[dst].getType() && norm <= 2)
            return norm;
        else
            return -1;
    }

    public int diffI(int u, int v) {
        return v / cols - u / cols;
    }
    public int diffJ(int u, int v) {
        return v % cols - u % cols;
    }

    public boolean hasEdge(int src, int dst) {
        long nrm = norm(src, dst);
        return nrm > 0 && nrm <= 2;
    }

    synchronized public double getWeight(int src, int dst) {
        long nrm = norm(src, dst);
        if(nrm <= 0)
            return Double.POSITIVE_INFINITY;
        else
            return Math.sqrt(nrm) * max(grid[src].getOcc(), grid[dst].getOcc());
    }

    public String toString() {
        return "GridGraph(" + rows + ", " + cols + ")";
    }

    synchronized public Map<Integer, Double> getPredsCopy(int u) {
        Map<Integer, Double> hm = new HashMap<Integer, Double>(8);
        int ui = u / cols, uj = u % cols;
        int[] drows = {-1, 0, 1};
        int[] dcols = drows;
        for(int i=0; i < drows.length; ++i) {
            int vi = ui + drows[i];
            if(vi >= 0 && vi < rows) {
                for(int j=0; j < dcols.length; ++j) {
                    int vj = uj + dcols[j];
                    int v = vi * cols + vj;
                    if(vj >= 0 && vj < cols && (u != v)) {
                        if(grid[u].getType() == grid[v].getType()) {
                            long nrm = (long)(vi-ui)*(vi-ui) + (long)(vj-uj)*(vj-uj);
                            double w = Math.sqrt(nrm) * max(grid[u].getOcc(), grid[v].getOcc());
                            hm.put(v, w);
                        }
                    }
                }
            }
        }
        return hm;
    }
    public Map<Integer, Double> getPreds(int u) {return getPredsCopy(u);}
    public Map<Integer, Double> getSuccsCopy(int u) {return getPredsCopy(u);}
    public Map<Integer, Double> getSuccs(int u) {return getPredsCopy(u);}
    public Set<Integer> getNbrs(int u) {return getPredsCopy(u).keySet();}

    public GridGraph(int rows, int cols, int[] types) {
        this.rows = rows;
        this.cols = cols;
        this._size = rows * cols;
        grid = new Node[_size];
        if(types.length != size())
            throw new IllegalArgumentException("length of types is not equal to grid size");
        for(int i=0; i < types.length; ++i) {
            grid[i] = new Node(types[i], 1);
        }
    }
    public GridGraph(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this._size = rows * cols;
        grid = new Node[_size];
        for(int i=0; i < _size; ++i) {
            grid[i] = new Node(0, 1);
        }
    }

    public GridGraph(GridGraph gridGraph) {
        this.rows = gridGraph.getRows();
        this.cols = gridGraph.getCols();
        this._size = rows * cols;
        grid = new Node[_size];
        for(int i=0; i < _size; ++i) {
            grid[i] = new Node(gridGraph.getNode(i));
        }
    }

    public void copyFrom(GridGraph gridGraph) throws CreateException {
        if(gridGraph.getRows() != this.rows) {
            throw new CreateException("number of rows don't match");
        }
        else if(gridGraph.getCols() != this.cols) {
            throw new CreateException("number of columns don't match");
        }
        for(int i=0; i < _size; ++i) {
            grid[i] = new Node(gridGraph.getNode(i));
        }
    }

    synchronized public void update(int i, int j, Node node) {
        grid[i * cols + j] = node;
        if(updateCallback != null)
            updateCallback.run(i, j);
    }
    synchronized public void update(int u, Node node) {
        grid[u] = node;
        if(updateCallback != null)
            updateCallback.run(u / cols, u % cols);
    }

    public static String zeros = "0-SsGg";

    public static Node nodeFromChar(char ch) throws CreateException {
        if(ch == ' ')
            throw new CreateException("Encountered space while reading GridGraph");
        int type = 1;
        for(int i=0; i < zeros.length(); ++i) {
            if(ch == zeros.charAt(i)) {
                type = 0;
                break;
            }
        }
        return new Node(type, 1);
    }

    public GridGraph(BufferedReader br) throws IOException, CreateException {
        String s;
        cols = -1;
        ArrayList<Node> gridArray = new ArrayList<Node>();
        for(rows = 0; (s = br.readLine()) != null; rows++) {
            s = s.trim();
            if(cols != s.length()) {
                if(cols == -1)
                    cols = s.length();
                else
                    throw new CreateException("Rows are of different lengths");
            }
            gridArray.ensureCapacity(cols * (rows + 1));
            for(int j=0; j < cols; ++j) {
                char ch = s.charAt(j);
                gridArray.add(nodeFromChar(ch));
            }
        }
        grid = new Node[gridArray.size()];
        _size = rows * cols;
        for(int i=0; i<_size; ++i)
            grid[i] = gridArray.get(i);

        /*
        for(int i=0; i<rows; ++i) {
            for(int j=0; j<cols; ++j) {
                System.err.print(" " + grid[i * cols + j]);
            }
            System.err.println();
        }
        */
    }

    public GridGraph(List<String> lines) throws CreateException {
        rows = lines.size();
        cols = lines.get(0).length();
        for(String line: lines)
            if(line.length() != cols)
                    throw new CreateException("Rows are of different lengths");
        _size = rows * cols;
        grid = new Node[_size];

        for(int i=0; i < rows; ++i) {
            for(int j=0; j < cols; ++j) {
                char ch = lines.get(i).charAt(j);
                grid[i * cols + j] = nodeFromChar(ch);
            }
        }
    }

    public String serialize() {
        char[] a = new char[rows * (cols + 1)];
        int k = 0;
        for(int i=0; i<rows; ++i) {
            for(int j=0; j<cols; ++j) {
                int type = grid[i*cols + j].getType();
                if(type == 0) {
                    a[k++] = '-';
                }
                else {
                    a[k++] = 'O';
                }
            }
            a[k++] = '\n';
        }
        return new String(a);
    }

    public static void main(String[] args) throws IOException, CreateException {
        String usage = "usage: java graphs.GridGraph [file]";
        GridGraph graph = new GridGraph(CmdUtil.getBrFromArgs(args, usage, true));
        System.out.println(graph.toGenGraph().toString());
    }
}
