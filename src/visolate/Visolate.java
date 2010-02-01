/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **/

package visolate;

import visolate.parser.*;
import visolate.simulator.*;
import visolate.model.*;
import visolate.processor.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.net.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.*;

public class Visolate extends JPanel implements SimulatorUI {

	public static final String DEMO_FILE = "example.grb";

	private static final String cvsid =
		"$Id: Visolate.java,v 1.11 2006/09/15 19:48:24 vona Exp $";

	public Visolate() {
		this(null);
	}

	public Visolate(File file) {

		display = new Display(this);
		simulator = new Simulator(this);
		model = new Model(this);

		setBackground(Color.WHITE);
		setOpaque(true);

		Box loadBox = Box.createHorizontalBox();

		loadField = new JTextField();
		Dimension d = loadField.getPreferredSize();
		loadField.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
		loadField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile();
			} });
		//    loadField.addFocusListener(new FocusAdapter() {
		//        public void focusLost(FocusEvent e) { 
		//          loadFile();
		//        } });

		loadBox.add(loadField);

		loadButton = new JButton("Browse...");
		loadButton.setBackground(Color.WHITE);
		loadButton.setVerticalAlignment(AbstractButton.CENTER);
		loadButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = loadButton.getPreferredSize();
		loadButton.setMaximumSize(new Dimension(d.width, d.height));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				File file = browse();
				if (file != null)
					loadFile(file);
			}});

		loadBox.add(loadButton);

		loadBox.setBorder(BorderFactory.createTitledBorder("Input File"));

		Box mosaicBox = Box.createHorizontalBox();

		mosaicButton = new JButton("Save High-Res");
		mosaicButton.setBackground(Color.WHITE);
		mosaicButton.setVerticalAlignment(AbstractButton.CENTER);
		mosaicButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = mosaicButton.getPreferredSize();
		mosaicButton.setMaximumSize(new Dimension(d.width, d.height));
		mosaicButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				saveMosaic();
			}});
		mosaicBox.add(mosaicButton);

		mosaicField = new JTextField();
		d = mosaicField.getPreferredSize();
		mosaicField.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
		mosaicField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { saveMosaic(); } });
		//    mosaicField.addFocusListener(new FocusAdapter() {
		//        public void focusLost(FocusEvent e) { saveMosaic(); } });
		mosaicBox.add(mosaicField);

		mosaicBrowseButton = new JButton("Browse...");
		mosaicBrowseButton.setBackground(Color.WHITE);
		mosaicBrowseButton.setVerticalAlignment(AbstractButton.CENTER);
		mosaicBrowseButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = mosaicBrowseButton.getPreferredSize();
		mosaicBrowseButton.setMaximumSize(new Dimension(d.width, d.height));
		mosaicBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				File file = browse();
				if (file != null)
					saveMosaic(file);
			}});
		mosaicBox.add(mosaicBrowseButton);

		mosaicTilesButton = new JCheckBox("individual tiles");
		mosaicTilesButton.setBackground(Color.WHITE);
		mosaicTilesButton.setSelected(false);
		mosaicBox.add(mosaicTilesButton);

		Box topologyBox = Box.createHorizontalBox();

		topologyButton = new JButton("Fix Topology");
		topologyButton.setBackground(Color.WHITE);
		topologyButton.setVerticalAlignment(AbstractButton.CENTER);
		topologyButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = topologyButton.getPreferredSize();
		topologyButton.setMaximumSize(new Dimension(d.width, d.height));
		topologyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				fixTopology();
			}});
		topologyBox.add(topologyButton);

		//    topologyBox.add(Box.createHorizontalStrut(16));

		manualTopology = new JCheckBox("manual");
		manualTopology.setBackground(Color.WHITE);
		manualTopology.setSelected(false);
		topologyBox.add(manualTopology);

		//    topologyBox.add(Box.createHorizontalGlue());

		Box toolpathsBox = Box.createHorizontalBox();

		toolpathsButton = new JButton("Make Toolpaths");
		toolpathsButton.setBackground(Color.WHITE);
		toolpathsButton.setVerticalAlignment(AbstractButton.CENTER);
		toolpathsButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = toolpathsButton.getPreferredSize();
		toolpathsButton.setMaximumSize(new Dimension(d.width, d.height));
		toolpathsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				computeToolpaths();
			}});
		toolpathsBox.add(toolpathsButton);

		//    toolpathsBox.add(Box.createHorizontalStrut(16));

		ButtonGroup modeGroup = new ButtonGroup();

		voronoiButton = new JRadioButton("voronoi");
		modeGroup.add(voronoiButton);
		voronoiButton.setBackground(Color.WHITE);
		if (ToolpathsProcessor.DEF_MODE == ToolpathsProcessor.VORONOI_MODE)
			voronoiButton.setSelected(true);
		toolpathsBox.add(voronoiButton);

		outlineButton = new JRadioButton("outline");
		modeGroup.add(outlineButton);
		outlineButton.setBackground(Color.WHITE);
		if (ToolpathsProcessor.DEF_MODE == ToolpathsProcessor.OUTLINE_MODE)
			outlineButton.setSelected(true);
		toolpathsBox.add(outlineButton);

		//    toolpathsBox.add(Box.createHorizontalGlue());

		Box topologyAndToolpathsBox = Box.createHorizontalBox();
		topologyAndToolpathsBox.add(topologyBox);
		topologyAndToolpathsBox.add(Box.createHorizontalGlue());
		topologyAndToolpathsBox.add(toolpathsBox);

		Box gcodeBox = Box.createHorizontalBox();

		gcodeButton = new JButton("Save G-Code");
		gcodeButton.setEnabled(false);
		gcodeButton.setBackground(Color.WHITE);
		gcodeButton.setVerticalAlignment(AbstractButton.CENTER);
		gcodeButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = gcodeButton.getPreferredSize();
		gcodeButton.setMaximumSize(new Dimension(d.width, d.height));
		gcodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				saveGCode();
			}});
		gcodeBox.add(gcodeButton);

		gcodeField = new JTextField();
		gcodeField.setEnabled(false);
		d = gcodeField.getPreferredSize();
		gcodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
		gcodeField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { saveGCode(); } });
		//    gcodeField.addFocusListener(new FocusAdapter() {
		//        public void focusLost(FocusEvent e) { saveGCode(); } });
		gcodeBox.add(gcodeField);

		gcodeBrowseButton = new JButton("Browse...");
		gcodeBrowseButton.setEnabled(false);
		gcodeBrowseButton.setBackground(Color.WHITE);
		gcodeBrowseButton.setVerticalAlignment(AbstractButton.CENTER);
		gcodeBrowseButton.setHorizontalAlignment(AbstractButton.CENTER);
		d = gcodeBrowseButton.getPreferredSize();
		gcodeBrowseButton.setMaximumSize(new Dimension(d.width, d.height));
		gcodeBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				File file = browse();
				if (file != null)
					saveGCode(file);
			}});
		gcodeBox.add(gcodeBrowseButton);

		stopButton = new JButton("Stop");
		stopButton.setBackground(Color.WHITE);
		//    stopButton.setVerticalAlignment(AbstractButton.CENTER);
		//    stopButton.setHorizontalAlignment(AbstractButton.CENTER);
		stopButton.setAlignmentX(0.5f);
		d = stopButton.getPreferredSize();
		stopButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { stopProcess(); } });
		stopButton.setEnabled(false);

		innerProgressBar = new JProgressBar();
		innerProgressBar.setBackground(Color.WHITE);

		outerProgressBar = new JProgressBar();
		outerProgressBar.setBackground(Color.WHITE);

		Box processingBox = Box.createVerticalBox();
		processingBox.setBorder(BorderFactory.createTitledBorder("Processing"));
		processingBox.add(mosaicBox);
		processingBox.add(topologyAndToolpathsBox);
		processingBox.add(gcodeBox);
		//    processingBox.add(Box.createVerticalStrut(8));
		processingBox.add(stopButton);
		//    processingBox.add(Box.createVerticalStrut(8));
		processingBox.add(innerProgressBar);
		processingBox.add(outerProgressBar);


		Box box = Box.createVerticalBox();

		box.add(loadBox);
		box.add(display);
		box.add(model);
		box.add(processingBox);

		setLayout(new BorderLayout());
		add(box, "Center");

		//make display take up max available space

		Dimension orig = getPreferredSize();

		d = loadBox.getPreferredSize();
		loadBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

		d = model.getPreferredSize();
		model.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

		d = processingBox.getPreferredSize();
		processingBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

		display.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setPreferredSize(orig);

		if (file != null)
			loadFile(file);
	}

	public void saveMosaic() {
		saveMosaic(new File(mosaicField.getText().trim()));
	}

	public void saveMosaic(File file) {
		saveMosaic(file, mosaicTilesButton.isSelected());
	}

	public void saveMosaic(File file, boolean tiles) {

		try {

			mosaicField.setText(file.toString());

			if (file.exists() &&
					!(JOptionPane.
							showConfirmDialog(this,
									"Overwrite existing mosaic file " + file + "?",
									"Overwrite?",
									JOptionPane.YES_NO_OPTION) ==
										JOptionPane.YES_OPTION))
				return;

			startProcess(new SaveMosaic(this, file, tiles));

		} catch (AccessControlException e1) {
			accessControlError();
		}
	}

	private void fixTopology() {
		startProcess(new TopologyProcessor(this));
	}

	private void computeToolpaths() {

		int mode = -1;

		if (voronoiButton.isSelected())
			mode = ToolpathsProcessor.VORONOI_MODE;
		else
			mode = ToolpathsProcessor.OUTLINE_MODE;

		toolpathsProcessor = new ToolpathsProcessor(this, mode);

		startProcess(toolpathsProcessor);
	}

	private File browse() {

		try {

			String dir = System.getProperty("user.dir");
			if (currentFile != null)
				dir = (currentFile.getParent()).toString();

			JFileChooser chooser = new JFileChooser(dir);

			//chooser.setFileFilter(fileFilter);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				return chooser.getSelectedFile();
			else
				return null;

		} catch (AccessControlException e1) {
			accessControlError();
			return null;
		}
	}

	private void accessControlError() {
		JOptionPane.
		showMessageDialog(this,
				"Cannot load or save from an applet.  You must " +
				"either change your browser's security policy or " +
				"download the application and run it directly.",
				"Access Denied",
				JOptionPane.ERROR_MESSAGE);
	}

	public void destroy() {
		display.destroy();
	}

	public void loadFile() {
		loadFile(new File(loadField.getText().trim()));
	}

	public void loadDemo() {
		loadURL(getClass().getResource(DEMO_FILE));
		loadField.setText("[built-in demo]");
	}

	public void loadURL(URL url) {
		try {
			load(url.openStream());
		} catch (IOException e) {
			JOptionPane.
			showMessageDialog(this,
					"I/O Error: " + e.getMessage(),
					"I/O Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void loadFile(File file) {

		currentFile = file;
		loadField.setText(file.toString());

		try {
			load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			JOptionPane.
			showMessageDialog(this,
					"File Not Found: " + e.getMessage(),
					"File Not Found",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void load(InputStream inputStream) {

		stopProcess();

		simulator.reset();

		try {
			Parser parser = new Parser(inputStream);
			parser.setSimulator(simulator);
			parser.Input();
		} catch (ParseException e) {
			JOptionPane.
			showMessageDialog(this,
					"Parse Error: " + e.getMessage(),
					"Parse Error",
					JOptionPane.ERROR_MESSAGE);
		}

		model.rebuild();
	}

	public void saveGCode() {
		saveGCode(new File(gcodeField.getText().trim()));
	}

	public void saveGCode(final File file) {

		gcodeField.setText(file.toString());

		if (toolpathsProcessor == null) {
			return;
		}

		try {

			if (file.exists()) {
				int yesno = JOptionPane.
							showConfirmDialog(this,
									"Overwrite existing G-Code file " + file + "?",
									"Overwrite?",
									JOptionPane.YES_NO_OPTION);
				if (yesno != JOptionPane.YES_OPTION) {					
					return;
				}
			}

			try {
				FileWriter w = new FileWriter(file);
				toolpathsProcessor.writeGCode(w);
				w.close();
			} catch (IOException e) {
				JOptionPane.
				showMessageDialog(this,
						"I/O Error writing G-Code: " + e.getMessage(),
						"I/O Error",
						JOptionPane.ERROR_MESSAGE);
			}

		} catch (AccessControlException e1) {

			accessControlError();

			try {
				toolpathsProcessor.writeGCode(null);
			} catch (IOException e2) {
				//nope
			}
		}
	}

	public void mouseClicked(double x, double y, int modifiers) {

		SortedSet<Net> clickedNets = new TreeSet<Net>();

		model.getNetsAtPoint(x, y, 1.0/display.getDPI(), clickedNets);

		if (manualTopology.isSelected()) {
			clearSelection();
			TopologyProcessor.mergeNets(clickedNets);
			return;
		}

		if ((selectedNet != null) && clickedNets.contains(selectedNet)) {

			Iterator<Net> it = (clickedNets.tailSet(selectedNet)).iterator();

			it.next();

			if (it.hasNext()) {
				selectedNet = it.next();
			} else {
				selectedNet = clickedNets.iterator().next();
			}

		} else {

			selectedNet = null;

			if (!clickedNets.isEmpty()) {
				selectedNet = clickedNets.iterator().next();
			}
		}

		Net selectedNetSave = selectedNet;

		if (!((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0))
			clearSelection();

		selectedNet = selectedNetSave;

		if (selectedNet != null) {
			selectedNets.add(selectedNet);
			selectedNet.setHighlighted(true);
		}
	}

	public void clearSelection() {

		for (Iterator<Net> it = selectedNets.iterator(); it.hasNext(); ) {
			it.next().setHighlighted(false);
		}

		selectedNets.clear();

		selectedNet = null;
	}

	public void keyReleased(KeyEvent e) {

		switch (e.getKeyCode()) {

		case KeyEvent.VK_F: startProcess(new FatnessProcessor(this)); break;

		case KeyEvent.VK_D: model.dump(); break;

		case KeyEvent.VK_I: {

			System.out.println(selectedNets.size() + " selected nets:");

			for (Net net : selectedNets) {
				net.dump();
			}

			break;
		}

		case KeyEvent.VK_DELETE: {

			for (Net net : selectedNets) {

				Set<Net> superNet = net.getSuperNet();

				if (superNet == null) {
					model.deleteNet(net);
				} else {
					for (Iterator<Net> jt = superNet.iterator(); jt.hasNext(); ) {
						model.deleteNet(jt.next());
					}
				}
			}

			undoHistory.add(0, new UndoDelete(selectedNets));

			clearSelection();

			break;
		}

		case KeyEvent.VK_U: {

			if (!undoHistory.isEmpty()) {

				Iterator<UndoTask> it = undoHistory.iterator();

				UndoTask undoTask = it.next();
				it.remove();

				undoTask.undo();
			}

			break;
		}
		}
	}

	public boolean askContinue(int line, int seq) {
		return
		JOptionPane.
		showConfirmDialog(this,
				"Continue after line " + line +
				((seq >= 0) ? ("(sequence number " + seq + ")") : "") +
				"?",
				"Continue?",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public Model getModel() {
		return model;
	}

	public Display getDisplay() {
		return display;
	}

	public void enableControls(boolean enable) {

		loadField.setEnabled(enable);
		loadButton.setEnabled(enable);

		mosaicButton.setEnabled(enable);
		mosaicField.setEnabled(enable);
		mosaicBrowseButton.setEnabled(enable);
		mosaicTilesButton.setEnabled(enable);

		topologyButton.setEnabled(enable);
		manualTopology.setEnabled(enable);

		toolpathsButton.setEnabled(enable);
		voronoiButton.setEnabled(enable);
		outlineButton.setEnabled(enable);

		if (toolpathsProcessor != null) {
			gcodeButton.setEnabled(enable);
			gcodeField.setEnabled(enable);
			gcodeBrowseButton.setEnabled(enable);
		}

	}

	public void resetOuterProgressBar(int numSteps) {
		outerProgressBar.setMaximum(numSteps);
		outerProgressBar.setValue(0);
	}

	public void tickOuterProgressBar() {
		outerProgressBar.setValue(outerProgressBar.getValue()+1);
	}

	public void resetInnerProgressBar(int numSteps) {
		innerProgressBar.setMaximum(numSteps);
		innerProgressBar.setValue(0);
	}

	public void tickInnerProgressBar() {
		innerProgressBar.setValue(innerProgressBar.getValue()+1);
	}

	public void startProcess(Processor processor) {

		stopProcess();

		this.processor = processor;

		stopButton.setEnabled(true);

		processor.start();
	}

	public void stopProcess() {

		if (processor == null)
			return;

		try {
			processor.stop();
		} catch (InterruptedException e) {
			System.err.println("WARNING: interrupted while stopping process");
		}

		processFinished();
	}

	public void processFinished() {

		outerProgressBar.setValue(0);
		innerProgressBar.setValue(0);

		stopButton.setEnabled(false);

		processor = null;
	}

	public void addFrameTask(Runnable task) {
		if (display != null)
			display.addFrameTask(task);
		else
			task.run();
	}

	private interface UndoTask {
		public void undo();
	}

	private class UndoDelete implements UndoTask {

		private UndoDelete(Collection<Net> nets) {

			for (Net net : nets) {

				Set<Net> superNet = net.getSuperNet();

				if (superNet == null) {
					this.nets.add(net);
				} else {
					this.nets.addAll(superNet);
				}
			}
		}

		public void undo() {
			//      System.out.println("undeleting " + nets.size() + " nets");
			for (Net net : nets) {
				model.undeleteNet(net);
			}
		}

		private Collection<Net> nets = new LinkedHashSet<Net>();
	}


	private Simulator simulator;
	private Model model;
	private Display display;

	private JTextField loadField;
	private JButton loadButton;

	private File currentFile = null;

	private JButton mosaicButton;
	private JTextField mosaicField;
	private JButton mosaicBrowseButton;
	private JCheckBox mosaicTilesButton;

	private JProgressBar innerProgressBar;
	private JProgressBar outerProgressBar;

	private Processor processor = null;

	private JButton stopButton;

	private JButton topologyButton;
	private JCheckBox manualTopology;

	private JButton toolpathsButton;
	private JRadioButton voronoiButton;
	private JRadioButton outlineButton;

	private JButton gcodeButton;
	private JTextField gcodeField;
	private JButton gcodeBrowseButton;

	/**
	 * The ToolpathsProcessor generates the g-code that we write to a file.
	 */
	private ToolpathsProcessor toolpathsProcessor = null;

	private Set<Net> selectedNets = new LinkedHashSet<Net>();
	private Net selectedNet = null;

	private List<UndoTask> undoHistory = new LinkedList<UndoTask>();
}
