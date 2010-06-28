/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Quote extends JComponent {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -2999545802715546566L;

    //~ Instance fields --------------------------------------------------------

    String mTxt;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Quote object.
     */
    public Quote() {
        super();
    }

    /**
     * Creates a new Quote object.
     *
     * @param  s  DOCUMENT ME!
     */
    public Quote(final String s) {
        this();
        setText(s);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  txt  DOCUMENT ME!
     */
    public void setText(final String txt) {
        mTxt = txt;
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final int width = getWidth();
        final int height = getHeight();
        g.setColor(Color.RED);
        final int wid = width / 10;
        final int ht = height / 10;
        g.drawArc(wid / 2, ht / 2, wid * 9, ht * 9, 0, -30);
        g.drawArc(wid / 2, ht / 2, wid * 9, ht * 9, 0, 300);
        final int ptx1 = (int)((width / 2) + (wid * 18 / 4.0 * (Math.cos(30 * Math.PI / 180))));
        final int pty1 = (int)((height / 2) + (ht * 18 / 4.0 * (Math.sin(30 * Math.PI / 180))));
        final int ptx2 = (int)((width / 2) + (wid * 18 / 4.0 * (Math.cos(60 * Math.PI / 180))));
        final int pty2 = (int)((height / 2) + (ht * 18 / 4.0 * (Math.sin(60 * Math.PI / 180))));
        g.drawLine(ptx1, pty1, width, height);
        g.drawLine(ptx2, pty2, width, height);
        if (mTxt != null) {
            final int x = (int)((width / 2) * (1 - (Math.sqrt(2) / 2)));
            final int y = (int)((height / 2) + (height / 2 * Math.sqrt(2) / 2));
            final int sqwidth = (int)(width * Math.sqrt(2) / 2);
            final int sqheight = (y - (height / 2)) * 2;
            System.out.println("x = " + x + " y = " + y); // NOI18N
            Font f = g.getFont();
            FontMetrics fm = g.getFontMetrics();
            FontMetrics last = fm;
            float pt = f.getSize();
            while ((fm.stringWidth(mTxt) < sqwidth) && (fm.getHeight() < sqheight)) {
                last = fm;
                pt += 2;
                f = f.deriveFont(pt);
                fm = g.getFontMetrics(f);
            }
            final int x_diff = sqwidth - fm.stringWidth(mTxt);
            final int y_diff = sqheight - fm.getHeight();
            g.setFont(f);
            g.drawString(mTxt, x + (x_diff / 2), y - (y_diff / 2) - (fm.getHeight() / 5));
        }
    }
}
