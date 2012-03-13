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

import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

public abstract class Action extends SimObject {

  private static final String cvsid =
  "$Id: Action.java,v 1.2 2004/06/30 17:26:29 vona Exp $";

  protected static void addTranslatedAperture(Collection<GeometryArray> geometries,
                                              Aperture aperture,
                                              Vertex v) {

    Collection<GeometryArray> apGeoms = aperture.getGeometries();

    if (apGeoms == null) {
      return;
    }

    Vector2f p = new Vector2f(v.getInchCoordinates());

    for (Iterator<GeometryArray> it = apGeoms.iterator(); it.hasNext(); ) {
      GeometryArray ga = dupGeometry(it.next());
      translateGeometry(ga, p);
      geometries.add(ga);
    }
  }
}
