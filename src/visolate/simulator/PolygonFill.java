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

public class PolygonFill extends Action {

  private static final String cvsid =
  "$Id: PolygonFill.java,v 1.2 2004/06/30 17:26:29 vona Exp $";


  protected void makeBounds() {
    throw new UnsupportedOperationException("TBD Polygon bounds unimplimented");
  }

  protected void makeGeometries() {
    throw
      new UnsupportedOperationException("TBD Polygon geometry not implemented");
  }

//  private List<Vertex> vertices = new LinkedList<Vertex>();
}
