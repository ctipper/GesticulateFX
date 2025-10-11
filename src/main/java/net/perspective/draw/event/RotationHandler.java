/*
 * RotationHandler.java
 * 
 * Created on Oct 19, 2013 8:28:22 PM
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw.event;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Cursor;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.ArrowLine;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Edge;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.geom.Text;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 * 
 * @author ctipper
 */

public class RotationHandler implements Handler {

    private final DrawingArea drawarea;
    private final CanvasView view;
    @Inject DrawAreaListener listener;
    private double omega;
    private final Map<CanvasPoint, Area> regionCache = new HashMap<>();

    /**
     * Creates a new instance of <code>RotationHandler</code> 
     */
    @Inject
    public RotationHandler(DrawingArea drawarea, CanvasView view) {
        this.drawarea = drawarea;
        this.view = view;
    }

    @Override
    public void upEvent() {
        if (view.getSelected() != -1) {
            view.updateSelectedItem();
        }
    }

    @Override
    public void downEvent() {
        view.setSelected(-1);
        List<DrawItem> drawings = view.getDrawings();
        if (!drawings.isEmpty()) {
            int i = drawings.size() - 1;
            do {
                DrawItem item = drawings.get(i);
                if ((item instanceof Figure) && !(((Figure) item).getType().equals(FigureType.LINE))
                        || (item instanceof Text)
                        || (item instanceof Grouped)
                        || ((item instanceof Picture) && !(item instanceof StreetMap))) {
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
                        view.setSelected(i);
                        break;
                    }
                }
                i--;
            } while (i >= 0);
        }
        if (view.getSelected() != -1) {
            omega = view.getDrawings().get(view.getSelected()).getAngle();
        }
    }

    @Override
    public void clickEvent() {
    }

    @Override
    public void hoverEvent() {
        if (view.getSelected() != -1) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            if (!(item instanceof Text) && !(item instanceof Edge) && !(item instanceof ArrowLine)) {
                if (getRegion(item.getTop()[0]).contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.CROSSHAIR);
                } else if (getRegion(item.getDown()[0]).contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.CROSSHAIR);
                } else if (getRegion(item.getBottom()[0]).contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.CROSSHAIR);
                } else if (getRegion(item.getUp()[0]).contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.CROSSHAIR);
                } else if (item.contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.OPEN_HAND);
                } else {
                    drawarea.getScene().setCursor(Cursor.DEFAULT);
                }
            } else if (item.contains(listener.getTempX(), listener.getTempY())) {
                drawarea.getScene().setCursor(Cursor.OPEN_HAND);
            } else {
                drawarea.getScene().setCursor(Cursor.DEFAULT);
            }
        } else {
            drawarea.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void dragEvent() {
        CanvasPoint A, B;

        if (view.getSelected() != -1) {
            int selection = view.getSelected();
            DrawItem item = view.getDrawings().get(selection);
            CanvasPoint centre = item.rotationCentre();
            A = B = new CanvasPoint(1, 1);

            if ((item instanceof Figure)
                    || (item instanceof Text)
                    || (item instanceof Grouped)
                    || ((item instanceof Picture) && !(item instanceof StreetMap))) {
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

            if (listener.isSnapEnabled()) {
                omega = omega + theta;
                theta = omega - item.getAngle();
                drawarea.rotateWithIncrements(item, theta);
            } else {
                drawarea.rotateTo(item, theta);
            }

            item.updateProperties(drawarea);
            view.updateCanvasItem(selection, item);
            view.moveSelection(selection);
            listener.setStartX(listener.getTempX());
            listener.setStartY(listener.getTempY());
        }
    }

    @Override
    public void zoomEvent() {

    }

    /**
     * Get an area centred on the specified point
     * 
     * @param p a {@link net.perspective.draw.util.CanvasPoint}
     * @return area
     */
    protected Area getRegion(CanvasPoint p) {
        return regionCache.computeIfAbsent(p, key -> {
            Rectangle2D rect = new Rectangle2D.Double(p.x - 10.0, p.y - 10.0, 20.0, 20.0);
            return new Area(rect);
        });
    }

}
