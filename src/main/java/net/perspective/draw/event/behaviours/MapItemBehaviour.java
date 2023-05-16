/**
 * MapItemBehaviour.java
 * 
 * Created on 10 May 2022 16:52:32
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.MapController;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class MapItemBehaviour implements ItemBehaviours {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;
    @Inject private MapController mapper;

    /**
     * Creates a new instance of <code>MapItemBehaviour</code> 
     */
    @Inject
    public MapItemBehaviour() {
    }

    @Override
    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        boolean found = false;
        if (context.getRegion(item.getTop()[0]).
            contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.TL);
            found = true;
        } else if (context.getRegion(item.getDown()[0]).
            contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.BL);
            found = true;
        } else if (context.getRegion(item.getBottom()[0]).
            contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.BR);
            found = true;
        } else if (context.getRegion(item.getUp()[0]).
            contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.TR);
            found = true;
        } else if (item.contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.SHAPE);
            found = true;
        }
        return found;
    }

    @Override
    public void editItem(BehaviourContext context, DrawItem item, int index) {
        view.setSelected(index);
        mapper.initMap();
        drawarea.changeHandlers(HandlerType.MAP);
        view.setEditing(KeyHandlerType.MAP);
    }

    @Override
    public void hoverItem(BehaviourContext context, DrawItem item) {
        
    }

    @Override
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        ContainsType contains;
        double diffsx, diffsy, diffex, diffey;

        CanvasPoint st = item.getStart();
        CanvasPoint e = item.getEnd();
        CanvasPoint en = new CanvasPoint(st.x + e.x, st.y + e.y);
        contains = context.getContainment();
        /**
         * Ternary operators ensure minimum size for StreetMap
         */
        double min_w = 276, min_h = 180;        // mimimum width and height of viewport
        diffsx = en.x - st.x - xinc < min_w ? 0 : xinc;   // diff relative to TL corner
        diffsy = en.y - st.y - yinc < min_h ? 0 : yinc;   // diff relative to TL corner
        diffex = en.x - st.x + xinc < min_w ? 0 : xinc;   // reversed sign for BR corner
        diffey = en.y - st.y + yinc < min_h ? 0 : yinc;   // reversed sign for BR corner
        switch (contains) {
            case TL:
                st.translate(diffsx, diffsy);
                item.setStart(st.x, st.y);
                item.setEnd(en.x - st.x, en.y - st.y);
                mapper.resizeMap((StreetMap) item);
                break;
            case BL:
                st.translate(diffsx, 0);
                en.translate(0, diffey);
                item.setStart(st.x, st.y);
                item.setEnd(en.x - st.x, en.y - st.y);
                mapper.resizeMap((StreetMap) item);
                break;
            case BR:
                en.translate(diffex, diffey);
                item.setStart(st.x, st.y);
                item.setEnd(en.x - st.x, en.y - st.y);
                mapper.resizeMap((StreetMap) item);
                break;
            case TR:
                st.translate(0, diffsy);
                en.translate(diffex, 0);
                item.setStart(st.x, st.y);
                item.setEnd(en.x - st.x, en.y - st.y);
                mapper.resizeMap((StreetMap) item);
                break;
            case SHAPE:
                if (listener.isSnapEnabled()) {
                    xinc = context.getOmega().getX() - item.getStart().getX();
                    yinc = context.getOmega().getY() - item.getStart().getY();
                    drawarea.moveToWithIncrements(item, xinc, yinc);
                } else {
                    item.moveTo(xinc, yinc);
                }
                break;
            case NONE:
            default:
                break;
        }
    }
}
