/*
 * GradientPainter.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 21. Februar 2006, 11:49
 *
 */

package de.cismet.tools.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * @author Christopher Butler
 */
public class GradientPainter {
	private Color startColor;
	private Color midColor;

	
	public GradientPainter(Color start, Color mid) {
		startColor = start;
		midColor = mid;
	}
	
	public void paintGradient(JComponent comp, Graphics g) {
		int h = comp.getHeight();
		int w = comp.getWidth();
		int mid = w/2;

		Color bgColor = comp.getBackground();
		Color start = startColor==null? bgColor: startColor;
		Color middle = midColor==null? bgColor: midColor;

		GradientPaint firstHalf = new GradientPaint(0, 0, start, mid, 0, middle);
		GradientPaint secondHalf = new GradientPaint(mid, 0, middle, w, 0, bgColor);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(firstHalf);
		g2.fillRect(0, 0, mid, h);
		g2.setPaint(secondHalf);
		g2.fillRect(mid-1, 0, mid, h);
	}

	public Color getMidColor() {
		return midColor;
	}
	public void setMidColor(Color midColor) {
		this.midColor = midColor;
	}
	public Color getStartColor() {
		return startColor;
	}
	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}
}
