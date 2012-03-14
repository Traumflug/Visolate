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

package visolate.misc;

public class Rect {

  public Rect() {
    this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    uninitialized = true;
  }

  public Rect(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public Rect(Rect other) {
    this.x = other.x;
    this.y = other.y;
    this.width = other.width;
    this.height = other.height;
  }

  public Rect(float[] x, float[] y) {
    
    float minX = Float.POSITIVE_INFINITY;
    float minY = Float.POSITIVE_INFINITY;
    
    float maxX = Float.NEGATIVE_INFINITY;
    float maxY = Float.NEGATIVE_INFINITY;
    
    for (int i = 0; i < x.length; i++) {
      
      if (x[i] < minX)
        minX = x[i];
      
      if (y[i] < minY)
        minY = y[i];
      
      if (x[i] > maxX)
        maxX = x[i];
      
      if (y[i] > maxY)
        maxY = y[i];
    }
    
    this.x = minX;
    this.y = minY;
    this.width = maxX-minX;
    this.height = maxY-minY;
  }

  public Rect add(Rect other) {
    
    if (uninitialized) {
      this.x = other.x;
      this.y = other.y;
      this.width = other.width;
      this.height = other.height;
      uninitialized = false;
      return this;
    }

    add(other.x, other.y);
    add(other.x + other.width, other.y);
    add(other.x + other.width, other.y + other.height);
    add(other.x, other.y + other.height);

    return this;
  }

  public Rect add(double x, double y) {
   
//    System.out.println("adding (" + x + ", " + y + ") to " + toString());

    if (uninitialized) {
      this.x = x;
      this.y = y;
      this.width = 0.0;
      this.height = 0.0;
      uninitialized = false;
      return this;
    }

    if (x < this.x) {
      width += this.x - x;
      this.x = x;
    } else if (x > (this.x + width)) {
      width = x - this.x;
    }

    if (y < this.y) {
      height += this.y - y;
      this.y = y;
    } else if (y > (this.y + height)) {
      height = y - this.y;
    }

//    System.out.println("  result " + toString());
    
    return this;
  }

  public String toString() {
    return
      "LLC: (" + x + ", " + y +
      "); width = " + width + "; height = " + height;
  }

  public void translate(double x, double y) {
    this.x += x;
    this.y += y;
  }

  public double x;
  public double y;
  public double width;
  public double height;

  public boolean uninitialized = false;
}
