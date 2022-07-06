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

public abstract class Stroke extends Action {

  public Stroke(Aperture aperture, Vertex start, Vertex end) {

    this.aperture = aperture;

    this.start = start;
    this.end = end;

    start.addIncidentAction(this);
    end.addIncidentAction(this);

//    System.out.println(toString());
  }

  public Vertex getStart() {
    return start;
  }

  public Vertex getEnd() {
    return end;
  }

  public abstract String toString();

  public abstract double getLength();

  public abstract double getStartForwardDirection();

  public abstract double getStartReverseDirection();

  public abstract double getEndForwardDirection();

  public abstract double getEndReverseDirection();

  protected void offsetChanged() {
    aperture.setOffset(offset);
  }

  protected void inverseChanged() {
    aperture.setInverse(inverse);
  }

  protected Aperture aperture;
  protected Vertex start;
  protected Vertex end;
}
