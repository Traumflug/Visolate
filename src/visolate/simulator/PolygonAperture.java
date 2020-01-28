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

public class PolygonAperture extends StandardAperture {

  public PolygonAperture(int number, double od, int sides, double rotation, double holeX, double holeY) {
    super(number, holeX, holeY);
    init(od, sides, rotation);
  }

  private void init(double od, int sides, double rotation) {
    this.od = od;
    this.sides = sides;
    this.rotation = rotation;
  }

  private void getPerimeter() {
    if (x == null) {
      x = new float[sides];
      y = new float[sides];
      angle = new double[sides];

      computeXYAngle(x, y, angle, getRadius(), sides, rotation);
    }
  }

  protected double getRadius() {
    return Math.max(0.0, od / 2 + Util.vertexOffset(signedOffset, Math.PI * 2.0 / ((double) sides)));
  }

  public static void computeXYAngle(float[] x, float[] y, double[] angle, double radius, int sides, double rotation) {

    rotation = rotation * (Math.PI / 180);

    double sector = Math.PI * 2.0 / ((double) sides);

    for (int i = 0; i < sides; i++) {

      double theta = Util.canonicalizeAngle(rotation + i * sector);

      if (angle != null)
        angle[i] = theta;

      x[i] = (float) (Math.cos(theta) * radius);
      y[i] = (float) (Math.sin(theta) * radius);
    }
  }

  public PolygonAperture(int number, double od, int sides, double rotation, double hole) {
    super(number, hole);
    init(od, sides, rotation);
  }

  public PolygonAperture(int number, double od, int sides, double rotation) {
    super(number);
    init(od, sides, rotation);
  }

  public PolygonAperture(int number, double od, int sides) {
    super(number);
    init(od, sides, 0.0);
  }

  public String toString() {
    return "Aperture " + number + ": polygon" + " od = " + od + " sides = " + sides + " rotation = " + rotation
        + " holeX = " + holeX + " holeY = " + holeY;
  }

  protected void makeBounds() {
    getPerimeter();
    bounds = new Rect(x, y);
  }

  public double getWidth(double direction) {
    getPerimeter();
    return Util.getPolyWidth(x, y, angle, direction + Math.PI / 2);
  }

  protected void makeGeometries() {
    getPerimeter();
    geometries = makeGeometriesFromXY(0.0, 0.0, x, y, sides, getRadius(), rotation);
  }

  public static Collection<GeometryArray> makeGeometriesFromXY(double xCenter, double yCenter, float[] x, float[] y,
      int sides, double radius, double rotation) {

    rotation = rotation * (Math.PI / 180);

    Collection<GeometryArray> geometries = new LinkedList<GeometryArray>();

    float[] coords = new float[3 * (sides + 2)];

    int i = 0;

    coords[i++] = (float) xCenter;
    coords[i++] = (float) yCenter;
    coords[i++] = 0.0f;

    for (int j = 0; j < sides; j++) {
      coords[i++] = (float) (x[j] + xCenter);
      coords[i++] = (float) (y[j] + yCenter);
      coords[i++] = 0.0f;
    }

    coords[i++] = (float) (x[0] + xCenter);
    coords[i++] = (float) (y[0] + yCenter);
    coords[i++] = 0.0f;

    geometries.add(makeTFA(coords));

    return geometries;
  }

  protected void offsetChanged() {
    super.offsetChanged();
    x = y = null;
    angle = null;
  }

  protected void inverseChanged() {
    super.inverseChanged();
    x = y = null;
    angle = null;
  }

  private double od;
  private int sides;
  private double rotation;

  private float[] x = null;
  private float[] y = null;
  private double[] angle = null;
}
