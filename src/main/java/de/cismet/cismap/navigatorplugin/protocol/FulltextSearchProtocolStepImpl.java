/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.search.CidsSearchExecutor;
import Sirius.navigator.search.CidsServerSearchMetaObjectNodeWrapper;
import Sirius.navigator.search.CidsServerSearchProtocolStepImpl;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObjectNode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vividsolutions.jts.geom.Geometry;

import lombok.Getter;

import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.cismet.cids.server.search.SearchResultListener;
import de.cismet.cids.server.search.SearchResultListenerProvider;
import de.cismet.cids.server.search.builtin.FullTextSearch;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
import de.cismet.cismap.navigatorplugin.metasearch.SearchClass;
import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;

import de.cismet.commons.gui.protocol.AbstractProtocolStep;
import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolHandler;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class FulltextSearchProtocolStepImpl extends AbstractProtocolStep implements FulltextSearchProtocolStep,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "FullTextSearch",
            "FullTextSearch protocol step");

    @Getter @JsonIgnore private static final transient MetaClassCacheService META_CLASS_CACHE_SERVICE = Lookup
                .getDefault().lookup(MetaClassCacheService.class);

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonProperty(required = true)
    protected String wkt;

    @Getter
    @JsonProperty(required = true)
    private final String searchText;

    @Getter
    @JsonProperty(required = true)
    private final boolean caseSensitiveEnabled;

    @Getter
    @JsonProperty(required = true)
    private List<CidsServerSearchMetaObjectNodeWrapper> searchResults;

    @Getter
    @JsonProperty(required = true)
    private Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos;

    @Getter @JsonIgnore private final CidsServerSearchProtocolStepImpl cidsServerSearchProtocolStep;

    @Getter @JsonIgnore private final GeometryProtocolStepImpl geometryProtocolStep;

    @Getter @JsonIgnore private final SearchTopicsProtocolStepImpl searchTopicsProtocolStep;

    private transient ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FulltextSearchProtocolStep object.
     *
     * @param  fullTextSearch  DOCUMENT ME!
     * @param  searchTopics    DOCUMENT ME!
     * @param  resultNodes     DOCUMENT ME!
     */
    public FulltextSearchProtocolStepImpl(final FullTextSearch fullTextSearch,
            final Collection<SearchTopic> searchTopics,
            final List resultNodes) {
        this.cidsServerSearchProtocolStep = new CidsServerSearchProtocolStepImpl(fullTextSearch, resultNodes);
        this.geometryProtocolStep = new GeometryProtocolStepImpl(fullTextSearch.getGeometry());
        this.searchTopicsProtocolStep = new SearchTopicsProtocolStepImpl(searchTopics);

        this.searchText = fullTextSearch.getSearchText();
        this.caseSensitiveEnabled = fullTextSearch.isCaseSensitive();

        getCidsServerSearchProtocolStep().setReexecutor(this);
    }

    /**
     * Creates a new FulltextSearchProtocolStep object.
     *
     * @param  searchText            DOCUMENT ME!
     * @param  caseSensitiveEnabled  DOCUMENT ME!
     * @param  wkt                   DOCUMENT ME!
     * @param  searchTopicInfos      DOCUMENT ME!
     * @param  searchResults         DOCUMENT ME!
     */
    @JsonCreator
    public FulltextSearchProtocolStepImpl(@JsonProperty("searchText") final String searchText,
            @JsonProperty("caseSensitiveEnabled") final boolean caseSensitiveEnabled,
            @JsonProperty("wkt") final String wkt,
            @JsonProperty("searchTopicInfos") final Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos,
            @JsonProperty("searchResults") final List<CidsServerSearchMetaObjectNodeWrapper> searchResults) {
        this.cidsServerSearchProtocolStep = new CidsServerSearchProtocolStepImpl(searchResults);
        this.geometryProtocolStep = new GeometryProtocolStepImpl(wkt);
        this.searchTopicsProtocolStep = new SearchTopicsProtocolStepImpl(searchTopicInfos);

        this.searchText = searchText;
        this.caseSensitiveEnabled = caseSensitiveEnabled;

        getCidsServerSearchProtocolStep().setReexecutor(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected ProtocolStepMetaInfo createMetaInfo() {
        return META_INFO;
    }

    @Override
    public void initParameters() {
        super.initParameters();
        getGeometryProtocolStep().initParameters();
        getCidsServerSearchProtocolStep().initParameters();
        getSearchTopicsProtocolStep().initParameters();

        this.searchTopicInfos = getSearchTopicsProtocolStep().getSearchTopicInfos();
        this.searchResults = getCidsServerSearchProtocolStep().getSearchResults();
        this.wkt = getGeometryProtocolStep().getWkt();
    }

    @Override
    public boolean isReExecuteSearchEnabled() {
        return true;
    }

    @Override
    public void reExecuteSearch() {
        final FullTextSearch fullTextSearch = Lookup.getDefault().lookup(FullTextSearch.class);
        fullTextSearch.setSearchText(getSearchText());
        fullTextSearch.setCaseSensitive(isCaseSensitiveEnabled());
        fullTextSearch.setGeometry(getGeometry());

        final Collection<String> validClassesFromStrings = new ArrayList<String>();
        for (final SearchTopic searchTopic : MetaSearch.instance().getSearchTopics()) {
            if (getSearchTopicsProtocolStep().getSearchTopics().contains(searchTopic)) {
                searchTopic.setSelected(true);
                if (META_CLASS_CACHE_SERVICE != null) {
                    for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
                        final MetaClass metaClass = META_CLASS_CACHE_SERVICE.getMetaClass(searchClass.getCidsDomain(),
                                searchClass.getCidsClass(),
                                getConnectionContext());
                        validClassesFromStrings.add(metaClass.getKey().toString());
                    }
                }
            } else {
                searchTopic.setSelected(true);
            }
        }
        fullTextSearch.setValidClassesFromStrings(validClassesFromStrings);

        if (fullTextSearch instanceof SearchResultListenerProvider) {
            ((SearchResultListenerProvider)fullTextSearch).setSearchResultListener(new SearchResultListener() {

                    @Override
                    public void searchDone(final List results) {
                        if (ProtocolHandler.getInstance().isRecordEnabled()) {
                            ProtocolHandler.getInstance()
                                    .recordStep(
                                        new FulltextSearchProtocolStepImpl(
                                            fullTextSearch,
                                            MetaSearch.instance().getSelectedSearchTopics(),
                                            results));
                        }
                    }
                });
        }

        CidsSearchExecutor.searchAndDisplayResultsWithDialog(fullTextSearch, getConnectionContext());
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new FulltextSearchProtocolStepPanel(this);
    }

    @Override
    public List<MetaObjectNode> getSearchResultNodes() {
        return getCidsServerSearchProtocolStep().getSearchResultNodes();
    }

    @Override
    public Geometry getGeometry() {
        return getGeometryProtocolStep().getGeometry();
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
