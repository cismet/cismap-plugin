/*
 * EditorPaneExample1.java
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
 * Created on 22. Februar 2006, 15:53
 *
 */

package de.cismet.cismap.navigatorplugin;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class EditorPaneExample1 extends JFrame {
  public EditorPaneExample1() {
    super("JEditorPane Example 1");

    pane = new JEditorPane();
    pane.setEditable(false); // Read-only
    getContentPane().add(new JScrollPane(pane), "Center");

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(4, 4));
    final JLabel urlLabel = new JLabel("URL: ", JLabel.RIGHT);
    panel.add(urlLabel, "West");
    textField = new JTextField(32);
    panel.add(textField, "Center");

    getContentPane().add(panel, "South");

    // Change page based on text field
    textField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        String url = textField.getText();
        try {
          // Try to display the page
          pane.setPage(url);
          urlLabel.setToolTipText(pane.getText());
          
        } catch (IOException e) {
          JOptionPane.showMessageDialog(pane, new String[] {
              "Unable to open file", url }, "File Open Error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    });
  }

  public static void main(String[] args) {
    try {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception evt) {}
    JFrame f = new EditorPaneExample1();

    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        System.exit(0);
      }
    });
    f.setSize(500, 400);
    f.setVisible(true);
  }

  private JEditorPane pane;

  private JTextField textField;
}