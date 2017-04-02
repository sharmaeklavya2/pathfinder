/* Modified version of the grid-drawing code at http://stackoverflow.com/a/15422801 */

package gridpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import util.GUIUtil;
import gridpanel.GridPanelCell;
import gridpanel.GridPanel;

public class Main
{
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
                                    setCell(r, c, new GridPanelCell(Color.BLACK, 0, 0, 0.5, ""));
                                else
                                    setCell(r, c, new GridPanelCell(Color.LIGHT_GRAY, -dcols[j], -drows[i], 0.0, ""));
                            }
                        }
                    }
                }
            }
        };
        GUIUtil.makeGUI("Testing GridPanel", gridPanel, null, null);
    }

    public static void main(String[] args)
    {
        String usage = "Usage: java gridpanel.Main <rows> <columns>";
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
