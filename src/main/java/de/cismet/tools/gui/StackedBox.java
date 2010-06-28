/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StackedBox.java
 *
 * Created on 15. Februar 2006, 15:25
 */
package de.cismet.tools.gui;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.VerticalLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * Stacks components vertically in boxes. Each box is created with a title and a component.<br>
 *
 * <p>The <code>StackedBox</code> can be added to a {@link javax.swing.JScrollPane}.</p>
 *
 * <p>Note: this class is not part of the SwingX core classes. It is just an example of what can be achieved with the
 * components.</p>
 *
 * @author   <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @version  $Revision$, $Date$
 */
public class StackedBox extends JPanel implements Scrollable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -4201145972336077827L;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Color titleBackgroundColor;
    private Color titleForegroundColor;
    private Color separatorColor;
    private Border separatorBorder;

    private JXCollapsiblePane fillingPane = null;
    private Component fillingComponent = null;
    private int minHeight;
    private JScrollPane scroller = null;
    private boolean blockEvents = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StackedBox object.
     */
    public StackedBox() {
        setLayout(new VerticalLayout());
        setOpaque(true);
        setBackground(Color.WHITE);
        separatorBorder = new SeparatorBorder();
        setTitleForegroundColor(Color.BLACK);
        setTitleBackgroundColor(new Color(248, 248, 248));
        setSeparatorColor(new Color(214, 223, 247));
        if (log.isDebugEnabled()) {
            log.debug("StackedBox erzeugt"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  scroller  DOCUMENT ME!
     */
    public void setScrollPane(final JScrollPane scroller) {
        this.scroller = scroller;
        scroller.addComponentListener(new ComponentListener() {

                @Override
                public void componentHidden(final ComponentEvent e) {
                }
                @Override
                public void componentMoved(final ComponentEvent e) {
                }
                @Override
                public void componentResized(final ComponentEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("REsizeEvent e=" + e); // NOI18N
                    }
                    adjustFillingComponent();
                }
                @Override
                public void componentShown(final ComponentEvent e) {
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getSeparatorColor() {
        return separatorColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  separatorColor  DOCUMENT ME!
     */
    public void setSeparatorColor(final Color separatorColor) {
        this.separatorColor = separatorColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getTitleForegroundColor() {
        return titleForegroundColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  titleForegroundColor  DOCUMENT ME!
     */
    public void setTitleForegroundColor(final Color titleForegroundColor) {
        this.titleForegroundColor = titleForegroundColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getTitleBackgroundColor() {
        return titleBackgroundColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  titleBackgroundColor  DOCUMENT ME!
     */
    public void setTitleBackgroundColor(final Color titleBackgroundColor) {
        this.titleBackgroundColor = titleBackgroundColor;
    }

    /**
     * Adds a new component to this <code>StackedBox.</code>
     *
     * @param  open         DOCUMENT ME!
     * @param  title        DOCUMENT ME!
     * @param  component    DOCUMENT ME!
     * @param  icon         DOCUMENT ME!
     * @param  fillingPane  DOCUMENT ME!
     */
    public void addBox(final boolean open,
            final String title,
            final Component component,
            final Icon icon,
            final boolean fillingPane) {
        final JXCollapsiblePane collapsible = new JXCollapsiblePane();
        collapsible.setCollapsed(!open);

        if (fillingPane) {
            this.fillingPane = collapsible;

            // component.setPreferredSize(new Dimension(component.getPreferredSize().width,100));
            minHeight = component.getPreferredSize().height;
            fillingComponent = component;
            if (log.isDebugEnabled()) {
                log.debug(component);
            }
        }

        collapsible.addPropertyChangeListener("animationState", new PropertyChangeListener() { // NOI18N
                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    final JXCollapsiblePane src = ((JXCollapsiblePane)evt.getSource());
                    if (log.isDebugEnabled()) {
                        log.debug("collapsed property changed"); // NOI18N
                    }
                    if (isShowing() && !blockEvents) {
                        if (log.isDebugEnabled()) {
                            log.debug("collapsedListener");      // NOI18N
                        }

                        if (src == fillingComponent) {
                            blockEvents = true;
                        }
                        adjustFillingComponent();
                        blockEvents = false;
                        if (scroller != null) {
                            java.awt.EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Collapsed:" + evt); // NOI18N
                                        }
                                        scroller.getViewport().scrollRectToVisible(src.getBounds(null));
                                    }
                                });
                        }
                    }
                }
            });

        collapsible.getContentPane().setBackground(Color.WHITE);
        collapsible.setLayout(new BorderLayout());
        collapsible.add(component, BorderLayout.CENTER);
        collapsible.setBorder(new CompoundBorder(separatorBorder, collapsible.getBorder()));

        final Action toggleAction = collapsible.getActionMap().get(
                JXCollapsiblePane.TOGGLE_ACTION);
        // use the collapse/expand icons from the JTree UI
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon")); // NOI18N
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));  // NOI18N

        final JXHyperlink link = new JXHyperlink(toggleAction);
        link.setText(title);
        link.setFont(link.getFont().deriveFont(Font.BOLD));
        link.setOpaque(true);
        link.setBackground(getTitleBackgroundColor());
        link.setFocusPainted(false);

        link.setUnclickedColor(getTitleForegroundColor());
        link.setClickedColor(getTitleForegroundColor());

        link.setBorder(new CompoundBorder(separatorBorder, BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        link.setBorderPainted(true);

        add(link);
        add(collapsible);
    }

    /**
     * @see  Scrollable#getPreferredScrollableViewportSize()
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * @see  Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return 10;
    }

    /**
     * @see  Scrollable#getScrollableTracksViewportHeight()
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
        } else {
            return false;
        }
    }

    /**
     * @see  Scrollable#getScrollableTracksViewportWidth()
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * @see  Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return 10;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getHeightFilledByComponents() {
        final Component[] cont = this.getComponents();
        int height = 0;
        for (int i = 0; i < cont.length; i++) {
            height += cont[i].getHeight();
        }
        return height;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getPotentialNewHeightOfFillingComponent() {
        if (fillingPane != null) {
            return scroller.getHeight() - getHeightFilledByComponents() + fillingPane.getSize().height;
        } else {
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void adjustFillingComponent() {
        if (fillingPane != null) {
//            log.debug("getHeightFilledByComponents():"+getHeightFilledByComponents());
            if (log.isDebugEnabled()) {
                log.debug("getPotentialNewHeightOfFillingComponent():" + getPotentialNewHeightOfFillingComponent()); // NOI18N
            }
            if (log.isDebugEnabled()) {
                log.debug("minHeight:" + minHeight);                                                                 // NOI18N
            }
            final int potentialNewHeight = getPotentialNewHeightOfFillingComponent();
            final boolean mindesHoeheWirdNichtUnterschritten = potentialNewHeight > minHeight;
            if (mindesHoeheWirdNichtUnterschritten && !fillingPane.isCollapsed()) {
                java.awt.EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            fillingComponent.setPreferredSize(
                                new Dimension(fillingComponent.getPreferredSize().width, potentialNewHeight - 4));
                        }
                    });
                if (scroller != null) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                setPreferredSize(new Dimension(getWidth(), scroller.getSize().height));
                            }
                        });
                }
            } else {
                if (scroller != null) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                setPreferredSize(
                                    new Dimension(scroller.getSize().width, getHeightFilledByComponents() + 2));
                            }
                        });
                }
            }

            java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        blinkFillingPane();
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JXCollapsiblePane getFillingPane() {
        return fillingPane;
    }

    /**
     * DOCUMENT ME!
     */
    private void blinkFillingPane() {
        if (fillingPane != null) {
            final boolean anim = fillingPane.isAnimated();
            fillingPane.setAnimated(false);
            final boolean coll = fillingPane.isCollapsed();
            fillingPane.setCollapsed(true);
            fillingPane.setCollapsed(false);
            fillingPane.setCollapsed(coll);
            fillingPane.setAnimated(anim);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 300, Short.MAX_VALUE));
    }
    // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * The border between the stack components. It separates each component with a fine line border.
     *
     * @version  $Revision$, $Date$
     */
    class SeparatorBorder implements Border {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   c  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        boolean isFirst(final Component c) {
            return (c.getParent() == null) || (c.getParent().getComponent(0) == c);
        }

        @Override
        public Insets getBorderInsets(final Component c) {
            // if the collapsible is collapsed, we do not want its border to be
            // painted.
            if (c instanceof JXCollapsiblePane) {
                if (((JXCollapsiblePane)c).isCollapsed()) {
                    return new Insets(0, 0, 0,
                            0);
                }
            }
            return new Insets(isFirst(c) ? 4 : 1, 0, 1, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(final Component c,
                final Graphics g,
                final int x,
                final int y,
                final int width,
                final int height) {
            g.setColor(getSeparatorColor());
            if (isFirst(c)) {
                g.drawLine(x, y + 2, x + width, y + 2);
            }
            g.drawLine(x, y + height - 1, x + width, y + height - 1);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
