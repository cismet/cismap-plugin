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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.SLDStyledFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.piccolo.PSticky;

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
     * @param  layerProperties  DOCUMENT ME!
     * @param  styles           DOCUMENT ME!
     */
    public CidsLayerFeature(final Map<String, Object> properties,
            final MetaClass metaClass,
            final LayerProperties layerProperties,
            final List<org.deegree.style.se.unevaluated.Style> styles) {
        super((Integer)properties.get(OBJECT_ID), (Geometry)properties.get(GEOMETRIE), layerProperties, styles);
        this.metaClass = metaClass;
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
    public static String getCLASS_ID() {
        return CLASS_ID;
    }

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
        return super.getProperty(propertyName);
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

        final Map<String, Object> map = super.getProperties();
        final CidsBean bean = metaObject.getBean();

        for (final String key : map.keySet()) {
            if (key.equals(OBJECT_ID) || key.equals(GEOMETRIE)) {
                continue;
            }
            bean.setProperty(key, map.get(key));
        }

        bean.persist();
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
