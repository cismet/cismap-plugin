/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
import de.cismet.cids.featurerenderer.SubFeatureAwareFeatureRenderer;

import de.cismet.cids.utils.ClassloadingHelper;
import de.cismet.cids.utils.interfaces.CidsBeanAction;
import de.cismet.cids.utils.interfaces.CidsBeanActionsProvider;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.Bufferable;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureGroup;
import de.cismet.cismap.commons.features.FeatureGroups;
import de.cismet.cismap.commons.features.FeatureRenderer;
import de.cismet.cismap.commons.features.FeatureRendererAwareFeature;
import de.cismet.cismap.commons.features.Highlightable;
import de.cismet.cismap.commons.features.InfoNodeAwareFeature;
import de.cismet.cismap.commons.features.PureFeatureGroup;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupporterRasterServiceUrl;
import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;

import de.cismet.commons.classloading.BlacklistClassloading;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CidsFeature implements XStyledFeature,
    Highlightable,
    Bufferable,
    RasterLayerSupportedFeature,
    FeatureGroup,
    CidsBeanActionsProvider,
    InfoNodeAwareFeature,
    FeatureRendererAwareFeature,
    ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsFeature.class);

    //~ Instance fields --------------------------------------------------------

    private Paint featureFG = Color.black;
    private Paint featureBG = Color.gray;
    private float featureTranslucency = 0.5f;
    private float featureBorder = 10.0f;
    private String renderFeatureString = null;
    private String renderMultipleFeatures = null;
    private int renderAllFeatures = 1;
    private boolean hiding = false;
    private Geometry geom;
    private MetaObject mo;
    private MetaClass mc;
    private boolean editable = false;
    private String namenszusatz = "";                                 // NOI18N
    private FeatureRenderer featureRenderer = null;
    private SubFeatureAwareFeatureRenderer parentFeatureRenderer = null;
    private FeatureAwareRasterService featureAwareRasterService = null;
    private String supportingRasterServiceRasterLayerName = null;
    private String supportingRasterServiceRasterLayerPrefix = null;
    private String supportingRasterServiceRasterLayerPostfix = null;
    private String supportingRasterServiceIdAttributeName = null;
    private String supportingRasterServiceLayerStyleName = "default"; // NOI18N
    private Image pointSymbol = null;
    private double pointSymbolSweetSpotX = 0d;
    private double pointSymbolSweetSpotY = 0d;
    private final Collection<Feature> subFeatures = new ArrayList<Feature>();

    // CidsFeature is FeatureGroup + SubFeature
    private FeatureGroup parentFeature = null;
    private String myAttributeStringInParentFeature = null;
    private Collection<CidsBeanAction> cidsBeanActions = new ArrayList<CidsBeanAction>();
    private boolean infoNodeEnabled = true;

    private final ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CidsFeature.
     *
     * @param   mon  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @Deprecated
    public CidsFeature(final MetaObjectNode mon) throws IllegalArgumentException {
        this(mon.getObject());
    }

    /**
     * Creates a new CidsFeature object.
     *
     * @param   mo  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public CidsFeature(final MetaObject mo) throws IllegalArgumentException {
        this(mo, null, null);
    }

    /**
     * Creates a new CidsFeature object.
     *
     * @param   mo     DOCUMENT ME!
     * @param   oAttr  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public CidsFeature(final MetaObject mo, final ObjectAttribute oAttr) throws IllegalArgumentException {
        this(mo, ((oAttr != null) ? oAttr.getMai().getFieldName() : null), null);
    }

    /**
     * Creates a new CidsFeature object.
     *
     * @param   mon    DOCUMENT ME!
     * @param   oAttr  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @Deprecated
    public CidsFeature(final MetaObjectNode mon, final ObjectAttribute oAttr) throws IllegalArgumentException {
        this(mon.getObject(), oAttr.getMai().getFieldName(), null);
    }

    /**
     * Creates a new CidsFeature object.
     *
     * @param  mo            DOCUMENT ME!
     * @param  rootRenderer  DOCUMENT ME!
     */
    private CidsFeature(final MetaObject mo, final SubFeatureAwareFeatureRenderer rootRenderer) {
        this(mo, null, rootRenderer);
    }

    /**
     * Creates a new instance of CidsFeature.
     *
     * @param   mo                        DOCUMENT ME!
     * @param   localRenderFeatureString  DOCUMENT ME!
     * @param   rootRenderer              DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private CidsFeature(final MetaObject mo,
            final String localRenderFeatureString,
            final SubFeatureAwareFeatureRenderer rootRenderer) throws IllegalArgumentException {
        this.parentFeatureRenderer = rootRenderer;
        try {
            this.mo = mo;
            this.mc = SessionManager.getProxy().getMetaClass(mo.getClassKey(), getConnectionContext());
            initFeatureSettings();

            // evaluate renderFeature
            try {
                if (localRenderFeatureString != null) {
                    renderFeatureString = localRenderFeatureString;
                }

                if ((renderFeatureString != null) && !renderFeatureString.equals("")) { // NOI18N
                    final String[] renderFeatures = renderFeatureString.split(",");     // NOI18N
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("renderFeatures: " + Arrays.asList(renderFeatures));  // NOI18N
                    }
                    if (renderFeatures.length == 1) {
                        final Object tester = mo.getBean().getProperty(renderFeatureString);
                        if (tester instanceof Collection) {
                            // single renderer attribute, multiple geometries case
                            createSubFeatures(renderFeatures);
                        } else if (tester instanceof Geometry) {
                            // old default case, single atribute and geometry
                            geom = (Geometry)tester;
                        } else if (tester instanceof CidsBean) {
                            geom = searchGeometryInMetaObject(((CidsBean)tester).getMetaObject());
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Wert in Attribut RENDER_FEATURE = " + renderFeatureString
                                            + " hat zu keinem Geometrieobjekt gefuehrt.");
                            }
                        }
                    } else {
                        // multi renderer attribute case
                        createSubFeatures(renderFeatures);
                    }
                }
            } catch (final Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("RENDER_FEATURE war fehlerhaft gesetzt. Geometrie unter Attribut mit dem Namen: "
                                + renderFeatureString + " konnte nicht gefunden werden",
                        e);
                }
                geom = null;
            }

            if (geom == null) {
                // Defaultfall: Es ist kein explizites Geometriefeld angegeben
                geom = searchGeometryInMetaObject(mo);
            }
        } catch (final Exception e) {
            LOG.error("Error CidsFeature(MetaObjectNode mon)", e);                     // NOI18N
            throw new IllegalArgumentException("Error on creating a CidsFeatures", e); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry searchGeometryInMetaObject(final MetaObject mo) {
        final Collection c = mo.getAttributesByType(Geometry.class, 1);
        for (final Object elem : c) {
            final ObjectAttribute oa = (ObjectAttribute)elem;

            return (Geometry)oa.getValue();
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  renderFeatures  DOCUMENT ME!
     */
    private void createSubFeatures(final String[] renderFeatures) {
        SubFeatureAwareFeatureRenderer rootRenderer = parentFeatureRenderer;
        if ((rootRenderer == null) && (featureRenderer instanceof SubFeatureAwareFeatureRenderer)) {
            rootRenderer = (SubFeatureAwareFeatureRenderer)featureRenderer;
        }

        for (String renderFeature : renderFeatures) {
            renderFeature = renderFeature.trim();
            final Object tester = mo.getBean().getProperty(renderFeature);
            Feature result;
            if (tester instanceof Collection) {
                // expand case
                final Collection<CidsBean> cbc = (Collection<CidsBean>)tester;
                final PureFeatureGroup fg = new PureFeatureGroup();
                for (final CidsBean cb : cbc) {
                    final CidsFeature cf = new CidsFeature(cb.getMetaObject(), rootRenderer);
                    cf.setParentFeature(this); // first we had fg here ;-)
                    cf.setMyAttributeStringInParentFeature(renderFeature);
                    fg.addFeature(cf);
                    // sub features should not show their InfoNode Panel
                    cf.setInfoNodeEnabled(false);
                }
                fg.setParentFeature(this);
                fg.setMyAttributeStringInParentFeature(renderFeature);
                result = fg;
            } else {
                // no expand, single feature case, CidsFeature itself will neglect unusable geometry attributes
                final CidsFeature cf = new CidsFeature(this.getMetaObject(), renderFeature, rootRenderer);
                cf.setParentFeature(this);
                cf.setMyAttributeStringInParentFeature(renderFeature);
                // sub features should not show their InfoNode Panel
                cf.setInfoNodeEnabled(false);
                result = cf;
            }

            if (result.getGeometry() != null) {
                // ok case
                subFeatures.add(result);
            } else {
                // features without geom can cause trouble in other code parts -> we do not add them.
                LOG.warn("Did not add Feature " + result + " because the geometry is null"); // NOI18N
            }
        }
        geom = FeatureGroups.getEnclosingGeometry(subFeatures);
        hide(true);

        if (LOG.isDebugEnabled()) {
            LOG.debug("subFeatures: " + subFeatures); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initFeatureSettings() {
        try {
            if (CismapBroker.getInstance().getMappingComponent().getMappingModel() instanceof ActiveLayerModel) {
                if (((ActiveLayerModel)CismapBroker.getInstance().getMappingComponent().getMappingModel()).getSrs()
                            .getCode().equalsIgnoreCase("epsg:4326")) {                                           // NOI18N
                    featureBorder = 0.001f;
                }
            }
        } catch (Exception e) {
        }
        try {
            renderFeatureString = getAttribValue("RENDER_FEATURE", mo, mc).toString();                            // NOI18N
            if (LOG.isDebugEnabled()) {
                LOG.debug("RENDER_FEATURE=" + renderFeatureString);                                               // NOI18N
            }
        } catch (Exception t) {
            LOG.info("RENDER_FEATURE corrupt or missing", t);                                                     // NOI18N
        }
        try {
            renderMultipleFeatures = getAttribValue("RENDER_MULTIPLE_FEATURES", mo, mc).toString();               // NOI18N
            if (LOG.isDebugEnabled()) {
                LOG.debug("RENDER_MULTIPLE_FEATURES=" + renderMultipleFeatures);                                  // NOI18N
            }
        } catch (Exception t) {
            LOG.info("RENDER_MULTIPLE_FEATURES corrupt or missing", t);                                           // NOI18N
        }
        try {
            renderAllFeatures = new Integer(getAttribValue("RENDER_ALL_FEATURES", mo, mc).toString()).intValue(); // NOI18N
            if (LOG.isDebugEnabled()) {
                LOG.debug("renderAllFeatures=" + renderAllFeatures);                                              // NOI18N
            }
        } catch (Exception t) {
            LOG.info("RENDER_AKK_FEATURES corrupt or missing", t);                                                // NOI18N
        }
        try {
            final Object hideFeature = getAttribValue("HIDE_FEATURE", mo, mc);                                    // NOI18N
            if (hideFeature == null) {
                hiding = false;
            } else {
                hiding = Boolean.valueOf(hideFeature.toString());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("HIDE_FEATURE=" + hiding);             // NOI18N
            }
        } catch (final Exception e) {
            LOG.info(("HIDE_FEATURE corrupt or missing"), e);    // NOI18N
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("VERSUCHE FEATURERENDERER ZU SETZEN"); // NOI18N
            }                                                    // NOI18N

            final Class<?> clazz = ClassloadingHelper.getDynamicClass(
                    mc,
                    ClassloadingHelper.CLASS_TYPE.FEATURE_RENDERER);
            final Constructor<?> constructor = clazz.getConstructor();
            featureRenderer = (FeatureRenderer)constructor.newInstance();
            ((CustomCidsFeatureRenderer)featureRenderer).setMetaObject(mo);
            if (LOG.isDebugEnabled()) {
                // Method assignM=assigner.getMethod("assign", new Class[] {Connection.class,String[].class,
                // UniversalContainer.class});
                LOG.debug("HAT GEKLAPPT:" + clazz);                                                        // NOI18N
            }
        } catch (Exception t) {
            LOG.warn(("FEATURE_RENDERER corrupt or missing"), t);                                          // NOI18N
        }
        try {
            final float featureTranslucencyValue = new Float(getAttribValue("FEATURE_TRANSLUCENCY", mo, mc).toString())
                        .floatValue();                                                                     // NOI18N
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_TRANSLUCENCY=" + featureTranslucencyValue);                             // NOI18N
            }
            featureTranslucency = featureTranslucencyValue;
        } catch (Exception t) {
            LOG.info("FEATURE_TRANSLUCENCY corrupt or missing", t);                                        // NOI18N
        }
        try {
            setFeatureBorder(new Float(getAttribValue("FEATURE_BORDER", mo, mc).toString()).floatValue()); // NOI18N
            if (LOG.isDebugEnabled()) {
                LOG.debug("featureBorder=" + featureBorder);                                               // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_BORDER corrupt or missing", t);                                              // NOI18N
            try {
                setFeatureBorder(new Float(getAttribValue("UMGEBUNG", mo, mc).toString()).floatValue());   // NOI18N
                if (LOG.isDebugEnabled()) {
                    LOG.debug("featureBorder=" + featureBorder);                                           // NOI18N
                }
            } catch (Exception tt) {
                LOG.info("UMGEBUNG corrupt or missing", tt);                                               // NOI18N
            }
        }
        try {
            final String fg = getAttribValue("FEATURE_FG", mo, mc).toString();                             // NOI18N
            final String[] t = fg.split(",");                                                              // NOI18N
            final int r = new Integer(t[0]).intValue();
            final int g = new Integer(t[1]).intValue();
            final int b = new Integer(t[2]).intValue();

            if (t.length == 4) {
                // The feature colour has an alpha component
                final int a = new Integer(t[3]).intValue();
                featureFG = new Color(r, g, b, a);
            } else {
                featureFG = new Color(r, g, b);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_FG=Color(" + r + "," + g + "," + b + ")"); // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_FG corrupt or missing", t);                     // NOI18N
        }
        try {
            final String s = getAttribValue("FEATURE_BG", mo, mc).toString(); // NOI18N
            final String[] t = s.split(",");                                  // NOI18N
            final int r = new Integer(t[0]).intValue();
            final int g = new Integer(t[1]).intValue();
            final int b = new Integer(t[2]).intValue();

            if (t.length == 4) {
                // The feature colour has an alpha component
                final int a = new Integer(t[3]).intValue();
                featureBG = new Color(r, g, b, a);
            } else {
                featureBG = new Color(r, g, b);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_BG=Color(" + r + "," + g + "," + b + ")");                       // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_BG corrupt or missing", t);                                           // NOI18N
        }
        try {
            final String s = getAttribValue("FEATURE_HIGH_FG", mo, mc).toString();                  // NOI18N
            final String[] t = s.split(",");                                                        // NOI18N
            final int r = new Integer(t[0]).intValue();
            final int g = new Integer(t[1]).intValue();
            final int b = new Integer(t[2]).intValue();
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_HIGH_FG=Color(" + r + "," + g + "," + b + ")");                  // NOI18N
            }
//            featureHighFG = new Color(r, g, b);
        } catch (Exception t) {
            LOG.info("FEATURE_HIGH_FG corrupt or missing", t);                                      // NOI18N
        }
        try {
            final String s = getAttribValue("FEATURE_HIGH_BG", mo, mc).toString();                  // NOI18N
            final String[] t = s.split(",");                                                        // NOI18N
            final int r = new Integer(t[0]).intValue();
            final int g = new Integer(t[1]).intValue();
            final int b = new Integer(t[2]).intValue();
//            featureHighFG = new Color(r, g, b);
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_HIGH_BG=Color(" + r + "," + g + "," + b + ")");                  // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_HIGH_BG corrupt or missing", t);                                      // NOI18N
        }
        try {
            final String path = getAttribValue("FEATURE_POINT_SYMBOL", mo, mc).toString();          // NOI18N
            pointSymbol = new javax.swing.ImageIcon(getClass().getResource(path)).getImage();
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_POINT_SYMBOL=" + path);                                          // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_POINT_SYMBOL Error", t);                                              // NOI18N
        }
        try {
            final String x = getAttribValue("FEATURE_POINT_SYMBOL_SWEETSPOT_X", mo, mc).toString(); // NOI18N
            pointSymbolSweetSpotX = new Double(x).doubleValue();
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_POINT_SYMBOL_SWEETSPOT_X=" + x);                                 // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_POINT_SYMBOL_SWEETSPOT_X Error", t);                                  // NOI18N
        }
        try {
            final String y = getAttribValue("FEATURE_POINT_SYMBOL_SWEETSPOT_Y", mo, mc).toString(); // NOI18N
            pointSymbolSweetSpotY = new Double(y).doubleValue();
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURE_POINT_SYMBOL_SWEETSPOT_Y=" + y);                                 // NOI18N
            }
        } catch (Exception t) {
            LOG.info("FEATURE_POINT_SYMBOL_SWEETSPOT_Y Error", t);                                  // NOI18N
        }
        try {
            final String supportingRasterService = String.valueOf(getAttribValue(
                        "FEATURESUPPORTINGRASTERSERVICE_TYPE",
                        mo,
                        mc));                                                                       // NOI18N
            final String supportingRasterServiceUrl = (String)getAttribValue(
                    "FEATURESUPPORTINGRASTERSERVICE_SIMPLEURL",
                    mo,
                    mc);                                                                            // NOI18N

            supportingRasterServiceRasterLayerName = (String)getAttribValue(
                    "FEATURESUPPORTINGRASTERSERVICE_RASTERLAYER",
                    mo,
                    mc);                                                                                       // NOI18N
            supportingRasterServiceRasterLayerPrefix = (String)getAttribValue(
                    "FEATURESUPPORTINGRASTERSERVICE_RASTERLAYERPREFIX",
                    mo,
                    mc);                                                                                       // NOI18N
            supportingRasterServiceRasterLayerPostfix = (String)getAttribValue(
                    "FEATURESUPPORTINGRASTERSERVICE_RASTERLAYERPOSTFIX",
                    mo,
                    mc);                                                                                       // NOI18N
            supportingRasterServiceIdAttributeName = (String)getAttribValue(
                    "FEATURESUPPORTINGRASTERSERVICE_ID_ATTRIBUTE",
                    mo,
                    mc);                                                                                       // NOI18N
            final String serviceName = (String)getAttribValue("FEATURESUPPORTINGRASTERSERVICE_NAME", mo, mc);  // NOI18N
            if (LOG.isDebugEnabled()) {
                LOG.debug("FEATURESUPPORTINGRASTERSERVICE_TYPE=" + supportingRasterService);                   // NOI18N
            }
            final Class c = BlacklistClassloading.forName(supportingRasterService);
            if (supportingRasterServiceUrl != null) {
                final SimpleFeatureSupporterRasterServiceUrl url = new SimpleFeatureSupporterRasterServiceUrl(
                        supportingRasterServiceUrl);
                final Constructor constructor = c.getConstructor(SimpleFeatureSupporterRasterServiceUrl.class);
                this.featureAwareRasterService = (FeatureAwareRasterService)constructor.newInstance(url);
            } else {
                final Constructor constructor = c.getConstructor();
                this.featureAwareRasterService = (FeatureAwareRasterService)constructor.newInstance();
            }
            featureAwareRasterService.setName(serviceName);
        } catch (Exception t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while creating the FeaureSupportingRasterService, or it does not exist.", t); // NOI18N
            }
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("VERSUCHE OBJECTACTIONSPROVIDER ZU SETZEN"); // NOI18N
            }                                                          // NOI18N

            final Class<?> clazz = ClassloadingHelper.getDynamicClass(
                    mc,
                    ClassloadingHelper.CLASS_TYPE.ACTION_PROVIDER);
            if (clazz != null) {
                final Constructor<?> constructor = clazz.getConstructor();
                final CidsBeanActionsProvider provider = (CidsBeanActionsProvider)constructor.newInstance();
                cidsBeanActions = provider.getActions();
                for (final CidsBeanAction cba : cidsBeanActions) {
                    cba.setCidsBean(getMetaObject().getBean());
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("HAT GEKLAPPT:" + clazz);              // NOI18N
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ACTION_PROVIDER corrupt or missing"); // NOI18N
                }
            }
        } catch (Exception t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(("ACTION_PROVIDER corrupt or missing"), t); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     * @param   mo    DOCUMENT ME!
     * @param   mc    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private Object getAttribValue(final String name, final MetaObject mo, final MetaClass mc) {
        if ((name == null) || (mo == null) || (mc == null)) {
            throw new IllegalArgumentException("no argument must be null"); // NOI18N
        }

        final Collection coa = mo.getAttributeByName(name, 1);
        final Collection cca = mc.getAttributeByName(name);

        if (LOG.isDebugEnabled()) {
            LOG.debug("mc.getAttributeByName(" + name + ")=" + cca); // NOI18N
        }

        if (coa.size() == 1) {
            final ObjectAttribute oa = (ObjectAttribute)(coa.toArray()[0]);
            return oa.getValue();
        } else if (cca.size() > 0) {
            final ClassAttribute ca = (ClassAttribute)(cca.toArray()[0]);
            return ca.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void setGeometry(final Geometry geom) {
        this.geom = geom;
    }

    @Override
    public float getTransparency() {
        float transparency = -1f;
        if (parentFeatureRenderer != null) {
            transparency = parentFeatureRenderer.getTransparency(this);
        } else if (featureRenderer != null) {
            transparency = featureRenderer.getTransparency();
        }
        return (transparency > 0) ? transparency : featureTranslucency;
    }

    @Override
    public Stroke getLineStyle() {
        if (subFeatures.isEmpty()) {
            if (parentFeatureRenderer != null) {
                return parentFeatureRenderer.getLineStyle(this);
            } else if (featureRenderer != null) {
                return featureRenderer.getLineStyle();
            }
        }
        return null;
    }

    @Override
    public Paint getLinePaint() {
        if (subFeatures.isEmpty()) {
            if (parentFeatureRenderer != null) {
                return parentFeatureRenderer.getLinePaint(this);
            } else if ((featureRenderer != null) && (featureRenderer.getLinePaint() != null)) {
                return featureRenderer.getLinePaint();
            } else {
                return featureFG;
            }
        } else {
            return new Color(255, 255, 255, 0);
        }
    }

    @Override
    public Geometry getGeometry() {
        return geom;
    }

    @Override
    public Paint getFillingPaint() {
        if (subFeatures.isEmpty()) {
            if (parentFeatureRenderer != null) {
                return parentFeatureRenderer.getFillingStyle(this);
            } else if ((featureRenderer != null) && (featureRenderer.getFillingStyle() != null)) {
                return featureRenderer.getFillingStyle();
            } else {
                return featureBG;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean canBeSelected() {
        return subFeatures.isEmpty();
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
    }

    @Override
    public void setHighlighting(final boolean highlighting) {
    }

    @Override
    public boolean getHighlighting() {
        return false;
    }

    @Override
    public String getName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getName() von " + mo);                 // NOI18N
        }
        try {
            if ((featureRenderer instanceof CustomCidsFeatureRenderer)
                        && (((CustomCidsFeatureRenderer)featureRenderer).getAlternativeName() != null)) {
                return ((CustomCidsFeatureRenderer)featureRenderer).getAlternativeName();
            } else {
                return mo.toString() + namenszusatz;
            }
        } catch (Exception t) {
            LOG.info("Error while identifying the name.", t); // NOI18N
            return null;
        }
    }

    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInfoComponent"); // NOI18N
        }

        if (parentFeatureRenderer != null) {
            return parentFeatureRenderer.getInfoComponent(refresh, this);
        } else if (featureRenderer != null) {
            return featureRenderer.getInfoComponent(refresh);
        } else {
            return null;
        }
    }

    @Override
    public ImageIcon getIconImage() {
        ImageIcon ii = null;
        try {
            ii = new ImageIcon(mc.getObjectIconData());
        } catch (Exception t) {
            LOG.info("Error on reading icon data. Trying to load class icon.", t); // NOI18N
            try {
                ii = new ImageIcon(mc.getIconData());
            } catch (Exception tt) {
                LOG.info("Error on reading icon data.", tt);                       // NOI18N

                ii = null;
            }
            ii = null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIconImage:" + ii); // NOI18N
        }
        return ii;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getFeatureBorder() {
        return featureBorder;
    }

    @Override
    public double getBuffer() {
        return featureBorder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureBorder  DOCUMENT ME!
     */
    public void setFeatureBorder(final float featureBorder) {
        this.featureBorder = featureBorder;
    }

    @Override
    public String getType() {
        return mc.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof CidsFeature)) {
            return false;
        } else {
            try {
                final String thisString = mo.getID() + "@" + mo.getMetaClass().getID() + "(" + this.renderFeatureString
                            + ")";                            // NOI18N
                final CidsFeature that = (CidsFeature)o;
                final String thatString = that.mo.getID() + "@" + that.mo.getMetaClass().getID() + "("
                            + that.renderFeatureString + ")"; // NOI18N
                final boolean parents = (getParentFeature() == that.getParentFeature())
                    ? true
                    : ((getParentFeature() == null) ? false : getParentFeature().equals(that.getParentFeature()));
                return thisString.equals(thatString) && parents;
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    public boolean isEditable() {
        return editable && (subFeatures.isEmpty());
    }

    @Override
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those
     * provided by <code>java.util.Hashtable</code>.
     *
     * <p>The general contract of <code>hashCode</code> is:</p>
     *
     * <ul>
     *   <li>Whenever it is invoked on the same object more than once during an execution of a Java application, the
     *     <tt>hashCode</tt> method must consistently return the same integer, provided no information used in <tt>
     *     equals</tt> comparisons on the object is modified. This integer need not remain consistent from one execution
     *     of an application to another execution of the same application.</li>
     *   <li>If two objects are equal according to the <tt>equals(Object)</tt> method, then calling the <code>
     *     hashCode</code> method on each of the two objects must produce the same integer result.</li>
     *   <li>It is <em>not</em> required that if two objects are unequal according to the
     *     {@link java.lang.Object#equals(java.lang.Object)} method, then calling the <tt>hashCode</tt> method on each
     *     of the two objects must produce distinct integer results. However, the programmer should be aware that
     *     producing distinct integer results for unequal objects may improve the performance of hashtables.</li>
     * </ul>
     *
     * <p>As much as is reasonably practical, the hashCode method defined by class <tt>Object</tt> does return distinct
     * integers for distinct objects. (This is typically implemented by converting the internal address of the object
     * into an integer, but this implementation technique is not required by the Java<font size="-2"><sup>TM</sup></font>
     * programming language.)</p>
     *
     * @return  a hash code value for this object.
     *
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    @Override
    public int hashCode() {
        int retValue;
        if (mo != null) {
            final String thisString = mo.getID() + "@" + mo.getMetaClass().getID() + "(" + this.renderFeatureString
                        + ")";
            retValue = thisString.hashCode();
            if (getParentFeature() != null) {
                retValue += parentFeature.hashCode();
            }
        } else {
            retValue = super.hashCode();
        }
        return retValue;
    }

    @Override
    public void hide(final boolean hiding) {
        this.hiding = hiding;
    }

    @Override
    public boolean isHidden() {
        return hiding;
    }

    @Override
    public void setSupportingRasterService(final FeatureAwareRasterService featureAwareRasterService) {
        this.featureAwareRasterService = featureAwareRasterService;
    }

    @Override
    public FeatureAwareRasterService getSupportingRasterService() {
        return featureAwareRasterService;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaObject getMetaObject() {
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaClass getMetaClass() {
        return mc;
    }

    @Override
    public String getFilterPart() {
        final ObjectAttribute oa = (ObjectAttribute)
            mo.getAttributeByName(supportingRasterServiceIdAttributeName, 1).toArray()[0];
        final String id = oa.getValue().toString();
        return supportingRasterServiceRasterLayerName + "@" + id + "@" + supportingRasterServiceLayerStyleName + ","; // NOI18N
    }

    @Override
    public String getSpecialLayerName() {
        if (supportingRasterServiceRasterLayerPrefix == null) {
            supportingRasterServiceRasterLayerPrefix = "";
        }

        if (supportingRasterServiceRasterLayerPostfix == null) {
            supportingRasterServiceRasterLayerPostfix = "";
        }

        if ((supportingRasterServiceRasterLayerName != null)
                    && supportingRasterServiceRasterLayerName.startsWith("cidsAttribute::")) {                         // NOI18N
            try {
                final String attrField = supportingRasterServiceRasterLayerName.substring("cidsAttribute::".length()); // NOI18N
                if (LOG.isDebugEnabled()) {
                    LOG.debug("FeatureSupportingRasterService:attrField:" + attrField);                                // NOI18N
                }
                final String ret = getMetaObject().getBean().getProperty(attrField).toString();

                return ((ret == null)
                        ? null
                        : (supportingRasterServiceRasterLayerPrefix + ret + supportingRasterServiceRasterLayerPostfix));
            } catch (Exception e) {
                LOG.error("AttrFieldProblem", e); // NOI18N
            }
        }

        return ((supportingRasterServiceRasterLayerName == null)
                ? null
                : (supportingRasterServiceRasterLayerPrefix + supportingRasterServiceRasterLayerName
                            + supportingRasterServiceRasterLayerPostfix));
    }

    @Override
    public Object clone() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CLONE"); // NOI18N
        }
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        if (parentFeatureRenderer != null) {
            return parentFeatureRenderer.getPointSymbol(this);
        } else if ((featureRenderer != null)
                    && (featureRenderer.getPointSymbol() != null)) {
            return featureRenderer.getPointSymbol();
        } else if (pointSymbol != null) {
            final FeatureAnnotationSymbol ret = new FeatureAnnotationSymbol(pointSymbol);
            ret.setSweetSpotX(pointSymbolSweetSpotX);
            ret.setSweetSpotY(pointSymbolSweetSpotY);
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public int getLineWidth() {
        if (subFeatures.isEmpty()) {
            if ((featureRenderer != null) && (featureRenderer.getLineStyle() != null)
                        && (featureRenderer.getLineStyle() instanceof BasicStroke)) {
                return (int)((BasicStroke)featureRenderer.getLineStyle()).getLineWidth();
            }
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void setFillingPaint(final Paint fillingStyle) {
    }

    @Override
    public void setLineWidth(final int width) {
    }

    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
    }

    @Override
    public void setTransparency(final float transparrency) {
        this.featureTranslucency = transparrency;
    }

    @Override
    public boolean isHighlightingEnabled() {
        return true;
    }

    @Override
    public void setHighlightingEnabled(final boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setLinePaint(final Paint linePaint) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public FeatureGroup getParentFeature() {
        return parentFeature;
    }

    @Override
    public void setParentFeature(final FeatureGroup parentFeature) {
        this.parentFeature = parentFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Feature> getSubFeatures() {
        return subFeatures;
    }

    @Override
    public Collection<Feature> getFeatures() {
        return subFeatures;
    }

    @Override
    public String getMyAttributeStringInParentFeature() {
        return myAttributeStringInParentFeature;
    }

    @Override
    public void setMyAttributeStringInParentFeature(final String myAttributeStringInParentFeature) {
        this.myAttributeStringInParentFeature = myAttributeStringInParentFeature;
    }

    @Override
    public Iterator<Feature> iterator() {
        return subFeatures.iterator();
    }

    @Override
    public boolean addFeature(final Feature toAdd) {
        return subFeatures.add(toAdd);
    }

    @Override
    public boolean addFeatures(final Collection<? extends Feature> toAdd) {
        return subFeatures.addAll(toAdd);
    }

    @Override
    public boolean removeFeature(final Feature toRemove) {
        return subFeatures.remove(toRemove);
    }

    @Override
    public boolean removeFeatures(final Collection<? extends Feature> toRemove) {
        return subFeatures.removeAll(toRemove);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public FeatureRenderer getFeatureRenderer() {
        return featureRenderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public SubFeatureAwareFeatureRenderer getParentFeatureRenderer() {
        return parentFeatureRenderer;
    }

    @Override
    public String toString() {
        return "CidsFeature<" + getMetaObject() + ">"; // NOI18N
    }

    @Override
    public Collection<CidsBeanAction> getActions() {
        return cidsBeanActions;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  infoNodeEnabled  the infoComponentDisabled to set
     */
    public void setInfoNodeEnabled(final boolean infoNodeEnabled) {
        this.infoNodeEnabled = infoNodeEnabled;
    }

    @Override
    public boolean hasInfoNode() {
        return infoNodeEnabled;
    }

    @Override
    public final ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
