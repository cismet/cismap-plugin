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

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.nodes.PImage;

import org.apache.log4j.Logger;
import org.apache.xerces.xs.XSElementDeclaration;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.Feature;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;

import org.jfree.util.Log;


import java.awt.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.cidslayer.StationInfo;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.SLDStyledFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
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
public class CidsLayerFeature extends DefaultFeatureServiceFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static transient Logger LOG = Logger.getLogger(CidsLayerFeature.class);

    //~ Instance fields --------------------------------------------------------

    // private final int classId;
    private MetaObject metaObject;
    private MetaClass metaClass;
    // protected Map<String, Object> properties;
    private CidsLayerInfo layerInfo;
    private Map<String, DisposableCidsBeanStore> stations = null;
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
        } else {
            /*final ExecutorService executor = CismetConcurrency.getInstance("CidsLayerFeature").getDefaultExecutor();
             * executor.execute(new Runnable() {
             *
             * @Override public void run() { try { metaObject = SessionManager.getConnection() .getMetaObject(
             * SessionManager.getSession().getUser(), getId(), (Integer)properties.get(CLASS_ID),
             * SessionManager.getSession().getUser().getDomain()); LOG.info("MetaObject geladen: " + getId()); } catch
             * (ConnectionException ex) { LOG.error("Could not load the metaObject", ex); } } });*/
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
        // this.properties = new HashMap<String, Object>(properties);
        // this.style = style;

        /*} catch (ConnectionException ex) {
         *  Exceptions.printStackTrace(ex); this.stylings = style.evaluate(null, null);}*/

        /*final ExecutorService executor = CismetConcurrency.getInstance("CidsLayerFeature").getDefaultExecutor();
         * executor.execute(new Runnable() {
         *
         * @Override public void run() { try { metaObject = SessionManager.getConnection() .getMetaObject(
         * SessionManager.getSession().getUser(), getId(), (Integer)properties.get(CLASS_ID),
         * SessionManager.getSession().getUser().getDomain()); LOG.info( "MetaObject geladen: " + getId()); } catch
         * (ConnectionException ex) { LOG.error("Could not load the metaObject", ex); } } });*/
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean getBean() {
        if (metaObject == null) {
            try {
                if (metaObject == null) {
                    metaObject = SessionManager.getConnection()
                                .getMetaObject(SessionManager.getSession().getUser(),
                                        CidsLayerFeature.this.getId(),
                                        (Integer)metaClass.getID(),
                                        SessionManager.getSession().getUser().getDomain());
                }
            } catch (ConnectionException ex) {
                CidsLayerFeature.LOG.info("CidsBean could not be loaded, property is null", ex);
                return null;
            }
        }
        return metaObject.getBean();
    }
    @Override
    protected org.deegree.feature.Feature getDeegreeFeature() {
        return new CidSLayerDeegreeFeature();
    }

    /*@Override
     * public FeatureAnnotationSymbol getPointAnnotationSymbol() { return annotation; }
     */
    @Override
    public boolean isPrimaryAnnotationVisible() {
        return false; // To change body of generated methods, choose Tools | Templates.
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
                    if (metaObject == null) {
                        metaObject = SessionManager.getConnection()
                                    .getMetaObject(SessionManager.getSession().getUser(),
                                            CidsLayerFeature.this.getId(),
                                            metaClass.getID(),
                                            SessionManager.getSession().getUser().getDomain());
                    }
                    return metaObject.getBean().getProperty(propertyName);
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
                    stations.get(key).dispose();
                }
                stations.clear();
            } else {
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unholdFeature(this);
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(this);
            }

            if (editable && hasStations()) {
                backupProperties = (HashMap)super.getProperties().clone();
                try {
                    if (metaObject == null) {
                        metaObject = SessionManager.getConnection()
                                    .getMetaObject(SessionManager.getSession().getUser(),
                                            CidsLayerFeature.this.getId(),
                                            metaClass.getID(),
                                            SessionManager.getSession().getUser().getDomain());
                    }

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
                                    final CidsBean bean = (CidsBean)metaObject.getBean()
                                                .getProperty(layerInfo.getColumnPropertyNames()[i]);
                                    st = new TableLinearReferencedLineEditor();
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
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error while retrieving meta object", e);
                }
            } else if (editable) {
                backupGeometry = (Geometry)getGeometry().clone();
                backupProperties = (HashMap)super.getProperties().clone();
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
                backgroundColor = new Color(255, 91, 0);
            }
        }
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

    @Override
    public void saveChanges() throws Exception {
        if (metaObject == null) {
            metaObject = SessionManager.getConnection()
                        .getMetaObject(SessionManager.getSession().getUser(),
                                CidsLayerFeature.this.getId(),
                                metaClass.getID(),
                                SessionManager.getSession().getUser().getDomain());
        }

        final Map<String, Object> propertyMap = super.getProperties();
        final CidsBean bean = metaObject.getBean();
        final String[] cols = layerInfo.getColumnNames();
        final String[] props = layerInfo.getColumnPropertyNames();
        final HashMap<String, String> colMap = new HashMap<String, String>();

        for (int i = 0; i < cols.length; ++i) {
            colMap.put(cols[i], props[i]);
        }

        for (final String key : propertyMap.keySet()) {
            if (layerInfo.isPrimitive(key)) {
                bean.setProperty(colMap.get(key), propertyMap.get(key));
            } else if (layerInfo.getGeoField().equals(key) && (colMap.get(key) != null)) {
                bean.setProperty(colMap.get(key), getGeometry());
            }
        }

        bean.persist();
    }

    @Override
    public void undoAll() {
        super.setProperties((HashMap)backupProperties.clone());
        if (backupGeometry != null) {
            setGeometry((Geometry)backupGeometry.clone());
        }
        final PFeature feature = CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(this);
        if (feature != null) {
            feature.visualize();
        }

        for (final String key : stations.keySet()) {
            final DisposableCidsBeanStore editor = stations.get(key);

            if (editor instanceof TableLinearReferencedLineEditor) {
                ((TableLinearReferencedLineEditor)editor).undoChanges();
            } else if (editor instanceof TableStationEditor) {
                ((TableStationEditor)editor).undoChanges();
            }
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
     * @return  DOCUMENT ME!
     */
    public Color getBackgroundColor() {
        return backgroundColor;
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
