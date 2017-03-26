package gridpanel;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class GridPanelCell
{
    protected Color color;
    protected int arrowX, arrowY;

    public Color getColor() {
        return color;
    }
    public int getArrowX() {return arrowX;}
    public int getArrowY() {return arrowY;}

    public static Color complColor(Color c) {
        double brightness = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
        if(c == Color.BLACK)
            return Color.DARK_GRAY;
        else if(brightness <= 0.1)
            return c.brighter();
        else
            return c.darker();
    }

    public GridPanelCell(Color color, int arrowX, int arrowY) {
        this.color = color;
        this.arrowX = arrowX;
        this.arrowY = arrowY;
    }
    public GridPanelCell(GridPanelCell gpc) {
        this(gpc.getColor(), gpc.getArrowX(), gpc.getArrowY());
    }
    public GridPanelCell() {
        this(Color.WHITE, 0, 0);
    }

    public GridPanelCell getCopy() {
        return this;
    }

    public void draw1(Graphics2D g2d, Rectangle rect, boolean selected) {
        Color defColor = g2d.getColor();
        Stroke defStroke = g2d.getStroke();

        g2d.setColor(getColor());
        g2d.fill(rect);
        if(selected)
            g2d.setStroke(new BasicStroke(3));
        else
            g2d.setStroke(new BasicStroke(1));

        double x = rect.getX();
        double y = rect.getY();
        double w = rect.getWidth();
        double h = rect.getHeight();

        g2d.setColor(complColor(getColor()));
        //g2d.setColor(Color.BLUE);
        Line2D line = new Line2D.Double(x+w/2, y+h/2, x+w*(1+arrowX)/2, y+h*(1+arrowY)/2);
        g2d.draw(line);

        g2d.setColor(defColor);
        g2d.setStroke(defStroke);
    }

    public void draw2(Graphics2D g2d, Rectangle rect, boolean selected) {
        Color defColor = g2d.getColor();
        Stroke defStroke = g2d.getStroke();

        g2d.setColor(Color.GRAY);
        if(selected)
            g2d.setStroke(new BasicStroke(3));
        else
            g2d.setStroke(new BasicStroke(1));
        g2d.draw(rect);

        g2d.setColor(defColor);
        g2d.setStroke(defStroke);
    }
}
