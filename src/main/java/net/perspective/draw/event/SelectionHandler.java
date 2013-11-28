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
            if (figure.contains(canvas.getStartX(), canvas.getStartY())) {
                view.setSelected(drawings.indexOf(figure));
            }
        }
    }

    public void dragEvent() {
        int selection = view.getSelected();
        if (selection != -1) {
            double xinc = canvas.getTempX() - canvas.getStartX();
            double yinc = canvas.getTempY() - canvas.getStartY();
            view.getDrawings().get(selection).moveFigure(xinc, yinc);
            canvas.setStartX(canvas.getTempX());
            canvas.setStartY(canvas.getTempY());
        }
    }
}
