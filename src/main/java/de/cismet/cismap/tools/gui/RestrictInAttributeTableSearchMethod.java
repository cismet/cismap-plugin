/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.tools.gui;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import org.openide.util.NbBundle;

import java.io.StringReader;

import de.cismet.cids.search.QuerySearch;
import de.cismet.cids.search.QuerySearchMethod;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RestrictInAttributeTableSearchMethod implements QuerySearchMethod {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SelectInAttributeTableSearchMethod.class);

    //~ Instance fields --------------------------------------------------------

    private QuerySearch querySearch;
    private boolean searching = false;
    private AttributeTable table;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectInAttributeTableSearchMethod object.
     *
     * @param  table  DOCUMENT ME!
     */
    public RestrictInAttributeTableSearchMethod(final AttributeTable table) {
        this.table = table;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setQuerySearch(final QuerySearch querySearch) {
        this.querySearch = querySearch;
    }

    @Override
    public void actionPerformed(final Object layer, final String query) {
        if (LOG.isInfoEnabled()) {
            LOG.info((searching ? "Cancel" : "Search") + " button was clicked.");
        }

        querySearch.setControlsAccordingToState(searching);
        if ((query != null) && !query.isEmpty()) {
            if (layer instanceof WebFeatureService) {
                final WebFeatureService wfs = (WebFeatureService)layer;
                try {
                    final Element e = (Element)wfs.getQueryElement().clone();
                    final Element queryElement = e.getChild(
                            "Query",
                            Namespace.getNamespace("wfs", "http://www.opengis.net/wfs"));
                    queryElement.removeChild("Filter", Namespace.getNamespace("ogc", "http://www.opengis.net/ogc"));
                    final Element filterElement = new Element(
                            "Filter",
                            Namespace.getNamespace("ogc", "http://www.opengis.net/ogc"));
                    final SAXBuilder builder = new SAXBuilder();
                    final Document d = builder.build(new StringReader(query));
                    filterElement.addContent((Element)d.getRootElement().clone());
                    queryElement.addContent(0, filterElement);
                    table.setQuery(FeatureServiceUtilities.elementToString(e));
                } catch (Exception ex) {
                    LOG.error("Error while retrieving features", ex);
                }
            } else if (layer instanceof AbstractFeatureService) {
                table.setQuery(query);
            }
        } else {
            table.setQuery(null);
        }
        searching = false;
        querySearch.setControlsAccordingToState(searching);
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                RestrictInAttributeTableSearchMethod.class,
                "RestrictInAttributeTableSearchMethod.toString");
    }
}
