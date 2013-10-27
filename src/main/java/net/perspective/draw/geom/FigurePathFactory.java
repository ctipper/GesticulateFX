/*
 * ConcretePathFactory.java
 * 
 * Created on Oct 19, 2013 6:07:12 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import net.perspective.draw.util.CanvasPoint;
import org.jhotdraw.geom.Bezier;

/**
 *
 * @author ctipper
 */

public class ConcretePathFactory implements PathFactory {
	
	GeneralPath path;

	public ConcretePathFactory() {
		
	}
	
	public GeneralPath createPath(Figure fig) {
		CanvasPoint p0, p1, p2, p3;
		double x, y, w, h;
		CanvasPoint[] cPoints;
		java.util.List<CanvasPoint> points = fig.getPoints();
		path = new GeneralPath();
		FigureType type = fig.getType();
		switch (type) {
		case LINE:
			if (points.size() > 1) {
				path.moveTo(points.get(0).getX(), points.get(0).getY());
				path.lineTo(points.get(1).getX(), points.get(1).getY());
				return path;
			} else {
				return null;
			}
		case CIRCLE:
			if (points.size() > 3) {
				x = y = w = h = 0;
				p0 = points.get(0); p1 = points.get(1);
				p2 = points.get(2); p3 = points.get(3);
				// handles cases where clockwise/anti-clockwise points
				if ((p0.getX() < p2.getX()) && (p0.getY() < p2.getY())) {
					x = p0.getX();
					y = p0.getY();
					w = (p2.getX() - x);
					h = (p2.getY() - y);
				} else if ((p0.getX() > p2.getX()) && (p0.getY() > p2.getY())) {
					x = p2.getX();
					y = p2.getY();
					w = (p0.getX() - x);
					h = (p0.getY() - y);
				} else if ((p3.getX() > p1.getX()) && (p3.getY() > p1.getY())) {
					x = p1.getX();
					y = p1.getY();
					w = (p3.getX() - x);
					h = (p3.getY() - y);
				} else if ((p3.getX() < p1.getX()) && (p3.getY() < p1.getY())) {
					x = p3.getX();
					y = p3.getY();
					w = (p1.getX() - x);
					h = (p1.getY() - y);
				}
				path = new GeneralPath(new Ellipse2D.Double(x, y, w, h));
				return path;
			} else {
				return null;
			}
		case SQUARE:
			if (points.size() > 3) {
				path.moveTo(points.get(0).getX(), points.get(0).getY());
				path.lineTo(points.get(1).getX(), points.get(1).getY());
				path.lineTo(points.get(2).getX(), points.get(2).getY());
				path.lineTo(points.get(3).getX(), points.get(3).getY());
				path.closePath();
				return path;
			} else {
				return null;
			}
		case TRIANGLE:
			if (points.size() > 2) {
				path.moveTo(points.get(0).getX(), points.get(0).getY());
				path.lineTo(points.get(1).getX(), points.get(1).getY());
				path.lineTo(points.get(2).getX(), points.get(2).getY());
				path.closePath();
				return path;
			} else {
				return null;
			}
		case SKETCH:
			cPoints = new CanvasPoint[points.size()];
			points.toArray(cPoints);
			path = Bezier.fitBezierPath(cPoints, 0.75).toGeneralPath();
			return path;
		case POLYGON:
			cPoints = new CanvasPoint[points.size()];
			points.toArray(cPoints);
			path = Bezier.fitBezierPath(cPoints, 0.75).toGeneralPath();
			path.closePath();
			return path;
		default:
			return null;
		}
	}
}
