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

import visolate.misc.*;

import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;

public class ThermalInstance extends PrimitiveInstance {

  private static final String cvsid =
  "$Id: ThermalInstance.java,v 1.3 2004/07/06 23:32:51 vona Exp $";


  public ThermalInstance(double x, double y,
                         double od, double id,
                         double xHairThickness,
                         double rotation) {

    this.x = x;
    this.y = y;

    this.od = od;
    this.id = id;

    this.xHairThickness = xHairThickness;

    this.rotation = rotation;
  }

  protected void makeBounds() {
    double o = getOD();
    bounds = new Rect(x-o/2, y-o/2, o, o);
  }

  private double getOD() {
    return Math.max(0.0, od + 2*signedOffset);
  }

  private double getID() {
    return Math.max(0.0, id - 2*signedOffset);
  }

  protected void makeGeometries() {
    geometries = new LinkedList<GeometryArray>();
    for (int i = 0; i < 4; i++)
      geometries.addAll(makeArcGeometries(x, y,
                                          getOD()/2, getID()/2,
                                          Math.max(0.0,
                                                   xHairThickness -
                                                   2*signedOffset),
                                          rotation*(Math.PI/180) +
                                          i*(Math.PI/2)));
  }

  public static Collection<TriangleArray> makeArcGeometries(double xCenter, double yCenter,
                                             double ro, double ri,
                                             double gapWidth,
                                             double rotation) {
    
    Collection<TriangleArray> geometries = new LinkedList<TriangleArray>();

    int segments = CircleAperture.SEGMENTS;

    float[] coords = new float[2*3*(segments + 1)];

    double angleo = Util.getOffsetAngle(ro, gapWidth/2);
    double segmento = (Math.PI/2 - 2*angleo)/segments;

    double anglei = Util.getOffsetAngle(ri, gapWidth/2);
    double segmenti = -(Math.PI/2 - 2*anglei)/segments;

    anglei = Math.PI/2 - anglei;

    angleo += rotation;
    anglei += rotation;

    double x, y;

    double angle = angleo;
    double rad = ro;
    double segment = segmento;

    int i = 0;

    for (int k = 0; k < 2; k++) {
      for (int j = 0; j <= segments; j++) {

        x = rad*Math.cos(angle);
        y = rad*Math.sin(angle);
      
        coords[i++] = (float) (x+xCenter);
        coords[i++] = (float) (y+yCenter);
        coords[i++] = 0.0f;
      
        angle += segment;
      }
      angle = anglei;
      rad = ri;
      segment = segmenti;
    }
    
    GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    gi.setCoordinates(coords);
    gi.setStripCounts(new int[] {2*(segments + 1)});

    geometries.add((TriangleArray) gi.getGeometryArray(true, false, false));

    return geometries;
  }


  private double x, y;

  private double od, id;
  
  private double xHairThickness;

  private double rotation;
}
