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

import javax.vecmath.*;
import javax.media.j3d.*;

import visolate.*;
import visolate.simulator.*;
import visolate.misc.*;

import java.util.*;

public class Net implements Comparable<Net> {

  private static final String cvsid =
  "$Id: Net.java,v 1.10 2006/08/29 04:02:32 vona Exp $";

  public static final int CIRCLE_SEGMENTS = 16;
  public static final double CIRCLE_SECTOR = 2.0*Math.PI/CIRCLE_SEGMENTS;

  public static final Vector3f Z = new Vector3f(0.0f, 0.0f, 1.0f);

  public static final float TAIL_SIZE = 0.001f;
  public static final double MIN_TAIL_ANGLE = 5*Math.PI/180;
  public static final double MAX_TAIL_ANGLE = 30*Math.PI/180;
  public static final double LINE_TWIDDLE = 0.0005;

  public static final float GCODE_Z_MIN = 0.2f;
  public static final float PATH_Z = 0.1f;
  public static final float OUTLINE_Z = 0.05f;
  public static final float LINE_Z = 0.0f;
  public static final float POINT_Z = 0.0f;
  public static final float FLAT_Z_MAX = -0.1f;
  public static final float FLAT_Z_MIN = -0.9f;
  public static final float LOOP_Z_MAX = -1.0f;
  public static final float CONE_Z_MAX = -1.0f;

  public static final float HIGHLIGHT = 0.9f;

  public Net(Visolate visolate) {
    this.visolate = visolate;
  }

  public void resetArea(int dpi) {
    areaPixels = 0;
    areaDPI = dpi;
  }

  public void incrementAreaPixels() {
    areaPixels++;
  }

  public double getArea() {
    double ipd = 1.0/((double) areaDPI);
    return ((double) areaPixels)*ipd*ipd;
  }

  public double getFatness() {

    double length = getLength();

    if (length > 0.0)
      return getArea()/length;
    else
      return getArea()/getWidth();
  }

  public double getLength() {
   
    double length = 0.0;
    
    if (mySuperNet == null) {
      length = getLocalLength();
    } else {
      for (Net snet : mySuperNet) {
        length += snet.getLocalLength();
      }
    }

    return length;
  }

  protected double getLocalLength() {

    double length = 0.0;

    for (Iterator<Stroke> it = strokes.iterator(); it.hasNext(); )
      length += it.next().getLength();

    return length;
  }

  public double getWidth() {
    if (mySuperNet == null) {
      return getBounds().width;
    } else {
      Rect r = new Rect();
      for (Iterator<Net> it = mySuperNet.iterator(); it.hasNext(); )
        r.add(it.next().getBounds());
      return r.width;
    }
  }

  public void dump() {
    System.out.println("(R, G, B): " + color + ": " +
                       + ((mySuperNet != null) ? (mySuperNet.size()-1) : 0) +
                       " siblings; " +
                       + strokes.size() + " strokes; " +
                       + pads.size() + " pads" +
                       "\n  bounds: " + getBounds() +
                       "\n  length: " + getLength() +
                       "; area: " + getArea() +
                       "; fatness: " + getFatness() +
                       "; areaDPI: " + areaDPI +
                       "; areaPixels: " + areaPixels);
  }

  public void setSuperNet(final Set<Net> aSuperNet) {

    if (this.mySuperNet == aSuperNet) {
      return;
    }

    if (this.mySuperNet != null) {
      aSuperNet.addAll(this.mySuperNet);
    }

    aSuperNet.add(this);

    for (Iterator<Net> it = aSuperNet.iterator(); it.hasNext(); ) {

      Net net = it.next();
      net.mySuperNet = aSuperNet;

      if (!(net.color).equals(color))
        net.setColor(color);
    }
  }

  public Set<Net> getSuperNet() {
    return mySuperNet;
  }

  public void setOffset(final double offset) {

    if (this.offset == offset) {
      return;
    }

//    System.out.println("setting offset on net");

    this.offset = offset;

    bounds = null;

    for (Stroke stroke : strokes) {
    	stroke.setOffset(offset);
    }

    for (Flash flash : pads) {
      flash.setOffset(offset);
    }

    rebuildFlatGeometry();
  }

  public void addStroke(Stroke stroke) {
    stroke.setOffset(offset);
    strokes.add(stroke);
    halfEdges.add(new HalfEdge(stroke, false));
    halfEdges.add(new HalfEdge(stroke, true));
  }

  public void addPad(Flash f) {
    f.setOffset(offset);
    pads.add(f);
  }

  public int getNumEdges() {
    return halfEdges.size()/2;
  }

  public int getNumPads() {
    return pads.size();
  }

  public int makeHalfEdgeLoops() {


    Set<HalfEdge> halfEdges = new HashSet<HalfEdge>();
    halfEdges.addAll(this.halfEdges);

//    System.out.println("  making half edge loops for a net with " +
//                       halfEdges.size() + " half edges");

    while (!halfEdges.isEmpty()) {

//      System.out.println("    starting loop with " +
//                         halfEdges.size() + " half edges remaining");

      HalfEdge he = halfEdges.iterator().next();

      loopStarts.add(he);

      HalfEdge startHalfEdge = he;
      Vertex start = he.getStart();

//      System.out.println("      start vertex: " + start);

      start.removeOutgoingHalfEdge(he);

      for (;;) {

        boolean ok = halfEdges.remove(he);

        Vertex end = he.getEnd();

//        System.out.println("      vertex: " + end);

        assert ok;

        if (start.equals(end))
          break;

        HalfEdge next = end.getNextOutgoingHalfEdge(he);
        
        assert next != he;
        assert (he != null);

        he.setNext(next);
        
        he = next;
      }

      he.setNext(startHalfEdge);

//      System.out.println("    end vertex: " + he.getEnd());
    }

    return loopStarts.size();
  }

  public void printHalfEdgeLoops() {

    int i = 0;
    for (Iterator<HalfEdge> it = loopStarts.iterator(); it.hasNext(); i++) {

      System.out.println("half edge loop " + i);

      HalfEdge start = (HalfEdge) it.next();

      HalfEdge next = start;

      do {

        System.out.println(((Vertex) next.getStart()).toStringInches());

        next = next.getNext();

      } while (next != start);

      System.out.println();
    }
  }

  public void setColor(Color3b color) {

    this.color = color;

    if (coloringAttributes != null)
      coloringAttributes.setColor(applyHighlight(toColor3f(color)));
  }

  public void setHighlighted(boolean highlighted) {

    if (mySuperNet == null) {

      this.highlighted = highlighted;
      setColor(color);

    } else {
      for (Net net : mySuperNet) {
        net.highlighted = highlighted;
        net.setColor(net.color);
      }
    }
  }

  private Color3f applyHighlight(Color3f color) {

    if (highlighted) {
      color.x += HIGHLIGHT*(1.0f-color.x);
      color.y += HIGHLIGHT*(1.0f-color.y);
      color.z += HIGHLIGHT*(1.0f-color.z);
    }
    
    return color;
  }

  public static Color3f toColor3f(Color3b color) {
    return new Color3f(unsignedByteToInt(color.x)/255.0f,
                       unsignedByteToInt(color.y)/255.0f,
                       unsignedByteToInt(color.z)/255.0f);
  }

  public static int unsignedByteToInt(byte b) {
    return b & 0xff;
  }

  public Color3b getColor() {
    return color;
  }

  public void enableLineGeometry(boolean enable) {
   
    if (enable && (lineGeometry == null))
      makeLineGeometry();

    if (enable && (pointGeometry == null))
      makePointGeometry();

    showLineGeometry = enable;

    enableGeometry(lineS3D, lineGeometry, enable);
    enableGeometry(pointS3D, pointGeometry, enable);
  }

  public void enableVoronoiGeometry(boolean enable) {
    
    if (enable && (loopGeometry == null))
      makeVoronoiGeometry();

    showVoronoiGeometry = enable;

    enableGeometry(loopS3D, loopGeometry, enable);
//    enableGeometry(loopEdgesS3D, loopGeometry, enable);
    enableGeometry(coneS3D, coneGeometry, enable);
//    enableGeometry(coneEdgesS3D, coneGeometry, enable);
  }


  private void rebuildVoronoiGeometry() {

    boolean voronoiGeometryWas = showVoronoiGeometry;

    enableVoronoiGeometry(false);

    coneGeometry = null;
    loopGeometry = null;

    enableVoronoiGeometry(voronoiGeometryWas);
  }

  private void rebuildFlatGeometry() {

    boolean flatGeometryWas = showFlatGeometry;
    
    enableFlatGeometry(false);

    flatFanGeometry = null;
    flatNonFanGeometry = null;

    enableFlatGeometry(flatGeometryWas);
  }

  public void enableFlatGeometry(boolean enable) {
    
    if (enable && (flatFanGeometry == null))
      makeFlatGeometry();

    showFlatGeometry = enable;
    
    enableGeometry(flatFanS3D, flatFanGeometry, enable);
    enableGeometry(flatNonFanS3D, flatNonFanGeometry, enable);
  }

  public void setTranslucent2D(boolean enable) {
    
    if (enable == translucent2D)
      return;

    translucent2D = enable;

    if (appearance2D == null)
      return;

    if (enable)
      appearance2D.setTransparencyAttributes(transparencyAttributes);
    else
      appearance2D.setTransparencyAttributes(null);
  }
  
  public boolean isTranslucent2D() {
    return (appearance2D.getTransparencyAttributes() != null);
  }

  private void enableGeometry(final Shape3D shape3D,
                              final Geometry geometry,
                              final boolean enable) {

    if ((geometry == null) || (shape3D == null))
      return;

    Runnable task = new Runnable() {
        public void run() {
          if (enable) {
            if (shape3D.indexOfGeometry(geometry) < 0) {
              if (!geometry.isLive()) {
                geometry.setCapability(Geometry.ALLOW_INTERSECT);
                geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
                geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
                geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
              }
              shape3D.addGeometry(geometry);
            }
          } else {
            shape3D.removeAllGeometries();
          }
        }
      };

    if (shape3D.isLive())
      visolate.addFrameTask(task);
    else
      task.run();
  }

  public BranchGroup getSceneGraph() {

    if (sceneBG == null) {

      Appearance appearance = new Appearance();

      coloringAttributes = new ColoringAttributes();
      coloringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
      coloringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
      if (color != null)
        coloringAttributes.setColor(applyHighlight(toColor3f(color)));
      appearance.setColoringAttributes(coloringAttributes);
      PolygonAttributes polygonAttributes =
        new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
                              PolygonAttributes.CULL_NONE,
                              0.0f);
      appearance.setPolygonAttributes(polygonAttributes);

/*
      Appearance appearanceEdges = new Appearance();
      appearanceEdges.
        setColoringAttributes(new ColoringAttributes(0.0f,
                                                     1.0f,
                                                     0.0f,
                                                     ColoringAttributes.
                                                     SHADE_FLAT));
      appearanceEdges.
        setPolygonAttributes(new PolygonAttributes(
          PolygonAttributes.POLYGON_LINE,
          PolygonAttributes.CULL_NONE,
          1.0f,
          true,
          -1.0f));
      appearanceEdges.setLineAttributes(new LineAttributes(1.0f,
                                                           LineAttributes.
                                                           PATTERN_SOLID,
                                                           true));
//      appearanceEdges.
//        setTransparencyAttributes(new TransparencyAttributes(
//          TransparencyAttributes.SCREEN_DOOR,
//          0.8f));
*/


      Appearance appearance1D = new Appearance();

      LineAttributes lineAttributes1D = new LineAttributes(1.0f, 
//                                                           4.0f,
                                                           LineAttributes.
                                                           PATTERN_SOLID,
                                                           false);
      appearance1D.setLineAttributes(lineAttributes1D);
      PointAttributes pointAttributes1D = new PointAttributes(1.0f,
                                                              //4.0f,
                                                              false);
      appearance1D.setPointAttributes(pointAttributes1D);
      ColoringAttributes coloringAttributes1D = new ColoringAttributes();
      coloringAttributes1D.setColor(new Color3f(0.0f, 0.0f, 0.0f));
//      coloringAttributes1D.setColor(new Color3f(1.0f, 1.0f, 1.0f));
      appearance1D.setColoringAttributes(coloringAttributes1D);
      PolygonAttributes polygonAttributes1D = new PolygonAttributes();
      polygonAttributes1D.setPolygonOffset(0.1f);
      appearance1D.setPolygonAttributes(polygonAttributes1D);

      appearance2D = new Appearance();
      appearance2D.setColoringAttributes(coloringAttributes);
      appearance2D.setPolygonAttributes(polygonAttributes);
      appearance2D.
        setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
      appearance2D.
        setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
      transparencyAttributes =
        new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.5f);
      
      if (translucent2D)
        appearance2D.setTransparencyAttributes(transparencyAttributes);

      lineS3D = new Shape3D();
      pointS3D = new Shape3D();
      coneS3D = new Shape3D();
//      coneEdgesS3D = new Shape3D();
      loopS3D = new Shape3D();
//      loopEdgesS3D = new Shape3D();
      flatFanS3D = new Shape3D();
      flatNonFanS3D = new Shape3D();
      
      lineS3D.setPickable(false);
      pointS3D.setPickable(false);
      coneS3D.setPickable(false);
//      coneEdgesS3D.setPickable(false);
      loopS3D.setPickable(false);
//      loopEdgesS3D.setPickable(false);
      flatFanS3D.setPickable(true);
      flatNonFanS3D.setPickable(true);

      lineS3D.setUserData(this);
      pointS3D.setUserData(this);
      coneS3D.setUserData(this);
//      coneEdgesS3D.setUserData(this);
      loopS3D.setUserData(this);
//      loopEdgesS3D.setUserData(this);
      flatFanS3D.setUserData(this);
      flatNonFanS3D.setUserData(this);

      lineS3D.setAppearance(appearance1D);
      pointS3D.setAppearance(appearance1D);
      coneS3D.setAppearance(appearance);
//      coneEdgesS3D.setAppearance(appearanceEdges);
      loopS3D.setAppearance(appearance);
//      loopEdgesS3D.setAppearance(appearanceEdges);
      flatFanS3D.setAppearance(appearance2D);
      flatNonFanS3D.setAppearance(appearance2D);
      
      lineS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      lineS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      
      pointS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      pointS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

      coneS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      coneS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//      coneEdgesS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//      coneEdgesS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      
      loopS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      loopS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//      loopEdgesS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//      loopEdgesS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

      flatFanS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      flatFanS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

      flatNonFanS3D.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      flatNonFanS3D.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

      sceneBG = new BranchGroup();
      sceneBG.setCapability(BranchGroup.ALLOW_DETACH);

      sceneBG.addChild(lineS3D);
      sceneBG.addChild(pointS3D);
      sceneBG.addChild(coneS3D);
//      sceneBG.addChild(coneEdgesS3D);
      sceneBG.addChild(loopS3D);
//      sceneBG.addChild(loopEdgesS3D);
      sceneBG.addChild(flatFanS3D);
      sceneBG.addChild(flatNonFanS3D);

      enableLineGeometry(showLineGeometry);
      enableVoronoiGeometry(showVoronoiGeometry);
      enableFlatGeometry(showFlatGeometry);
    }
    
    return sceneBG;
  }

  public void detach() {

    if (sceneBG != null)
      sceneBG.detach();

    sceneBG = null;
  }

  private void makeLineGeometry() {

    if (strokes.isEmpty())
      return;

//    int vertexCount = 4*strokes.size();// + 4*2;
    int vertexCount = 2*strokes.size();// + 4*2;

    float[] coords = new float[3*vertexCount];
   
    int i = 0;
    for (Stroke stroke : strokes) {

      Point2f s = (stroke.getStart()).getInchCoordinates();
      Point2f e = (stroke.getEnd()).getInchCoordinates();

      Vector3f d = new Vector3f();
      Vector3f n = new Vector3f();

      if (stroke instanceof Segment) {

        d.set(e.x-s.x, e.y-s.y, 0.0f);
        d.normalize();
        n.cross(d, Z);

//        float t = (float) (-LINE_TWIDDLE/2 + LINE_TWIDDLE*Math.random());
        float t = 0.0f;
        
        coords[i++] = s.x + t*n.x;
        coords[i++] = s.y + t*n.y;
        coords[i++] = LINE_Z;

        coords[i++] = e.x + t*n.x;
        coords[i++] = e.y + t*n.y;
        coords[i++] = LINE_Z;
/* (tail)
        coords[i++] = e.x + t*n.x;
        coords[i++] = e.y + t*n.y;
        coords[i++] = LINE_Z;

        double a =
          MIN_TAIL_ANGLE + (MAX_TAIL_ANGLE-MIN_TAIL_ANGLE)*Math.random();
        float sa = (float) Math.sin(a);
        float ca = (float) Math.cos(a);

        coords[i++] = e.x+t*n.x+(n.x*sa-d.x*ca)*TAIL_SIZE;
        coords[i++] = e.y+t*n.y+(n.y*sa-d.y*ca)*TAIL_SIZE;
        coords[i++] = LINE_Z;
*/
      } else {
        System.out.println("WARNING: unsupported stroke: " + stroke);
      }
    }
/*
    Rect bounds = getBounds();

    coords[i++] = (float) bounds.x;
    coords[i++] = (float) bounds.y;
    coords[i++] = LINE_Z;

    coords[i++] = (float) (bounds.x + bounds.width);
    coords[i++] = (float) bounds.y;
    coords[i++] = LINE_Z;

    coords[i++] = (float) (bounds.x + bounds.width);
    coords[i++] = (float) bounds.y;
    coords[i++] = LINE_Z;

    coords[i++] = (float) (bounds.x + bounds.width);
    coords[i++] = (float) (bounds.y + bounds.height);
    coords[i++] = LINE_Z;

    coords[i++] = (float) (bounds.x + bounds.width);
    coords[i++] = (float) (bounds.y + bounds.height);
    coords[i++] = LINE_Z;

    coords[i++] = (float) bounds.x;
    coords[i++] = (float) (bounds.y + bounds.height);
    coords[i++] = LINE_Z;

    coords[i++] = (float) bounds.x;
    coords[i++] = (float) (bounds.y + bounds.height);
    coords[i++] = LINE_Z;

    coords[i++] = (float) bounds.x;
    coords[i++] = (float) bounds.y;
    coords[i++] = LINE_Z;
*/

//    System.out.println(coords.length/6 + " segments");
//
//    for (i = 0; i < coords.length/6; i++)
//      System.out.println("(" + coords[6*i] + ", " + coords[6*i+1] + ") -> (" +
//                         coords[6*i+3] + ", " + coords[6*i+4] + ")");

    lineGeometry = new LineArray(vertexCount, GeometryArray.COORDINATES);
    lineGeometry.setCoordinates(0, coords);
  }

  private void makePointGeometry() {

    if (pads.isEmpty())
      return;

    int vertexCount = pads.size();

    float[] coords = new float[3*vertexCount];
   
    int i = 0;
    for (Iterator<Flash> it = pads.iterator(); it.hasNext(); ) {
      Point2f p = (it.next().getLocation()).getInchCoordinates();

      coords[i++] = p.x;
      coords[i++] = p.y;
      coords[i++] = POINT_Z;

    }

    pointGeometry = new PointArray(vertexCount, GeometryArray.COORDINATES);
    pointGeometry.setCoordinates(0, coords);
  }

  private void makeConeGeometry() {

    if (pads.isEmpty())
      return;

    // The order of this must be the same over multiple loops.
    List<TriangleFanArray> parts = new ArrayList<TriangleFanArray>();

    for (Flash flash : pads) {

      Collection<GeometryArray> geometries = flash.getGeometries();

      if (geometries == null) {
        continue;
      }

      for (GeometryArray geometry : geometries) {

        if (geometry == null) {
          continue;
        }

        if (geometry instanceof TriangleFanArray) {
          //System.out.println("Supported: " + flash.toString());
          parts.add((TriangleFanArray) geometry);
        } else {
          System.out.println("Not supported for voronoi: " + flash.toString());
        }
      }
    }

    if (parts.size() != 0) {
      int vertexCount = 0;
      int[] vertexCounts = new int[parts.size()];
      int i = 0;

      for (TriangleFanArray part : parts) {

        // This gives us 1 for the center + the number of segments, ...
        int partVertexCount = part.getVertexCount();
        // ... but cones have no center and two vertices per segment:
        partVertexCount--;
        partVertexCount *= 2;
        vertexCount += partVertexCount;
        vertexCounts[i++] = partVertexCount;
      }

      float[] coords = new float[vertexCount*3];

      i = 0;
      for (int j = 0; j < parts.size(); j++) {

        TriangleFanArray part = parts.get(j);
        // This gives us all the coordinates for a
        // flat fan, sized to draw the pad properly.
        float[] partCoords = part.getCoordRefFloat();

        float[] center = new float[2];
        center[0] = partCoords[0];
        center[1] = partCoords[1];
        for (int k = 3; k < part.getVertexCount() * 3; k += 3) {
          // To get a proper cone, we don't take over the center, take over the
          // outer fan vertices as is for the top and offset the same vertices
          // by zCeiling() for the bottom.
          //
          // Additionally, the top vertices get projected to CONE_Z_MAX,
          // the bottom ones to CONE_Z_MAX-zCeiling().

          coords[i++] = partCoords[k];
          coords[i++] = partCoords[k+1];
          coords[i++] = CONE_Z_MAX;

          // TODO: *sigh* After coding this I suddenly recognized this
          // isn't an offset, but sort of a scaling.
          // For a circle or a square, this scaling is identical to the
          // wanted offset, though. For obounds, the shorter extents is too small.
          float dx = partCoords[k] - center[0];
          float dy = partCoords[k+1] - center[1];
          float l = (float)Math.sqrt(dx * dx + dy * dy);
          dx *= (zCeiling() + l) / l;
          dy *= (zCeiling() + l) / l;
          coords[i++] = center[0] + dx;
          coords[i++] = center[1] + dy;
          coords[i++] = CONE_Z_MAX - zCeiling();
        }
      }
      coneGeometry = new TriangleStripArray(vertexCount,
                                            GeometryArray.COORDINATES,
                                            vertexCounts);
      coneGeometry.setCoordinates(0, coords);
    }
  }

  private void makeLoopGeometry() {

    List<float[]> parts = new LinkedList<float[]>();

    //half edge loops
    for (HalfEdge startEdge : loopStarts) {

      HalfEdge he = startEdge;

      Point2f startPoint, endPoint;
      Vector3f dir = new Vector3f();
      Vector3f offTop = new Vector3f();
      Vector3f offBottom = new Vector3f();
      
// TODO: the edge between cone and flat part isn't entirely tight.
//       They should overlap a bit.
      do {
        
        Stroke stroke = he.getStroke();

        startPoint = (he.getStart()).getInchCoordinates();
        endPoint = (he.getEnd()).getInchCoordinates();

        if (stroke instanceof Segment) {

          float[] part = new float[4*3];

          dir.x = endPoint.x - startPoint.x;
          dir.y = endPoint.y - startPoint.y;
          dir.z = 0.0f;

          dir.normalize();
          
          offTop.cross(dir, Z); // offTop = normal to dir
          offBottom.set(offTop);
          
          float width = (float) (((Segment) stroke).getWidth() / 2);
          offTop.scale(width);
          offBottom.scale(width + zCeiling());

          int i = 0;
          part[i++] = startPoint.x + offTop.x;
          part[i++] = startPoint.y + offTop.y;
          part[i++] = LOOP_Z_MAX;

          part[i++] = startPoint.x + offBottom.x;
          part[i++] = startPoint.y + offBottom.y;
          part[i++] = LOOP_Z_MAX-zCeiling();

          part[i++] = endPoint.x + offTop.x;
          part[i++] = endPoint.y + offTop.y;
          part[i++] = LOOP_Z_MAX;
          
          part[i++] = endPoint.x + offBottom.x;
          part[i++] = endPoint.y + offBottom.y;
          part[i++] = LOOP_Z_MAX-zCeiling();
          
          parts.add(part);

        } else {
          System.out.println("WARNING: unsupported stroke: " + stroke);
        }
        
        HalfEdge next = he.getNext(); 

        if (next.getStroke() == stroke) {

          parts.add(makeCircleCone(endPoint.x, endPoint.y,
                                   endPoint.x + offTop.x, endPoint.y + offTop.y,
                                   endPoint.x + offBottom.x, endPoint.y + offBottom.y,
                                   (float) Math.PI));

        }
        else {

          double angle = he.angleTo(next);

          if (angle > Math.PI) {
            parts.add(makeCircleCone(endPoint.x, endPoint.y,
                                     endPoint.x + offTop.x, endPoint.y + offTop.y,
                                     endPoint.x + offBottom.x, endPoint.y + offBottom.y,
                                     (float) (angle - Math.PI)));
          }
        }

        he = next;
        
      } while (he != startEdge);
    }
      
    if (parts.isEmpty())
      return;
    
    //collect parts
    
    int vertexCount = 0;
    int numFans = parts.size();
    int[] vertexCounts = new int[numFans];
    
    int i = 0;
    for (float[] part : parts) {      
      vertexCount += part.length/3;
      vertexCounts[i++] = part.length/3;
    }
    
//    System.out.println(vertexCount + " vertices");
    
    float[] coords = new float[vertexCount*3];
    
    i = 0;
    for (float[] part : parts) {
      
      int len = part.length;
      
      System.arraycopy(part, 0, coords, i, len);
      
      i += len;
    }
    
    loopGeometry = new TriangleStripArray(vertexCount,
                                          GeometryArray.COORDINATES,
                                          vertexCounts);
    loopGeometry.setCoordinates(0, coords);
  }

  private void makeVoronoiGeometry() {
    makeConeGeometry();
    makeLoopGeometry();
  }

  private float[] makeCircleCone(float centerX, float centerY,
                                 float topX, float topY,
                                 float bottomX, float bottomY,
                                 float angle) {
 
    int n = (int) Math.ceil(angle/CIRCLE_SECTOR);

    double sector = angle/n;

    float[] coord = new float[3*2*(n+1)];

    int i = 0;
    
    Transform3D t3d = new Transform3D();
    t3d.rotZ(sector);

    Point3f pt = new Point3f(topX - centerX, topY - centerY, LOOP_Z_MAX);
    Point3f pb = new Point3f(bottomX - centerX, bottomY - centerY, LOOP_Z_MAX-zCeiling());

    coord[i++] = topX;
    coord[i++] = topY;
    coord[i++] = LOOP_Z_MAX;
    
    coord[i++] = bottomX;
    coord[i++] = bottomY;
    coord[i++] = LOOP_Z_MAX-zCeiling();
    
    for (int j = 0; j < n; j++) {

      t3d.transform(pt);
      t3d.transform(pb);
      
      coord[i++] = pt.x + centerX;
      coord[i++] = pt.y + centerY;
      coord[i++] = LOOP_Z_MAX;
      
      coord[i++] = pb.x + centerX;
      coord[i++] = pb.y + centerY;
      coord[i++] = LOOP_Z_MAX-zCeiling();
    }

    return coord;
  }

  private void makeFlatGeometry() {

    if (Float.isNaN(flatZ))
      flatZ = (float) (FLAT_Z_MAX + Math.random()*(FLAT_Z_MIN-FLAT_Z_MAX));

//    System.out.println("flatZ = " + flatZ);

    Collection<TriangleFanArray> fanParts = new LinkedHashSet<TriangleFanArray>();
    Collection<GeometryArray> nonFanParts = new LinkedHashSet<GeometryArray>();
    
//    System.out.println(strokes.size() + " strokes");

    for (Stroke stroke : strokes) {

//      System.out.println(stroke.toString());

      Collection<GeometryArray> geometries = stroke.getGeometries();

      if (geometries == null) {
        continue;
      }

      for (GeometryArray geometry : geometries) {

        if (geometry == null) {
          continue;
        }

        if (geometry instanceof TriangleFanArray) {
          fanParts.add((TriangleFanArray) geometry);
        } else {
          nonFanParts.add(geometry);
        }
      }
    }

//    System.out.println(fanParts.size() + " fan; " +
//                       nonFanParts.size() + " nonfan");

//    System.out.println(pads.size() + " pads");

    for (Flash flash : pads) {

//      System.out.println(flash.toString());

      Collection<GeometryArray> geometries = flash.getGeometries();
      
      if (geometries == null) {
        continue;
      }
      
      for (GeometryArray geometry : geometries) {
        
        if (geometry == null) {
          continue;
        }
      
        if (geometry instanceof TriangleFanArray) {
          fanParts.add((TriangleFanArray) geometry);
        } else {
          nonFanParts.add(geometry);
        }
      }
    }
    
//    System.out.println(fanParts.size() + " fan; " +
//                       nonFanParts.size() + " nonfan");

    if (fanParts.size() != 0) {

      int numFans = fanParts.size();
      int[] vertexCounts = new int[numFans];
      int vertexCount = computeVertexCounts(fanParts, vertexCounts);
      float[] coords = new float[vertexCount*3];
      populateCoords(fanParts, coords);
      
      flatFanGeometry = new TriangleFanArray(vertexCount,
                                             GeometryArray.COORDINATES,
                                             vertexCounts);
      flatFanGeometry.setCoordinates(0, coords);
    }

    if (nonFanParts.size() != 0) {
      
      int vertexCount = 0;
      for (Iterator<GeometryArray> it = nonFanParts.iterator(); it.hasNext(); ) {
        vertexCount += ((GeometryArray) it.next()).getVertexCount();
      }
 

      float[] coords = new float[vertexCount*3];
      populateCoords(nonFanParts, coords);

//      System.out.println(vertexCount + " non-fan verts:");
//      for (int i = 0; i < vertexCount; i++)
//        System.out.println("  (" + coords[3*i] +
//                           ", " + coords[3*i+1] +
//                           ", " + coords[3*i+2] + ")");

      flatNonFanGeometry = new TriangleArray(vertexCount,
                                             GeometryArray.COORDINATES);
      flatNonFanGeometry.setCoordinates(0, coords);
    }
  }

  private int computeVertexCounts(Collection<? extends GeometryArray> parts, int[] vertexCounts) {

    int total = 0;
    int i = 0;
    for (GeometryArray part : parts) {
      
      int vertexCount = part.getVertexCount();
      total += vertexCount;
      vertexCounts[i++] = vertexCount;
    }
  
    return total;
  }

  private void populateCoords(Collection<? extends GeometryArray> parts, float[] coords) {

    int i = 0;
    for (GeometryArray gpart : parts) {
      
      float[] part = gpart.getCoordRefFloat();
      
      int len = part.length;
      System.arraycopy(part, 0, coords, i, len);
      i += len;
    }
    
    for (i = 0; i < coords.length/3; i++) {
      coords[3*i+2] = flatZ;
    }
  }
                                                                   
  
  public Rect getBounds() {

    if (bounds == null) {
      
      bounds = new Rect();

      for (Iterator<Stroke> it = strokes.iterator(); it.hasNext(); ) {

        Rect strokeBounds = (it.next()).getBounds();
        
//        System.out.println("adding stroke bounds: " + strokeBounds);
//        System.out.println("to net bounds: " + bounds);
        bounds.add(strokeBounds);
//        System.out.println("result: " + bounds);
      }

      for (Iterator<Flash> it = pads.iterator(); it.hasNext(); )
        bounds.add(it.next().getBounds());

//      System.out.println("net bounds: " + bounds);
    }

    return bounds;
  }

  private float zCeiling() {

    if (Float.isNaN(zCeiling))
      zCeiling = (float) (visolate.getModel()).getMaxDimension();

    return zCeiling;
  }

  public void setZCeiling(double zCeiling) {

    if (this.zCeiling == zCeiling)
      return;

    this.zCeiling = (float) zCeiling;

    rebuildVoronoiGeometry();
  }

  public int compareTo(final Net o) {

//    if (!(o instanceof Net))
//      return 0;

    float otherFlatZ = ((Net) o).flatZ;

    if (flatZ > otherFlatZ) {
      return -1;
    }
    
    if (flatZ < otherFlatZ) {
      return 1;
    }

    return 0;
  }

  private Visolate visolate;

  private Collection<Stroke> strokes = new LinkedList<Stroke>();
  private Collection<HalfEdge> loopStarts = new LinkedList<HalfEdge>();
  private LinkedHashSet<HalfEdge> halfEdges = new LinkedHashSet<HalfEdge>();
  private LinkedHashSet<Flash> pads = new LinkedHashSet<Flash>();

  private ColoringAttributes coloringAttributes = null;
  private Color3b color;

  private GeometryArray lineGeometry = null;
  private GeometryArray pointGeometry = null;
  private GeometryArray coneGeometry = null;
  private GeometryArray loopGeometry = null;
  private GeometryArray flatFanGeometry = null;
  private GeometryArray flatNonFanGeometry = null;

  private boolean showLineGeometry = false;
  private boolean showVoronoiGeometry = false;
  private boolean showFlatGeometry = false;

  private Shape3D lineS3D = null;
  private Shape3D pointS3D = null;
  private Shape3D coneS3D = null;
//  private Shape3D coneEdgesS3D = null;
  private Shape3D loopS3D = null;
//  private Shape3D loopEdgesS3D = null;
  private Shape3D flatFanS3D = null;
  private Shape3D flatNonFanS3D = null;

  private BranchGroup sceneBG = null;

  private Rect bounds = null;

  private float zCeiling = Float.NaN;

  private float flatZ = Float.NaN;

  private Appearance appearance2D;
  private TransparencyAttributes transparencyAttributes;

  private Set<Net> mySuperNet = null;

  private double offset = 0.0;

  private boolean translucent2D = false;

  private int areaDPI;
  private int areaPixels;

  private boolean highlighted = false;
}
