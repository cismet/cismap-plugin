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
package de.cismet.cismap.cidslayer;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.permission.PermissionHolder;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.nodes.PImage;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.awt.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.editors.DefaultBindableReferenceCombo;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.cidslayer.StationInfo;

import de.cismet.cids.tools.tostring.CidsLayerFeatureToStringConverter;
import de.cismet.cids.tools.tostring.ToStringConverter;

import de.cismet.cids.utils.ClassloadingHelper;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.features.SLDStyledFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PSticky;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.cismap.linearreferencing.FeatureRegistry;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;
import de.cismet.cismap.linearreferencing.TableLinearReferencedLineEditor;
import de.cismet.cismap.linearreferencing.TableStationEditor;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerFeature extends DefaultFeatureServiceFeature implements ModifiableFeature, PermissionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static transient Logger LOG = Logger.getLogger(CidsLayerFeature.class);

    //~ Instance fields --------------------------------------------------------

    // private final int classId;
    /** this object should not be used directly. {@link getMetaObject()} should be used instead. */
    private MetaObject metaObject;
    private MetaClass metaClass;
    // protected Map<String, Object> properties;
    private CidsLayerInfo layerInfo;
    private Map<String, DisposableCidsBeanStore> stations = null;
    private Map<String, DefaultCidsLayerBindableReferenceCombo> combos = null;
    private Color backgroundColor;
    private PropertyChangeListener propListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                modified = true;
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

                if ((getLayerProperties() != null) && (getLayerProperties().getAttributeTableRuleSet() != null)) {
                    if (!getLayerProperties().getAttributeTableRuleSet().isCatThree()) {
                        if (evt.getSource() instanceof TableStationEditor) {
                            final TableStationEditor stat = (TableStationEditor)evt.getSource();

                            if (stat.isLine()) {
                                final LinearReferencingHelper helper = FeatureRegistry.getInstance()
                                            .getLinearReferencingSolver();
                                final Geometry g = (Geometry)helper.getGeomBeanFromLineBean(stat.getLineBean())
                                            .getProperty("geo_field");

                                setGeometry(g);
                            }
                        }
                    }
                }
                if (evt.getSource() instanceof TableStationEditor) {
                    final LinearReferencingHelper helper = FeatureRegistry.getInstance().getLinearReferencingSolver();
                    final TableStationEditor stat = (TableStationEditor)evt.getSource();
                    final String routeProperty = stat.getStationProperty();
                    String routeName = null;

                    if (stat.getCidsBean() != null) {
                        routeName = helper.getRouteNameFromStationBean(stat.getCidsBean());
                    }

                    setProperty(routeProperty, routeName);
                }
            }
        };

    private HashMap backupProperties;
    private Geometry backupGeometry;
    private boolean modified;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerFeature object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public CidsLayerFeature(final CidsLayerFeature feature) {
        super(feature);
        // properties = new HashMap<String, Object>(feature.properties);
        // classId = feature.classId;
        metaClass = feature.metaClass;
        this.layerInfo = feature.layerInfo;
        if (feature.metaObject != null) {
            metaObject = feature.metaObject;
        }
    }

    /**
     * Creates a new CidsLayerFeature object.
     *
     * @param  properties       oid DOCUMENT ME!
     * @param  metaClass        cid DOCUMENT ME!
     * @param  layerInfo        DOCUMENT ME!
     * @param  layerProperties  DOCUMENT ME!
     * @param  styles           DOCUMENT ME!
     */
    public CidsLayerFeature(final Map<String, Object> properties,
            final MetaClass metaClass,
            final CidsLayerInfo layerInfo,
            final LayerProperties layerProperties,
            final List<org.deegree.style.se.unevaluated.Style> styles) {
        super((Integer)properties.get(layerInfo.getIdField()),
            (Geometry)properties.get(layerInfo.getGeoField()),
            layerProperties,
            styles);
        this.metaClass = metaClass;
        this.layerInfo = layerInfo;
        this.addProperties(properties);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean getBean() {
        try {
            return getMetaObject().getBean();
        } catch (ConnectionException ex) {
            CidsLayerFeature.LOG.info("CidsBean could not be loaded, property is null", ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaObject  DOCUMENT ME!
     */
    public void setMetaObject(final MetaObject metaObject) {
        this.metaObject = metaObject;

        syncWithBean();
    }

    /**
     * fills the feature with the properties from its cids bean.
     */
    public void syncWithBean() {
        final CidsBean bean = metaObject.getBean();

        for (final String propName : bean.getPropertyNames()) {
            setProperty(propName, bean.getProperty(propName));

            if (propName.equals(layerInfo.getIdField())) {
                setId((Integer)bean.getProperty(propName));
            }
            if (propName.equals(layerInfo.getGeoField())) {
                // clone the geom object
                final Object geomObject = bean.getProperty(propName);

                if (geomObject instanceof CidsBean) {
                    final MetaObject mo = ((CidsBean)geomObject).getMetaObject().getMetaClass().getEmptyInstance();

                    for (final ObjectAttribute oa : mo.getAttribs()) {
                        if (!oa.isPrimaryKey() && !oa.isArray()) {
                            try {
                                mo.getBean()
                                        .setProperty(oa.getMai().getFieldName().toLowerCase(),
                                            ((CidsBean)geomObject).getProperty(
                                                oa.getMai().getFieldName().toLowerCase()));
                            } catch (Exception ex) {
                                LOG.error("Cannot copy attribute", ex);
                            }
                        }
                    }
                    setProperty(propName, mo.getBean());
                }
            }
        }
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        if ((styles == null) || styles.isEmpty()) {
            if ((getLayerProperties() != null) && (getLayerProperties().getAttributeTableRuleSet() != null)) {
                return getLayerProperties().getAttributeTableRuleSet().getPointAnnotationSymbol(this);
            } else {
                return this.getStyle().getPointSymbol();
            }
        } else {
            return null;
        }
    }

    @Override
    protected org.deegree.feature.Feature getDeegreeFeature() {
        return new CidSLayerDeegreeFeature();
    }

    @Override
    public boolean isPrimaryAnnotationVisible() {
        return false;
    }

    @Override
    public Object clone() {
        return new CidsLayerFeature(this);
    }

    @Override
    public Object getProperty(final String propertyName) {
        if ((propertyName != null) && !propertyName.isEmpty()) {
            if (propertyName.startsWith("original:")) {
                try {
                    return getMetaObject().getBean().getProperty(propertyName.substring("original:".length()));
                } catch (ConnectionException ex) {
                    CidsLayerFeature.LOG.info("CidsBean could not be loaded, property is null", ex);
                    return null;
                }
            }
        }

        if ((stations != null) && layerInfo.isStation(propertyName) && (stations.get(propertyName) != null)) {
            final TableStationEditor se = (TableStationEditor)stations.get(propertyName);
            return se.getValue();
        } else {
            return super.getProperty(propertyName);
        }
    }

    @Override
    public void setEditable(final boolean editable) {
        final boolean oldEditableStatus = isEditable();
        super.setEditable(editable);

        if (oldEditableStatus != editable) {
            modified = false;
            if (!editable && (stations != null)) {
                removeStations();

                if ((getLayerProperties().getAttributeTableRuleSet() != null)
                            && getLayerProperties().getAttributeTableRuleSet().isCatThree()) {
                    if (CismapBroker.getInstance().getMappingComponent().getFeatureCollection().isHoldFeature(this)) {
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unholdFeature(this);
                    }
                    if (CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(this) != null) {
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(this);
                    }
                }
            } else {
                if (CismapBroker.getInstance().getMappingComponent().getFeatureCollection().isHoldFeature(this)) {
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unholdFeature(this);
                }
                if (CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(this) != null) {
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(this);
                }
            }

            if (editable) {
                backupProperties = (HashMap)super.getProperties().clone();
                if (hasStations()) {
                    initStations();

                    if ((getLayerProperties().getAttributeTableRuleSet() != null)
                                && getLayerProperties().getAttributeTableRuleSet().isCatThree()) {
                        backupGeometry = (Geometry)getGeometry().clone();
                        backupProperties = (HashMap)super.getProperties().clone();
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                        SelectionManager.getInstance().addSelectedFeatures(Collections.nCopies(1, this));
//                        backgroundColor = new Color(255, 91, 0);
                    }
                } else {
                    if (getGeometry() != null) {
                        backupGeometry = (Geometry)getGeometry().clone();
                    }
                    backupProperties = (HashMap)super.getProperties().clone();
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                    SelectionManager.getInstance().addSelectedFeatures(Collections.nCopies(1, this));
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unselect(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addToSelection(this);
                    backgroundColor = new Color(255, 91, 0);
                }

                if (hasCatalogues()) {
                    for (int i = 0; i < layerInfo.getColumnNames().length; ++i) {
                        try {
                            final String col = layerInfo.getColumnNames()[i];
                            if (layerInfo.isCatalogue(col)) {
                                final int referencedForeignClassId = layerInfo.getCatalogueClass(col);

                                if (combos == null) {
                                    combos = new HashMap<String, DefaultCidsLayerBindableReferenceCombo>();
                                }
                                final MetaClass foreignClass = getMetaClass(referencedForeignClassId);
                                final DefaultCidsLayerBindableReferenceCombo catalogueEditor =
                                    new DefaultCidsLayerBindableReferenceCombo(
                                        foreignClass,
                                        true);
                                final String colName = layerInfo.getColumnPropertyNames()[i];
                                FeatureServiceFeature feature = null;
                                final CidsBean bean = (CidsBean)getMetaObject().getBean().getProperty(colName);

                                if (bean != null) {
                                    feature = retrieveFeature(bean.getPrimaryKeyValue(),
                                            bean.getMetaObject().getMetaClass());
                                }
                                catalogueEditor.setSelectedItem(feature);
                                combos.put(col, catalogueEditor);
                            }
                        } catch (Exception e) {
                            LOG.error("Error while receiving meta class", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void initStations() {
        try {
            for (int i = 0; i < layerInfo.getColumnNames().length; ++i) {
                final String col = layerInfo.getColumnNames()[i];

                if (layerInfo.isStation(col)) {
                    final String colName = layerInfo.getColumnPropertyNames()[i];
                    final CidsBean bean = (CidsBean)getMetaObject().getBean()
                                .getProperty(colName.substring(0, colName.indexOf(".")));
                    final StationInfo statInfo = layerInfo.getStationInfo(col);

                    if (bean == null) {
                        continue;
                    }

                    if (statInfo.isStationLine()) {
                        if (stations == null) {
                            stations = new HashMap<String, DisposableCidsBeanStore>();
                        }
                        TableLinearReferencedLineEditor st = (TableLinearReferencedLineEditor)stations.get(
                                String.valueOf(statInfo.getLineId()));

                        if (st == null) {
                            final String statField = colName.substring(0, colName.indexOf("."));
                            st = new TableLinearReferencedLineEditor(statInfo.getRouteTable(),
                                    this,
                                    statInfo.getRoutePropertyName());
                            st.setOtherLinesFrom(metaClass.getTableName());
                            st.setOtherLinesQuery(metaClass.getTableName() + "." + statField + " = ");

//                            if (bean == null) {
//                                final HashMap attribs = metaClass.getMemberAttributeInfos();
//                                final String statName = colName.substring(0, colName.indexOf("."));
//
//                                for (final Object key : attribs.keySet()) {
//                                    final MemberAttributeInfo attr = (MemberAttributeInfo)attribs.get(key);
//                                    final LinearReferencingHelper helper = FeatureRegistry.getInstance()
//                                                .getLinearReferencingSolver();
//
//                                    if (attr.getName().equalsIgnoreCase(statName)) {
//                                        final MetaClass lineClass = getMetaClass(
//                                                attr.getForeignKeyClassId());
//                                        bean = lineClass.getEmptyInstance().getBean();
//                                        final MetaClass stationClass = getMetaClass(bean.getMetaObject()
//                                                        .getAttributeByFieldName("von").getMai()
//                                                        .getForeignKeyClassId());
//                                        bean.setProperty("von", stationClass.getEmptyInstance().getBean());
//                                        bean.setProperty("bis", stationClass.getEmptyInstance().getBean());
//                                    }
//                                }
//                            }

                            st.setCidsBean(bean);
                            st.addPropertyChangeListener(propListener);
                            backgroundColor = st.getLineColor();

                            stations.put(String.valueOf(statInfo.getLineId()), st);
                        }

                        if (statInfo.isFromStation()) {
                            stations.put(col, st.getFromStation());
                        } else {
                            stations.put(col, st.getToStation());
                        }
                    } else {
                        if (stations == null) {
                            stations = new HashMap<String, DisposableCidsBeanStore>();
                        }
                        final String statField = colName.substring(0, colName.indexOf("."));
                        final TableStationEditor st = new TableStationEditor(statInfo.getRouteTable(),
                                this,
                                statInfo.getRoutePropertyName());
                        st.setOtherLinesFrom(metaClass.getTableName());
                        st.setOtherLinesQuery(metaClass.getTableName() + "." + statField + " = ");
                        st.setCidsBean(bean);
                        st.addPropertyChangeListener(propListener);
//                                        backgroundColor = st.getLineColor();

                        stations.put(col, st);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while retrieving meta object", e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void removeStations() {
        for (final String key : stations.keySet()) {
            final DisposableCidsBeanStore editor = stations.get(key);

            if (editor instanceof TableLinearReferencedLineEditor) {
                ((TableLinearReferencedLineEditor)editor).removePropertyChangeListener(propListener);
            }

            if (stations.get(key) != null) {
                stations.get(key).dispose();
            }
        }
        stations.clear();
    }

    /**
     * If the feature is in the edit mode and the given property references on a catalogue, this method returns the
     * catalogue object. Otherwise, the getProperty method will be invoked.
     *
     * @param   propertyName  the name of the property
     *
     * @return  the underlaying object
     */
    public Object getPropertyObject(final String propertyName) {
        if (layerInfo.isCatalogue(propertyName) && (getCatalogueCombo(propertyName) != null)) {
            return getCatalogueCombo(propertyName).getSelectedItem();
        } else {
            return getProperty(propertyName);
        }
    }

    /**
     * Adds the given property. This method uses a client object and a server object. In the most cases is the server
     * object a cidsBean and the client object is its representation on the client side (the value of a specific field
     * e.g.).
     *
     * @param  propertyName  the name of the property
     * @param  property      the value that is shown on the client side
     * @param  serverObject  the object, that is saved on the server side
     */
    public void addProperty(final String propertyName, final Object property, final Object serverObject) {
        super.addProperty(propertyName, property); // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
        super.setProperty(propertyName, propertyValue);

        if (isEditable()) {
            modified = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasStations() {
        final AttributeTableRuleSet ruleSet = getLayerProperties().getAttributeTableRuleSet();

        for (final String col : layerInfo.getColumnNames()) {
            if (layerInfo.isStation(col) && ((ruleSet == null) || ruleSet.isColumnEditable(col))) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasCatalogues() {
        for (final String col : layerInfo.getColumnNames()) {
            if (layerInfo.isCatalogue(col)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public FeatureServiceFeature saveChanges() throws Exception {
        saveChangesWithoutReload();

        final FeatureServiceFeature feature = reloadFeature();

        if (feature != null) {
            return feature;
        } else {
            return this;
        }
    }

    @Override
    public void saveChangesWithoutReload() throws Exception {
        final Map<String, Object> propertyMap = super.getProperties();
        final CidsBean bean = getMetaObject().getBean();
        final String[] cols = layerInfo.getColumnNames();
        final String[] props = layerInfo.getColumnPropertyNames();
        final HashMap<String, String> colMap = new HashMap<String, String>();
        final boolean hasAdditionalFields = hasAdditionalProperties();
        final AttributeTableRuleSet ruleSet = getLayerProperties().getAttributeTableRuleSet();

        for (int i = 0; i < cols.length; ++i) {
            colMap.put(cols[i], props[i]);
        }

        // to ensure that the geometries are set properly
        if (stations != null) {
            for (final String key : stations.keySet()) {
                final DisposableCidsBeanStore editor = stations.get(key);

                if (editor instanceof TableLinearReferencedLineEditor) {
                    ((TableLinearReferencedLineEditor)editor).recreateGeometry();
                }
            }
        }

        for (final String key : propertyMap.keySet()) {
            if (hasAdditionalFields && (ruleSet.getIndexOfAdditionalFieldName(key) != Integer.MIN_VALUE)) {
                // additional fields cannot be saved
                continue;
            }
            if (key.equalsIgnoreCase("id")) {
                // nothing to do. the id should not be changed
            } else if (layerInfo.isPrimitive(key)) {
                bean.setProperty(colMap.get(key), propertyMap.get(key));
            } else if ((layerInfo.getGeoField() != null) && layerInfo.getGeoField().equals(key)
                        && (colMap.get(key) != null)) {
                final String colName = colMap.get(key);
                if (
                    bean.getMetaObject().getAttributeByFieldName(
                                colMap.get(key).substring(0, colMap.get(key).indexOf("."))).getMai()
                            .getForeignKeyClassId()
                            == layerInfo.getReferencedCidsClass(key)) {
                    // there must be a direct reference to the geom table
                    if (layerInfo.isReferenceToCidsClass(key) && (bean.getProperty(key) == null)) {
                        // create a new object. Mostly, a new instance of geom is created
                        final CidsBean newGeoObject = getMetaClass(layerInfo.getReferencedCidsClass(key))
                                    .getEmptyInstance().getBean();
                        bean.setProperty(colName.substring(0, colName.indexOf(".")), newGeoObject);
                    }
                    Geometry geom = getGeometry();
                    if (geom != null) {
                        geom = CrsTransformer.transformToDefaultCrs(geom);
                        geom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                    }
                    bean.setProperty(colMap.get(key), geom);
                }
            } else if (layerInfo.isCatalogue(key)) {
                if (getCatalogueCombo(key) != null) {
                    if (getCatalogueCombo(key).getSelectedItem() instanceof CidsLayerFeature) {
                        bean.setProperty(colMap.get(key),
                            ((CidsLayerFeature)getCatalogueCombo(key).getSelectedItem()).getMetaObject().getBean());
                    } else {
                        bean.setProperty(colMap.get(key), getCatalogueCombo(key).getSelectedItem());
                    }
                } else {
                    // A new object was created
                    bean.setProperty(colMap.get(key), propertyMap.get(key));
                }
            } else if (layerInfo.isStation(key)) {
                final StationInfo info = layerInfo.getStationInfo(key);

                if (info.isStationLine()) {
                    if (stations != null) {
                        final DisposableCidsBeanStore store = stations.get(String.valueOf(info.getLineId()));
                        if (store != null) {
                            bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")),
                                store.getCidsBean());
                        } else {
                            bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")),
                                null);
                        }
                    } else {
                        if (getProperty(key) != null) {
                            bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")),
                                getProperty(key));
                        }
                    }
                } else {
                    if ((stations != null) && (stations.get(key) != null)) {
                        final DisposableCidsBeanStore store = stations.get(key);
                        bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")),
                            store.getCidsBean());
                    } else {
                        bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")), getProperty(key));
                    }
                }
            } else {
                String propKey = colMap.get(key);

                if (propKey == null) {
                    propKey = key;
                }

                if (propKey.contains(".")) {
                    continue;
                }
                bean.setProperty(propKey, propertyMap.get(key));
            }
        }
//LOG.error(bean.getMOString());
        final CidsBean newBean = bean.persist();

        if (newBean != null) {
            setId(newBean.getMetaObject().getID());
            setProperty("id", newBean.getMetaObject().getID());
        } else {
            setId(bean.getMetaObject().getID());
            setProperty("id", bean.getMetaObject().getID());
        }
        // to decrease the memory usage
        metaObject = null;

        fillBackupObjects();
    }

    /**
     * DOCUMENT ME!
     */
    private void fillBackupObjects() {
        backupGeometry = null;
        backupProperties = null;

        if (getGeometry() != null) {
            backupGeometry = (Geometry)getGeometry().clone();
        }
        backupProperties = (HashMap)super.getProperties().clone();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureServiceFeature reloadFeature() {
        try {
            String idField = "id";
            final String[] colNames = layerInfo.getColumnNames();

            for (int i = 0; i < colNames.length; ++i) {
                if (colNames[i].equalsIgnoreCase("id")) {
                    idField = layerInfo.getSqlColumnNames()[i];
                    break;
                }
            }

            final String query = idField + " = " + getId();
            final List<FeatureServiceFeature> features = getLayerProperties().getFeatureService()
                        .getFeatureFactory()
                        .createFeatures(query, null, null, 0, 1, null);

            if (features.size() == 1) {
                setProperties(features.get(0).getProperties());

                return features.get(0);
            }
        } catch (Exception e) {
            LOG.error("Error while reloading feature from server", e);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     * @param   mc  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureServiceFeature retrieveFeature(final int id, final MetaClass mc) {
        try {
            final String idField = "id";
            final CidsLayer service = new CidsLayer(mc);
            final String query = service.decoratePropertyName(idField) + " = " + id;
            service.initAndWait();
            final List<FeatureServiceFeature> features = service.getFeatureFactory()
                        .createFeatures(query, null, null, 0, 1, null);

            if (features.size() == 1) {
                return features.get(0);
            }
        } catch (Exception e) {
            LOG.error("Error while reloading feature from server", e);
        }

        return null;
    }

    @Override
    public void delete() throws Exception {
        final CidsBean bean = getMetaObject().getBean();
        bean.delete();
        bean.persist();
    }

    @Override
    public void restore() throws Exception {
        final CidsBean bean = getMetaObject().getBean();
        bean.getMetaObject().setStatus(MetaObject.NEW);
        bean.persist();
        metaObject = null;
    }

    @Override
    public boolean hasWritePermissions() {
//        try {
        final User usr = SessionManager.getSession().getUser();
        boolean groupPermission = false;
        final PermissionHolder ph = metaClass.getPermissions();

        if (ph != null) {
            groupPermission = ph.hasWritePermission(usr);
        }
        if (groupPermission) {
            final Class cl = ClassloadingHelper.getDynamicClass(
                    metaClass,
                    ClassloadingHelper.CLASS_TYPE.PERMISSION_PROVIDER);

            if (cl != null) {
                final Object o;
                try {
                    o = cl.newInstance();

                    if (o instanceof CidsLayerPermissionProvider) {
                        final CidsLayerPermissionProvider permissionProvider = (CidsLayerPermissionProvider)o;
                        return permissionProvider.getCustomCidsLayerWritePermissionDecisionforUser(usr, this);
                    }
                } catch (Exception ex) {
                    LOG.error("Error while checking write permission", ex);
                    return false;
                }
            }

            return true;
//                return getMetaObject().hasObjectWritePermission(usr);
        } else {
            return false;
        }
//        } catch (ConnectionException e) {
//            LOG.error("Error during permission determination", e);
//            return false;
//        }
    }

    @Override
    public boolean hasReadPermissions() {
        return true;
    }

    /**
     * The meta object of this feature. If the meta object is not loaded from the server, yet, it will be loaded.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConnectionException  DOCUMENT ME!
     */
    private MetaObject getMetaObject() throws ConnectionException {
        if (metaObject == null) {
            if (CidsLayerFeature.this.getId() < 0) {
                // This is a new Object
                metaObject = metaClass.getEmptyInstance();

                copyFeaturePropertiesToMetaObject();
            } else {
                metaObject = SessionManager.getConnection()
                            .getMetaObject(SessionManager.getSession().getUser(),
                                    CidsLayerFeature.this.getId(),
                                    metaClass.getID(),
                                    SessionManager.getSession().getUser().getDomain());

                if (metaObject == null) {
                    metaObject = metaClass.getEmptyInstance();
                }
            }
        }

        return metaObject;
    }

    /**
     * Copies all properties of the feature into the bean. This is required, if the feature is new, so that no cids bean
     * exists on the server.
     */
    private void copyFeaturePropertiesToMetaObject() {
        final CidsBean bean = metaObject.getBean();
        final Map<Integer, CidsBean> stationLines = new HashMap<Integer, CidsBean>();

        try {
            for (final String colName : layerInfo.getColumnNames()) {
                if (layerInfo.isCatalogue(colName)) {
                    final Object propValue = getProperty(colName);

                    if (propValue != null) {
                        final MemberAttributeInfo attr = getMemberAttribute(colName);

                        if (attr != null) {
                            final int foereignKeyClass = attr.getForeignKeyClassId();
                            final CidsBean catval = getCatalogueElement(metaClass.getDomain(),
                                    foereignKeyClass,
                                    String.valueOf(propValue));
                            bean.setProperty(colName, catval);
                        }
                    }
                } else if (layerInfo.isStation(colName)) {
                    final StationInfo info = layerInfo.getStationInfo(colName);
                    final Object valueObject = getProperty(colName);

                    if (!(valueObject instanceof Double)) {
                        continue;
                    }

                    final Double value = (Double)valueObject;
                    final Object routeNameObject = getProperty(info.getRoutePropertyName());

                    if ((value == null) || (routeNameObject == null)) {
                        // station bean cannot be created
                        continue;
                    }

                    if (info.isStationLine()) {
                        CidsBean firstStation = stationLines.get(info.getLineId());

                        if (firstStation == null) {
                            final LinearReferencingHelper helper = FeatureRegistry.getInstance()
                                        .getLinearReferencingSolver();
                            final CidsBean secondStation = createStationBean(info, value);
                            CidsBean line = null;

                            if (info.isFromStation()) {
                                line = helper.createLineBeanFromStationBean(secondStation, firstStation);
                            } else {
                                line = helper.createLineBeanFromStationBean(firstStation, secondStation);
                            }

                            bean.setProperty(colName, line);
                        } else {
                            firstStation = createStationBean(info, value);
                            stationLines.put(info.getLineId(), firstStation);
                        }
                    } else {
                        bean.setProperty(colName, createStationBean(info, value));
                    }
                } else if (layerInfo.isPrimitive(colName)) {
                    bean.setProperty(colName, getProperty(colName));
                }
            }
        } catch (Exception e) {
            LOG.error("Error while setting bean properties", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   info   DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean createStationBean(final StationInfo info, final Double value) {
        final String routeClass = info.getRouteTable();
        final Object routeNameObject = getProperty(info.getRoutePropertyName());
        final LinearReferencingHelper helper = FeatureRegistry.getInstance().getLinearReferencingSolver();
        final String domain = helper.getDomainOfRouteTable(routeClass)[0];
        final String routeNameProperty = helper.getRouteNamePropertyFromRouteByClassName(routeClass);
        CidsBean routeBean = null;

        routeBean = getRouteBean(domain, routeClass, routeNameProperty, routeNameObject);

        return helper.createStationBeanFromRouteBean(routeBean, value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attrName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MemberAttributeInfo getMemberAttribute(final String attrName) {
        final HashMap attrMap = metaClass.getMemberAttributeInfos();

        for (final Object key : attrMap.keySet()) {
            final Object attrInfoObject = attrMap.get(key);

            if (attrInfoObject instanceof MemberAttributeInfo) {
                final MemberAttributeInfo attr = (MemberAttributeInfo)attrInfoObject;

                if (attr.getName().equals(attrName)) {
                    return attr;
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain   DOCUMENT ME!
     * @param   classId  DOCUMENT ME!
     * @param   value    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getCatalogueElement(final String domain, final int classId, final String value) {
        final MetaClass mc = ClassCacheMultiple.getMetaClass(domain, classId);

        final String query = "select " + mc.getID() + ", " + mc.getPrimaryKey() + " from " + mc.getTableName(); // NOI18N

        try {
            final MetaObject[] mos = SessionManager.getConnection()
                        .getMetaObjectByQuery(SessionManager.getSession().getUser(), query);

            if ((mos != null) && (mos.length > 0)) {
                for (final MetaObject object : mos) {
                    if (object.getBean().toString().equals(value)) {
                        return object.getBean();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot load catalogue data", e);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain             DOCUMENT ME!
     * @param   routeTable         DOCUMENT ME!
     * @param   routeNameProperty  DOCUMENT ME!
     * @param   routeName          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getRouteBean(final String domain,
            final String routeTable,
            final String routeNameProperty,
            final Object routeName) {
        final MetaClass mc = ClassCacheMultiple.getMetaClass(domain, routeTable);

        final String route = ((routeName instanceof String) ? ("'" + routeName + "'") : String.valueOf(routeName));
        String query = "select " + mc.getID() + ", " + mc.getPrimaryKey() + " from " + mc.getTableName(); // NOI18N
        query += " where " + routeNameProperty + "=" + route;

        try {
            final MetaObject[] mos = SessionManager.getConnection()
                        .getMetaObjectByQuery(SessionManager.getSession().getUser(), query);

            if ((mos != null) && (mos.length > 0)) {
                return mos[0].getBean();
            }
        } catch (Exception e) {
            LOG.error("Cannot load catalogue data", e);
        }

        return null;
    }

    @Override
    public void undoAll() {
        if (backupProperties != null) {
            super.setProperties((HashMap)backupProperties.clone());
        }

        if (backupGeometry != null) {
            setGeometry((Geometry)backupGeometry.clone());
        }
        final PFeature feature = CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(this);
        if (feature != null) {
            feature.visualize();
        }

        if (stations != null) {
            for (final String key : stations.keySet()) {
                final DisposableCidsBeanStore editor = stations.get(key);

                if (editor instanceof TableLinearReferencedLineEditor) {
                    ((TableLinearReferencedLineEditor)editor).undoChanges();
                } else if (editor instanceof TableStationEditor) {
                    ((TableStationEditor)editor).undoChanges();
                }
            }
        }
    }

    @Override
    public void setGeometry(final Geometry geom) {
        final Geometry oldGeom = getGeometry();

        if (((oldGeom == null) != (geom == null))
                    || ((oldGeom != null) && (geom != null)
                        && (!oldGeom.getEnvelope().equalsExact(geom.getEnvelope()) || !oldGeom.equalsExact(geom)))) {
            // the old geometry and the new geometry are different
            super.setGeometry(geom);

            if (layerInfo != null) {
                super.addProperty(layerInfo.getGeoField(), geom);
            }

            if (isEditable()) {
                modified = true;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsLayerInfo getLayerInfo() {
        return layerInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TableStationEditor getStationEditor(final String columnName) {
        if (stations == null) {
            return null;
        }

        final DisposableCidsBeanStore store = stations.get(columnName);

        if (store instanceof TableStationEditor) {
            return (TableStationEditor)store;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lineId  columnName DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TableLinearReferencedLineEditor getLineEditorEditor(final int lineId) {
        if (stations == null) {
            return null;
        }

        final DisposableCidsBeanStore store = stations.get(String.valueOf(lineId));

        if (store instanceof TableLinearReferencedLineEditor) {
            return (TableLinearReferencedLineEditor)store;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo getCatalogueCombo(final String columnName) {
        if (combos == null) {
            return null;
        }
        final DefaultCidsLayerBindableReferenceCombo c = combos.get(columnName);

        return c;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CidsLayerFeature) {
            final CidsLayerFeature other = (CidsLayerFeature)obj;

            if ((getId() != -1) || (other.getId() != -1)) {
                return metaClass.getTableName().equals(other.metaClass.getTableName()) && (getId() == other.getId());
            } else {
                return obj == other;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (83 * hash) + this.getId();
        hash = (83 * hash)
                    + (((this.metaClass != null) && (metaClass.getTableName() != null))
                        ? this.metaClass.getTableName().hashCode() : 0);
        return hash;
    }

    /**
     * The delivered meta class is always from the same domain as the meta object of this feature.
     *
     * @param   classId  the id of the meta class
     *
     * @return  the meta class object of the class with the given class id
     *
     * @throws  ConnectionException  DOCUMENT ME!
     */
    private MetaClass getMetaClass(final int classId) throws ConnectionException {
        return ClassCacheMultiple.getMetaClass(metaClass.getDomain(), classId);
//        return SessionManager.getConnection()
//                    .getMetaClass(SessionManager.getSession().getUser(),
//                        classId,
//                        metaClass.getDomain());
    }

    @Override
    public boolean isFeatureChanged() {
        final Geometry geom = getGeometry();

        if (((backupGeometry == null) != (geom == null))
                    || ((backupGeometry != null) && (geom != null) && !backupGeometry.equalsExact(geom))) {
            // The geometry will not changed with the setGeometry() method, but also within the geometry object itself.
            return true;
        } else {
            return modified;
        }
    }

    @Override
    public String toString() {
        final ToStringConverter converter = metaClass.getToStringConverter();

        if (converter instanceof CidsLayerFeatureToStringConverter) {
            final CidsLayerFeatureToStringConverter featureConverter = (CidsLayerFeatureToStringConverter)converter;

            return featureConverter.featureToString(this);
        } else {
            return super.toString();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class PImageWithDisplacement extends PImage implements PSticky {

        //~ Instance fields ----------------------------------------------------

        double displacementX;
        double displacementY;
        private SLDStyledFeature.UOM uom = UOM.metre;
        private double anchorPointX;
        private double anchorPointY;
        private WorldToScreenTransform wtst;
        private double scaledDisplacementX;
        private double scaledDisplacementY;
        private double oldScaledDisplacementX;
        private double oldScaledDisplacementY;
        private PCamera camera;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PImageWithDisplacement object.
         */
        public PImageWithDisplacement() {
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  uom  DOCUMENT ME!
         */
        public void setUOM(final SLDStyledFeature.UOM uom) {
            this.uom = uom;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  uomFromDeegree  DOCUMENT ME!
         * @param  displacementX   DOCUMENT ME!
         * @param  displacementY   DOCUMENT ME!
         * @param  anchorPointX    DOCUMENT ME!
         * @param  anchorPointY    DOCUMENT ME!
         * @param  wtst            DOCUMENT ME!
         * @param  camera          DOCUMENT ME!
         */
        public void setDisplacement(final SLDStyledFeature.UOM uomFromDeegree,
                final double displacementX,
                final double displacementY,
                final double anchorPointX,
                final double anchorPointY,
                final WorldToScreenTransform wtst,
                final PCamera camera) {
            this.uom = uomFromDeegree;
            this.displacementX = displacementX;
            this.displacementY = displacementY;
            this.anchorPointX = anchorPointX;
            this.anchorPointY = anchorPointY;
            this.wtst = wtst;
            this.camera = camera;
        }

        @Override
        public void setScale(final double scale) {
            if (uom != UOM.pixel) {
                super.setScale(scale);
            } else {
                // if(scale > 1.0f) {
                // offset(-scaledDisplacementX, -scaledDisplacementY);
                super.setScale(scale);
                /*final double w = this.getWidth();
                 *final double h = this.getHeight();*/
                // double cameraScale = camera.getScale();
                // double inverted = 1d / cameraScale;
                oldScaledDisplacementX = scaledDisplacementX;
                oldScaledDisplacementY = scaledDisplacementY;
                scaledDisplacementX = (displacementX /*- anchorPointX * w*/) * scale;
                // scaledDisplacementY = (displacementY /*+ anchorPointY * h*/)/* * scale*/;
                offset(scaledDisplacementX - oldScaledDisplacementX, scaledDisplacementY - oldScaledDisplacementY);
                // }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   o  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int compareTo(final Object o) {
            return toString().compareTo(o.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class CidSLayerDeegreeFeature extends DeegreeFeature {

        //~ Methods ------------------------------------------------------------

        @Override
        public org.deegree.feature.types.FeatureType getType() {
            return new DeegreeFeatureType() {

                    @Override
                    public QName getName() {
                        return new QName(metaClass.getTableName()); // To change body of generated methods, choose Tools
                        // | Templates.
                    }
                };
        }

        /*@Override
         * public List<Property> getProperties(final QName qname) { if ("original".equalsIgnoreCase(qname.getPrefix()))
         * { final List<Property> deegreeProperties = new LinkedList(); final Object value; try { if (metaObject ==
         * null) { metaObject = SessionManager.getConnection() .getMetaObject(SessionManager.getSession().getUser(),
         * CidsLayerFeature.this.getId(), (Integer) CidsLayerFeature.this.getProperty(CidsLayerFeature.CLASS_ID),
         * SessionManager.getSession().getUser().getDomain()); } value =
         * metaObject.getBean().getProperty(qname.getLocalPart()); if (value == null) { deegreeProperties.add(null); }
         * else { deegreeProperties.add(new DeegreeProperty(qname, value)); } } catch (ConnectionException ex) {
         * CidsLayerFeature.LOG.info("CidsBean could not be loaded, property is null", ex); deegreeProperties.add(null);
         * } return deegreeProperties; } else { return super.getProperties(qname); } }*/
    }
}
