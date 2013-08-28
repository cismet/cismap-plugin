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
import edu.umd.cs.piccolo.nodes.PPath;

import org.apache.log4j.Logger;
import org.apache.xerces.xs.XSElementDeclaration;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;

import org.jfree.util.Log;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.SLDStyledFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.piccolo.CustomFixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PSticky;

import static org.deegree.style.styling.components.Font.Style.OBLIQUE;
import static org.deegree.style.styling.components.UOM.Foot;
import static org.deegree.style.styling.components.UOM.Metre;
import static org.deegree.style.styling.components.UOM.Pixel;
import static org.deegree.style.styling.components.UOM.mm;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerFeature extends DefaultFeatureServiceFeature implements SLDStyledFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static transient Logger LOG = Logger.getLogger(CidsLayerFeature.class);
    private static final String OBJECT_ID = "object_id";
    private static final String CLASS_ID = "class_id";
    private static final String GEOMETRIE = "geo_field";

    //~ Instance fields --------------------------------------------------------

    // private final int classId;
    private MetaObject metaObject;
    private MetaClass metaClass;
    private LinkedList<org.deegree.commons.utils.Triple<org.deegree.style.styling.Styling, LinkedList<org.deegree
                        .geometry.Geometry>, String>> stylings;
    private Map<String, Object> properties;
    private org.deegree.filter.XPathEvaluator<org.deegree.feature.Feature> evaluator;
    private final Style style;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerFeature object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public CidsLayerFeature(final CidsLayerFeature feature) {
        super(feature);
        properties = new HashMap<String, Object>(feature.properties);
        // classId = feature.classId;
        metaClass = feature.metaClass;
        style = feature.style;
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
     * @param   properties       oid DOCUMENT ME!
     * @param   metaClass        cid DOCUMENT ME!
     * @param   layerProperties  DOCUMENT ME!
     * @param   style            DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public CidsLayerFeature(final Map<String, Object> properties,
            final MetaClass metaClass,
            final LayerProperties layerProperties,
            final org.deegree.style.se.unevaluated.Style style) {
        super((Integer)properties.get(OBJECT_ID), (Geometry)properties.get(GEOMETRIE), layerProperties);
        this.metaClass = metaClass;
        this.properties = new HashMap<String, Object>(properties);
        this.style = style;
        // classId = cid;
        // try {
        /*metaObject = SessionManager.getConnection()
         *   .getMetaObject(SessionManager.getSession().getUser(), getId(), (Integer) properties.get(CLASS_ID),
         * SessionManager.getSession().getUser().getDomain());*/

        /*} catch (ConnectionException ex) {
         *  Exceptions.printStackTrace(ex); this.stylings = style.evaluate(null, null);}*/
        evaluator = new org.deegree.filter.XPathEvaluator<org.deegree.feature.Feature>() {

                @Override
                public TypedObjectNode[] eval(final Feature t, final ValueReference vr)
                        throws FilterEvaluationException {
                    final List<Property> properties = t.getProperties(vr.getAsQName());
                    final TypedObjectNode[] ret = properties.toArray(new TypedObjectNode[properties.size()]);
                    return ret;
                }

                @Override
                public String getId(final Feature t) {
                    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                    // methods, choose Tools |
                    // Templates.
                }
            };
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

    @Override
    public void applyStyle(final PFeature pfeature, final WorldToScreenTransform wtst) {
        if (stylings == null) {
            this.stylings = style.evaluate(new DeegreeFeature(), evaluator);
        }

        if (stylings.size() == 0) {
            return;
        }

        int polygonNr = -1;
        int textNr = 0;
        int imageNr = 0;

        for (final org.deegree.commons.utils.Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String> styling
                    : stylings) {
            if (styling.first instanceof PolygonStyling) {
                PPath path;
                if (polygonNr < 0) {
                    path = pfeature;
                } else {
                    try {
                        path = pfeature.sldStyledPolygon.get(polygonNr);
                    } catch (IndexOutOfBoundsException ex) {
                        path = new PPath();
                        pfeature.sldStyledPolygon.add(path);
                        pfeature.addChild(path);
                    }
                    path.setPathTo(pfeature.getPathReference());
                }
                applyPolygonStyling(path, (PolygonStyling)styling.first);
                polygonNr++;
            } else if (styling.first instanceof TextStyling) {
                PFeature.PTextWithDisplacement text;
                try {
                    text = pfeature.sldStyledText.get(textNr++);
                } catch (IndexOutOfBoundsException ex) {
                    text = pfeature.new PTextWithDisplacement();
                    pfeature.sldStyledText.add(text);
                    pfeature.addChild(text);
                    pfeature.getMappingComponent().addStickyNode(text);
                }

                final com.vividsolutions.jts.geom.Point intPoint = CrsTransformer.transformToGivenCrs(
                            getGeometry(),
                            pfeature.getMappingComponent().getMappingModel().getSrs().getCode())
                            .getInteriorPoint();

                applyTextStyling(
                    text,
                    styling.third,
                    (TextStyling)styling.first,
                    wtst,
                    intPoint.getX(),
                    intPoint.getY());
                pfeature.getMappingComponent().rescaleStickyNode(text);
            } else if (styling.first instanceof PointStyling) {
                PImage image;
                try {
                    image = pfeature.sldStyledImage.get(imageNr++);
                } catch (IndexOutOfBoundsException ex) {
                    image = new PImageWithDisplacement();
                    pfeature.sldStyledImage.add(image);
                    pfeature.addChild(image);
                }
                final com.vividsolutions.jts.geom.Point intPoint = CrsTransformer.transformToGivenCrs(
                            getGeometry(),
                            pfeature.getMappingComponent().getMappingModel().getSrs().getCode())
                            .getInteriorPoint();

                applyPointStyling(
                    image,
                    (PointStyling)styling.first,
                    wtst,
                    intPoint.getX(),
                    intPoint.getY(),
                    pfeature.getMappingComponent().getCamera());
                pfeature.getMappingComponent().removeStickyNode(image);
                if (((PointStyling)styling.first).uom == Pixel) {
                    pfeature.getMappingComponent().addStickyNode(image);
                    pfeature.getMappingComponent().rescaleStickyNode(image);
                }
            }
        }

        /*
         * //if (stylings.getFirst().first instanceof PolygonStyling) { applyStyling(pfeature,
         * stylings.getFirst().first); //}
         *
         * while (pfeature.sldStyledPolygon.size() < (stylings.size() - 1)) { final PPath child = new PPath();
         * pfeature.sldStyledPolygon.add(child); pfeature.addChild(child); }
         *
         * for (int i = 0; i < pfeature.sldStyledPolygon.size(); i++) { //
         * pfeature.sldStyled.get(i).getPathReference().reset();
         * pfeature.sldStyledPolygon.get(i).setPathTo(pfeature.getPathReference());
         * applyStyling(pfeature.sldStyledPolygon.get(i), stylings.get(i + 1).first);}*/
    }

    /**
     * DOCUMENT ME!
     *
     * @param  image    DOCUMENT ME!
     * @param  styling  DOCUMENT ME!
     * @param  wtst     DOCUMENT ME!
     * @param  x        DOCUMENT ME!
     * @param  y        DOCUMENT ME!
     * @param  camera   DOCUMENT ME!
     */
    private void applyPointStyling(final PImage image,
            final PointStyling styling,
            final WorldToScreenTransform wtst,
            final double x,
            final double y,
            final PCamera camera) {
        Log.info("Test");
        image.setImage(styling.graphic.image);
        if (getUOMFromDeegree(styling.uom) == UOM.pixel) {
            image.setOffset(wtst.getScreenX(x), wtst.getScreenY(y));
            ((PImageWithDisplacement)image).setDisplacement(getUOMFromDeegree(styling.uom),
                styling.graphic.displacementX,
                styling.graphic.displacementY,
                styling.graphic.anchorPointX,
                styling.graphic.anchorPointY,
                wtst,
                camera);
        } else {
            ((PImageWithDisplacement)image).setUOM(getUOMFromDeegree(styling.uom));
            final double multiplier = getMultiplierFromDeegreeUOM(styling.uom);
            final double sizeMulti = styling.graphic.size / (double)(styling.graphic.image.getHeight());

            image.setScale(multiplier * sizeMulti);
            image.setOffset(wtst.getScreenX(
                    x
                            + ((styling.graphic.displacementX
                                    + (( /*1.0d - */-styling.graphic.anchorPointX)
                                        * styling.graphic.image.getWidth() * sizeMulti)) * multiplier)),
                wtst.getScreenY(
                    y
                            + ((styling.graphic.displacementY
                                    + (( /*1.0d - */styling.graphic.anchorPointY) * styling.graphic.size))
                                * multiplier)));
        }

        image.setRotation(Math.toRadians(styling.graphic.rotation));
        image.setTransparency((float)styling.graphic.opacity);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    private UOM getUOMFromDeegree(final org.deegree.style.styling.components.UOM uom) {
        switch (uom) {
            case Foot: {
                return UOM.foot;
            }
            case Metre: {
                return UOM.metre;
            }
            case Pixel: {
                return UOM.pixel;
            }
            case mm: {
                return UOM.mm;
            }
        }
        throw new RuntimeException("unknown UOM" + uom.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    private double getMultiplierFromDeegreeUOM(final org.deegree.style.styling.components.UOM uom) {
        switch (uom) {
            case Foot: {
                return 0.3048d;
            }
            case Metre: {
                return 1.0d;
            }
            case Pixel: {
                return 1.0d;
            }
            case mm: {
                return 0.001d;
            }
        }
        throw new RuntimeException("unknown UOM" + uom.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  styling   DOCUMENT ME!
     */
    private void applyPolygonStyling(final PPath pfeature, final PolygonStyling styling) {
        if (styling.fill != null) {
            applyFill(styling.uom, styling.fill, pfeature);
        }
        if (styling.stroke != null) {
            applyStroke(styling.uom, styling.stroke, pfeature);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  uom       DOCUMENT ME!
     * @param  stroke    DOCUMENT ME!
     * @param  pfeature  DOCUMENT ME!
     */
    private void applyStroke(final org.deegree.style.styling.components.UOM uom,
            final org.deegree.style.styling.components.Stroke stroke,
            final PPath pfeature) {
        /*double scale = 1.0d;
         * if(PDebug.getProcessingOutput()) { if(PPaintContext.CURRENT_PAINT_CONTEXT != null)     scale =
         * PPaintContext.CURRENT_PAINT_CONTEXT.getScale(); } else { if(PPickPath.CURRENT_PICK_PATH != null)     scale =
         * PPickPath.CURRENT_PICK_PATH.getScale();}*/
        final double multiplier = getMultiplierFromDeegreeUOM(uom);

        int linecap = BasicStroke.CAP_ROUND;
        if (stroke.linecap == org.deegree.style.styling.components.Stroke.LineCap.BUTT) {
            linecap = BasicStroke.CAP_BUTT;
        } else if (stroke.linecap == org.deegree.style.styling.components.Stroke.LineCap.ROUND) {
            linecap = BasicStroke.CAP_ROUND;
        } else if (stroke.linecap == org.deegree.style.styling.components.Stroke.LineCap.SQUARE) {
            linecap = BasicStroke.CAP_SQUARE;
        }
        int lineJoin = BasicStroke.JOIN_ROUND;
        if (stroke.linejoin == org.deegree.style.styling.components.Stroke.LineJoin.BEVEL) {
            lineJoin = BasicStroke.JOIN_BEVEL;
        } else if (stroke.linejoin == org.deegree.style.styling.components.Stroke.LineJoin.MITRE) {
            lineJoin = BasicStroke.JOIN_MITER;
        } else if (stroke.linejoin == org.deegree.style.styling.components.Stroke.LineJoin.ROUND) {
            lineJoin = BasicStroke.JOIN_ROUND;
        }
        float[] dash_array = null;
        if ((stroke.dasharray != null) && (stroke.dasharray.length != 0)) {
            dash_array = new float[stroke.dasharray.length];
            for (int i = 0; i < stroke.dasharray.length; i++) {
                dash_array[i] = (float)(stroke.dasharray[i] * multiplier);
            }
        }
        Stroke newStroke;
        if (uom == org.deegree.style.styling.components.UOM.Pixel) {
            newStroke = new CustomFixedWidthStroke((float)(stroke.width),
                    linecap,
                    lineJoin,
                    1.0f,
                    dash_array,
                    (float)(stroke.dashoffset));
        } else {
            newStroke = new BasicStroke((float)(stroke.width * multiplier),
                    linecap,
                    lineJoin,
                    1.0f,
                    dash_array,
                    (float)(stroke.dashoffset * multiplier));
        }
        pfeature.setStroke(newStroke);
        pfeature.setStrokePaint(getPaintFromDeegree(stroke.fill, stroke.color, uom));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   graphic  DOCUMENT ME!
     * @param   color    DOCUMENT ME!
     * @param   uom      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Paint getPaintFromDeegree(final org.deegree.style.styling.components.Graphic graphic,
            final Color color,
            final org.deegree.style.styling.components.UOM uom) {
        if (graphic == null) {
            return color;
        } else {
            final double multiplier = getMultiplierFromDeegreeUOM(uom);
            final TexturePaint texture = new TexturePaint(
                    graphic.image,
                    new Rectangle2D.Double(
                        0,
                        0,
                        multiplier
                                * graphic.size
                                * graphic.image.getWidth()
                                / graphic.image.getHeight(),
                        graphic.size
                                * multiplier));

            return texture;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  uom       DOCUMENT ME!
     * @param  fill      DOCUMENT ME!
     * @param  pfeature  DOCUMENT ME!
     */
    private void applyFill(final org.deegree.style.styling.components.UOM uom,
            final org.deegree.style.styling.components.Fill fill,
            final PPath pfeature) {
        pfeature.setPaint(getPaintFromDeegree(fill.graphic, fill.color, uom));
        // applyGraphic(fill.graphic, pfeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   font  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getFontStyling(final org.deegree.style.styling.components.Font font) {
        final int bolt = font.bold ? 1 : 0;
        switch (font.fontStyle) {
            case OBLIQUE:
            case ITALIC: {
                return bolt + 2;
            }
            case NORMAL: {
                return bolt;
            }
        }
        return bolt;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ptext        DOCUMENT ME!
     * @param  value        DOCUMENT ME!
     * @param  textStyling  DOCUMENT ME!
     * @param  wtst         DOCUMENT ME!
     * @param  x            DOCUMENT ME!
     * @param  y            DOCUMENT ME!
     */
    private void applyTextStyling(final PFeature.PTextWithDisplacement ptext,
            final String value,
            final TextStyling textStyling,
            final WorldToScreenTransform wtst,
            final double x,
            final double y) {
        ptext.setText(value);
        ptext.setOffset(wtst.getScreenX(x), wtst.getScreenY(y));
        ptext.setDisplacement(getUOMFromDeegree(textStyling.uom),
            textStyling.displacementX,
            textStyling.displacementY,
            textStyling.anchorPointX,
            textStyling.anchorPointY,
            wtst);
        /*double multiplier = getMultiplierFromDeegreeUOM(textStyling.uom);
         * ptext.setOffset(wtst.getScreenX(x + ((textStyling.displacementX)*multiplier)), wtst.getScreenY(y +
         * ((textStyling.displacementY)*multiplier)));*/
        /*ptext.setOffset(wtst.getScreenX(x + ((styling.graphic.displacementX + (1.0d - styling.graphic.anchorPointX) *
         * styling.graphic.image.getWidth() * sizeMulti)* multiplier)),         wtst.getScreenY(y +
         * ((styling.graphic.displacementY + (1.0d - styling.graphic.anchorPointY) *
         * styling.graphic.size)*multiplier)));
         */
        ptext.setTextPaint(textStyling.fill.color);
        Font font = null;
        try {
            for (final String fontName : textStyling.font.fontFamily) {
                font = new Font(fontName, getFontStyling(textStyling.font), (int)textStyling.font.fontSize);
            }
        } catch (Exception ex) {
        }
        ptext.setFont(font);
        ptext.setRotation(Math.toRadians(textStyling.rotation));
        // ptext.setVisible(true);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DeegreeFeature implements Feature {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DeegreeFeature object.
         */
        public DeegreeFeature() {
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void setId(final String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public QName getName() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
        }

        @Override
        public FeatureType getType() {
            return new org.deegree.feature.types.FeatureType() {

                    @Override
                    public GeometryPropertyType getDefaultGeometryPropertyDeclaration() {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }

                    @Override
                    public org.deegree.feature.Feature newFeature(final String string,
                            final List<Property> list,
                            final ExtraProps ep) {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }

                    @Override
                    public AppSchema getSchema() {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }

                    @Override
                    public GMLObjectCategory getCategory() {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }

                    @Override
                    public QName getName() {
                        return new QName(metaClass.getTableName());
                    }

                    @Override
                    public boolean isAbstract() {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }

                    @Override
                    public PropertyType getPropertyDeclaration(final QName qname) {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }

                    @Override
                    public List<PropertyType> getPropertyDeclarations() {
                        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                        // methods, choose Tools |
                        // Templates.
                    }
                };
        }

        @Override
        public List<Property> getGeometryProperties() {
            return new ArrayList<Property>();
        }

        @Override
        public Envelope getEnvelope() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setEnvelope(final Envelope envlp) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public Envelope calcEnvelope() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setPropertyValue(final QName qname, final int i, final TypedObjectNode ton) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setProperties(final List<Property> list) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public ExtraProps getExtraProperties() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setExtraProperties(final ExtraProps ep) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public List<Property> getProperties() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public List<Property> getProperties(final QName qname) {
            /*final Collection<Attribute> attributes = metaObject.getAttributeByName(qname.getLocalPart(),
             *   Integer.MAX_VALUE);*/

            final List<Property> deegreeProperties = new LinkedList();
            final Object value;
            if (properties.containsKey(qname.getLocalPart())) {
                value = properties.get(qname.getLocalPart());
                if (value == null) {
                    deegreeProperties.add(null);
                } else {
                    deegreeProperties.add(new DeegreeProperty(qname, value));
                }
            } else if ("original".equalsIgnoreCase(qname.getPrefix())) {
                try {
                    if (metaObject == null) {
                        metaObject = SessionManager.getConnection()
                                    .getMetaObject(
                                            SessionManager.getSession().getUser(),
                                            (Integer)properties.get(OBJECT_ID),
                                            (Integer)properties.get(CLASS_ID),
                                            SessionManager.getSession().getUser().getDomain());
                    }
                    value = metaObject.getBean().getProperty(qname.getLocalPart());
                    if (value == null) {
                        deegreeProperties.add(null);
                    } else {
                        deegreeProperties.add(new DeegreeProperty(qname, value));
                    }
                } catch (ConnectionException ex) {
                    LOG.info("CidsBean could not be loaded, property is null", ex);
                }
            }
            /*for (final Attribute attr : attributes) {
             *  properties.add(new DeegreeProperty(attr));}*/

            return deegreeProperties;
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        private class DeegreeProperty implements Property {

            //~ Instance fields ------------------------------------------------

            private QName name;
            private Object value;

            //~ Constructors ---------------------------------------------------

            /**
             * Creates a new DeegreeProperty object.
             *
             * @param  name   DOCUMENT ME!
             * @param  value  DOCUMENT ME!
             */
            public DeegreeProperty(final QName name, final Object value) {
                this.name = name;
                this.value = value;
            }

            //~ Methods --------------------------------------------------------

            @Override
            public QName getName() {
                return name;
            }

            @Override
            public PropertyType getType() {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                // generated methods,
                // choose Tools |
                // Templates.
            }

            @Override
            public TypedObjectNode getValue() {
                if (value == null) {
                    return new PrimitiveValue("null");
                } else if (value instanceof String) {
                    return new PrimitiveValue((String)value, new PrimitiveType(BaseType.STRING));
                } else if (value instanceof Float) {
                    return new PrimitiveValue(new Double((Float)value), new PrimitiveType(BaseType.DOUBLE));
                } else if (value instanceof Boolean) {
                    return new PrimitiveValue((Boolean)value, new PrimitiveType(BaseType.BOOLEAN));
                } else if (value instanceof Double) {
                    return new PrimitiveValue((Double)value, new PrimitiveType(BaseType.DOUBLE));
                } else if (value instanceof Integer) {
                    return new PrimitiveValue((Integer)value, new PrimitiveType(BaseType.INTEGER));
                } else if (value instanceof Geometry) {
                    return new org.deegree.geometry.Geometry() {

                            @Override
                            public org.deegree.geometry.Geometry.GeometryType getGeometryType() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public void setId(final String string) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public void setType(final GMLObjectType gmlot) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public PrecisionModel getPrecision() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public void setPrecision(final PrecisionModel pm) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public ICRS getCoordinateSystem() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public void setCoordinateSystem(final ICRS icrs) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public void setProperties(final List<Property> list) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean isSFSCompliant() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public int getCoordinateDimension() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean contains(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean crosses(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean equals(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean intersects(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean isBeyond(final org.deegree.geometry.Geometry gmtr, final Measure msr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean isDisjoint(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean isWithin(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean isWithinDistance(final org.deegree.geometry.Geometry gmtr,
                                    final Measure msr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean overlaps(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public boolean touches(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public org.deegree.geometry.Geometry getBuffer(final org.deegree.commons.uom.Measure msr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public Point getCentroid() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public Envelope getEnvelope() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public org.deegree.geometry.Geometry getDifference(
                                    final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public org.deegree.geometry.Geometry getIntersection(
                                    final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public org.deegree.geometry.Geometry getUnion(final org.deegree.geometry.Geometry gmtr) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public org.deegree.geometry.Geometry getConvexHull() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public Measure getDistance(final org.deegree.geometry.Geometry gmtr, final Unit unit) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public String getId() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public GMLObjectType getType() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public List<Property> getProperties() {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }

                            @Override
                            public List<Property> getProperties(final QName qname) {
                                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                                // generated methods,
                                // choose Tools |
                                // Templates.
                            }
                        };
                } else {
                    return new PrimitiveValue("null");
                }
            }

            @Override
            public void setValue(final TypedObjectNode ton) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                // generated methods,
                // choose Tools |
                // Templates.
            }

            @Override
            public void setChildren(final List<TypedObjectNode> list) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                // generated methods,
                // choose Tools |
                // Templates.
            }

            @Override
            public Map<QName, PrimitiveValue> getAttributes() {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                // generated methods,
                // choose Tools |
                // Templates.
            }

            @Override
            public List<TypedObjectNode> getChildren() {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                // generated methods,
                // choose Tools |
                // Templates.
            }

            @Override
            public XSElementDeclaration getXSType() {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of
                // generated methods,
                // choose Tools |
                // Templates.
            }

            @Override
            public String toString() {
                return value.toString();
            }
        }
    }

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
}
