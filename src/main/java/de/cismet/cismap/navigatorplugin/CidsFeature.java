/*
 * CidsFeature.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 5. Mai 2006, 12:02Ø
 *
 */
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.connection.SessionManager;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.featurerenderer.CustomCidsFeatureRenderer;
import de.cismet.cids.featurerenderer.SubFeatureAwareFeatureRenderer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.Bufferable;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureGroup;
import de.cismet.cismap.commons.features.FeatureGroups;
import de.cismet.cismap.commons.features.PureFeatureGroup;
import de.cismet.cismap.commons.features.FeatureRenderer;
import de.cismet.cismap.commons.features.Highlightable;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupporterRasterServiceUrl;
import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;
import de.cismet.tools.BlacklistClassloading;
import de.cismet.tools.collections.TypeSafeCollections;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CidsFeature implements XStyledFeature, Highlightable, Bufferable, RasterLayerSupportedFeature, FeatureGroup {

    private Paint featureFG = Color.black;
    private Paint featureBG = Color.gray;
//    private Paint featureHighFG = Color.blue;
//    private Paint featureHighBG = Color.darkGray;
    private float featureTranslucency = 0.5f;
    private float featureBorder = 10.0f;
//    private String[] renderFeatures = null;
    private String renderFeatureString = null;
    private String renderMultipleFeatures = null;
    private int renderAllFeatures = 1;
    private boolean hiding = false;
    private Geometry geom;
    private MetaObject mo;
    private MetaClass mc;
    // private MetaObjectNode mon;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean editable = false;
    private String namenszusatz = "";//NOI18N
    private FeatureRenderer featureRenderer = null;
    private SubFeatureAwareFeatureRenderer parentFeatureRenderer = null;
    private FeatureAwareRasterService featureAwareRasterService = null;
    private String supportingRasterServiceRasterLayerName = null;
    private String supportingRasterServiceIdAttributeName = null;
    private String supportingRasterServiceLayerStyleName = "default";//NOI18N
    private Image pointSymbol = null;
    private double pointSymbolSweetSpotX = 0d;
    private double pointSymbolSweetSpotY = 0d;
    private final Collection<Feature> subFeatures = TypeSafeCollections.newArrayList();
    //CidsFeature is FeatureGroup + SubFeature
    private FeatureGroup parentFeature = null;
    private String myAttributeStringInParentFeature = null;

    /**
     * Creates a new instance of CidsFeature
     * @param mon
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public CidsFeature(MetaObjectNode mon) throws IllegalArgumentException {
        this(mon.getObject());
    }

    public CidsFeature(MetaObject mo) throws IllegalArgumentException {
        this(mo, null);
    }

    /**
     * Creates a new instance of CidsFeature
     * @param mon
     * @throws java.lang.IllegalArgumentException
     */
    private CidsFeature(MetaObject mo, String localRenderFeatureString) throws IllegalArgumentException {
//        log.debug("New CIDSFEATURE");
//        log.fatal(mo + " of " + mo.getMetaClass());
        try {
//            this.mon = mon;
            this.mo = mo;
            this.mc = SessionManager.getProxy().getMetaClass(mo.getClassKey());
            initFeatureSettings();
            //renderFeature auswerten

            try {
                if (localRenderFeatureString != null) {
                    renderFeatureString = localRenderFeatureString;
                }

                if (renderFeatureString != null && !renderFeatureString.trim().equals("")) {//NOI18N
                    final String[] renderFeatures = renderFeatureString.split(",");//NOI18N
                    if (renderFeatures.length == 1) {
                        Object tester = mo.getBean().getProperty(renderFeatureString);
                        if (tester instanceof Collection) {
                            //single renderer attribute, multiple geometries case
                            createSubFeatures(renderFeatures);
                        } else if (tester instanceof Geometry) {
                            //old default case, single atribute and geometry
                            geom = (Geometry) tester;
                        } else if (tester instanceof CidsBean) {
                            geom = searchGeometryInMetaObject(((CidsBean)tester).getMetaObject());
                        } else {
                            log.debug("RENDER_FEATURE war fehlerhaft gesetzt. Geometrieattribut mit dem Namen: " + renderFeatureString + " konnte nicht gefunden werden");//NOI18N
                        }
                    } else {
                        //multi renderer attribute case
                        createSubFeatures(renderFeatures);
                    }
                }
            } catch (Exception e) {
                log.debug("RENDER_FEATURE war fehlerhaft gesetzt. Geometrieattribut mit dem Namen: " + renderFeatureString + " konnte nicht gefunden werden", e);//NOI18N
                geom = null;
            }

            if (geom == null) {
                //Defaultfall: Es ist kein Geometriefeld angegeben
                geom = searchGeometryInMetaObject(mo);
            }

        } catch (Throwable t) {
            log.error("Error CidsFeature(MetaObjectNode mon)", t);//NOI18N
            throw new IllegalArgumentException("Error on creating a CidsFeatures", t);//NOI18N
        }
    }

    private Geometry searchGeometryInMetaObject(MetaObject mo) {
        Collection c = mo.getAttributesByType(Geometry.class, 1);
        for (Object elem : c) {
            ObjectAttribute oa = (ObjectAttribute) elem;
            return (Geometry) oa.getValue();
        }
        return null;
    }

    private void createSubFeatures(String[] renderFeatures) {
        for (String renderFeature : renderFeatures) {
            Object tester = mo.getBean().getProperty(renderFeature);
            if (tester instanceof Geometry) {
                CidsFeature cf = new CidsFeature(this.getMetaObject(), renderFeature);
                cf.setParentFeature(this);
                cf.setMyAttributeStringInParentFeature(renderFeature);
                subFeatures.add(cf);
            } else if (tester instanceof Collection) {
                Collection<CidsBean> cbc = (Collection<CidsBean>) tester;
                final PureFeatureGroup fg = new PureFeatureGroup();
                for (CidsBean cb : cbc) {
                    CidsFeature cf = new CidsFeature(cb.getMetaObject());
                    cf.setParentFeature(this); //first we had fg here ;-)
                    cf.setMyAttributeStringInParentFeature(renderFeature);
                    fg.addFeature(cf);
                }
                subFeatures.add(fg);
                fg.setParentFeature(this);
                fg.setMyAttributeStringInParentFeature(renderFeature);
            }

        }
        geom = FeatureGroups.getEnclosingGeometry(subFeatures);
        hide(true);
    }

    @Deprecated
    public CidsFeature(MetaObjectNode mon, ObjectAttribute oAttr) throws IllegalArgumentException {
        this(mon.getObject(), oAttr.getMai().getFieldName());
    }

//    public CidsFeature(MetaObject mo, String property) throws IllegalArgumentException {
//        log.debug("New CIDSFEATURE");
//        try {
////            this.mon = mon;
//            this.mo = mo;
//            this.mc = mo.getMetaClass();
//            initFeatureSettings();
//
//            //TODO noch irgendwie sinnvoll den namenszusatz f\u00FCllen
//            Object test = mo.getBean().getProperty(property);
//            if (test instanceof Geometry) {
//                geom = (Geometry) test;
//                if (geom == null) {
//                    throw new IllegalArgumentException("Geometry==null");
//                }
//            } else {
//                throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.Keine_Geometrie_im_�bergebenen_ObjectAttribute."));
//            }
//        } catch (Throwable t) {
//            throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CidsFeature.Fehler_beim_Erstellen_eines_CidsFeatures"), t);
//        }
//    }
    private void initFeatureSettings() throws Throwable {
        if (CismapBroker.getInstance().getMappingComponent().getMappingModel() instanceof ActiveLayerModel) {
            if (((ActiveLayerModel) CismapBroker.getInstance().getMappingComponent().getMappingModel()).getSrs().equalsIgnoreCase("epsg:4326")) {//NOI18N
                featureBorder = 0.001f;
            }
        }
        try {
            renderFeatureString = getAttribValue("RENDER_FEATURE", mo, mc).toString();//NOI18N
            log.debug("RENDER_FEATURE=" + renderFeatureString);//NOI18N
        } catch (Throwable t) {
            log.info("RENDER_FEATURE corrupt or missing", t);//NOI18N
        }
        try {
            renderMultipleFeatures = getAttribValue("RENDER_MULTIPLE_FEATURES", mo, mc).toString();//NOI18N
            log.debug("RENDER_MULTIPLE_FEATURES=" + renderMultipleFeatures);//NOI18N
        } catch (Throwable t) {
            log.info("RENDER_MULTIPLE_FEATURES corrupt or missing", t);//NOI18N
        }
        try {
            renderAllFeatures = new Integer(getAttribValue("RENDER_ALL_FEATURES", mo, mc).toString()).intValue();//NOI18N
            log.debug("renderAllFeatures=" + renderAllFeatures);//NOI18N
        } catch (Throwable t) {
            log.info("RENDER_AKK_FEATURES corrupt or missing", t);//NOI18N
        }
        try {
//            hiding = new Boolean(getAttribValue("HIDE_FEATURE", mo, mc).toString()).booleanValue();
            log.debug("HIDE_FEATURE=" + hiding);//NOI18N
        } catch (Throwable t) {
            log.info(("HIDE_FEATURE corrupt or missing"), t);//NOI18N
        }
        try {
            log.debug("VERSUCHE FEATURERENDERER ZU SETZEN");//NOI18N
            String overrideFeatureRendererClassName = System.getProperty(mo.getDomain().toLowerCase() + "." + mo.getMetaClass().getTableName().toLowerCase() + ".featurerenderer");//NOI18N

            String featureRendererClass = overrideFeatureRendererClassName;
            if (featureRendererClass == null) {
                String mcName = mo.getMetaClass().getTableName();
                featureRendererClass = "de.cismet.cids.custom.featurerenderer." + mo.getDomain().toLowerCase() + "." + mcName.substring(0, 1).toUpperCase() + mcName.substring(1).toLowerCase() + "FeatureRenderer";//NOI18N
            }
            log.debug("FEATURE_RENDERER=" + featureRendererClass);//NOI18N
            Class c = BlacklistClassloading.forName(featureRendererClass);
            if (c == null) {
                c = BlacklistClassloading.forName((String) getAttribValue("FEATURE_RENDERER", mo, mc));//NOI18N
            }
            Constructor constructor = c.getConstructor();
            featureRenderer = (FeatureRenderer) constructor.newInstance();
            ((CustomCidsFeatureRenderer) featureRenderer).setMetaObject(mo);
            //Method assignM=assigner.getMethod("assign", new Class[] {Connection.class,String[].class, UniversalContainer.class});
            log.debug("HAT GEKLAPPT:" + featureRendererClass);//NOI18N
        } catch (Throwable t) {
            log.warn(("FEATURE_RENDERER corrupt or missing"), t);//NOI18N
        }
        try {
            float featureTranslucencyValue = new Float(getAttribValue("FEATURE_TRANSLUCENCY", mo, mc).toString()).floatValue();//NOI18N
            log.debug("FEATURE_TRANSLUCENCY=" + featureTranslucencyValue);//NOI18N
            featureTranslucency = featureTranslucencyValue;
        } catch (Throwable t) {
            log.info("FEATURE_TRANSLUCENCY corrupt or missing", t);//NOI18N
        }
        try {
            setFeatureBorder(new Float(getAttribValue("FEATURE_BORDER", mo, mc).toString()).floatValue());//NOI18N
            log.debug("featureBorder=" + featureBorder);//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_BORDER corrupt or missing", t);//NOI18N
            try {
                setFeatureBorder(new Float(getAttribValue("UMGEBUNG", mo, mc).toString()).floatValue());//NOI18N
                log.debug("featureBorder=" + featureBorder);//NOI18N
            } catch (Throwable tt) {
                log.info("UMGEBUNG corrupt or missing", tt);//NOI18N
            }
        }
        try {
            String fg = getAttribValue("FEATURE_FG", mo, mc).toString();//NOI18N
            String[] t = fg.split(",");//NOI18N
            int r = new Integer(t[0]).intValue();
            int g = new Integer(t[1]).intValue();
            int b = new Integer(t[2]).intValue();
            featureFG = new Color(r, g, b);
            log.debug("FEATURE_FG=Color(" + r + "," + g + "," + b + ")");//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_FG corrupt or missing", t);//NOI18N
        }
        try {
            String s = getAttribValue("FEATURE_BG", mo, mc).toString();//NOI18N
            String[] t = s.split(",");//NOI18N
            int r = new Integer(t[0]).intValue();
            int g = new Integer(t[1]).intValue();
            int b = new Integer(t[2]).intValue();
            featureBG = new Color(r, g, b);
            log.debug("FEATURE_BG=Color(" + r + "," + g + "," + b + ")");//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_BG corrupt or missing", t);//NOI18N
        }
        try {
            String s = getAttribValue("FEATURE_HIGH_FG", mo, mc).toString();//NOI18N
            String[] t = s.split(",");//NOI18N
            int r = new Integer(t[0]).intValue();
            int g = new Integer(t[1]).intValue();
            int b = new Integer(t[2]).intValue();
            log.debug("FEATURE_HIGH_FG=Color(" + r + "," + g + "," + b + ")");//NOI18N
//            featureHighFG = new Color(r, g, b);
        } catch (Throwable t) {
            log.info("FEATURE_HIGH_FG corrupt or missing", t);//NOI18N
        }
        try {
            String s = getAttribValue("FEATURE_HIGH_BG", mo, mc).toString();//NOI18N
            String[] t = s.split(",");//NOI18N
            int r = new Integer(t[0]).intValue();
            int g = new Integer(t[1]).intValue();
            int b = new Integer(t[2]).intValue();
//            featureHighFG = new Color(r, g, b);
            log.debug("FEATURE_HIGH_BG=Color(" + r + "," + g + "," + b + ")");//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_HIGH_BG corrupt or missing", t);//NOI18N
        }
        try {
            String path = getAttribValue("FEATURE_POINT_SYMBOL", mo, mc).toString();//NOI18N
            pointSymbol = new javax.swing.ImageIcon(getClass().getResource(path)).getImage();
            log.debug("FEATURE_POINT_SYMBOL=" + path);//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_POINT_SYMBOL Error", t);//NOI18N
        }
        try {
            String x = getAttribValue("FEATURE_POINT_SYMBOL_SWEETSPOT_X", mo, mc).toString();//NOI18N
            pointSymbolSweetSpotX = new Double(x).doubleValue();
            log.debug("FEATURE_POINT_SYMBOL_SWEETSPOT_X=" + x);//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_POINT_SYMBOL_SWEETSPOT_X Error", t);//NOI18N
        }
        try {
            String y = getAttribValue("FEATURE_POINT_SYMBOL_SWEETSPOT_Y", mo, mc).toString();//NOI18N
            pointSymbolSweetSpotY = new Double(y).doubleValue();
            log.debug("FEATURE_POINT_SYMBOL_SWEETSPOT_Y=" + y);//NOI18N
        } catch (Throwable t) {
            log.info("FEATURE_POINT_SYMBOL_SWEETSPOT_Y Error", t);//NOI18N
        }
        try {
            String supportingRasterService = String.valueOf(getAttribValue("FEATURESUPPORTINGRASTERSERVICE_TYPE", mo, mc));//NOI18N
            String supportingRasterServiceUrl = (String) getAttribValue("FEATURESUPPORTINGRASTERSERVICE_SIMPLEURL", mo, mc);//NOI18N

            supportingRasterServiceRasterLayerName = (String) getAttribValue("FEATURESUPPORTINGRASTERSERVICE_RASTERLAYER", mo, mc);//NOI18N
            supportingRasterServiceIdAttributeName = (String) getAttribValue("FEATURESUPPORTINGRASTERSERVICE_ID_ATTRIBUTE", mo, mc);//NOI18N
            String serviceName = (String) getAttribValue("FEATURESUPPORTINGRASTERSERVICE_NAME", mo, mc);//NOI18N
            log.debug("FEATURESUPPORTINGRASTERSERVICE_TYPE=" + supportingRasterService);//NOI18N
            Class c = BlacklistClassloading.forName(supportingRasterService);
            if (supportingRasterServiceUrl != null) {
                SimpleFeatureSupporterRasterServiceUrl url = new SimpleFeatureSupporterRasterServiceUrl(supportingRasterServiceUrl);
                Constructor constructor = c.getConstructor(SimpleFeatureSupporterRasterServiceUrl.class);
                this.featureAwareRasterService = (FeatureAwareRasterService) constructor.newInstance(url);
            } else {
                Constructor constructor = c.getConstructor();
                this.featureAwareRasterService = (FeatureAwareRasterService) constructor.newInstance();
            }
            featureAwareRasterService.setName(serviceName);
        } catch (Throwable t) {
            log.debug("Error while creating the FeaureSupportingRasterService, or it does not exist.", t);//NOI18N
        }
    }

    private Object getAttribValue(String name, MetaObject mo, MetaClass mc) {
        Collection coa = mo.getAttributeByName(name, 1);
        Collection cca = mc.getAttributeByName(name);
        log.debug("mc.getAttributeByName(" + name + ")=" + cca);//NOI18N
        if (coa.size() == 1) {
            ObjectAttribute oa = (ObjectAttribute) (coa.toArray()[0]);
            return oa.getValue();
        } else if (cca.size() > 0) {
            ClassAttribute ca = (ClassAttribute) (cca.toArray()[0]);
            return ca.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void setGeometry(Geometry geom) {
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
        return transparency > 0 ? transparency : featureTranslucency;
    }

    @Override
    public Stroke getLineStyle() {
        if (subFeatures.size() == 0) {
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
        if (subFeatures.size() == 0) {
            if (parentFeatureRenderer != null) {
                return parentFeatureRenderer.getLinePaint(this);
            } else if (featureRenderer != null && featureRenderer.getLinePaint() != null) {
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
        if (subFeatures.size() == 0) {
            if (parentFeatureRenderer != null) {
                return parentFeatureRenderer.getFillingStyle(this);
            } else if (featureRenderer != null && featureRenderer.getFillingStyle() != null) {
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
        return subFeatures.size() == 0;
    }

    @Override
    public void setCanBeSelected(boolean canBeSelected) {
    }

    @Override
    public void setHighlighting(boolean highlighting) {
    }

    @Override
    public boolean getHighlighting() {
        return false;
    }

    @Override
    public String getName() {
        log.debug("getName() von " + mo);//NOI18N
        try {
            if (featureRenderer instanceof CustomCidsFeatureRenderer && ((CustomCidsFeatureRenderer) featureRenderer).getAlternativeName() != null) {
                return ((CustomCidsFeatureRenderer) featureRenderer).getAlternativeName();
            } else {
                return mo.toString() + namenszusatz;
            }
        } catch (Throwable t) {
            log.info("Error while identifying the name.", t);//NOI18N
            return null;
        }
    }

    @Override
    public JComponent getInfoComponent(Refreshable refresh) {
        log.debug("getInfoComponent");//NOI18N
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
        } catch (Throwable t) {
            log.info("Error on reading icon data. Trying to load class icon.", t);//NOI18N
            try {
                ii = new ImageIcon(mc.getIconData());
            } catch (Throwable tt) {
                log.info("Error on reading icon data.", tt);//NOI18N

                ii = null;
            }
            ii = null;
        }
        log.debug("getIconImage:" + ii);//NOI18N
        return ii;
    }

    public float getFeatureBorder() {
        return featureBorder;
    }

    @Override
    public double getBuffer() {
        return featureBorder;
    }

    public void setFeatureBorder(float featureBorder) {
        this.featureBorder = featureBorder;
    }

    @Override
    public String getType() {
        return mc.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CidsFeature)) {
            return false;
        } else {
            try {
                String thisString = mo.getID() + "@" + mo.getMetaClass().getID();//NOI18N
                String thatString = ((CidsFeature) o).mo.getID() + "@" + ((CidsFeature) o).mo.getMetaClass().getID();//NOI18N
                return thisString.equals(thatString);
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    public boolean isEditable() {
        return editable && subFeatures.size() == 0;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * <p>
     * The general contract of <code>hashCode</code> is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the <tt>hashCode</tt> method
     *     must consistently return the same integer, provided no information
     *     used in <tt>equals</tt> comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the <tt>equals(Object)</tt>
     *     method, then calling the <code>hashCode</code> method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link java.lang.Object#equals(java.lang.Object)}
     *     method, then calling the <tt>hashCode</tt> method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hashtables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class <tt>Object</tt> does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java<font size="-2"><sup>TM</sup></font> programming language.)
     *
     *
     * @return a hash code value for this object.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.util.Hashtable
     */
    @Override
    public int hashCode() {
        int retValue;
        if (mo != null) {
            retValue = mo.hashCode();
        } else {
            retValue = super.hashCode();
        }
        return retValue;
    }

    @Override
    public void hide(boolean hiding) {
        this.hiding = hiding;
    }

    @Override
    public boolean isHidden() {
//        if (subFeatures.size() == 0) {
//            return hiding;
//        } else {
//            return true;
//        }
        return false;
    }

    @Override
    public void setSupportingRasterService(FeatureAwareRasterService featureAwareRasterService) {
        this.featureAwareRasterService = featureAwareRasterService;
    }

    @Override
    public FeatureAwareRasterService getSupportingRasterService() {
//        if (featureAwareRasterService == null) {
//            try {
//                String tablename = this.getMetaClass().getTableName();
//                String domain = this.getMetaClass().getDomain();
//                String lazyClassName = "de.cismet.cids.custom.featuresupportingrasterservices." +
//                        domain.toLowerCase() + "." + tablename.substring(0, 1).toUpperCase() +
//                        tablename.substring(1).toLowerCase() + "FeatureSupportingRasterService";
//                Class lazyClass = BlacklistClassloading.forName(lazyClassName);
//                Constructor constructor = lazyClass.getConstructor();
//                featureAwareRasterService = (FeatureAwareRasterService) constructor.newInstance();
//            } catch (Throwable t) {
//            }
//        }
        return featureAwareRasterService;
    }

    public MetaObject getMetaObject() {
        return mo;
    }

    public MetaClass getMetaClass() {
        return mc;
    }

    @Override
    public String getFilterPart() {
        ObjectAttribute oa = (ObjectAttribute) mo.getAttributeByName(supportingRasterServiceIdAttributeName, 1).toArray()[0];
        String id = oa.getValue().toString();
        return supportingRasterServiceRasterLayerName + "@" + id + "@" + supportingRasterServiceLayerStyleName + ",";//NOI18N
    }

    @Override
    public String getSpecialLayerName() {
        if (supportingRasterServiceRasterLayerName != null && supportingRasterServiceRasterLayerName.startsWith("cidsAttribute::")) {//NOI18N
            try {
                String attrField = supportingRasterServiceRasterLayerName.substring("cidsAttribute::".length());//NOI18N
                log.fatal("attrField" + attrField);//NOI18N
                String ret = getMetaObject().getBean().getProperty(attrField).toString();
                return ret;
            } catch (Exception e) {
                log.error("AttrFieldProblem", e);//NOI18N
            }

        }
        return supportingRasterServiceRasterLayerName;

    }

    @Override
    public Object clone() {
        log.debug("CLONE");//NOI18N
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
        } else if (featureRenderer != null
                && featureRenderer.getPointSymbol() != null) {
            return featureRenderer.getPointSymbol();
        } else if (pointSymbol != null) {
            FeatureAnnotationSymbol ret = new FeatureAnnotationSymbol(pointSymbol);
            ret.setSweetSpotX(pointSymbolSweetSpotX);
            ret.setSweetSpotY(pointSymbolSweetSpotY);
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public int getLineWidth() {
        if (subFeatures.size() == 0) {
            if (featureRenderer != null && featureRenderer.getLineStyle() != null && featureRenderer.getLineStyle() instanceof BasicStroke) {
                return (int) ((BasicStroke) featureRenderer.getLineStyle()).getLineWidth();
            }
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void setFillingPaint(Paint fillingStyle) {
    }

    @Override
    public void setLineWidth(int width) {
    }

    @Override
    public void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol) {
    }

    @Override
    public void setTransparency(float transparrency) {
        this.featureTranslucency = transparrency;
    }

    @Override
    public boolean isHighlightingEnabled() {
        return true;
    }

    @Override
    public void setHighlightingEnabled(boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    @Override
    public void setLinePaint(Paint linePaint) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    @Override
    public FeatureGroup getParentFeature() {
        return parentFeature;
    }

    @Override
    public void setParentFeature(FeatureGroup parentFeature) {
        this.parentFeature = parentFeature;
        if (parentFeature instanceof CidsFeature) {
            CidsFeature cidsParent = (CidsFeature) parentFeature;
            FeatureRenderer fr = cidsParent.getFeatureRenderer();
            if (fr instanceof SubFeatureAwareFeatureRenderer) {
                parentFeatureRenderer = (SubFeatureAwareFeatureRenderer) fr;
            }
        }
    }

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
    public void setMyAttributeStringInParentFeature(String myAttributeStringInParentFeature) {
        this.myAttributeStringInParentFeature = myAttributeStringInParentFeature;
    }

    @Override
    public Iterator<Feature> iterator() {
        return subFeatures.iterator();
    }

    @Override
    public boolean addFeature(Feature toAdd) {
        return subFeatures.add(toAdd);
    }

    @Override
    public boolean addFeatures(Collection<? extends Feature> toAdd) {
        return subFeatures.addAll(toAdd);
    }

    @Override
    public boolean removeFeature(Feature toRemove) {
        return subFeatures.remove(toRemove);
    }

    @Override
    public boolean removeFeatures(Collection<? extends Feature> toRemove) {
        return subFeatures.removeAll(toRemove);
    }

    public FeatureRenderer getFeatureRenderer() {
        return featureRenderer;
    }

    @Override
    public String toString() {
        return "CidsFeature<" + getMetaObject() + ">";//NOI18N
    }
}
