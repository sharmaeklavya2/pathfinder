package graph;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Math;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import graph.AbstractGraph;
import graph.GridGraphNode;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class GridGraph extends AbstractGraph
{
    private int rows, cols;
    private int _size;
    private GridGraphNode[] grid;

    public int getRows() {return rows;}
    public int getCols() {return cols;}
    public int size() {return _size;}

    synchronized public long norm(int src, int dst) {
        int si = src / cols, sj = src % cols;
        int di = dst / cols, dj = dst % cols;
        long norm = (long)(si-di)*(si-di) + (long)(sj-dj)*(sj-dj);
        if(grid[src].getType() == grid[dst].getType() && norm <= 2)
            return norm;
        else
            return -1;
    }

    public boolean adjacent(int src, int dst) {
        return norm(src, dst) <= 0;
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

    synchronized public Map<Integer, Double> getNbrs(int u) {
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

    public GridGraph(int rows, int cols, int[] types) {
        this.rows = rows;
        this.cols = cols;
        this._size = rows * cols;
        if(types.length != size())
            throw new IllegalArgumentException("length of types is not equal to grid size");
        for(int i=0; i < types.length; ++i) {
            grid[i] = new GridGraphNode(types[i], 1);
        }
    }
    public GridGraph(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this._size = rows * cols;
        for(int i=0; i < _size; ++i) {
            grid[i] = new GridGraphNode(0, 1);
        }
    }

    synchronized void update(int i, int j, GridGraphNode node) {
        grid[i * cols + j] = node;
    }

    public GridGraph(BufferedReader br) throws IOException, Exception {
        String s;
        cols = -1;
        ArrayList<GridGraphNode> gridArray = new ArrayList<GridGraphNode>();
        for(rows = 0; (s = br.readLine()) != null; rows++) {
            s = s.trim();
            System.err.println("<" + s + ">");
            if(cols != s.length()) {
                if(cols == -1)
                    cols = s.length();
                else
                    throw new Exception("Rows are of different lengths");
            }
            gridArray.ensureCapacity(cols * (rows + 1));
            for(int j=0; j < cols; ++j) {
                char ch = s.charAt(j);
                int type = (ch == '0' || ch == '-') ? 0 : 1;
                gridArray.add(new GridGraphNode(type, 1));
            }
        }
        grid = new GridGraphNode[gridArray.size()];
        _size = rows * cols;
        for(int i=0; i<_size; ++i)
            grid[i] = gridArray.get(i);
    }

    public GridGraph(String fpath) throws IOException, Exception {
        this(new BufferedReader(new FileReader(fpath)));
    }
    public static void main(String[] args) throws IOException, Exception {
        System.out.println((new GridGraph(args[0])).toGraph().toString());
    }
}
