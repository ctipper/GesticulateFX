/**
 * RotateBehaviour.java
 * 
 * Created on 27 Jul 2026 14:34:26
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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
package net.perspective.draw.event.behaviours;

import javax.inject.Inject;
import javafx.scene.Cursor;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
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

public class RotateBehaviour  implements ItemBehaviours {

    private final DrawingArea drawarea;
    private final CanvasView view;
    @Inject DrawAreaListener listener;
    private double omega;

    @Inject
    public RotateBehaviour(DrawingArea drawarea, CanvasView view) {
        this.drawarea = drawarea;
        this.view = view;
    }

    @Override
    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        boolean found = false;
        if (item.getRotateBounds().contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.SHAPE);
            found = true;
        }
        if (view.getSelected() != -1) {
            omega = view.getDrawings().get(view.getSelected()).getAngle();
        }
        return found;
    }

    @Override
    public void editItem(BehaviourContext context, DrawItem item, int index) {
        
    }

    @Override
    public void hoverItem(BehaviourContext context, DrawItem item) {
        // only claim the cursor over the handle, the item behaviour has set it otherwise
        if (item.getRotateBounds().contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().setCursor(Cursor.OPEN_HAND);
        }
    }

    @Override
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        CanvasPoint A, B;

        if (view.getSelected() != -1) {
            int selection = view.getSelected();
            drawarea.getScene().setCursor(Cursor.CLOSED_HAND);
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
            double h2 = V2.L2(B);

            if (h1 == 0d || h2 == 0d) {
                // a pointer on the rotation centre has no direction, a NaN angle would stick to the item
                if (h2 != 0d) {
                    // the reference point was degenerate, adopt the current one instead
                    listener.setStartX(listener.getTempX());
                    listener.setStartY(listener.getTempY());
                }
                return;
            }

            CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
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

}
