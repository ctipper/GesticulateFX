/*
 * RotationHandler.java
 * 
 * Created on Oct 19, 2013 8:28:22 PM
 * 
 */
package net.perspective.draw.event;

import java.util.List;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingCanvas;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 *
 * @author ctipper
 */

public class RotationHandler implements Handler {

    private final DrawingCanvas c;
    private final CanvasView view;

    public RotationHandler(DrawingCanvas c) {
        this.c = c;
        view = c.getView();
    }

    public void upEvent() {
    }

    public void downEvent() {
        view.setSelected(-1);
        List<Figure> drawings = view.getDrawings();
        for (Figure figure : drawings) {
            if (!(figure.getType().equals(FigureType.LINE))) {
                if (figure.contains(c.getStartX(), c.getStartY())) {
                    view.setSelected(drawings.indexOf(figure));
                }
            }
        }
    }

    public void dragEvent() {
        CanvasPoint A, B;

        int selection = view.getSelected();
        if (selection != -1) {
            Figure item = view.getDrawings().get(selection);
            double angle = item.getAngle();
            CanvasPoint centre = item.rotationCentre();
            A = B = new CanvasPoint(1, 1);

            FigureType type = item.getType();
            if (!type.equals(FigureType.LINE)) {
                A = new CanvasPoint(c.getStartX() - centre.x, c.getStartY() - centre.y);
                B = new CanvasPoint(c.getTempX() - centre.x, c.getTempY() - centre.y);
            }

            double h1 = V2.L2(A);
            CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
            double h2 = V2.L2(B);
            CanvasPoint q2 = new CanvasPoint(B.x / h2, B.y / h2);

            double cos_t = V2.dot(q1, q2);
            double sin_t = V2.dot(V2.rot90(q1), q2);

            double theta = Math.atan2(sin_t, cos_t);
            item.setAngle(angle + theta);

            angle = item.getAngle();
            if (angle > Math.PI) {
                item.setAngle(angle - 2 * Math.PI);
            }
            if (angle < -Math.PI) {
                item.setAngle(angle + 2 * Math.PI);
            }

            c.setStartX(c.getTempX());
            c.setStartY(c.getTempY());
        }
    }
}
