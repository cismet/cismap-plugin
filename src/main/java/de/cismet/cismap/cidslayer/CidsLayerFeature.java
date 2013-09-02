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

import java.awt.Paint;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.SLDStyledFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
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
     * @param  style            DOCUMENT ME!
     */
    public CidsLayerFeature(final Map<String, Object> properties,
            final MetaClass metaClass,
            final LayerProperties layerProperties,
            final org.deegree.style.se.unevaluated.Style style) {
        super((Integer)properties.get(OBJECT_ID), (Geometry)properties.get(GEOMETRIE), layerProperties, style);
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

    @Override
    protected Feature getDeegreeFeature() {
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
    public Paint getLinePaint() {
        return null;
            /*
             * if (stylings.getFirst().first instanceof PolygonStyling) { return ((PolygonStyling)
             * stylings.getFirst().first).stroke.color; } return super.getLinePaint();
             */
    }

    @Override
    public int getLineWidth() {
        return 0;
            /*
             * if(stylings.getFirst().first instanceof PolygonStyling) { return
             * (int)((PolygonStyling)stylings.getFirst().first).stroke.width; }return super.getLineWidth();*/
    }

    @Override
    public Paint getFillingPaint() {
        return
            null; /*
                   * if(stylings.getFirst().first instanceof PolygonStyling) { return
                   * ((PolygonStyling)stylings.getFirst().first).fill.color; }return super.getFillingPaint();*/
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

        @Override
        public List<Property> getProperties(final QName qname) {
            if ("original".equalsIgnoreCase(qname.getPrefix())) {
                final List<Property> deegreeProperties = new LinkedList();
                final Object value;
                try {
                    if (metaObject == null) {
                        metaObject = SessionManager.getConnection()
                                    .getMetaObject(SessionManager.getSession().getUser(),
                                            CidsLayerFeature.this.getId(),
                                            (Integer)CidsLayerFeature.this.getProperty(CidsLayerFeature.CLASS_ID),
                                            SessionManager.getSession().getUser().getDomain());
                    }
                    value = metaObject.getBean().getProperty(qname.getLocalPart());
                    if (value == null) {
                        deegreeProperties.add(null);
                    } else {
                        deegreeProperties.add(new DeegreeProperty(qname, value));
                    }
                } catch (ConnectionException ex) {
                    CidsLayerFeature.LOG.info("CidsBean could not be loaded, property is null", ex);
                    deegreeProperties.add(null);
                }
                return deegreeProperties;
            } else {
                return super.getProperties(qname);
            }
        }
    }
}
