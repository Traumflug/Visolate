/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
 *               2012 Markus Hitter <mah@jump-ing.de>
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
import javax.vecmath.Point3f;

public class ObroundAperture extends StandardAperture {

  private static final String cvsid =
  "$Id: ObroundAperture.java,v 1.2 2004/06/30 17:26:29 vona Exp $";

  
  public static final int SEGMENTS = 16; // Must be a multiple of four.
  public static final double SECTOR = 2.0*Math.PI/SEGMENTS;

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
    // This is pretty similar to the counterpart of CircleAperture,
    // just stretched in the direction of the bigger radius.
    // Also, the center is fixed at 0.0, 0.0.

    geometries = new LinkedList<GeometryArray>();

    float[] coords = new float[3*(SEGMENTS + 4)];

    int i = 0;

    // center
    coords[i++] = 0.0f;
    coords[i++] = 0.0f;
    coords[i++] = 0.0f;

    double x, y;
    double angle = 0.0;
    double rx = getA();
    double ry = getB();
    double radius = Math.min(rx, ry);
    double halfLength = Math.abs(ry - rx);

    for (int j = 0; j <= SEGMENTS; j++) {

      x = radius*Math.cos(angle);
      y = radius*Math.sin(angle);

      // vertical stretch
      if (j <= SEGMENTS / 2) {
        coords[i++] = (float) (x);
        coords[i++] = (float) (y+halfLength);
        coords[i++] = 0.0f;
      }
      // Yes, j == SEGMENTS / 2 gives two points!
      if (j >= SEGMENTS / 2) {
        coords[i++] = (float) (x);
        coords[i++] = (float) (y-halfLength);
        coords[i++] = 0.0f;
      }
      // Close the oval, it's a double-point again.
      if (j == SEGMENTS) {
        coords[i++] = (float) (x);
        coords[i++] = (float) (y+halfLength);
        coords[i++] = 0.0f;
      }

      angle += SECTOR;
    }

    TriangleFanArray tfa = makeTFA(coords);
    if (rx > ry) {
      tfa = (TriangleFanArray) rotateGeometry(tfa, Math.PI / 2);
    }
    
    geometries.add(tfa);
  }

  private double diameterX;
  private double diameterY;
}
