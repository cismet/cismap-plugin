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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.SwingWorker;

import de.cismet.cids.search.QuerySearch;
import de.cismet.cids.search.QuerySearchMethod;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.concurrency.CismetExecutors;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RemoveFromSelectionAttributeTableSearchMethod implements QuerySearchMethod {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RemoveFromSelectionAttributeTableSearchMethod.class);

    //~ Instance fields --------------------------------------------------------

    private QuerySearch querySearch;
    private boolean searching = false;
    private SearchAndSelectThread searchThread;
    private Object lastLayer;
    private AttributeTable table;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectInAttributeTableSearchMethod object.
     *
     * @param  table  DOCUMENT ME!
     */
    public RemoveFromSelectionAttributeTableSearchMethod(final AttributeTable table) {
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

        if (searching) {
            if (searchThread != null) {
                if (lastLayer instanceof AbstractFeatureService) {
                    final FeatureFactory ff = ((AbstractFeatureService)lastLayer).getFeatureFactory();
                    if (ff instanceof AbstractFeatureFactory) {
                        ((AbstractFeatureFactory)ff).waitUntilInterruptedIsAllowed();
                    }
                }
                searchThread.cancel(true);
            }
        } else {
            lastLayer = layer;
            searchThread = new SearchAndSelectThread(layer, query);
            CismetExecutors.newSingleThreadExecutor().submit(searchThread);

            searching = true;
            querySearch.setControlsAccordingToState(searching);
        }
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                RemoveFromSelectionAttributeTableSearchMethod.class,
                "RemoveFromSelectionAttributeTableSearchMethod.toString");
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SearchAndSelectThread extends SwingWorker<List<Feature>, Void> {

        //~ Instance fields ----------------------------------------------------

        private final Object layer;
        private final String query;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SearchAndSelectThread object.
         *
         * @param  layer  DOCUMENT ME!
         * @param  query  DOCUMENT ME!
         */
        public SearchAndSelectThread(final Object layer, final String query) {
            this.layer = layer;
            this.query = query;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected List<Feature> doInBackground() throws Exception {
            List<Feature> features = null;

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
                    features = wfs.getFeatureFactory()
                                .createFeatures(FeatureServiceUtilities.elementToString(e),
                                        null,
                                        null,
                                        0,
                                        0,
                                        null);
                } catch (Exception ex) {
                    LOG.error("Error while retrieving features", ex);
                }
            } else if (layer instanceof AbstractFeatureService) {
                final AbstractFeatureService fs = (AbstractFeatureService)layer;
                features = fs.getFeatureFactory().createFeatures(
                        query,
                        null,
                        null,
                        0,
                        0,
                        null);
            }

            return features;
        }

        @Override
        protected void done() {
            try {
                final List<Feature> features = get();

                if (isCancelled()) {
                    return;
                }

                if ((features != null) && (layer instanceof AbstractFeatureService)) {
                    final HashSet<Feature> selectedFeaturesSet = new HashSet<Feature>();
                    final List<FeatureServiceFeature> selectedFeatures = table.getSelectedFeatures();
                    final List<FeatureServiceFeature> featuresToRemove = new ArrayList<FeatureServiceFeature>();
                    selectedFeaturesSet.addAll(features);

                    for (final FeatureServiceFeature f : selectedFeatures) {
                        if (selectedFeaturesSet.contains(f)) {
                            featuresToRemove.add(f);
                        }
                    }
                    SelectionManager.getInstance().removeSelectedFeatures(featuresToRemove);
                }
            } catch (Exception e) {
                LOG.error("Error while selecting features", e);
            }
            searching = false;
            querySearch.setControlsAccordingToState(searching);
        }
    }
}
