/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **/
                                                                                
package visolate.misc;

import javax.vecmath.*;

import java.util.*;

public class Util {

  private static final String cvsid =
  "$Id: Util.java,v 1.4 2005/04/11 15:16:21 vona Exp $";

  public static final double MIN_DY = 1e-6;

  public static float distance(float x0, float y0,
                                float x1, float y1) {
    return (float) Math.sqrt(distanceSquared(x0, y0, x1, y1));
  }

  public static float distanceSquared(float x0, float y0,
                                       float x1, float y1) {
    double dx = x1-x0;
    double dy = y1-y0;

    return (float) (dx*dx + dy*dy);
  }

  public static double canonicalAngle(double x, double y) {

    double angle = Math.atan2(y, x);

    if (angle < 0.0)
      angle += Math.PI*2.0;

    return angle;
  }

  public static double canonicalizeAngle(double theta) {

    while (theta > Math.PI*2)
      theta -= Math.PI*2;

    while (theta < 0)
      theta += Math.PI*2;

    return theta;
  }

  public static int findClosest(double theta, double[] angle) {

//    System.out.println("find " + theta + " in:");
//    for (int i = 0; i < angle.length; i++)
//      System.out.println("  " + angle[i]);

    int closest = Arrays.binarySearch(angle, theta);

    if (closest < 0) {

      int next = -closest - 1;
      
      int prev = next - 1;
      
      if (next == angle.length)
        next = 0;
      
      if (prev < 0)
        prev = angle.length-1;

      if ((theta - angle[prev]) < (angle[next] - theta))
        closest = prev;
      else
        closest = next;
    }

    return closest;
  }

  public static double getPolyWidth(float[] x, float[] y, double[] angle,
                                    double theta) {
    
    theta = canonicalizeAngle(theta);

    double dx = Math.cos(theta);
    double dy = Math.sin(theta);

    int i = findClosest(theta, angle);

    double d1 = dx*x[i] + dy*y[i];

    theta = canonicalizeAngle(theta + Math.PI);

    dx = -dx;
    dy = -dy;

    i = findClosest(theta, angle);

    double d2 = dx*x[i] + dy*y[i];

    return Math.abs(d1)+Math.abs(d2);
  }

  /**
   * is this just asin?
   **/
  public static double getOffsetAngle(double rad, double y) {
    return Math.atan2(y, Math.sqrt(rad*rad-y*y));
  }

  public static double angleCCW(Vector3d from, Vector3d to) {

    double angleF = canonicalizeAngle(Math.atan2(from.y, from.x));
    double angleT = canonicalizeAngle(Math.atan2(to.y, to.x));

    return canonicalizeAngle(angleT-angleF);
  }

  public static double vertexOffset(double offset, double angle) {
    
    Vector2d p0 = new Vector2d(Math.cos(angle-Math.PI/2),
                              Math.sin(angle-Math.PI/2));
    
    p0.scale(-offset);

    Vector2d p1 = new Vector2d(p0.x + Math.cos(angle),
                               p0.y + Math.sin(angle));
    
    double dy = p1.y-p0.y;

    if (dy < MIN_DY)
      return offset;

    double dx = p1.x-p0.x;

    double xIntercept = p0.x - (dx/dy)*p0.y;

    double y = -offset;
    double x = xIntercept + (dx/dy)*y;

    return Math.sqrt(x*x + y*y)*sign(offset);
  }

  public static double sign(double d) {
    if (d < 0)
      return -1;
    else if (d == 0)
      return 0;
    else
      return 1;
  }
}



