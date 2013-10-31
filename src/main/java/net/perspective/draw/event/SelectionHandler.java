/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */
package net.perspective.draw.event;

import java.util.List;
import net.perspective.draw.DocModel;
import net.perspective.draw.DrawingCanvas;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */

public class SelectionHandler extends HandlerAdapter {

    DocModel model;

    public SelectionHandler(DrawingCanvas c) {
        super(c);
        model = c.getView();
    }

    @Override
    public void upEvent() {
    }

    @Override
    public void downEvent() {
        model.setSelected(-1);
        List<Figure> drawings = model.getDrawings();
        for (Figure figure : drawings) {
            if (figure.contains(c.getStartX(), c.getStartY())) {
                model.setSelected(drawings.indexOf(figure));
            }
        }
    }

    @Override
    public void dragEvent() {
        int selection = model.getSelected();
        if (selection != -1) {
            double xinc = c.getTempX() - c.getStartX();
            double yinc = c.getTempY() - c.getStartY();
            model.getDrawings().get(selection).moveFigure(xinc, yinc);
            c.setStartX(c.getTempX());
            c.setStartY(c.getTempY());
        }
    }
}
