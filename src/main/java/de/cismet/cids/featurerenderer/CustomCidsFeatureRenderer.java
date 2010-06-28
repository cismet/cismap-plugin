/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CustomCidsFeatureRenderer.java
 *
 * Created on 22. August 2007, 14:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.featurerenderer;

import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import java.awt.Paint;
import java.awt.Stroke;

import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.cismet.cids.annotations.CidsAttribute;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.StaticCidsUtilities;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.FeatureRenderer;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.cismap.navigatorplugin.CidsFeature;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public abstract class CustomCidsFeatureRenderer extends JPanel implements SubFeatureAwareFeatureRenderer {

    //~ Instance fields --------------------------------------------------------

    protected MetaObject metaObject;
    protected MetaClass metaClass;
    protected Refreshable refreshable;
    protected CidsBean cidsBean;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject  DOCUMENT ME!
     *
     * @throws  ConnectionException  DOCUMENT ME!
     */
    public void setMetaObject(final MetaObject metaObject) throws ConnectionException {
        this.metaObject = metaObject;
        cidsBean = metaObject.getBean();
        // metaClass=SessionManager.getProxy().getMetaClass(metaObject.getClassKey());
    }

    /**
     * DOCUMENT ME!
     */
    public abstract void assign();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAlternativeName() {
        return null;
    }

    @Override
    public float getTransparency() {
        return -1f;
    }

    @Override
    public FeatureAnnotationSymbol getPointSymbol() {
        return null;
    }

    @Override
    public Stroke getLineStyle() {
        return null;
    }

    @Override
    public Paint getLinePaint() {
        return null;
    }

    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        refreshable = refresh;
        refreshValues();
        return this;
    }

    @Override
    public Paint getFillingStyle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshValues() {
        final Class customRenderer = this.getClass();
        try {
            final Field[] fields = customRenderer.getDeclaredFields();
            for (final Field f : fields) {
                if (f.isAnnotationPresent(CidsAttribute.class)) {
                    try {
                        final CidsAttribute ca = f.getAnnotation(CidsAttribute.class);
                        final String attributeName = ca.value();
                        final Object value = StaticCidsUtilities.getValueOfAttributeByString(attributeName, metaObject);
                        f.set(this, value);
                    } catch (Exception e) {
                        log.warn("Fehler beim Zuweisen im Renderer", e); // NOI18N
                    }
                }
//            else if (f.isAnnotationPresent(CidsRendererTitle.class)){
//                try {
//                    f.set(this,title);
//                } catch (Exception e) {
//                    log.warn("Fehler beim Zuweisen von RendererTitle im Renderer",e);
//                }
//            }
            }
            assign();
        } catch (Throwable t) {
            log.fatal("Error in refrehhValues()", t);                    // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getInfoComponentTransparency() {
        return -1f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Refreshable getRefreshable() {
        return refreshable;
    }

    @Override
    public Paint getFillingStyle(final CidsFeature subFeature) {
        return getFillingStyle();
    }

    @Override
    public JComponent getInfoComponent(final Refreshable refresh, final CidsFeature subFeature) {
        return getInfoComponent(refresh);
    }

    @Override
    public Paint getLinePaint(final CidsFeature subFeature) {
        return getLinePaint();
    }

    @Override
    public Stroke getLineStyle(final CidsFeature subFeature) {
        return getLineStyle();
    }

    @Override
    public FeatureAnnotationSymbol getPointSymbol(final CidsFeature subFeature) {
        return getPointSymbol();
    }

    @Override
    public float getTransparency(final CidsFeature subFeature) {
        return getTransparency();
    }
}
