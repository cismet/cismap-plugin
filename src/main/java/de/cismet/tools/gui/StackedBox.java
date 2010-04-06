/*
 * StackedBox.java
 *
 * Created on 15. Februar 2006, 15:25
 */

package de.cismet.tools.gui;
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
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Stacks components vertically in boxes. Each box is created with a title and a
 * component.<br>
 *
 * <p>
 * The <code>StackedBox</code> can be added to a
 * {@link javax.swing.JScrollPane}.
 *
 * <p>
 * Note: this class is not part of the SwingX core classes. It is just an
 * example of what can be achieved with the components.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class StackedBox extends JPanel implements Scrollable {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Color titleBackgroundColor;
    private Color titleForegroundColor;
    private Color separatorColor;
    private Border separatorBorder;
    
    private JXCollapsiblePane fillingPane=null;
    private Component fillingComponent=null;
    private int minHeight;
    private JScrollPane scroller=null;
    private boolean blockEvents=false;
    public StackedBox() {
        setLayout(new VerticalLayout());
        setOpaque(true);
        setBackground(Color.WHITE);
        separatorBorder = new SeparatorBorder();
        setTitleForegroundColor(Color.BLACK);
        setTitleBackgroundColor(new Color(248, 248, 248));
        setSeparatorColor(new Color(214, 223, 247));
        log.debug("StackedBox erzeugt");//NOI18N
        
    }
    
    
    public void setScrollPane(JScrollPane scroller) {
        this.scroller=scroller;
        scroller.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
                
            }
            public void componentMoved(ComponentEvent e) {
                
            }
            public void componentResized(ComponentEvent e) {
                log.debug("REsizeEvent e="+e);//NOI18N
                adjustFillingComponent();
            }
            public void componentShown(ComponentEvent e) {
                
            }
        });
        
    }
    
    public Color getSeparatorColor() {
        return separatorColor;
    }
    
    public void setSeparatorColor(Color separatorColor) {
        this.separatorColor = separatorColor;
    }
    
    public Color getTitleForegroundColor() {
        return titleForegroundColor;
    }
    
    public void setTitleForegroundColor(Color titleForegroundColor) {
        this.titleForegroundColor = titleForegroundColor;
    }
    
    public Color getTitleBackgroundColor() {
        return titleBackgroundColor;
    }
    
    public void setTitleBackgroundColor(Color titleBackgroundColor) {
        this.titleBackgroundColor = titleBackgroundColor;
    }
    
    /**
     * Adds a new component to this <code>StackedBox</code>
     *
     * @param title
     * @param component
     */
    public void addBox(boolean open,String title, Component component, Icon icon, boolean fillingPane) {
        final JXCollapsiblePane collapsible = new JXCollapsiblePane();
        collapsible.setCollapsed(!open);
        
        if (fillingPane) {
            this.fillingPane=collapsible;
            
            //component.setPreferredSize(new Dimension(component.getPreferredSize().width,100));
            minHeight=component.getPreferredSize().height;
            fillingComponent=component;
            log.debug(component);
        }
        
        collapsible.addPropertyChangeListener("animationState", new PropertyChangeListener() {//NOI18N
            public void propertyChange(final PropertyChangeEvent evt) {
                final JXCollapsiblePane src=((JXCollapsiblePane)evt.getSource());
                log.debug("collapsed property changed");//NOI18N
                if (isShowing()&&!blockEvents) {
                    log.debug("collapsedListener");//NOI18N
                    
                    if (src==fillingComponent) {
                        blockEvents=true;
                    }
                    adjustFillingComponent();
                    blockEvents=false;
                    if (scroller!=null) {
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                log.debug("Collapsed:"+evt);//NOI18N
                                scroller.getViewport().scrollRectToVisible(src.getBounds(null));
                            }
                        });
                    }
                }
            }
        });
        
        collapsible.getContentPane().setBackground(Color.WHITE);
        collapsible.setLayout(new BorderLayout());
        collapsible.add(component,BorderLayout.CENTER);
        collapsible.setBorder(new CompoundBorder(separatorBorder, collapsible
                .getBorder()));
        
        Action toggleAction = collapsible.getActionMap().get(
                JXCollapsiblePane.TOGGLE_ACTION);
        // use the collapse/expand icons from the JTree UI
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
                .getIcon("Tree.expandedIcon"));//NOI18N
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
                .getIcon("Tree.collapsedIcon"));//NOI18N
        
        JXHyperlink link = new JXHyperlink(toggleAction);
        link.setText(title);
        link.setFont(link.getFont().deriveFont(Font.BOLD));
        link.setOpaque(true);
        link.setBackground(getTitleBackgroundColor());
        link.setFocusPainted(false);
        
        link.setUnclickedColor(getTitleForegroundColor());
        link.setClickedColor(getTitleForegroundColor());
        
        link.setBorder(new CompoundBorder(separatorBorder, BorderFactory
                .createEmptyBorder(2, 4, 2, 4)));
        link.setBorderPainted(true);
        
        add(link);
        add(collapsible);
        
        
        
    }
    
    /**
     * @see Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    /**
     * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return 10;
    }
    
    /**
     * @see Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
        } else {
            return false;
        }
    }
    
    /**
     * @see Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }
    
    /**
     * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
            int direction) {
        return 10;
    }
    
    /**
     * The border between the stack components. It separates each component with a
     * fine line border.
     */
    class SeparatorBorder implements Border {
        
        boolean isFirst(Component c) {
            return c.getParent() == null || c.getParent().getComponent(0) == c;
        }
        
        public Insets getBorderInsets(Component c) {
            // if the collapsible is collapsed, we do not want its border to be
            // painted.
            if (c instanceof JXCollapsiblePane) {
                if (((JXCollapsiblePane)c).isCollapsed()) { return new Insets(0, 0, 0,
                        0); }
            }
            return new Insets(isFirst(c)?4:1, 0, 1, 0);
        }
        
        public boolean isBorderOpaque() {
            return true;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                int height) {
            g.setColor(getSeparatorColor());
            if (isFirst(c)) {
                g.drawLine(x, y + 2, x + width, y + 2);
            }
            g.drawLine(x, y + height - 1, x + width, y + height - 1);
        }
    }
    
    
    private int getHeightFilledByComponents() {
        Component[] cont=this.getComponents();
        int height=0;
        for (int i = 0; i < cont.length; i++) {
            height+=cont[i].getHeight();
        }
        return height;
    }
    private int getPotentialNewHeightOfFillingComponent() {
        if (fillingPane!=null) {
            return scroller.getHeight()-getHeightFilledByComponents()+fillingPane.getSize().height;
        } else {
            return -1;
        }
    }
    
    
    public void adjustFillingComponent() {
        if (fillingPane!=null) {
//            log.debug("getHeightFilledByComponents():"+getHeightFilledByComponents());
            log.debug("getPotentialNewHeightOfFillingComponent():"+getPotentialNewHeightOfFillingComponent());//NOI18N
            log.debug("minHeight:"+minHeight);//NOI18N
            final int potentialNewHeight=getPotentialNewHeightOfFillingComponent();
            boolean mindesHoeheWirdNichtUnterschritten=potentialNewHeight>minHeight;
            if (mindesHoeheWirdNichtUnterschritten && !fillingPane.isCollapsed()) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        fillingComponent.setPreferredSize(new Dimension(fillingComponent.getPreferredSize().width,potentialNewHeight-4));
                    }
                });
                if (scroller!=null){
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            setPreferredSize(new Dimension(getWidth(),scroller.getSize().height));
                        }
                    });
                }
            }else {
                if (scroller!=null){
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            setPreferredSize(new Dimension(scroller.getSize().width,getHeightFilledByComponents()+2));
                        }
                    });
                }
                
            }
            
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    blinkFillingPane();
                }
            });
        }
    }
    
    public JXCollapsiblePane getFillingPane() {
        return fillingPane;
    }
    
    private void blinkFillingPane() {
        if (fillingPane!=null) {
            boolean anim=fillingPane.isAnimated();
            fillingPane.setAnimated(false);
            boolean coll=fillingPane.isCollapsed();
            fillingPane.setCollapsed(true);
            fillingPane.setCollapsed(false);
            fillingPane.setCollapsed(coll);
            fillingPane.setAnimated(anim);
            
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 400, Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 300, Short.MAX_VALUE)
                );
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
