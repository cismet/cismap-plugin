/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.tools.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * DOCUMENT ME!
 *
 * @author   Christopher Butler
 * @version  $Revision$, $Date$
 */
public class GradientPainter {

    //~ Instance fields --------------------------------------------------------

    private Color startColor;
    private Color midColor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GradientPainter object.
     *
     * @param  start  DOCUMENT ME!
     * @param  mid    DOCUMENT ME!
     */
    public GradientPainter(final Color start, final Color mid) {
        startColor = start;
        midColor = mid;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  comp  DOCUMENT ME!
     * @param  g     DOCUMENT ME!
     */
    public void paintGradient(final JComponent comp, final Graphics g) {
        final int h = comp.getHeight();
        final int w = comp.getWidth();
        final int mid = w / 2;

        final Color bgColor = comp.getBackground();
        final Color start = (startColor == null) ? bgColor : startColor;
        final Color middle = (midColor == null) ? bgColor : midColor;

        final GradientPaint firstHalf = new GradientPaint(0, 0, start, mid, 0, middle);
        final GradientPaint secondHalf = new GradientPaint(mid, 0, middle, w, 0, bgColor);

        final Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(firstHalf);
        g2.fillRect(0, 0, mid, h);
        g2.setPaint(secondHalf);
        g2.fillRect(mid - 1, 0, mid, h);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getMidColor() {
        return midColor;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  midColor  DOCUMENT ME!
     */
    public void setMidColor(final Color midColor) {
        this.midColor = midColor;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getStartColor() {
        return startColor;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  startColor  DOCUMENT ME!
     */
    public void setStartColor(final Color startColor) {
        this.startColor = startColor;
    }
}
