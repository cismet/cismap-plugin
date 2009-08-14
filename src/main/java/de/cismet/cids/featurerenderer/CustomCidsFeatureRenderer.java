/*
 * CustomCidsFeatureRenderer.java
 *
 * Created on 22. August 2007, 14:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.featurerenderer;

import de.cismet.cismap.commons.features.FeatureRenderer;
import Sirius.navigator.exception.ConnectionException;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import de.cismet.cids.annotations.CidsAttribute;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.tools.StaticCidsUtilities;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import java.awt.Paint;
import java.awt.Stroke;
import java.lang.reflect.Field;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author hell
 */
public abstract class CustomCidsFeatureRenderer extends JPanel implements FeatureRenderer {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected MetaObject metaObject;
    protected MetaClass metaClass;
    protected Refreshable refreshable;
    protected CidsBean cidsBean;

    public void setMetaObject(MetaObject metaObject) throws ConnectionException {
        this.metaObject = metaObject;
        cidsBean=metaObject.getBean();
    //metaClass=SessionManager.getProxy().getMetaClass(metaObject.getClassKey());
    }

    public abstract void assign();

    public String getAlternativeName() {
        return null;
    }

    public float getTransparency() {
        return -1f;
    }

    public FeatureAnnotationSymbol getPointSymbol() {
        return null;
    }

    public Stroke getLineStyle() {
        return null;
    }

    public Paint getLinePaint() {
        return null;
    }

    public JComponent getInfoComponent(Refreshable refresh) {
        refreshable=refresh;
        refreshValues();
        return this;
    }

    public Paint getFillingStyle() {
        return null;
    }

    public void refreshValues() {
        Class customRenderer = this.getClass();
        try {
            Field[] fields = customRenderer.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(CidsAttribute.class)) {
                    try {
                        CidsAttribute ca = f.getAnnotation(CidsAttribute.class);
                        String attributeName = ca.value();
                        Object value = StaticCidsUtilities.getValueOfAttributeByString(attributeName, metaObject);
                        f.set(this, value);
                    } catch (Exception e) {
                        log.warn("Fehler beim Zuweisen im Renderer", e);
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
            log.fatal("Fehler in refrehhValues()", t);
        }
    }

    public float getInfoComponentTransparency() {
        return -1f;
    }

    public Refreshable getRefreshable() {
        return refreshable;
    }

}

