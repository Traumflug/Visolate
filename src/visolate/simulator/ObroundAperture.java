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
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;

public class ObroundAperture extends StandardAperture {

  private static final String cvsid =
  "$Id: ObroundAperture.java,v 1.2 2004/06/30 17:26:29 vona Exp $";


  public ObroundAperture(int number,
                         double diameterX,
                         double diameterY,
                         double holeX,
                         double holeY) {
    super(number, holeX, holeY);
    init(diameterX, diameterY);
  }

  private void init(double diameterX, double diameterY) {

    this.diameterX = diameterX;
    this.diameterY = diameterY;
  }

  public double getA() {
    return Math.max(0.0, 0.5*diameterX + signedOffset);
  }

  public double getB() {
    return Math.max(0.0, 0.5*diameterY + signedOffset);
  }

  public ObroundAperture(int number,
                         double diameterX,
                         double diameterY,
                         double hole) {
    super(number, hole);
    init(diameterX, diameterY);
  }

  public ObroundAperture(int number, double diameterX, double diameterY) {
    super(number);
    init(diameterX, diameterY);
  }

  public String toString() {
    return
      "Aperture " + number +
      ": obround" +
      " diameterX = " + diameterX +
      " diameterY = " + diameterY +
      " holeX = " + holeX +
      " holeY = " + holeY;
  }

  protected void makeBounds() {
    double a = getA();
    double b = getB();
    bounds = new Rect(-a, -b, 2*a, 2*b);
  }

  public double getWidth(double direction) {
    //TBD this is for an ellipse...
    return 2*radius(direction+Math.PI/2);
  }

  private double radius(double theta) {
    double a = getA();
    double b = getB();
    double p = b*Math.cos(theta);
    double q = a*Math.sin(theta);
    return a*b/Math.sqrt(p*p+q*q);
  }

  protected void makeGeometries() {
    
    geometries = new LinkedList<GeometryArray>();
   
    double a = getA();
    double b = getB();

    int segments = CircleAperture.SEGMENTS/2;

    double segment = Math.PI/segments;
    
    float[] coords = new float[2*3*(segments + 1)];
    
    double rad = Math.min(a, b);
    double length = Math.max(2*a, 2*b)-2*rad;
    
    boolean flip = b > a;
    
    double xc = length/2;
    
    double x, y;
    
    int i = 0;
    
    double angle = -Math.PI/2;
    
    for (int k = 0; k < 2; k++) {
      for (int j = 0; j <= segments; j++) {
        
        x = rad*Math.cos(angle);
        y = rad*Math.sin(angle);
        
        if (!flip) {
          coords[i++] = (float) (x+xc);
          coords[i++] = (float) y;
        } else {
          coords[i++] = (float) y;
          coords[i++] = (float) (x+xc);
        }
        coords[i++] = 0.0f;
        
        angle += segment;
      }

      xc = -length/2;
      angle = Math.PI/2;
    }

    GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    gi.setCoordinates(coords);
    gi.setStripCounts(new int[] {2*(segments + 1)});
    
    geometries.add(gi.getGeometryArray(true, false, false));
  }

  private double diameterX;
  private double diameterY;
}
