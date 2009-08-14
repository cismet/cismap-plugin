/*
 * Quote.java
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
 * Created on 28. Februar 2006, 16:46
 *
 */

package de.cismet.cismap.navigatorplugin;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.LayoutManager;
 
import javax.swing.JComponent;
import javax.swing.JPanel;
 
public class Quote extends JComponent {
 
	String mTxt;
	
	public Quote() {
		super();
	}
	
	public Quote(String s) {
		this();
		setText(s);
	}
	
	public void setText(String txt) {
		mTxt = txt;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();
		g.setColor(Color.RED);
		int wid = width / 10;
		int ht = height / 10;
		g.drawArc(wid / 2, ht / 2, wid * 9, ht * 9 , 0, -30);
		g.drawArc(wid / 2, ht / 2, wid * 9 , ht * 9, 0, 300);
		int ptx1 = (int) (width / 2 + wid * 18 / 4.0 * (Math.cos(30 * Math.PI / 180)));
		int pty1 = (int) (height / 2 + ht * 18 / 4.0 * (Math.sin(30 * Math.PI / 180)));
		int ptx2 = (int) (width / 2 + wid * 18 / 4.0 * (Math.cos(60 * Math.PI / 180)));
		int pty2 = (int) (height / 2 + ht * 18 / 4.0 * (Math.sin(60 * Math.PI / 180)));
		g.drawLine(ptx1, pty1, width, height);
		g.drawLine(ptx2, pty2, width, height);
		if (mTxt != null) {
			int x = (int) ((width / 2) * (1 - Math.sqrt(2) / 2));
			int y = (int) ((height / 2) + (height / 2 * Math.sqrt(2) / 2));
			int sqwidth = (int) (width * Math.sqrt(2) / 2);
			int sqheight = (y - height / 2) * 2;
			System.out.println("x = " + x + " y = " + y);
			Font f = g.getFont();
			FontMetrics fm = g.getFontMetrics();
			FontMetrics last = fm;
			float pt = f.getSize();
			while (fm.stringWidth(mTxt) < sqwidth && fm.getHeight() < sqheight) {
				last = fm;
				pt += 2;
				f = f.deriveFont(pt);
				fm = g.getFontMetrics(f);
			}
			int x_diff = sqwidth - fm.stringWidth(mTxt);
			int y_diff = sqheight - fm.getHeight();
			g.setFont(f);
			g.drawString(mTxt, x + x_diff / 2, y - y_diff / 2 - fm.getHeight() / 5);
		}
	}
}
