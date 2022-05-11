/*
 * GroupedItemBehaviour.java
 * 
 * Created on Oct 13, 2013 1:37:12 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import javafx.scene.Cursor;
import javax.inject.Inject;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.geom.DrawItem;

/**
 * 
 * @author ctipper
 */

public class GroupedItemBehaviour implements ItemBehaviours {

    @Inject private DrawingArea drawarea;
    @Inject private DrawAreaListener listener;

    @Override
    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        // Not implemented
        return false;
    }

    @Override
    public void editItem(BehaviourContext context, DrawItem item, int index) {
        // not implemented
    }

    @Override
    public void hoverItem(BehaviourContext context, DrawItem item) {
        if (item.contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().setCursor(Cursor.OPEN_HAND);
        } else {
            drawarea.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        drawarea.getScene().getRoot().setCursor(Cursor.CLOSED_HAND);
        if (listener.isSnapEnabled()) {
            xinc = context.getOmega().getX() - item.getStart().getX();
            yinc = context.getOmega().getY() - item.getStart().getY();
            drawarea.moveToWithIncrements(item, xinc, yinc);
        } else {
            item.moveTo(xinc, yinc);
        }
    }

}
