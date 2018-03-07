/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.linearreferencing;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import org.openide.util.NbBundle;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.DecimalFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.*;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.CidsBeanStore;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.editors.EditorClosedEvent;
import de.cismet.cids.editors.EditorSaveListener;

import de.cismet.cids.navigator.utils.CidsBeanDropListener;
import de.cismet.cids.navigator.utils.CidsBeanDropListenerComponent;
import de.cismet.cids.navigator.utils.CidsBeanDropTarget;
import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableTransferHandler;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedLineFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.CrsChangeListener;
import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class LinearReferencedLineEditor extends JPanel implements DisposableCidsBeanStore,
    LinearReferencingConstants,
    CidsBeanDropListener,
    EditorSaveListener,
    LinearReferencingSingletonInstances,
    PointBeanMergeRequestListener,
    WindowListener,
    ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static Icon ICON_MERGED_WITH_FROM_POINT = new javax.swing.ImageIcon(LinearReferencedLineEditor.class
                    .getResource("/de/cismet/cismap/linearreferencing/sql-join-left.png"));
    private static Icon ICON_MERGED_WITH_TO_POINT = new javax.swing.ImageIcon(LinearReferencedLineEditor.class
                    .getResource("/de/cismet/cismap/linearreferencing/sql-join-right.png"));

    static DataFlavor CIDSBEAN_DATAFLAVOR = DataFlavor.stringFlavor;
    private static DecimalFormat decimalFormat = new DecimalFormat("###.00");

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

    private LinearReferencingHelper linearReferencingHelper = FeatureRegistry.getInstance()
                .getLinearReferencingSolver();
    private LinearReferencedLineFeature feature;
    private LinearReferencedPointFeature fromPointFeature;
    private LinearReferencedPointFeature toPointFeature;
    private boolean isFromSpinnerChangeLocked = false;
    private boolean isFromFeatureChangeLocked = false;
    private boolean isFromBeanChangeLocked = false;
    private boolean isToSpinnerChangeLocked = false;
    private boolean isToFeatureChangeLocked = false;
    private boolean isToBeanChangeLocked = false;
    private PropertyChangeListener fromPointBeanChangeListener;
    private PropertyChangeListener toPointBeanChangeListener;
    private PointBeanMergeListener fromPointBeanMergeListener;
    private PointBeanMergeListener toPointBeanMergeListener;
    private LinearReferencedPointFeatureListener fromFeatureListener;
    private LinearReferencedPointFeatureListener toFeatureListener;
    private FeatureRegistryListener fromPointToFeatureRegistryListener;
    private FeatureRegistryListener toPointToFeatureRegistryListener;
    private LineEditorDropBehavior dropBehavior;
    private CrsChangeListener crsChangeListener;
    private Feature fromBadGeomFeature;
    private Feature toBadGeomFeature;
    private String lineField;
    private CidsBean cidsBean;
    private XBoundingBox boundingbox;
    private boolean isAutoZoomActivated = true;
    private boolean inited = false;
    private boolean changedSinceDrop = false;
    private boolean isEditable;
    private boolean isDrawingFeatureEnabled;
    private LinearReferencedLineEditor mergeParentLineEditor;
    private double backupFromPointValue = 0d;
    private double backupToPointValue = 0d;
    private String otherLinesFromQueryPart;
    private String otherLinesWhereQueryPart;
    private boolean isOtherLinesEnabled = true;
    private List<CidsBean> otherLines;

    private Collection<LinearReferencedLineEditorListener> listeners =
        new ArrayList<>();

    private boolean showOtherInDialog = false;
    private LinearReferencedLineEditor externalOthersEditor;
    private LinearReferencedLineEditor parent = null;
    private LinePropertyChangeListener linePropertyChangeListener = new LinePropertyChangeListener();
    private boolean routeCombo = false;
    private boolean allowDoubleValues = true;
    private boolean routesComboInitialised = false;
    private String routeMetaClassName;
    private CidsBeanStore cidsBeanStore;

    private final ConnectionContext connectionContext;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnFromBadGeom;
    private javax.swing.JButton btnFromBadGeomCorrect;
    private javax.swing.JButton btnFromPointSplit;
    private javax.swing.JToggleButton btnRoute;
    private javax.swing.JToggleButton btnToBadGeom;
    private javax.swing.JButton btnToBadGeomCorrect;
    private javax.swing.JButton btnToPointSplit;
    private javax.swing.JButton butApply;
    private javax.swing.JButton butCancel;
    private javax.swing.JComboBox cbPossibleRoute;
    private javax.swing.JDialog externalGeomDialog;
    private javax.swing.JPanel geomDialogInternalPanel;
    private javax.swing.JScrollPane geomDialogScrollPane;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblFromIcon;
    private javax.swing.JLabel lblFromValue;
    private javax.swing.JLabel lblFrontPointSplit;
    private javax.swing.JLabel lblRoute;
    private javax.swing.JLabel lblToIcon;
    private javax.swing.JLabel lblToPointSplit;
    private javax.swing.JLabel lblToValue;
    private javax.swing.JPanel panAdd;
    private javax.swing.JPanel panAddFromFeature;
    private javax.swing.JPanel panEdit;
    private javax.swing.JPanel panError;
    private javax.swing.JPanel panFromBadGeomSpacer;
    private javax.swing.JPanel panLine;
    private javax.swing.JPanel panLinePoints;
    private javax.swing.JPanel panOtherLines;
    private javax.swing.JPanel panSpacer;
    private javax.swing.JPanel panToBadGeomSpacer;
    private javax.swing.JSpinner spnFrom;
    private javax.swing.JSpinner spnTo;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinearReferencedLineEditor object.
     *
     * @param  routeMetaClassName  DOCUMENT ME!
     */
    @Deprecated
    public LinearReferencedLineEditor(final String routeMetaClassName) {
        this(routeMetaClassName, ConnectionContext.createDeprecated());
    }

    /**
     * Creates a new LinearReferencedLineEditor object.
     *
     * @param  routeMetaClassName  DOCUMENT ME!
     * @param  connectionContext   DOCUMENT ME!
     */
    public LinearReferencedLineEditor(final String routeMetaClassName,
            final ConnectionContext connectionContext) {
        this(true, routeMetaClassName, connectionContext);
    }

    /**
     * Creates a new LinearReferencedLineEditor object.
     *
     * @param  isEditable          DOCUMENT ME!
     * @param  routeMetaClassName  DOCUMENT ME!
     */
    @Deprecated
    public LinearReferencedLineEditor(final boolean isEditable, final String routeMetaClassName) {
        this(isEditable, routeMetaClassName, ConnectionContext.createDeprecated());
    }

    /**
     * Creates a new LinearReferencedLineEditor object.
     *
     * @param  isEditable          DOCUMENT ME!
     * @param  routeMetaClassName  DOCUMENT ME!
     * @param  connectionContext   DOCUMENT ME!
     */
    public LinearReferencedLineEditor(final boolean isEditable,
            final String routeMetaClassName,
            final ConnectionContext connectionContext) {
        this(isEditable, isEditable, false, routeMetaClassName);
    }

    /**
     * Creates a new LinearReferencedLineEditor object.
     *
     * @param  isEditable                DOCUMENT ME!
     * @param  isDrawingFeaturesEnabled  DOCUMENT ME!
     * @param  routeCombo                DOCUMENT ME!
     * @param  routeMetaClassName        DOCUMENT ME!
     */
    @Deprecated
    public LinearReferencedLineEditor(final boolean isEditable,
            final boolean isDrawingFeaturesEnabled,
            final boolean routeCombo,
            final String routeMetaClassName) {
        this(isEditable,
            isDrawingFeaturesEnabled,
            routeCombo,
            routeMetaClassName,
            ConnectionContext.createDeprecated());
    }

    /**
     * Creates new form LinearReferencedLineEditor.
     *
     * @param  isEditable                DOCUMENT ME!
     * @param  isDrawingFeaturesEnabled  DOCUMENT ME!
     * @param  routeCombo                DOCUMENT ME!
     * @param  routeMetaClassName        DOCUMENT ME!
     * @param  connectionContext         DOCUMENT ME!
     */
    public LinearReferencedLineEditor(final boolean isEditable,
            final boolean isDrawingFeaturesEnabled,
            final boolean routeCombo,
            final String routeMetaClassName,
            final ConnectionContext connectionContext) {
        this.routeCombo = routeCombo;
        this.routeMetaClassName = routeMetaClassName;
        this.connectionContext = connectionContext;

        initComponents();
        final String routeNamePropertyName = linearReferencingHelper.getRouteNamePropertyFromRouteByClassName(
                routeMetaClassName);
        AutoCompleteDecorator.decorate(cbPossibleRoute, new ObjectToStringConverter() {

                @Override
                public String getPreferredStringForItem(final Object o) {
                    if (o instanceof CidsLayerFeature) {
                        final Object prop = ((CidsLayerFeature)o).getProperty(routeNamePropertyName);

                        if (prop == null) {
                            return "";
                        } else {
                            return String.valueOf(prop);
                        }
                    } else {
                        if (o == null) {
                            return "";
                        } else {
                            return o.toString();
                        }
                    }
                }
            });
        cbPossibleRoute.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    Object newValue = value;

                    if (value instanceof CidsLayerFeature) {
                        final Object prop = ((CidsLayerFeature)value).getProperty(routeNamePropertyName);

                        if (prop != null) {
                            newValue = String.valueOf(prop);
                        }
                    } else {
                        if (value != null) {
                            newValue = value.toString();
                        } else {
                            newValue = " ";
                        }
                    }

                    return super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus);
                }
            });

        setEditable(isEditable);
        setDrawingFeaturesEnabled(isDrawingFeaturesEnabled);

        try {
            if (isEditable) {
                new LineEditorDropTarget(this);
            }
            new DropTarget(lblFromIcon, new PointBeanDropTargetListener(FROM));
            new DropTarget(lblToIcon, new PointBeanDropTargetListener(TO));
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error while creating DropTargets", ex);
            }
        }
        initSpinnerListener(FROM);
        initSpinnerListener(TO);
        initFeatureRegistryListener(FROM);
        initFeatureRegistryListener(TO);
        initPointFeatureListener(FROM);
        initPointFeatureListener(TO);
        initPointBeanChangeListener(FROM);
        initPointBeanChangeListener(TO);
        initPointBeanMergeListener(FROM);
        initPointBeanMergeListener(TO);
        initPointTransferHandler(FROM);
        initPointTransferHandler(TO);
        initPointIconLabelMouseListener(FROM);
        initPointIconLabelMouseListener(TO);

        if (isDrawingFeaturesEnabled()) {
            initCrsChangeListener();

            CismapBroker.getInstance().addCrsChangeListener(getCrsChangeListener());
        }

        // allows the scrolling of the external geometry box during a drag operation
        new DropTarget(externalGeomDialog, new DropTargetAdapter() {

                @Override
                public void dragOver(final DropTargetDragEvent dtde) {
                    if (dtde.getLocation().getY() < 30) {
                        final Point p = geomDialogScrollPane.getViewport().getViewPosition();
                        int y = (int)(p.getY() - 5.0);
                        if (y < 0) {
                            y = 0;
                        }
                        final Point newP = new Point((int)p.getX(), y);

                        geomDialogScrollPane.getViewport().setViewPosition(newP);
                    } else if (dtde.getLocation().getY() > (externalGeomDialog.getHeight() - 20)) {
                        final Point p = geomDialogScrollPane.getViewport().getViewPosition();
                        final Point newP = new Point((int)p.getX(), (int)(p.getY() + 5.0));

                        if (newP.getY() < (geomDialogInternalPanel.getHeight() - geomDialogScrollPane.getHeight())) {
                            geomDialogScrollPane.getViewport().setViewPosition(newP);
                        }
                    }
                }

                @Override
                public void drop(final DropTargetDropEvent dtde) {
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public final ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  routeCombo  DOCUMENT ME!
     */
    public void setRouteCombo(final boolean routeCombo) {
        this.routeCombo = routeCombo;
    }

    @Override
    public void pointBeanMergeRequest(final boolean fromPoint, final CidsBean withPointBean) {
        mergeRequest(fromPoint, withPointBean);
    }

    @Override
    public void pointBeanSplitRequest(final boolean fromPoint) {
        splitPoint(fromPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the otherLines
     */
    public List<CidsBean> getOtherLines() {
        return otherLines;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  otherLines  the otherLines to set
     */
    public void setOtherLines(final List<CidsBean> otherLines) {
        this.otherLines = otherLines;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isOtherLinesEnabled  DOCUMENT ME!
     */
    public void setOtherLinesEnabled(final boolean isOtherLinesEnabled) {
        this.isOtherLinesEnabled = isOtherLinesEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isOtherLinesEnabled() {
        return isOtherLinesEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isEditable  DOCUMENT ME!
     */
    protected final void setEditable(final boolean isEditable) {
        this.isEditable = isEditable;
        spnFrom.setVisible(isEditable);
        spnTo.setVisible(isEditable);
        lblFromValue.setVisible(!isEditable);
        lblToValue.setVisible(!isEditable);
        btnRoute.setVisible(isEditable);
        lblRoute.setVisible(!isEditable);
        if (isInited()) {
            refresh();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isEditable() {
        return isEditable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isDrawingFeatureEnabled  DOCUMENT ME!
     */
    protected final void setDrawingFeaturesEnabled(final boolean isDrawingFeatureEnabled) {
        this.isDrawingFeatureEnabled = isDrawingFeatureEnabled;
        if (isInited()) {
            refresh();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isDrawingFeaturesEnabled() {
        return isDrawingFeatureEnabled;
    }

    /**
     * DOCUMENT ME!
     */
    private void fireLineAdded() {
        for (final LinearReferencedLineEditorListener listener : listeners) {
            listener.linearReferencedLineCreated();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    private void fireOtherLinesPanelVisibilityChange(final boolean visible) {
        for (final LinearReferencedLineEditorListener listener : listeners) {
            listener.otherLinesPanelVisibilityChange(visible);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final LinearReferencedLineEditorListener listener) {
        return listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final LinearReferencedLineEditorListener listener) {
        return listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedLineFeature getLineFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void setFeature(final LinearReferencedLineFeature feature) {
        this.feature = feature;

        if (feature != null) {
            final LinearReferencedPointFeature fromPointFeature = feature.getPointFeature(FROM);
            final LinearReferencedPointFeature toPointFeature = feature.getPointFeature(TO);

            // feature editable status setzen, außer es ist schon editable
            if ((fromPointFeature != null) && !fromPointFeature.isEditable()) {
                fromPointFeature.setEditable(isEditable());
            }
            if ((toPointFeature != null) && !toPointFeature.isEditable()) {
                toPointFeature.setEditable(isEditable());
            }
        }
    }

    @Override
    public CidsBean getCidsBean() {
        return cidsBean;
    }

    /**
     * >> BEAN.
     */
    private void cleanupLine() {
        final LinearReferencedLineFeature oldFeature = getLineFeature();
        if (oldFeature != null) {
            final CidsBean oldBean = FEATURE_REGISTRY.getCidsBean(oldFeature);
            FEATURE_REGISTRY.removeLinearReferencedLineFeature(oldBean);
            setFeature(null);
        }

        cleanupPoint(FROM);
        cleanupPoint(TO);
        cleanOtherLinesPanel();

        showCard(Card.add);
        setInited(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void main(final String[] args) throws Exception {
//        new WrrlEditorTester("station_linie", LinearReferencedLineEditor.class, WRRLUtil.DOMAIN_NAME).run();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void cleanupPoint(final boolean isFrom) {
        final CidsBean pointBean = getPointBean(isFrom);

        if (pointBean != null) {
            pointBean.removePropertyChangeListener(getPointBeanChangeListener(isFrom));
            MERGE_REGISTRY.removeListener(pointBean, getPointBeanMergeListener(isFrom));
        }

        final LinearReferencedPointFeature pointFeature = getPointFeature(isFrom);
        if (pointFeature != null) {
            pointFeature.removeListener(getPointFeatureListener(isFrom));
            if (pointBean != null) {
                FEATURE_REGISTRY.removeStationFeature(pointBean);
            }
            FEATURE_REGISTRY.removeListener(pointBean, getFeatureRegistryListener(isFrom));

            // bean ist null, feature also auch
            setPointFeature(null, isFrom);
        }

        final Feature badGeomFeature = getBadGeomFeature(isFrom);
        if (badGeomFeature != null) {
            MAPPING_COMPONENT.getFeatureCollection().removeFeature(badGeomFeature);
            setBadGeomFeature(null, isFrom);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  color  DOCUMENT ME!
     */
    protected void setLineColor(final Color color) {
        panLine.setBackground(color);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Color getLineColor() {
        return panLine.getBackground();
    }

    /**
     * DOCUMENT ME!
     */
    private void initLine() {
        // wird das aktuelle crs unterstützt ?
        if (!isCrsSupported(CismapBroker.getInstance().getSrs())) {
            showCrsNotSupported();
            // noch nicht initialisiert ?
        } else if (!isInited()) {
            final CidsBean lineBean = getLineBean();
            if (lineBean != null) {
                initPoint(FROM);
                initPoint(TO);

                if (isDrawingFeaturesEnabled() && (linearReferencingHelper.getGeomBeanFromLineBean(lineBean) != null)) {
                    // feature erzeugen
                    final LinearReferencedLineFeature lineFeature = FEATURE_REGISTRY.addLinearReferencedLineFeature(
                            lineBean,
                            getPointFeature(FROM),
                            getPointFeature(TO));

                    lineFeature.setLinePaint(getLineColor());

                    setFeature(lineFeature);
                }
                final String routeText = "Route: "
                            + linearReferencingHelper.getRouteNameFromStationBean(getPointBean(FROM));

                fireLineAdded();

                pointBeanValueChanged(FROM);
                pointBeanValueChanged(TO);

                lblRoute.setText(routeText);
                if (isEditable()) {
                    initSpinner(FROM);
                    initSpinner(TO);
                    btnRoute.setText(routeText);
                }

                if (linearReferencingHelper.getGeomBeanFromLineBean(lineBean) != null) {
                    showCard(Card.edit);

                    updateOtherLinesPanelVisibility();
                }
                setInited(true);
            } else {
                if (isEditable()) {
                    showCard(Card.add);
                } else {
                    setErrorMsg("keine Stationierung zugewiesen.");
                    showCard(Card.error);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initSpinner(final boolean isFrom) {
        if (getRouteGeometry() != null) {
            ((SpinnerNumberModel)getPointSpinner(isFrom).getModel()).setMaximum(Math.ceil(
                    getRouteGeometry().getLength()));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void backupPointValue(final boolean isFrom) {
        setBackupPointValue(getPointValue(isFrom), isFrom);
    }

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        // aufräumen falls vorher cidsbean schon gesetzt war
        cleanupLine();

        this.cidsBean = cidsBean;

        if (cidsBean != null) {
            MERGE_REGISTRY.addRequestListener(cidsBean, this);
//            CidsBean station = linearReferencingHelper.getStationBeanFromLineBean(cidsBean, true);
//            cbRoute.setMetaClass(linearReferencingHelper.getRouteBeanFromStationBean(station).getMetaObject().getMetaClass());
        }

        // cache für beans setzen
        final CidsBean cachedLineBean = CIDSBEAN_CACHE.getCachedBeanFor(getLineBean());
        CidsBean cachedFromPointBean = CIDSBEAN_CACHE.getCachedBeanFor(getPointBean(FROM));
        CidsBean cachedToPointBean = CIDSBEAN_CACHE.getCachedBeanFor(getPointBean(TO));
        final CidsBean lineBean = getLineBean();

        if (lineBean != null) {
            lineBean.addPropertyChangeListener(linePropertyChangeListener);
        }

        // beans mit denen aus dem Cache erstzen
        // (nur wenn nicht editable, sonst werden die änderungen an der bean unter umständen nicht gespeichert)
        if (!isEditable()) {
            setLineBean(cachedLineBean);
            cachedFromPointBean = CIDSBEAN_CACHE.getCachedBeanFor(getPointBean(FROM));
            cachedToPointBean = CIDSBEAN_CACHE.getCachedBeanFor(getPointBean(TO));
            setPointBean(cachedFromPointBean, FROM);
            setPointBean(cachedToPointBean, TO);
        }

        // position der punkte sichern (für reset)
        backupPointValue(FROM);
        backupPointValue(TO);

        // andere linien auf selber route ermitteln
        if (isEditable() && isOtherLinesEnabled()) {
            updateOtherLinesOnBaseline();
        }
        btnRoute.setVisible(panOtherLines.getComponents().length != 0);
        lblRoute.setVisible(panOtherLines.getComponents().length == 0);

        if (getLineBean() != null) {
            // Farbe setzen (wird neu ermittelt, falls nicht schon eine feature existiert)
            setLineColor(FeatureRegistry.getNextColor(getLineBean()));
        }

        // neu initialisieren
        initLine();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsBeanStore  DOCUMENT ME!
     * @param  line           DOCUMENT ME!
     */
    public void setCidsBeanStore(final CidsBeanStore cidsBeanStore, final CidsBean line) {
        this.cidsBeanStore = cidsBeanStore;

        if (line != null) {
            setCidsBean(line);
        } else {
            setCidsBean(null);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refresh() {
        cleanupLine();
        initLine();
        if (isOtherLinesEnabled && isInited() && isEditable) {
            updateOtherLinesOnBaseline();
        }
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
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JButton getPointSplitButton(final boolean isFrom) {
        return (isFrom) ? btnFromPointSplit : btnToPointSplit;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JLabel getPointSplitLabel(final boolean isFrom) {
        return (isFrom) ? lblFrontPointSplit : lblToPointSplit;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String getPointField(final boolean isFrom) {
        return (isFrom) ? PROP_STATIONLINIE_FROM : PROP_STATIONLINIE_TO;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lineField  DOCUMENT ME!
     */
    public void setLineField(final String lineField) {
        this.lineField = lineField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getLineField() {
        return lineField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasBadGeomFeature() {
        return (getBadGeomFeature(FROM) != null) && (getBadGeomFeature(TO) != null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature getBadGeomFeature(final boolean isFrom) {
        return (isFrom) ? fromBadGeomFeature : toBadGeomFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  badGeomFeature  DOCUMENT ME!
     * @param  isFrom          DOCUMENT ME!
     */
    private void setBadGeomFeature(final Feature badGeomFeature, final boolean isFrom) {
        if (isFrom) {
            fromBadGeomFeature = badGeomFeature;
        } else {
            toBadGeomFeature = badGeomFeature;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  otherLinesFromQueryPart   DOCUMENT ME!
     * @param  otherLinesWhereQueryPart  DOCUMENT ME!
     */
    public void setOtherLinesQueryAddition(final String otherLinesFromQueryPart,
            final String otherLinesWhereQueryPart) {
        this.otherLinesFromQueryPart = otherLinesFromQueryPart;
        this.otherLinesWhereQueryPart = otherLinesWhereQueryPart;
    }

    /**
     * DOCUMENT ME!
     */
    private void updateOtherLinesOnBaseline() {
        if ((getLineBean() != null) && (getLineBean().getProperty("von.route.id") != null)) {
            if (otherLines == null) {
                final int route_id = (Integer)getLineBean().getProperty("von.route.id");
                final int id = (Integer)getLineBean().getProperty("id");
                final MetaClass mcStationLine = cidsBean.getMetaObject().getMetaClass();
                final MetaClass mcStation = linearReferencingHelper.getStationBeanFromLineBean(cidsBean, true)
                            .getMetaObject()
                            .getMetaClass();

                final String queryOtherLines = "SELECT "
                            + "   " + mcStationLine.getID() + ", "
                            + "   station_linie." + mcStationLine.getPrimaryKey() + " "
                            + "FROM "
                            + "   " + mcStationLine.getTableName() + " AS station_linie, "
                            + "   " + mcStation.getTableName() + " AS station "
                            + ((otherLinesFromQueryPart != null) ? (", " + otherLinesFromQueryPart + " ") : "")
                            + "WHERE "
                            + "   station.id = station_linie.von "
                            + "   AND station.route = " + route_id + " "
                            + "   AND station_linie.id != " + id + " "
                            + ((otherLinesWhereQueryPart != null)
                                ? (" AND " + otherLinesWhereQueryPart + " station_linie.id") : "")
                            + ";";
                if (LOG.isDebugEnabled()) {
                    LOG.debug(queryOtherLines);
                }
                MetaObject[] mosOtherLines = null;
                try {
                    mosOtherLines = SessionManager.getProxy()
                                .getMetaObjectByQuery(queryOtherLines, 0, getConnectionContext());
                } catch (Exception ex) {
                    LOG.error("error while loading other lines on baseline", ex);
                }

                cleanOtherLinesPanel();
                for (final MetaObject moOtherLine : mosOtherLines) {
                    final CidsBean otherLineBean = moOtherLine.getBean();
                    final LinearReferencedLineRenderer renderer = new LinearReferencedLineRenderer(routeMetaClassName);
                    renderer.setMergeParentLineEditor(this);
                    renderer.setCidsBean(otherLineBean);
                    renderer.updateSplitMergeControls(FROM);
                    renderer.updateSplitMergeControls(TO);
                    panOtherLines.add(renderer);
                }
            } else {
                cleanOtherLinesPanel();
                Collections.sort(otherLines, new OtherLinesComparator());
                for (final CidsBean otherLineBean : otherLines) {
                    final LinearReferencedLineRenderer renderer = new LinearReferencedLineRenderer(routeMetaClassName);
                    renderer.setMergeParentLineEditor(this);
                    renderer.setCidsBean(otherLineBean);
                    renderer.updateSplitMergeControls(FROM);
                    renderer.updateSplitMergeControls(TO);
                    panOtherLines.add(renderer);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JButton getBadGeomCorrectButton(final boolean isFrom) {
        return (isFrom) ? btnFromBadGeomCorrect : btnToBadGeomCorrect;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JToggleButton getBadGeomButton(final boolean isFrom) {
        return (isFrom) ? btnFromBadGeom : btnToBadGeom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JLabel getPointIconLabel(final boolean isFrom) {
        return (isFrom) ? lblFromIcon : lblToIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureRegistryListener getFeatureRegistryListener(final boolean isFrom) {
        return (isFrom) ? fromPointToFeatureRegistryListener : toPointToFeatureRegistryListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     * @param  isFrom    DOCUMENT ME!
     */
    private void setFeatureRegistryListener(final FeatureRegistryListener listener, final boolean isFrom) {
        if (isFrom) {
            fromPointToFeatureRegistryListener = listener;
        } else {
            toPointToFeatureRegistryListener = listener;
        }
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
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PropertyChangeListener getPointBeanChangeListener(final boolean isFrom) {
        return (isFrom) ? fromPointBeanChangeListener : toPointBeanChangeListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     * @param  isFrom    DOCUMENT ME!
     */
    private void setPointBeanChangeListener(final PropertyChangeListener listener, final boolean isFrom) {
        if (isFrom) {
            fromPointBeanChangeListener = listener;
        } else {
            toPointBeanChangeListener = listener;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PointBeanMergeListener getPointBeanMergeListener(final boolean isFrom) {
        return (isFrom) ? fromPointBeanMergeListener : toPointBeanMergeListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     * @param  isFrom    DOCUMENT ME!
     */
    private void setPointBeanMergeListener(final PointBeanMergeListener listener, final boolean isFrom) {
        if (isFrom) {
            fromPointBeanMergeListener = listener;
        } else {
            toPointBeanMergeListener = listener;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected LinearReferencedPointFeature getPointFeature(final boolean isFrom) {
        return (isFrom) ? fromPointFeature : toPointFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointFeature  DOCUMENT ME!
     * @param  isFrom        DOCUMENT ME!
     */
    private void setPointFeature(final LinearReferencedPointFeature pointFeature,
            final boolean isFrom) {
        // feature editable status setzen, außer es ist schon editable
        if ((pointFeature != null) && !pointFeature.isEditable()) {
            pointFeature.setEditable(isEditable());
        }

        if (isFrom) {
            fromPointFeature = pointFeature;
        } else {
            toPointFeature = pointFeature;
        }

        final LinearReferencedLineFeature lineFeature = getLineFeature();
        if (lineFeature != null) {
            getLineFeature().setPointFeature(pointFeature, isFrom);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LinearReferencedPointFeatureListener getPointFeatureListener(final boolean isFrom) {
        return (isFrom) ? fromFeatureListener : toFeatureListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureListener  DOCUMENT ME!
     * @param  isFrom           DOCUMENT ME!
     */
    private void setPointFeatureListener(final LinearReferencedPointFeatureListener featureListener,
            final boolean isFrom) {
        if (isFrom) {
            fromFeatureListener = featureListener;
        } else {
            toFeatureListener = featureListener;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JSpinner getPointSpinner(final boolean isFrom) {
        return (isFrom) ? spnFrom : spnTo;
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
     * @param  value   DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void setBackupPointValue(final double value, final boolean isFrom) {
        if (isFrom) {
            backupFromPointValue = value;
        } else {
            backupToPointValue = value;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getBackupPointValue(final boolean isFrom) {
        return (isFrom) ? backupFromPointValue : backupToPointValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    protected void resetPointValue(final boolean isFrom) {
        setPointValueToBean(getBackupPointValue(isFrom), isFrom);
        pointBeanValueChanged(isFrom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initPoint(final boolean isFrom) {
        final CidsBean pointBean = getPointBean(isFrom);
        if (pointBean != null) {
            pointBean.addPropertyChangeListener(getPointBeanChangeListener(isFrom));
            MERGE_REGISTRY.addListener(pointBean, getPointBeanMergeListener(isFrom));

            if (isEditable()) {
                final double distance = linearReferencingHelper.distanceOfStationGeomToRouteGeomFromStationBean(
                        pointBean);

                if (distance > 1) {
                    setBadGeomFeature(StationEditor.createBadGeomFeature(
                            linearReferencingHelper.getPointGeometryFromStationBean(pointBean)),
                        isFrom);
                } else {
                    setBadGeomFeature(null, isFrom);
                }
            }

            if (isDrawingFeaturesEnabled()
                        && (linearReferencingHelper.getPointGeometryFromStationBean(pointBean) != null)) {
                FEATURE_REGISTRY.addListener(pointBean, getFeatureRegistryListener(isFrom));

                // feature erzeugen
                final LinearReferencedPointFeature pointFeature = FEATURE_REGISTRY.addStationFeature(
                        pointBean);

                // feature listener
                pointFeature.addListener(getPointFeatureListener(isFrom));

                // feature setzen
                setPointFeature(pointFeature, isFrom);
            }
        }
        updateBadGeomButton(isFrom);
        updateSplitMergeControls(isFrom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointBean  DOCUMENT ME!
     * @param  isFrom     DOCUMENT ME!
     */
    private void setPoint(final CidsBean pointBean, final boolean isFrom) {
        cleanupPoint(isFrom);
        setPointBean(pointBean, isFrom);
        initPoint(isFrom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void switchBadGeomVisibility(final boolean isFrom) {
        if (isEditable()) {
            final Feature badGeomFeature = getBadGeomFeature(isFrom);
            final Feature pointFeature = getPointFeature(isFrom);

            final boolean selected = getBadGeomButton(isFrom).isSelected();

            if (selected) {
                boundingbox = (XBoundingBox)MAPPING_COMPONENT.getCurrentBoundingBox();

                MAPPING_COMPONENT.getFeatureCollection().addFeature(badGeomFeature);
                MAPPING_COMPONENT.getFeatureCollection().select(pointFeature);

                zoomToBadFeature(isFrom);
            } else {
                MAPPING_COMPONENT.getFeatureCollection().removeFeature(badGeomFeature);
                MAPPING_COMPONENT.gotoBoundingBoxWithoutHistory(boundingbox);
            }

            getBadGeomCorrectButton(isFrom).setVisible(selected);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void zoomToBadFeature(final boolean isFrom) {
        final Feature badGeomFeature = getBadGeomFeature(isFrom);
        final Collection<Feature> aFeatureCollection = new ArrayList<Feature>();
        aFeatureCollection.add(badGeomFeature);
        aFeatureCollection.add(getLineFeature());
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
        final Feature fromPointFeature = getPointFeature(FROM);
        final Feature toPointFeature = getPointFeature(TO);
        if ((fromPointFeature != null) && (toPointFeature != null)) {
            final Feature boundedFromFeature = new PureNewFeature(fromPointFeature.getGeometry().buffer(500));
            final Feature boundedToFeature = new PureNewFeature(toPointFeature.getGeometry().buffer(500));
            collection.add(boundedFromFeature);
            collection.add(boundedToFeature);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void correctBadGeom(final boolean isFrom) {
        if (isEditable()) {
            final LinearReferencedPointFeature feature = getPointFeature(isFrom);
            final Feature badFeature = getBadGeomFeature(isFrom);
            feature.moveTo(badFeature.getGeometry().getCoordinate(), null);
            zoomToBadFeature(isFrom);
        }
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
                if (routeCombo) {
                    fillRoutesCombo();
                    ((CardLayout)getLayout()).show(this, "addFeature");
                } else {
                    ((CardLayout)getLayout()).show(this, "add");
                }
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
     *
     * @param  routeBean  DOCUMENT ME!
     * @param  cidsBean   DOCUMENT ME!
     * @param  lineField  DOCUMENT ME!
     */
    public static void fillFromRoute(final CidsBean routeBean, final CidsBean cidsBean, final String lineField) {
        try {
            final CidsBean linieBean = FeatureRegistry.getInstance()
                        .getLinearReferencingSolver()
                        .createLineBeanFromRouteBean(routeBean);
            cidsBean.setProperty(lineField, linieBean);
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while filling bean", ex);
            }
        }
    }

    @Override
    public void dispose() {
        cleanupLine();
        MERGE_REGISTRY.removeRequestListener(cidsBean, this);
//        setOtherLines(null);
        CismapBroker.getInstance().removeCrsChangeListener(getCrsChangeListener());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initFeatureRegistryListener(final boolean isFrom) {
        final FeatureRegistryListener featureRegistryListener = new FeatureRegistryListener() {

                @Override
                public void FeatureCountChanged() {
                    updateSplitMergeControls(isFrom);
                }
            };

        setFeatureRegistryListener(featureRegistryListener, isFrom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initSpinnerListener(final boolean isFrom) {
        final JSpinner spinner = getPointSpinner(isFrom);
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField()
                .getDocument()
                .addDocumentListener(new DocumentListener() {

                        @Override
                        public void insertUpdate(final DocumentEvent de) {
                            spinnerValueChanged(isFrom);
                        }

                        @Override
                        public void removeUpdate(final DocumentEvent de) {
                            spinnerValueChanged(isFrom);
                        }

                        @Override
                        public void changedUpdate(final DocumentEvent de) {
                            spinnerValueChanged(isFrom);
                        }
                    });
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(final FocusEvent fe) {
                    MAPPING_COMPONENT.getFeatureCollection().select(getPointFeature(isFrom));
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initPointFeatureListener(final boolean isFrom) {
        final LinearReferencedPointFeatureListener featureListener = new LinearReferencedPointFeatureListener() {

                @Override
                public void featureMoved(final LinearReferencedPointFeature pointFeature) {
                    featureValueChanged(isFrom);
                }

                @Override
                public void featureMerged(final LinearReferencedPointFeature mergePoint,
                        final LinearReferencedPointFeature withPoint) {
//                    final CidsBean withBean = FEATURE_REGISTRY.getCidsBean(withPoint);
//
//                    final LinearReferencedPointFeature fromFeature = getPointFeature(FROM);
//                    final LinearReferencedPointFeature toFeature = getPointFeature(TO);
//                    if ((fromFeature.equals(mergePoint) && !toFeature.equals(withPoint))
//                                || (toFeature.equals(mergePoint) && !fromFeature.equals(withPoint))) {
//                        final boolean isFrom = fromFeature.equals(mergePoint);
//
//                        MERGE_REGISTRY.firePointBeanMerged(getPointBean(isFrom), withBean);
//                    }
                }
            };

        setPointFeatureListener(featureListener, isFrom);
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
                        initLine();
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void showCrsNotSupported() {
        cleanupLine();
        setErrorMsg("Das aktuelle CRS wird vom Stationierungseditor nicht unterstützt.");
        showCard(Card.error);
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
     * cidsbean ändern.
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void featureValueChanged(final boolean isFrom) {
        if (isEditable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("feature changed", new CurrentStackTrace());
            }
            try {
                lockFeatureChange(true, isFrom);

                if (!isBeanChangeLocked(isFrom)) {
                    final LinearReferencedPointFeature linearRefFeature = getPointFeature(isFrom);

                    final double value = round(linearRefFeature.getCurrentPosition());
                    setPointValueToBean(value, isFrom);
                }
            } finally {
                lockFeatureChange(false, isFrom);
            }
        }
    }

    /**
     * cidsbean ändern.
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void spinnerValueChanged(final boolean isFrom) {
        if (isEditable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("spinner changed", new CurrentStackTrace());
            }
            try {
                lockSpinnerChange(true, isFrom);

                final AbstractFormatter formatter = ((JSpinner.DefaultEditor)getPointSpinner(isFrom).getEditor())
                            .getTextField().getFormatter();
                final String text = ((JSpinner.DefaultEditor)getPointSpinner(isFrom).getEditor()).getTextField()
                            .getText();
                if (!text.isEmpty()) {
                    try {
                        final double value = (Double)formatter.stringToValue(text);
                        setPointValueToBean(value, isFrom);
                    } catch (ParseException ex) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("error parsing spinner", ex);
                        }
                    }
                }
            } finally {
                lockSpinnerChange(false, isFrom);
            }
        }
    }
    /**
     * spinner ändern feature ändern realgeoms anpassen.
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void pointBeanValueChanged(final boolean isFrom) {
        try {
            lockBeanChange(true, isFrom);

            setChangedSinceDrop(true);

            final CidsBean pointBean = getPointBean(isFrom);

            if (pointBean != null) {
                final Double value = (Double)pointBean.getProperty(PROP_STATION_VALUE);

                if (value != null) {
                    if (isEditable()) {
                        setPointValueToSpinner(value, isFrom);
                    } else {
                        setPointValueToLabel(value, isFrom);
                    }

                    if (isDrawingFeaturesEnabled()) {
                        setPointValueToFeature(value, isFrom);
                    }

                    // realgeoms nur nach manueller eingabe updaten
                    if (isInited()) {
                        updateRealGeoms(isFrom);
                    }
                }
            }
        } finally {
            lockBeanChange(false, isFrom);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void updateRealGeoms() {
        updateRealGeoms(FROM);
        updateRealGeoms(TO);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JLabel getValueLabel(final boolean isFrom) {
        return (isFrom) ? lblFromValue : lblToValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value   DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void setPointValueToLabel(final double value, final boolean isFrom) {
        final JLabel valueLabel = getValueLabel(isFrom);
        valueLabel.setText(decimalFormat.format(round(value)));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getPointValue(final boolean isFrom) {
        final CidsBean pointBean = getPointBean(isFrom);
        if (pointBean != null) {
            return linearReferencingHelper.getLinearValueFromStationBean(pointBean);
        } else {
            return 0d;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    protected void updateSplitMergeControls(final boolean isFrom) {
        if (isEditable()) {
            final boolean isPointMerged = isPointMerged(isFrom);
            if (btnRoute.isSelected()) {
                getPointSplitButton(isFrom).setVisible(isPointMerged);
                getPointSplitLabel(isFrom).setVisible(false);
            } else {
                getPointSplitButton(isFrom).setVisible(false);
                getPointSplitLabel(isFrom).setVisible(isPointMerged);
                getPointSplitLabel(isFrom).setIcon(isFrom ? ICON_MERGED_WITH_FROM_POINT : ICON_MERGED_WITH_TO_POINT);
            }
        } else {
            getPointSplitButton(isFrom).setVisible(false);
            final LinearReferencedLineEditor editor = getMergeParentLineEditor();
            final JLabel lblPointSplit = getPointSplitLabel(isFrom);
            if (editor == null) {
                lblPointSplit.setVisible(false);
            } else if (getPointBean(isFrom).equals(editor.getPointBean(FROM))) {
                lblPointSplit.setIcon(ICON_MERGED_WITH_FROM_POINT);
                lblPointSplit.setVisible(true);
            } else if (getPointBean(isFrom).equals(editor.getPointBean(TO))) {
                lblPointSplit.setIcon(ICON_MERGED_WITH_TO_POINT);
                lblPointSplit.setVisible(true);
            } else {
                lblPointSplit.setVisible(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isPointMerged(final boolean isFrom) {
        if (getLineBean() != null) {
            final CidsBean pointBean = getPointBean(isFrom);
            for (final Component component : panOtherLines.getComponents()) {
                final LinearReferencedLineRenderer renderer = (LinearReferencedLineRenderer)component;
                final CidsBean fromPointBean = renderer.getPointBean(FROM);
                final CidsBean toPointBean = renderer.getPointBean(TO);
                if ((fromPointBean != null) && fromPointBean.equals(pointBean)) {
                    return true;
                } else if ((toPointBean != null) && toPointBean.equals(pointBean)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void updateBadGeomButton(final boolean isFrom) {
        final boolean visible = isEditable() && (getBadGeomFeature(isFrom) != null);
        getBadGeomButton(isFrom).setVisible(visible);
        getBadGeomCorrectButton(isFrom).setVisible(visible && getBadGeomButton(isFrom).isSelected());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value   DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void setPointValueToSpinner(final double value, final boolean isFrom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("change spinner");
        }

        if (!isSpinnerChangeLocked(isFrom)) {
            final JSpinner pointSpinner = getPointSpinner(isFrom);
            pointSpinner.setValue(round(value));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value   DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void setPointValueToFeature(final double value, final boolean isFrom) {
        if (!isFeatureChangeLocked(isFrom)) {
            final LinearReferencedPointFeature pointFeature = getPointFeature(isFrom);
            if (pointFeature != null) {
                pointFeature.setInfoFormat(decimalFormat);
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
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void updateRealGeoms(final boolean isFrom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("update real geom");
        }

        try {
            final LinearReferencedPointFeature pointFeature = getPointFeature(isFrom);
            final CidsBean pointBean = getPointBean(isFrom);

            // realgeom der Station anpassen
            if (pointFeature != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("change station geom");
                }

                final Geometry pointGeom = LinearReferencedPointFeature.getPointOnLine(
                        linearReferencingHelper.getLinearValueFromStationBean(pointBean),
                        linearReferencingHelper.getRouteGeometryFromStationBean(pointBean));
                pointGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                linearReferencingHelper.setPointGeometryToStationBean(pointGeom, pointBean);
            }

            // realgeom der Linie anpassen
            if (LOG.isDebugEnabled()) {
                LOG.debug("change line geom");
            }
            final Geometry lineGeom = LinearReferencedLineFeature.createSubline(getPointValue(FROM),
                    getPointValue(TO),
                    linearReferencingHelper.getRouteGeometryFromStationBean(getPointBean(isFrom)));
            lineGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
            setLineGeometry(lineGeom);
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while setting real geoms", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value   DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void setPointValueToBean(final double value, final boolean isFrom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("change bean value to " + value);
        }
        if (!isBeanChangeLocked(isFrom)) {
            final CidsBean pointBean = getPointBean(isFrom);
            final double oldValue = linearReferencingHelper.getLinearValueFromStationBean(pointBean);

            if (oldValue != round(value)) {
                try {
                    linearReferencingHelper.setLinearValueToStationBean(round(value), pointBean);
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
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isFeatureChangeLocked(final boolean isFrom) {
        return (isFrom) ? isFromFeatureChangeLocked : isToFeatureChangeLocked;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isSpinnerChangeLocked(final boolean isFrom) {
        return (isFrom) ? isFromSpinnerChangeLocked : isToSpinnerChangeLocked;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isBeanChangeLocked(final boolean isFrom) {
        return (isFrom) ? isFromBeanChangeLocked : isToBeanChangeLocked;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lock    DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void lockFeatureChange(final boolean lock, final boolean isFrom) {
        if (isFrom) {
            isFromFeatureChangeLocked = lock;
        } else {
            isToFeatureChangeLocked = lock;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lock    DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void lockSpinnerChange(final boolean lock, final boolean isFrom) {
        if (isFrom) {
            isFromSpinnerChangeLocked = lock;
        } else {
            isToSpinnerChangeLocked = lock;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lock    DOCUMENT ME!
     * @param  isFrom  DOCUMENT ME!
     */
    private void lockBeanChange(final boolean lock, final boolean isFrom) {
        if (isFrom) {
            isFromBeanChangeLocked = lock;
        } else {
            isToBeanChangeLocked = lock;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initPointBeanChangeListener(final boolean isFrom) {
        final PropertyChangeListener listener = new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent pce) {
                    if (pce.getPropertyName().equals(PROP_STATION_VALUE)) {
                        pointBeanValueChanged(isFrom);
                    }
                }
            };

        setPointBeanChangeListener(listener, isFrom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initPointBeanMergeListener(final boolean isFrom) {
        final PointBeanMergeListener listener = new PointBeanMergeListener() {

                @Override
                public void pointBeanMerged(final CidsBean pointBean) {
                    updateSplitMergeControls(isFrom);
                    refresh();
                }

                @Override
                public void pointBeanSplitted() {
                    updateSplitMergeControls(isFrom);
                    resetPointValue(isFrom);
                    refresh();
                }
            };

        setPointBeanMergeListener(listener, isFrom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initPointTransferHandler(final boolean isFrom) {
        getPointIconLabel(isFrom).setTransferHandler(new PointBeanTransferHandler(isFrom));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void initPointIconLabelMouseListener(final boolean isFrom) {
        getPointIconLabel(isFrom).addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(final MouseEvent evt) {
                    final boolean isNew = getPointBean(isFrom).getMetaObject().getStatus() == MetaObject.NEW;
                    if (isEditable() && !isPointMerged(isFrom) /* && !isNew*/) {
                        final JComponent comp = (JComponent)evt.getSource();
                        final TransferHandler th = comp.getTransferHandler();
                        th.exportAsDrag(comp, evt, TransferHandler.COPY);
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry getRouteGeometry() {
        final CidsBean routeGeomBean = getRouteGeomBean();
        if (routeGeomBean == null) {
            return null;
        }
        return (Geometry)routeGeomBean.getProperty(PROP_GEOM_GEOFIELD);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean getPointBean(final boolean isFrom) {
        final CidsBean pointBean = linearReferencingHelper.getStationBeanFromLineBean(getLineBean(), isFrom);
        return pointBean;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean getLineBean() {
        if (getLineField() == null) {
            return getCidsBean();
        } else {
            final CidsBean lineBean = getLineBean(getCidsBean(), getLineField());
            return lineBean;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean   DOCUMENT ME!
     * @param   lineField  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static CidsBean getLineBean(final CidsBean cidsBean, final String lineField) {
        if (cidsBean == null) {
            return null;
        }
        return (CidsBean)cidsBean.getProperty(lineField);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getRouteBean() {
        final CidsBean pointBean = getPointBean(FROM);
        if (pointBean == null) {
            return null;
        }
        return (CidsBean)pointBean.getProperty(PROP_STATION_ROUTE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getRouteGeomBean() {
        final CidsBean routeBean = getRouteBean();
        if (routeBean == null) {
            return null;
        }
        return (CidsBean)routeBean.getProperty(PROP_ROUTE_GEOM);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void setLineGeometry(final Geometry line) throws Exception {
        linearReferencingHelper.setGeometryToLineBean(line, getLineBean());
        if (getLineBean() != null) {
            getLineBean().setArtificialChangeFlag(true);
        }
    }

    @Override
    public void setEnabled(final boolean bln) {
        super.setEnabled(bln);
        jLabel3.setVisible(bln);
        getPointSpinner(FROM).setEnabled(bln);
        getPointSpinner(TO).setEnabled(bln);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        externalGeomDialog = new JDialog(StaticSwingTools.getParentFrame(this));
        geomDialogScrollPane = new javax.swing.JScrollPane();
        geomDialogInternalPanel = new javax.swing.JPanel();
        panEdit = new javax.swing.JPanel();
        panLinePoints = new javax.swing.JPanel();
        lblFromIcon = new javax.swing.JLabel();
        lblToIcon = new javax.swing.JLabel();
        spnFrom = new javax.swing.JSpinner();
        spnTo = new javax.swing.JSpinner();
        lblFromValue = new javax.swing.JLabel();
        lblToValue = new javax.swing.JLabel();
        panFromBadGeomSpacer = new javax.swing.JPanel();
        btnFromBadGeom = new javax.swing.JToggleButton();
        btnFromBadGeomCorrect = new javax.swing.JButton();
        panToBadGeomSpacer = new javax.swing.JPanel();
        btnToBadGeomCorrect = new javax.swing.JButton();
        btnToBadGeom = new javax.swing.JToggleButton();
        lblToPointSplit = new javax.swing.JLabel();
        lblFrontPointSplit = new javax.swing.JLabel();
        btnFromPointSplit = new javax.swing.JButton();
        btnToPointSplit = new javax.swing.JButton();
        lblRoute = new javax.swing.JLabel();
        btnRoute = new javax.swing.JToggleButton();
        panLine = new javax.swing.JPanel();
        panOtherLines = new javax.swing.JPanel();
        panSpacer = new javax.swing.JPanel();
        panAdd = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        panError = new javax.swing.JPanel();
        lblError = new javax.swing.JLabel();
        panAddFromFeature = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        cbPossibleRoute = new javax.swing.JComboBox();
        butApply = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();

        externalGeomDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        externalGeomDialog.setTitle(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.externalGeomDialog.title")); // NOI18N
        externalGeomDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        geomDialogInternalPanel.setLayout(new java.awt.GridBagLayout());
        geomDialogScrollPane.setViewportView(geomDialogInternalPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        externalGeomDialog.getContentPane().add(geomDialogScrollPane, gridBagConstraints);

        setOpaque(false);
        setLayout(new java.awt.CardLayout());

        panEdit.setOpaque(false);
        panEdit.setLayout(new java.awt.GridBagLayout());

        panLinePoints.setOpaque(false);
        panLinePoints.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(final java.awt.event.MouseEvent evt) {
                    panLinePointsMouseClicked(evt);
                }
            });
        panLinePoints.setLayout(new java.awt.GridBagLayout());

        lblFromIcon.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/station.png"))); // NOI18N
        lblFromIcon.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblFromIcon.text_1"));                           // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        panLinePoints.add(lblFromIcon, gridBagConstraints);

        lblToIcon.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/station.png"))); // NOI18N
        lblToIcon.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblToIcon.text_1"));                             // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        panLinePoints.add(lblToIcon, gridBagConstraints);

        spnFrom.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 0.0d, 1.0d));
        spnFrom.setEditor(new javax.swing.JSpinner.NumberEditor(spnFrom, "###.00"));
        spnFrom.setMinimumSize(new java.awt.Dimension(100, 28));
        spnFrom.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        panLinePoints.add(spnFrom, gridBagConstraints);

        spnTo.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 0.0d, 1.0d));
        spnTo.setEditor(new javax.swing.JSpinner.NumberEditor(spnTo, "###.00"));
        spnTo.setMinimumSize(new java.awt.Dimension(100, 28));
        spnTo.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        panLinePoints.add(spnTo, gridBagConstraints);

        lblFromValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblFromValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblFromValue.setMaximumSize(new java.awt.Dimension(100, 28));
        lblFromValue.setMinimumSize(new java.awt.Dimension(100, 28));
        lblFromValue.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        panLinePoints.add(lblFromValue, gridBagConstraints);

        lblToValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblToValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblToValue.setMaximumSize(new java.awt.Dimension(100, 28));
        lblToValue.setMinimumSize(new java.awt.Dimension(100, 28));
        lblToValue.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        panLinePoints.add(lblToValue, gridBagConstraints);

        panFromBadGeomSpacer.setMaximumSize(new java.awt.Dimension(86, 28));
        panFromBadGeomSpacer.setMinimumSize(new java.awt.Dimension(86, 28));
        panFromBadGeomSpacer.setOpaque(false);
        panFromBadGeomSpacer.setPreferredSize(new java.awt.Dimension(86, 28));
        panFromBadGeomSpacer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        btnFromBadGeom.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/exclamation.png"))); // NOI18N
        btnFromBadGeom.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnFromBadGeom.text"));                              // NOI18N
        btnFromBadGeom.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnFromBadGeom.toolTipText"));                       // NOI18N
        btnFromBadGeom.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnFromBadGeomActionPerformed(evt);
                }
            });
        panFromBadGeomSpacer.add(btnFromBadGeom);

        btnFromBadGeomCorrect.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/node-delete.png"))); // NOI18N
        btnFromBadGeomCorrect.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnFromBadGeomCorrect.text_1"));                     // NOI18N
        btnFromBadGeomCorrect.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnFromBadGeomCorrect.toolTipText"));                // NOI18N
        btnFromBadGeomCorrect.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnFromBadGeomCorrectActionPerformed(evt);
                }
            });
        panFromBadGeomSpacer.add(btnFromBadGeomCorrect);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        panLinePoints.add(panFromBadGeomSpacer, gridBagConstraints);

        panToBadGeomSpacer.setMaximumSize(new java.awt.Dimension(86, 28));
        panToBadGeomSpacer.setMinimumSize(new java.awt.Dimension(86, 28));
        panToBadGeomSpacer.setOpaque(false);
        panToBadGeomSpacer.setPreferredSize(new java.awt.Dimension(86, 28));
        panToBadGeomSpacer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        btnToBadGeomCorrect.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/node-delete-child.png"))); // NOI18N
        btnToBadGeomCorrect.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnToBadGeomCorrect.text_1"));                             // NOI18N
        btnToBadGeomCorrect.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnToBadGeomCorrect.toolTipText"));                        // NOI18N
        btnToBadGeomCorrect.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnToBadGeomCorrectActionPerformed(evt);
                }
            });
        panToBadGeomSpacer.add(btnToBadGeomCorrect);

        btnToBadGeom.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/linearreferencing/exclamation.png"))); // NOI18N
        btnToBadGeom.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnToBadGeom.text"));                                // NOI18N
        btnToBadGeom.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnToBadGeom.toolTipText"));                         // NOI18N
        btnToBadGeom.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnToBadGeomActionPerformed(evt);
                }
            });
        panToBadGeomSpacer.add(btnToBadGeom);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        panLinePoints.add(panToBadGeomSpacer, gridBagConstraints);

        lblToPointSplit.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblToPointSplit.text"));        // NOI18N
        lblToPointSplit.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblToPointSplit.toolTipText")); // NOI18N
        lblToPointSplit.setMaximumSize(new java.awt.Dimension(16, 16));
        lblToPointSplit.setMinimumSize(new java.awt.Dimension(16, 16));
        lblToPointSplit.setPreferredSize(new java.awt.Dimension(16, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        panLinePoints.add(lblToPointSplit, gridBagConstraints);

        lblFrontPointSplit.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblFrontPointSplit.text_1"));      // NOI18N
        lblFrontPointSplit.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblFrontPointSplit.toolTipText")); // NOI18N
        lblFrontPointSplit.setMaximumSize(new java.awt.Dimension(16, 16));
        lblFrontPointSplit.setMinimumSize(new java.awt.Dimension(16, 16));
        lblFrontPointSplit.setPreferredSize(new java.awt.Dimension(16, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        panLinePoints.add(lblFrontPointSplit, gridBagConstraints);

        btnFromPointSplit.setIcon(ICON_MERGED_WITH_FROM_POINT);
        btnFromPointSplit.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnFromPointSplit.text"));        // NOI18N
        btnFromPointSplit.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnFromPointSplit.toolTipText")); // NOI18N
        btnFromPointSplit.setMaximumSize(new java.awt.Dimension(28, 28));
        btnFromPointSplit.setMinimumSize(new java.awt.Dimension(28, 28));
        btnFromPointSplit.setPreferredSize(new java.awt.Dimension(28, 28));
        btnFromPointSplit.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnFromPointSplitActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        panLinePoints.add(btnFromPointSplit, gridBagConstraints);

        btnToPointSplit.setIcon(ICON_MERGED_WITH_TO_POINT);
        btnToPointSplit.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnToPointSplit.text"));        // NOI18N
        btnToPointSplit.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnToPointSplit.toolTipText")); // NOI18N
        btnToPointSplit.setMaximumSize(new java.awt.Dimension(28, 28));
        btnToPointSplit.setMinimumSize(new java.awt.Dimension(28, 28));
        btnToPointSplit.setPreferredSize(new java.awt.Dimension(28, 28));
        btnToPointSplit.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnToPointSplitActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        panLinePoints.add(btnToPointSplit, gridBagConstraints);

        lblRoute.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRoute.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblRoute.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        panLinePoints.add(lblRoute, gridBagConstraints);

        btnRoute.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnRoute.text"));        // NOI18N
        btnRoute.setToolTipText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.btnRoute.toolTipText")); // NOI18N
        btnRoute.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnRouteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        panLinePoints.add(btnRoute, gridBagConstraints);

        panLine.setBackground(new java.awt.Color(255, 91, 0));
        panLine.setMinimumSize(new java.awt.Dimension(10, 3));
        panLine.setPreferredSize(new java.awt.Dimension(100, 3));
        panLine.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(final java.awt.event.MouseEvent evt) {
                    panLineMouseClicked(evt);
                }
            });
        panLine.setLayout(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panLinePoints.add(panLine, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        panEdit.add(panLinePoints, gridBagConstraints);

        panOtherLines.setOpaque(false);
        panOtherLines.setLayout(new javax.swing.BoxLayout(panOtherLines, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        panEdit.add(panOtherLines, gridBagConstraints);

        panSpacer.setMaximumSize(new java.awt.Dimension(0, 0));
        panSpacer.setMinimumSize(new java.awt.Dimension(0, 0));
        panSpacer.setOpaque(false);
        panSpacer.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        panEdit.add(panSpacer, gridBagConstraints);

        add(panEdit, "edit");

        panAdd.setOpaque(false);
        panAdd.setLayout(new java.awt.GridBagLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.jLabel3.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panAdd.add(jLabel3, gridBagConstraints);

        add(panAdd, "add");

        panError.setOpaque(false);
        panError.setLayout(new java.awt.GridBagLayout());

        lblError.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.lblError.text_1")); // NOI18N
        panError.add(lblError, new java.awt.GridBagConstraints());

        add(panError, "error");

        panAddFromFeature.setOpaque(false);
        panAddFromFeature.setLayout(new java.awt.GridBagLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.jLabel4.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panAddFromFeature.add(jLabel4, gridBagConstraints);

        cbPossibleRoute.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPossibleRoute.setPreferredSize(new java.awt.Dimension(300, 20));
        cbPossibleRoute.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbPossibleRouteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        panAddFromFeature.add(cbPossibleRoute, gridBagConstraints);

        butApply.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.butApply.text_1",
                new Object[] {})); // NOI18N
        butApply.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butApplyActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        panAddFromFeature.add(butApply, gridBagConstraints);

        butCancel.setText(org.openide.util.NbBundle.getMessage(
                LinearReferencedLineEditor.class,
                "LinearReferencedLineEditor.butCancel.text_1",
                new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        panAddFromFeature.add(butCancel, gridBagConstraints);

        add(panAddFromFeature, "addFeature");
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnFromPointSplitActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFromPointSplitActionPerformed
        splitPoint(FROM);
    }//GEN-LAST:event_btnFromPointSplitActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnToPointSplitActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToPointSplitActionPerformed
        splitPoint(TO);
    }//GEN-LAST:event_btnToPointSplitActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnToBadGeomCorrectActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToBadGeomCorrectActionPerformed
        correctBadGeom(TO);
    }//GEN-LAST:event_btnToBadGeomCorrectActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnToBadGeomActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToBadGeomActionPerformed
        switchBadGeomVisibility(TO);
    }//GEN-LAST:event_btnToBadGeomActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnFromBadGeomCorrectActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFromBadGeomCorrectActionPerformed
        correctBadGeom(FROM);
    }//GEN-LAST:event_btnFromBadGeomCorrectActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnFromBadGeomActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFromBadGeomActionPerformed
        switchBadGeomVisibility(FROM);
    }//GEN-LAST:event_btnFromBadGeomActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnRouteActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRouteActionPerformed
        updateOtherLinesPanelVisibility();
    }//GEN-LAST:event_btnRouteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbPossibleRouteActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPossibleRouteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbPossibleRouteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butApplyActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butApplyActionPerformed
        if (isEnabled() && isEditable()) {
            if (cbPossibleRoute.getSelectedItem() instanceof CidsLayerFeature) {
                final CidsLayerFeature f = (CidsLayerFeature)cbPossibleRoute.getSelectedItem();
                final CidsBean routeBean = f.getBean();

                if ((getDropBehavior() == null) || getDropBehavior().checkForAdding(routeBean)) {
                    setLineBeanFromRouteBean(routeBean);
                    setChangedSinceDrop(false);
                }
                if (cidsBeanStore != null) {
                    cidsBeanStore.setCidsBean(cidsBean);
                }
                if (isAutoZoomActivated) {
                    zoomToFeatureCollection(getZoomFeatures());
                }
            }
        }
    }//GEN-LAST:event_butApplyActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCancelActionPerformed
        showCard(Card.edit);
    }//GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void panLineMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panLineMouseClicked
        if (!routeCombo) {
            return;
        }
        showCard(Card.add);

        fillRoutesCombo();
    }//GEN-LAST:event_panLineMouseClicked

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void panLinePointsMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panLinePointsMouseClicked
        panLineMouseClicked(evt);
    }//GEN-LAST:event_panLinePointsMouseClicked

    /**
     * DOCUMENT ME!
     */
    private void fillRoutesCombo() {
        if (!routeCombo) {
            return;
        }
        final List<Feature> selectedRoutes = getPossibleRoutes();

        if (!selectedRoutes.isEmpty()) {
            cbPossibleRoute.setModel(new DefaultComboBoxModel(selectedRoutes.toArray()));
        } else if (!routesComboInitialised) {
            routesComboInitialised = true;
            cbPossibleRoute.setModel(new DefaultComboBoxModel(new Object[] { "Lade" }));
            final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                    linearReferencingHelper.getDomainOfRouteTable(routeMetaClassName)[0],
                    routeMetaClassName, getConnectionContext());
            final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();

            final CidsLayer layer = new CidsLayer(routeMc);
            try {
                layer.setBoundingBox(mc.getCurrentBoundingBox());
                layer.addRetrievalListener(new RetrievalListener() {

                        @Override
                        public void retrievalStarted(final RetrievalEvent e) {
                        }

                        @Override
                        public void retrievalProgress(final RetrievalEvent e) {
                        }

                        @Override
                        public void retrievalComplete(final RetrievalEvent e) {
                            if (!e.isInitialisationEvent()) {
                                complete((List)e.getRetrievedObject());
                            }
                        }

                        @Override
                        public void retrievalAborted(final RetrievalEvent e) {
                            complete((List)e.getRetrievedObject());
                        }

                        @Override
                        public void retrievalError(final RetrievalEvent e) {
                            complete((List)e.getRetrievedObject());
                        }

                        private void complete(final List features) {
                            final List<Feature> routes = new ArrayList<>();

                            if (features instanceof List) {
                                final List fl = (List)features;

                                for (final Object o : fl) {
                                    if (o instanceof Feature) {
                                        routes.add((Feature)o);
                                    }
                                }
                            }
                            cbPossibleRoute.setModel(new DefaultComboBoxModel(routes.toArray()));
                        }
                    });

                layer.retrieve(true);
            } catch (Exception e) {
                LOG.error("Error while retrieving features", e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Feature> getPossibleRoutes() {
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> featureList = sl.getAllSelectedPFeatures();
        final List<Feature> possibleRoutes = new ArrayList<>();
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                linearReferencingHelper.getDomainOfRouteTable(routeMetaClassName)[0],
                routeMetaClassName, getConnectionContext());

        for (final PFeature f : featureList) {
            final Feature selectedFeature = f.getFeature();

            if (selectedFeature instanceof CidsLayerFeature) {
                final CidsLayerFeature clFeature = (CidsLayerFeature)selectedFeature;

                if (routeMc.equals(clFeature.getBean().getMetaObject().getMetaClass())) {
                    possibleRoutes.add(selectedFeature);
                }
            }
        }

        return possibleRoutes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean changeOtherLinesPanelVisibility() {
        btnRoute.setSelected(!btnRoute.isSelected());
        updateOtherLinesPanelVisibility();

        return btnRoute.isSelected();
    }

    @Override
    public void windowOpened(final WindowEvent e) {
    }

    @Override
    public void windowClosing(final WindowEvent e) {
    }

    @Override
    public void windowClosed(final WindowEvent evt) {
        if (evt.getSource() == externalGeomDialog) {
            externalGeomDialog.removeWindowListener(this);
            geomDialogInternalPanel.remove(externalOthersEditor);
            externalOthersEditor.dispose();
            externalOthersEditor = null;
            btnRoute.setSelected(false);
            // updateSplitMergeControls(FROM);
            // updateSplitMergeControls(TO);
            updateOtherLinesPanelVisibility();
        }
    }

    @Override
    public void windowIconified(final WindowEvent e) {
    }

    @Override
    public void windowDeiconified(final WindowEvent e) {
    }

    @Override
    public void windowActivated(final WindowEvent e) {
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
    }

    /**
     * DOCUMENT ME!
     */
    private void updateOtherLinesPanelVisibility() {
        if (isEditable() && isOtherLinesEnabled()) {
            // panel anzeigen / verbergen
            fireOtherLinesPanelVisibilityChange(btnRoute.isSelected());

            // gegebenenfalls die features der subrenderer anzeigen / verbergen
            if (isDrawingFeaturesEnabled()) {
                for (final Component component : panOtherLines.getComponents()) {
                    final LinearReferencedLineRenderer renderer = (LinearReferencedLineRenderer)component;
                    renderer.setDrawingFeaturesEnabled(btnRoute.isSelected());
                }
            }

            if (btnRoute.isSelected() && showOtherInDialog) {
                if (externalOthersEditor != null) {
                    return;
                }
                externalOthersEditor = new LinearReferencedLineEditor(routeMetaClassName);
                externalOthersEditor.setOtherLines(otherLines);
                externalOthersEditor.setOtherLinesEnabled(isOtherLinesEnabled);
                externalOthersEditor.setOtherLinesQueryAddition(otherLinesFromQueryPart, otherLinesWhereQueryPart);
                externalOthersEditor.setDrawingFeaturesEnabled(true);
                externalOthersEditor.setLineField(lineField);
                externalOthersEditor.parent = this;
                externalOthersEditor.setCidsBean(cidsBean);
                geomDialogInternalPanel.add(externalOthersEditor);

                if (!externalOthersEditor.btnRoute.isSelected()) {
                    externalOthersEditor.btnRoute.doClick();
                }

                externalGeomDialog.addWindowListener(this);
                externalGeomDialog.setSize(500, 400);
                externalGeomDialog.setModal(true);
                StaticSwingTools.showDialog(StaticSwingTools.getParentFrame(this), externalGeomDialog, true);
            } else {
                panOtherLines.setVisible(btnRoute.isSelected());
            }

            updateSplitMergeControls(FROM);
            updateSplitMergeControls(TO);
        } else {
            panOtherLines.setVisible(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  showOtherInDialog  DOCUMENT ME!
     */
    public void setShowOtherInDialog(final boolean showOtherInDialog) {
        this.showOtherInDialog = showOtherInDialog;
    }

    /**
     * DOCUMENT ME!
     */
    private void cleanOtherLinesPanel() {
        for (final Component component : panOtherLines.getComponents()) {
            final LinearReferencedLineRenderer renderer = (LinearReferencedLineRenderer)component;
            renderer.dispose();
        }
        panOtherLines.removeAll();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom  DOCUMENT ME!
     */
    private void splitPoint(final boolean isFrom) {
        if (isEditable()) {
            final CidsBean oldPointBean = getPointBean(isFrom);
            final LinearReferencedPointFeature oldPointFeature = getPointFeature(isFrom);
            final double oldPosition = getPointFeature(isFrom).getCurrentPosition();

            final CidsBean newPointBean = linearReferencingHelper.createStationBeanFromRouteBean(
                    linearReferencingHelper.getRouteBeanFromStationBean(getPointBean(isFrom)));
            if (parent != null) {
                parent.cleanupPoint(isFrom);
            }
            setPoint(newPointBean, isFrom);
            if (parent != null) {
                parent.initPoint(isFrom);
            }
            pointBeanValueChanged(isFrom);

            final LinearReferencedPointFeature pointFeature = getPointFeature(isFrom);
            getLineFeature().setPointFeature(pointFeature, isFrom);

            if ((fromPointFeature != null) && !fromPointFeature.isEditable()) {
                fromPointFeature.setEditable(isEditable());
            }

            if (oldPointFeature != null) {
                oldPointFeature.setEditable(false);
            }

            MERGE_REGISTRY.firePointBeanSplitted(oldPointBean);

            // neue station auf selbe position setzen wie die alte
            pointFeature.moveToPosition(oldPosition);

            // das reseten eines punktes wieder rückgängig machen falls das feature editable ist (was bedeutet dass
            // irgendein editor diese bean verwendet)
            if (oldPointFeature.isEditable()) {
                try {
                    linearReferencingHelper.setLinearValueToStationBean(oldPosition, oldPointBean);
                } catch (Exception ex) {
                    LOG.error("error while avoiding reset of editable point", ex);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointBean  DOCUMENT ME!
     * @param  isFrom     DOCUMENT ME!
     */
    private void setPointBean(final CidsBean pointBean, final boolean isFrom) {
        try {
            getLineBean().setProperty(getPointField(isFrom), pointBean);
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error while setting cidsbean for point", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lineBean  DOCUMENT ME!
     */
    private void setLineBean(final CidsBean lineBean) {
        try {
            cleanupLine();
            if ((getCidsBean() != null) && (getLineField() != null)) {
                final CidsBean bean = (CidsBean)getCidsBean().getProperty(getLineField());
                if (bean != null) {
                    bean.removePropertyChangeListener(linePropertyChangeListener);
                }
                getCidsBean().setProperty(getLineField(), lineBean);
                lineBean.addPropertyChangeListener(linePropertyChangeListener);
            } else {
                final CidsBean bean = getCidsBean();
                if (bean != null) {
                    bean.removePropertyChangeListener(linePropertyChangeListener);
                }
                cidsBean = lineBean;
                lineBean.addPropertyChangeListener(linePropertyChangeListener);
            }
            initLine();
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error while setting line bean to cidsbean", ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  routeBean  DOCUMENT ME!
     */
    private void setLineBeanFromRouteBean(final CidsBean routeBean) {
        final CidsBean lineBean = linearReferencingHelper.createLineBeanFromRouteBean(routeBean);
        setLineBean(lineBean);

        // Geometrie für BoundingBox erzeufen
        final XBoundingBox boundingBox = (XBoundingBox)MAPPING_COMPONENT.getCurrentBoundingBox();
        final Collection<Coordinate> coordinates = new ArrayList<Coordinate>();
        coordinates.add(new Coordinate(boundingBox.getX1(), boundingBox.getY1()));
        coordinates.add(new Coordinate(boundingBox.getX2(), boundingBox.getY1()));
        coordinates.add(new Coordinate(boundingBox.getX2(), boundingBox.getY2()));
        coordinates.add(new Coordinate(boundingBox.getX1(), boundingBox.getY2()));
        coordinates.add(new Coordinate(boundingBox.getX1(), boundingBox.getY1()));
        final GeometryFactory gf = new GeometryFactory();
        final LineString boundingBoxGeom = gf.createLineString(coordinates.toArray(new Coordinate[0]));

        final LinearReferencedLineFeature lineFeature = getLineFeature();

        // ermitteln welche Punkte sich innerhalb der Boundingbox befinden
        final Coordinate fromCoord = lineFeature.getPointFeature(FROM).getGeometry().getCoordinate();
        final Coordinate toCoord = lineFeature.getPointFeature(TO).getGeometry().getCoordinate();
        final boolean testFrom = (fromCoord.x > boundingBox.getX1())
                    && (fromCoord.x < boundingBox.getX2())
                    && (fromCoord.y > boundingBox.getY1())
                    && (fromCoord.y < boundingBox.getY2());
        final boolean testTo = (toCoord.x > boundingBox.getX1())
                    && (toCoord.x < boundingBox.getX2())
                    && (toCoord.y > boundingBox.getY1())
                    && (toCoord.y < boundingBox.getY2());

        // Startwerte festlegen
        final LineString featureGeom = (LineString)lineFeature.getGeometry();
        double minPosition = (testFrom) ? 0 : featureGeom.getLength();
        double maxPosition = (testTo) ? featureGeom.getLength() : 0;

        // Coordinaten durchlaufen und anhand der Position auf der Linie sortieren
        final Geometry intersectionGeom = featureGeom.intersection(boundingBoxGeom);
        for (final Coordinate coord : intersectionGeom.getCoordinates()) {
            final double position = LinearReferencedPointFeature.getPositionOnLine(
                    coord,
                    featureGeom);
            if (position > maxPosition) {
                maxPosition = position;
            }
            if (position < minPosition) {
                minPosition = position;
            }
        }

        // sollte max größer min sein, dann umdrehen
        if (minPosition > maxPosition) {
            final double tmp = minPosition;
            minPosition = maxPosition;
            maxPosition = tmp;
        }

        // ermittelte from und to Position setzen
        setPointValueToBean(minPosition, FROM);
        setPointValueToBean(maxPosition, TO);
    }

    @Override
    public void beansDropped(final ArrayList<CidsBean> cidsBeans) {
        if (isEnabled() && isEditable()) {
            for (final CidsBean routeBean : cidsBeans) {
                if (routeBean.getMetaObject().getMetaClass().getName().equals(CN_ROUTE)) {
                    if ((getDropBehavior() == null) || getDropBehavior().checkForAdding(routeBean)) {
                        setLineBeanFromRouteBean(routeBean);
                        setChangedSinceDrop(false);
                    }
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
     * @param  isFrom        DOCUMENT ME!
     * @param  targetIsFrom  DOCUMENT ME!
     * @param  lineBean      DOCUMENT ME!
     */
    private void updateSnappedRealGeoms(final boolean isFrom,
            final boolean targetIsFrom,
            final CidsBean lineBean) {
        final MetaClass mcLine = lineBean.getMetaObject().getMetaClass();

        final int ownLineId = lineBean.getMetaObject().getId();
        final CidsBean pointBean = (isFrom)
            ? (CidsBean)lineBean.getProperty(LinearReferencingConstants.PROP_STATIONLINIE_FROM)
            : (CidsBean)lineBean.getProperty(LinearReferencingConstants.PROP_STATIONLINIE_TO);
        final int pointId = pointBean.getMetaObject().getId();
        final String query = "SELECT "
                    + mcLine.getId() + ", "
                    + mcLine.getPrimaryKey() + " "
                    + "FROM "
                    + mcLine.getTableName() + " "
                    + "WHERE "
                    + mcLine.getPrimaryKey() + " != " + ownLineId + " AND "
                    + getPointField(targetIsFrom) + " = " + pointId + " "
                    + ";";

        try {
            // stationierte Linien mit gleicher Station am Ende 'targetIsFrom' holen
            final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0, getConnectionContext());

            for (final MetaObject mo : mos) {
                // bean der stationierten Linie
                final CidsBean targetBean = CIDSBEAN_CACHE.getCachedBeanFor(mo.getBean());

                // beans der stationen
                final CidsBean targetFromBean = (CidsBean)targetBean.getProperty(getPointField(FROM));
                final CidsBean targetToBean = (CidsBean)targetBean.getProperty(getPointField(TO));

                final Geometry routeGeometry = FeatureRegistry.getInstance()
                            .getLinearReferencingSolver()
                            .getRouteGeometryFromStationBean(targetFromBean);
                final double currentValue = (Double)pointBean.getProperty(
                        LinearReferencingConstants.PROP_STATION_VALUE);

                // muss from oder to angepasst werden ?
                final double targetFromValue = (targetIsFrom)
                    ? currentValue
                    : FeatureRegistry.getInstance().getLinearReferencingSolver()
                            .getLinearValueFromStationBean(targetFromBean);
                final double targetToValue = (targetIsFrom)
                    ? FeatureRegistry.getInstance().getLinearReferencingSolver()
                            .getLinearValueFromStationBean(targetToBean) : currentValue;

                // geometry der linie neu berechnen
                final Geometry targetLineGeometry = LinearReferencedLineFeature.createSubline(
                        targetFromValue,
                        targetToValue,
                        routeGeometry);

                // von feature neu berechnete geometrie im wk_teil setzen
                FeatureRegistry.getInstance()
                        .getLinearReferencingSolver()
                        .setGeometryToLineBean(targetLineGeometry, targetBean);

                // linie speichern
                targetBean.persist(getConnectionContext());
            }
        } catch (Exception ex) {
            LOG.error("error while updating snapped real geoms", ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isFrom    DOCUMENT ME!
     * @param  lineBean  DOCUMENT ME!
     */
    private void updateSnappedRealGeoms(final boolean isFrom, final CidsBean lineBean) {
        updateSnappedRealGeoms(isFrom, FROM, lineBean);
        updateSnappedRealGeoms(isFrom, TO, lineBean);
    }

    @Override
    public void editorClosed(final EditorClosedEvent event) {
        // es wurde hoechstwahrscheinlich ein hauptobjekt geschlossen (die Methode editorClosed wird normalerweise
        // nur dann aufgerufen), wir wissen also dass der cache nicht mehr benötigt wird
        CIDSBEAN_CACHE.clear();
        if (event.getStatus() == EditorSaveStatus.SAVE_SUCCESS) {
            if (isEditable()) {
                new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            // otherlinebeans kram noch notwending ??? updatesnappedrealgeoms macht doch schon persist
                            // auf gesnappte stationien for (final CidsBean otherLineBean : otherLineBeans) { if
                            // (otherLineBean.hasArtificialChangeFlag()) { try { otherLineBean.persist(); } catch
                            // (Exception ex) { LOG.error("error during persist", ex); } } } otherLineBeans.clear();

                            final CidsBean savedBean = event.getSavedBean();
                            updateSnappedRealGeoms(FROM, getLineBean(savedBean, lineField));
                            updateSnappedRealGeoms(TO, getLineBean(savedBean, lineField));
                            return null;
                        }
                    }.execute();
            }
        }
    }

    @Override
    public boolean prepareForSave() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mergeParentLineEditor  DOCUMENT ME!
     */
    protected void setMergeParentLineEditor(final LinearReferencedLineEditor mergeParentLineEditor) {
        this.mergeParentLineEditor = mergeParentLineEditor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LinearReferencedLineEditor getMergeParentLineEditor() {
        return mergeParentLineEditor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fromPoint      DOCUMENT ME!
     * @param   withPointBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean mergeRequest(final boolean fromPoint, final CidsBean withPointBean) {
        final CidsBean parentPointBean = getPointBean(fromPoint);
        // darf nicht mergen wenn dadurch der from und der to point gleich wären
        if (!withPointBean.equals(getPointBean(!fromPoint))) {
            if (parent != null) {
                parent.cleanupPoint(fromPoint);
            }
            setPoint(withPointBean, fromPoint);
            if (parent != null) {
                parent.initPoint(fromPoint);
            }
            pointBeanValueChanged(fromPoint);
            MERGE_REGISTRY.firePointBeanMerged(parentPointBean, withPointBean);

            return true;
        }

        return false;
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

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double round(final double value) {
        if (allowDoubleValues) {
            return value;
        } else {
            return Math.round(value);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Processes the route drop events. The transferable object can either contains cids beans or features
     *
     * @version  $Revision$, $Date$
     */
    private class LineEditorDropTarget extends CidsBeanDropTarget {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LineEditorDropTarget object.
         *
         * @param   c  DOCUMENT ME!
         *
         * @throws  HeadlessException  DOCUMENT ME!
         */
        public LineEditorDropTarget(final CidsBeanDropListenerComponent c) throws HeadlessException {
            super(c);
        }

        /**
         * Creates a new LineEditorDropTarget object.
         *
         * @param   c  DOCUMENT ME!
         *
         * @throws  HeadlessException  DOCUMENT ME!
         */
        public LineEditorDropTarget(final Component c) throws HeadlessException {
            super(c);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public synchronized void drop(final DropTargetDropEvent dtde) {
            boolean consumed = false;

            try {
                if (dtde.getTransferable().isDataFlavorSupported(AttributeTableTransferHandler.rowFlavor)) {
                    final Transferable t = dtde.getTransferable();
                    final Object data = t.getTransferData(AttributeTableTransferHandler.rowFlavor);

                    if (data instanceof FeatureServiceFeature[]) {
                        final FeatureServiceFeature[] fsf = (FeatureServiceFeature[])t.getTransferData(
                                AttributeTableTransferHandler.rowFlavor);
                        consumed = true;

                        for (final FeatureServiceFeature f : fsf) {
                            if (f instanceof CidsLayerFeature) {
                                final CidsBean routeBean = ((CidsLayerFeature)f).getBean();
                                final CidsBean station = linearReferencingHelper.getStationBeanFromLineBean(
                                        cidsBean,
                                        true);
                                final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                                        linearReferencingHelper.getDomainOfRouteTable(routeMetaClassName)[0],
                                        routeMetaClassName,
                                        getConnectionContext());

                                if (routeBean.getMetaObject().getMetaClass().equals(routeMc)) {
                                    if ((getDropBehavior() == null) || getDropBehavior().checkForAdding(routeBean)) {
                                        setLineBeanFromRouteBean(routeBean);
                                        setChangedSinceDrop(false);
                                    }
                                    if (isAutoZoomActivated) {
                                        zoomToFeatureCollection(getZoomFeatures());
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(
                                        LinearReferencedLineEditor.this,
                                        NbBundle.getMessage(
                                            LinearReferencedLineEditor.class,
                                            "LinearReferencedLineEditor.LineEditorDropTarget.invalidRoute.message"),
                                        NbBundle.getMessage(
                                            LinearReferencedLineEditor.class,
                                            "LinearReferencedLineEditor.LineEditorDropTarget.invalidRoute.title"),
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error processing drop event", e);
            }

            if (!consumed) {
                super.drop(dtde);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class OtherLinesComparator implements Comparator<CidsBean> {

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final CidsBean o1, final CidsBean o2) {
            final Double from = (Double)o1.getProperty("von.wert");
            final Double from2 = (Double)o2.getProperty("von.wert");

            if ((from == null) && (from2 == null)) {
                return 0;
            } else if (from == null) {
                return -1;
            } else if (from2 == null) {
                return 1;
            } else {
                return from.compareTo(from2);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LinePropertyChangeListener implements PropertyChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
//            if (!inited || testBool) {
//                LOG.error("not inited " + evt.getPropertyName());
//                return;
//            }
//            LOG.error("property change " + evt.getPropertyName());
//            if (evt.getPropertyName().equals(PROP_STATIONLINIE_FROM)) {
//                testBool = true;
////                if ((getPointFeature(FROM) != null)) { // && (evt.getOldValue() != null)) {
////                cleanupPoint((CidsBean)evt.getOldValue(), FROM);
//                setPointBean((CidsBean)evt.getOldValue(), FROM);
//                cleanupPoint(FROM);
//                setPointBean((CidsBean)evt.getNewValue(), FROM);
//                initPoint(FROM);
////                initPoint((CidsBean)evt.getNewValue(), FROM);
//                updateOtherLinesPanelVisibility();
//                testBool = false;
////                }
//            } else if (evt.getPropertyName().equals(PROP_STATIONLINIE_TO)) {
//                testBool = true;
////                if ((getPointFeature(TO) != null)) {   // && (evt.getOldValue() != null)) {
////                cleanupPoint((CidsBean)evt.getOldValue(), TO);
//                setPointBean((CidsBean)evt.getOldValue(), TO);
//                cleanupPoint(TO);
//                setPointBean((CidsBean)evt.getNewValue(), TO);
//                initPoint(TO);
////                initPoint((CidsBean)evt.getNewValue(), TO);
//                updateOtherLinesPanelVisibility();
//                testBool = false;
////                }
//            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class PointBeanTransferHandler extends TransferHandler {

        //~ Instance fields ----------------------------------------------------

        boolean isFrom;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PointBeanTransferHandler object.
         *
         * @param  isFrom  DOCUMENT ME!
         */
        public PointBeanTransferHandler(final boolean isFrom) {
            this.isFrom = isFrom;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Transferable createTransferable(final JComponent c) {
            return new PointBeanTransferable(isFrom);
        }

        @Override
        public int getSourceActions(final JComponent c) {
            return COPY;
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        private class PointBeanTransferable implements Transferable {

            //~ Instance fields ------------------------------------------------

            private final boolean isFrom;

            //~ Constructors ---------------------------------------------------

            /**
             * Creates a new PointTransferable object.
             *
             * @param  isFrom  pointBean DOCUMENT ME!
             */
            private PointBeanTransferable(final boolean isFrom) {
                this.isFrom = isFrom;
            }

            //~ Methods --------------------------------------------------------

            @Override
            public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return isFrom;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { CIDSBEAN_DATAFLAVOR };
            }

            @Override
            public boolean isDataFlavorSupported(final DataFlavor flavor) {
                return flavor.equals(CIDSBEAN_DATAFLAVOR);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class PointBeanDropTargetListener implements DropTargetListener {

        //~ Instance fields ----------------------------------------------------

        private boolean isFrom;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PointBeanDropTargetListener object.
         *
         * @param  isFrom  DOCUMENT ME!
         */
        public PointBeanDropTargetListener(final boolean isFrom) {
            this.isFrom = isFrom;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void drop(final DropTargetDropEvent e) {
            try {
                final LinearReferencedLineEditor mergeParentLineEditor = getMergeParentLineEditor();
                if (mergeParentLineEditor != null) {
                    final Transferable tr = e.getTransferable();
                    final DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        if (flavors[i].isFlavorSerializedObjectType()) {
                            e.acceptDrop(e.getDropAction());
                            final boolean isParentFrom = (Boolean)tr.getTransferData(CIDSBEAN_DATAFLAVOR);
                            e.dropComplete(mergeParentLineEditor.mergeRequest(isParentFrom, getPointBean(isFrom)));
//                            final CidsBean parentPointBean = mergeParentLineEditor.getPointBean(isParentFrom);
//                            // darf nicht mergen wenn dadurch der from und der to point gleich wären
//                            if (!getPointBean(isFrom).equals(mergeParentLineEditor.getPointBean(!isParentFrom))) {
//                                mergeParentLineEditor.setPoint(getPointBean(isFrom), isParentFrom);
//                                mergeParentLineEditor.pointBeanValueChanged(isParentFrom);
//                                e.dropComplete(true);
//                                MERGE_REGISTRY.firePointBeanMerged(parentPointBean, getPointBean(isFrom));
//                                return;
//                            }
                        }
                    }
                }
            } catch (Throwable t) {
                LOG.error("error while drop", t);
            }
            e.rejectDrop();
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
            LOG.info("dragEnter");
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
            LOG.info("dragOver");
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
            LOG.info("dropActionChanged");
        }

        @Override
        public void dragExit(final DropTargetEvent dte) {
            LOG.info("dragExit");
        }
    }
}
