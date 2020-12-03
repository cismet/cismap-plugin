/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.cidslayer;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.style.se.unevaluated.Style;

import org.jdom.Element;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CidsLayer extends AbstractFeatureService<CidsLayerFeature, String> implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsLayer.class);

    public static final String CIDS_FEATURELAYER_TYPE = "cidsFeatureService";
    private static final Map<Integer, ImageIcon> ICON_MAP = new HashMap<Integer, ImageIcon>();

    static {
        // todo: add icons for every type
        ICON_MAP.put(
            LAYER_ENABLED_VISIBLE,
            new ImageIcon(CidsLayer.class.getResource("/de/cismet/cismap/cidslayer/featureSupporter.png")));
        ICON_MAP.put(
            LAYER_ENABLED_INVISIBLE,
            new ImageIcon(CidsLayer.class.getResource("/de/cismet/cismap/cidslayer/featureSupporter.png")));
        ICON_MAP.put(
            LAYER_DISABLED_VISIBLE,
            new ImageIcon(CidsLayer.class.getResource("/de/cismet/cismap/cidslayer/featureSupporter.png")));
        ICON_MAP.put(
            LAYER_DISABLED_INVISIBLE,
            new ImageIcon(CidsLayer.class.getResource("/de/cismet/cismap/cidslayer/featureSupporter.png")));
    }

    //~ Instance fields --------------------------------------------------------

    private String query;
    private String tableName;
    private MetaClass metaClass;
    private String metaDocumentLink;
    private boolean downloadAllowed;
    private String geometryType = AbstractFeatureService.UNKNOWN;
    private Integer maxFeaturesPerPage = null;
    private Double maxArea = null;
    private Double maxScale = null;

    private final ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayer object.
     *
     * @param  cl  DOCUMENT ME!
     */
    public CidsLayer(final CidsLayer cl) {
        super(cl);
        tableName = cl.tableName;
        query = cl.getQuery();
        metaClass = cl.metaClass;
        metaDocumentLink = cl.metaDocumentLink;
        downloadAllowed = cl.downloadAllowed;
        maxFeaturesPerPage = cl.maxFeaturesPerPage;
        maxArea = cl.maxArea;
        maxScale = cl.maxScale;
        setAttributeTableRuleSet();
        // sldDefinition = cl.sldDefinition;
    }

    /**
     * Creates a new CidsLayer object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsLayer(final Element e) throws Exception {
        super(e);
        additionalInitializationFromElement(e);
        evaluateClassAttributes();
        // sldDefinition = new InputStreamReader(getClass().getResourceAsStream("/testSLD.xml")); name = "CidsLayer";
        // cidsStatement = new CidsLayerSearchStatement();
    }

    /**
     * Creates a new CidsLayer object.
     *
     * @param  clazz  DOCUMENT ME!
     */
    public CidsLayer(final MetaClass clazz) {
        super();

        name = determineLayerName(clazz);

        tableName = clazz.getTableName();
        metaClass = clazz;
        setAttributeTableRuleSet();
        evaluateClassAttributes();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void evaluateClassAttributes() {
        final ClassAttribute metaDocumentAttr = getMetaClass().getClassAttribute("metaDocument");

        if ((metaDocumentAttr != null) && (metaDocumentAttr.getValue() != null)) {
            metaDocumentLink = metaDocumentAttr.getValue().toString();
        }

        final ClassAttribute maxPageSizeAttr = getMetaClass().getClassAttribute("maxPageSize");

        if ((maxPageSizeAttr != null) && (maxPageSizeAttr.getValue() != null)) {
            try {
                maxFeaturesPerPage = Integer.parseInt(maxPageSizeAttr.getValue().toString());
            } catch (Exception e) {
                LOG.error("the max page size attribute does not contain a valid number: "
                            + maxPageSizeAttr.getValue().toString(),
                    e);
            }
        }

        try {
            String camalizedName = DefaultLayerProperties.camelize(getMetaClass().getName());
            camalizedName = String.valueOf(camalizedName.charAt(0)).toLowerCase() + camalizedName.substring(1);
            final String downloadForbiddenName = camalizedName + "DownloadForbidden";
            final String attrForbidden = SessionManager.getProxy()
                        .getConfigAttr(SessionManager.getSession().getUser(),
                            downloadForbiddenName,
                            getConnectionContext());

            if (attrForbidden != null) {
                final String downloadAllowedName = camalizedName + "DownloadAllowed";
                final String attrAllowed = SessionManager.getProxy()
                            .getConfigAttr(SessionManager.getSession().getUser(),
                                downloadAllowedName,
                                getConnectionContext());
                downloadAllowed = (attrAllowed != null);
            } else {
                downloadAllowed = true;
            }
        } catch (Exception e) {
            LOG.error("Error while checking the download permissions", e);
            downloadAllowed = false;
        }

        final ClassAttribute areaAttr = metaClass.getClassAttribute("maxArea");

        if ((areaAttr != null) && (areaAttr.getValue() != null)) {
            try {
                maxArea = Double.parseDouble(areaAttr.getValue().toString());
            } catch (Exception e) {
                LOG.error("the max scale attribute does not contain a valid number: "
                            + areaAttr.getValue().toString(),
                    e);
            }
        }

        final ClassAttribute scaleAttr = metaClass.getClassAttribute("maxScale");

        if ((scaleAttr != null) && (scaleAttr.getValue() != null)) {
            try {
                maxScale = Double.parseDouble(scaleAttr.getValue().toString());
            } catch (Exception e) {
                LOG.error("the max scale attribute does not contain a valid number: "
                            + scaleAttr.getValue().toString(),
                    e);
            }
        }
    }

    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
        final FeatureAnnotationSymbol symbol = new FeatureAnnotationSymbol(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/res/pushpin.png")).getImage());
        symbol.setSweetSpotX(0.46d);
        symbol.setSweetSpotY(0.9d);
        defaultLayerProperties.getStyle().setPointSymbol(symbol);
        defaultLayerProperties.setFeatureService(this);
        defaultLayerProperties.setIdExpression("id", LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);

        return defaultLayerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   clazz  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String determineLayerName(final MetaClass clazz) {
        String name;
        final ClassAttribute titleAttribute = clazz.getClassAttribute(CidsLayerConfig.LAYER_TITLE);

        if ((titleAttribute != null) && (titleAttribute.getValue() != null)) {
            name = titleAttribute.getValue().toString();
        } else {
            name = clazz.getName();
        }

        return name;
    }

    /**
     * DOCUMENT ME!
     */
    private void setAttributeTableRuleSet() {
        final String ruleSetName = DefaultLayerProperties.camelize(metaClass.getName()) + "RuleSet";

        try {
            final Class ruleSetClass = Class.forName("de.cismet.cismap.custom.attributerule." + ruleSetName);
            final Object o = ruleSetClass.newInstance();
            if (o instanceof DefaultAttributeTableRuleSet) {
                ((DefaultLayerProperties)getLayerProperties()).setAttributeTableRuleSet((DefaultAttributeTableRuleSet)
                    o);
            }
        } catch (Exception e) {
            // nothing to do
        }
    }

    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        final Map<String, LinkedList<Style>> styles = parseSLD(getSLDDefiniton());
        featureFactory = new CidsFeatureFactory(metaClass, this.getLayerProperties(), styles);
        geometryType = ((CidsFeatureFactory)featureFactory).getGeometryType();
        return featureFactory;
    }

    @Override
    public String getQuery() {
        return this.query;
    }

    @Override
    public void setQuery(final String query) {
        this.query = query;

        if (featureFactory != null) {
            ((CidsFeatureFactory)featureFactory).initEnvelope(query);
        }
    }

    @Override
    protected void initConcreteInstance() throws Exception {
    }

    @Override
    protected String getFeatureLayerType() {
        return CIDS_FEATURELAYER_TYPE;
    }

    @Override
    public Icon getLayerIcon(final int type) {
        return ICON_MAP.get(type);
    }

    @Override
    public CidsLayer clone() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cloning CidsLayer " + this.getName());
        }
        return new CidsLayer(this);
    }

    @Override
    public Element toElement() {
        final Element parentElement = super.toElement();
        final Element className = new Element("className");
        className.setText(tableName);
        parentElement.addContent(className);

        if ((query != null) && !query.isEmpty()) {
            final Element queryElement = new Element("currentQuery");
            queryElement.addContent(query);
            parentElement.addContent(queryElement);
        }

        return parentElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   element  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void additionalInitializationFromElement(final Element element) throws Exception {
        tableName = element.getChildText("className").trim();
        metaClass = ClassCacheMultiple.getMetaClass(SessionManager.getSession().getUser().getDomain(),
                tableName,
                getConnectionContext());
        if (metaClass == null) {
            return;
        }
        setAttributeTableRuleSet();

        final String queryText = element.getChildText("currentQuery");
        if (queryText != null) {
            setQuery(queryText);
        }
    }

    @Override
    public boolean isVisibleInBoundingBox(final XBoundingBox box) {
        if (((getMaxArea() != null) || (getMaxScale() != null)) && (box != null)) {
            Geometry bbox = box.getGeometry();
            bbox = CrsTransformer.transformToMetricCrs(bbox);

            if (((getMaxArea() != null) && (bbox.getArea() > getMaxArea()))
                        || ((getMaxScale() != null)
                            && (CismapBroker.getInstance().getMappingComponent().getScaleDenominator()
                                > getMaxScale()))) {
                return false;
            }
        }

        return super.isVisibleInBoundingBox(box);
    }

    @Override
    public boolean isEditable() {
        if ((metaClass != null) && (metaClass.getPermissions() != null)) {
            return metaClass.getPermissions().hasWritePermission(SessionManager.getSession().getUser());
        } else {
            // At the moment, this method is only used in the attributeTable and the FeatureInfoPanel.
            // If the hasWritePermission method cannot be invoked, true should be retuned, because the
            // individual objects will also be checked, before they can be changed
            return true;
        }
    }

    @Override
    public String decoratePropertyName(final String name) {
        try {
            initAndWait();

            final CidsLayerInfo info = ((CidsFeatureFactory)getFeatureFactory()).getLayerInfo();
            final AttributeTableRuleSet ruleSet = getLayerProperties().getAttributeTableRuleSet();

            if ((ruleSet != null) && (ruleSet.getAdditionalFieldFormula(name) != null)) {
                return decorateQuery(ruleSet.getAdditionalFieldFormula(name));
            }
            return getSQLName(info, name);
        } catch (Exception ex) {
            LOG.error("Error while decorating property name", ex);
        }

        return name;
    }

    @Override
    public boolean decorateLater() {
        return true;
    }

    @Override
    public String decorateQuery(final String query) {
        final char TEXT_DELIMITER = '\'';
        final StringBuilder decoratedQuery = new StringBuilder(query);
        boolean inDelimiter = false;
        final StringBuilder lastWord = new StringBuilder();

        for (int i = 0; i < decoratedQuery.length(); ++i) {
            final char currentCharacter = decoratedQuery.charAt(i);
            if (!inDelimiter
                        && (Character.isAlphabetic(currentCharacter) || Character.isDigit(currentCharacter)
                            || (currentCharacter == '_'))) {
                lastWord.append(currentCharacter);
            } else if (!inDelimiter && (lastWord.length() > 0)) {
                final String decoratedName = decoratePropertyName(lastWord.toString());
                if (!lastWord.toString().equals(decoratedName) && (currentCharacter != '(')) {
                    decoratedQuery.replace(i - lastWord.length(), i, decoratedName);
                    i += decoratedName.length() - lastWord.length();
                }
                lastWord.setLength(0);
            } else {
                lastWord.setLength(0);
            }

            if (currentCharacter == TEXT_DELIMITER) {
                inDelimiter = !inDelimiter;
            }
        }

        return decoratedQuery.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   info  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getSQLName(final CidsLayerInfo info, final String name) {
        int i;
        final String[] colNames = info.getColumnNames();

        for (i = 0; i < colNames.length; ++i) {
            if (colNames[i].equals(name)) {
                return info.getSqlColumnNames()[i];
            }
        }

        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the meta class of this CidsLayer
     */
    public MetaClass getMetaClass() {
        return metaClass;
    }

//
//    @Override
//    public String decoratePropertyValue(String columnName, String value) {
//        CidsLayerInfo layerInfo = ((CidsFeatureFactory)getFeatureFactory()).getLayerInfo();
//
//        if (layerInfo.isCatalogue(columnName)) {
//            int classId = layerInfo.getCatalogueClass(columnName);
//            try {
//                MetaClass mc = getMetaClass(classId);
//                final String queryTemplate = "SELECT %s, %s FROM %s;";
//                final String routeQuery = String.format(
//                        queryTemplate,
//                        mc.getID(),
//                        mc.getPrimaryKey(),
//                        mc.getTableName());
//                MetaObject[] mos = MetaObjectCache.getInstance().getMetaObjectsByQuery(routeQuery, false);
//
//                if (mos != null && mos.length > 0) {
//                    for (MetaObject tmp : mos) {
//                        if (tmp.toString().equals(value)) {
//                            return String.valueOf( tmp.getID() );
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                LOG.error("ConnectionException e");
//                return super.decoratePropertyValue(columnName, value);
//            }
//        }
//
//        return super.decoratePropertyValue(columnName, value);
//    }
//
//    private MetaClass getMetaClass(int classId) throws ConnectionException {
//        return SessionManager.getConnection()
//                    .getMetaClass(SessionManager.getSession().getUser(),
//                        classId,
//                        metaClass.getDomain());
//    }

    @Override
    public String getGeometryType() {
        if ((geometryType == null) || geometryType.equals(AbstractFeatureService.UNKNOWN)) {
            // if the geometry type cannot be determined from the database, it should be determined from the creator

            if ((getLayerProperties() != null) && (getLayerProperties().getAttributeTableRuleSet() != null)) {
                final AttributeTableRuleSet set = getLayerProperties().getAttributeTableRuleSet();
                final FeatureCreator creator = set.getFeatureCreator();

                if (creator instanceof PrimitiveGeometryCreator) {
                    if (((PrimitiveGeometryCreator)creator).isMulti()) {
                        if (((PrimitiveGeometryCreator)creator).getMode().equals(
                                        CreateGeometryListenerInterface.LINESTRING)) {
                            return "MultiLineString";
                        } else if (((PrimitiveGeometryCreator)creator).getMode().equals(
                                        CreateGeometryListenerInterface.POINT)) {
                            return "MultiPoint";
                        } else if (((PrimitiveGeometryCreator)creator).getMode().equals(
                                        CreateGeometryListenerInterface.POLYGON)) {
                            return "MultiPolygon";
                        }
                    } else {
                        if (((PrimitiveGeometryCreator)creator).getMode().equals(
                                        CreateGeometryListenerInterface.LINESTRING)) {
                            return "LineString";
                        } else if (((PrimitiveGeometryCreator)creator).getMode().equals(
                                        CreateGeometryListenerInterface.POINT)) {
                            return "Point";
                        } else if (((PrimitiveGeometryCreator)creator).getMode().equals(
                                        CreateGeometryListenerInterface.POLYGON)) {
                            return "Polygon";
                        }
                    }
                } else if ((creator instanceof LineAndStationCreator) || (creator instanceof StationLineCreator)) {
                    return "LineString";
                } else if ((creator instanceof PointAndStationCreator) || (creator instanceof StationCreator)) {
                    return "Point";
                }
            }
        }

        return geometryType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the metaDocumentLink
     */
    public String getMetaDocumentLink() {
        return metaDocumentLink;
    }

    @Override
    public int getMaxFeaturesPerPage() {
        if (maxFeaturesPerPage == null) {
            return -1;
        } else {
            return maxFeaturesPerPage;
        }
    }

    @Override
    public String[] getCalculatedAttributes() {
        if ((this.getLayerProperties() != null) && (this.getLayerProperties().getAttributeTableRuleSet() != null)) {
            return this.getLayerProperties().getAttributeTableRuleSet().getAdditionalFieldNames();
        } else {
            return super.getCalculatedAttributes();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the maxArea
     */
    public Double getMaxArea() {
        return maxArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the maxArea
     */
    public Double getMaxScale() {
        return maxScale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the downloadAllowed
     */
    public boolean isDownloadAllowed() {
        return downloadAllowed;
    }

    @Override
    public final ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CidsLayer) {
            return ((CidsLayer)obj).getMetaClass().equals(metaClass);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (97 * hash) + Objects.hashCode(this.metaClass);
        return hash;
    }
}
