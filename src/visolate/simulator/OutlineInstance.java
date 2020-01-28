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

import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.*;

public class OutlineInstance extends PrimitiveInstance {

  public static final Vector3d Z = new Vector3d(0.0, 0.0, 1.0);

  public static final double MIN_D_LENGTH = 1e-6;

  public OutlineInstance(final int aN, final List<Point2d> aPoints, double aRotation) {

//    this.myN = aN;

    this.myPoints = aPoints;

    this.myRotation = aRotation;

    numVerts = aPoints.size();

    if (aN != numVerts)
      System.err.println("WARNING: amacro primitive type 4 num points (" + aN
          + ") disagrees with actual number of coordinates; " + "ignoring");
  }

  private void getPerimeter() {

    if (x == null) {

      x = new float[numVerts];
      y = new float[numVerts];

      if (numVerts == 0) {
        return;
      }

      Transform3D t3d = new Transform3D();
      t3d.rotZ(myRotation * (Math.PI / 180));

      Point2d p = null;
      if (myPoints.size() > 1) {
        p = (Point2d) myPoints.get(myPoints.size() - 1);
      }

      Point2d prev = null;
      if (myPoints.size() > 2)
        prev = (Point2d) myPoints.get(myPoints.size() - 2);

      Point2d next;

      Point3d p3 = new Point3d();

      Vector3d d = new Vector3d();

      Vector3d d0 = new Vector3d();
      Vector3d d1 = new Vector3d();

      int i = 0;
      for (Iterator<Point2d> it = myPoints.iterator(); it.hasNext();) {

        next = it.next();

        if ((prev != null) && (p != null)) {

          d0.set(p.x - prev.x, p.y - prev.y, 0.0);
          d1.set(p.x - next.x, p.y - next.y, 0.0);

          d0.normalize();
          d1.normalize();

          d.set(0.5 * (d0.x + d1.x), 0.5 * (d0.y + d1.y), 0.0);
          d.normalize();
          if (d.length() < MIN_D_LENGTH)
            d.cross(d0, Z);

          double angle = Util.angleCCW(d1, d0);

          if (angle > Math.PI)
            angle = Math.PI * 2.0 - angle;

          d.scale(Util.vertexOffset(signedOffset, angle));

        } else if (p != null) {

          // degenerate case: 2 points

          d.set(p.x - next.x, p.y - next.y, 0.0);
          d.normalize();
          d.scale(signedOffset);

        } else {

          // degenerate case: 1 point

          p = next;
          d.set(0.0, 0.0, 0.0);
        }

        p3.set(p.x + d.x, p.y + d.y, 0.0);

        t3d.transform(p3);

        x[i] = (float) p3.x;
        y[i] = (float) p3.y;

        i++;

        prev = p;
        p = next;
      }
    }
  }

  protected void makeBounds() {
    getPerimeter();
    bounds = new Rect();
    for (int i = 0; i < numVerts; i++)
      bounds.add(x[i], y[i]);
  }

  protected void makeGeometries() {

    getPerimeter();

    geometries = new LinkedList<GeometryArray>();

    GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);

    float[] coords = new float[3 * numVerts];

    int j = 0;
    for (int i = 0; i < numVerts; i++) {
      coords[j++] = x[i];
      coords[j++] = y[i];
      coords[j++] = 0.0f;
    }

    gi.setCoordinates(coords);
    gi.setStripCounts(new int[] { numVerts });

    geometries.add(gi.getGeometryArray(true, false, false));
  }

  protected void offsetChanged() {
    super.offsetChanged();
    x = y = null;
  }

  protected void inverseChanged() {
    super.inverseChanged();
    x = y = null;
  }

//  private int myN;

  private List<Point2d> myPoints;

  private int numVerts;

  private double myRotation;

  private float[] x = null;
  private float[] y = null;
}
