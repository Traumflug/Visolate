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

public class MoireInstance extends PrimitiveInstance {

  public MoireInstance(
      double x, 
      double y, 
      double od, 
      double circleThickness, 
      double gap, int n, 
      double xHairThickness,
      double xHairLength, 
      double rotation) {

    this.x = x;
    this.y = y;

    this.od = od;

    this.circleThickness = circleThickness;

    this.gap = gap;

    this.n = n;

    this.xHairThickness = xHairThickness;
    this.xHairLength = xHairLength;

    this.rotation = rotation;
  }

  protected void makeBounds() {
    double o = getOD();
    bounds = new Rect(x - o / 2, y - o / 2, o, o);
  }

  private double getOD() {
    return Math.max(0.0, od + 2 * signedOffset);
  }

  protected void makeGeometries() {

    geometries = new LinkedList<GeometryArray>();

    double spokeDiameter = Math.max(0.0, xHairLength + 2 * signedOffset);
    double spokeWidth = Math.max(0.0, xHairThickness + 2 * signedOffset);

    for (int i = 0; i < 2; i++)
      geometries
          .addAll(makeSpokeGeometries(x, y, spokeDiameter, spokeWidth, rotation * (Math.PI / 180) + i * (Math.PI / 2)));

    double o = getOD();
    double i = od - 2 * (circleThickness + signedOffset);
    double diff = 2 * (circleThickness + gap);

    for (int j = 0; (j < n) && (i >= 0); j++) {

      geometries.addAll(makeDonutGeometries(x, y, o / 2, i / 2));

      o -= diff;
      i -= diff;
    }
  }

  public static Collection<TriangleArray> makeSpokeGeometries(
      double xCenter, 
      double yCenter, 
      double diameter,
      double width, 
      double rotation) {

    Collection<TriangleArray> geometries = new LinkedList<TriangleArray>();

    int segments = CircleAperture.SEGMENTS;

    float[] coords = new float[2 * 3 * (segments + 1)];

    double rad = diameter / 2;

    double angle = Util.getOffsetAngle(rad, -width / 2);
    double segment = (-2 * angle) / segments;

    angle += rotation;

    double x, y;

    int i = 0;

    for (int k = 0; k < 2; k++) {
      for (int j = 0; j <= segments; j++) {

        x = rad * Math.cos(angle);
        y = rad * Math.sin(angle);

        coords[i++] = (float) (x + xCenter);
        coords[i++] = (float) (y + yCenter);
        coords[i++] = 0.0f;

        angle += segment;
      }
      angle = angle + Math.PI - segments * segment;
    }

    GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    gi.setCoordinates(coords);
    gi.setStripCounts(new int[] { 2 * (segments + 1) });

    geometries.add((TriangleArray) gi.getGeometryArray(true, false, false));

    return geometries;
  }

  public static Collection<TriangleArray> makeDonutGeometries(
      double xCenter, 
      double yCenter, 
      double ro, 
      double ri) {

    Collection<TriangleArray> geometries = new LinkedList<TriangleArray>();

    int segments = CircleAperture.SEGMENTS;

    double segment = (Math.PI * 2.0) / segments;

    float[] coords = new float[2 * 3 * segments];

    double xo, yo;
    double xi, yi;
    double angle = 0.0;

    int i = 0;
    int k = 3 * segments;

    for (int j = 0; j < segments; j++) {

      xo = ro * Math.cos(angle);
      yo = ro * Math.sin(angle);

      xi = ri * Math.cos(-angle);
      yi = ri * Math.sin(-angle);

      coords[i++] = (float) (xo + xCenter);
      coords[i++] = (float) (yo + yCenter);
      coords[i++] = 0.0f;

      coords[k++] = (float) (xi + xCenter);
      coords[k++] = (float) (yi + yCenter);
      coords[k++] = 0.0f;

      angle += segment;
    }

    GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    gi.setCoordinates(coords);
    gi.setStripCounts(new int[] { segments, segments });
    gi.setContourCounts(new int[] { 2 });

    geometries.add((TriangleArray) gi.getGeometryArray(true, false, false));

    return geometries;
  }

  private double x, y;

  private double od;

  private double circleThickness;

  private double gap;

  private int n;

  private double xHairThickness;
  private double xHairLength;

  private double rotation;
}
