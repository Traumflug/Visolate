/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package visolate.simulator;

import java.util.*;

import visolate.misc.*;

import javax.media.j3d.GeometryArray;
import javax.vecmath.*;

public class Segment extends Stroke {

  private static final String cvsid =
  "$Id: Segment.java,v 1.3 2004/08/05 02:57:53 vona Exp $";


  public static final Vector3d Z = new Vector3d(0.0, 0.0, 1.0);

  public static final double MIN_LENGTH = 0.00001;

  public Segment(Aperture aperture, Vertex start, Vertex end) {
    super(aperture, start, end);

    double dx = end.x - start.x;
    double dy = end.y - start.y;

    double length = Math.sqrt(dx*dx + dy*dy);

    double s = dy/length;
    double c = dx/length;

    forwardDirection = Math.atan2(s, c);

    if (forwardDirection < 0.0)
      forwardDirection = 2*Math.PI + forwardDirection;

    reverseDirection = Math.atan2(-s, -c);

    if (reverseDirection < 0.0)
      reverseDirection = 2*Math.PI + reverseDirection;

//    System.out.println(toString());
  }

  public double getLength() {

    if (lengthInches < 0.0) {

      lengthInches = 0.0;

      lengthInches =
        start.getInchCoordinates().distance(end.getInchCoordinates());
    }

    return lengthInches;
  }

  public double getStartForwardDirection() {
    return forwardDirection;
  }

  public double getStartReverseDirection() {
    return reverseDirection;
  }

  public double getEndForwardDirection() {
    return forwardDirection;
  }

  public double getEndReverseDirection() {
    return reverseDirection;
  }

  protected void makeBounds() {

    bounds = new Rect();
    
    Point2f s = start.getInchCoordinates();
    Point2f e = end.getInchCoordinates();
    
    Rect apBounds = aperture.getBounds();
    
    bounds.add(new Rect(apBounds.x + s.x,
                        apBounds.y + s.y,
                        apBounds.width,
                        apBounds.height));
    
    bounds.add(new Rect(apBounds.x + e.x,
                        apBounds.y + e.y,
                        apBounds.width,
                        apBounds.height));
    
    getBodyRect();

    //body rect is null if segment is 0 length
    if (bodyRect != null) {
      for (int i = 0; i < bodyRect.length; i++)
        bounds.add(bodyRect[i].x, bodyRect[i].y);
    }
  }
  
  protected Point2d[] getBodyRect() {

    if (bodyRect == null) {
      
      if (getLength() < MIN_LENGTH)
        return null;

      bodyRect = new Point2d[4];

      double width = aperture.getWidth(forwardDirection);

      Point2f s = start.getInchCoordinates();
      Point2f e = end.getInchCoordinates();

      Vector3d d = new Vector3d(e.x - s.x, e.y - s.y, 0.0);
      d.normalize();

      Vector3d n = new Vector3d();
      n.cross(d, Z);

      n.scale(width/2);

      bodyRect[0] = new Point2d(s.x + n.x, s.y + n.y);
      bodyRect[1] = new Point2d(e.x + n.x, e.y + n.y);
      bodyRect[2] = new Point2d(e.x - n.x, e.y - n.y);
      bodyRect[3] = new Point2d(s.x - n.x, s.y - n.y);
    }

    return bodyRect;
  }

  protected void makeGeometries() {

    geometries = new LinkedList<GeometryArray>();
    
    getBodyRect();
    
    //body rect is null if segment is 0 length
    if (bodyRect != null) {
      
      float[] coords = new float[3*4];
      
      for (int i = 0; i < 4; i++) {
        coords[3*i+0] = (float) bodyRect[i].x;
        coords[3*i+1] = (float) bodyRect[i].y;
        coords[3*i+2] = 0.0f;
      }
      
      geometries.add(makeTFA(coords));
      
      addXlatedAperture(geometries, aperture, start);
    }
    
    addXlatedAperture(geometries, aperture, end);
  }

  public String toString() {
    return "segment from " + start + " to " + end + "; " + aperture;
  }

  protected void offsetChanged() {
    super.offsetChanged();
    bodyRect = null;
  }
                                   
  protected void inverseChanged() {
    super.inverseChanged();
    bodyRect = null;
  }

  private double lengthInches = -1.0;
  private double forwardDirection;
  private double reverseDirection;

  private Point2d[] bodyRect = null;
}
