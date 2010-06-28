/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class EditorPaneExample1 extends JFrame {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -479698153634397409L;

    //~ Instance fields --------------------------------------------------------

    private JEditorPane pane;

    private JTextField textField;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new EditorPaneExample1 object.
     */
    public EditorPaneExample1() {
        super(org.openide.util.NbBundle.getMessage(EditorPaneExample1.class, "EditorPaneExample1.title")); // NOI18N

        pane = new JEditorPane();
        pane.setEditable(false);                               // Read-only
        getContentPane().add(new JScrollPane(pane), "Center"); // NOI18N

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(4, 4));
        final JLabel urlLabel = new JLabel("URL: ", JLabel.RIGHT); // NOI18N
        panel.add(urlLabel, "West");                               // NOI18N
        textField = new JTextField(32);
        panel.add(textField, "Center");                            // NOI18N

        getContentPane().add(panel, "South"); // NOI18N

        // Change page based on text field
        textField.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent evt) {
                    final String url = textField.getText();
                    try {
                        // Try to display the page
                        pane.setPage(url);
                        urlLabel.setToolTipText(pane.getText());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(
                            pane,
                            new String[] { "Unable to open file", url },
                            "File Open Error", // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); // NOI18N
        } catch (Exception evt) {
        }
        final JFrame f = new EditorPaneExample1();

        f.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(final WindowEvent evt) {
                    System.exit(0);
                }
            });
        f.setSize(500, 400);
        f.setVisible(true);
    }
}
