/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */
package net.perspective.draw.event;

import java.util.List;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */

public class SelectionHandler extends Handler {

    private final CanvasView view;

    public SelectionHandler(DrawingArea c) {
        super(c);
        view = c.getView();
    }

    public void upEvent() {
    }

    public void downEvent() {
        view.setSelected(-1);
        List<Figure> drawings = view.getDrawings();
        for (Figure figure : drawings) {
            if (figure.contains(c.getStartX(), c.getStartY())) {
                view.setSelected(drawings.indexOf(figure));
            }
        }
    }

    public void dragEvent() {
        int selection = view.getSelected();
        if (selection != -1) {
            double xinc = c.getTempX() - c.getStartX();
            double yinc = c.getTempY() - c.getStartY();
            view.getDrawings().get(selection).moveFigure(xinc, yinc);
            c.setStartX(c.getTempX());
            c.setStartY(c.getTempY());
        }
    }
}
