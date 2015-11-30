/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.search.CidsServerSearchMetaObjectNodeWrapper;

import Sirius.server.middleware.types.MetaObjectNode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.search.builtin.GeoSearch;

import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchCreateSearchGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.CidsBeansSearchFeature;
import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;

import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

import static de.cismet.cismap.commons.gui.MappingComponent.CREATE_SEARCH_POLYGON;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MetaSearchProtocolStepImpl extends GeomSearchProtocolStepImpl implements MetaSearchProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    private static final ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "MetaSearch",
            "MetaSearch protocol step");

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonIgnore
    private final Collection<MetaObjectNode> searchObjectNodes;

    @Getter
    @JsonIgnore
    private final Collection<SearchTopic> searchTopics;

    @Getter
    @JsonProperty(required = true)
    private String mode;

    @Getter
    @JsonProperty(required = true)
    private Set<MetaSearchProtocolStepSearchTopic> searchTopicInfos;

    @Getter
    @JsonProperty(required = true)
    private Collection<CidsServerSearchMetaObjectNodeWrapper> searchObjects;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSearchProtocolStep object.
     *
     * @param  search          DOCUMENT ME!
     * @param  searchListener  DOCUMENT ME!
     * @param  searchTopics    DOCUMENT ME!
     * @param  resultNodes     DOCUMENT ME!
     */
    public MetaSearchProtocolStepImpl(final GeoSearch search,
            final MetaSearchCreateSearchGeometryListener searchListener,
            final Collection<SearchTopic> searchTopics,
            final List resultNodes) {
        super(search, search.getGeometry(), resultNodes);

        final Collection<MetaObjectNode> searchObjectNodes;
        if (searchListener.getSearchFeature() instanceof CidsBeansSearchFeature) {
            final CidsBeansSearchFeature cidsBeansSearchFeature = (CidsBeansSearchFeature)
                searchListener.getSearchFeature();
            searchObjectNodes = new ArrayList<MetaObjectNode>();
            for (final CidsBean bean : cidsBeansSearchFeature.getBeans()) {
                final MetaObjectNode searchObjectNode = new MetaObjectNode(bean);
                searchObjectNodes.add(searchObjectNode);
            }
        } else {
            searchObjectNodes = null;
        }

        this.searchObjectNodes = searchObjectNodes;
        this.mode = searchListener.getMode();
        this.searchTopics = searchTopics;
    }

    /**
     * Creates a new GeoSearchProtocolStep object.
     *
     * @param  wkt               DOCUMENT ME!
     * @param  mode              DOCUMENT ME!
     * @param  searchTopicInfos  DOCUMENT ME!
     * @param  searchObjects     DOCUMENT ME!
     * @param  searchResults     DOCUMENT ME!
     */
    @JsonCreator
    public MetaSearchProtocolStepImpl(@JsonProperty("wkt") final String wkt,
            @JsonProperty("mode") final String mode,
            @JsonProperty("searchTopicInfos") final Set<MetaSearchProtocolStepSearchTopic> searchTopicInfos,
            @JsonProperty("searchObjects") final Collection<CidsServerSearchMetaObjectNodeWrapper> searchObjects,
            @JsonProperty("searchResults") final List<CidsServerSearchMetaObjectNodeWrapper> searchResults) {
        super(wkt, searchResults);
        this.mode = mode;
        this.searchTopicInfos = searchTopicInfos;
        this.searchObjects = searchObjects;

        final Collection<MetaObjectNode> searchObjectNodes = new ArrayList<MetaObjectNode>();
        if (searchObjects != null) {
            for (final CidsServerSearchMetaObjectNodeWrapper searchObject : searchObjects) {
                searchObjectNodes.add(new MetaObjectNode(
                        searchObject.getDomain(),
                        searchObject.getObjectId(),
                        searchObject.getClassId(),
                        searchObject.getName()));
            }
        }

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

        this.searchObjectNodes = searchObjectNodes;
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

        final Set<MetaSearchProtocolStepSearchTopic> searchTopics = new HashSet<MetaSearchProtocolStepSearchTopic>();
        for (final SearchTopic topic : MetaSearch.instance().getSelectedSearchTopics()) {
            searchTopics.add(new MetaSearchProtocolStepSearchTopic(
                    topic.getKey(),
                    topic.getName(),
                    topic.getIconName()));
        }

        final Collection<CidsServerSearchMetaObjectNodeWrapper> searchObjects;
        if (getSearchObjectNodes() != null) {
            searchObjects = new ArrayList<CidsServerSearchMetaObjectNodeWrapper>();
            for (final MetaObjectNode searchObjectNode : getSearchObjectNodes()) {
                searchObjects.add(new CidsServerSearchMetaObjectNodeWrapper(searchObjectNode));
            }
        } else {
            searchObjects = null;
        }

        this.searchObjects = searchObjects;
        this.searchTopicInfos = searchTopics;
    }

    @Override
    public GeoSearch getSearch() {
        return (GeoSearch)super.getSearch();
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new MetaSearchProtocolStepPanel(this);
    }

    @Override
    public boolean isReExecuteSearchEnabled() {
        return true;
    }

    @Override
    public void reExecuteSearch() {
        for (final SearchTopic searchTopic : MetaSearch.instance().getSearchTopics()) {
            searchTopic.setSelected(getSearchTopics().contains(searchTopic));
        }

        final CreateSearchGeometryListener inputListener = (CreateSearchGeometryListener)CismapBroker
                    .getInstance().getMappingComponent().getInputListener(CREATE_SEARCH_POLYGON);
        inputListener.setMode(getMode());
        inputListener.search(new SearchFeature(getGeometry(), CREATE_SEARCH_POLYGON));
    }
}
