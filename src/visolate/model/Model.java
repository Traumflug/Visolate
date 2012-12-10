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

import visolate.simulator.*;
import visolate.simulator.Action;
import visolate.simulator.Stroke;
import visolate.misc.*;
import visolate.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.geometry.GeometryInfo;

import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public class Model extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 214293510166172826L;

	public static final int NUM_CIRCLE_SEGMENTS = 256;

	public static final Vector3d PICK_Z = new Vector3d(0.0, 0.0, -1.0);

	public static final boolean DEF_OUTLINE = true;
	public static final boolean DEF_OUTLINE_CIRCULAR = false;
	public static final boolean DEF_OUTLINE_WHITE = true;
	public static final boolean DEF_LINE = false;
	public static final boolean DEF_VORONOI = false;
	public static final boolean DEF_FLAT = true;
	public static final boolean DEF_TRANSLUCENT = false;
	public static final boolean DEF_PATHS = true;
	public static final boolean DEF_GCODE = false;

	public static final float OUTLINE_PAD = 100.0f;

	public static final double MIN_TOOL_DIAMETER = 0.0;
	public static final double MAX_TOOL_DIAMETER = 1.0;

	public static final double MIN_VORONOI_LIMIT = 0.0;
	public static final double MAX_VORONOI_LIMIT = 100.0;
	public static final double DEF_VORONOI_LIMIT = 1.0;

	public Model(Visolate visolate) {
		this.visolate = visolate;

		setBackground(Color.WHITE);

		Box geometryBox = Box.createHorizontalBox();
		//    geometryBox.add(Box.createHorizontalGlue());

		Box borderBox = Box.createHorizontalBox();
		borderBox.setAlignmentY(0.0f);

		borderButton = new JCheckBox("border");
		borderButton.setBackground(Color.WHITE);
		borderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean borderOn = borderButton.isSelected();
				circularButton.setEnabled(borderOn);
				whiteButton.setEnabled(borderOn);
				enableBorderGeometry(borderOn);
			}
		});
		borderButton.setSelected(DEF_OUTLINE);
		borderBox.add(borderButton);

		circularButton = new JCheckBox("c");
		circularButton.setBackground(Color.WHITE);
		circularButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rebuildGeometry();
			}
		});
		circularButton.setSelected(DEF_OUTLINE_CIRCULAR);

		borderBox.add(circularButton);

		whiteButton = new JCheckBox("w");
		whiteButton.setBackground(Color.WHITE);
		whiteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rebuildGeometry();
			}
		});
		whiteButton.setSelected(DEF_OUTLINE_WHITE);

		borderBox.add(whiteButton);

		geometryBox.add(borderBox);
		geometryBox.add(Box.createHorizontalGlue());

		enableBorderGeometry(DEF_OUTLINE);

		lineButton = new JCheckBox("line");
		lineButton.setBackground(Color.WHITE);
		lineButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableLineGeometry(lineButton.isSelected());
			}
		});
		lineButton.setSelected(DEF_LINE);
		enableLineGeometry(DEF_LINE);
		lineButton.setAlignmentY(0.0f);
		geometryBox.add(lineButton);

		voronoiButton = new JCheckBox("voronoi");
		voronoiButton.setBackground(Color.WHITE);
		voronoiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableVoronoiGeometry(voronoiButton.isSelected());
			}
		});
		voronoiButton.setSelected(DEF_VORONOI);
		enableVoronoiGeometry(DEF_VORONOI);
		voronoiButton.setAlignmentY(0.0f);
		geometryBox.add(voronoiButton);

		//    geometryBox.add(Box.createHorizontalStrut(16));
		geometryBox.add(Box.createHorizontalGlue());

		translucentButton = new JCheckBox("translucent");
		flatButton = new JCheckBox("flat");
		flatButton.setBackground(Color.WHITE);
		flatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableFlatGeometry(flatButton.isSelected());
			}
		});
		flatButton.setSelected(DEF_FLAT);
		enableFlatGeometry(DEF_FLAT);
		flatButton.setAlignmentY(0.0f);
		geometryBox.add(flatButton);

		translucentButton.setBackground(Color.WHITE);
		translucentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTranslucent2D(translucentButton.isSelected());
			}
		});
		translucentButton.setSelected(DEF_TRANSLUCENT);
		setTranslucent2D(DEF_TRANSLUCENT);
		translucentButton.setAlignmentY(0.0f);
		geometryBox.add(translucentButton);

		//    geometryBox.add(Box.createHorizontalStrut(16));
		geometryBox.add(Box.createHorizontalGlue());

		pathsButton = new JCheckBox("path");
		pathsButton.setBackground(Color.WHITE);
		pathsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enablePathsGeometry(pathsButton.isSelected());
			}
		});
		pathsButton.setSelected(DEF_PATHS);
		enablePathsGeometry(DEF_PATHS);
		pathsButton.setAlignmentY(0.0f);
		geometryBox.add(pathsButton);

		gCodeButton = new JCheckBox("g-code");
		gCodeButton.setBackground(Color.WHITE);
		gCodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableGCodeGeometry(gCodeButton.isSelected());
			}
		});
		gCodeButton.setSelected(DEF_GCODE);
		enableGCodeGeometry(DEF_GCODE);
		gCodeButton.setAlignmentY(0.0f);
		geometryBox.add(gCodeButton);

		//    geometryBox.add(Box.createHorizontalGlue());


		Box settingsBox = Box.createHorizontalBox();

		settingsBox.add(new JLabel("flip"));

		flipXButton = new JCheckBox("X");
		flipXButton.setBackground(Color.WHITE);
		flipXButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFlipX(flipXButton.isSelected());
			}
		});
		flipXButton.setSelected(false);
		settingsBox.add(flipXButton);

		flipYButton = new JCheckBox("Y");
		flipYButton.setBackground(Color.WHITE);
		flipYButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFlipY(flipYButton.isSelected());
			}
		});
		flipYButton.setSelected(false);
		settingsBox.add(flipYButton);

		settingsBox.add(Box.createHorizontalGlue());

		offsetLabel = new JLabel("tool dia [in]: ");
		settingsBox.add(offsetLabel);

		offsetField = new JTextField() {
			private static final long serialVersionUID = 1223546354547661586L;

			{ columnWidth = getColumnWidth(); }
		};
		offsetField.setHorizontalAlignment(JTextField.RIGHT);
		Dimension d = offsetField.getPreferredSize();
		offsetField.setMaximumSize(new Dimension(columnWidth*6, d.height));
		offsetField.setPreferredSize(new Dimension(columnWidth*6, d.height));
		offsetField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { updateOffset(); } });
		//    offsetField.addFocusListener(new FocusAdapter() {
		//        public void focusLost(FocusEvent e) { updateOffset(); } });
		settingsBox.add(offsetField);

		settingsBox.add(Box.createHorizontalGlue());

		limitLabel = new JLabel("voronoi lim [in]: ");
		settingsBox.add(limitLabel);

		limitField = new JTextField();
		limitField.setHorizontalAlignment(JTextField.RIGHT);
		d = limitField.getPreferredSize();
		limitField.setMaximumSize(new Dimension(columnWidth*6, d.height));
		limitField.setPreferredSize(new Dimension(columnWidth*6, d.height));
		limitField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) { updateLimit(); } });
		settingsBox.add(limitField);

		autoLimitButton = new JCheckBox("auto");
		autoLimitButton.setBackground(Color.WHITE);
		autoLimitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLimit();
			}
		});
		autoLimitButton.setSelected(false);
		settingsBox.add(autoLimitButton);


		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createTitledBorder("Geometry"));
		box.add(geometryBox);
		box.add(settingsBox);

		setLayout(new BorderLayout());
		add(box, "Center");
	}

	public void rebuild() {

		sceneBG = null;
		bounds = null;

		nets.clear();
		colorToNet.clear();
		clearPaths();

		Simulator simulator = visolate.getSimulator();
		System.out.println("computing inch coords...");
		simulator.computeInchCoordinates();

		System.out.println("making nets...");
		makeNets(simulator.getActions());
		System.out.println(nets.size() + " nets");

		System.out.println("making half edge loops...");
		int num = 0;
		for (Net net : nets) {
			num += net.makeHalfEdgeLoops();
		}
		System.out.println(num + " loops");

		for (Net net : nets) {
			int c = color3bToInt(net.getColor());
			//      System.out.println(net.getColor() + " -> " + colorToString(c));
			colorToNet.put(new Integer(c), net);
		}

		rebuildGeometry();
	}

	private void rebuildGeometry() {

		for (Net net : nets) {
			net.enableLineGeometry(lineGeometry);
			net.enableVoronoiGeometry(voronoiGeometry);
			net.enableFlatGeometry(flatGeometry);

			net.setTranslucent2D(translucent2D);

			net.setZCeiling(voronoiLimit);
			net.setOffset(toolDiameter/2.0);
		}

		bounds = null;

		if (borderBG != null) {
			boolean borderGeometryWas = borderGeometry;
			enableBorderGeometry(false);
			borderBG.removeAllChildren();
			borderBG.addChild(makeBorder());
			enableBorderGeometry(borderGeometryWas);
		}

		enablePathsGeometry(pathsGeometry);
		enableGCodeGeometry(gCodeGeometry);

		limitField.setText(numberFormat.format(voronoiLimit));
		offsetField.setText(numberFormat.format(toolDiameter));

		Display display = visolate.getDisplay();

		if (display != null)
			display.reset();
	}

	public double getToolDiameter() {
		return toolDiameter;
	}

	public void setToolDiameter(double toolDiameter) {

		if (toolDiameter == this.toolDiameter)
			return;

		this.toolDiameter = toolDiameter;

		rebuildGeometry();
	}

	public void deleteNet(final Net net) {

		if (nets.remove(net)) {

			visolate.addFrameTask(new Runnable() {
				public void run() {
					net.detach();
				}
			});

			rebuildGeometry();
		}
	}

	public void undeleteNet(final Net net) {

		if (nets.add(net)) {

			visolate.addFrameTask(new Runnable() {
				public void run() {
					if (sceneBG != null)
						sceneBG.addChild(net.getSceneGraph());
				}
			});

			rebuildGeometry();
		}
	}

	private int makeNets(Collection<Action> actions) {

		Set<Stroke> strokes = new LinkedHashSet<Stroke>();
		Set<Action> flashes = new LinkedHashSet<Action>();
		Set<Vertex> vertices = new LinkedHashSet<Vertex>();

		for (Iterator<visolate.simulator.Action> it = actions.iterator(); it.hasNext(); ) {

			visolate.simulator.Action action = (visolate.simulator.Action) it.next();

			if (action instanceof visolate.simulator.Stroke) {
				strokes.add((Stroke) action);
				vertices.add(((visolate.simulator.Stroke) action).getStart());
				vertices.add(((visolate.simulator.Stroke) action).getEnd());
			} else if (action instanceof Flash) {
				flashes.add(action);
				vertices.add(((Flash) action).getLocation());
			}

		}

		while (!strokes.isEmpty()) {

			Net net = new Net(visolate);

			List<Stroke> neighbors = new LinkedList<Stroke>();

			neighbors.add((Stroke) (strokes.iterator()).next());

			while (!neighbors.isEmpty()) {

				visolate.simulator.Stroke stroke = neighbors.remove(0);
				strokes.remove(stroke);

				net.addStroke(stroke);

				Vertex start = stroke.getStart();
				Vertex end = stroke.getEnd();

				if (vertices.contains(start)) {
					addFlashesAtVertex(net, start, flashes);
					collectNeighborStrokes(neighbors, start, strokes);
					vertices.remove(start);
				}

				if (vertices.contains(end)) {
					addFlashesAtVertex(net, end, flashes);
					collectNeighborStrokes(neighbors, end, strokes);
					vertices.remove(end);
				}
			}

			nets.add(net);
		}

		//some nets may consist entirely of one or more flashes at a single loc
		while (!flashes.isEmpty()) {

			Net net = new Net(visolate);

			Flash flash = (Flash) (flashes.iterator()).next();

			addFlashesAtVertex(net, flash.getLocation(), flashes);

			nets.add(net);
		}

//		int i = 0;
		int n = nets.size();
		for (Net net : nets) {
			net.setColor(visolate.getDisplay().getRandomColor());
		}

		return n;
	}

	private void collectNeighborStrokes(final Collection<Stroke> neighbors,
			final Vertex vertex,
			final Set<Stroke> strokes) {

		Collection<Action> actions = vertex.getIncidentActions();

		for (Action action : actions) {

			if ((action instanceof visolate.simulator.Stroke) &&
					strokes.remove(action)) {
				neighbors.add((Stroke) action);
			}
		}
	}

	private void addFlashesAtVertex(Net net,
			Vertex vertex,
			Set<Action> flashes) {

		Collection<Action> actions = vertex.getIncidentActions();

		for (Iterator<Action> it = actions.iterator(); it.hasNext(); ) {

			Action action = it.next();

			if ((action instanceof Flash) && flashes.remove(action))
				net.addPad((Flash) action);
		}
	}

	public int color3bToInt(Color3b c) {

		int r = ((int) c.x) & 0xff;
		int g = ((int) c.y) & 0xff;
		int b = ((int) c.z) & 0xff;

		int i = b;
		i |= g << 8;
		i |= r << 16;

		//      System.out.println(c + " -> (" + r + ", " + g + ", " + b + ") -> " +
				//                         colorToString(i));

		return i;
	}

	public Set<Integer> getNetColors() {
		return colorToNet.keySet();
	}

	public Net getNet(int color) {
		return (Net) colorToNet.get(new Integer(color));
	}

	public Collection<Net> getNets() {
		return nets;
	}

	public Collection<Net> getNetsAtPoint(double x, double y,
			double radius,
			Collection<Net> nets) {

		PickTool pickTool = new PickTool(getSceneGraph());
		pickTool.setMode(PickTool.BOUNDS);
		pickTool.setShapeCylinderRay(new Point3d(x, y, Display.EYE.z),
				PICK_Z,
				radius);

		PickResult[] result = pickTool.pickAll();

		if (result == null) {
			return nets;
		}

		for (int i = 0; i < result.length; i++) {
			result[i].setFirstIntersectOnly(true);

			if (result[i].numIntersections() > 0) {
				Net net = (Net) (result[i].
						getSceneGraphPath().
						getObject().
						getUserData());

				if (net != null) {
					nets.add(net);
				}
			}
		}

		return nets;
	}

	public BranchGroup getSceneGraph() {

		if (sceneBG == null) {

			modelBG = new BranchGroup();
			modelBG.setCapability(BranchGroup.ALLOW_DETACH);
			modelBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			modelBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			modelBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

			flipTG = new TransformGroup();
			flipTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			flipTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

			flipT3D = new Transform3D();
      setFlipX(flipXButton.isSelected());
      setFlipY(flipYButton.isSelected());

			sceneBG = new BranchGroup();
			sceneBG.setCapability(BranchGroup.ALLOW_DETACH);
			sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

			pathsBG = new BranchGroup();
			pathsBG.setCapability(BranchGroup.ALLOW_DETACH);
			pathsBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			pathsBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			pathsBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

			gCodeBG = new BranchGroup();
			gCodeBG.setCapability(BranchGroup.ALLOW_DETACH);
			gCodeBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			gCodeBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			gCodeBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

			borderBG = new BranchGroup();
			borderBG.setCapability(BranchGroup.ALLOW_DETACH);
			borderBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			borderBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			borderBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			borderBG.addChild(makeBorder());

			if (pathsGeometry)
				modelBG.addChild(pathsBG);

			if (gCodeGeometry)
				modelBG.addChild(gCodeBG);

			if (borderGeometry)
				sceneBG.addChild(borderBG);

			for (Net net : nets) {
				sceneBG.addChild(net.getSceneGraph());
			}

			flipTG.addChild(sceneBG);

			modelBG.addChild(flipTG);
		}

		return modelBG;
	}

	private BranchGroup makeBorder() {

		Rect bounds = getBoardBounds();

		boolean circular = circularButton.isSelected();

		int numInnerVertices = (circular) ? NUM_CIRCLE_SEGMENTS : 4;

		float[] coords = new float[4*3 + numInnerVertices*3];

		int i = 0;

		float z = Net.OUTLINE_Z;

		//outer rect, ccw
		coords[i++] = (float) (bounds.x + bounds.width + OUTLINE_PAD);
		coords[i++] = (float) (bounds.y + bounds.height + OUTLINE_PAD);
		coords[i++] = z;

		coords[i++] = (float) (bounds.x - OUTLINE_PAD);
		coords[i++] = (float) (bounds.y + bounds.height + OUTLINE_PAD);
		coords[i++] = z;

		coords[i++] = (float) (bounds.x - OUTLINE_PAD);
		coords[i++] = (float) (bounds.y - OUTLINE_PAD);
		coords[i++] = z;

		coords[i++] = (float) (bounds.x + bounds.width + OUTLINE_PAD);
		coords[i++] = (float) (bounds.y - OUTLINE_PAD);
		coords[i++] = z;

		//inner rect, cw
		if (circular) {

			float cx = (float) (bounds.x + bounds.width/2.0);
			float cy = (float) (bounds.y + bounds.height/2.0);

			float r = (float) (Math.max(bounds.width, bounds.height)/2.0);

			double t = 0.0;
			double segment = (Math.PI*2.0)/((double) NUM_CIRCLE_SEGMENTS);
			for (int j = 0; j < NUM_CIRCLE_SEGMENTS; j++) {
				coords[i++] = cx + (float) (r*Math.cos(t));
				coords[i++] = cy + (float) (r*Math.sin(t));
				coords[i++] = z;
				t += segment;
			}

		} else {

			coords[i++] = (float) (bounds.x + bounds.width);
			coords[i++] = (float) (bounds.y + bounds.height);
			coords[i++] = z;

			coords[i++] = (float) (bounds.x + bounds.width);
			coords[i++] = (float) (bounds.y);
			coords[i++] = z;

			coords[i++] = (float) (bounds.x);
			coords[i++] = (float) (bounds.y);
			coords[i++] = z;

			coords[i++] = (float) (bounds.x);
			coords[i++] = (float) (bounds.y + bounds.height);
			coords[i++] = z;
		}

		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(coords);
		gi.setStripCounts(new int[] {4, numInnerVertices});
		gi.setContourCounts(new int[] {2});

		Appearance appearance = new Appearance();

		ColoringAttributes coloringAttributes = new ColoringAttributes();
		if (whiteButton.isSelected())
			coloringAttributes.setColor(new Color3f(1.0f, 1.0f, 1.0f));
		else
			coloringAttributes.setColor(Net.toColor3f(visolate.getDisplay().
					getRandomColor()));
		appearance.setColoringAttributes(coloringAttributes);

		PolygonAttributes polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		appearance.setPolygonAttributes(polygonAttributes);

		Shape3D border = new Shape3D();
		border.setPickable(false);
		border.setGeometry(gi.getGeometryArray());
		border.setAppearance(appearance);

		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);

		bg.addChild(border);

		return bg;
	}

	public void setPaths(BranchGroup pathBG) {
		pathsBG.removeAllChildren();
		pathsBG.addChild(pathBG);
	}

	public void clearPaths() {
		if (pathsBG != null)
			pathsBG.removeAllChildren();
	}

	public void setGCode(BranchGroup gCodeBG) {
		(this.gCodeBG).removeAllChildren();
		(this.gCodeBG).addChild(gCodeBG);
	}

	public void clearGCode() {
		if (gCodeBG != null)
			gCodeBG.removeAllChildren();
	}

	public Rect getModelBounds() {

		if (bounds == null) {

			bounds = new Rect();

			for (Net net : nets) {
				bounds.add(net.getBounds());
			}
		}

		return bounds;
	}

	public Rect getBoardBounds() {
		//TBD check for specified board bounds
		//TBD offset model bounds
		return getModelBounds();
	}

	public double getMaxDimension() {

		Rect bounds = getModelBounds();

		if (bounds.width > bounds.height)
			return bounds.width;
		else
			return bounds.height;
	}

	public double getDiagonal() {
		Rect bounds = getModelBounds();
		return Math.sqrt(bounds.width*bounds.width + bounds.height*bounds.height);
	}

	public void enableBorderGeometry(boolean enable) {

		if (borderGeometry == enable)
			return;

		borderGeometry = enable;

		if ((borderBG == null) || (sceneBG == null))
			return;

		if (enable)
			sceneBG.addChild(borderBG);
		else
			borderBG.detach();
	}

	public boolean isBorderGeometryEnabled() {
		return borderGeometry;
	}

	public void enableLineGeometry(boolean enable) {

		if (lineGeometry == enable)
			return;

		lineGeometry = enable;

		for (Net net : nets) {
			net.enableLineGeometry(enable);
		}
	}

	public void enablePathsGeometry(boolean enable) {

		if (pathsGeometry == enable)
			return;

		pathsGeometry = enable;

		if ((pathsBG == null) || (sceneBG == null))
			return;

		if (enable)
			modelBG.addChild(pathsBG);
		else
			pathsBG.detach();
	}

	public void enableGCodeGeometry(boolean enable) {

		if (gCodeGeometry == enable)
			return;

		gCodeGeometry = enable;

		if ((gCodeBG == null) || (sceneBG == null))
			return;

		if (enable)
			modelBG.addChild(gCodeBG);
		else
			gCodeBG.detach();
	}

	public boolean isGCodeGeometryEnabled() {
		return gCodeGeometry;
	}

	public boolean isLineGeometryEnabled() {
		return lineGeometry;
	}

	public void enableVoronoiGeometry(boolean enable) {

		if (voronoiGeometry == enable)
			return;

		voronoiGeometry = enable;

		for (Net net : nets) {
			net.enableVoronoiGeometry(enable);
		}
	}

	public boolean isVoronoiGeometryEnabled() {
		return voronoiGeometry;
	}

	public void enableFlatGeometry(boolean enable) {

		if (flatGeometry == enable)
			return;

		flatGeometry = enable;

		for (Net net : nets) {
			net.enableFlatGeometry(enable);
		}

		translucentButton.setEnabled(enable);
	}

	public boolean isFlatGeometryEnabled() {
		return flatGeometry;
	}

	public void setTranslucent2D(boolean enable) {

		if (translucent2D == enable)
			return;

		translucent2D = enable;

		for (Net net : nets) {
			net.setTranslucent2D(enable);
		}

		if (translucentButton.isSelected() != enable) {
			translucentButton.setSelected(enable);
		}
	}

	public boolean isTranslucent2D() {
		return translucent2D;
	}

	public void enableControls(boolean enable) {

		borderButton.setEnabled(enable);
		circularButton.setEnabled(borderButton.isSelected() && enable);
		whiteButton.setEnabled(borderButton.isSelected() && enable);
		lineButton.setEnabled(enable);
		voronoiButton.setEnabled(enable);
		flatButton.setEnabled(enable);
		translucentButton.setEnabled(enable);
		pathsButton.setEnabled(enable);
		gCodeButton.setEnabled(enable);

		offsetLabel.setEnabled(enable);
		offsetField.setEnabled(enable);

		limitLabel.setEnabled(!autoLimitButton.isSelected() && enable);
		limitField.setEnabled(!autoLimitButton.isSelected() && enable);
		autoLimitButton.setEnabled(enable);
	}

	public void dump() {

		System.out.println("model bounds: " + getModelBounds());
		System.out.println("board bounds: " + getBoardBounds());

		System.out.println(nets.size() + " nets");

		Set<Set<Net>> seen = new LinkedHashSet<Set<Net>>();
		int n = 0;
		int numSup = 0;
		for (Net net : nets) {

			Set<Net> superNet = net.getSuperNet();

			if (superNet != null) {
				if (!seen.contains(superNet)) {
					seen.add(superNet);
					numSup++;
					n++;
				}
			} else {
				n++;
			}

			net.dump();
		}

		System.out.println(n + " collected nets (" + numSup + " supernets)");
	}

	public void setFlipX(boolean flipX) {
		if (flipX) {
		  flipXScale = -1.0;
		  Rect b = getBoardBounds();
		  flipXOffset = 2.0*b.x+b.width;
		  updateFlipT3D();
		} else {
		  flipXScale = 1.0;
		  flipXOffset = 0.0;
		  updateFlipT3D();
		}
		flipXButton.setSelected(flipX);
	}

	public void setFlipY(boolean flipY) {
		if (flipY) {
		  flipYScale = -1.0;
		  Rect b = getBoardBounds();
		  flipYOffset = 2.0*b.y+b.height;
		  updateFlipT3D();
		} else {
		  flipYScale = 1.0;
		  flipYOffset = 0.0;
		  updateFlipT3D();
		}
		flipYButton.setSelected(flipY);
	}

	private void updateFlipT3D() {
		flipT3D.set(new double[] {
		    flipXScale, 0.0,        0.0, flipXOffset,
				0.0,        flipYScale, 0.0, flipYOffset,
				0.0,        0.0,        1.0, 0.0,
				0.0,        0.0,        0.0, 1.0});
		flipTG.setTransform(flipT3D);
	}

	private void updateOffset() {

		double diameterWas = toolDiameter;

		try {

			double tmp = Double.parseDouble(offsetField.getText());

			if ((tmp >= MIN_TOOL_DIAMETER) && (tmp <= MAX_TOOL_DIAMETER))
				toolDiameter = tmp;

		} catch (NumberFormatException e) { }

		offsetField.setText(numberFormat.format(toolDiameter));

		if (diameterWas != toolDiameter)
			rebuildGeometry();
	}

	private void updateLimit() {

		double limitWas = voronoiLimit;

		if (autoLimitButton.isSelected()) {
			setAutoLimit();
		} else {
			limitLabel.setEnabled(true);
			limitField.setEnabled(true);
		}

		try {

			double tmp = Double.parseDouble(limitField.getText());

			if ((tmp >= MIN_VORONOI_LIMIT) && (tmp <= MAX_VORONOI_LIMIT))
				voronoiLimit = tmp;

		} catch (NumberFormatException e) { }

		limitField.setText(numberFormat.format(voronoiLimit));

		if (limitWas != voronoiLimit)
			rebuildGeometry();
	}

	private void setAutoLimit() {

		String tmp = numberFormat.format(getDiagonal());

		try {
			voronoiLimit = Double.parseDouble(tmp);
		} catch (NumberFormatException e) { }

		limitField.setText(tmp);

		autoLimitButton.setSelected(true);
		limitLabel.setEnabled(false);
		limitField.setEnabled(false);
	}

	public static String colorToString(int c) {

		int b = c & 0xff;
		int g = (c & 0xff00) >> 8;
			int r = (c & 0xff0000) >> 16;

		return "(" + r + ", " + g + ", " + b + ")";
	}

	private Visolate visolate;

	private Collection<Net> nets = new LinkedHashSet<Net>();

	private BranchGroup modelBG = null;
	private TransformGroup flipTG = null;
	private Transform3D flipT3D = null;
	private BranchGroup sceneBG = null;
	private BranchGroup pathsBG = null;
	private BranchGroup gCodeBG = null;
	private BranchGroup borderBG = null;

	private Rect bounds = null;

	private boolean borderGeometry = false;
	private boolean lineGeometry = false;
	private boolean voronoiGeometry = false;
	private boolean flatGeometry = false;
	private boolean pathsGeometry = false;
	private boolean gCodeGeometry = false;

	private JCheckBox borderButton;
	private JCheckBox circularButton;
	private JCheckBox whiteButton;
	private JCheckBox lineButton;
	private JCheckBox voronoiButton;
	private JCheckBox flatButton;
	private JCheckBox translucentButton;
	private JCheckBox pathsButton;
	private JCheckBox gCodeButton;

	private boolean translucent2D = DEF_TRANSLUCENT;

	private LinkedHashMap<Integer, Net> colorToNet = new LinkedHashMap<Integer, Net>();

//	private Rect boardBounds = null;

	private int columnWidth;

	private JCheckBox flipXButton;
	private JCheckBox flipYButton;

	private JLabel offsetLabel;
	private JTextField offsetField;

	private JLabel limitLabel;
	private JTextField limitField;
	private JCheckBox autoLimitButton;

	private double toolDiameter = 0.0;
	private double voronoiLimit = DEF_VORONOI_LIMIT;

	private NumberFormat numberFormat = new DecimalFormat() {

		private static final long serialVersionUID = -2414650013441669791L;

		{ setMaximumFractionDigits(4); }
	};

	private double flipXScale = 1.0;
	private double flipXOffset = 0.0;

	private double flipYScale = 1.0;
	private double flipYOffset = 0.0;
}
