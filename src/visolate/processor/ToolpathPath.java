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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.PointArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point2d;

import visolate.misc.Util;
import visolate.model.Net;
import visolate.processor.ToolpathsProcessor;
import visolate.processor.ToolpathNode;
import visolate.processor.GCodeFileWriter;

public class ToolpathPath {

  public static final double[] HORIZ_DIR_BIAS = {-1, 1, 1, -1};
  public static final double[] VERT_DIR_BIAS = {1, 1, -1, -1};

  ToolpathPath(final ToolpathsProcessor processor, final ToolpathNode seed) {

    this.processor = processor;
    //      this.seed = seed;

    path.add(seed);

    dir[HEAD] = -1;
    dir[TAIL] = -1;

    switch (seed.numNeighbors()) {

    case 0: {
      return;
    }

    case 1: {
      for (int i = 0; i < 4; i++) {
        if (seed.getNeighbor(i) != null) {
          dir[TAIL] = i;
          break;
        }
      }
      break;
    }

    case 2: {
      boolean tailSet = false;
      for (int i = 0; i < 4; i++) {
        if (seed.getNeighbor(i) != null) {
          if (!tailSet) {
            dir[TAIL] = i;
            tailSet = true;
          } else {
            dir[HEAD] = i;
            break;
          }
        }
      }
      break;
    }

    case 3: {
      for (int i = 0; i < 4; i++) {
        if ((seed.getNeighbor(i) != null) &&
            (seed.getNeighbor(oppositeDir(i)) != null)) {
          dir[TAIL] = i;
          dir[HEAD] = oppositeDir(i);
          break;
        }
      }
      break;
    }

    case 4: {
      dir[HEAD] = ToolpathNode.N;
      dir[TAIL] = ToolpathNode.S;
      break;
    }
    }

    while ((dir[TAIL] >= 0) && extendTail())
      ;

    while ((dir[HEAD] >= 0) && extendHead())
      ;

  }

  public void setStraightTolerance(final double tolerance) {
    straightTol = tolerance;
  }
  
  private boolean extendTail() {

    ToolpathNode next = getNext((ToolpathNode) path.getLast(), TAIL);

    if (next == null)
      return false;

    path.addLast(next);

    return true;
  }

  private boolean extendHead() {

    ToolpathNode next = getNext((ToolpathNode) path.getFirst(), HEAD);

    if (next == null)
      return false;

    path.addFirst(next);

    return true;
  }

  private ToolpathNode getNext(ToolpathNode n, int whichDir) {

    int d = dir[whichDir];

    ToolpathNode next = n.getNeighbor(d);

    if (next == null) {

      for (int i = 0; i < 4; i++) {

        if (i == d)
          continue;

        if (i == oppositeDir(d))
          continue;

        ToolpathNode neighbor = n.getNeighbor(i);

        if (neighbor != null) {

          if (next != null)
            return null;

          next = neighbor;
          d = dir[whichDir] = i;
        }
      }
    }

    if (next == null)
      return null;

    n.setNeighbor(d, null);
    next.setNeighbor(oppositeDir(d), null);

    if (n.numNeighbors() == 0)
      processor.nodes.remove(n);

    if (next.numNeighbors() == 0)
      processor.nodes.remove(next);

    return next;
  }
  
  private int oppositeDir(int d) {
    switch(d) {
    case ToolpathNode.N:
      return ToolpathNode.S;
    case ToolpathNode.S:
      return ToolpathNode.N;
    case ToolpathNode.W:
      return ToolpathNode.E;
    case ToolpathNode.E:
      return ToolpathNode.W;
    default:
      return -1;
    }
  }

  public int numPathNodes() {

    if (optimalPathEnd == null)
      return path.size();

    int n = 0;

    for (PathNode node = optimalPathEnd;
        node != null;
        node = node.getBestPrev())
      n++;

    return n;
  }

  public double length() {

    double length = 0;

    if (optimalPathEnd == null) {

      ToolpathNode prev = null;

      for (ToolpathNode node : path) {

        if (prev != null) {
          length += Util.distance(processor.toModelX(prev.x), processor.toModelY(prev.y),
                                  processor.toModelX(node.x), processor.toModelY(node.y));
        }
        prev = node;
      }

    } else {

      PathNode prev = null;

      for (PathNode node = optimalPathEnd;
          node != null;
          node = node.getBestPrev()) {

        if (prev != null)
          length += Util.distance(prev.x, prev.y, node.x, node.y);

        prev = node;
      }
    }

    return length;
  }

  public Geometry getGeometry() {

    if (geometry == null) {

      Color3f color = Net.toColor3f(processor.visolate.getDisplay().getRandomColor());

      int vertexCount = path.size()*2;

      float[] coords = new float[vertexCount*6];

      int i = 0;

      if (optimalPathEnd == null) {

        ToolpathNode prev = null;

        for (ToolpathNode node : path) {

          if (prev != null) {

            coords[i++] = color.x;
            coords[i++] = color.y;
            coords[i++] = color.z;

            coords[i++] = processor.toModelX(prev.x);
            coords[i++] = processor.toModelY(prev.y);
            coords[i++] = Net.PATH_Z;

            coords[i++] = color.x;
            coords[i++] = color.y;
            coords[i++] = color.z;

            coords[i++] = processor.toModelX(node.x);
            coords[i++] = processor.toModelY(node.y);
            coords[i++] = Net.PATH_Z;
          }

          prev = node;
        }

      } else {

        PathNode prev = null;

        for (PathNode node = optimalPathEnd;
            node != null;
            node = node.getBestPrev()) {

          if (prev != null) {

            coords[i++] = color.x;
            coords[i++] = color.y;
            coords[i++] = color.z;

            coords[i++] = prev.x;
            coords[i++] = prev.y;
            coords[i++] = Net.PATH_Z;

            coords[i++] = color.x;
            coords[i++] = color.y;
            coords[i++] = color.z;

            coords[i++] = node.x;
            coords[i++] = node.y;
            coords[i++] = Net.PATH_Z;
          }

          prev = node;
        }
      }

      geometry = new LineArray(vertexCount,
                               GeometryArray.COORDINATES |
                               GeometryArray.COLOR_3 |
                               GeometryArray.INTERLEAVED |
                               GeometryArray.BY_REFERENCE);
      geometry.setInterleavedVertices(coords);
    }

    return geometry;
  }

  // Used for debugging only. This draws a white point for each Node.
  // Swap it against getGeometry() in PathNode.getSceneGraph().
  @SuppressWarnings("unused")
  public Geometry getPointGeometry() {
    
    final float[] normalColor = { 1.0f, 1.0f, 1.0f };
    final float[] fixedColor = { 1.0f, 0.0f, 0.0f };
    
    int vertexCount = path.size();
    float[] coords = new float[vertexCount*6];

    int i = 0;

    ToolpathNode prev = null;
    for (ToolpathNode node : path) {

      if (prev != null) {

        if (node.locked) {
          coords[i++] = fixedColor[0];
          coords[i++] = fixedColor[1];
          coords[i++] = fixedColor[2];
        }
        else {
          coords[i++] = normalColor[0];
          coords[i++] = normalColor[1];
          coords[i++] = normalColor[2];
        }
    
        coords[i++] = processor.toModelX(prev.x);
        coords[i++] = processor.toModelY(prev.y);
        coords[i++] = Net.PATH_Z;

      }

      prev = node;
    }

    GeometryArray pointGeometry = new PointArray(vertexCount,
                                                 GeometryArray.COORDINATES |
                                                 GeometryArray.COLOR_3 |
                                                 GeometryArray.INTERLEAVED |
                                                 GeometryArray.BY_REFERENCE);
    pointGeometry.setInterleavedVertices(coords);

    return pointGeometry;
  }

  public Point2d getStartPoint() {

    if (optimalPathEnd == null) {
      ToolpathNode start = (ToolpathNode) path.getFirst();
      return new Point2d(processor.toModelX(start.x), processor.toModelY(start.y));
    } else {
      return new Point2d(optimalPathEnd.x, optimalPathEnd.y);
    }
  }
  
  public void writeGCode(GCodeFileWriter writer) throws IOException {

    writer.cutterUp();

    boolean first = true;

    if (optimalPathEnd == null) {

      for (ToolpathNode node : path) {
        Point2d p = new Point2d(processor.toModelX(node.x),
                                processor.toModelY(node.y));

        if (first) {
          writer.rapidMovement(p); //rapid to start
          writer.cutterDown();
          first = false;
        } else {
          writer.linearMovement(p);
        }
      }

    } else {

      for (PathNode node = optimalPathEnd; node != null; node = node.getBestPrev()) {
        Point2d p = new Point2d(node.x, node.y);

        if (first) {
          writer.rapidMovement(p); //rapid to start
          writer.cutterDown();
          first = false;
        } else {
          writer.linearMovement(p);
        }
      }
    }
  }

  public void optimize() {
    //      System.out.println("init optimal");
    initOptimalPath();
    //      System.out.println("add potential segs");
    addPotentialSegments();
    //      System.out.println("compute topo");
    computeTopologicallyOptimalPaths();
    //      System.out.println("compute weights");
    computeSegmentWeights();
    //      System.out.println("find optimal");
    findOptimalPath();
  }

  private void initOptimalPath() {

    PathNode prev = null;
    int i = 0;
    for (Iterator<ToolpathNode> it = path.iterator(); it.hasNext(); ) {
      PathNode node = new PathNode((ToolpathNode) it.next(), prev, i++);

      if (optimalPathStart == null) {
        optimalPathStart = node;
      }

      prev = node;
    }

    optimalPathEnd = prev;
  }

  private void addPotentialSegments() {

    //      boolean[] usedDir = new boolean[4];

    PathNode next = null;
    PathNode nextStart = null;
    for (PathNode start = optimalPathStart;
        start != null;
        start = nextStart) {

      nextStart = next = start.getFirstNext();

      if (next == null)
        break;

      PathNode prev = null;

      for (Sector sector = new Sector(start, next);
          (next != null);
          next = next.getFirstNext()) {

        sector.intersectWithSectorTo(next);

        if (sector.isEmpty())
          break;

        if (prev != null)
          nextStart.addNext(prev);

        prev = next;
      }
    }
  }

  //CLR 25.4: single-source shortest paths in directed acyclic graphs
  private void computeTopologicallyOptimalPaths() {

    for (PathNode node = optimalPathStart;
        node != null;
        node = node.getFirstNext())
      node.d = Double.POSITIVE_INFINITY;

    if (optimalPathStart != null)
      optimalPathStart.d = 0;

    for (PathNode node = optimalPathStart;
        node != null;
        node = node.getFirstNext()) {

      for (PathNode next : node.nexts) {

        double newD = node.d + 1.0;

        if (newD < next.d) {
          next.clearPrevs();
          next.addPrev(node);
          next.d = newD;
        } else if (newD == next.d) {
          next.addPrev(node);
        }
      }
    }
  }

  private void computeSegmentWeights() {

    int n = path.size();

    Sx = new double[n];
    Sy = new double[n];

    Sxx = new double[n];
    Syy = new double[n];

    Sxy = new double[n];

    double x = 0;
    double y = 0;

    double xx = 0;
    double yy = 0;

    double xy = 0;

    int i = 0;

    for (PathNode node = optimalPathStart;
        node != null;
        node = node.getFirstNext()) {

      x += node.x;
      y += node.y;

      xx += node.x*node.x;
      yy += node.y*node.y;

      xy += node.x*node.y;

      i = node.index;

      Sx[i] = x;
      Sy[i] = y;

      Sxx[i] = xx;
      Syy[i] = yy;

      Sxy[i] = xy;
    }
  }

  private double Ex(int i, int j) {
    return (Sx[j] - Sx[i])/((double) (j-i));
  }

  private double Ey(int i, int j) {
    return (Sy[j] - Sy[i])/((double) (j-i));
  }

  private double Exx(int i, int j) {
    return (Sxx[j] - Sxx[i])/((double) (j-i));
  }

  private double Eyy(int i, int j) {
    return (Syy[j] - Syy[i])/((double) (j-i));
  }

  private double Exy(int i, int j) {
    return (Sxy[j] - Sxy[i])/((double) (j-i));
  }

  private double segmentWeight(PathNode from, PathNode to) {

    double x = to.x - from.x;
    double y = to.y - from.y;

    double xAvg = (to.x + from.x)/2.0;
    double yAvg = (to.y + from.y)/2.0;

    int i = from.index;
    int j = to.index;

    double a = Exx(i, j) - 2*xAvg*Ex(i, j) + xAvg*xAvg;
    double b = Exy(i, j) - xAvg*Ex(i, j) - yAvg*Ey(i, j) + xAvg*yAvg;
    double c = Eyy(i, j) - 2*yAvg*Ey(i, j) + yAvg*yAvg;

    return Math.sqrt(c*x*x + 2*b*x*y + a*y*y);
  }

  private void findOptimalPath() {

    for (PathNode node = optimalPathStart; node != null; node = node.getFirstNext()) {
      node.d = Double.POSITIVE_INFINITY;
    }

    if (optimalPathStart != null) {
      optimalPathStart.d = 0;
    }

    for (PathNode node = optimalPathStart;
        node != null;
        node = node.getFirstNext()) {

      for (PathNode next : node.nexts) {

        if (!next.hasPrev(node)) {
          continue;
        }

        double newD = node.d + segmentWeight(node, next);

        if (newD < next.d) {
          next.optimalPrev = node;
          next.d = newD;
        }
      }
    }
  }

  private ToolpathsProcessor processor = null;
  
  int[] dir = new int[2];

  final int HEAD = 0;
  final int TAIL = 1;

  private LinkedList<ToolpathNode> path = new LinkedList<ToolpathNode>();

  GeometryArray geometry;

  //    Node seed;

  PathNode optimalPathStart = null;
  PathNode optimalPathEnd = null;

  double[] Sx;
  double[] Sy;

  double[] Sxx;
  double[] Syy;

  double[] Sxy;
  
  private double straightTol;

  
  private class PathNode {
    PathNode(ToolpathNode node, PathNode prev, int index) {

      x = processor.toModelX(node.x);
      y = processor.toModelY(node.y);

      this.index = index;

      if (prev != null)
        prev.addNext(this);

      d = Double.POSITIVE_INFINITY;
    }

    void addNext(PathNode node) {
      nexts.add(node);
    }

    PathNode getFirstNext() {

      if (nexts.isEmpty()) {
        return null;
      }

      //      return (PathNode) nexts.getFirst();
      return (PathNode) nexts.get(0);
    }

    void addPrev(PathNode node) {
      prevs.add(node);
      //      if (prevs.size() > 1)
      //        System.out.println("more than one prev from (" + x + ", " + y + ")");
    }

    boolean hasPrev(PathNode node) {
      return prevs.contains(node);
    }

    PathNode getBestPrev() {

      if (optimalPrev != null) {
        return optimalPrev;
      }

      if (prevs.isEmpty()) {
        return null;
      }

      return (PathNode) prevs.iterator().next();
    }

    void clearPrevs() {
      prevs.clear();
    }

    float x;
    float y;

    double d;

    ArrayList<PathNode> nexts = new ArrayList<PathNode>();
    Set<PathNode> prevs = new LinkedHashSet<PathNode>();

    PathNode optimalPrev = null;

    int index;
  }
  
  private class Sector {
    
    Sector(PathNode apex, PathNode first) {

      apexX = apex.x;
      apexY = apex.y;

      computeAnglesTo(first);

      startAngle = startAngleTo;
      endAngle = endAngleTo;
    }

    void intersectWithSectorTo(PathNode node) {

      computeAnglesTo(node);

      startAngle = Math.max(startAngle, startAngleTo);
      endAngle = Math.min(endAngle, endAngleTo);
    }

    boolean isEmpty() {
      return startAngle > endAngle;
    }

    private void computeAnglesTo(PathNode node) {

      for (int i = 0; i < 4; i++)
        angle[i] =
          Util.canonicalAngle(
              (node.x + HORIZ_DIR_BIAS[i]*straightTol) - apexX,
              (node.y + VERT_DIR_BIAS[i]*straightTol) - apexY);

      startAngleTo = Double.POSITIVE_INFINITY;
      endAngleTo = Double.NEGATIVE_INFINITY;

      for (int i = 0; i < 4; i++) {
        startAngleTo = Math.min(startAngleTo, angle[i]);
        endAngleTo = Math.max(endAngleTo, angle[i]);
      }
    }

    double[] angle = new double[4];

    double startAngleTo;
    double endAngleTo;

    double startAngle;
    double endAngle;

    double apexX;
    double apexY;
  }
}
