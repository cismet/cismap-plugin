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
package de.cismet.cismap.linearreferencing;

import Sirius.navigator.ui.ComponentRegistry;

import com.vividsolutions.jts.geom.Geometry;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.DecimalFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeatureListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.tools.StationEditorInterface;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TableStationEditor extends javax.swing.JPanel implements DisposableCidsBeanStore, StationEditorInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(TableStationEditor.class);

    //~ Instance fields --------------------------------------------------------

    private CidsBean cidsBean;
    private CidsBean backupBean;
    private boolean inited = false;
    private PropertyChangeListener cidsBeanListener;
    private boolean isSpinnerChangeLocked = false;
    private boolean isFeatureChangeLocked = false;
    private boolean isBeanChangeLocked = false;
    private boolean changedSinceDrop = false;
    private LinearReferencedPointFeatureListener featureListener;
    private final LinearReferencingHelper linearReferencingHelper = FeatureRegistry.getInstance()
                .getLinearReferencingSolver();
    private boolean line;
    private boolean fromStation = false;
    private CidsBean lineBean;
    private LinearReferencedLineEditor dialogLineEditor;
    private StationEditor dialogStationEditor;
    private String routeTable;
    private String otherLinesFrom;
    private String otherLinesQuery;
    private FeatureServiceFeature parentFeature;
    private String stationProperty;
    private final WindowListener dialogCleanupListener = new WindowAdapter() {

            @Override
            public void windowClosed(final WindowEvent e) {
                dialogCleanup();
            }
        };

    private boolean dialogInitialised = false;

    private DockingWindowListener windowCleanupListener = new DockingWindowListener() {

            @Override
            public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
            }

            @Override
            public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow) {
            }

            @Override
            public void windowShown(final DockingWindow window) {
            }

            @Override
            public void windowHidden(final DockingWindow window) {
            }

            @Override
            public void viewFocusChanged(final View previouslyFocusedView, final View focusedView) {
            }

            @Override
            public void windowClosing(final DockingWindow window) throws OperationAbortedException {
                diaExp.dispose();
                dialogCleanup();
            }

            @Override
            public void windowClosed(final DockingWindow window) {
                diaExp.dispose();
                dialogCleanup();
            }

            @Override
            public void windowUndocking(final DockingWindow window) throws OperationAbortedException {
            }

            @Override
            public void windowUndocked(final DockingWindow window) {
            }

            @Override
            public void windowDocking(final DockingWindow window) throws OperationAbortedException {
            }

            @Override
            public void windowDocked(final DockingWindow window) {
            }

            @Override
            public void windowMinimizing(final DockingWindow window) throws OperationAbortedException {
            }

            @Override
            public void windowMinimized(final DockingWindow window) {
            }

            @Override
            public void windowMaximizing(final DockingWindow window) throws OperationAbortedException {
            }

            @Override
            public void windowMaximized(final DockingWindow window) {
            }

            @Override
            public void windowRestoring(final DockingWindow window) throws OperationAbortedException {
            }

            @Override
            public void windowRestored(final DockingWindow window) {
            }
        };

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butExpand;
    private javax.swing.JButton butRemove;
    private javax.swing.JDialog diaExp;
    private javax.swing.JPanel dialogPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form StationEditor.
     *
     * @param  routeTable  DOCUMENT ME!
     */
    public TableStationEditor(final String routeTable) {
        this(false, null, routeTable, null, null);
    }

    /**
     * Creates new form StationEditor.
     *
     * @param  routeTable       DOCUMENT ME!
     * @param  parentFeature    DOCUMENT ME!
     * @param  stationProperty  DOCUMENT ME!
     */
    public TableStationEditor(final String routeTable,
            final FeatureServiceFeature parentFeature,
            final String stationProperty) {
        this(false, null, routeTable, parentFeature, stationProperty);
    }

    /**
     * Creates new form StationEditor.
     *
     * @param  line             DOCUMENT ME!
     * @param  lineBean         DOCUMENT ME!
     * @param  routeTable       DOCUMENT ME!
     * @param  parentFeature    DOCUMENT ME!
     * @param  stationProperty  DOCUMENT ME!
     */
    public TableStationEditor(final boolean line,
            final CidsBean lineBean,
            final String routeTable,
            final FeatureServiceFeature parentFeature,
            final String stationProperty) {
        this.line = line;
        this.lineBean = lineBean;
        this.routeTable = routeTable;
        this.parentFeature = parentFeature;
        this.stationProperty = stationProperty;
        initComponents();
//        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 0.0d, 0.01d));
        setButRemoveVisibility();

        initSpinnerListener();
        initFeatureListener();
        initCidsBeanListener();

        if (lineBean != null) {
            lineBean.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(final PropertyChangeEvent evt) {
                        // todo Konstanten austauschen
                        if ((fromStation && evt.getPropertyName().equals("von"))
                                    || (!fromStation && evt.getPropertyName().equals("bis"))) {
                            cleanup();
                            // neue cidsbean setzen
                            cidsBean = (CidsBean)evt.getNewValue();
                            // neu initialisieren
                            init();
                            cidsBeanChanged((Double)getValue());
                        }
                    }
                });
        }

        diaExp.addWindowListener(dialogCleanupListener);
    }

    //~ Methods ----------------------------------------------------------------

    public boolean isLine() {
        return line;
    }

    public CidsBean getLineBean() {
        return lineBean;
    }

    /**
     * DOCUMENT ME!
     */
    private void setButRemoveVisibility() {
        butRemove.setVisible((parentFeature != null) && (stationProperty != null)
                    && (parentFeature.getProperty(stationProperty) != null));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private View getParentView(final Component c) {
        if ((c.getParent() == null) || (c.getParent() instanceof View)) {
            return (View)c.getParent();
        } else {
            return getParentView(c.getParent());
        }
    }

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        // aufräumen falls vorher cidsbean schon gesetzt war
        cleanup();
        // neue cidsbean setzen
        this.cidsBean = cidsBean;
        createBackupBean();
        if (line && (lineBean != null)) {
            fromStation = cidsBean.equals(lineBean.getProperty("von"));
        }
        // neu initialisieren
        init();

        cidsBeanChanged((Double)getValue());

        if (diaExp.isVisible()) {
            diaExp.pack();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        diaExp = new javax.swing.JDialog();
        dialogPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jSpinner1 = new javax.swing.JSpinner();
        butExpand = new javax.swing.JButton();
        butRemove = new javax.swing.JButton();

        diaExp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        diaExp.setTitle(org.openide.util.NbBundle.getMessage(
                TableStationEditor.class,
                "TableStationEditor.diaExp.title")); // NOI18N
        diaExp.getContentPane().setLayout(new java.awt.GridBagLayout());

        dialogPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(jPanel1);

        dialogPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        diaExp.getContentPane().add(dialogPanel, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 0.0d, 1.0d));
        jSpinner1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner1, "###.##"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSpinner1, gridBagConstraints);

        butExpand.setText(org.openide.util.NbBundle.getMessage(
                TableStationEditor.class,
                "TableStationEditor.butExpand.text")); // NOI18N
        butExpand.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butExpandActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(butExpand, gridBagConstraints);

        butRemove.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/icon-remove-sign.png"))); // NOI18N
        butRemove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butRemoveActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(butRemove, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butExpandActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butExpandActionPerformed
        final View w = getParentView(this);

        if (w != null) {
            w.removeListener(windowCleanupListener);
            w.addListener(windowCleanupListener);
        }

        if (!dialogInitialised && !diaExp.isVisible()) {
            if (line) {
                dialogLineEditor = new LinearReferencedLineEditor(true, true, true, routeTable);

                if ((otherLinesFrom != null) && (otherLinesQuery != null)) {
                    dialogLineEditor.setOtherLinesEnabled(true);
                    dialogLineEditor.setOtherLinesQueryAddition(otherLinesFrom, otherLinesQuery);
                }
                // se.setDrawingFeaturesEnabled(false);
                dialogLineEditor.setCidsBean(lineBean);
                jPanel1.removeAll();
                jPanel1.add(dialogLineEditor);
//                diaExp.setSize(460,100);
                dialogLineEditor.addListener(new LinearReferencedLineEditorListener() {

                        @Override
                        public void linearReferencedLineCreated() {
                            // route was changed
                            if (line) {
                                final CidsBean newLineBean = dialogLineEditor.getCidsBean();
                                for (final String propertyName : newLineBean.getPropertyNames()) {
                                    if (!propertyName.equalsIgnoreCase("id")) {
                                        try {
                                            lineBean.setProperty(propertyName, newLineBean.getProperty(propertyName));
                                        } catch (Exception ex) {
                                            LOG.error("Error while setting a property", ex);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void otherLinesPanelVisibilityChange(final boolean visible) {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        diaExp.pack();
                                    }
                                });
                        }
                    });
                diaExp.pack();
            } else {
                dialogStationEditor = new StationEditor(true, routeTable, true);
                dialogStationEditor.setCidsBeanStore(this);
                jPanel1.removeAll();
                jPanel1.add(dialogStationEditor);
//                diaExp.setSize(200,100);
                diaExp.pack();
            }
            dialogInitialised = true;
        }

        EventQueue.invokeLater(new Thread("show linear referencing editor popup") {

                @Override
                public void run() {
                    diaExp.setAlwaysOnTop(true);
                    try {
                        final Point p = TableStationEditor.this.getLocationOnScreen();
                        final Rectangle r = TableStationEditor.this.getBounds();
                        diaExp.setLocation((int)p.getX(), (int)(p.getY() + r.getHeight()));
                        diaExp.setVisible(true);
                    } catch (IllegalComponentStateException e) {
                        LOG.warn("Cannot calculate popup position under editor", e);
                        StaticSwingTools.centerWindowOnScreen(diaExp);
                    }
                }
            });
    }//GEN-LAST:event_butExpandActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butRemoveActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRemoveActionPerformed
        final int ans = JOptionPane.showConfirmDialog(
                ComponentRegistry.getRegistry().getMainWindow(),
                NbBundle.getMessage(
                    TableStationEditor.class,
                    "TableStationEditor.butExpand1ActionPerformed.JOptionPane.message"),
                NbBundle.getMessage(
                    TableStationEditor.class,
                    "TableStationEditor.butExpand1ActionPerformed.JOptionPane.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (ans == JOptionPane.YES_OPTION) {
            if (parentFeature != null) {
                parentFeature.setProperty(stationProperty, null);
            }
        }
    }//GEN-LAST:event_butRemoveActionPerformed

    @Override
    public CidsBean getCidsBean() {
        return cidsBean;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void undoChanges() {
        restore();
    }

    /**
     * DOCUMENT ME!
     */
    private void createBackupBean() {
        try {
            if (cidsBean == null) {
                return;
            }
            final Double value = linearReferencingHelper.getLinearValueFromStationBean(cidsBean);
            final CidsBean routeBean = linearReferencingHelper.getRouteBeanFromStationBean(cidsBean);
            backupBean = linearReferencingHelper.createStationBeanFromRouteBean(routeBean, value);
        } catch (Exception e) {
            LOG.error("Error while creatng backup bean.", e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void restore() {
        try {
            if (backupBean == null) {
                cidsBean = null;
            }
            final Double value = linearReferencingHelper.getLinearValueFromStationBean(backupBean);
            linearReferencingHelper.setLinearValueToStationBean(value, cidsBean);

            final CidsBean routeBean = linearReferencingHelper.getRouteBeanFromStationBean(backupBean);
            if (routeBean != null) {
                linearReferencingHelper.setRouteBeanToStationBean(routeBean, cidsBean);
            }
        } catch (Exception e) {
            LOG.error("Cannot restore station bean.", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   crs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isCrsSupported(final Crs crs) {
        return (CrsTransformer.extractSridFromCrs(crs.getCode()) == 35833)
                    || (CrsTransformer.extractSridFromCrs(crs.getCode()) == 5650);
    }

    /**
     * DOCUMENT ME!
     */
    private void cleanup() {
        final CidsBean pointBean = getCidsBean();
        if (pointBean != null) {
            pointBean.removePropertyChangeListener(getCidsBeanListener());
        }

        if (pointBean != null) {
            // altes feature entfernen
            final LinearReferencedPointFeature oldFeature = FeatureRegistry.getInstance()
                        .removeStationFeature(
                            pointBean);
            if (oldFeature != null) {
                // listener auf altem Feature entfernen
                oldFeature.removeListener(getFeatureListener());
            }

//            FeatureRegistry.getInstance().removeListener(pointBean, getMapRegistryListener());
        }

//            final Feature badGeomFeature = getBadGeomFeature();
//            if (badGeomFeature != null) {
//                // badgeomfeature entfernen.
//                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(badGeomFeature);
//                setBadGeomFeature(null);
//            }
//        diaExp.dispose();
        setInited(false);
    }

    /**
     * DOCUMENT ME!
     */
    private void dialogCleanup() {
        if (dialogLineEditor != null) {
            dialogLineEditor.dispose();
            jPanel1.remove(dialogLineEditor);
            dialogLineEditor = null;
            dialogInitialised = false;
        }

        if (dialogStationEditor != null) {
            dialogStationEditor.dispose();
            jPanel1.remove(dialogStationEditor);
            dialogStationEditor = null;
            dialogInitialised = false;
        }
        final View w = getParentView(this);

        if (w != null) {
            w.removeListener(windowCleanupListener);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void init() {
        // wird das aktuelle crs unterstützt ?
        if (!isCrsSupported(CismapBroker.getInstance().getSrs())) {
            // TODO: reagiere
// showCrsNotSupported();
            // noch nicht initialisiert ?
        } else if (!isInited()) {
            final CidsBean pointBean = getCidsBean();

            if (pointBean != null) {
                pointBean.addPropertyChangeListener(getCidsBeanListener());
                final double pointValue = (Double)getValue();

                setValueToFeature(pointValue);
                setValueToSpinner(pointValue);

                // badgeom feature und button nur falls die realgeom weiter als 1 von der route entfernt ist
// final double distance = linearReferencingHelper.distanceOfStationGeomToRouteGeomFromStationBean(
// pointBean);
// if (distance > 1) {
// setBadGeomFeature(createBadGeomFeature(
// linearReferencingHelper.getPointGeometryFromStationBean(pointBean)));
// } else {
// setBadGeomFeature(null);
// }

                // die aktuelle cidsBean als listener bei stationtomapregistry anmelden
// FeatureRegistry.getInstance().addListener(pointBean, getMapRegistryListener());

                // feature erzeugen und auf der Karte anzeigen lassen
                final LinearReferencedPointFeature pointFeature = FeatureRegistry.getInstance()
                            .addStationFeature(pointBean);
                pointFeature.setEditable(true);

                // spinner auf intervall der neuen route anpassen
                ((SpinnerNumberModel)jSpinner1.getModel()).setMaximum(Math.ceil(
                        pointFeature.getLineGeometry().getLength()));

                // auf änderungen des features horchen
                pointFeature.addListener(getFeatureListener());

//                firePointCreated();
//                updateBadGeomButton();
//                updateSplitButton();

                // editier panel anzeigen
//                showCard(Card.edit);

                // fertig intialisiert
                setInited(true);
            }
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        jSpinner1.setEnabled(enabled);
        butExpand.setEnabled(enabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature getStationFeature() {
        return (LinearReferencedPointFeature)FeatureRegistry.getInstance().getFeature(cidsBean);
    }

    /**
     * DOCUMENT ME!
     */
    private void initSpinnerListener() {
        ((JSpinner.DefaultEditor)jSpinner1.getEditor()).getTextField()
                .getDocument()
                .addDocumentListener(new DocumentListener() {

                        @Override
                        public void insertUpdate(final DocumentEvent de) {
                            spinnerChanged();
                        }

                        @Override
                        public void removeUpdate(final DocumentEvent de) {
                            spinnerChanged();
                        }

                        @Override
                        public void changedUpdate(final DocumentEvent de) {
                            spinnerChanged();
                        }
                    });
        ((JSpinner.DefaultEditor)jSpinner1.getEditor()).getTextField().addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(final FocusEvent fe) {
//                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().select(getFeature());
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void initFeatureListener() {
        setFeatureListener(new LinearReferencedPointFeatureListener() {

                @Override
                public void featureMoved(final LinearReferencedPointFeature pointFeature) {
                    featureChanged(pointFeature.getCurrentPosition());
                }

                @Override
                public void featureMerged(final LinearReferencedPointFeature mergePoint,
                        final LinearReferencedPointFeature withPoint) {
                    final CidsBean withBean = FeatureRegistry.getInstance().getCidsBean(withPoint);
                    setCidsBean(withBean);

//                    updateSplitButton();
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void initCidsBeanListener() {
        setCidsBeanListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent pce) {
                    if (pce.getPropertyName().equals(linearReferencingHelper.getValueProperty(cidsBean))) {
                        cidsBeanChanged((Double)pce.getNewValue());
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getValue() {
        if (cidsBean != null) {
            return linearReferencingHelper.getLinearValueFromStationBean(cidsBean);
        } else {
            return 0d;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void cidsBeanChanged(final double value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cidsbean changed: " + value, new CurrentStackTrace());
        }

        try {
            lockBeanChange(true);

            setChangedSinceDrop(true);

            setValueToSpinner(value);
//            setValueToLabel(value);
            setValueToFeature(value);
            firePropertyChange("wert", 0, value);

            // realgeoms nur nach manueller eingabe updaten
            if (isInited()) {
                updateGeometry();
            }
        } finally {
            lockBeanChange(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lock  DOCUMENT ME!
     */
    private void lockBeanChange(final boolean lock) {
        isBeanChangeLocked = lock;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  changedSinceDrop  DOCUMENT ME!
     */
    private void setChangedSinceDrop(final boolean changedSinceDrop) {
        this.changedSinceDrop = changedSinceDrop;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PropertyChangeListener getCidsBeanListener() {
        return cidsBeanListener;
    }

    /**
     * DOCUMENT ME!
     */
    private void spinnerChanged() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("spinner changed", new CurrentStackTrace());
        }

        try {
            lockSpinnerChange(true);

            final JFormattedTextField.AbstractFormatter formatter = ((JSpinner.DefaultEditor)jSpinner1.getEditor())
                        .getTextField().getFormatter();
            final String text = ((JSpinner.DefaultEditor)jSpinner1.getEditor()).getTextField().getText();
            if (!text.isEmpty()) {
                final Double d = new Double(text.replace(',', '.'));
                setPointValue(d);
            }
        } finally {
            lockSpinnerChange(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void featureChanged(final double value) {
        try {
            lockFeatureChange(true);
            setPointValue(value);
        } finally {
            lockFeatureChange(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void setValueToSpinner(final double value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("change spinner");
        }

        if (!isSpinnerChangeLocked()) {
            jSpinner1.setValue(value);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LinearReferencedPointFeatureListener getFeatureListener() {
        return featureListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureListener  DOCUMENT ME!
     */
    private void setFeatureListener(final LinearReferencedPointFeatureListener featureListener) {
        this.featureListener = featureListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void setValueToFeature(final double value) {
        if (!isFeatureChangeLocked()) {
            final LinearReferencedPointFeature pointFeature = getFeature();
            if (pointFeature != null) {
                pointFeature.setInfoFormat(new DecimalFormat("###.00"));
                pointFeature.moveToPosition(value);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("there are no feature to move");
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updateGeometry() {
        try {
            final Geometry geom = LinearReferencedPointFeature.getPointOnLine(
                    linearReferencingHelper.getLinearValueFromStationBean(cidsBean),
                    linearReferencingHelper.getRouteGeometryFromStationBean(cidsBean));
            geom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
            linearReferencingHelper.setPointGeometryToStationBean(geom, getCidsBean());
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error while setting the station geometry", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void setPointValue(final double value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("change bean value to " + value);
        }
        final CidsBean pointBean = getCidsBean();
        final double oldValue = linearReferencingHelper.getLinearValueFromStationBean(pointBean);

        if (oldValue != value) {
            try {
                if (!isFeatureChangeLocked()) {
//                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().select(getFeature());
                }
                if (!isBeanChangeLocked()) {
//                    linearReferencingHelper.setLinearValueToStationBean((double)Math.round(value), pointBean);
                    linearReferencingHelper.setLinearValueToStationBean(value, pointBean);
                }
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("error changing bean", ex);
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no changes needed, old value was " + oldValue);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsBeanListener  DOCUMENT ME!
     */
    private void setCidsBeanListener(final PropertyChangeListener cidsBeanListener) {
        this.cidsBeanListener = cidsBeanListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature getFeature() {
        return (LinearReferencedPointFeature)FeatureRegistry.getInstance().getFeature(getCidsBean());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isSpinnerChangeLocked() {
        return isSpinnerChangeLocked;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isFeatureChangeLocked() {
        return isFeatureChangeLocked;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isBeanChangeLocked() {
        return isBeanChangeLocked;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lock  DOCUMENT ME!
     */
    private void lockSpinnerChange(final boolean lock) {
        isSpinnerChangeLocked = lock;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lock  DOCUMENT ME!
     */
    private void lockFeatureChange(final boolean lock) {
        isFeatureChangeLocked = lock;
    }

    @Override
    public void dispose() {
        cleanup();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isInited() {
        return inited;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  inited  DOCUMENT ME!
     */
    private void setInited(final boolean inited) {
        this.inited = inited;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the otherLinesFrom
     */
    public String getOtherLinesFrom() {
        return otherLinesFrom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  otherLinesFrom  the otherLinesFrom to set
     */
    public void setOtherLinesFrom(final String otherLinesFrom) {
        this.otherLinesFrom = otherLinesFrom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the otherLinesQuery
     */
    public String getOtherLinesQuery() {
        return otherLinesQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  otherLinesQuery  the otherLinesQuery to set
     */
    public void setOtherLinesQuery(final String otherLinesQuery) {
        this.otherLinesQuery = otherLinesQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the parentFeature
     */
    public FeatureServiceFeature getParentFeature() {
        return parentFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parentFeature  the parentFeature to set
     */
    public void setParentFeature(final FeatureServiceFeature parentFeature) {
        this.parentFeature = parentFeature;
        setButRemoveVisibility();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the stationProperty
     */
    public String getStationProperty() {
        return stationProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  stationProperty  the stationProperty to set
     */
    public void setStationProperty(final String stationProperty) {
        this.stationProperty = stationProperty;
        setButRemoveVisibility();
    }
}
