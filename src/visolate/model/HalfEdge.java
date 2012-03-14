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

package visolate.model;

import visolate.simulator.*;

public class HalfEdge {

  public HalfEdge(Stroke stroke, boolean reverse) {

    this.stroke = stroke;
    this.reverse = reverse;

    if (!reverse)
      (stroke.getStart()).addOutgoingHalfEdge(this);
    else
      (stroke.getEnd()).addOutgoingHalfEdge(this);
  }

  public void setNext(HalfEdge next) {
    this.next = next;
  }

  public HalfEdge getNext() {
    return next;
  }

  public Stroke getStroke() {
    return stroke;
  }

  public Vertex getStart() {
    if (!reverse)
      return stroke.getStart();
    else
      return stroke.getEnd();
  }

  public Vertex getEnd() {
    if (!reverse)
      return stroke.getEnd();
    else
      return stroke.getStart();
  }

  public double getStartDirection() {
    if (!reverse)
      return stroke.getStartForwardDirection();
    else
      return stroke.getEndReverseDirection();
  }

  public double getEndDirection() {
    if (!reverse)
      return stroke.getEndForwardDirection();
    else
      return stroke.getStartReverseDirection();
  }

  public double angleTo(HalfEdge he) {
    
    double myDir = getEndDirection();
    double hisDir = he.getStartDirection();

    double himToMe;
    
    if (myDir >= hisDir)
      himToMe = myDir - hisDir;
    else
      himToMe = 2*Math.PI - (hisDir - myDir);

    if (himToMe <= Math.PI)
      return Math.PI - himToMe;
    else
      return 2*Math.PI - (himToMe - Math.PI);
  }

  private Stroke stroke;
  private boolean reverse;

  private HalfEdge next;
}
