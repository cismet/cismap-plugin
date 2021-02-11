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

import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
import Sirius.navigator.types.treenode.ObjectTreeNode;
import Sirius.navigator.ui.ComponentRegistry;

import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.CidsBeanDropTarget;

import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.SelectFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.tools.gui.CidsBeanDropJPopupMenuButton;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.menu.CidsUiComponent;

import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.ELLIPSE;
import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.LINESTRING;
import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.POLYGON;
import static de.cismet.cismap.commons.features.AbstractNewFeature.geomTypes.RECTANGLE;

import static de.cismet.cismap.navigatorplugin.GeoSelectionButton.createSelectAction;
import static de.cismet.cismap.navigatorplugin.GeoSelectionButton.createSelectMenuSelectedAction;
import static de.cismet.cismap.navigatorplugin.GeoSelectionButton.createSelectRectangleAction;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiComponent.class)
public class GeoSelectionButton extends CidsBeanDropJPopupMenuButton implements PropertyChangeListener,
    CidsUiComponent {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GeoSelectionButton.class);

    //~ Instance fields --------------------------------------------------------

    private SelectionListener selectListener;
    private Action selectAction;
    private Action selectMenuSelectedAction;
    private Action selectRectangleAction;
    private Action selectPolygonAction;
    private Action selectCidsFeatureAction;
    private Action selectEllipseAction;
    private Action selectPolylineAction;
    private Action selectRedoAction;
    private Action selectShowLastFeatureAction;
    private Action selectBufferAction;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JMenuItem mniSelectBuffer1;
    private javax.swing.JRadioButtonMenuItem mniSelectCidsFeature1;
    private javax.swing.JRadioButtonMenuItem mniSelectEllipse1;
    private javax.swing.JRadioButtonMenuItem mniSelectPolygon1;
    private javax.swing.JRadioButtonMenuItem mniSelectPolyline1;
    private javax.swing.JRadioButtonMenuItem mniSelectRectangle1;
    private javax.swing.JMenuItem mniSelectRedo1;
    private javax.swing.JMenuItem mniSelectShowLastFeature1;
    private javax.swing.JPopupMenu popMenSelect;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSelectionButton object.
     */
    public GeoSelectionButton() {
        this(MappingComponent.SELECT, CismapBroker.getInstance().getMappingComponent(), null);
    }

    /**
     * Creates a new GeoSelectionButton object.
     *
     * @param  interactionMode   DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  selectName        DOCUMENT ME!
     */
    public GeoSelectionButton(final String interactionMode,
            final MappingComponent mappingComponent,
            final String selectName) {
        this(interactionMode, mappingComponent, selectName, ""); // NOI18N
    }

    /**
     * Creates new form GeoSelectionButton.
     *
     * @param  interactionMode   DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  selectName        DOCUMENT ME!
     * @param  toolTipText       DOCUMENT ME!
     */
    public GeoSelectionButton(final String interactionMode,
            final MappingComponent mappingComponent,
            final String selectName,
            final String toolTipText) {
        setModel(new JToggleButton.ToggleButtonModel());
        init(interactionMode, mappingComponent, selectName, toolTipText);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates new form GeoSelectionButton.
     *
     * @param  interactionMode   DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  selectName        DOCUMENT ME!
     * @param  toolTipText       DOCUMENT ME!
     */
    public final void init(final String interactionMode,
            final MappingComponent mappingComponent,
            final String selectName,
            final String toolTipText) {
        super.init(interactionMode, mappingComponent, selectName);
        selectAction = createSelectAction(interactionMode, mappingComponent);
        selectMenuSelectedAction = createSelectMenuSelectedAction(interactionMode, mappingComponent);
        selectRectangleAction = createSelectRectangleAction(interactionMode, mappingComponent);
        selectPolygonAction = createSelectPolygonAction(interactionMode, mappingComponent);
        selectCidsFeatureAction = createSelectCidsFeatureAction(interactionMode, mappingComponent);
        selectEllipseAction = createSelectEllipseAction(interactionMode, mappingComponent);
        selectPolylineAction = createSelectPolylineAction(interactionMode, mappingComponent);
        selectRedoAction = createSelectRedoAction(interactionMode, mappingComponent);
        selectShowLastFeatureAction = createSelectShowLastFeatureAction(interactionMode, mappingComponent);
        selectBufferAction = createSelectBufferAction(interactionMode, mappingComponent);
        initComponents();

        setPopupMenu(popMenSelect);
        new CidsBeanDropTarget(this);

        setTargetIcon(new javax.swing.ImageIcon(getClass().getResource("/images/selectTarget.png"))); // NOI18N

        this.selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);

        setButtonIcon(selectListener.getMode());
        setModeSelection(selectListener.getMode());
        setLastFeature(selectListener.getLastSelectFeature());

        selectListener.addPropertyChangeListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectAction(final String interactionMode, final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectAction"); // NOI18N
                    }
                    mappingComponent.setInteractionMode(interactionMode);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectMenuSelectedAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectMenuSelectedAction"); // NOI18N
                    }
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectRectangleAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectRectangleAction"); // NOI18N
                    }
                    mappingComponent.setInteractionMode(interactionMode);
                    selectListener.setMode(SelectionListener.RECTANGLE);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectPolygonAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectPolygonAction"); // NOI18N
                    }
                    mappingComponent.setInteractionMode(interactionMode);
                    selectListener.setMode(SelectionListener.POLYGON);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectCidsFeatureAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectCidsFeatureAction"); // NOI18N
                    }
                    mappingComponent.setInteractionMode(interactionMode);

                    CismetThreadPool.execute(new javax.swing.SwingWorker<SelectFeature, Void>() {

                            @Override
                            protected SelectFeature doInBackground() throws Exception {
                                Thread.currentThread().setName("GeoSelectionButton createSelectCidsFeatureAction");
                                final DefaultMetaTreeNode[] nodes = ComponentRegistry.getRegistry()
                                            .getActiveCatalogue()
                                            .getSelectedNodesArray();
                                final Collection<CidsBean> beans = new ArrayList<CidsBean>();
                                for (final DefaultMetaTreeNode dmtn : nodes) {
                                    if (dmtn instanceof ObjectTreeNode) {
                                        final MetaObject mo = ((ObjectTreeNode)dmtn).getMetaObject();
                                        beans.add(mo.getBean());
                                    }
                                }
                                final SelectFeature sf = CidsBeansSelectFeature.createFromBeans(beans, interactionMode);
                                sf.setGeometryType(AbstractNewFeature.geomTypes.MULTIPOLYGON);
                                return sf;
                            }

                            @Override
                            protected void done() {
                                try {
                                    final SelectFeature select = get();
                                    if (select != null) {
                                        selectListener.select(select);
                                    }
                                } catch (final Exception e) {
                                    LOG.error("Exception in Background Thread", e); // NOI18N
                                }
                            }
                        });
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectEllipseAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectEllipseAction"); // NOI18N
                    }
                    mappingComponent.setInteractionMode(interactionMode);
                    selectListener.setMode(SelectionListener.ELLIPSE);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectPolylineAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectPolylineAction"); // NOI18N
                    }
                    mappingComponent.setInteractionMode(interactionMode);
                    selectListener.setMode(SelectionListener.LINESTRING);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectRedoAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("redoSelectAction"); // NOI18N
                    }

                    selectListener.redoLastSelect();
                    mappingComponent.setInteractionMode(interactionMode);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectShowLastFeatureAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selectShowLastFeatureAction"); // NOI18N
                    }

                    selectListener.showLastFeature();
                    mappingComponent.setInteractionMode(interactionMode);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Action createSelectBufferAction(final String interactionMode,
            final MappingComponent mappingComponent) {
        final SelectionListener selectListener = (SelectionListener)mappingComponent.getInputListener(interactionMode);
        return new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("bufferSelectGeometry");                                     // NOI18N
                    }
                    final String message = NbBundle.getMessage(
                            GeoSelectionButton.class,
                            "GeoSelectionButton.createSelectBufferAction().bufferDialog.message"); // NOI18N
                    final String title = NbBundle.getMessage(
                            GeoSelectionButton.class,
                            "GeoSelectionButton.createSelectBufferAction().bufferDialog.title"); // NOI18N
                    final String s = (String)JOptionPane.showInputDialog(
                            StaticSwingTools.getParentFrame(mappingComponent),
                            message,
                            title,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            "");                                                               // NOI18N
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(s);
                    }

                    // , statt . ebenfalls erlauben
                    if (s.matches("\\d*,\\d*")) { // NOI18N
                        s.replace(",", ".");  // NOI18N
                    }

                    try {
                        final float buffer = Float.valueOf(s);

                        final SelectFeature lastFeature = selectListener.getLastSelectFeature();

                        if (lastFeature != null) {
                            // Geometrie-Daten holen
                            final Geometry geom = lastFeature.getGeometry();

                            // Puffer-Geometrie holen
                            final Geometry bufferGeom = geom.buffer(buffer);

                            // und setzen
                            lastFeature.setGeometry(bufferGeom);

                            // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder
                            // ähnliches mehr)
                            lastFeature.setGeometryType(AbstractNewFeature.geomTypes.POLYGON);

                            for (final Object feature
                                        : mappingComponent.getFeatureCollection().getAllFeatures()) {
                                final PFeature sel = (PFeature)mappingComponent.getPFeatureHM().get(feature);

                                if (sel.getFeature().equals(lastFeature)) {
                                    // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                                    // setzen
                                    sel.setCoordArr(bufferGeom.getCoordinates());

                                    // refresh
                                    sel.syncGeometry();

                                    final List v = new ArrayList();
                                    v.add(sel.getFeature());
                                    ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                            .fireFeaturesChanged(v);
                                }
                            }

                            selectListener.select(lastFeature);
                            mappingComponent.setInteractionMode(interactionMode);
                        }
                    } catch (final NumberFormatException ex) {
                        final String messageEx = NbBundle.getMessage(
                                GeoSelectionButton.class,
                                "GeoSelectionButton.createSelectBufferAction().NumberFormatException.bufferDialog.message"); // NOI18N
                        final String titleEx = NbBundle.getMessage(
                                GeoSelectionButton.class,
                                "GeoSelectionButton.createSelectBufferAction().NumberFormatException.bufferDialog.title"); // NOI18N

                        JOptionPane.showMessageDialog(
                            StaticSwingTools.getParentFrame(mappingComponent),
                            messageEx,
                            titleEx,
                            JOptionPane.ERROR_MESSAGE); // NOI18N
                    } catch (final Exception ex) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("", ex);      // NOI18N
                        }
                    }
                }
            };
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        popMenSelect = new javax.swing.JPopupMenu();
        mniSelectRectangle1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        mniSelectPolygon1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        mniSelectEllipse1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        mniSelectPolyline1 = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        jSeparator12 = new javax.swing.JSeparator();
        mniSelectCidsFeature1 = new javax.swing.JRadioButtonMenuItem();
        mniSelectShowLastFeature1 = new javax.swing.JMenuItem();
        mniSelectRedo1 = new javax.swing.JMenuItem();
        mniSelectBuffer1 = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();

        popMenSelect.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) {
                    popMenSelectPopupMenuWillBecomeVisible(evt);
                }
                @Override
                public void popupMenuWillBecomeInvisible(final javax.swing.event.PopupMenuEvent evt) {
                }
                @Override
                public void popupMenuCanceled(final javax.swing.event.PopupMenuEvent evt) {
                }
            });

        mniSelectRectangle1.setAction(selectRectangleAction);
        mniSelectRectangle1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectRectangle1,
            org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectRectangle1.text"));                                                 // NOI18N
        mniSelectRectangle1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
        popMenSelect.add(mniSelectRectangle1);

        mniSelectPolygon1.setAction(selectPolygonAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectPolygon1,
            org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectPolygon1.text"));                                               // NOI18N
        mniSelectPolygon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
        popMenSelect.add(mniSelectPolygon1);

        mniSelectEllipse1.setAction(selectEllipseAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectEllipse1,
            org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectEllipse1.text"));                                               // NOI18N
        mniSelectEllipse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
        popMenSelect.add(mniSelectEllipse1);

        mniSelectPolyline1.setAction(selectPolylineAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectPolyline1,
            org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectPolyline1.text"));                                                // NOI18N
        mniSelectPolyline1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
        popMenSelect.add(mniSelectPolyline1);
        popMenSelect.add(jSeparator12);

        mniSelectCidsFeature1.setAction(selectCidsFeatureAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectCidsFeature1,
            org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectCidsFeature1.text"));                                               // NOI18N
        mniSelectCidsFeature1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
        popMenSelect.add(mniSelectCidsFeature1);

        mniSelectShowLastFeature1.setAction(selectShowLastFeatureAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectShowLastFeature1,
            org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectShowLastFeature1.text"));        // NOI18N
        mniSelectShowLastFeature1.setToolTipText(org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectShowLastFeature1.toolTipText")); // NOI18N
        popMenSelect.add(mniSelectShowLastFeature1);

        mniSelectRedo1.setAction(selectRedoAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectRedo1,
            org.openide.util.NbBundle.getMessage(GeoSelectionButton.class, "GeoSelectionButton.mniSelectRedo1.text")); // NOI18N
        mniSelectRedo1.setToolTipText(org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectRedo1.toolTipText"));                                                     // NOI18N
        popMenSelect.add(mniSelectRedo1);

        mniSelectBuffer1.setAction(selectBufferAction);
        mniSelectBuffer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png")));               // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectBuffer1,
            org.openide.util.NbBundle.getMessage(GeoSelectionButton.class, "GeoSelectionButton.mniSelectBuffer1.text")); // NOI18N
        mniSelectBuffer1.setToolTipText(org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.mniSelectBuffer1.toolTipText"));                                                     // NOI18N
        popMenSelect.add(mniSelectBuffer1);

        setAction(selectAction);
        setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/selectRectangle.png"))); // NOI18N
        setToolTipText(org.openide.util.NbBundle.getMessage(
                GeoSelectionButton.class,
                "GeoSelectionButton.toolTipText"));                                                // NOI18N
    }                                                                                              // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void popMenSelectPopupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) { //GEN-FIRST:event_popMenSelectPopupMenuWillBecomeVisible
        selectMenuSelectedAction.actionPerformed(new ActionEvent(popMenSelect, ActionEvent.ACTION_PERFORMED, null));
    }                                                                                                 //GEN-LAST:event_popMenSelectPopupMenuWillBecomeVisible

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource().equals(selectListener)) {
            if (SelectionListener.PROPERTY_LAST_FEATURE.equals(evt.getPropertyName())) {
                setLastFeature(selectListener.getLastSelectFeature());
            } else if (SelectionListener.PROPERTY_MODE.equals(evt.getPropertyName())) {
                setSelected(true);
                setModeSelection(selectListener.getMode());
                setButtonIcon(selectListener.getMode());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastFeature  DOCUMENT ME!
     */
    private void setLastFeature(final AbstractNewFeature lastFeature) {
        if (lastFeature == null) {
            mniSelectShowLastFeature1.setIcon(null);
            mniSelectShowLastFeature1.setEnabled(false);
            mniSelectRedo1.setIcon(null);
            mniSelectRedo1.setEnabled(false);
            mniSelectBuffer1.setEnabled(false);
        } else {
            switch (lastFeature.getGeometryType()) {
                case ELLIPSE: {
                    mniSelectRedo1.setIcon(mniSelectEllipse1.getIcon());
                    break;
                }

                case LINESTRING: {
                    mniSelectRedo1.setIcon(mniSelectPolyline1.getIcon());
                    break;
                }

                case POLYGON: {
                    mniSelectRedo1.setIcon(mniSelectPolygon1.getIcon());
                    break;
                }

                case RECTANGLE: {
                    mniSelectRedo1.setIcon(mniSelectRectangle1.getIcon());
                    break;
                }
            }

            mniSelectRedo1.setEnabled(true);
            mniSelectBuffer1.setEnabled(true);
            mniSelectShowLastFeature1.setIcon(mniSelectRedo1.getIcon());
            mniSelectShowLastFeature1.setEnabled(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    private void setButtonIcon(final String mode) {
        if (SelectionListener.RECTANGLE.equals(mode)) {
            setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectRectangle.png"))); // NOI18N
            setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectRectangle.png"))); // NOI18N
        } else if (SelectionListener.POLYGON.equals(mode)) {
            setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectPolygon.png")));   // NOI18N
            setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectPolygon.png")));   // NOI18N
        } else if (SelectionListener.ELLIPSE.equals(mode)) {
            setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectEllipse.png")));   // NOI18N
            setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectEllipse.png")));   // NOI18N
        } else if (SelectionListener.LINESTRING.equals(mode)) {
            setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectPolyline.png")));  // NOI18N
            setSelectedIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/images/selectPolyline.png")));  // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    private void setModeSelection(final String mode) {
        mniSelectRectangle1.setSelected(SelectionListener.RECTANGLE.equals(mode));
        mniSelectPolygon1.setSelected(SelectionListener.POLYGON.equals(mode));
        mniSelectEllipse1.setSelected(SelectionListener.ELLIPSE.equals(mode));
        mniSelectPolyline1.setSelected(SelectionListener.LINESTRING.equals(mode));
    }

    @Override
    public String getValue(final String key) {
        return "GeoSelectionButton";
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
