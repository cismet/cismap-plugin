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

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import org.deegree.datatypes.Types;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.search.builtin.CidsLayerInitStatement;
import de.cismet.cids.server.search.builtin.CidsLayerSearchStatement;

import de.cismet.cids.tools.CidsLayerUtil;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

import de.cismet.connectioncontext.ClientConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsFeatureFactory extends AbstractFeatureFactory<CidsLayerFeature, String>
        implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static int currentId = -1;

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureServiceAttribute> featureServiceAttributes;

    GeomFromWktConverter converter = new GeomFromWktConverter();
    MetaClass metaClass;
    private Geometry envelope;
    private CidsLayerInfo layerInfo;
    private String geometryType = AbstractFeatureService.UNKNOWN;
    private Double maxArea = null;
    private Double maxScale = null;
    private Integer maxFeaturesPerPage = null;

    private final ClientConnectionContext connectionContext = ClientConnectionContext.create(getClass()
                    .getSimpleName());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  cff  DOCUMENT ME!
     */
    public CidsFeatureFactory(final CidsFeatureFactory cff) {
        super(cff);
        metaClass = cff.metaClass;
        this.envelope = cff.envelope;
        this.featureServiceAttributes = cff.featureServiceAttributes;
        this.layerInfo = cff.layerInfo;
        this.geometryType = cff.geometryType;
        this.maxArea = cff.maxArea;
        this.maxScale = cff.maxScale;
        this.maxFeaturesPerPage = cff.maxFeaturesPerPage;
        this.layerProperties = cff.layerProperties;
    }

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param       metaClass        DOCUMENT ME!
     * @param       layerProperties  DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    public CidsFeatureFactory(final MetaClass metaClass, final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
        this.metaClass = metaClass;
        layerName = CidsLayer.determineLayerName(metaClass);
        initLayer();
    }

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  metaClass        DOCUMENT ME!
     * @param  layerProperties  DOCUMENT ME!
     * @param  styles           DOCUMENT ME!
     */
    public CidsFeatureFactory(final MetaClass metaClass,
            final LayerProperties layerProperties,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) {
        this.layerProperties = layerProperties;
        this.setSLDStyle(styles);
        this.metaClass = metaClass;
        layerName = CidsLayer.determineLayerName(metaClass);
        initLayer();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected boolean isGenerateIds() {
        return false;
    }

    @Override
    public CidsFeatureFactory clone() {
        return new CidsFeatureFactory(this);
    }

    @Override
    public synchronized FeatureServiceFeature createNewFeature() {
        final HashMap<String, Object> properties = new HashMap<String, Object>(featureServiceAttributes.size());

        for (int j = featureServiceAttributes.size() - 1; j >= 0; j--) {
            if (featureServiceAttributes.get(j).getName().equalsIgnoreCase("id")) {
                properties.put(featureServiceAttributes.get(j).getName(), getFreeId());
            } else {
                properties.put(featureServiceAttributes.get(j).getName(), null);
            }
        }

        final CidsLayerFeature feature = new CidsLayerFeature(
                properties /*oid, cid, geom,*/,
                metaClass,
                getLayerInfo(),
                layerProperties,
                getStyle(layerName));

        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private synchronized int getFreeId() {
        return currentId--;
    }

    @Override
    public List<CidsLayerFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread) throws TooManyFeaturesException, Exception {
        if ((maxArea != null) && (boundingBox != null)) {
            Geometry bbox = boundingBox.getGeometry(CrsTransformer.extractSridFromCrs(
                        CismapBroker.getInstance().getSrs().getCode()));
            bbox = CrsTransformer.transformToMetricCrs(bbox);

            if (bbox.getArea() > maxArea) {
                return new ArrayList<CidsLayerFeature>();
            }
        }
        if ((maxScale != null) && (boundingBox != null)) {
            if (CismapBroker.getInstance().getMappingComponent().getScaleDenominator() > maxScale) {
                return new ArrayList<CidsLayerFeature>();
            }
        }

        return createFeaturesInternal(query, boundingBox, workerThread, 0, 0, null, true);
    }

    /**
     * DOCUMENT ME!
     */
    private void initLayer() {
        try {
            final Object info = CidsLayerUtil.getCidsLayerInfo(metaClass, SessionManager.getSession().getUser());
            setLayerInfo((CidsLayerInfo)info);

            ClassAttribute scaleAttr = metaClass.getClassAttribute("maxArea");

            if ((scaleAttr != null) && (scaleAttr.getValue() != null)) {
                try {
                    maxArea = Double.parseDouble(scaleAttr.getValue().toString());
                } catch (Exception e) {
                    logger.error("the max area attribute does not contain a valid number: "
                                + scaleAttr.getValue().toString(),
                        e);
                }
            }

            scaleAttr = metaClass.getClassAttribute("maxScale");

            if ((scaleAttr != null) && (scaleAttr.getValue() != null)) {
                try {
                    maxScale = Double.parseDouble(scaleAttr.getValue().toString());
                } catch (Exception e) {
                    logger.error("the max scale attribute does not contain a valid number: "
                                + scaleAttr.getValue().toString(),
                        e);
                }
            }
            final ClassAttribute maxPageSizeAttr = metaClass.getClassAttribute("maxPageSize");

            if ((maxPageSizeAttr != null) && (maxPageSizeAttr.getValue() != null)) {
                try {
                    maxFeaturesPerPage = Integer.parseInt(maxPageSizeAttr.getValue().toString());
                } catch (Exception e) {
                    logger.error("the max page size attribute does not contain a valid number: "
                                + maxPageSizeAttr.getValue().toString(),
                        e);
                }
            }

            final ClassAttribute typeAttr = metaClass.getClassAttribute("geometryType");
            final ClassAttribute boundingBoxAttr = metaClass.getClassAttribute("boundingBox");
            geometryType = AbstractFeatureService.UNKNOWN;
            final String crs = CismapBroker.getInstance().getDefaultCrs();

            if ((typeAttr == null) || (boundingBoxAttr == null)) {
                final CidsLayerInitStatement serverSearch = new CidsLayerInitStatement(
                        metaClass,
                        SessionManager.getSession().getUser());
                final ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(SessionManager.getSession().getUser(),
                                    serverSearch,
                                    getConnectionContext());

                for (final ArrayList row : resultArray) {
                    if (row.get(0) != null) {
                        envelope = converter.convertForward((String)row.get(0), crs);

                        if (envelope instanceof Point) {
                            envelope = envelope.buffer(1);
                        }

                        if (row.size() == 2) {
                            geometryType = postgisToJtsGeometryType((String)row.get(1));
                        }
                    }
                }
            }

            if ((typeAttr != null) && (typeAttr.getValue() != null)) {
                geometryType = typeAttr.getValue().toString();
            }

            if ((boundingBoxAttr != null) && (boundingBoxAttr.getValue() != null)) {
                envelope = converter.convertForward(boundingBoxAttr.getValue().toString(), crs);
            }

            featureServiceAttributes = new ArrayList<FeatureServiceAttribute>();
            final String[] names = layerInfo.getColumnNames();
            final String[] types = layerInfo.getPrimitiveColumnTypes();

            for (int i = 0; i < names.length; ++i) {
                final String type = String.valueOf(getTypeByTypeName(types[i]));
                featureServiceAttributes.add(new FeatureServiceAttribute(names[i], type, true));
            }

            final AttributeTableRuleSet ruleSet = layerProperties.getAttributeTableRuleSet();

            if (ruleSet != null) {
                final String[] additionalFields = ruleSet.getAdditionalFieldNames();

                if (additionalFields != null) {
                    for (int index = 0; index < additionalFields.length; ++index) {
                        final String name = additionalFields[index];
                        final Class cl = ruleSet.getAdditionalFieldClass(index);
                        final FeatureServiceAttribute fsa = new FeatureServiceAttribute(
                                name,
                                FeatureTools.getType(cl),
                                true);
                        int attributeIndex = ruleSet.getIndexOfAdditionalFieldName(name);

                        if (attributeIndex < 0) {
                            attributeIndex = featureServiceAttributes.size() + 1 + attributeIndex;
                        }
                        featureServiceAttributes.add(attributeIndex, fsa);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while initialiseing the cids layer.", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String postgisToJtsGeometryType(final String geometryType) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), -1);

        if (geometryType.equalsIgnoreCase("ST_Point")) {
            return "Point";
        } else if (geometryType.equalsIgnoreCase("ST_MultiPoint")) {
            return "MultiPoint";
        } else if (geometryType.equalsIgnoreCase("ST_LineString")) {
            return "LineString";
        } else if (geometryType.equalsIgnoreCase("ST_MultiLineString")) {
            return "MultiLineString";
        } else if (geometryType.equalsIgnoreCase("ST_Polygon")) {
            return "Polygon";
        } else if (geometryType.equalsIgnoreCase("ST_MultiPolygon")) {
            return "MultiPolygon";
        }

        return AbstractFeatureService.UNKNOWN;
    }

    @Override
    public List<FeatureServiceAttribute> createAttributes(final SwingWorker workerThread)
            throws TooManyFeaturesException, UnsupportedOperationException, Exception {
        if (featureServiceAttributes == null) {
            initLayer();
        }

        if (featureServiceAttributes == null) {
            logger.warn("FeatureServiceAttributes for cids feature factory not found");
        }

        return featureServiceAttributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dataType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getTypeByTypeName(final String dataType) {
        String type = dataType;

        if (type.indexOf(".") != -1) {
            type = type.substring(type.lastIndexOf(".") + 1);
        }

        if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("text") || type.equalsIgnoreCase("varchar")) {
            return Types.VARCHAR;
        } else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("double precision")
                    || type.equalsIgnoreCase("float8")) {
            return Types.DOUBLE;
        } else if (type.equalsIgnoreCase("float")) {
            return Types.FLOAT;
        } else if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("int")) {
            return Types.INTEGER;
        } else if (type.equalsIgnoreCase("long") || type.equalsIgnoreCase("int8")) {
            return Types.BIGINT;
        } else if (type.toLowerCase().contains("timestamp")) {
            return Types.TIMESTAMP;
        } else if (type.toLowerCase().contains("time")) {
            return Types.TIME;
        } else if (type.toLowerCase().contains("date")) {
            return Types.DATE;
        } else if (type.toLowerCase().contains("bool")) {
            return Types.BOOLEAN;
        } else if (type.toLowerCase().contains("geometry")) {
            return Types.GEOMETRY;
        } else if (type.equalsIgnoreCase("BigDecimal")) {
            return Types.NUMERIC;
        } else {
            return Types.VARCHAR;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  query  DOCUMENT ME!
     */
    public void initEnvelope(final String query) {
        final Thread determineEnvelope = new Thread("determine envelope") {

                @Override
                public void run() {
                    try {
                        final CidsLayerInitStatement serverSearch = new CidsLayerInitStatement(
                                metaClass,
                                SessionManager.getSession().getUser(),
                                query);
                        final ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)SessionManager
                                    .getProxy()
                                    .customServerSearch(SessionManager.getSession().getUser(),
                                            serverSearch,
                                            getConnectionContext());
                        final String crs = CismapBroker.getInstance().getDefaultCrs();

                        for (final ArrayList row : resultArray) {
                            if (row.get(0) != null) {
                                envelope = converter.convertForward((String)row.get(0), crs);

                                if (envelope instanceof Point) {
                                    envelope = envelope.buffer(1);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error while determine new extend", e);
                    }
                }
            };

        determineEnvelope.start();
    }

    @Override
    public List<CidsLayerFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
        return createFeaturesInternal(query, boundingBox, workerThread, offset, limit, orderBy, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   query              DOCUMENT ME!
     * @param   boundingBox        DOCUMENT ME!
     * @param   workerThread       DOCUMENT ME!
     * @param   offset             DOCUMENT ME!
     * @param   limit              DOCUMENT ME!
     * @param   orderBy            DOCUMENT ME!
     * @param   saveAsLastCreated  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    private List<CidsLayerFeature> createFeaturesInternal(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy,
            final boolean saveAsLastCreated) throws TooManyFeaturesException, Exception {
        if (checkCancelled(workerThread, "creatureFeatures()")) {
            return null;
        }
        final long startTime = System.currentTimeMillis();
        final String crs = CismapBroker.getInstance().getDefaultCrs();
        final int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());

        final CrsTransformer transformer = new CrsTransformer(crs);
        BoundingBox boundingBoxIncurrentCrs = null;
        final CidsLayerSearchStatement serverSearch = new CidsLayerSearchStatement(
                metaClass,
                SessionManager.getSession().getUser());

        if (boundingBox != null) {
            boundingBoxIncurrentCrs = transformer.transformBoundingBox(
                    boundingBox,
                    CismapBroker.getInstance().getSrs().getCode());
        }

        serverSearch.setExactSearch(true);
        serverSearch.setSrid(CismapBroker.getInstance().getDefaultCrsAlias());
        String[] orderByStrings = new String[0];

        if (orderBy != null) {
            orderByStrings = new String[orderBy.length];

            for (int i = 0; i < orderBy.length; ++i) {
                orderByStrings[i] = CidsLayer.getSQLName(layerInfo, orderBy[i].getName()) + " "
                            + (orderBy[i].isAscOrder() ? "asc" : "desc");
            }
        }
        final boolean ignoreGeoLimitations = ((boundingBoxIncurrentCrs == null)
                ? true : ((envelope == null) || envelope.coveredBy(boundingBoxIncurrentCrs.getGeometry(srid))));

        // if the hole envelope of the layer should be requested, the coordinate limitation is not required
        if ((boundingBoxIncurrentCrs != null)
                    && ((envelope == null) || !ignoreGeoLimitations)) {
            serverSearch.setX1(boundingBoxIncurrentCrs.getX1());
            serverSearch.setY1(boundingBoxIncurrentCrs.getY1());
            serverSearch.setX2(boundingBoxIncurrentCrs.getX2());
            serverSearch.setY2(boundingBoxIncurrentCrs.getY2());
        }

        serverSearch.setQuery(query);
        if ((limit == 0) && (maxFeaturesPerPage != null)) {
            serverSearch.setLimit(maxFeaturesPerPage);
        } else {
            serverSearch.setLimit(limit);
        }
        serverSearch.setOffset(offset);
        serverSearch.setOrderBy(orderByStrings);
//        if (!saveAsLastCreated) {
//            serverSearch.setExactSearch(true);
//        }

        if (checkCancelled(workerThread, "PreQuery")) {
            return null;
        }

        final boolean compressed = true;

        serverSearch.setCompressed(compressed);

        final Collection resultCollection = SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                        serverSearch,
                        getConnectionContext());
        if (checkCancelled(workerThread, "PostQuery")) {
            return null;
        }

        ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)resultCollection;

        if (compressed) {
            resultArray = CidsLayerSearchStatement.uncompressResult(resultArray);
        }

        if (resultArray == null) {
            return new ArrayList<CidsLayerFeature>();
        }

        final Vector<CidsLayerFeature> features = new Vector<CidsLayerFeature>();
        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(crs));
        final WKBReader wkbReader = new WKBReader(geomFactory);
        final List<FeatureServiceAttribute> attributeListWithoutGenericAttributes =
            new ArrayList<FeatureServiceAttribute>();

        for (final FeatureServiceAttribute attr : featureServiceAttributes) {
            if ((layerProperties.getAttributeTableRuleSet() == null)
                        || (layerProperties.getAttributeTableRuleSet().getIndexOfAdditionalFieldName(attr.getName())
                            == Integer.MIN_VALUE)) {
                attributeListWithoutGenericAttributes.add(attr);
            }
        }

        // determine the feature class
        Class<? extends CidsLayerFeature> featureClass;

        if ((getLayerProperties() != null) && (getLayerProperties().getAttributeTableRuleSet() != null)
                    && (getLayerProperties().getAttributeTableRuleSet().getFeatureClass() != null)
                    && CidsLayerFeature.class.isAssignableFrom(
                        getLayerProperties().getAttributeTableRuleSet().getFeatureClass())) {
            featureClass = (Class<? extends CidsLayerFeature>)getLayerProperties().getAttributeTableRuleSet()
                        .getFeatureClass();
        } else {
            if ((getLayerProperties() != null) && (getLayerProperties().getAttributeTableRuleSet() != null)
                        && (getLayerProperties().getAttributeTableRuleSet().getFeatureClass() != null)) {
                logger.warn(
                    "The custom feature class of the cids layer is not an instance of CidsLayerFeature. The class CidsLayerFeature will be used.");
            }
            featureClass = CidsLayerFeature.class;
        }

        Constructor<? extends CidsLayerFeature> featureConstructor;

        try {
            featureConstructor = featureClass.getConstructor(
                    Map.class,
                    MetaClass.class,
                    CidsLayerInfo.class,
                    LayerProperties.class,
                    List.class);
        } catch (Exception e) {
            logger.warn(
                "The custom CidsLayerFeature class has no suitable constructor (Map, MetaClass, CidsLayerInfo, LayerProperties, List). The class CidsLayerFeature will be used.",
                e);
            featureClass = CidsLayerFeature.class;

            try {
                featureConstructor = featureClass.getConstructor(
                        Map.class,
                        MetaClass.class,
                        CidsLayerInfo.class,
                        LayerProperties.class,
                        List.class);
            } catch (Exception ex) {
                logger.error(
                    "No suitable constructor found in class CidsLayerFeature. The cids layer cannot be used.",
                    ex);
                return features;
            }
        }

        for (int i = 0; i < resultArray.size(); i++) {
            final HashMap<String, Object> properties = new HashMap<String, Object>(
                    attributeListWithoutGenericAttributes.size());
            boolean abort = false;
            for (int j = resultArray.get(i).size() - 1; j >= 0; j--) {
                if (resultArray.get(i).get(j) instanceof byte[]) {
                    try {
                        final Geometry g = wkbReader.read((byte[])resultArray.get(i).get(j));

                        if (!ignoreGeoLimitations && !saveAsLastCreated) {
                            if ((boundingBoxIncurrentCrs != null)
                                        && !g.intersects(boundingBoxIncurrentCrs.getGeometry(srid))) {
                                abort = true;
                                break;
                            }
                        }

                        properties.put(attributeListWithoutGenericAttributes.get(j).getName(), g);
                    } catch (final Exception ex) {
                        properties.put(attributeListWithoutGenericAttributes.get(j).getName(),
                            resultArray.get(i).get(j));
                    }
                } else {
                    properties.put(attributeListWithoutGenericAttributes.get(j).getName(), resultArray.get(i).get(j));
                }
            }

            if (abort) {
                continue;
            }

            CidsLayerFeature lastFeature = featureConstructor.newInstance(
                    properties /*oid, cid, geom,*/,
                    metaClass,
                    getLayerInfo(),
                    layerProperties,
                    getStyle(layerName));
            lastFeature.setSimplifiedGeometryAllowed(saveAsLastCreated);

            features.add(lastFeature);
            lastFeature = null;
        }
        if (checkCancelled(workerThread, "PreReturn()")) {
            return null;
        }

        if (saveAsLastCreated && (boundingBoxIncurrentCrs != null)) {
            updateLastCreatedFeatures(
                features,
                boundingBoxIncurrentCrs.getGeometry(CrsTransformer.extractSridFromCrs(crs)),
                query);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("time to receive features" + (System.currentTimeMillis() - startTime));
        }

        return features;
    }

    @Override
    public int getFeatureCount(final String query, final BoundingBox bb) {
        try {
            final String crs = CismapBroker.getInstance().getDefaultCrs();

            final CrsTransformer transformer = new CrsTransformer(crs);
            final BoundingBox boundingBox2 = transformer.transformBoundingBox(
                    bb,
                    CismapBroker.getInstance().getSrs().getCode());
            final CidsLayerSearchStatement serverSearch = new CidsLayerSearchStatement(
                    metaClass,
                    SessionManager.getSession().getUser());
            final int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
            serverSearch.setSrid(CismapBroker.getInstance().getDefaultCrsAlias());
            final boolean ignoreGeoLimitations = envelope.coveredBy(boundingBox2.getGeometry(srid));

            // if the hole envelope of the layer should be requested, the coordinate limitation is not required
            if ((boundingBox2 != null) && ((envelope == null) || !ignoreGeoLimitations)) {
                serverSearch.setX1(boundingBox2.getX1());
                serverSearch.setY1(boundingBox2.getY1());
                serverSearch.setX2(boundingBox2.getX2());
                serverSearch.setY2(boundingBox2.getY2());
            }
            serverSearch.setCountOnly(true);
            serverSearch.setQuery(query);

            final Collection resultCollection = SessionManager.getProxy()
                        .customServerSearch(SessionManager.getSession().getUser(),
                            serverSearch,
                            getConnectionContext());

            final ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)resultCollection;

            if ((resultArray != null) && (resultArray.size() > 0) && (resultArray.get(0).size() > 0)) {
                return ((Number)resultArray.get(0).get(0)).intValue();
            }
        } catch (Exception e) {
            logger.error("Cannot determine the feature count", e);
        }

        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the envelope
     */
    public Geometry getEnvelope() {
        return envelope;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the layerInfo
     */
    public CidsLayerInfo getLayerInfo() {
        return layerInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerInfo  the layerInfo to set
     */
    public void setLayerInfo(final CidsLayerInfo layerInfo) {
        this.layerInfo = layerInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the geometryType
     */
    public String getGeometryType() {
        return geometryType;
    }

    @Override
    public final ClientConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
