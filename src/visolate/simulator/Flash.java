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

import java.util.*;

import javax.media.j3d.GeometryArray;
import javax.vecmath.*;

import visolate.misc.*;

public class Flash extends Action {

  private static final String cvsid =
  "$Id: Flash.java,v 1.2 2004/06/30 17:26:29 vona Exp $";


  public Flash(Aperture aperture, Vertex location) {

    this.aperture = aperture;
    this.location = location;
    
    location.addIncidentAction(this);

//    System.out.println(toString());
  }

  public Vertex getLocation() {
    return location;
  }

  protected void makeBounds() {
    
    Rect b = aperture.getBounds();
    Point2f p = location.getInchCoordinates();
    
    bounds = new Rect(b.x + p.x, b.y + p.y, b.width, b.height);
  }

  protected void makeGeometries() {
    geometries = new LinkedList<GeometryArray>();
    addXlatedAperture(geometries, aperture, location);
  }

  public String toString() {
    return "flash at " + location + "; " + aperture;
  }

  protected void offsetChanged() {
    aperture.setOffset(offset);
  }
                                   
  protected void inverseChanged() {
    aperture.setInverse(inverse);
  }

  private Aperture aperture;
  private Vertex location;
}
