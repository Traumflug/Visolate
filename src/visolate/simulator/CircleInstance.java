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
 
package visolate.simulator;


import visolate.misc.*;

public class CircleInstance extends PrimitiveInstance {

  private static final String cvsid =
  "$Id: CircleInstance.java,v 1.2 2004/06/30 17:26:29 vona Exp $";


  public CircleInstance(double diameter, double x, double y) {

    this.diameter = diameter;

    this.x = x;
    this.y = y;
  }

  protected void makeBounds() {
    double r = getRadius();
    bounds = new Rect(x-r, y-r, 2*r, 2*r);
  }

  protected double getRadius() {
    return Math.max(0.0, diameter/2 + signedOffset);
  }

  protected void makeGeometries() {
    geometries = CircleAperture.makeCircleGeometries(x, y, getRadius());
  }

  private double diameter;
  private double x, y;
}
