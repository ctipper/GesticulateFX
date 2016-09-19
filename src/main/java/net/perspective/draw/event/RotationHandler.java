/*
 * RotationHandler.java
 * 
 * Created on Oct 19, 2013 8:28:22 PM
 * 
 */
package net.perspective.draw.event;

import java.util.List;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 *
 * @author ctipper
 */

public class RotationHandler implements Handler {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;

    public void upEvent() {
        if (view.getSelected() != -1) {
            view.updateSelectedItem();
        }
    }

    public void downEvent() {
        view.setSelected(-1);
        List<DrawItem> drawings = view.getDrawings();
        if (!drawings.isEmpty()) {
            int i = drawings.size() - 1;
            do {
                DrawItem item = drawings.get(i);
                if (item instanceof Figure) {
                    if (!(((Figure) item).getType().equals(FigureType.LINE))) {
                        if (item.contains(listener.getStartX(), listener.getStartY())) {
                            view.setSelected(drawings.indexOf(item));
                        }
                    }
                }
                i--;
            } while (i >= 0);
        }
    }

    public void dragEvent() {
        CanvasPoint A, B;

        int selection = view.getSelected();
        if (selection != -1) {
            DrawItem item = view.getDrawings().get(selection);
            double angle = item.getAngle();
            CanvasPoint centre = item.rotationCentre();
            A = B = new CanvasPoint(1, 1);

            if (item instanceof Figure) {
                if (!((Figure) item).getType().equals(FigureType.LINE)) {
                    A = new CanvasPoint(listener.getStartX() - centre.x, listener.getStartY() - centre.y);
                    B = new CanvasPoint(listener.getTempX() - centre.x, listener.getTempY() - centre.y);
                }
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

            item.updateProperties(drawarea);
            view.updateCanvasItem(selection, item);
            view.moveSelection(selection);
            listener.setStartX(listener.getTempX());
            listener.setStartY(listener.getTempY());
        }
    }
}
