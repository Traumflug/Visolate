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

package visolate.processor;

public class ToolpathNode {

  public static final int N = 0;
  public static final int S = 1;
  public static final int W = 2;
  public static final int E = 3;

  ToolpathNode(int x, int y) {

    this.x = x;
    this.y = y;

    locked = false;

    hashCode = x^(y*31);
  }

  public int hashCode() {
    return hashCode;
  }

  public void setIsLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isLocked() {
    return locked;
  }

  public boolean equals(Object object) {

    if (!(object instanceof ToolpathNode))
      return false;

    ToolpathNode other = (ToolpathNode) object;

    return (x == other.x) && (y == other.y);
  }

  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  public int numNeighbors() {

    int n = 0;

    if (north != null)
      n++;

    if (south != null)
      n++;

    if (west != null)
      n++;

    if (east != null)
      n++;

    return n;
  }

  public ToolpathNode getNeighbor(int d) {
    switch(d) {
    case N:
      return north;
    case S:
      return south;
    case W:
      return west;
    case E:
      return east;
    default:
      return null;
    }
  }

  public void setNeighbor(int d, ToolpathNode n) {
    switch(d) {
    case N:
      north = n;
      break;
    case S:
      south = n;
      break;
    case W:
      west = n;
      break;
    case E:
      east = n;
      break;
    }
  }

  int hashCode;

  int x;
  int y;

  boolean locked;

  ToolpathNode north = null;
  ToolpathNode south = null;
  ToolpathNode west = null;
  ToolpathNode east = null;
}
