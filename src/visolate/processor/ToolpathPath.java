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

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.LinkedList;

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

    return path.size();
  }

  public double length() {

    double length = 0;

    ToolpathNode prev = null;

    for (ToolpathNode node : path) {

      if (prev != null) {
        length += Util.distance(processor.toModelX(prev.x), processor.toModelY(prev.y),
                                processor.toModelX(node.x), processor.toModelY(node.y));
      }
      prev = node;
    }

    return length;
  }

  public Geometry getGeometry() {

    if (geometry == null) {

      Color3f color = Net.toColor3f(processor.visolate.getDisplay().getRandomColor());

      int vertexCount = path.size()*2;

      float[] coords = new float[vertexCount*6];

      int i = 0;

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

      geometry = new LineArray(vertexCount,
                               GeometryArray.COORDINATES |
                               GeometryArray.COLOR_3 |
                               GeometryArray.INTERLEAVED |
                               GeometryArray.BY_REFERENCE);
      geometry.setInterleavedVertices(coords);
    }

    return geometry;
  }

  // Used for debugging only. This draws a white point for each standard Node
  // and a red dot for each fixed node. Swap it against getGeometry() in
  // ToolpathsProcessor.getSceneGraph().
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

  public ToolpathNode getStartNode() {
    return (ToolpathNode) path.getFirst();
  }
  
  public ToolpathNode getEndNode() {
    return (ToolpathNode) path.getLast();
  }
  
  public void lockNode(ToolpathNode nodeToLock) {
    
    for (ToolpathNode node : path) {
      if (node.equals(nodeToLock))
        node.setIsLocked(true);
    }
  }
  
  public Point2d getStartPoint() {

    ToolpathNode start = (ToolpathNode) path.getFirst();
    return new Point2d(processor.toModelX(start.x), processor.toModelY(start.y));
  }

  public void writeGCode(GCodeFileWriter writer) throws IOException {

    writer.cutterUp();

    boolean first = true;

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
  }

  public void optimize() {
    
    // TODO: Pre-optimize by removing nodes which are on a straight line.
    //       Pretty simple, as such straight lines can be on X and Y only,
    //       so if three consecutive nodes have the same X or the same Y value,
    //       the middle one can be removed.
    //
    //       Start and end of longer straight lines should be fixed at start
    //       and end, so no almost-horizontal and almost-vertical lines appear?
    
    LinkedList<ToolpathNode> optimizedPath = new LinkedList<ToolpathNode>();
    optimizedPath.add(path.get(0));
    
    // Proceed until there are no nodes left.
    while (path.size() > 1) {
      int end = 0;

      // At most, a segment can go to the next locked node.
      for (end = 1; end < path.size(); end++) {
        if (path.get(end).isLocked())
          break;
      }

      while (end > 0) {
        
        int segmentEnd = end;
        
        // This algorithm lays a single path/segment from start to end, then
        // looks wether this is within tolerance. If not, it tries again with
        // the first half of the nodes, then with the first quarter ... and so on.
        //
        // As soon as a segment is within tolerance, the segment is used and the
        // process is repeated with the remaining set of nodes.
        //
        // Stepping from the start node to each subsequent node until one of the
        // nodes in between is out of tolerance would produce some fewer segments,
        // but also require a lot more processing power.
        while (true) {

          int i;
          
          // Fully closed paths require at least two segments.
          if (path.get(0).x == path.get(segmentEnd).x &&
              path.get(0).y == path.get(segmentEnd).y) {
            segmentEnd /= 2;
          }
          
          Line2D line = new Line2D.Float(processor.toModelX(path.get(0).x),
                                         processor.toModelY(path.get(0).y),
                                         processor.toModelX(path.get(segmentEnd).x),
                                         processor.toModelY(path.get(segmentEnd).y));
          
          for (i = 1; i < segmentEnd; i++) {
            if (line.ptLineDist(processor.toModelX(path.get(i).x),
                                processor.toModelY(path.get(i).y)) > straightTol)
              break;
          }
          
          if (i == segmentEnd) {
            // No intermediate node was out of tolerance -> make the segment.
            optimizedPath.add(path.get(i));
            
            // Remove processed nodes.
            for ( ; segmentEnd > 0; segmentEnd--, end--) {
              path.removeFirst();
            }
            
            break;
          }
          else {
            // Try again with half the distance.
            segmentEnd /= 2;
          }
        }
      }
    }
    
    path = optimizedPath;
  }

  private ToolpathsProcessor processor = null;
  
  int[] dir = new int[2];

  final int HEAD = 0;
  final int TAIL = 1;

  private LinkedList<ToolpathNode> path = new LinkedList<ToolpathNode>();

  GeometryArray geometry;
  
  private double straightTol;

}
