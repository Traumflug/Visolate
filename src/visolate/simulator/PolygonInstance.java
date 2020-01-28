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

import visolate.misc.*;

public class PolygonInstance extends PrimitiveInstance {

  public PolygonInstance(int n, double x, double y, double diameter, double rotation) {

    this.n = n;

    this.x = x;
    this.y = y;

    this.diameter = diameter;

    this.rotation = rotation;
  }

  private void getPerimeter() {

    if (xCoord == null) {

      xCoord = new float[n];
      yCoord = new float[n];

      PolygonAperture.computeXYAngle(xCoord, yCoord, null, getRadius(), n, rotation);
    }
  }

  protected double getRadius() {
    return Math.max(0.0, diameter / 2 + Util.vertexOffset(signedOffset, Math.PI * 2.0 / ((double) n)));
  }

  protected void makeBounds() {
    getPerimeter();
    bounds = new Rect(xCoord, yCoord);
  }

  protected void makeGeometries() {
    getPerimeter();
    geometries = PolygonAperture.makeGeometriesFromXY(x, y, xCoord, yCoord, n, getRadius(), rotation);
  }

  protected void offsetChanged() {
    super.offsetChanged();
    xCoord = yCoord = null;
  }

  protected void inverseChanged() {
    super.inverseChanged();
    xCoord = yCoord = null;
  }

  private int n;

  private double x, y;

  private double diameter;
  private double rotation;

  private float[] xCoord = null;
  private float[] yCoord = null;
}
