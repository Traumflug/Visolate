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

package visolate;

import visolate.parser.*;
import visolate.simulator.*;
import visolate.model.*;
import visolate.processor.*;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.net.*;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.*;

import org.apache.commons.cli.CommandLine;

import visolate.processor.GCodeFileWriter;

public class Visolate extends JPanel implements SimulatorUI {

  private static final long serialVersionUID = 1L;

	public static final String DEMO_FILE = "example.grb";
	/**
	 * kept here so it is available in the invokeLater Runnable in Main
	 */
	public CommandLine commandline;
	public int processstatus;
	public boolean auto_mode;

	private double selectedInitialYCoordinate = 0;


	public Visolate() {
		this(null);
	}

	public Visolate(File file) {

		processstatus=0;
		display = new Display(this);
		simulator = new Simulator(this);
		model = new Model(this);
		// TODO: get a toolpathsProcessor here, too, and get rid of myToolpathsProcessor.
		gCodeWriter = new GCodeFileWriter();

		setBackground(Color.WHITE);
		setOpaque(true);

		Dimension d;


		Box processingBox = getProcessingBox();


		Box box = Box.createVerticalBox();

		box.add(getLoadFileBox());
		box.add(display);
		box.add(model);
		box.add(getGCodeOptionsBox());
		box.add(processingBox);

		setLayout(new BorderLayout());
		add(box, "Center");

		//make display take up max available space

		Dimension orig = getPreferredSize();

		d = getLoadFileBox().getPreferredSize();
		getLoadFileBox().setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

		d = model.getPreferredSize();
		model.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

		d = processingBox.getPreferredSize();
		processingBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));

		display.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setPreferredSize(orig);

		if (file != null)
			loadFile(file);
	}

	private Box getLoadFileBox() {
		if (myLoadFileBox == null) {
			myLoadFileBox = Box.createHorizontalBox();

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

			myLoadFileBox.add(loadField);

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

			myLoadFileBox.add(loadButton);

			myLoadFileBox.setBorder(BorderFactory.createTitledBorder("Input File"));
		}
		return myLoadFileBox;
	}

	private Box getGcodeBox() {
		if (myGcodeBox == null) {
			Dimension d;
			myGcodeBox = Box.createHorizontalBox();

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
			myGcodeBox.add(gcodeButton);

			gcodeField = new JTextField();
			gcodeField.setEnabled(false);
			d = gcodeField.getPreferredSize();
			gcodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
			gcodeField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { saveGCode(); } });
			//    gcodeField.addFocusListener(new FocusAdapter() {
			//        public void focusLost(FocusEvent e) { saveGCode(); } });
			myGcodeBox.add(gcodeField);

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
			myGcodeBox.add(gcodeBrowseButton);

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

			progressBar = new JProgressBar();
			progressBar.setBackground(Color.WHITE);
		}
		return myGcodeBox;
	}

	private Box getTopologyAndToolpathsBox() {
		Box topologyAndToolpathsBox = Box.createHorizontalBox();
		topologyAndToolpathsBox.add(getTopologyBox());
		topologyAndToolpathsBox.add(Box.createHorizontalGlue());
		topologyAndToolpathsBox.add(getToolpathBox());
		return topologyAndToolpathsBox;
	}

	private Box getToolpathBox() {
		Dimension d;
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
		// VORONOI_MODE is the default mode.
		voronoiButton.setSelected(true);
		toolpathsBox.add(voronoiButton);

		outlineButton = new JRadioButton("outline");
		modeGroup.add(outlineButton);
		outlineButton.setBackground(Color.WHITE);
    // VORONOI_MODE is the default mode.
		outlineButton.setSelected(false);
		toolpathsBox.add(outlineButton);

		return toolpathsBox;
	}

	private Box getTopologyBox() {
		Dimension d;
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
		return topologyBox;
	}

	private Box getMosaicBox() {
		Dimension d;
		if (myMosaicBox == null) {
			myMosaicBox = Box.createHorizontalBox();

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
			myMosaicBox.add(mosaicButton);

			mosaicField = new JTextField();
			d = mosaicField.getPreferredSize();
			mosaicField.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
			mosaicField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { saveMosaic(); } });
			//    mosaicField.addFocusListener(new FocusAdapter() {
			//        public void focusLost(FocusEvent e) { saveMosaic(); } });
			myMosaicBox.add(mosaicField);

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
			myMosaicBox.add(mosaicBrowseButton);

			mosaicTilesButton = new JCheckBox("individual tiles");
			mosaicTilesButton.setBackground(Color.WHITE);
			mosaicTilesButton.setSelected(false);
			myMosaicBox.add(mosaicTilesButton);
		}
		return myMosaicBox;
	}

	private Box getProcessingBox() {
		if (myProcessingBox == null) {
			myProcessingBox = Box.createVerticalBox();
			myProcessingBox.setBorder(BorderFactory.createTitledBorder("Processing"));
			myProcessingBox.add(getMosaicBox());
			myProcessingBox.add(getTopologyAndToolpathsBox());
			myProcessingBox.add(getGcodeBox());
			//    processingBox.add(Box.createVerticalStrut(8));
			myProcessingBox.add(stopButton);
			//    processingBox.add(Box.createVerticalStrut(8));
			myProcessingBox.add(progressBar);
		}
		return myProcessingBox;
	}

	private Box getGCodeOptionsBox() {
		if (myGCodeOptionsBox == null) {
			myGCodeOptionsBox = Box.createVerticalBox();
			myGCodeOptionsBox.setBorder(BorderFactory.createTitledBorder("G-Code"));
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(3, 4));

			panel.add(new JLabel("Coordinates"));
			panel.add(new JLabel("Z-coordinates"));
			panel.add(new JLabel("left upper coordinates"));
			panel.add(new JLabel("Metric"));
			panel.add(new JLabel("Feedrates"));

			panel.add(getRelativeCoordinatesButton());
			panel.add(getZCuttingHeightPanel());
			panel.add(getInitialXPanel());
			panel.add(getMetricButton());
			panel.add(getMillingSpeedPanel());

			panel.add(getAbsoluteCoordinatesButton());
			panel.add(getZDownMovementPanel());
			panel.add(getInitialYPanel());
			panel.add(getImperialButton());
			panel.add(getPlungeSpeedPanel());

			myGCodeOptionsBox.add(panel);
		}
		return myGCodeOptionsBox;
	}

	private JPanel getInitialXPanel() {
    // TODO: Don't store the panel, but the field instead.
		if (myInitialXPanel == null) {
			myInitialXPanel = new JPanel();
			myInitialXPanel.setLayout(new BorderLayout());
			myInitialXPanel.add(new JLabel("X"), BorderLayout.WEST);
			myInitialXPanel.setToolTipText("Left side is at this coordinate (mm or inch)");
			myInitialXPanel.setEnabled(gCodeWriter.getIsAbsolute());
      final JTextField field = new JTextField(NumberFormat.getInstance().format(gCodeWriter.getXOffset()));
			myInitialXPanel.add(field, BorderLayout.CENTER);
			myInitialXPanel.addPropertyChangeListener("enabled", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					field.setEnabled(myInitialXPanel.isEnabled());
				}
			});
      field.setEnabled(myInitialXPanel.isEnabled());
			// TODO: get rid fo this listener and fetch the value from the field
			//       just before the G-code file is written.
      //       See also dpiField in Display.java.
			field.getDocument().addUndoableEditListener(new UndoableEditListener() {
				@Override
				public void undoableEditHappened(UndoableEditEvent evt) {
					try {
            gCodeWriter.setXOffset(NumberFormat.getInstance().parse(field.getText()).doubleValue());
					} catch (ParseException e) {
						evt.getEdit().undo();
					}
				}
			});
		}
		return myInitialXPanel;
	}
	
	private JPanel getInitialYPanel() {
	  // TODO: Don't store the panel, but the field instead.
		if (myInitialYPanel == null) {
			myInitialYPanel = new JPanel();
			myInitialYPanel.setLayout(new BorderLayout());
			myInitialYPanel.add(new JLabel("Y"), BorderLayout.WEST);
			myInitialYPanel.setToolTipText("Upper side is at this coordinates (mm or inch)");
			myInitialYPanel.setEnabled(gCodeWriter.getIsAbsolute());
			final JTextField field = new JTextField(NumberFormat.getInstance().format(0.0));
			myInitialYPanel.add(field, BorderLayout.CENTER);
			myInitialYPanel.addPropertyChangeListener("enabled", new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					field.setEnabled(myInitialYPanel.isEnabled());
				}
			});
      field.setEnabled(myInitialYPanel.isEnabled());

      // TODO: get rid fo this listener and fetch the value from the field
      //       just before the G-code file is written.
      //       See also dpiField in Display.java.
			field.getDocument().addUndoableEditListener(new UndoableEditListener() {
				
				@Override
				public void undoableEditHappened(UndoableEditEvent evt) {
					try {
						selectedInitialYCoordinate = NumberFormat.getInstance().parse(field.getText()).doubleValue();
						if (myToolpathsProcessor != null) {
							myToolpathsProcessor.setAbsoluteYStart(selectedInitialYCoordinate);
						}
					} catch (ParseException e) {
						evt.getEdit().undo();
					}
					
				}
			});
		}
		return myInitialYPanel;
	}
	private Component getZDownMovementPanel() {
    // TODO: Don't store the panel, but the field instead.
		if (myZDownMovementPanel == null) {
			myZDownMovementPanel = new JPanel();
			myZDownMovementPanel.setLayout(new BorderLayout());
			myZDownMovementPanel.add(new JLabel("travel clearance"), BorderLayout.WEST);
			myZDownMovementPanel.setToolTipText("When not cutting, lift the cutter to this above origin, in mm or inch. Decimals in native language (point or comma).");
			final JTextField field = new JTextField(NumberFormat.getInstance().format(gCodeWriter.getZClearance()));
			myZDownMovementPanel.add(field, BorderLayout.CENTER);
      // TODO: get rid fo this listener and fetch the value from the field
      //       just before the G-code file is written.
      //       See also dpiField in Display.java.
			field.getDocument().addUndoableEditListener(new UndoableEditListener() {
				
				@Override
				public void undoableEditHappened(UndoableEditEvent evt) {
					try {
						gCodeWriter.setZClearance(NumberFormat.getInstance().parse(field.getText()).doubleValue());
					} catch (ParseException e) {
						evt.getEdit().undo();
					}
					
				}
			});
		}
		return myZDownMovementPanel;
	}

	private JPanel getMillingSpeedPanel() {
    // TODO: Don't store the panel, but the field instead.
		if (myMillingSpeedPanel == null) {
			myMillingSpeedPanel = new JPanel();
			myMillingSpeedPanel.setLayout(new BorderLayout());
			myMillingSpeedPanel.add(new JLabel("cutting feedrate"), BorderLayout.WEST);
			myMillingSpeedPanel.setToolTipText("Feedrate during cutting in mm or inch per minute.");
			final JTextField field = new JTextField(NumberFormat.getInstance().format(0.0));
			myMillingSpeedPanel.add(field, BorderLayout.CENTER);

      // TODO: get rid fo this listener and fetch the value from the field
      //       just before the G-code file is written.
      //       See also dpiField in Display.java.
			field.getDocument().addUndoableEditListener(new UndoableEditListener() {
				


				@Override
				public void undoableEditHappened(UndoableEditEvent evt) {
					try {
						myMillingSpeed = NumberFormat.getInstance().parse(field.getText()).doubleValue();
						if (myToolpathsProcessor != null) {
							myToolpathsProcessor.setMillingSpeed(myMillingSpeed);
						}
					} catch (ParseException e) {
						evt.getEdit().undo();
					}
					
				}
			});
		}
		return myMillingSpeedPanel;
	}

	private JPanel getPlungeSpeedPanel() {
    // TODO: Don't store the panel, but the field instead.
		if (myPlungeSpeedPanel == null) {
			myPlungeSpeedPanel = new JPanel();
			myPlungeSpeedPanel.setLayout(new BorderLayout());
			myPlungeSpeedPanel.add(new JLabel("plunge feedrate"), BorderLayout.WEST);
			myPlungeSpeedPanel.setToolTipText("Feedrate when moving vertically into the workpiece in mm or inch per minute.");
			final JTextField field = new JTextField(NumberFormat.getInstance().format(0.0));
			myPlungeSpeedPanel.add(field, BorderLayout.CENTER);

      // TODO: get rid fo this listener and fetch the value from the field
      //       just before the G-code file is written.
      //       See also dpiField in Display.java.
			field.getDocument().addUndoableEditListener(new UndoableEditListener() {
				
				@Override
				public void undoableEditHappened(UndoableEditEvent evt) {
					try {
						myPlungeSpeed = NumberFormat.getInstance().parse(field.getText()).doubleValue();
						if (myToolpathsProcessor != null) {
							myToolpathsProcessor.setPlungeSpeed(myPlungeSpeed);
						}
					} catch (ParseException e) {
						evt.getEdit().undo();
					}
					
				}
			});
		}
		return myPlungeSpeedPanel;
	}

	private JPanel getZCuttingHeightPanel() {
    // TODO: Don't store the panel, but the field instead.
		if (myZCuttingHeightPanel == null) {
			myZCuttingHeightPanel = new JPanel();
			myZCuttingHeightPanel.setLayout(new BorderLayout());
			myZCuttingHeightPanel.add(new JLabel("cutting height"), BorderLayout.WEST);
			myZCuttingHeightPanel.setToolTipText("When cutting, the head should have this z-coordinate, in mm or inch. Likely a negative value, decimals in native language (point or comma)");
      myZCuttingHeightPanel.setEnabled(gCodeWriter.getIsAbsolute());
			final JTextField field = new JTextField(NumberFormat.getInstance().format(gCodeWriter.getZCuttingHeight()));
			myZCuttingHeightPanel.add(field, BorderLayout.CENTER);
			myZCuttingHeightPanel.addPropertyChangeListener("enabled", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					field.setEnabled(myZCuttingHeightPanel.isEnabled());
				}
			});
      field.setEnabled(myZCuttingHeightPanel.isEnabled());

      // TODO: get rid fo this listener and fetch the value from the field
      //       just before the G-code file is written.
			//       See also dpiField in Display.java.
			field.getDocument().addUndoableEditListener(new UndoableEditListener() {
				
				@Override
				public void undoableEditHappened(UndoableEditEvent evt) {
					try {
            gCodeWriter.setZCuttingHeight(NumberFormat.getInstance().parse(field.getText()).doubleValue());
					} catch (ParseException e) {
						evt.getEdit().undo();
					}
					
				}
			});
		}
		return myZCuttingHeightPanel;
	}

	private JRadioButton getAbsoluteCoordinatesButton() {
		if (myAbsoluteCoordinatesButton == null) {
			myAbsoluteCoordinatesButton = new JRadioButton("absolute");
			myAbsoluteCoordinatesButton.setSelected(gCodeWriter.getIsAbsolute());
      myAbsoluteCoordinatesButton.setToolTipText("Output absolute G-code coordinates. For the starting point, set X, Y and cutting height.");
			myAbsoluteCoordinatesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					setAbsoluteCoordinates(myAbsoluteCoordinatesButton.isSelected());
				}
			});
		}
		return myAbsoluteCoordinatesButton;
	}

	private JRadioButton getRelativeCoordinatesButton() {
		if (myRelativeCoordinatesButton == null) {
			myRelativeCoordinatesButton = new JRadioButton("relative");
			myRelativeCoordinatesButton.setSelected( ! gCodeWriter.getIsAbsolute());
			myRelativeCoordinatesButton.setToolTipText("Output relative G-code coordinates, starting at { 0.0, 0.0, 0.0 }.");
			myRelativeCoordinatesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					setAbsoluteCoordinates( ! myRelativeCoordinatesButton.isSelected());
				}
			});
		}
		return myRelativeCoordinatesButton;
	}

	public void setAbsoluteCoordinates(final boolean newValue) {
		getAbsoluteCoordinatesButton().setSelected(newValue);
		getRelativeCoordinatesButton().setSelected(!newValue);
		getZCuttingHeightPanel().setEnabled(newValue);
		getInitialXPanel().setEnabled(newValue);
		getInitialYPanel().setEnabled(newValue);
		gCodeWriter.setIsAbsolute(newValue);
	}

	private JRadioButton getMetricButton() {
		if (myMetricButton == null) {
			myMetricButton = new JRadioButton("metric");
			myMetricButton.setSelected(gCodeWriter.getIsMetric());
			myMetricButton.setToolTipText("Output G-code in mm. Coordinates entered here are in mm, too.");
			myMetricButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (myMetricButton.isSelected()) {
						getImperialButton().setSelected(false);
						gCodeWriter.setIsMetric(true);
					}
				}
			});
		}
		return myMetricButton;
	}

	private JRadioButton getImperialButton() {
		if (myImperialButton == null) {
			myImperialButton = new JRadioButton("imperial");
			myImperialButton.setSelected( ! gCodeWriter.getIsMetric());
			myImperialButton.setToolTipText("Output G-code in inches. Coordinates entered here are in inches, too.");
			myImperialButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (myImperialButton.isSelected()) {
						getMetricButton().setSelected(false);
						gCodeWriter.setIsMetric(false);
					}
				}
			});
		}
		return myImperialButton;
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

	public void fixTopology() {
		startProcess(new TopologyProcessor(this));
	}

	private void computeToolpaths() {

		int mode = -1;

		if (voronoiButton.isSelected())
			mode = ToolpathsProcessor.VORONOI_MODE;
		else
			mode = ToolpathsProcessor.OUTLINE_MODE;

		myToolpathsProcessor = new ToolpathsProcessor(this, mode);
		myToolpathsProcessor.setAbsoluteYStart(selectedInitialYCoordinate);
		myToolpathsProcessor.setMillingSpeed(myMillingSpeed);
		myToolpathsProcessor.setPlungeSpeed(myPlungeSpeed);

		startProcess(myToolpathsProcessor);
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
		setGcodeFile(file.toString()+".ngc");

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
		} catch (visolate.parser.ParseException e) {
			JOptionPane.
			showMessageDialog(this,
					"Parse Error: " + e.getMessage(),
					"Parse Error",
					JOptionPane.ERROR_MESSAGE);
		}

		model.rebuild();
	}

	public void setGcodeFile(String filename) {
		gcodeField.setText(filename);
	}

	public void saveGCode() {
		saveGCode(new File(gcodeField.getText().trim()));
	}

	public void saveGCode(final File file) {

		gcodeField.setText(file.toString());

		if (myToolpathsProcessor == null) {
			return;
		}

		try {

			if (file.exists()&& (!auto_mode)) {
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
			  // TODO: set up the writer here. Currently this is done
			  //       from the field's editListeners via the toolpathsProcessor.
			  // TODO: read the relevant fields and write them back,
			  //       so the user sees what was taken.
				gCodeWriter.open(file);
				myToolpathsProcessor.writeGCode(gCodeWriter);
				gCodeWriter.close();
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
				myToolpathsProcessor.writeGCode(null);
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

		if (myToolpathsProcessor != null) {
			gcodeButton.setEnabled(enable);
			gcodeField.setEnabled(enable);
			gcodeBrowseButton.setEnabled(enable);
		}

	}

	public void resetProgressBar(int numSteps) {
		progressBar.setMaximum(numSteps);
		progressBar.setValue(0);
	}

	public void tickProgressBar() {
		progressBar.setValue(progressBar.getValue()+1);
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

		progressBar.setValue(0);

		stopButton.setEnabled(false);

		processor = null;

		if (processstatus == 1) {  //returning from automated topology fixing
		  processstatus=2;
		  computeToolpaths();
		} else if (processstatus==2) { //returning from automated toolpath creation
		  System.out.println("Writing to gcode file: "+gcodeField.getText().trim());
		  saveGCode(new File(gcodeField.getText().trim()));
		  System.out.println("Exiting, all work done");
		  System.exit(0);
		}

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


	private Simulator simulator = null;
	public Model model = null;
	private Display display = null;
	private Processor processor = null;
	private ToolpathsProcessor myToolpathsProcessor = null;
	private GCodeFileWriter gCodeWriter = null;

	private JTextField loadField;
	private JButton loadButton;

	private File currentFile = null;

	private JButton mosaicButton;
	private JTextField mosaicField;
	private JButton mosaicBrowseButton;
	private JCheckBox mosaicTilesButton;

	private JProgressBar progressBar;


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

	private Set<Net> selectedNets = new LinkedHashSet<Net>();
	private Net selectedNet = null;

	private List<UndoTask> undoHistory = new LinkedList<UndoTask>();

	private Box myProcessingBox;
	private Box myGCodeOptionsBox;

	private Box myMosaicBox;

	private Box myLoadFileBox;

	private Box myGcodeBox;

	private JRadioButton myRelativeCoordinatesButton;
	private JRadioButton myAbsoluteCoordinatesButton;
	private JRadioButton myImperialButton;
	private JRadioButton myMetricButton;
	private JPanel myZCuttingHeightPanel;
	private JPanel myZDownMovementPanel;
	private JPanel myMillingSpeedPanel;
	private JPanel myPlungeSpeedPanel;
	private JPanel myInitialXPanel;
	private JPanel myInitialYPanel;
	private double myMillingSpeed = 2;
	private double myPlungeSpeed = 2;
}
