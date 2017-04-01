/* Modified version of the grid-drawing code at http://stackoverflow.com/a/15422801 */

package gridpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

import java.lang.IllegalArgumentException;

import gridpanel.GridPanelCell;

public class GridPanel extends JPanel
{
    private int cols, rows;
    private int pref_cell_size;
    private Rectangle[] rects;
    private GridPanelCell[] cells;
    private int mouseCellIndex;
    private boolean needsRectsRecalc;
    public static final int timerDelay = 50;

    public int getRows() {return rows;}
    public int getCols() {return cols;}

    public GridPanel(int rows, int cols)
    {
        if(cols <= 0 || rows <= 0)
            throw new IllegalArgumentException("Grid dimensions should be positive.");
        this.cols = cols;
        this.rows = rows;
        int pref_cell_size1 = 640 / cols;
        int pref_cell_size2 = 480 / rows;
        pref_cell_size = (pref_cell_size1 < pref_cell_size2 ? pref_cell_size1 : pref_cell_size2);

        rects = new Rectangle[cols * rows];
        needsRectsRecalc = true;
        cells = new GridPanelCell[cols * rows];
        for(int i=0; i<cells.length; ++i)
            cells[i] = new GridPanelCell();

        MouseAdapter mouseHandler;
        mouseHandler = new MouseAdapter() {
            int row, col;

            public void getCoords(MouseEvent e) {
                int width = getWidth();
                int height = getHeight();

                int cellWidth = width / cols;
                int cellHeight = height / rows;

                int xOffset = (width % cols) / 2;
                int yOffset = (height % rows) / 2;

                col = (e.getX() >= xOffset) ? (e.getX() - xOffset) / cellWidth : -1;
                if(col >= cols)
                    col = -1;
                row = (e.getY() >= yOffset) ? (e.getY() - yOffset) / cellHeight : -1;
                if(row >= rows)
                    row = -1;
                mouseCellIndex = (col != -1 && row != -1) ? row * cols + col : -1;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                getCoords(e);
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                getCoords(e);
                handleClick(row, col);
                repaint();
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(pref_cell_size * cols, pref_cell_size * rows);
    }

    @Override
    public void invalidate() {
        needsRectsRecalc = true;
        mouseCellIndex = -1;
        super.invalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        int cellWidth = width / cols;
        int cellHeight = height / rows;

        int xOffset = (width % cols) / 2;
        int yOffset = (height % rows) / 2;

        if (needsRectsRecalc) {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    Rectangle rect = new Rectangle(
                        xOffset + (col * cellWidth),
                        yOffset + (row * cellHeight),
                        cellWidth,
                        cellHeight);
                    rects[row * cols + col] = rect;
                }
            }
            needsRectsRecalc = false;
        }

        for(int i=0; i<cells.length; ++i)
            cells[i].draw1(g2d, rects[i], i == mouseCellIndex);

        for(int i=0; i<cells.length; ++i)
            cells[i].draw2(g2d, rects[i], i == mouseCellIndex);

        g2d.dispose();
    }

    public void setCell(int row, int col, GridPanelCell gpc) {
        setCell(row * cols + col, gpc);
    }
    public void setCell(int node, GridPanelCell gpc) {
        cells[node] = gpc.getCopy();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });
    }

    public GridPanelCell getCell(int row, int col) {
        return cells[row * cols + col];
    }
    public GridPanelCell getCell(int node) {
        return cells[node];
    }

    public void handleClick(int row, int col){}
}
