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

import org.deegree.style.se.unevaluated.Style;

import org.jdom.Element;

import org.openide.util.Exceptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import java.util.LinkedList;
import java.util.Map;

import javax.swing.Icon;

import javax.xml.stream.XMLInputFactory;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.builtin.CidsLayerSearchStatement;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.SLDStyledLayer;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CidsLayer extends AbstractFeatureService<CidsLayerFeature, CidsLayerSearchStatement>
        implements SLDStyledLayer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsLayer.class);

    public static final String CIDS_FEATURELAYER_TYPE = "cidsFeatureService";

    //~ Instance fields --------------------------------------------------------

    final XMLInputFactory factory = XMLInputFactory.newInstance();
    String sldDefinition;

    private CidsLayerSearchStatement cidsStatement;
    private String tableName;
    private MetaClass metaClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayer object.
     *
     * @param  cl  DOCUMENT ME!
     */
    public CidsLayer(final CidsLayer cl) {
        super(cl);
        tableName = cl.tableName;
        cidsStatement = cl.getQuery();
        metaClass = cl.metaClass;
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
        // sldDefinition = new InputStreamReader(getClass().getResourceAsStream("/testSLD.xml"));
        // name = "CidsLayer"; cidsStatement = new CidsLayerSearchStatement();
    }

    /**
     * Creates a new CidsLayer object.
     *
     * @param  clazz  DOCUMENT ME!
     */
    public CidsLayer(final MetaClass clazz) {
        super();
        name = clazz.getName();
        tableName = clazz.getTableName();
        cidsStatement = new CidsLayerSearchStatement(clazz);
        metaClass = clazz;
        // sldDefinition = new InputStreamReader(getClass().getResourceAsStream("/testSLD.xml"));
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
        final Map<String, LinkedList<Style>> styles = parseSLD(getSLDDefiniton());
        featureFactory = new CidsFeatureFactory(metaClass, this.getLayerProperties(), styles);
        return featureFactory;
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
        final Element className = new Element("className");
        className.setText(tableName);
        parentElement.addContent(className);
        
            try {
                final Document sldDoc = new org.jdom.input.SAXBuilder().build(getSLDDefiniton());
                parentElement.addContent(sldDoc.detachRootElement());
            } catch (JDOMException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        return parentElement;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        try {
            super.initFromElement(element);
            tableName = element.getChildText("className").trim();
            metaClass = ClassCacheMultiple.getMetaClass(SessionManager.getSession().getUser().getDomain(),
                    tableName);
            if (metaClass == null) {
                return;
            }
            // this.setName(clazz.getName());
            setQuery(new CidsLayerSearchStatement(metaClass));
            final Element sldStyle = element.getChild("StyledLayerDescriptor", Namespace.getNamespace("http://www.opengis.net/sld"));
            if(sldStyle != null) {
                sldDefinition = new org.jdom.output.XMLOutputter().outputString(sldStyle);
            }
        } catch (ConnectionException ex) {
            LOG.error("Configuration could not be loaded", ex);
        }
    }

    @Override
    public void setSLDInputStream(final String inputStream) {
        sldDefinition = inputStream;
        final Map<String, LinkedList<Style>> styles = parseSLD(new StringReader(inputStream));
        if (styles.isEmpty()) {
            return;
        }
        ((CidsFeatureFactory)featureFactory).styles = styles;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   input  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Map<String, LinkedList<Style>> parseSLD(final Reader input) {
        Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = null;
        try {
            styles = org.deegree.style.persistence.sld.SLDParser.getStyles(factory.createXMLStreamReader(
                        input));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        if (styles == null) {
            LOG.info("SLD Parser funtkioniert nicht");
        }
        return styles;
    }

    @Override
    public Reader getSLDDefiniton() {
        return (sldDefinition == null) ? new InputStreamReader(
getClass().getResourceAsStream("/testSLD.xml")) : new StringReader(sldDefinition);
    }
}
