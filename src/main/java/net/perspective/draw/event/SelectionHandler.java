/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */
package net.perspective.draw.event;

import java.util.List;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */

public class SelectionHandler implements Handler {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;

    public void upEvent() {
    }

    public void downEvent() {
        view.setSelected(-1);
        List<Figure> drawings = view.getDrawings();
        for (Figure figure : drawings) {
            if (figure.contains(drawarea.getStartX(), drawarea.getStartY())) {
                view.setSelected(drawings.indexOf(figure));
            }
        }
    }

    public void dragEvent() {
        int selection = view.getSelected();
        if (selection != -1) {
            double xinc = drawarea.getTempX() - drawarea.getStartX();
            double yinc = drawarea.getTempY() - drawarea.getStartY();
            view.getDrawings().get(selection).moveFigure(xinc, yinc);
            drawarea.setStartX(drawarea.getTempX());
            drawarea.setStartY(drawarea.getTempY());
        }
    }
}
