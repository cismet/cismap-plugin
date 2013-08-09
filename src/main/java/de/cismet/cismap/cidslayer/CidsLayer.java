/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.cidslayer;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;

import org.apache.log4j.Logger;

import org.deegree.security.session.Session;

import org.jdom.Element;

import org.openide.util.Exceptions;

import javax.swing.Icon;

import de.cismet.cids.server.search.builtin.CidsLayerSearchStatement;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CidsLayer extends AbstractFeatureService<CidsLayerFeature, CidsLayerSearchStatement> {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsLayer.class);

    public static final String CIDS_FEATURELAYER_TYPE = "cidsFeatureService";

    //~ Instance fields --------------------------------------------------------

    private CidsLayerSearchStatement cidsStatement;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayer object.
     *
     * @param  cl  DOCUMENT ME!
     */
    public CidsLayer(final CidsLayer cl) {
        super(cl);
        cidsStatement = cl.getQuery();
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
        //name = "CidsLayer";
        //cidsStatement = new CidsLayerSearchStatement();
    }

    /**
     * Creates a new CidsLayer object.
     *
     * @param  clazz  DOCUMENT ME!
     */
    public CidsLayer(final MetaClass clazz) {
        super();
        name = clazz.getName();
        cidsStatement = new CidsLayerSearchStatement(clazz);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
        defaultLayerProperties.getStyle()
                .setPointSymbol(new FeatureAnnotationSymbol(
                        new javax.swing.ImageIcon(
                            getClass().getResource(
                                "/de/cismet/cismap/commons/gui/res/pushpin.png")).getImage()));
        return defaultLayerProperties;
    }

    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        return new CidsFeatureFactory(this.getLayerProperties());
    }

    @Override
    public CidsLayerSearchStatement getQuery() {
        return cidsStatement;
    }

    @Override
    public void setQuery(final CidsLayerSearchStatement query) {
        cidsStatement = query;
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
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
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
        final Element classId = new Element("classId");
        classId.setText(String.valueOf(cidsStatement.getClassId()));
        parentElement.addContent(classId);
        return parentElement;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        try {
            super.initFromElement(element);
            final int classId = Integer.parseInt(element.getChildText("classId").trim());
            final MetaClass clazz = SessionManager.getConnection()
                        .getMetaClass(SessionManager.getSession().getUser(),
                            classId,
                            SessionManager.getSession().getUser().getDomain());
            if (clazz == null) {
                return;
            }
            //this.setName(clazz.getName());
            setQuery(new CidsLayerSearchStatement(clazz));
        } catch (ConnectionException ex) {
            LOG.error("Configuration could not be loaded", ex);
        }
    }
}
