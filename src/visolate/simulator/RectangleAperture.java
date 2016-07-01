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

public class RectangleAperture extends StandardAperture {

  public RectangleAperture(int number,
                           double dimensionX,
                           double dimensionY,
                           double holeX,
                           double holeY) {
    super(number, holeX, holeY);
    init(dimensionX, dimensionY);
  }

  private void init(double dimensionX, double dimensionY) {
    this.dimensionX = dimensionX;
    this.dimensionY = dimensionY;
  }

  private void getPerimeter() {

    if (x == null) {

      x = new float[4];
      y = new float[4];
      angle = new double[4];

      h = (float) Math.max(0.0, (dimensionX/2.0 + signedOffset));
      v = (float) Math.max(0.0, (dimensionY/2.0 + signedOffset));

      x[0] = h;
      y[0] = v;

      x[1] = -h;
      y[1] = v;

      x[2] = -h;
      y[2] = -v;

      x[3] = h;
      y[3] = -v;

      for (int i = 0; i < angle.length; i++)
        angle[i] = Util.canonicalAngle(x[i], y[i]);
    }
  }

  public RectangleAperture(int number,
                           double dimensionX,
                           double dimensionY,
                           double hole) {
    super(number, hole);
    init(dimensionX, dimensionY);
  }

  public RectangleAperture(int number, double dimensionX, double dimensionY) {
    super(number);
    init(dimensionX, dimensionY);
  }

  public String toString() {
    return
      "Aperture " + number +
      ": rectangle" +
      " dimensionX = " + dimensionX +
      " dimensionY = " + dimensionY +
      " holeX = " + holeX +
      " holeY = " + holeY;
  }

  public double getWidth(double direction) {
    getPerimeter();
    return Util.getPolyWidth(x, y, angle, direction + Math.PI/2);
  }

  protected void makeBounds() {
    getPerimeter();
    bounds = new Rect(-h, -v, 2*h, 2*v);
  }

  protected void makeGeometries() {

    getPerimeter();

    geometries = new LinkedList<GeometryArray>();

    float[] coords = new float[3*6];

    int i = 0;

    coords[i++] = 0.0f;
    coords[i++] = 0.0f;
    coords[i++] = 0.0f;

    for (int j = 0; j < 4; j++) {
      coords[i++] = x[j];
      coords[i++] = y[j];
      coords[i++] = 0.0f;
    }

    coords[i++] = x[0];
    coords[i++] = y[0];
    coords[i++] = 0.0f;

    geometries.add(makeTFA(coords));
  }

  protected void offsetChanged() {
    super.offsetChanged();
    x = y = null;
    h = v = Float.NaN;
    angle = null;
  }

  protected void inverseChanged() {
    super.inverseChanged();
    x = y = null;
    h = v = Float.NaN;
    angle = null;
  }

  private float h = Float.NaN;
  private float v = Float.NaN;

  private float[] x = null;
  private float[] y = null;
  private double[] angle = null;

  private double dimensionX;
  private double dimensionY;
}
