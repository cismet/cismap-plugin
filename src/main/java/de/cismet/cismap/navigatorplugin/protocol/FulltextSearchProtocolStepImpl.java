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

import Sirius.server.middleware.types.MetaClass;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cismet.cids.server.search.SearchResultListener;
import de.cismet.cids.server.search.SearchResultListenerProvider;
import de.cismet.cids.server.search.builtin.FullTextSearch;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
import de.cismet.cismap.navigatorplugin.metasearch.SearchClass;
import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;

import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolHandler;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class FulltextSearchProtocolStepImpl extends GeomSearchProtocolStepImpl implements FulltextSearchProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    private static final ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "FullTextSearch",
            "FullTextSearch protocol step");

    @Getter
    @JsonIgnore
    private static final transient MetaClassCacheService META_CLASS_CACHE_SERVICE = Lookup.getDefault()
                .lookup(MetaClassCacheService.class);

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonIgnore
    private final transient Collection<SearchTopic> searchTopics;

    @Getter
    @JsonProperty(required = true)
    private final String searchText;

    @Getter
    @JsonProperty(required = true)
    private final boolean caseSensitiveEnabled;

    @Getter
    @JsonProperty(required = true)
    private Set<MetaSearchProtocolStepSearchTopic> searchTopicInfos;

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
        super(fullTextSearch, fullTextSearch.getGeometry(), resultNodes);

        this.searchText = fullTextSearch.getSearchText();
        this.caseSensitiveEnabled = fullTextSearch.isCaseSensitive();
        this.searchTopics = searchTopics;
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
            @JsonProperty("searchTopicInfos") final Set<MetaSearchProtocolStepSearchTopic> searchTopicInfos,
            @JsonProperty("searchResults") final List<CidsServerSearchMetaObjectNodeWrapper> searchResults) {
        super(wkt, searchResults);

        this.searchText = searchText;
        this.caseSensitiveEnabled = caseSensitiveEnabled;
        this.searchTopicInfos = searchTopicInfos;

        final Set<String> searchTopicInfoKeys = new HashSet<String>();
        for (final MetaSearchProtocolStepSearchTopic searchTopicInfo : searchTopicInfos) {
            searchTopicInfoKeys.add(searchTopicInfo.getKey());
        }

        final Collection<SearchTopic> searchTopics = new ArrayList<SearchTopic>();
        for (final SearchTopic searchTopic : MetaSearch.instance().getSearchTopics()) {
            if (searchTopicInfoKeys.contains(searchTopic.getKey())) {
                searchTopics.add(searchTopic);
            }
        }

        this.searchTopics = searchTopics;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected ProtocolStepMetaInfo createMetaInfo() {
        return META_INFO;
    }

    @Override
    public void initParameters() {
        super.initParameters();
        final Set<MetaSearchProtocolStepSearchTopic> searchTopicInfos =
            new HashSet<MetaSearchProtocolStepSearchTopic>();
        for (final SearchTopic topic
                    : MetaSearch.instance().getSelectedSearchTopics()) {
            searchTopicInfos.add(new MetaSearchProtocolStepSearchTopic(
                    topic.getKey(),
                    topic.getName(),
                    topic.getIconName()));
        }

        this.searchTopicInfos = searchTopicInfos;
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
        if (META_CLASS_CACHE_SERVICE != null) {
            for (final SearchTopic searchTopic : getSearchTopics()) {
                for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
                    final MetaClass metaClass = META_CLASS_CACHE_SERVICE.getMetaClass(searchClass.getCidsDomain(),
                            searchClass.getCidsClass());
                    validClassesFromStrings.add(metaClass.getKey().toString());
                }
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

        CidsSearchExecutor.searchAndDisplayResultsWithDialog(fullTextSearch);
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new FulltextSearchProtocolStepPanel(this);
    }
}
