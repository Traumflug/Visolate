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

public class Arc extends Stroke {

  public Arc(Aperture aperture, Vertex start, Vertex end, Vertex center) {
    super(aperture, start, end);

    this.center = center;

//    System.out.println(toString());
  }

  public double getLength() {
    throw new UnsupportedOperationException("TBD Arc.getLength() unimplemented");
  }

  public double getStartForwardDirection() {
    throw new UnsupportedOperationException("TBD Arc direction unimplemented");
  }

  public double getStartReverseDirection() {
    throw new UnsupportedOperationException("TBD Arc direction unimplemented");
  }

  public double getEndForwardDirection() {
    throw new UnsupportedOperationException("TBD Arc direction unimplemented");
  }

  public double getEndReverseDirection() {
    throw new UnsupportedOperationException("TBD Arc direction unimplemented");
  }

  protected void makeBounds() {
    throw new UnsupportedOperationException("TBD Arc bounds unimplimented");
  }

  protected void makeGeometries() {
    throw new UnsupportedOperationException("TBD Arc geometry not implemented");
  }

  public String toString() {
    return "arc from " + start + " to " + end + "; center " + center + "; " + aperture;
  }

  private Vertex center;
}
