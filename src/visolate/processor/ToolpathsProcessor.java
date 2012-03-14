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
import java.util.Locale;
import java.text.*;
import java.io.*;

import visolate.*;
import visolate.model.*;
import visolate.misc.*;
import visolate.processor.ToolpathNode;
import visolate.processor.ToolpathPath;

/**
 * The ToolpathsProcessor generates the content for a  g-code file.
 */
public class ToolpathsProcessor extends MosaicProcessor {

	public static final Color3f ORIGIN_COLOR = new Color3f(1.0f, 0.0f, 1.0f);
	public static final float ORIGIN_TICK = 0.1f;

	public static final double[] HORIZ_DIR_BIAS = {-1, 1, 1, -1};
	public static final double[] VERT_DIR_BIAS = {1, 1, -1, -1};

	public static final int VORONOI_MODE = 0;
	public static final int OUTLINE_MODE = 1;

	public static final int DEF_MODE = VORONOI_MODE;

	public static final double MMPERINCH = 25.4;

	/**
	 * This is the number format used for all numbers in the gcode.
	 */
	private static NumberFormat gCodeFormat = new DecimalFormat("###.#####", new DecimalFormatSymbols(Locale.ENGLISH));

	/**
	 * How much to move the toolhead in z-direction between cutting and moving.
	 */
	public static final double CLEARANCE_Z = 0.1;


	public static final Color3f G_CODE_COLOR_NORMAL = new Color3f(0.0f,
			1.0f,
			0.0f);

	public static final Color3f G_CODE_COLOR_RAPID = new Color3f(1.0f,
			1.0f,
			0.0f);

	/**
	 * If set to true, output absolute coordinates instead of relative.
	 */
	private boolean outputAbsoluteCoordinates;

	/**
	 * If set to true, output metric coordinates instead of relative.
	 */
	private boolean outputMetricCoordinates;

	/**
	 * @return If set to true, output metric coordinates instead of relative.
	 */
	public boolean isOutputMetricCoordinates() {
		return outputMetricCoordinates;
	}

	/**
	 * @param outputMetricCoordinates If set to true, output metric coordinates instead of relative.
	 */
	public void setOutputMetricCoordinates(final boolean outputMetricCoordinates) {
		this.outputMetricCoordinates = outputMetricCoordinates;
	}

	/**
	 * We move this much above origin for traveling.
	 */
	private double myzClearance;

	/**
	 * If we use absolute coordinates,
	 * then this is the height-value for cutting.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	private double myZCuttingHeight = 0.0;

	/**
	 * If we use absolute coordinates,
	 * then this is the X-value for the left upper corner.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	private double myAbsoluteXStart = 0.0;
	
	/**
	 * Our current work feedrate.
	 * @see #gcodeLinear()
	 */
	private double currentFeedrate = 0.0;

	/**
	 * @return If we use absolute coordinates, then this is the X-value for the left upper corner.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	public double getAbsoluteXStart() {
		return myAbsoluteXStart;
	}

	/**
	 * @param myZCuttingHeight If we use absolute coordinates, then this is the X-value for the left upper corner.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	public void setAbsoluteXStart(final double setAbsoluteXStart) {
		this.myAbsoluteXStart = setAbsoluteXStart;
	}

	/**
	 * @return If we use absolute coordinates, then this is the Y-value for the left upper corner.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	public double getAbsoluteYStart() {
		return myAbsoluteYStart;
	}

	/**
	 * @param myZCuttingHeight If we use absolute coordinates, then this is the Y-value for the left upper corner.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	public void setAbsoluteYStart(final double setAbsoluteYStart) {
		this.myAbsoluteYStart = setAbsoluteYStart;
	}

	private double myPlungeSpeed = 2;
	private double myMillingSpeed = 2;
	public double getPlungeSpeed() {
		return myPlungeSpeed;
	}

	public void setPlungeSpeed(double myPlungeSpeed) {
		this.myPlungeSpeed = myPlungeSpeed;
	}

	public double getMillingSpeed() {
		return myMillingSpeed;
	}

	public void setMillingSpeed(double myMillingSpeed) {
		this.myMillingSpeed = myMillingSpeed;
	}

	/**
	 * If we use absolute coordinates,
	 * then this is the X-value for the left upper corner.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	private double myAbsoluteYStart = 0.0;
	/**
	 * @return If we use absolute coordinates, then this is the height-value for cutting.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	public double getZCuttingHeight() {
		return myZCuttingHeight;
	}

	/**
	 * @param myZCuttingHeight If we use absolute coordinates, then this is the height-value for cutting.
	 * @see #isOutputAbsoluteCoordinates()
	 */
	public void setZCuttingHeight(final double myZCuttingHeight) {
		this.myZCuttingHeight = myZCuttingHeight;
	}

	/**
	 * @return  We move this much upward from cutting to traveling.
	 */
	public double getZClearance() {
		return myzClearance;
	}

	/**
	 * @param myzClearance  We move this much upward from cutting to traveling.
	 */
	public void setZClearance(final double myzClearance) {
		this.myzClearance = myzClearance;
	}

	/**
	 * @return If true, output absolute coordinates instead of relative.
	 */
	public boolean isOutputAbsoluteCoordinates() {
		return outputAbsoluteCoordinates;
	}

	/**
	 * @param outputAbsoluteCoordinates If true, output absolute coordinates instead of relative.
	 */
	public void setOutputAbsoluteCoordinates(final boolean outputAbsoluteCoordinates) {
		this.outputAbsoluteCoordinates = outputAbsoluteCoordinates;
	}

	public ToolpathsProcessor(final Visolate visolate, final int mode, final boolean useAbsoluteCoordinates, final double zClearance, final boolean metric) {
		super(visolate);
		this.mode = mode;
		this.outputMetricCoordinates = metric;
		this.outputAbsoluteCoordinates = useAbsoluteCoordinates;
		this.myzClearance = zClearance;
	}

	public void processTile(int r, int c,
			int ulx, int uly,
			int width, int height,
			double left, double bottom,
			double right, double top) {

		visolate.resetInnerProgressBar(1);

		super.processTile(r, c,
				ulx, uly,
				width, height,
				left, bottom, right, top);

		visolate.tickInnerProgressBar();
	}

	protected void processStarted() {
		super.processStarted();

		java.awt.image.Raster raster = mosaic.getRaster();
		buffer = raster.getDataBuffer();

		switch (mode) {
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

		straightTol = 0.5/((double) dpi);
	}

	private void restoreModel() {

		model.enableBorderGeometry(borderGeometryWas);
		model.enableLineGeometry(lineGeometryWas);
		model.enableVoronoiGeometry(voronoiGeometryWas);
		model.enableFlatGeometry(flatGeometryWas);
		model.enableGCodeGeometry(gcodeGeometryWas);

		model.setTranslucent2D(wasTranslucent);
	}

	protected void processInterrupted() {
		restoreModel();
	}

	protected void processCompleted() {

		super.processCompleted();

		restoreModel();

		tile = null;

		extractNodes();

		if (thread.isInterrupted())
			return;

		makePaths();

		if (thread.isInterrupted())
			return;

		optimizePaths();

		if (thread.isInterrupted())
			return;

		model.setPaths(getSceneGraph());
	}

	private void extractNodes() {

		System.out.println("extracting nodes...");

		visolate.resetInnerProgressBar(mosaicHeight);

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

			visolate.tickInnerProgressBar();
		}

		System.out.println(nodes.size() + " nodes");

		mosaic = null;
	}

	private void makePaths() {

		System.out.println("making paths...");

		Set<ToolpathNode> nodes = this.nodes.keySet();

		currentTick = 0;
		visolate.resetInnerProgressBar(100);

		while (!nodes.isEmpty()) {
			paths.add(new ToolpathPath(this, nodes.iterator().next()));

	    if (Math.floor(((double) this.nodes.size())/((double) 100)) > currentTick) {
	      currentTick++;
	      visolate.tickInnerProgressBar();
	    }

			if (thread.isInterrupted()) {
				return;
			}
		}

		System.out.println(paths.size() + " paths");

		reportPathStats();
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

		visolate.resetInnerProgressBar(paths.size());

		for (ToolpathPath path : paths) {
			path.optimize();

			visolate.tickInnerProgressBar();

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

	public class Sector {

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

	public class PathNode {

		PathNode(ToolpathNode node, PathNode prev, int index) {

			x = toModelX(node.x);
			y = toModelY(node.y);

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

		paths.remove(closest);

		return closest;
	}

	public void writeGCode(Writer w) throws IOException {

		model.clearGCode();
		gCodeStrokes.clear();

		gCodePreAmble(w);

		Point3d p = new Point3d(0.0, 0.0, 0.0);

		//    for (Iterator it = paths.iterator(); it.hasNext(); )
		//      ((Path) it.next()).writeGCode(w, p);

		Collection<ToolpathPath> paths = new LinkedList<ToolpathPath>();
		paths.addAll(this.paths);

		while (!paths.isEmpty()) {
			getClosestPath(paths, p).writeGCode(w, p);
		}
		gCodePostAmble(w, p);

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

			p3f.add(stroke.d);

			coords[i++] = stroke.color.x;
			coords[i++] = stroke.color.y;
			coords[i++] = stroke.color.z;

			coords[i++] = p3f.x;
			coords[i++] = p3f.y;
			coords[i++] = p3f.z;
		}

		gCodeStrokes.clear();

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

	private class GCodeStroke {

		GCodeStroke(Vector3f d, Color3f color) {
			this.color = color;
			this.d = d;
		}

		Color3f color;
		Vector3f d;
	}

	private void gCodePreAmble(Writer w) throws IOException {

		if (w == null) {
			return;
		}

		if (isOutputMetricCoordinates()) {
			w.write("G21\n");    // millimeters
		} else {
			w.write("G20\n");    // inches
		}
		w.write("G17\n");     // X-Y plane
		w.write("G40\nG49\n"); // Cancel tool lengh & cutter dia compensation
		//    w.write("G53\n");     // Motion in machine co-ordinate system
		w.write("G80\n");    // Cancel any existing motion cycle

		if (isOutputAbsoluteCoordinates()) {
			w.write("G90\n");    // Absolute distance mode
		} else {
			w.write("G91\n");    // Relative distance mode
		}
	}

	private void gCodePostAmble(Writer w, Point3d p) throws IOException {

		gCodeCutterUp(w, p);
		gCodeRapidMovement(w, p, 0.0, 0.0); //rapid to origin

		if (w == null) {
			return;
		}

		w.write("M5\n"); // Spindle Stop
		w.write("M2\n"); // End of program
	}

	public void gCodeCutterUp(Writer w, final Point3d p) throws IOException {

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G0 Z" + 
						gCodeFormat.format(getZClearance()) + "\n");
				p.z = getZClearance();
			} else {
				w.write("G0 Z" +
						gCodeFormat.format(getZCuttingHeight() + getZClearance()) + "\n");
			}
		}

		gCodeStrokes.add(new GCodeStroke(new Vector3f(0.0f,
				0.0f,
				(float) getZClearance()),
				G_CODE_COLOR_NORMAL));
	}

	public void gCodeCutterDown(final Writer w, final Point3d p) throws IOException {

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				currentFeedrate = getPlungeSpeed();
				w.write("G1 Z" +
						gCodeFormat.format(getZCuttingHeight()) + " F"+
						gCodeFormat.format(60 * currentFeedrate) + "\n");
				p.z = 0.0;
			} else {
				w.write("G1 Z" + gCodeFormat.format(-1 * getZClearance()) + "\n");
			}
		}

		gCodeStrokes.add(new GCodeStroke(new Vector3f(0.0f,
				0.0f,
				(float) (-1 * getZClearance())),
				G_CODE_COLOR_NORMAL));
	}

	/**
	 * Add a g-code for a rapid, linear movement.
     *
	 * @param w where to write the gcode to
	 * @param p the current location. SIDE EFFECT: Will be updated to be x,y
	 * @param x the absolute location to move to
	 * @param y the absolute location to move to
	 * @throws IOException
	 */
	public void gCodeRapidMovement(final Writer w, final Point3d p, final double x, final double y)
	throws IOException {

		double dx = x - p.x;
		double dy = y - p.y;

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G0 X" +
						gCodeFormat.format(convertUnits(x) + getAbsoluteXStart()) + " Y" +
						gCodeFormat.format(convertUnits(y) + getAbsoluteYStart()) + "\n");
			} else {
				w.write("G0 X" +
						gCodeFormat.format(convertUnits(dx)) + " Y" +
						gCodeFormat.format(convertUnits(dy)) + "\n");
			}
		}

		p.x += dx;
		p.y += dy;

		gCodeStrokes.add(new GCodeStroke(new Vector3f((float) dx,
				(float) dy,
				0.0f),
				G_CODE_COLOR_RAPID));
	}

	public void gCodeLinear(Writer w, Point3d p, double x, double y)
	throws IOException {

		double dx = x - p.x;
		double dy = y - p.y;

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G1 X" +
						gCodeFormat.format(convertUnits(x) + getAbsoluteXStart()) + " Y" +
						gCodeFormat.format(convertUnits(y) + getAbsoluteYStart()));
			} else {
				w.write("G1 X" +
						gCodeFormat.format(convertUnits(dx)) + " Y" +
						gCodeFormat.format(convertUnits(dy)));
			}
			if (currentFeedrate != getMillingSpeed()) {
				currentFeedrate = getMillingSpeed();
				w.write(" F" + gCodeFormat.format(60 * currentFeedrate));
			}
			w.write("\n");
		}

		p.x += dx;
		p.y += dy;

		gCodeStrokes.add(new GCodeStroke(new Vector3f((float) dx,
				(float) dy,
				0.0f),
				G_CODE_COLOR_NORMAL));
	}

	private double convertUnits(final double x) {
		if (isOutputMetricCoordinates()) {
			return x * MMPERINCH;
		}
		return x;
	}

	public Map<ToolpathNode, ToolpathNode> nodes = new LinkedHashMap<ToolpathNode, ToolpathNode>();
	private List<ToolpathPath> paths = new LinkedList<ToolpathPath>();

	private DataBuffer buffer;

	private BranchGroup sceneBG = null;

	private int currentTick = 0;

	private int mode;

	private double straightTol;

	private List<GCodeStroke> gCodeStrokes = new LinkedList<GCodeStroke>();
}
