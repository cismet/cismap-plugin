/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.search.CidsSearchExecutor;
import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
import Sirius.navigator.types.treenode.ObjectTreeNode;
import Sirius.navigator.ui.ComponentRegistry;

import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.jdom.Element;

import org.openide.util.Lookup;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import de.cismet.cids.navigator.utils.CidsBeanDropTarget;

import de.cismet.cids.server.search.builtin.GeoSearch;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchCreateSearchGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapSearchListener;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;

import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;

import de.cismet.cismap.tools.gui.CidsBeanDropJPopupMenuButton;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.JPopupMenuButton;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.StayOpenCheckBoxMenuItem;

import static de.cismet.cismap.commons.features.PureNewFeature.geomTypes.ELLIPSE;
import static de.cismet.cismap.commons.features.PureNewFeature.geomTypes.LINESTRING;
import static de.cismet.cismap.commons.features.PureNewFeature.geomTypes.POLYGON;
import static de.cismet.cismap.commons.features.PureNewFeature.geomTypes.RECTANGLE;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MetaSearchComponentFactory extends javax.swing.JPanel implements PropertyChangeListener,
    MapSearchListener,
    Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            MetaSearchComponentFactory.class);

    //~ Instance fields --------------------------------------------------------

    private MappingComponent mappingComponent;

    private Action searchAction;
    private Action searchMenuSelectedAction;
    private Action searchRectangleAction;
    private Action searchPolygonAction;
    private Action searchCidsFeatureAction;
    private Action searchEllipseAction;
    private Action searchPolylineAction;
    private Action searchRedoAction;
    private Action searchShowLastFeatureAction;
    private Action searchBufferAction;
    private String interactionMode;
    private final String searchName;
    private MetaSearch metaSearch;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdPluginSearch;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenu menSearch;
    private javax.swing.JMenuItem mniSearchBuffer;
    private javax.swing.JMenuItem mniSearchBuffer1;
    private javax.swing.JRadioButtonMenuItem mniSearchCidsFeature;
    private javax.swing.JRadioButtonMenuItem mniSearchCidsFeature1;
    private javax.swing.JRadioButtonMenuItem mniSearchEllipse;
    private javax.swing.JRadioButtonMenuItem mniSearchEllipse1;
    private javax.swing.JRadioButtonMenuItem mniSearchPolygon;
    private javax.swing.JRadioButtonMenuItem mniSearchPolygon1;
    private javax.swing.JRadioButtonMenuItem mniSearchPolyline;
    private javax.swing.JRadioButtonMenuItem mniSearchPolyline1;
    private javax.swing.JRadioButtonMenuItem mniSearchRectangle;
    private javax.swing.JRadioButtonMenuItem mniSearchRectangle1;
    private javax.swing.JMenuItem mniSearchRedo;
    private javax.swing.JMenuItem mniSearchRedo1;
    private javax.swing.JMenuItem mniSearchShowLastFeature;
    private javax.swing.JMenuItem mniSearchShowLastFeature1;
    private javax.swing.JPopupMenu popMenSearch;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MetaSearchComponentFactory.
     *
     * @param  plugin            DOCUMENT ME!
     * @param  interactionMode   DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  searchName        DOCUMENT ME!
     */
    private MetaSearchComponentFactory(final boolean plugin,
            final String interactionMode,
            final MappingComponent mappingComponent,
            final String searchName) {
        this.interactionMode = interactionMode;
        this.mappingComponent = mappingComponent;
        this.searchName = searchName;
        metaSearch = MetaSearch.instance();

        CismapBroker.getInstance().addMapSearchListener(this);

        if (plugin) {
            final MetaSearchCreateSearchGeometryListener listener = new MetaSearchCreateSearchGeometryListener(
                    mappingComponent,
                    metaSearch);
            mappingComponent.addInputListener(MappingComponent.CREATE_SEARCH_POLYGON, listener);
            mappingComponent.addPropertyChangeListener(listener);
            listener.addPropertyChangeListener(this);
        }
        CismapBroker.getInstance().setMetaSearch(metaSearch);

        initSearchAction();
        initSearchMenuSelectedAction();
        initSearchRectangleAction();
        initSearchPolygonAction();
        initSearchCidsFeatureAction();
        initSearchEllipseAction();
        initSearchPolylineAction();
        initSearchRedoAction();
        initShowLastFeatureAction();
        initSearchBufferAction();
        initComponents();

        ((JPopupMenuButton)cmdPluginSearch).setPopupMenu(popMenSearch);

        if (plugin) {
            visualizeSearchMode();
        }

        new CidsBeanDropTarget(cmdPluginSearch);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   plugin            DOCUMENT ME!
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     * @param   searchName        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MetaSearchComponentFactory createNewInstance(final boolean plugin,
            final String interactionMode,
            final MappingComponent mappingComponent,
            final String searchName) {
        return new MetaSearchComponentFactory(plugin, interactionMode, mappingComponent, searchName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaSearch getMetaSearch() {
        return metaSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JMenu getMenSearch() {
        return menSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JButton getCmdPluginSearch() {
        return cmdPluginSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    private void initMetaSearch(final Geometry geom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selected Search Classes " + metaSearch.getSelectedSearchClassesForQuery()); // NOI18N
        }
        final Geometry transformed = CrsTransformer.transformToDefaultCrs(geom);
        // Damits auch mit -1 funzt:
        transformed.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

        // there is always the default implementation
        final GeoSearch gs = Lookup.getDefault().lookup(GeoSearch.class);
        gs.setGeometry(transformed);
        gs.setValidClassesFromStrings(metaSearch.getSelectedSearchClassesForQuery());
        CidsSearchExecutor.searchAndDisplayResultsWithDialog(gs);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mse  DOCUMENT ME!
     */
    @Override
    public void mapSearchStarted(final MapSearchEvent mse) {
        initMetaSearch(mse.getGeometry());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmdPluginSearch = new CidsBeanDropJPopupMenuButton(interactionMode, mappingComponent, searchName);
        menSearch = new javax.swing.JMenu();
        mniSearchRectangle = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        mniSearchPolygon = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        mniSearchEllipse = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        mniSearchPolyline = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        jSeparator8 = new javax.swing.JSeparator();
        mniSearchCidsFeature = new javax.swing.JRadioButtonMenuItem();
        mniSearchShowLastFeature = new javax.swing.JMenuItem();
        mniSearchRedo = new javax.swing.JMenuItem();
        mniSearchBuffer = new javax.swing.JMenuItem();
        popMenSearch = new javax.swing.JPopupMenu();
        mniSearchRectangle1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        mniSearchPolygon1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        mniSearchEllipse1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        mniSearchPolyline1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), Color.WHITE);
        jSeparator12 = new javax.swing.JSeparator();
        mniSearchCidsFeature1 = new javax.swing.JRadioButtonMenuItem();
        mniSearchShowLastFeature1 = new javax.swing.JMenuItem();
        mniSearchRedo1 = new javax.swing.JMenuItem();
        mniSearchBuffer1 = new javax.swing.JMenuItem();

        cmdPluginSearch.setAction(searchAction);
        cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
        cmdPluginSearch.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.cmdPluginSearch.toolTipText")); // NOI18N
        cmdPluginSearch.setFocusPainted(false);

        org.openide.awt.Mnemonics.setLocalizedText(menSearch, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.menSearch.text")); // NOI18N
        menSearch.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menSearchMenuSelected(evt);
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        mniSearchRectangle.setAction(searchRectangleAction);
        mniSearchRectangle.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchRectangle, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRectangle.text")); // NOI18N
        mniSearchRectangle.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRectangle.toolTipText")); // NOI18N
        mniSearchRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
        menSearch.add(mniSearchRectangle);

        mniSearchPolygon.setAction(searchPolygonAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchPolygon, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchPolygon.text")); // NOI18N
        mniSearchPolygon.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchPolygon.toolTipText")); // NOI18N
        mniSearchPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
        menSearch.add(mniSearchPolygon);

        mniSearchEllipse.setAction(searchEllipseAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchEllipse, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchEllipse.text")); // NOI18N
        mniSearchEllipse.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchEllipse.toolTipText")); // NOI18N
        mniSearchEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
        menSearch.add(mniSearchEllipse);

        mniSearchPolyline.setAction(searchPolylineAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchPolyline, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchPolyline.text")); // NOI18N
        mniSearchPolyline.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchPolyline.toolTipText")); // NOI18N
        mniSearchPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
        menSearch.add(mniSearchPolyline);
        menSearch.add(jSeparator8);

        mniSearchCidsFeature.setAction(searchCidsFeatureAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchCidsFeature, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchCidsFeature.text")); // NOI18N
        mniSearchCidsFeature.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchCidsFeature.toolTipText")); // NOI18N
        mniSearchCidsFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
        mniSearchCidsFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSearchCidsFeatureActionPerformed(evt);
            }
        });
        menSearch.add(mniSearchCidsFeature);

        mniSearchShowLastFeature.setAction(searchShowLastFeatureAction);
        mniSearchShowLastFeature.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchShowLastFeature, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchShowLastFeature.text")); // NOI18N
        mniSearchShowLastFeature.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchShowLastFeature.toolTipText")); // NOI18N
        menSearch.add(mniSearchShowLastFeature);

        mniSearchRedo.setAction(searchRedoAction);
        mniSearchRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchRedo, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRedo.text")); // NOI18N
        mniSearchRedo.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRedo.toolTipText")); // NOI18N
        menSearch.add(mniSearchRedo);

        mniSearchBuffer.setAction(searchBufferAction);
        mniSearchBuffer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchBuffer, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchBuffer.text")); // NOI18N
        mniSearchBuffer.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchBuffer.toolTipText")); // NOI18N
        menSearch.add(mniSearchBuffer);

        popMenSearch.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                popMenSearchPopupMenuWillBecomeVisible(evt);
            }
        });

        mniSearchRectangle1.setAction(searchRectangleAction);
        mniSearchRectangle1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchRectangle1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRectangle1.text")); // NOI18N
        mniSearchRectangle1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
        popMenSearch.add(mniSearchRectangle1);

        mniSearchPolygon1.setAction(searchPolygonAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchPolygon1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchPolygon1.text")); // NOI18N
        mniSearchPolygon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
        popMenSearch.add(mniSearchPolygon1);

        mniSearchEllipse1.setAction(searchEllipseAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchEllipse1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchEllipse1.text")); // NOI18N
        mniSearchEllipse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
        popMenSearch.add(mniSearchEllipse1);

        mniSearchPolyline1.setAction(searchPolylineAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchPolyline1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchPolyline1.text")); // NOI18N
        mniSearchPolyline1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
        popMenSearch.add(mniSearchPolyline1);
        popMenSearch.add(jSeparator12);

        mniSearchCidsFeature1.setAction(searchCidsFeatureAction);
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchCidsFeature1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchCidsFeature1.text")); // NOI18N
        mniSearchCidsFeature1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
        popMenSearch.add(mniSearchCidsFeature1);

        mniSearchShowLastFeature1.setAction(searchShowLastFeatureAction);
        mniSearchShowLastFeature1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchShowLastFeature1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchShowLastFeature1.text")); // NOI18N
        mniSearchShowLastFeature1.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchShowLastFeature1.toolTipText")); // NOI18N
        popMenSearch.add(mniSearchShowLastFeature1);

        mniSearchRedo1.setAction(searchRedoAction);
        mniSearchRedo1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchRedo1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRedo1.text")); // NOI18N
        mniSearchRedo1.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchRedo1.toolTipText")); // NOI18N
        popMenSearch.add(mniSearchRedo1);

        mniSearchBuffer1.setAction(searchBufferAction);
        mniSearchBuffer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(mniSearchBuffer1, org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchBuffer1.text")); // NOI18N
        mniSearchBuffer1.setToolTipText(org.openide.util.NbBundle.getMessage(MetaSearchComponentFactory.class, "MetaSearchComponentFactory.mniSearchBuffer1.toolTipText")); // NOI18N
        popMenSearch.add(mniSearchBuffer1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void mniSearchCidsFeatureActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSearchCidsFeatureActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mniSearchCidsFeatureActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void menSearchMenuSelected(final javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menSearchMenuSelected
        searchMenuSelectedAction.actionPerformed(new ActionEvent(menSearch, ActionEvent.ACTION_PERFORMED, null));
    }//GEN-LAST:event_menSearchMenuSelected

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void popMenSearchPopupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_popMenSearchPopupMenuWillBecomeVisible
        searchMenuSelectedAction.actionPerformed(new ActionEvent(popMenSearch, ActionEvent.ACTION_PERFORMED, null));
    }//GEN-LAST:event_popMenSearchPopupMenuWillBecomeVisible

    /**
     * DOCUMENT ME!
     */
    private void initSearchMenuSelectedAction() {
        searchMenuSelectedAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchMenuSelectedAction"); // NOI18N
                                }

                                final MetaSearchCreateSearchGeometryListener searchListener =
                                    (MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                        MappingComponent.CREATE_SEARCH_POLYGON);
                                final PureNewFeature lastGeometry = searchListener.getLastSearchFeature();

                                if (lastGeometry == null) {
                                    mniSearchShowLastFeature.setIcon(null);
                                    mniSearchShowLastFeature.setEnabled(false);
                                    mniSearchRedo.setIcon(null);
                                    mniSearchRedo.setEnabled(false);
                                    mniSearchBuffer.setEnabled(false);
                                } else {
                                    switch (lastGeometry.getGeometryType()) {
                                        case ELLIPSE: {
                                            mniSearchRedo.setIcon(mniSearchEllipse.getIcon());
                                            break;
                                        }

                                        case LINESTRING: {
                                            mniSearchRedo.setIcon(mniSearchPolyline.getIcon());
                                            break;
                                        }

                                        case POLYGON: {
                                            mniSearchRedo.setIcon(mniSearchPolygon.getIcon());
                                            break;
                                        }

                                        case RECTANGLE: {
                                            mniSearchRedo.setIcon(mniSearchRectangle.getIcon());
                                            break;
                                        }
                                    }

                                    mniSearchShowLastFeature.setIcon(mniSearchRedo.getIcon());

                                    mniSearchRedo.setEnabled(true);
                                    mniSearchBuffer.setEnabled(true);
                                    mniSearchShowLastFeature.setEnabled(true);
                                }

                                // kopieren nach popupmenu im grünen M
                                mniSearchRectangle1.setSelected(mniSearchRectangle.isSelected());
                                mniSearchPolygon1.setSelected(mniSearchPolygon.isSelected());
                                mniSearchEllipse1.setSelected(mniSearchEllipse.isSelected());
                                mniSearchPolyline1.setSelected(mniSearchPolyline.isSelected());
                                mniSearchRedo1.setIcon(mniSearchRedo.getIcon());
                                mniSearchRedo1.setEnabled(mniSearchRedo.isEnabled());
                                mniSearchBuffer1.setEnabled(mniSearchBuffer.isEnabled());
                                mniSearchShowLastFeature1.setIcon(mniSearchShowLastFeature.getIcon());
                                mniSearchShowLastFeature1.setEnabled(mniSearchShowLastFeature.isEnabled());
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchAction() {
        searchAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchAction"); // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);

                                            if (mniSearchRectangle.isSelected()) {
                                                ((MetaSearchCreateSearchGeometryListener)
                                                    mappingComponent.getInputListener(
                                                        MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                    MetaSearchCreateSearchGeometryListener.RECTANGLE);
                                            } else if (mniSearchPolygon.isSelected()) {
                                                ((MetaSearchCreateSearchGeometryListener)
                                                    mappingComponent.getInputListener(
                                                        MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                    MetaSearchCreateSearchGeometryListener.POLYGON);
                                            } else if (mniSearchEllipse.isSelected()) {
                                                ((MetaSearchCreateSearchGeometryListener)
                                                    mappingComponent.getInputListener(
                                                        MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                    MetaSearchCreateSearchGeometryListener.ELLIPSE);
                                            } else if (mniSearchPolyline.isSelected()) {
                                                ((MetaSearchCreateSearchGeometryListener)
                                                    mappingComponent.getInputListener(
                                                        MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                    MetaSearchCreateSearchGeometryListener.LINESTRING);
                                            }
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchRectangleAction() {
        searchRectangleAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchRectangleAction");                                // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                mniSearchRectangle.setSelected(true);
                                cmdPluginSearch.setIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
                                cmdPluginSearch.setSelectedIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                            ((MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                                    MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                MetaSearchCreateSearchGeometryListener.RECTANGLE);
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchPolygonAction() {
        searchPolygonAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchPolygonAction");                                // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                mniSearchPolygon.setSelected(true);
                                cmdPluginSearch.setIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchPolygon.png"))); // NOI18N
                                cmdPluginSearch.setSelectedIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchPolygon.png"))); // NOI18N
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                            ((MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                                    MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                MetaSearchCreateSearchGeometryListener.POLYGON);
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchCidsFeatureAction() {
        searchCidsFeatureAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchCidsFeatureAction"); // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                            final MetaSearchCreateSearchGeometryListener searchListener =
                                                ((MetaSearchCreateSearchGeometryListener)
                                                    mappingComponent.getInputListener(
                                                        MappingComponent.CREATE_SEARCH_POLYGON));

                                            de.cismet.tools.CismetThreadPool.execute(
                                                new javax.swing.SwingWorker<SearchFeature, Void>() {

                                                    @Override
                                                    protected SearchFeature doInBackground() throws Exception {
                                                        final DefaultMetaTreeNode[] nodes = ComponentRegistry
                                                                        .getRegistry().getActiveCatalogue()
                                                                        .getSelectedNodesArray();
                                                        final Collection<Geometry> searchGeoms =
                                                            new ArrayList<Geometry>();

                                                        for (final DefaultMetaTreeNode dmtn : nodes) {
                                                            if (dmtn instanceof ObjectTreeNode) {
                                                                final MetaObject mo = ((ObjectTreeNode)dmtn)
                                                                                .getMetaObject();
                                                                final CidsFeature cf = new CidsFeature(mo);
                                                                searchGeoms.add(cf.getGeometry());
                                                            }
                                                        }
                                                        final Geometry[] searchGeomsArr = searchGeoms.toArray(
                                                                new Geometry[0]);
                                                        final GeometryCollection coll =
                                                            new GeometryFactory().createGeometryCollection(
                                                                searchGeomsArr);

                                                        final Geometry newG = coll.buffer(0.1d);
                                                        if (LOG.isDebugEnabled()) {
                                                            LOG.debug("SearchGeom " + newG.toText()); // NOI18N
                                                        }

                                                        final SearchFeature sf = new SearchFeature(newG);
                                                        sf.setGeometryType(PureNewFeature.geomTypes.MULTIPOLYGON);
                                                        return sf;
                                                    }

                                                    @Override
                                                    protected void done() {
                                                        try {
                                                            final SearchFeature search = get();
                                                            if (search != null) {
                                                                searchListener.search(search);
                                                            }
                                                        } catch (final Exception e) {
                                                            LOG.error("Exception in Background Thread", e); // NOI18N
                                                        }
                                                    }
                                                });
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchEllipseAction() {
        searchEllipseAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchEllipseAction");                                // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                mniSearchEllipse.setSelected(true);
                                cmdPluginSearch.setIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchEllipse.png"))); // NOI18N
                                cmdPluginSearch.setSelectedIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchEllipse.png"))); // NOI18N
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                            ((MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                                    MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                MetaSearchCreateSearchGeometryListener.ELLIPSE);
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchPolylineAction() {
        searchPolylineAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchPolylineAction");                                // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                mniSearchPolyline.setSelected(true);
                                cmdPluginSearch.setIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchPolyline.png"))); // NOI18N
                                cmdPluginSearch.setSelectedIcon(
                                    new javax.swing.ImageIcon(
                                        getClass().getResource("/images/pluginSearchPolyline.png"))); // NOI18N

                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                            ((MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                                    MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                MetaSearchCreateSearchGeometryListener.LINESTRING);
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchRedoAction() {
        searchRedoAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("redoSearchAction"); // NOI18N
                                }

                                final MetaSearchCreateSearchGeometryListener searchListener =
                                    (MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                        MappingComponent.CREATE_SEARCH_POLYGON);
                                searchListener.redoLastSearch();
                                mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initShowLastFeatureAction() {
        searchShowLastFeatureAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("searchShowLastFeatureAction"); // NOI18N
                                }

                                final MetaSearchCreateSearchGeometryListener searchListener =
                                    (MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                                        MappingComponent.CREATE_SEARCH_POLYGON);
                                searchListener.showLastFeature();
                                mappingComponent.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchBufferAction() {
        searchBufferAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("bufferSearchGeometry"); // NOI18N
                                }
                                cmdPluginSearch.setSelected(true);
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            final String s = (String)JOptionPane.showInputDialog(
                                                    StaticSwingTools.getParentFrame(mappingComponent),
                                                    "Geben Sie den Abstand des zu erzeugenden\n"       // NOI18N
                                                            + "Puffers der letzten Suchgeometrie an.", // NOI18N
                                                    "Puffer",                                          // NOI18N
                                                    JOptionPane.PLAIN_MESSAGE,
                                                    null,
                                                    null,
                                                    "");                                               // NOI18N
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug(s);
                                            }

                                            // , statt . ebenfalls erlauben
                                            if (s.matches("\\d*,\\d*")) { // NOI18N
                                                s.replace(",", ".");      // NOI18N
                                            }

                                            try {
                                                final float buffer = Float.valueOf(s);

                                                final MetaSearchCreateSearchGeometryListener searchListener =
                                                    (MetaSearchCreateSearchGeometryListener)
                                                    mappingComponent.getInputListener(
                                                        MappingComponent.CREATE_SEARCH_POLYGON);
                                                final PureNewFeature lastFeature =
                                                    searchListener.getLastSearchFeature();

                                                if (lastFeature != null) {
                                                    // Geometrie-Daten holen
                                                    final Geometry geom = lastFeature.getGeometry();

                                                    // Puffer-Geometrie holen
                                                    final Geometry bufferGeom = geom.buffer(buffer);

                                                    // und setzen
                                                    lastFeature.setGeometry(bufferGeom);

                                                    // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder
                                                    // ähnliches mehr)
                                                    lastFeature.setGeometryType(PureNewFeature.geomTypes.POLYGON);

                                                    for (final Object feature
                                                                : mappingComponent.getFeatureCollection()
                                                                .getAllFeatures()) {
                                                        final PFeature sel = (PFeature)mappingComponent.getPFeatureHM()
                                                                    .get(feature);

                                                        if (sel.getFeature().equals(lastFeature)) {
                                                            // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                                                            // setzen
                                                            sel.setCoordArr(bufferGeom.getCoordinates());

                                                            // refresh
                                                            sel.syncGeometry();

                                                            final List v = new ArrayList();
                                                            v.add(sel.getFeature());
                                                            ((DefaultFeatureCollection)
                                                                mappingComponent.getFeatureCollection())
                                                                    .fireFeaturesChanged(v);
                                                        }
                                                    }

                                                    searchListener.search(lastFeature);
                                                    mappingComponent.setInteractionMode(
                                                        MappingComponent.CREATE_SEARCH_POLYGON);
                                                }
                                            } catch (final NumberFormatException ex) {
                                                JOptionPane.showMessageDialog(
                                                    StaticSwingTools.getParentFrame(mappingComponent),
                                                    "The given value was not a floating point value.!",
                                                    "Error",
                                                    JOptionPane.ERROR_MESSAGE); // NOI18N
                                            } catch (final Exception ex) {
                                                if (LOG.isDebugEnabled()) {
                                                    LOG.debug("", ex);          // NOI18N
                                                }
                                            }
                                        }
                                    });
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    protected void visualizeSearchMode() {
        final MetaSearchCreateSearchGeometryListener searchListener = (MetaSearchCreateSearchGeometryListener)
            mappingComponent.getInputListener(
                MappingComponent.CREATE_SEARCH_POLYGON);
        final String searchMode = searchListener.getMode();
        final PureNewFeature lastGeometry = searchListener.getLastSearchFeature();

        if (MetaSearchCreateSearchGeometryListener.RECTANGLE.equals(searchMode)) {
            cmdPluginSearch.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
            cmdPluginSearch.setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
        } else if (MetaSearchCreateSearchGeometryListener.POLYGON.equals(searchMode)) {
            cmdPluginSearch.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchPolygon.png")));   // NOI18N
            cmdPluginSearch.setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchPolygon.png")));   // NOI18N
        } else if (MetaSearchCreateSearchGeometryListener.ELLIPSE.equals(searchMode)) {
            cmdPluginSearch.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchEllipse.png")));   // NOI18N
            cmdPluginSearch.setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchEllipse.png")));   // NOI18N
        } else if (MetaSearchCreateSearchGeometryListener.LINESTRING.equals(searchMode)) {
            cmdPluginSearch.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchPolyline.png")));  // NOI18N
            cmdPluginSearch.setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/pluginSearchPolyline.png")));  // NOI18N
        }

        mniSearchRectangle.setSelected(MetaSearchCreateSearchGeometryListener.RECTANGLE.equals(searchMode));
        mniSearchPolygon.setSelected(MetaSearchCreateSearchGeometryListener.POLYGON.equals(searchMode));
        mniSearchEllipse.setSelected(MetaSearchCreateSearchGeometryListener.ELLIPSE.equals(searchMode));
        mniSearchPolyline.setSelected(MetaSearchCreateSearchGeometryListener.LINESTRING.equals(searchMode));

        if (lastGeometry == null) {
            mniSearchShowLastFeature.setIcon(null);
            mniSearchShowLastFeature.setEnabled(false);
            mniSearchRedo.setIcon(null);
            mniSearchRedo.setEnabled(false);
            mniSearchBuffer.setEnabled(false);
        } else {
            switch (lastGeometry.getGeometryType()) {
                case ELLIPSE: {
                    mniSearchRedo.setIcon(mniSearchEllipse.getIcon());
                    break;
                }

                case LINESTRING: {
                    mniSearchRedo.setIcon(mniSearchPolyline.getIcon());
                    break;
                }

                case POLYGON: {
                    mniSearchRedo.setIcon(mniSearchPolygon.getIcon());
                    break;
                }

                case RECTANGLE: {
                    mniSearchRedo.setIcon(mniSearchRectangle.getIcon());
                    break;
                }
            }

            mniSearchRedo.setEnabled(true);
            mniSearchBuffer.setEnabled(true);
            mniSearchShowLastFeature.setIcon(mniSearchRedo.getIcon());
            mniSearchShowLastFeature.setEnabled(true);
        }

        // kopieren nach popupmenu im grünen M
        mniSearchRectangle1.setSelected(mniSearchRectangle.isSelected());
        mniSearchPolygon1.setSelected(mniSearchPolygon.isSelected());
        mniSearchEllipse1.setSelected(mniSearchEllipse.isSelected());
        mniSearchPolyline1.setSelected(mniSearchPolyline.isSelected());
        mniSearchRedo1.setIcon(mniSearchRedo.getIcon());
        mniSearchRedo1.setEnabled(mniSearchRedo.isEnabled());
        mniSearchBuffer1.setEnabled(mniSearchBuffer.isEnabled());
        mniSearchShowLastFeature1.setIcon(mniSearchShowLastFeature.getIcon());
        mniSearchShowLastFeature1.setEnabled(mniSearchShowLastFeature.isEnabled());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (MetaSearchCreateSearchGeometryListener.PROPERTY_FORGUI_LAST_FEATURE.equals(evt.getPropertyName())
                    || MetaSearchCreateSearchGeometryListener.PROPERTY_FORGUI_MODE.equals(evt.getPropertyName())
                    || MetaSearchCreateSearchGeometryListener.PROPERTY_MODE.equals(evt.getPropertyName())) {
            visualizeSearchMode();
        }
    }

    @Override
    public void configure(final Element parent) {
        metaSearch.configure(parent);
        initSearchTopicMenues();
    }

    /**
     * DOCUMENT ME!
     */
    private void initSearchTopicMenues() {
        if ((metaSearch.getSearchTopics() != null) && !metaSearch.getSearchTopics().isEmpty()) {
            popMenSearch.add(new JSeparator());
            menSearch.add(new JSeparator());
            for (final SearchTopic searchTopic : metaSearch.getSearchTopics()) {
                popMenSearch.add(new StayOpenCheckBoxMenuItem(
                        (Action)searchTopic,
                        javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                        Color.WHITE));
                menSearch.add(new StayOpenCheckBoxMenuItem(
                        (Action)searchTopic,
                        javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                        Color.WHITE));

                searchTopic.addPropertyChangeListener((PropertyChangeListener)mappingComponent.getInputListener(
                        MappingComponent.CREATE_SEARCH_POLYGON));
            }
        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        metaSearch.masterConfigure(parent);
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        return metaSearch.getConfiguration();
    }
}
