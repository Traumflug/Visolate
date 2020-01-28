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
import javax.media.j3d.GeometryArray;
import visolate.misc.*;

public class CircleAperture extends StandardAperture {

  public static final int SEGMENTS = 16;
  public static final double SECTOR = 2.0 * Math.PI / SEGMENTS;

  public CircleAperture(int number, double diameter, double holeX, double holeY) {
    super(number, holeX, holeY);

    this.diameter = diameter;
  }

  public CircleAperture(int number, double diameter, double hole) {
    super(number, hole);

    this.diameter = diameter;
  }

  public CircleAperture(int number, double diameter) {
    super(number);

    this.diameter = diameter;
  }

  public String toString() {
    return "Aperture " + number + ": circle" + " diameter = " + diameter + " holeX = " + holeX + " holeY = " + holeY;
  }

  public double getWidth(double direction) {
    return 2 * getRadius();
  }

  protected void makeBounds() {
    double r = getRadius();
    bounds = new Rect(-r, -r, 2 * r, 2 * r);
  }

  protected void makeGeometries() {
    geometries = makeCircleGeometries(0.0, 0.0, getRadius());
  }

  protected double getRadius() {
    return Math.max(0.0, diameter / 2 + signedOffset);
  }

  public static Collection<GeometryArray> makeCircleGeometries(double xCenter, double yCenter, double radius) {

    Collection<GeometryArray> geometries = new LinkedList<GeometryArray>();

    float[] coords = new float[3 * (SEGMENTS + 2)];

    int i = 0;

    coords[i++] = (float) xCenter;
    coords[i++] = (float) yCenter;
    coords[i++] = 0.0f;

    double x, y;
    double angle = 0.0;

    for (int j = 0; j <= SEGMENTS; j++) {

      x = radius * Math.cos(angle);
      y = radius * Math.sin(angle);

      coords[i++] = (float) (x + xCenter);
      coords[i++] = (float) (y + yCenter);
      coords[i++] = 0.0f;

      angle += SECTOR;
    }

    geometries.add(makeTFA(coords));

    return geometries;
  }

  private double diameter;
}
