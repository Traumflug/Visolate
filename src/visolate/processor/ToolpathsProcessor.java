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

import java.awt.image.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.*;
import java.io.*;

import visolate.*;
import visolate.model.*;
import visolate.processor.ToolpathNode;
import visolate.processor.ToolpathPath;
import visolate.processor.GCodeFileWriter;
import visolate.processor.GCodeFileWriter.GCodeStroke;

/**
 * The ToolpathsProcessor generates the content for a  g-code file.
 */
public class ToolpathsProcessor extends MosaicProcessor {

	public static final Color3f ORIGIN_COLOR = new Color3f(1.0f, 0.0f, 1.0f);
	public static final float ORIGIN_TICK = 0.1f;

	public static final int VORONOI_MODE = 0;
	public static final int OUTLINE_MODE = 1;

	public ToolpathsProcessor(final Visolate visolate, final int mode) {
		super(visolate);
		this.mode = mode;
	}

	public void processTile(int r, int c,
			int ulx, int uly,
			int width, int height,
			double left, double bottom,
			double right, double top) {

		super.processTile(r, c,
				ulx, uly,
				width, height,
				left, bottom, right, top);
	}

	protected void processStarted() {
		super.processStarted();

		java.awt.image.Raster raster = mosaic.getRaster();
		buffer = raster.getDataBuffer();

		switch (mode) {
    // The sender will restore these when done.
		case VORONOI_MODE:
			System.out.println("generating voronoi toolpaths");
			model.enableBorderGeometry(true);
			model.enableLineGeometry(false);
			model.enableVoronoiGeometry(true);
			model.enableFlatGeometry(true);
			model.enableGCodeGeometry(false);
			break;
		case OUTLINE_MODE:
			System.out.println("generating outline toolpaths");
			model.enableBorderGeometry(false);
			model.enableLineGeometry(false);
			model.enableVoronoiGeometry(false);
			model.enableFlatGeometry(true);
			model.enableGCodeGeometry(false);
			break;
		default:
			 System.out.println("no mode!");
			 return;
		}

		model.setTranslucent2D(false);

		model.clearPaths();
		model.clearGCode();
	}

	protected void processCompleted() {

		super.processCompleted();

		tile = null;

		extractNodes();

		if (thread.isInterrupted())
			return;

		makePaths();

		if (thread.isInterrupted())
			return;
		
		lockCrossingNodes();
		
		if (thread.isInterrupted())
		  return;

		optimizePaths();

		if (thread.isInterrupted())
			return;

		model.setPaths(getSceneGraph());
	}

	private void extractNodes() {

		System.out.println("extracting nodes...");

		for (int y = 0; y < mosaicHeight; y++) {

			for (int x = 0; x < mosaicWidth; x++) {

				int color = getColor(x, y);
				int lColor = getColor(x-1, y);
				int uColor = getColor(x, y-1);

				ToolpathNode n = null;

				if ((x > 0) && (lColor != color)) {

					if (n == null)
						n = getNode(x, y);

					ToolpathNode o = getNode(x, y+1);

					n.south = o;

					if (o != null)
						o.north = n;
				}

				if ((y > 0) && (uColor != color)) {

					if (n == null)
						n = getNode(x, y);

					ToolpathNode o = getNode(x+1, y);

					n.east = o;

					if (o != null)
						o.west = n;
				}
			}

			if (thread.isInterrupted())
				return;
		}

		System.out.println(nodes.size() + " nodes");

		mosaic = null;
	}

	private void makePaths() {

		System.out.println("making paths...");

		Set<ToolpathNode> nodes = this.nodes.keySet();

		while (!nodes.isEmpty()) {
		  
			paths.add(new ToolpathPath(this, nodes.iterator().next()));

			if (thread.isInterrupted()) {
				return;
			}
		}

		System.out.println(paths.size() + " paths");

		reportPathStats();
	}

	private void lockCrossingNodes() {
	  
	   for (ToolpathPath pathToLock : paths) {
	     // Don't exclude pathToFix == path, as paths can be self-intersecting.
	     for (ToolpathPath path : paths) {
	       pathToLock.lockNode(path.getStartNode());
	       pathToLock.lockNode(path.getEndNode());
	     }
	   }
	}
	
	private void reportPathStats() {

		double length = 0;
		int segments = 0;
		for (ToolpathPath path : paths) {
			length += path.length();
			segments += path.numPathNodes();
		}

		System.out.println("total length: " + length);
		System.out.println("total segments: " + segments);
	}

	private void optimizePaths() {

		System.out.println("optimizing paths...");

		for (ToolpathPath path : paths) {
		  
      // TODO: This "straightTolerance" is actually an very important value,
      //       it tells the optimizer how close the optimized path should
      //       match the original, exact path. At such importance,
      //       it should definitely be user-configurable by a GUI text field.
		  path.setStraightTolerance(1.1/((double) dpi));
			path.optimize();

			if (thread.isInterrupted())
				return;
		}

		reportPathStats();
	}

	private int getColor(int x, int y) {

		if (x < 0)
			return 0;

		if (y < 0)
			return 0;

		if (x >= mosaicWidth)
			return 0;

		if (y >= mosaicHeight)
			return 0;

		return buffer.getElem(y*mosaicWidth + x) & 0xffffff;
	}

	private ToolpathNode getNode(int x, int y) {

		if (x < 0)
			return null;

		if (y < 0)
			return null;

		if (x >= mosaicWidth)
			return null;

		if (y >= mosaicHeight)
			return null;

		ToolpathNode key = new ToolpathNode(x, y);

		ToolpathNode node = (ToolpathNode) nodes.get(key);

		if (node == null) {
			node = key;
			nodes.put(key, node);
		}

		return node;
	}
	
	public BranchGroup getSceneGraph() {

		if (sceneBG == null) {

			Shape3D shape = new Shape3D();
			shape.setPickable(false);

			for (Iterator<ToolpathPath> it = paths.iterator(); it.hasNext(); ) {
				shape.addGeometry(it.next().getGeometry());
//				shape.addGeometry(it.next().getPointGeometry()); // nomally commented out
			}

			sceneBG = new BranchGroup();
			sceneBG.setPickable(false);
			sceneBG.setCapability(BranchGroup.ALLOW_DETACH);
			sceneBG.addChild(shape);
		}

		return sceneBG;
	}

	public float toModelX(int x) {
		return (float) (mosaicBounds.x + x/((float) dpi));
	}

	public float toModelY(int y) {
		return (float) (mosaicBounds.y + modelHeight-y/((float) dpi));
	}

	private ToolpathPath getClosestPath(Collection<ToolpathPath> paths, Point3d p) {

		double minDist = Double.POSITIVE_INFINITY;
		ToolpathPath closest = null;

		for (ToolpathPath path : paths) {

			double dist = new Point2d(p.x, p.y).distance(path.getStartPoint());

			if (dist < minDist) {
				minDist = dist;
				closest = path;
			}
		}

		return closest;
	}

	public void writeGCode(GCodeFileWriter w) throws IOException {

		model.clearGCode();

    w.preAmble();

		Collection<ToolpathPath> paths = new LinkedList<ToolpathPath>();
		paths.addAll(this.paths);

    while (!paths.isEmpty()) {
      
      ToolpathPath closestPath = getClosestPath(paths, w.getCurrentPosition());
      
      closestPath.writeGCode(w);
      paths.remove(closestPath);
    }
		
		w.postAmble();
		
		List<GCodeStroke>gCodeStrokes = w.getGCodeStrokes();

		int vertexCount = 2*gCodeStrokes.size() + 4;
		float[] coords = new float[6*vertexCount];

		Point3f p3f = new Point3f(0.0f, 0.0f, Net.GCODE_Z_MIN);

		int i = 0;

		for (GCodeStroke stroke : gCodeStrokes) {

			coords[i++] = stroke.color.x;
			coords[i++] = stroke.color.y;
			coords[i++] = stroke.color.z;

			coords[i++] = p3f.x;
			coords[i++] = p3f.y;
			coords[i++] = p3f.z;

			p3f = stroke.target;

			coords[i++] = stroke.color.x;
			coords[i++] = stroke.color.y;
			coords[i++] = stroke.color.z;

			coords[i++] = p3f.x;
			coords[i++] = p3f.y;
			coords[i++] = p3f.z;
		}

		float[] h = new float[] {-1, 1, 0, 0};
		float[] v = new float[] {0, 0, -1, 1};

		for (int j = 0; j < 4; j++) {
			coords[i++] = ORIGIN_COLOR.x;
			coords[i++] = ORIGIN_COLOR.y;
			coords[i++] = ORIGIN_COLOR.z;

			coords[i++] = h[j]*ORIGIN_TICK;
			coords[i++] = v[j]*ORIGIN_TICK;
			coords[i++] = Net.GCODE_Z_MIN;
		}

		GeometryArray gCodeGeometry = new LineArray(vertexCount,
				GeometryArray.COORDINATES |
				GeometryArray.COLOR_3 |
				GeometryArray.INTERLEAVED |
				GeometryArray.BY_REFERENCE);
		gCodeGeometry.setInterleavedVertices(coords);

		Shape3D gCodeS3D = new Shape3D();
		gCodeS3D.setGeometry(gCodeGeometry);

		BranchGroup gCodeBG = new BranchGroup();
		gCodeBG.setPickable(false);
		gCodeBG.setCapability(BranchGroup.ALLOW_DETACH);
		gCodeBG.addChild(gCodeS3D);

		model.setGCode(gCodeBG);
	}

	public Map<ToolpathNode, ToolpathNode> nodes = new LinkedHashMap<ToolpathNode, ToolpathNode>();
	private List<ToolpathPath> paths = new LinkedList<ToolpathPath>();

	private DataBuffer buffer;

	private BranchGroup sceneBG = null;
	
	private int mode;
}
