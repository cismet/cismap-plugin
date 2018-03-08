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
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.search.CidsSearchExecutor;

import com.vividsolutions.jts.geom.Geometry;

import org.jdom.Element;

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenu;

import de.cismet.cids.server.search.SearchResultListener;
import de.cismet.cids.server.search.SearchResultListenerProvider;
import de.cismet.cids.server.search.builtin.GeoSearch;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchCreateSearchGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapSearchListener;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;

import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;
import de.cismet.cismap.navigatorplugin.protocol.GeoSearchProtocolStepImpl;

import de.cismet.commons.gui.protocol.ProtocolHandler;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import static de.cismet.cismap.commons.gui.MappingComponent.CREATE_SEARCH_POLYGON;
import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MetaSearchHelper extends javax.swing.JPanel implements MapSearchListener, Configurable, ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            MetaSearchHelper.class);

    //~ Instance fields --------------------------------------------------------

    private final MappingComponent mappingComponent;

    private final String interactionMode;
    private final String searchName;
    private final MetaSearch metaSearch;
    private GeoSearch customGeoSearch;
    private final ConnectionContext connectionContext;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdPluginSearch;
    private javax.swing.JMenu metaSearchMenu;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MetaSearchComponentFactory.
     *
     * @param  plugin            DOCUMENT ME!
     * @param  interactionMode   DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  searchName        DOCUMENT ME!
     */
    private MetaSearchHelper(final boolean plugin,
            final String interactionMode,
            final MappingComponent mappingComponent,
            final String searchName,
            final ConnectionContext connectionContext) {
        this.interactionMode = interactionMode;
        this.mappingComponent = mappingComponent;
        this.searchName = searchName;
        this.connectionContext = connectionContext;
        metaSearch = MetaSearch.instance();

        CismapBroker.getInstance().addMapSearchListener(this);

        if (plugin) {
            final MetaSearchCreateSearchGeometryListener listener = new MetaSearchCreateSearchGeometryListener(
                    mappingComponent,
                    metaSearch);
            mappingComponent.addInputListener(interactionMode, listener);
            mappingComponent.addPropertyChangeListener(listener);
        }
        CismapBroker.getInstance().setMetaSearch(metaSearch);
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   plugin            DOCUMENT ME!
     * @param   interactionMode   DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     * @param   searchName        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MetaSearchHelper createNewInstance(final boolean plugin,
            final String interactionMode,
            final MappingComponent mappingComponent,
            final String searchName,
            final ConnectionContext connectionContext) {
        return new MetaSearchHelper(plugin, interactionMode, mappingComponent, searchName, connectionContext);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaSearch getMetaSearch() {
        return metaSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JMenu getMenSearch() {
        return metaSearchMenu;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JButton getCmdPluginSearch() {
        return cmdPluginSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    private void initMetaSearch(final Geometry geom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selected Search Classes " + metaSearch.getSelectedSearchClassesForQuery()); // NOI18N
        }
        final Geometry transformed = CrsTransformer.transformToDefaultCrs(geom);
        // Damits auch mit -1 funzt:
        transformed.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

        final GeoSearch geoSearch;
        if (customGeoSearch == null) {
            // there is always the default implementation
            geoSearch = Lookup.getDefault().lookup(GeoSearch.class);
        } else {
            geoSearch = customGeoSearch;
        }

        geoSearch.setGeometry(transformed);
        geoSearch.setValidClassesFromStrings(metaSearch.getSelectedSearchClassesForQuery());
        if (geoSearch instanceof SearchResultListenerProvider) {
            ((SearchResultListenerProvider)geoSearch).setSearchResultListener(new SearchResultListener() {

                    @Override
                    public void searchDone(final List results) {
                        if (ProtocolHandler.getInstance().isRecordEnabled()) {
                            final Collection<SearchTopic> searchTopics = MetaSearch.instance()
                                        .getSelectedSearchTopics();
                            final CreateSearchGeometryListener createSearchGeometryListener =
                                (CreateSearchGeometryListener)CismapBroker.getInstance().getMappingComponent()
                                        .getInputListener(CREATE_SEARCH_POLYGON);
                            ProtocolHandler.getInstance()
                                    .recordStep(
                                        new GeoSearchProtocolStepImpl(
                                            geoSearch,
                                            (MetaSearchCreateSearchGeometryListener)createSearchGeometryListener,
                                            searchTopics,
                                            results));
                        }
                    }
                });
        }
        CidsSearchExecutor.searchAndDisplayResultsWithDialog(geoSearch, getConnectionContext());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GeoSearch getCustomGeoSearch() {
        return customGeoSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  customGeoSearch  DOCUMENT ME!
     */
    public void setCustomGeoSearch(final GeoSearch customGeoSearch) {
        this.customGeoSearch = customGeoSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mse  DOCUMENT ME!
     */
    @Override
    public void mapSearchStarted(final MapSearchEvent mse) {
        initMetaSearch(mse.getGeometry());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        cmdPluginSearch = new GeoSearchButton(interactionMode, mappingComponent, searchName);
        metaSearchMenu = new de.cismet.cismap.navigatorplugin.GeoSearchMenu(interactionMode, mappingComponent);

        cmdPluginSearch.setToolTipText(org.openide.util.NbBundle.getMessage(
                MetaSearchHelper.class,
                "GeoSearchButton.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            metaSearchMenu,
            org.openide.util.NbBundle.getMessage(MetaSearchHelper.class, "GeoSearchMenu.text")); // NOI18N

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void popMenSearchPopupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_popMenSearchPopupMenuWillBecomeVisible
    }//GEN-LAST:event_popMenSearchPopupMenuWillBecomeVisible

    @Override
    public void configure(final Element parent) {
        metaSearch.configure(parent);
        ((GeoSearchButton)cmdPluginSearch).initSearchTopicMenues(metaSearch);
        ((GeoSearchMenu)metaSearchMenu).initSearchTopicMenues(metaSearch);
    }

    @Override
    public void masterConfigure(final Element parent) {
        metaSearch.masterConfigure(parent);
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        return metaSearch.getConfiguration();
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
