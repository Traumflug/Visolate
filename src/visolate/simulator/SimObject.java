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

import visolate.misc.*;

public abstract class SimObject {

  public Rect getBounds() {

    if (bounds == null)
      makeBounds();

    return bounds;
  }

  protected abstract void makeBounds();

  public Collection<GeometryArray> getGeometries() {
    if (geometries == null) {
      makeGeometries();
    }

    return geometries;
  }

  protected abstract void makeGeometries();

  public double computeSignedOffset(double offset, boolean inverse) {
    return offset * ((inverse) ? -1.0 : 1.0);
  }

  public double getOffset() {
    return offset;
  }

  public void setOffset(double offset) {

    boolean changed = (this.offset != offset);

    this.offset = offset;
    signedOffset = computeSignedOffset(offset, inverse);

    if (changed) {
      bounds = null;
      geometries = null;
      offsetChanged();
    }
  }

  protected void offsetChanged() {
  }

  public boolean getInverse() {
    return inverse;
  }

  public void setInverse(boolean inverse) {

    boolean changed = (this.inverse != inverse);

    this.inverse = inverse;
    signedOffset = computeSignedOffset(offset, inverse);

    if (changed) {
      bounds = null;
      geometries = null;
      inverseChanged();
    }
  }

  protected void inverseChanged() {
  }

  protected static TriangleArray makeTA(float[] vertices) {

    if (vertices == null)
      return null;

    int numVertices = vertices.length / 3;

    if (numVertices < 3)
      return null;

    TriangleArray geometry = new MyTriangleArray(numVertices, GeometryArray.COORDINATES | GeometryArray.BY_REFERENCE);

    geometry.setCoordRefFloat(vertices);

    return geometry;
  }

  protected static TriangleArray makeTA() {
    return makeTA(null);
  }

  protected static TriangleFanArray makeTFA(final float[] vertices, int[] stripVertexCounts) {

    if (vertices == null) {
      return null;
    }

    int numVertices = vertices.length / 3;

    if (numVertices < 3) {
      return null;
    }

    if (stripVertexCounts == null) {
      stripVertexCounts = new int[] { numVertices };
    }

    TriangleFanArray geometry = new MyTriangleFanArray(numVertices,
        GeometryArray.COORDINATES | GeometryArray.BY_REFERENCE, stripVertexCounts);

    geometry.setCoordRefFloat(vertices);

    return geometry;
  }

  protected static TriangleFanArray makeTFA(float[] vertices) {
    return makeTFA(vertices, null);
  }

  protected static TriangleFanArray makeTFA() {
    return makeTFA(null);
  }

  protected static GeometryArray dupGeometry(final GeometryArray geometry, final float[] newCoords) {

    float[] coords = (newCoords != null) ? newCoords : geometry.getCoordRefFloat();

    if (geometry instanceof TriangleArray) {
      return makeTA(coords);
    } else if (geometry instanceof TriangleFanArray) {
      return makeTFA(coords);
    } else {
      throw new UnsupportedOperationException(geometry.getClass() + " unhandled");
    }
  }

  protected static GeometryArray dupGeometry(final GeometryArray geometry) {
    return dupGeometry(geometry, null);
  }

  protected static void translateGeometry(final GeometryArray geometry, final Vector2f t) {

    float[] coords = geometry.getCoordRefFloat();

    float[] newCoords = new float[coords.length];

    for (int i = 0; i < coords.length / 3; i++) {
      newCoords[3 * i + 0] = coords[3 * i + 0] + t.x;
      newCoords[3 * i + 1] = coords[3 * i + 1] + t.y;
      newCoords[3 * i + 2] = coords[3 * i + 2];
    }

    geometry.setCoordRefFloat(newCoords);
  }

  // This rotates a geometry around (0.0, 0.0).
  protected static void rotateGeometry(final GeometryArray geometry, final double angle) {

    float[] coords = geometry.getCoordRefFloat();

    float[] newCoords = new float[coords.length];

    Transform3D t = new Transform3D();
    t.rotZ(angle);

    Point3f p = new Point3f();

    for (int i = 0; i < coords.length; i += 3) {
      p.x = coords[i];
      p.y = coords[i + 1];
      p.z = coords[i + 2];

      t.transform(p);

      newCoords[i] = p.x;
      newCoords[i + 1] = p.y;
      newCoords[i + 2] = p.z;
    }

    geometry.setCoordRefFloat(newCoords);
  }

  public static int hashCode(final int[] a) {
    int c = 0;
    for (int i = 0; i < a.length; i++) {
      c = 31 * c + a[i];
    }
    return c;
  }

  public static int hashCode(float[] a) {
    int c = 0;
    for (int i = 0; i < a.length; i++) {
      c = 31 * c + Float.floatToRawIntBits(a[i]);
    }
    return c;
  }

  protected double offset = 0.0;
  protected double signedOffset = 0.0;
  protected boolean inverse = false;

  protected Rect bounds = null;
  protected Collection<GeometryArray> geometries = null;
}

class MyTriangleArray extends TriangleArray {

  public MyTriangleArray(int vertexCount, int vertexFormat) {
    super(vertexCount, vertexFormat);
    setupHash = vertexCount ^ (vertexFormat * 31);
    hashCode = setupHash;
  }

  public MyTriangleArray(int vertexCount, int vertexFormat, int texCoordSetCount, int[] texCoordSetMap) {
    super(vertexCount, vertexFormat, texCoordSetCount, texCoordSetMap);
    setupHash = vertexCount ^ (vertexFormat * 31);
    hashCode = setupHash;
  }

  public int hashCode() {
    return hashCode;
  }

  public boolean equals(Object o) {

    if (!(o instanceof MyTriangleArray))
      return false;

    MyTriangleArray other = (MyTriangleArray) o;

    boolean ret = ((other.getVertexCount() == getVertexCount()) && (other.getVertexFormat() == getVertexFormat())
        && Arrays.equals(myCoords, other.myCoords));

//    System.out.println((ret) ? "eq" : "neq");

    return ret;
  }

  public void setCoordRefFloat(float[] coords) {
    myCoords = coords;
    hashCode = SimObject.hashCode(myCoords) ^ (setupHash * 31);
    super.setCoordRefFloat(coords);
  }

  protected float[] myCoords;
  protected int setupHash;
  protected int hashCode;
}

class MyTriangleFanArray extends TriangleFanArray {

  public MyTriangleFanArray(int vertexCount, int vertexFormat, int[] stripVertexCounts) {
    super(vertexCount, vertexFormat, stripVertexCounts);
    setupHash = vertexCount ^ (vertexFormat * 31);
    setupHash = setupHash ^ (SimObject.hashCode(stripVertexCounts) * 31);
    hashCode = setupHash;
    myStripVertexCounts = stripVertexCounts;
  }

  public MyTriangleFanArray(int vertexCount, int vertexFormat, int texCoordSetCount, int[] texCoordSetMap,
      int[] stripVertexCounts) {
    super(vertexCount, vertexFormat, texCoordSetCount, texCoordSetMap, stripVertexCounts);
    setupHash = vertexCount ^ (vertexFormat * 31);
    setupHash = setupHash ^ (SimObject.hashCode(stripVertexCounts) * 31);
    hashCode = setupHash;
    myStripVertexCounts = stripVertexCounts;
  }

  public int hashCode() {
    return hashCode;
  }

  public boolean equals(Object o) {

    if (!(o instanceof MyTriangleFanArray)) {
      return false;
    }

    MyTriangleFanArray other = (MyTriangleFanArray) o;

    boolean ret = ((other.getVertexCount() == getVertexCount()) && (other.getVertexFormat() == getVertexFormat())
        && Arrays.equals(myStripVertexCounts, other.myStripVertexCounts) && Arrays.equals(myCoords, other.myCoords));

//    System.out.println((ret) ? "eq" : "neq");

    return ret;
  }

  public void setCoordRefFloat(float[] coords) {
    myCoords = coords;
    hashCode = SimObject.hashCode(myCoords) ^ (setupHash * 31);
    super.setCoordRefFloat(coords);
  }

  protected int[] myStripVertexCounts;
  protected float[] myCoords;
  protected int setupHash;
  protected int hashCode;
}
