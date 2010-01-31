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

package visolate.simulator;

import java.util.*;
import javax.vecmath.*;

public class LineVectorInstance extends OutlineInstance {

	private static final String cvsid =
		"$Id: LineVectorInstance.java,v 1.2 2004/06/30 17:26:29 vona Exp $";


	public LineVectorInstance(final double width,
			final double xStart, final double yStart,
			final double xEnd, final double yEnd,
			final double rotation) {
		super(4, computePoints(width, xStart, yStart, xEnd, yEnd), rotation);

//		this.width = width;
//
//		this.xStart = xStart;
//		this.yStart = yStart;
//
//		this.xEnd = xEnd;
//		this.yEnd = yEnd;
//
//		this.rotation = rotation;
	}

	private static List<Point2d> computePoints(final double width,
			final double xStart, final double yStart,
			final double xEnd, final double yEnd) {

		List<Point2d> points = new LinkedList<Point2d>();

		Vector3d d = new Vector3d((xEnd-xStart), (yEnd-yStart), 0.0f);
		d.normalize();

		Vector3d n = new Vector3d();
		n.cross(d, Z);

		//no offset here, it will be takencare of in OutlineInstance.getPerimeter()

		points.add(new Point2d((xStart + (width/2)*n.x),
				(yStart + (width/2)*n.y)));

		points.add(new Point2d((xStart - (width/2)*n.x),
				(yStart - (width/2)*n.y)));

		points.add(new Point2d((xEnd - (width/2)*n.x),
				(yEnd - (width/2)*n.y)));

		points.add(new Point2d((xEnd + (width/2)*n.x),
				(yEnd + (width/2)*n.y)));

		return points;
	}

//	private double width;
//
//	private double xStart, yStart;
//
//	private double xEnd, yEnd;
//
//	private double rotation;
}
