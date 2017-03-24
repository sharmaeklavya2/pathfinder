/* This is a modified version of the grid-drawing code found at http://stackoverflow.com/a/15422801 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.Timer;

import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalArgumentException;

class GridPanelCell
{
    protected Color color = Color.white;
    public Color getColor() {
        return color;
    }

    public GridPanelCell(Color c) {
        color = c;
    }
    public GridPanelCell(GridPanelCell gpc) {
        this(gpc.color);
    }
    public GridPanelCell() {
        this(Color.WHITE);
    }

    public GridPanelCell clone() {
        return this;
    }
}

class MutableGridPanelCell extends GridPanelCell
{
    public void setColor(Color c) {
        color = c;
    }
    public GridPanelCell clone() {
        return new GridPanelCell(this);
    }
}

public class GridPanel extends JPanel
{
    private int cols, rows;
    private int pref_cell_size;
    private Rectangle[] rects;
    private GridPanelCell[] cells;
    private int mouseCellIndex;
    private boolean needsRepaint, needsRectsRecalc;
    public static final int timerDelay = 50;

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

        Timer timer = new Timer(timerDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(needsRepaint) {
                    repaint();
                    needsRepaint = false;
                }
            }
        });
        timer.start();

        MouseAdapter mouseHandler;
        mouseHandler = new MouseAdapter() {
            int row, col;

            public void getCoords(MouseEvent e) {
                Point point = e.getPoint();

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
        {
            g2d.setColor(cells[i].getColor());
            g2d.fill(rects[i]);
        }

        if (mouseCellIndex != -1) {
            Rectangle rect = rects[mouseCellIndex];
            Color c = cells[mouseCellIndex].getColor(), c2;
            if(c == Color.BLACK)
                g2d.setColor(Color.DARK_GRAY);
            else
                g2d.setColor(c.darker());
            g2d.fill(rect);
        }

        g2d.setColor(Color.GRAY);
        for (Rectangle rect: rects) {
            g2d.draw(rect);
        }

        needsRepaint = false;
        g2d.dispose();
    }

    public void setCell(int row, int col, GridPanelCell gpc)
    {
        cells[row * cols + col] = gpc.clone();
        needsRepaint = true;
    }

    public GridPanelCell getCell(int row, int col) {
        return cells[row * cols + col];
    }

    public void handleClick(int row, int col){}

    public static void main2(int rows, int cols)
    {
        GridPanel gridPanel = new GridPanel(rows, cols) {
            @Override
            public void handleClick(int row, int col) {
                int[] drows = {-1, 0, 1};
                int[] dcols = drows;
                for(int i=0; i < drows.length; ++i) {
                    for(int j=0; j < dcols.length; ++j) {
                        int r = row + drows[i];
                        int c = col + dcols[j];
                        if(r >= 0 && c >= 0 && r < rows && c < cols) {
                            GridPanelCell gpc = getCell(r, c);
                            if(gpc.color != Color.BLACK) {
                                if(r == row && c == col)
                                    setCell(r, c, new GridPanelCell(Color.BLACK));
                                else
                                    setCell(r, c, new GridPanelCell(Color.LIGHT_GRAY));
                            }
                        }
                    }
                }
            }
        };
        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException |
                    IllegalAccessException | UnsupportedLookAndFeelException ex) {}

                JFrame frame = new JFrame("Testing GridPanel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(gridPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
        /*
        long sum = 0;
        for(long i=0; i<12345678901l; ++i)
            sum += i;
        System.out.println(sum);
        gridPanel.setCell(0, 0, new GridPanelCell(Color.GREEN));
        */
    }

    public static void main(String[] args)
    {
        String usage = "Usage: java GridPanel <rows> <columns>";
        String[] names = {"rows", "columns"};
        int params[] = new int[2];

        if(args.length == 0) {
            main2(6, 12);
        }
        else if(args.length == 2) {
            for(int i=0; i<2; ++i) {
                try {
                    params[i] = Integer.parseInt(args[i]);
                    if(params[i] <= 0) {
                        System.err.println(usage);
                        System.err.println(names[i] + " should be positive");
                        System.exit(1);
                    }
                } catch(NumberFormatException e) {
                    System.err.println(names[i] + " should be an integer.");
                    System.exit(1);
                }
            }
            main2(params[0], params[1]);
        }
        else {
            System.err.println(usage);
            System.exit(1);
        }
    }
}
