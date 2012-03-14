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

/**
 * The ToolpathsProcessor generates the content for a  g-code file.
 */
public class ToolpathsProcessor extends MosaicProcessor {

	public static final int N = 0;
	public static final int S = 1;
	public static final int W = 2;
	public static final int E = 3;

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
	 * We move this much upward from cutting to traveling.
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

	private double myMovementSpeed = 2;
	private double myMillingSpeed = 2;
	public double getMovementSpeed() {
		return myMovementSpeed;
	}

	public void setMovementSpeed(double myMovementSpeed) {
		this.myMovementSpeed = myMovementSpeed;
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

				Node n = null;

				if ((x > 0) && (lColor != color)) {

					if (n == null)
						n = getNode(x, y);

					Node o = getNode(x, y+1);

					n.south = o;

					if (o != null)
						o.north = n;
				}

				if ((y > 0) && (uColor != color)) {

					if (n == null)
						n = getNode(x, y);

					Node o = getNode(x+1, y);

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

		Set<Node> nodes = this.nodes.keySet();

		currentTick = 0;
		visolate.resetInnerProgressBar(100);

		while (!nodes.isEmpty()) {
			paths.add(new Path(nodes.iterator().next()));

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
		for (Path path : paths) {
			length += path.length();
			segments += path.numPathNodes();
		}

		System.out.println("total length: " + length);
		System.out.println("total segments: " + segments);
	}

	private void optimizePaths() {

		System.out.println("optimizing paths...");

		visolate.resetInnerProgressBar(paths.size());

		for (Path path : paths) {
			path.optimize();

			visolate.tickInnerProgressBar();

			if (thread.isInterrupted())
				return;
		}

		reportPathStats();
	}

	private class Node {

		Node(int x, int y) {

			this.x = x;
			this.y = y;

			hashCode = x^(y*31);
		}

		public int hashCode() {
			return hashCode;
		}

		public boolean equals(Object object) {

			if (!(object instanceof Node))
				return false;

			Node other = (Node) object;

			return (x == other.x) && (y == other.y);
		}

		public String toString() {
			return "(" + x + ", " + y + ")";
		}

		public int numNeighbors() {

			int n = 0;

			if (north != null)
				n++;

			if (south != null)
				n++;

			if (west != null)
				n++;

			if (east != null)
				n++;

			return n;
		}

		public Node getNeighbor(int d) {
			switch(d) {
			case N:
				return north;
			case S:
				return south;
			case W:
				return west;
			case E:
				return east;
			default:
				return null;
			}
		}

		public void setNeighbor(int d, Node n) {
			switch(d) {
			case N:
				north = n;
				break;
			case S:
				south = n;
				break;
			case W:
				west = n;
				break;
			case E:
				east = n;
				break;
			}
		}

		//    public Color3f getColor() {
		//      return getColor(((north != null) ? 1 : 0) |
		//                      ((south != null) ? 1 : 0) << 1 |
		//                      ((west != null) ? 1 : 0) << 2 |
		//                      ((east != null) ? 1 : 0) << 3);
		//    }

		//    public Color3f getColor(int i) {
		//
		//      if (nodeColor[i] == null) {
		//        Color3f color = Net.toColor3f(display.getRandomColor());
		//        nodeColor[i] = color;
		//        System.out.println("nodeColor[" + i + "] = " + color);
		//      }
		//
		//      return nodeColor[i];
		//    }

		int hashCode;

		int x;
		int y;

		Node north = null;
		Node south = null;
		Node west = null;
		Node east = null;
	}

	//  private Color3f[] nodeColor = new Color3f[16];

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

	private Node getNode(int x, int y) {

		if (x < 0)
			return null;

		if (y < 0)
			return null;

		if (x >= mosaicWidth)
			return null;

		if (y >= mosaicHeight)
			return null;

		Node key = new Node(x, y);

		Node node = (Node) nodes.get(key);

		if (node == null) {
			node = key;
			nodes.put(key, node);
		}

		return node;
	}

	private int oppositeDir(int d) {
		switch(d) {
		case N:
			return S;
		case S:
			return N;
		case W:
			return E;
		case E:
			return W;
		default:
			return -1;
		}
	}

	private class Path {

		Path(final Node seed) {

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
				dir[HEAD] = N;
				dir[TAIL] = S;
				break;
			}
			}

			while ((dir[TAIL] >= 0) && extendTail())
				;

			while ((dir[HEAD] >= 0) && extendHead())
				;

		}

		private boolean extendTail() {

			Node next = getNext((Node) path.getLast(), TAIL);

			if (next == null)
				return false;

			path.addLast(next);

			return true;
		}

		private boolean extendHead() {

			Node next = getNext((Node) path.getFirst(), HEAD);

			if (next == null)
				return false;

			path.addFirst(next);

			return true;
		}

		private Node getNext(Node n, int whichDir) {

			int d = dir[whichDir];

			Node next = n.getNeighbor(d);

			if (next == null) {

				for (int i = 0; i < 4; i++) {

					if (i == d)
						continue;

					if (i == oppositeDir(d))
						continue;

					Node neighbor = n.getNeighbor(i);

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
				nodes.remove(n);

			if (next.numNeighbors() == 0)
				nodes.remove(next);

			if (Math.floor(((double) nodes.size())/((double) 100)) > currentTick) {
				currentTick++;
				visolate.tickInnerProgressBar();
			}

			return next;
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

				Node prev = null;

				for (Node node : path) {

					if (prev != null) {
						length += Util.distance(toModelX(prev.x), toModelY(prev.y),
								toModelX(node.x), toModelY(node.y));
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

				Color3f color = Net.toColor3f(display.getRandomColor());

				int vertexCount = path.size()*2;

				float[] coords = new float[vertexCount*6];

				int i = 0;

				if (optimalPathEnd == null) {

					Node prev = null;

					for (Node node : path) {

						if (prev != null) {

							coords[i++] = color.x;
							coords[i++] = color.y;
							coords[i++] = color.z;

							coords[i++] = toModelX(prev.x);
							coords[i++] = toModelY(prev.y);
							coords[i++] = Net.PATH_Z;

							coords[i++] = color.x;
							coords[i++] = color.y;
							coords[i++] = color.z;

							coords[i++] = toModelX(node.x);
							coords[i++] = toModelY(node.y);
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

		public Point2d getStartPoint() {

			if (optimalPathEnd == null) {
				Node start = (Node) path.getFirst();
				return new Point2d(start.x, start.y);
			} else {
				return new Point2d(optimalPathEnd.x, optimalPathEnd.y);
			}
		}

		public void writeGCode(Writer w, Point3d p) throws IOException {

			gCodeCutterUp(w, p);

			boolean first = true;

			if (optimalPathEnd == null) {

				for (Node node : path) {

					if (first) {
						gCodeRapidMovement(w, p, node.x, node.y); //rapid to start
						gCodeCutterDown(w, p);
						first = false;
					} else {
						gCodeLinear(w, p, node.x, node.y);
					}
				}

			} else {

				for (PathNode node = optimalPathEnd;
				node != null;
				node = node.getBestPrev()) {

					if (first) {
						gCodeRapidMovement(w, p, node.x, node.y); //rapid to start
						gCodeCutterDown(w, p);
						first = false;
					} else {
						gCodeLinear(w, p, node.x, node.y);
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
			for (Iterator<Node> it = path.iterator(); it.hasNext(); ) {
				PathNode node = new PathNode((Node) it.next(), prev, i++);

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

		int[] dir = new int[2];

		final int HEAD = 0;
		final int TAIL = 1;

		private LinkedList<Node> path = new LinkedList<Node>();

		GeometryArray geometry;

		//    Node seed;

		PathNode optimalPathStart = null;
		PathNode optimalPathEnd = null;

		double[] Sx;
		double[] Sy;

		double[] Sxx;
		double[] Syy;

		double[] Sxy;
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

	private class PathNode {

		PathNode(Node node, PathNode prev, int index) {

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

			for (Iterator<Path> it = paths.iterator(); it.hasNext(); )
				shape.addGeometry(it.next().getGeometry());

			/*
      int vertexCount = nodes.size();

      float[] coords = new float[vertexCount*6];

      int i = 0;
      for (Iterator it = nodes.keySet().iterator(); it.hasNext(); ) {

        Node node = (Node) it.next();

        Color3f color = node.getColor();

        coords[i++] = color.x;
        coords[i++] = color.y;
        coords[i++] = color.z;

        float x = (float) (mosaicBounds.x + node.x/((float) dpi));
        float y = (float) (mosaicBounds.y + modelHeight-node.y/((float) dpi));

        coords[i++] = x;
        coords[i++] = y;
        coords[i++] = Net.PATH_Z;

      }

      System.out.println("vertexCount = " + vertexCount);

      GeometryArray geometry = new PointArray(vertexCount,
                                              GeometryArray.COORDINATES |
                                              GeometryArray.COLOR_3 |
                                              GeometryArray.INTERLEAVED |
                                              GeometryArray.BY_REFERENCE);
      geometry.setInterleavedVertices(coords);

      shape.addGeometry(geometry);
			 */

			sceneBG = new BranchGroup();
			sceneBG.setPickable(false);
			sceneBG.setCapability(BranchGroup.ALLOW_DETACH);
			sceneBG.addChild(shape);
		}

		return sceneBG;
	}

	private float toModelX(int x) {
		return (float) (mosaicBounds.x + x/((float) dpi));
	}

	private float toModelY(int y) {
		return (float) (mosaicBounds.y + modelHeight-y/((float) dpi));
	}

	private Path getClosestPath(Collection<Path> paths, Point3d p) {

		double minDist = Double.POSITIVE_INFINITY;
		Path closest = null;

		for (Path path : paths) {

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

		Collection<Path> paths = new LinkedList<Path>();
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

	private void gCodeCutterUp(Writer w, final Point3d p) throws IOException {

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G1 X" +
						gCodeFormat.format(convertUnits(p.x) + getAbsoluteXStart()) + " Y" +
						gCodeFormat.format(convertUnits(p.y) + getAbsoluteYStart()) + " Z" + 
						gCodeFormat.format(getZCuttingHeight() + getZClearance()) + " F"+
						gCodeFormat.format(60 * getMovementSpeed()) + "\n");
				p.z = getZClearance();
			} else {
				w.write("G1 Z" + gCodeFormat.format(getZClearance()) + "\n");
			}
		}

		gCodeStrokes.add(new GCodeStroke(new Vector3f(0.0f,
				0.0f,
				(float) getZClearance()),
				G_CODE_COLOR_NORMAL));
	}

	private void gCodeCutterDown(final Writer w, final Point3d p) throws IOException {

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G1 X" +
						gCodeFormat.format(convertUnits(p.x) + getAbsoluteXStart()) + " Y" +
						gCodeFormat.format(convertUnits(p.y) + getAbsoluteYStart()) + " Z" +
						gCodeFormat.format(getZCuttingHeight()) + " F"+
						gCodeFormat.format(60 * getMovementSpeed()) + "\n");
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
	 * @param p the current location. SIDEE FFECT: Will be updated to be x,y
	 * @param x the absolute location to move to
	 * @param y the absolute location to move to
	 * @throws IOException
	 */
	private void gCodeRapidMovement(final Writer w, final Point3d p, final double x, final double y)
	throws IOException {

		double dx = x - p.x;
		double dy = y - p.y;

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G0 X" +
						gCodeFormat.format(convertUnits(x) + getAbsoluteXStart()) + " Y" +
						gCodeFormat.format(convertUnits(y) + getAbsoluteYStart()) + " Z"+
						gCodeFormat.format(p.z + getZCuttingHeight()) + " F"+
						gCodeFormat.format(60 * getMovementSpeed()) + "\n");
			} else {
				w.write("G0 X" +
						gCodeFormat.format(convertUnits(dx)) + " Y" +
						gCodeFormat.format(convertUnits(dy)) + " F"+
						gCodeFormat.format(60 * getMovementSpeed()) + "\n");
			}
		}

		p.x += dx;
		p.y += dy;

		gCodeStrokes.add(new GCodeStroke(new Vector3f((float) dx,
				(float) dy,
				0.0f),
				G_CODE_COLOR_RAPID));
	}

	private void gCodeLinear(Writer w, Point3d p, double x, double y)
	throws IOException {

		double dx = x - p.x;
		double dy = y - p.y;

		if (w != null) {
			if (isOutputAbsoluteCoordinates()) {
				w.write("G1 X" +
						gCodeFormat.format(convertUnits(x) + getAbsoluteXStart()) + " Y" +
						gCodeFormat.format(convertUnits(y) + getAbsoluteYStart()) + " Z" +
						gCodeFormat.format(p.z + getZCuttingHeight()) + " F"+
						gCodeFormat.format(60 * getMillingSpeed()) + "\n");
			} else {
				w.write("G1 X" +
						gCodeFormat.format(convertUnits(dx)) + " Y" +
						gCodeFormat.format(convertUnits(dy)) + " F"+
						gCodeFormat.format(60 * getMillingSpeed()) + "\n");
			}
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

	private Map<Node, Node> nodes = new LinkedHashMap<Node, Node>();
	private List<Path> paths = new LinkedList<Path>();

	private DataBuffer buffer;

	private BranchGroup sceneBG = null;

	private int mode;

	private int currentTick = 0;

	private double straightTol;

	private List<GCodeStroke> gCodeStrokes = new LinkedList<GCodeStroke>();
}
