/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.linearreferencing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.CardLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.DecimalFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.navigator.utils.CidsBeanDropListener;
import de.cismet.cids.navigator.utils.CidsBeanDropTarget;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeatureListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.CrsChangeListener;
import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class StationEditor extends JPanel implements DisposableCidsBeanStore,
    LinearReferencingConstants,
    CidsBeanDropListener,
    LinearReferencingSingletonInstances {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private enum Card {

        //~ Enum constants -----------------------------------------------------

        edit, add, error
    }

    //~ Instance fields --------------------------------------------------------

    private boolean inited;

    // private LinearReferencedPointFeature feature;
    private PropertyChangeListener cidsBeanListener;
    private boolean isSpinnerChangeLocked = false;
    private boolean isFeatureChangeLocked = false;
    private boolean isBeanChangeLocked = false;
    private ImageIcon ico;
    private CidsBean cidsBean;
    private Collection<LinearReferencedPointEditorListener> listeners =
        new ArrayList<LinearReferencedPointEditorListener>();
    private LinearReferencedPointFeatureListener featureListener;
    private CrsChangeListener crsChangeListener;
    private FeatureRegistryListener mapRegistryListener;
    private LineEditorDropBehavior dropBehavior;
    private Feature badGeomFeature;
    private XBoundingBox boundingbox;
    private boolean isAutoZoomActivated = true;
    private boolean changedSinceDrop = false;
    private boolean isEditable;
    private boolean firstStationInCurrentBB = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton badGeomButton;
    private javax.swing.JButton badGeomCorrectButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel labGwk;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblPointValue;
    private javax.swing.JLabel lblRoute;
    private javax.swing.JPanel panAdd;
    private javax.swing.JPanel panEdit;
    private javax.swing.JPanel panError;
    private javax.swing.JButton splitButton;
    private javax.swing.JSpinner spnPointValue;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationEditor object.
     */
    public StationEditor() {
        this(true);
    }

    /**
     * Creates a new StationEditor object.
     *
     * @param  isEditable  DOCUMENT ME!
     */
    protected StationEditor(final boolean isEditable) {
        initComponents();

        setEditable(isEditable);
        if (isEditable) {
            try {
                new CidsBeanDropTarget(panAdd);
                new CidsBeanDropTarget(this);
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("error while creating CidsBeanDropTarget");
                }
            }

            initSpinnerListener();
            initFeatureListener();
            initMapRegistryListener();
            initCrsChangeListener();
            initCidsBeanListener();

            CismapBroker.getInstance().addCrsChangeListener(getCrsChangeListener());
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  isEditable  DOCUMENT ME!
     */
    private void setEditable(final boolean isEditable) {
        this.isEditable = isEditable;
        spnPointValue.setVisible(isEditable);
        lblPointValue.setVisible(!isEditable);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isEditable() {
        return isEditable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final LinearReferencedPointEditorListener listener) {
        return listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final LinearReferencedPointEditorListener listener) {
        return listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ico  DOCUMENT ME!
     */
    public void setImageIcon(final ImageIcon ico) {
        this.ico = ico;
        if (getFeature() != null) {
            getFeature().setIconImage(ico);
        }
        jLabel5.setIcon(ico);
    }

    /**
     * DOCUMENT ME!
     */
    private void firePointCreated() {
        for (final LinearReferencedPointEditorListener listener : listeners) {
            listener.pointCreated();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature getFeature() {
        return (LinearReferencedPointFeature)FEATURE_REGISTRY.getFeature(getCidsBean());
    }

    @Override
    public void beansDropped(final ArrayList<CidsBean> beans) {
        if (isEditable()) {
            CidsBean routeBean = null;
            for (final CidsBean bean : beans) {
                if (bean.getMetaObject().getMetaClass().getName().equals(CN_ROUTE)) {
                    if ((getDropBehavior() == null) || getDropBehavior().checkForAdding(routeBean)) {
                        routeBean = bean;
                        setChangedSinceDrop(false);
                    }
                    double value = 0d;

                    if (isFirstStationInCurrentBB()) {
                        value = getPointInCurrentBB(routeBean);
                    }

                    setCidsBean(FEATURE_REGISTRY.getInstance().getLinearReferencingSolver()
                                .createStationBeanFromRouteBean(routeBean, value));
                    if (isAutoZoomActivated) {
                        zoomToFeatureCollection(getZoomFeatures());
                    }
                    return;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("no route found in dropped objects");
            }
        }
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
     * @return  DOCUMENT ME!
     */
    private JButton getBadGeomCorrectButton() {
        return badGeomCorrectButton;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature getBadGeomFeature() {
        return badGeomFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  badGeomFeature  DOCUMENT ME!
     */
    private void setBadGeomFeature(final Feature badGeomFeature) {
        this.badGeomFeature = badGeomFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JToggleButton getBadGeomButton() {
        return badGeomButton;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JSpinner getValueSpinner() {
        return spnPointValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureRegistryListener getMapRegistryListener() {
        return mapRegistryListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mapRegistryListener  DOCUMENT ME!
     */
    private void setMapRegistryListener(final FeatureRegistryListener mapRegistryListener) {
        this.mapRegistryListener = mapRegistryListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CrsChangeListener getCrsChangeListener() {
        return crsChangeListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crsChangeListener  DOCUMENT ME!
     */
    private void setCrsChangeListener(final CrsChangeListener crsChangeListener) {
        this.crsChangeListener = crsChangeListener;
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
     * @return  DOCUMENT ME!
     */
    private JButton getSplitButton() {
        return splitButton;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LineEditorDropBehavior getDropBehavior() {
        return dropBehavior;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dropBehavior  DOCUMENT ME!
     */
    public void setDropBehavior(final LineEditorDropBehavior dropBehavior) {
        this.dropBehavior = dropBehavior;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the firstStationInCurrentBB
     */
    public boolean isFirstStationInCurrentBB() {
        return firstStationInCurrentBB;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  firstStationInCurrentBB  the firstStationInCurrentBB to set
     */
    public void setFirstStationInCurrentBB(final boolean firstStationInCurrentBB) {
        this.firstStationInCurrentBB = firstStationInCurrentBB;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasChangedSinceDrop() {
        return changedSinceDrop;
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
     */
    private void initMapRegistryListener() {
        setMapRegistryListener(new FeatureRegistryListener() {

                @Override
                public void FeatureCountChanged() {
                    updateSplitButton();
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void initCrsChangeListener() {
        setCrsChangeListener(new CrsChangeListener() {

                @Override
                public void crsChanged(final CrsChangedEvent event) {
                    if (!isCrsSupported(event.getCurrentCrs())) {
                        showCrsNotSupported();
                    } else {
                        init();
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void showCrsNotSupported() {
        cleanup();
        setErrorMsg("Das aktuelle CRS wird vom Stationierungseditor nicht unterstützt.");
        showCard(Card.error);
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
    private void initCidsBeanListener() {
        setCidsBeanListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent pce) {
                    if (pce.getPropertyName().equals(PROP_STATION_VALUE)) {
                        cidsBeanChanged((Double)pce.getNewValue());
                    }
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
                    final CidsBean withBean = FEATURE_REGISTRY.getCidsBean(withPoint);
                    setCidsBean(withBean);

                    updateSplitButton();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  card  DOCUMENT ME!
     */
    private void showCard(final Card card) {
        switch (card) {
            case edit: {
                ((CardLayout)getLayout()).show(this, "edit");
                break;
            }
            case add: {
                ((CardLayout)getLayout()).show(this, "add");
                break;
            }
            case error: {
                ((CardLayout)getLayout()).show(this, "error");
                break;
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initSpinnerListener() {
        ((JSpinner.DefaultEditor)getValueSpinner().getEditor()).getTextField()
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
        ((JSpinner.DefaultEditor)getValueSpinner().getEditor()).getTextField().addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(final FocusEvent fe) {
                    MAPPING_COMPONENT.getFeatureCollection().select(getFeature());
                }
            });
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

            final AbstractFormatter formatter = ((JSpinner.DefaultEditor)getValueSpinner().getEditor()).getTextField()
                        .getFormatter();
            final String text = ((JSpinner.DefaultEditor)getValueSpinner().getEditor()).getTextField().getText();
            if (!text.isEmpty()) {
                try {
                    setPointValue((Double)formatter.stringToValue(text));
                } catch (ParseException ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("error parsing spinner", ex);
                    }
                }
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
    private void cidsBeanChanged(final double value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cidsbean changed: " + value, new CurrentStackTrace());
        }

        try {
            lockBeanChange(true);

            setChangedSinceDrop(true);

            setValueToSpinner(value);
            setValueToLabel(value);
            setValueToFeature(value);

            // realgeoms nur nach manueller eingabe updaten
            if (isInited() && isEditable()) {
                updateGeometry();
            }
        } finally {
            lockBeanChange(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void setValueToLabel(final double value) {
        getPointValueLabel().setText(Long.toString(Math.round(value)));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JLabel getPointValueLabel() {
        return lblPointValue;
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
            getValueSpinner().setValue(Math.round(value));
        }
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
                pointFeature.setInfoFormat(new DecimalFormat("###"));
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
            final Geometry geom = LinearReferencedPointFeature.getPointOnLine(FEATURE_REGISTRY.getInstance()
                            .getLinearReferencingSolver().getLinearValueFromStationBean(cidsBean),
                    FEATURE_REGISTRY.getInstance().getLinearReferencingSolver().getRouteGeometryFromStationBean(
                        cidsBean));
            geom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
            FEATURE_REGISTRY.getInstance()
                    .getLinearReferencingSolver()
                    .setPointGeometryToStationBean(geom, getCidsBean());
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error while setting the " + PROP_STATION_GEOM + "property", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void setPointValue(final double value) {
        if (isEditable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("change bean value to " + value);
            }
            final CidsBean pointBean = getCidsBean();
            final double oldValue = FEATURE_REGISTRY.getInstance()
                        .getLinearReferencingSolver()
                        .getLinearValueFromStationBean(pointBean);

            if (oldValue != value) {
                try {
                    if (!isFeatureChangeLocked()) {
                        MAPPING_COMPONENT.getFeatureCollection().select(getFeature());
                    }
                    if (!isBeanChangeLocked()) {
                        FEATURE_REGISTRY.getInstance()
                                .getLinearReferencingSolver()
                                .setLinearValueToStationBean((double)Math.round(value), pointBean);
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
     * @param   geom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Feature createBadGeomFeature(final Geometry geom) {
        final DefaultStyledFeature dsf = new DefaultStyledFeature();
        dsf.setGeometry(geom);
        dsf.setCanBeSelected(false);
        dsf.setPointAnnotationSymbol(FeatureAnnotationSymbol.newCenteredFeatureAnnotationSymbol(
                new javax.swing.ImageIcon(
                    StationEditor.class.getResource(
                        "/de/cismet/cids/custom/objecteditors/wrrl_db_mv/exclamation-octagon.png")).getImage(),
                new javax.swing.ImageIcon(
                    StationEditor.class.getResource(
                        "/de/cismet/cids/custom/objecteditors/wrrl_db_mv/exclamation-octagon.png")).getImage()));
        return dsf;
    }

    /**
     * DOCUMENT ME!
     */
    private void cleanup() {
        final CidsBean pointBean = getCidsBean();
        if (pointBean != null) {
            pointBean.removePropertyChangeListener(getCidsBeanListener());
        }

        if (isEditable()) {
            if (pointBean != null) {
                // altes feature entfernen
                final LinearReferencedPointFeature oldFeature = FEATURE_REGISTRY.removeStationFeature(
                        pointBean);
                if (oldFeature != null) {
                    // listener auf altem Feature entfernen
                    oldFeature.removeListener(getFeatureListener());
                }

                FEATURE_REGISTRY.removeListener(pointBean, getMapRegistryListener());
            }

            final Feature badGeomFeature = getBadGeomFeature();
            if (badGeomFeature != null) {
                // badgeomfeature entfernen.
                MAPPING_COMPONENT.getFeatureCollection().removeFeature(badGeomFeature);
                setBadGeomFeature(null);
            }
        }

        setInited(false);

        if (isEditable()) {
            // auf startzustand setzen => hinzufügenpanel anzeigen
            showCard(Card.add);
        } else {
            setErrorMsg("keine Station zugewiesen");
            showCard(Card.error);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void init() {
        // wird das aktuelle crs unterstützt ?
        if (!isCrsSupported(CismapBroker.getInstance().getSrs())) {
            showCrsNotSupported();
            // noch nicht initialisiert ?
        } else if (!isInited()) {
            final CidsBean pointBean = getCidsBean();

            if (pointBean != null) {
                pointBean.addPropertyChangeListener(getCidsBeanListener());
                final double pointValue = getValue();
                setValueToLabel(pointValue);
                labGwk.setText(FEATURE_REGISTRY.getInstance().getLinearReferencingSolver().getRouteNameFromStationBean(
                        pointBean));

                if (isEditable()) {
                    setValueToFeature(pointValue);
                    setValueToSpinner(pointValue);

                    // badgeom feature und button nur falls die realgeom weiter als 1 von der route entfernt ist
                    final double distance = FEATURE_REGISTRY.getInstance()
                                .getLinearReferencingSolver()
                                .distanceOfStationGeomToRouteGeomFromStationBean(
                                    pointBean);
                    if (distance > 1) {
                        setBadGeomFeature(createBadGeomFeature(
                                FEATURE_REGISTRY.getInstance().getLinearReferencingSolver()
                                            .getPointGeometryFromStationBean(pointBean)));
                    } else {
                        setBadGeomFeature(null);
                    }

                    // die aktuelle cidsBean als listener bei stationtomapregistry anmelden
                    FEATURE_REGISTRY.addListener(pointBean, getMapRegistryListener());

                    // feature erzeugen und auf der Karte anzeigen lassen
                    final LinearReferencedPointFeature pointFeature = FEATURE_REGISTRY.addStationFeature(pointBean);
                    if (ico != null) {
                        pointFeature.setIconImage(ico);
                    }
                    pointFeature.setEditable(true);

                    // spinner auf intervall der neuen route anpassen
                    ((SpinnerNumberModel)getValueSpinner().getModel()).setMaximum(Math.ceil(
                            pointFeature.getLineGeometry().getLength()));

                    // auf änderungen des features horchen
                    pointFeature.addListener(getFeatureListener());

                    firePointCreated();
                }
                updateBadGeomButton();
                updateSplitButton();

                // editier panel anzeigen
                showCard(Card.edit);

                // fertig intialisiert
                setInited(true);
            } else {
                if (isEditable()) {
                    showCard(Card.add);
                } else {
                    setErrorMsg("keine Station zugewiesen");
                    showCard(Card.error);
                }
            }
        }
    }

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        // aufräumen falls vorher cidsbean schon gesetzt war
        cleanup();

        // neue cidsbean setzen
        this.cidsBean = cidsBean;

        // neu initialisieren
        init();

        cidsBeanChanged(getValue());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  msg  DOCUMENT ME!
     */
    private void setErrorMsg(final String msg) {
        lblError.setText(msg);
    }

    /**
     * DOCUMENT ME!
     */
    private void updateBadGeomButton() {
        final boolean visible = isEditable() && (getBadGeomFeature() != null);
        getBadGeomButton().setVisible(visible);
        getBadGeomCorrectButton().setVisible(visible && getBadGeomButton().isSelected());
    }

    @Override
    public CidsBean getCidsBean() {
        return cidsBean;
    }

    /**
     * DOCUMENT ME!
     */
    private void updateSplitButton() {
        if (getCidsBean() != null) {
            getSplitButton().setVisible(FEATURE_REGISTRY.getCounter(getCidsBean()) > 1);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void splitPoint() {
        if (isEditable()) {
            final double oldPosition = getFeature().getCurrentPosition();

            final CidsBean pointBean = FEATURE_REGISTRY.getInstance()
                        .getLinearReferencingSolver()
                        .createStationBeanFromRouteBean(FEATURE_REGISTRY.getInstance().getLinearReferencingSolver()
                            .getRouteBeanFromStationBean(getCidsBean()));
            setCidsBean(pointBean);

            // neue station auf selbe position setzen wie die alte
            getFeature().moveToPosition(oldPosition);
        }
    }

    @Override
    public void dispose() {
        cleanup();

        CismapBroker.getInstance().removeCrsChangeListener(getCrsChangeListener());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getValue() {
        if (cidsBean != null) {
            return FEATURE_REGISTRY.getInstance().getLinearReferencingSolver().getLinearValueFromStationBean(cidsBean);
        } else {
            return 0d;
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

        panEdit = new javax.swing.JPanel();
        spnPointValue = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        labGwk = new javax.swing.JLabel();
        splitButton = new javax.swing.JButton();
        badGeomButton = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        badGeomCorrectButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        lblPointValue = new javax.swing.JLabel();
        lblRoute = new javax.swing.JLabel();
        panAdd = new AddPanel();
        jLabel3 = new javax.swing.JLabel();
        panError = new javax.swing.JPanel();
        lblError = new javax.swing.JLabel();

        setEnabled(false);
        setOpaque(false);
        setLayout(new java.awt.CardLayout());

        panEdit.setOpaque(false);
        panEdit.setLayout(new java.awt.GridBagLayout());

        spnPointValue.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 0.0d, 1.0d));
        spnPointValue.setEditor(new javax.swing.JSpinner.NumberEditor(spnPointValue, "###"));
        spnPointValue.setMaximumSize(new java.awt.Dimension(100, 28));
        spnPointValue.setMinimumSize(new java.awt.Dimension(100, 28));
        spnPointValue.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panEdit.add(spnPointValue, gridBagConstraints);

        jLabel5.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/station.png")));                      // NOI18N
        jLabel5.setText(org.openide.util.NbBundle.getMessage(StationEditor.class, "StationEditor.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panEdit.add(jLabel5, gridBagConstraints);

        labGwk.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labGwk.setText(org.openide.util.NbBundle.getMessage(StationEditor.class, "StationEditor.labGwk.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panEdit.add(labGwk, gridBagConstraints);

        splitButton.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/sql-join-left.png"))); // NOI18N
        splitButton.setText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.splitButton.text"));                                                // NOI18N
        splitButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.splitButton.toolTipText"));                                         // NOI18N
        splitButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    splitButtonActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panEdit.add(splitButton, gridBagConstraints);

        badGeomButton.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/exclamation.png"))); // NOI18N
        badGeomButton.setText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.badGeomButton.text"));                                            // NOI18N
        badGeomButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.badGeomButton.toolTipText"));                                     // NOI18N
        badGeomButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    badGeomButtonActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panEdit.add(badGeomButton, gridBagConstraints);

        jPanel2.setMaximumSize(new java.awt.Dimension(32, 0));
        jPanel2.setMinimumSize(new java.awt.Dimension(32, 0));
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(32, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        panEdit.add(jPanel2, gridBagConstraints);

        badGeomCorrectButton.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/node-delete.png"))); // NOI18N
        badGeomCorrectButton.setText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.badGeomCorrectButton.text"));                                     // NOI18N
        badGeomCorrectButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.badGeomCorrectButton.toolTipText"));                              // NOI18N
        badGeomCorrectButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    badGeomCorrectButtonActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panEdit.add(badGeomCorrectButton, gridBagConstraints);

        jPanel3.setMaximumSize(new java.awt.Dimension(32, 0));
        jPanel3.setMinimumSize(new java.awt.Dimension(32, 0));
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(32, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        panEdit.add(jPanel3, gridBagConstraints);

        lblPointValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblPointValue.setText(org.openide.util.NbBundle.getMessage(
                StationEditor.class,
                "StationEditor.lblPointValue.text_1")); // NOI18N
        lblPointValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblPointValue.setMaximumSize(new java.awt.Dimension(100, 28));
        lblPointValue.setMinimumSize(new java.awt.Dimension(100, 28));
        lblPointValue.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panEdit.add(lblPointValue, gridBagConstraints);

        lblRoute.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRoute.setText(org.openide.util.NbBundle.getMessage(StationEditor.class, "StationEditor.lblRoute.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        panEdit.add(lblRoute, gridBagConstraints);

        add(panEdit, "edit");

        panAdd.setOpaque(false);
        panAdd.setLayout(new java.awt.GridBagLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(StationEditor.class, "StationEditor.jLabel3.text")); // NOI18N
        panAdd.add(jLabel3, new java.awt.GridBagConstraints());

        add(panAdd, "add");

        panError.setOpaque(false);
        panError.setLayout(new java.awt.GridBagLayout());

        lblError.setText(org.openide.util.NbBundle.getMessage(StationEditor.class, "StationEditor.lblError.text")); // NOI18N
        panError.add(lblError, new java.awt.GridBagConstraints());

        add(panError, "error");
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void splitButtonActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_splitButtonActionPerformed
        splitPoint();
    }                                                                               //GEN-LAST:event_splitButtonActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void badGeomButtonActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_badGeomButtonActionPerformed
        switchBadGeomVisibility();
    }                                                                                 //GEN-LAST:event_badGeomButtonActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void badGeomCorrectButtonActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_badGeomCorrectButtonActionPerformed
        correctBadGeomCorrect();
    }                                                                                        //GEN-LAST:event_badGeomCorrectButtonActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void switchBadGeomVisibility() {
        if (isEditable()) {
            final Feature badGeomFeature = getBadGeomFeature();
            final Feature pointFeature = getFeature();

            final boolean selected = getBadGeomButton().isSelected();

            if (selected) {
                boundingbox = (XBoundingBox)MAPPING_COMPONENT.getCurrentBoundingBox();

                MAPPING_COMPONENT.getFeatureCollection().addFeature(badGeomFeature);
                MAPPING_COMPONENT.getFeatureCollection().select(pointFeature);

                zoomToBadFeature();
            } else {
                MAPPING_COMPONENT.getFeatureCollection().removeFeature(badGeomFeature);
                MAPPING_COMPONENT.gotoBoundingBoxWithoutHistory(boundingbox);
            }

            getBadGeomCorrectButton().setVisible(selected);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void correctBadGeomCorrect() {
        if (isEditable()) {
            final LinearReferencedPointFeature feature = getFeature();
            final Feature badFeature = getBadGeomFeature();
            feature.moveTo(badFeature.getGeometry().getCoordinate());
            zoomToBadFeature();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void zoomToBadFeature() {
        final Feature badGeomFeature = getBadGeomFeature();
        final Collection<Feature> aFeatureCollection = new ArrayList<Feature>();
        aFeatureCollection.add(badGeomFeature);
        aFeatureCollection.add(getFeature());
        // TODO boundingbox
        MAPPING_COMPONENT.zoomToAFeatureCollection(aFeatureCollection, false, MAPPING_COMPONENT.isFixedMapScale());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Feature> getZoomFeatures() {
        final Collection<Feature> zoomFeatures = new ArrayList<Feature>();
        addZoomFeaturesToCollection(zoomFeatures);
        return zoomFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  collection  DOCUMENT ME!
     */
    public void addZoomFeaturesToCollection(final Collection<Feature> collection) {
        final Feature pointFeature = getFeature();
        if (pointFeature != null) {
            final Feature boundedFeature = new PureNewFeature(pointFeature.getGeometry().buffer(500));
            collection.add(boundedFeature);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getPointInCurrentBB(final CidsBean routeBean) {
        // Geometrie für BoundingBox erzeugen
        final XBoundingBox boundingBox = (XBoundingBox)MAPPING_COMPONENT.getCurrentBoundingBox();
        final Collection<Coordinate> coordinates = new ArrayList<Coordinate>();
        coordinates.add(new Coordinate(boundingBox.getX1(), boundingBox.getY1()));
        coordinates.add(new Coordinate(boundingBox.getX2(), boundingBox.getY1()));
        coordinates.add(new Coordinate(boundingBox.getX2(), boundingBox.getY2()));
        coordinates.add(new Coordinate(boundingBox.getX1(), boundingBox.getY2()));
        coordinates.add(new Coordinate(boundingBox.getX1(), boundingBox.getY1()));
        final GeometryFactory gf = new GeometryFactory();
        final LinearRing shell = gf.createLinearRing(coordinates.toArray(new Coordinate[coordinates.size()]));
        final Polygon boundingBoxGeom = gf.createPolygon(shell, new LinearRing[0]);

        // Testen, ob Punkt 0 in der BoundingBox liegt
        final Geometry routeGeom = (Geometry)routeBean.getProperty(PROP_ROUTE_GEOM + "." + PROP_GEOM_GEOFIELD);
        final Geometry pointZero = LinearReferencedPointFeature.getPointOnLine(0d, routeGeom);

        if (pointZero.within(boundingBoxGeom)) {
            return 0d;
        } else {
            // niedrigster Stationierungswert, der die Boundingbox schneidet, bestimmen
            final LineString boundingBoxLineGeom = gf.createLineString(coordinates.toArray(
                        new Coordinate[coordinates.size()]));
            double bestPosition = 0;

            // Coordinaten durchlaufen und anhand der Position auf der Linie sortieren
            final Geometry intersectionGeom = routeGeom.intersection(boundingBoxLineGeom);
            for (final Coordinate coord : intersectionGeom.getCoordinates()) {
                final double position = LinearReferencedPointFeature.getPositionOnLine(coord, routeGeom);
                if (bestPosition == 0) {
                    bestPosition = position;
                } else if (position < bestPosition) {
                    bestPosition = position;
                }
            }

            return bestPosition;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  collection  DOCUMENT ME!
     */
    private static void zoomToFeatureCollection(final Collection<Feature> collection) {
        if (!MAPPING_COMPONENT.isFixedMapExtent()) {
            if (!collection.isEmpty()) {
                MAPPING_COMPONENT.zoomToAFeatureCollection(
                    collection,
                    true,
                    MAPPING_COMPONENT.isFixedMapScale());
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class AddPanel extends JPanel implements CidsBeanDropListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void beansDropped(final ArrayList<CidsBean> beans) {
            if (isEditable()) {
                CidsBean routeBean = null;
                for (final CidsBean bean : beans) {
                    if (bean.getMetaObject().getMetaClass().getName().equals(CN_ROUTE)) {
                        if ((getDropBehavior() == null) || getDropBehavior().checkForAdding(routeBean)) {
                            routeBean = bean;
                            setChangedSinceDrop(false);
                        }
                        double value = 0d;

                        if (isFirstStationInCurrentBB()) {
                            value = getPointInCurrentBB(routeBean);
                        }
                        setCidsBean(FEATURE_REGISTRY.getInstance().getLinearReferencingSolver()
                                    .createStationBeanFromRouteBean(routeBean, value));
                        if (isAutoZoomActivated) {
                            zoomToFeatureCollection(getZoomFeatures());
                        }
                        return;
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no route found in dropped objects");
                }
            }
        }
    }
}
