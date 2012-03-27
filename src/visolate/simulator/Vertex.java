/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
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

import javax.vecmath.*;

import visolate.model.*;

import java.util.*;

public class Vertex {

  private static final String cvsid =
  "$Id: Vertex.java,v 1.1.1.1 2004/06/24 05:46:01 vona Exp $";


  public static final double MM_TO_IN = 1.0/25.4;

  public Vertex(int x, int y) {

    this.x = x;
    this.y = y;

    hashCode = x^(y*31);

//    System.out.println(toString());
  }

  public void addIncidentAction(Action action) {
    incidentActions.add(action);
  }
 
  public Set<Action> getIncidentActions() {
    return incidentActions;
  }

  public int hashCode() {
    return hashCode;
  }

  public boolean equals(Object object) {

    if (!(object instanceof Vertex))
      return false;

    Vertex other = (Vertex) object;

    return (x == other.x) && (y == other.y);
  }

  public String toString() {
    if (point2f != null)
      return "(" + point2f.x + ", " + point2f.y + ")";
    else
      return "(" + x + ", " + y + ")";
  }

  public String toStringInches() {
    return "(" + point2f.x + ", " + point2f.y + ")";
  }

  public Point2f getInchCoordinates() {
    return point2f;
  }

  public void computeInchCoordinates(int xScale, int yScale, boolean metric) {

    double xi = ((double) x)/((double) xScale);
    double yi = ((double) y)/((double) yScale);

    if (metric) {
      xi *= MM_TO_IN;
      yi *= MM_TO_IN;
    }

    point2f = new Point2f((float) xi, (float) yi);

//    System.out.println("(" + x + ", " + y + ")->(" + point2f.x + ", " + point2f.y + ")");
  }

  public void addOutgoingHalfEdge(HalfEdge e) {
    outgoingHalfEdges.add(e);
  }

  public void removeOutgoingHalfEdge(HalfEdge e) {
    outgoingHalfEdges.remove(e);
  }

  public HalfEdge getNextOutgoingHalfEdge(HalfEdge incomingHalfEdge) {

    HalfEdge next = null;

    if (outgoingHalfEdges.size() == 1) {

      next = (HalfEdge) (outgoingHalfEdges.iterator()).next();

    } else {

      double min = Double.POSITIVE_INFINITY;
      
//    int dbg = 0;
      for ( HalfEdge he : outgoingHalfEdges) {
        
        //only want to double back when that's the only way out, and would have
        //caught that above
        if (he.getStroke() == incomingHalfEdge.getStroke()) {
//        assert ++dbg <= 1;
          continue;
        }
        
        double angle = incomingHalfEdge.angleTo(he);
        
        if (angle < min) {
          min = angle;
          next = he;
        }
      }
      
      //HACK apparrently it's possible to have (only) k > 1 outgoing half
      //edges which all have the same stroke as incomingHalfEdges
      if (next == null)
        next = (HalfEdge) (outgoingHalfEdges.iterator()).next();
    }

    outgoingHalfEdges.remove(next);

    return next;
  }

  public int x;
  public int y;

  private Point2f point2f;

  private int hashCode;

  private Set<Action> incidentActions = new LinkedHashSet<Action>();

  private Collection<HalfEdge> outgoingHalfEdges = new LinkedHashSet<HalfEdge>();
}
