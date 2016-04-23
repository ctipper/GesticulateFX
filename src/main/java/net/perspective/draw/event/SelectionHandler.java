/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */
package net.perspective.draw.event;

import com.google.inject.Injector;
import java.util.List;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.FigureItemBehaviour;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */

public class SelectionHandler implements Handler {

    @Inject Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private BehaviourContext context;
    
    public void upEvent() {
        if ((view.getSelected() != -1)) {
            view.updateSelectedItem();
            context.resetContainment();
        }
    }

    public void downEvent() {
        List<DrawItem> drawings = view.getDrawings();
        if (!drawings.isEmpty()) {
            int i = drawings.size() - 1;
            context.setContainment(ContainsType.NONE);
            do {
                DrawItem item = drawings.get(i);
                if (item instanceof Figure) {
                    context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                    boolean found = context.select(item, i);
                    if (found) break;
                }
                i--;
            } while (i >= 0);
            if (context.getContainment().equals(ContainsType.NONE)) {
                view.setSelected(-1);
            }
        }
    }

    public void dragEvent() {
        int selection = view.getSelected();
        if (selection != -1) {
            double xinc = drawarea.getTempX() - drawarea.getStartX();
            double yinc = drawarea.getTempY() - drawarea.getStartY();
            DrawItem item = view.getDrawings().get(selection);
            if (item instanceof Figure) {
                context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                context.alter(item, xinc, yinc);
            }
            item.updateProperties(drawarea);
            view.updateCanvasItem(selection, item);
            view.moveSelection(selection);
            drawarea.setStartX(drawarea.getTempX());
            drawarea.setStartY(drawarea.getTempY());
        }
    }
}
