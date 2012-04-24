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
  
  public static final String APPNAME = "Visolate 3.0.1";
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
    options.addOption( "x", "flip-x", false, "flip around x axis" );
    options.addOption( "y", "flip-y", false, "flip around y axis" );
    options.addOption( "absolute", false, "use absolute cooridnates" );
    options.addOption( "d", "dpi", true, "dpi to use for rastering");
    options.addOption( "a", "auto", false, "auto-mode (run, save and exit)");
    options.addOption( "o", "outfile", true, "name of output file");

    options.addOption( "h", "help", false, "display this help and exit" );
    options.addOption( "version", false, "output version information and exit" );

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

            visolate.model.rebuild();

            if (visolate.commandline.hasOption("auto")) {
                     System.out.println("Automatic processing enabled! Files will be overwritten without asking!");
                     visolate.auto_mode=true;
            }

            if (visolate.commandline.hasOption("dpi")) {
                    visolate.getDisplay().setDPI(Integer.parseInt(visolate.commandline.getOptionValue("dpi")));
            }

            if (visolate.commandline.hasOption("flip-x")) {
                    visolate.model.setFlipX(true);
            }
            if (visolate.commandline.hasOption("flip-y")) {
                    visolate.model.setFlipY(true);
            }

            if (visolate.commandline.hasOption("absolute")) {
                    visolate.setAbsoluteCoordinates(true);
            }

            if (visolate.commandline.hasOption("outfile")) {
                    visolate.setGcodeFile(visolate.commandline.getOptionValue("outfile"));
            }

            if (visolate.commandline.hasOption("auto")) {
                     System.out.println("now starting fixing topology due to automatic mode");
                     visolate.processstatus=1;

                     visolate.fixTopology();
                     // fix.Topology() calls visolate.processFinished after its done. Also, the Toolpathprocessor does so. processstatus discriminates this.
            }
        }

      });
  }

  private Visolate visolate;
}
