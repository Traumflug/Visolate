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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.*;

public class Main extends JApplet {

  private static final long serialVersionUID = 1L;

  public static final String APPNAME = "Visolate 3.1.1";
  public static final int DEF_LOC_X = 100;
  public static final int DEF_LOC_Y = 100;

  // This is needed when Visolate is run as an applet.
  public void init() {

    visolate = new Visolate();

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(visolate, "Center");
    contentPane.setBackground(Color.WHITE);

    SwingUtilities.invokeLater(
      new Runnable() {
          public void run() {
            visolate.loadDemo();
          }
        });

  }

  // This is needed when Visolate is run as an applet.
  public void destroy() {
    visolate.destroy();
  }

  public static void main(final String[] argv) {

    CommandLineParser parser = new PosixParser();
    Options options = new Options();

    options.addOption( "h", "help", false, "Display this help and exit." );
    options.addOption( "V", "version", false, "Output version information and exit." );

    options.addOption( "fx", "flip-x", false, "Flip the geometry around the X axis." );
    options.addOption( "fy", "flip-y", false, "Flip the geometry around the Y axis." );
    options.addOption( "i", "inch", false, "Output G-code in inches. The default is mm." );
    options.addOption( "r", "relative", false, "Output relative G-code coordinates, starting at { 0.0, 0.0, 0.0 }. The default is absolute coordinates." );
    options.addOption( "ch", "cutting-height", true, "When cutting, the head should have this z-coordinate, in mm or inch. Likely a negative value." );
    options.addOption( "tc", "travel-clearance", true, "When not cutting, lift the cutter to this above origin, in mm or inch." );
    options.addOption( "xo", "x-offset", true, "Left side is at this coordinate (mm or inch)." );
    options.addOption( "yo", "y-offset", true, "Upper side is at this coordinates (mm or inch)." );
    options.addOption( "cf", "cutting-feedrate", true, "Feedrate during cutting in mm or inch per minute." );
    options.addOption( "pf", "plunge-feedrate", true, "Feedrate when moving vertically into the workpiece in mm or inch per minute." );
    options.addOption( "d", "dpi", true, "The dpi value to use for rastering. This influences the granularity of the G-code output.");
    options.addOption( "a", "auto", false, "Auto-mode (run, save and exit).");
    options.addOption( "o", "outfile", true, "Name of output file.");

    CommandLine commandline;
    try {
            commandline = parser.parse(options, argv);
    } catch (ParseException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
            return; // make it clear to the compiler that the following code is not run
    }

    if (commandline.hasOption("version")) {
        System.out.println(APPNAME);
        return;
    }

    if (commandline.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("visolate [options] [filename]", options );
        return;
    }

    if (commandline.getArgs().length >= 2) {
            System.err.println("Error: Too many arguments.");
            System.exit(1);
    }

    final JFrame frame = new JFrame(APPNAME);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(DEF_LOC_X, DEF_LOC_Y);

    // Add the Enter key to the forward traversal keys, so fields loose focus
    // when using it in a field and we don't need to set up both, an ActionListener
    // and a FocusListener for each text/number field.
    Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(
        frame.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(forwardKeys);
    newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    frame.setFocusTraversalKeys(
        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

    final Visolate visolate = new Visolate();
    visolate.commandline = commandline;

    Container contentPane = frame.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(visolate, "Center");
    contentPane.setBackground(Color.WHITE);

    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {

          frame.pack();
          frame.setVisible(true);

          if (visolate.commandline.getArgs().length == 1) {
            visolate.loadFile(new File(visolate.commandline.getArgs()[0]));
          } else {
            visolate.loadDemo();
          }

          if (visolate.commandline.hasOption("flip-x")) {
            visolate.model.setFlipX(true);
          }

          if (visolate.commandline.hasOption("flip-y")) {
            visolate.model.setFlipY(true);
          }

          if (visolate.commandline.hasOption("inch")) {
            visolate.gCodeWriter.setIsMetric(false);
          }

          if (visolate.commandline.hasOption("relative")) {
            visolate.gCodeWriter.setIsAbsolute(false);
          }

          if (visolate.commandline.hasOption("cutting-height")) {
            visolate.gCodeWriter.setZCuttingHeight(Double.parseDouble(visolate.commandline.getOptionValue("cutting-height")));
          }

          if (visolate.commandline.hasOption("travel-clearance")) {
            visolate.gCodeWriter.setZClearance(Double.parseDouble(visolate.commandline.getOptionValue("travel-clearance")));
          }

          if (visolate.commandline.hasOption("x-offset")) {
            visolate.gCodeWriter.setXOffset(Double.parseDouble(visolate.commandline.getOptionValue("x-offset")));
          }

          if (visolate.commandline.hasOption("y-offset")) {
            visolate.gCodeWriter.setYOffset(Double.parseDouble(visolate.commandline.getOptionValue("y-offset")));
          }

          if (visolate.commandline.hasOption("cutting-feedrate")) {
            visolate.gCodeWriter.setMillingFeedrate(Double.parseDouble(visolate.commandline.getOptionValue("cutting-feedrate")));
          }

          if (visolate.commandline.hasOption("plunge-feedrate")) {
            visolate.gCodeWriter.setPlungeFeedrate(Double.parseDouble(visolate.commandline.getOptionValue("plunge-feedrate")));
          }

          if (visolate.commandline.hasOption("dpi")) {
            visolate.getDisplay().setDPI(Integer.parseInt(visolate.commandline.getOptionValue("dpi")));
          }

          if (visolate.commandline.hasOption("outfile")) {
            visolate.setGcodeFile(visolate.commandline.getOptionValue("outfile"));
          }

          if (visolate.commandline.hasOption("auto")) {
            System.out.println("Automatic processing enabled! Files will be overwritten without asking!");
            System.out.println("Now starting fixing topology due to automatic mode.");
            visolate.auto_mode=true;
            visolate.processstatus=1;

            visolate.fixTopology();
            // fix.Topology() calls visolate.processFinished after its done. Also, the Toolpathprocessor does so. processstatus discriminates this.
          }

          visolate.model.rebuild();
        }
      });
  }

  private Visolate visolate;
}
