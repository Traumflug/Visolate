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

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Main extends JApplet {

  private static final String cvsid =
  "$Id: Main.java,v 1.5 2006/08/29 00:13:02 vona Exp $";

  public static final String APPNAME = "Visolate 2.0.0";
  public static final int DEF_LOC_X = 100;
  public static final int DEF_LOC_Y = 100;

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
 
  public void destroy() {
    visolate.destroy();
  }

  public static void main(final String[] argv) {
  
    final JFrame frame = new JFrame(APPNAME);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(DEF_LOC_X, DEF_LOC_Y);

    final Visolate visolate = new Visolate();

    Container contentPane = frame.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(visolate, "Center");
    contentPane.setBackground(Color.WHITE);

    SwingUtilities.invokeLater(
      new Runnable() {
          public void run() {

            frame.pack();
            frame.setVisible(true);

            if (argv.length > 0)
              visolate.loadFile(new File(argv[0]));
            else
              visolate.loadDemo();
          }
      });
  }

  private Visolate visolate;
}
