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

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.permission.PermissionHolder;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.nodes.PImage;

import org.apache.log4j.Logger;

import java.awt.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.editors.DefaultBindableReferenceCombo;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.cidslayer.StationInfo;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.features.SLDStyledFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PSticky;
import de.cismet.cismap.commons.interaction.CismapBroker;

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
    private Map<String, DefaultBindableReferenceCombo> combos = null;
    private Color backgroundColor;
    private PropertyChangeListener propListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        };

    private HashMap backupProperties;
    private Geometry backupGeometry;

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
                    return getMetaObject().getBean().getProperty(propertyName);
                } catch (ConnectionException ex) {
                    CidsLayerFeature.LOG.info("CidsBean could not be loaded, property is null", ex);
                    return null;
                }
            }
        }

        if (layerInfo.isStation(propertyName) && (stations != null) && (stations.get(propertyName) != null)) {
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
            if (!editable && (stations != null)) {
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
                    try {
                        for (int i = 0; i < layerInfo.getColumnNames().length; ++i) {
                            final String col = layerInfo.getColumnNames()[i];
                            if (layerInfo.isStation(col)) {
                                final StationInfo statInfo = layerInfo.getStationInfo(col);

                                if (statInfo.isStationLine()) {
                                    if (stations == null) {
                                        stations = new HashMap<String, DisposableCidsBeanStore>();
                                    }
                                    TableLinearReferencedLineEditor st = (TableLinearReferencedLineEditor)stations.get(
                                            String.valueOf(statInfo.getLineId()));

                                    if (st == null) {
                                        final String colName = layerInfo.getColumnPropertyNames()[i];
                                        final CidsBean bean = (CidsBean)getMetaObject().getBean()
                                                    .getProperty(colName.substring(0, colName.indexOf(".")));
                                        st = new TableLinearReferencedLineEditor(statInfo.getRouteTable());
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
                                    final String colName = layerInfo.getColumnPropertyNames()[i];
                                    final CidsBean bean = (CidsBean)getMetaObject().getBean()
                                                .getProperty(colName.substring(0, colName.indexOf(".")));
                                    final TableStationEditor st = new TableStationEditor(statInfo.getRouteTable());
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

                    if ((getLayerProperties().getAttributeTableRuleSet() != null)
                                && getLayerProperties().getAttributeTableRuleSet().isCatThree()) {
                        backupGeometry = (Geometry)getGeometry().clone();
                        backupProperties = (HashMap)super.getProperties().clone();
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().select(this);
//                        backgroundColor = new Color(255, 91, 0);
                    }
                } else {
                    backupGeometry = (Geometry)getGeometry().clone();
                    backupProperties = (HashMap)super.getProperties().clone();
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().select(this);
                    backgroundColor = new Color(255, 91, 0);
                }

                if (hasCatalogues()) {
                    for (int i = 0; i < layerInfo.getColumnNames().length; ++i) {
                        try {
                            final String col = layerInfo.getColumnNames()[i];
                            if (layerInfo.isCatalogue(col)) {
                                final int referencedForeignClassId = layerInfo.getCatalogueClass(col);

                                if (combos == null) {
                                    combos = new HashMap<String, DefaultBindableReferenceCombo>();
                                }
                                final MetaClass foreignClass = getMetaClass(referencedForeignClassId);
                                final DefaultBindableReferenceCombo catalogueEditor = new DefaultBindableReferenceCombo(
                                        foreignClass,
                                        true,
                                        false);
                                final String colName = layerInfo.getColumnPropertyNames()[i];
                                final CidsBean bean = (CidsBean)getMetaObject().getBean().getProperty(colName);
                                catalogueEditor.setSelectedItem(bean);
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
     * If the feature is in the edit mode and the given property references on a catalogue, this method returns the
     * catalogue object. Otherwise, the getProperty method will be invoked.
     *
     * @param   propertyName  the name of the property
     *
     * @return  the underlaying object
     */
    public Object getPropertyObject(final String propertyName) {
        if (layerInfo.isCatalogue(propertyName)) {
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasStations() {
        for (final String col : layerInfo.getColumnNames()) {
            if (layerInfo.isStation(col)) {
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
    public void saveChanges() throws Exception {
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

        for (final String key : propertyMap.keySet()) {
            if (hasAdditionalFields && (ruleSet.getIndexOfAdditionalFieldName(key) != Integer.MIN_VALUE)) {
                // additional fields cannot be saved
                continue;
            }
            if (key.equalsIgnoreCase("id")) {
                // nothing to do. the id should not be changed
            } else if (layerInfo.isPrimitive(key)) {
                bean.setProperty(colMap.get(key), propertyMap.get(key));
            } else if (layerInfo.getGeoField().equals(key) && (colMap.get(key) != null)) {
                if (layerInfo.isReferenceToCidsClass(key) && (bean.getProperty(key) == null)) {
                    // create a new object. Mostly, a new instance of geom is created
                    final String colName = colMap.get(key);
                    final CidsBean newGeoObject = getMetaClass(layerInfo.getReferencedCidsClass(key)).getEmptyInstance()
                                .getBean();
                    bean.setProperty(colName.substring(0, colName.indexOf(".")), newGeoObject);
                }
                Geometry geom = getGeometry();
                geom = CrsTransformer.transformToDefaultCrs(geom);
                geom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                bean.setProperty(colMap.get(key), geom);
            } else if (layerInfo.isCatalogue(key)) {
                if (getCatalogueCombo(key) != null) {
                    bean.setProperty(colMap.get(key), getCatalogueCombo(key).getSelectedItem());
                } else {
                    // A new object was created
                    bean.setProperty(colMap.get(key), propertyMap.get(key));
                }
            } else if (layerInfo.isStation(key)) {
                final StationInfo info = layerInfo.getStationInfo(key);

                if (info.isStationLine()) {
                    if (stations != null) {
                        final DisposableCidsBeanStore store = stations.get(String.valueOf(info.getLineId()));
                        bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")),
                            store.getCidsBean());
                    } else {
                        if (getProperty(key) != null) {
                            bean.setProperty(colMap.get(key).substring(0, colMap.get(key).indexOf(".")),
                                getProperty(key));
                        }
                    }
                } else {
                    if (stations != null) {
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
//                bean.setProperty(propKey, propertyMap.get(key));
            }
        }
//LOG.error(bean.getMOString());
        final CidsBean savedBean = bean.persist();
        setId(savedBean.getMetaObject().getID());
        setProperty("id", savedBean.getMetaObject().getID());
        metaObject = savedBean.getMetaObject();
    }

    @Override
    public void delete() throws Exception {
        final CidsBean bean = getMetaObject().getBean();
        bean.delete();
        bean.persist();
    }

    @Override
    public boolean hasWritePermissions() {
        try {
            final User usr = SessionManager.getSession().getUser();
            boolean groupPermission = false;
            final PermissionHolder ph = getMetaObject().getMetaClass().getPermissions();

            if (ph != null) {
                groupPermission = ph.hasWritePermission(usr);
            }

            if (groupPermission) {
                return getMetaObject().hasObjectWritePermission(usr);
            } else {
                return false;
            }
        } catch (ConnectionException e) {
            LOG.error("Error during permission determination", e);
            return false;
        }
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
            metaObject = SessionManager.getConnection()
                        .getMetaObject(SessionManager.getSession().getUser(),
                                CidsLayerFeature.this.getId(),
                                metaClass.getID(),
                                SessionManager.getSession().getUser().getDomain());

            if (metaObject == null) {
                metaObject = metaClass.getEmptyInstance();
            }
        }

        return metaObject;
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
        super.setGeometry(geom);

        if (layerInfo != null) {
            super.addProperty(layerInfo.getGeoField(), geom);
        }
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
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DefaultBindableReferenceCombo getCatalogueCombo(final String columnName) {
        if (combos == null) {
            return null;
        }
        final DefaultBindableReferenceCombo c = combos.get(columnName);

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
        return SessionManager.getConnection()
                    .getMetaClass(SessionManager.getSession().getUser(),
                        classId,
                        metaClass.getDomain());
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
