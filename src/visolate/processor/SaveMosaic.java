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

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import visolate.*;

public class SaveMosaic extends MosaicProcessor {

  public SaveMosaic(Visolate visolate,
                    File mosaicFile,
                    boolean individualTiles) {
    super(visolate);

    this.mosaicFile = mosaicFile;
    this.individualTiles = individualTiles;

    fileName = mosaicFile.toString();

    int dot = fileName.lastIndexOf(".");

    if ((dot < 0) || (dot == fileName.length()-1)) {
      System.err.println("unspecified format (give a suffix like \".png\")");
      return;
    }

    fileNameBase = fileName.substring(0, dot);
    fileNameSuffix = fileName.substring(dot+1, fileName.length());

    Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(fileNameSuffix);

    if (!it.hasNext()) {
      System.err.println("cannot write format \"" + fileNameSuffix + "\"");
      return;
    }

    imageWriter = (ImageWriter) it.next();
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

    if (!individualTiles)
      return;

    if (imageWriter == null)
      return;

    String rText = Integer.toString(r);
    for (int i = 0; i < 3; i++)
      if (rText.length() < 3)
        rText = "0" + rText;

    String cText = Integer.toString(c);
    for (int i = 0; i < 3; i++)
      if (cText.length() < 3)
        cText = "0" + cText;

    File file = new File(fileNameBase +
                         "-" + rText + "-" + cText +
                         "." + fileNameSuffix);

    if ((width == tile.getWidth()) && (height == tile.getHeight())) {
      saveFile(file, tile);
    } else {
      BufferedImage cropTile  = display.makeBufferedImage(width, height);
      Graphics2D cropG2D = (Graphics2D) (cropTile.getGraphics());
      cropG2D.drawRenderedImage(tile, new AffineTransform());
      cropG2D.dispose();
      saveFile(file, cropTile);
    }
  }

  protected void processCompleted() {

    super.processCompleted();

    if (individualTiles)
      return;

    saveFile(mosaicFile, mosaic);
  }

  private void saveFile(File file, BufferedImage bufferedImage) {

    if (imageWriter == null)
      return;

    try {
      ImageOutputStream outputStream = new FileImageOutputStream(file);
      imageWriter.setOutput(outputStream);
      imageWriter.write(bufferedImage);
      outputStream.close();
    } catch (IOException e) {
      System.err.println("I/O Exception writing \"" + file + "\": " +
                         e.getMessage());
    }
  }

  private File mosaicFile;
  private boolean individualTiles;

  private String fileName;
  private String fileNameSuffix;
  private String fileNameBase;

  private ImageWriter imageWriter;
}
