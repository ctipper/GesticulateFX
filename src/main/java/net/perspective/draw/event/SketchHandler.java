/*
 * SketchHandler.java
 * 
 * Created on Oct 27, 2013 10:22:09 PM
 * 
 */
package net.perspective.draw.event;

import javafx.scene.paint.Color;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.ArrowLine;
import net.perspective.draw.geom.ArrowType;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureFactoryImpl;
import net.perspective.draw.geom.FigureType;

/**
 * 
 * @author ctipper
 */

public class SketchHandler implements Handler  {

    @Inject
    private DrawingArea drawarea;
    @Inject
    private CanvasView view;
    @Inject
    private DrawAreaListener listener;
    private final FigureFactory figurefactory;

    public SketchHandler() {
        this.figurefactory = new FigureFactoryImpl();
    }

    public void upEvent() {
        view.setDrawing(false);
        if (view.getNewItem() == null || drawarea.getDrawType() == null) {
            return;
        }
        // add figure to canvas
        Figure item = (Figure) view.getNewItem();
        item.setEndPoints();
        item.updateProperties(drawarea);
        view.setNewItem(item);
        view.resetNewItem();
    }

    public void downEvent() {
        // Create figure
        Figure item = figurefactory.createFigure(drawarea.getDrawType());
        // Initialise stroke and fill
        item.setStroke(drawarea.getPlainStroke());
        item.setColor(Color.web(drawarea.getThemeFillColor()));
        item.setFillColor(Color.web(drawarea.getThemeBackgroundColor()));
        // Initialise sketch
        item.setPoints(drawarea.getDrawType());
        item.addPoint(listener.getStartX(), listener.getStartY());
        item.setEndPoints();
    	item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }

    public void dragEvent() {
        // Create Figure
        Figure item = (Figure) view.getNewItem();
        if (!(item instanceof ArrowLine) && (drawarea.getArrow().equals(ArrowType.END) || drawarea.getArrow().equals(ArrowType.BOTH))) {
            // Create arrow line
            if (!((Figure) item).getType().equals(FigureType.POLYGON)) {
                ArrowLine arrow = new ArrowLine((Figure) item);
                arrow.setArrowStroke(drawarea.getStroke());
                arrow.setArrowType(drawarea.getArrow());
                arrow.setEndPoints();
                item = arrow;
            }
        }        // continue sketch
        item.addPoint(listener.getTempX(), listener.getTempY());
        item.setEndPoints();
        item.setPath();
        view.setNewItem(item);
    }

}
