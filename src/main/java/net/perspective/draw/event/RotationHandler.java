/*
 * RotationHandler.java
 * 
 * Created on Oct 19, 2013 8:28:22 PM
 * 
 */
package net.perspective.draw.event;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.geom.*;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 * 
 * @author ctipper
 */

public class RotationHandler implements Handler {

    @Inject
    private DrawingArea drawarea;
    @Inject
    private CanvasView view;
    @Inject
    private DrawAreaListener listener;
    private CanvasPoint st, en;

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
                if ((item instanceof Figure) && !(((Figure) item).getType().equals(FigureType.LINE))
                    || (item instanceof Grouped)) {
                    if (getRegion(item.getTop()[0]).contains(listener.getStartX(), listener.getStartY())) {
                        view.setSelected(i);
                        break;
                    } else if (getRegion(item.getDown()[0]).contains(listener.getStartX(), listener.getStartY())) {
                        view.setSelected(i);
                        break;
                    } else if (getRegion(item.getBottom()[0]).contains(listener.getStartX(), listener.getStartY())) {
                        view.setSelected(i);
                        break;
                    } else if (getRegion(item.getUp()[0]).contains(listener.getStartX(), listener.getStartY())) {
                        view.setSelected(i);
                        break;
                    } else if (item.contains(listener.getStartX(), listener.getStartY())) {
                        view.setSelected(i);
                        break;
                    }
                }
                if ((item instanceof Figure) && ((Figure) item).getType().equals(FigureType.LINE)) {
                    if (item.contains(listener.getStartX(), listener.getStartY())) {
                        st = ((Figure) item).getStart();
                        en = ((Figure) item).getEnd();
                        view.setSelected(i);
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

            if ((item instanceof Figure)
                    || (item instanceof Grouped)) {
                A = new CanvasPoint(listener.getStartX() - centre.x, listener.getStartY() - centre.y);
                B = new CanvasPoint(listener.getTempX() - centre.x, listener.getTempY() - centre.y);
            }

            double h1 = V2.L2(A);
            CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
            double h2 = V2.L2(B);
            CanvasPoint q2 = new CanvasPoint(B.x / h2, B.y / h2);

            double cos_t = V2.dot(q1, q2);
            double sin_t = V2.dot(V2.rot90(q1), q2);

            double theta = Math.atan2(sin_t, cos_t);

            if (((item instanceof Figure) && (!((Figure) item).getType().equals(FigureType.LINE)))
                    || (item instanceof Grouped)) {
                item.setAngle(angle + theta);
                // normalise angle
                angle = item.getAngle();
                if (angle > Math.PI) {
                    item.setAngle(angle - 2 * Math.PI);
                }
                if (angle < -Math.PI) {
                    item.setAngle(angle + 2 * Math.PI);
                }
            }
            /**
             * manipulate lines directly
             */
            if ((item instanceof Figure) && (((Figure) item).getType().equals(FigureType.LINE))) {
                CanvasPoint s = this.rotate((Figure) item, st, theta);
                CanvasPoint e = this.rotate((Figure) item, en, theta);
                item.setStart(s.x, s.y);
                item.setEnd(e.x, e.y);
                ((Figure) item).setPoints(DrawingType.LINE);
                ((Figure) item).setPath();
            }

            item.updateProperties(drawarea);
            view.updateCanvasItem(selection, item);
            view.moveSelection(selection);
            listener.setStartX(listener.getTempX());
            listener.setStartY(listener.getTempY());
        }
    }

    /**
     * Get an area centred on the specified point
     * 
     * @param p a {@link net.perspective.draw.util.CanvasPoint}
     * @return area
     */
    protected Area getRegion(CanvasPoint p) {
        Rectangle2D rect;
        rect = new Rectangle2D.Double(p.x - 10.0, p.y - 10.0, 20.0, 20.0);
        return new Area(rect);
    }

    /**
     * Rotate a point around figure axis by an angle
     * 
     * @param figure
     * @param p
     * @param angle
     * @return 
     */
    protected CanvasPoint rotate(Figure figure, CanvasPoint p, double angle) {
        CanvasPoint centre = figure.rotationCentre();
        CanvasPoint point = new CanvasPoint(p.x, p.y);
        point.translate(-centre.x, -centre.y);
        if (angle != 0) {
            // rotate point about centroid
            point.rotate(angle);
        }
        point.translate(centre.x, centre.y);
        return point;
    }

}
