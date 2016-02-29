/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.search.CidsServerSearchMetaObjectNodeWrapper;
import Sirius.navigator.search.CidsServerSearchProtocolStepImpl;

import Sirius.server.middleware.types.MetaObjectNode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vividsolutions.jts.geom.Geometry;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
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

import de.cismet.commons.gui.protocol.AbstractProtocolStep;
import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

import static de.cismet.cismap.commons.gui.MappingComponent.CREATE_SEARCH_POLYGON;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class GeoSearchProtocolStepImpl extends AbstractProtocolStep implements GeoSearchProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    private static final ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "GeoSearch",
            "GeoSearchProtocolStep");

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonProperty(required = true)
    protected String wkt;

    @Getter
    @JsonProperty(required = true)
    private final String mode;

    @Getter
    @JsonProperty(required = true)
    private List<CidsServerSearchMetaObjectNodeWrapper> searchResults;

    @Getter
    @JsonProperty(required = true)
    private Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos;

    @Getter
    @JsonProperty(required = true)
    private Collection<CidsServerSearchMetaObjectNodeWrapper> searchObjects;

    @Getter @JsonIgnore private final CidsServerSearchProtocolStepImpl cidsServerSearchProtocolStep;

    @Getter @JsonIgnore private final GeometryProtocolStepImpl geometryProtocolStep;

    @Getter @JsonIgnore private final SearchTopicsProtocolStepImpl searchTopicsProtocolStep;

    @Getter @JsonIgnore private final List<MetaObjectNode> searchObjectNodes;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSearchProtocolStep object.
     *
     * @param  geoSearch       DOCUMENT ME!
     * @param  searchListener  DOCUMENT ME!
     * @param  searchTopics    DOCUMENT ME!
     * @param  resultNodes     DOCUMENT ME!
     */
    public GeoSearchProtocolStepImpl(final GeoSearch geoSearch,
            final MetaSearchCreateSearchGeometryListener searchListener,
            final Collection<SearchTopic> searchTopics,
            final List<MetaObjectNode> resultNodes) {
        this.cidsServerSearchProtocolStep = new CidsServerSearchProtocolStepImpl(geoSearch, resultNodes);
        this.geometryProtocolStep = new GeometryProtocolStepImpl(geoSearch.getGeometry());
        this.searchTopicsProtocolStep = new SearchTopicsProtocolStepImpl(searchTopics);

        final List<MetaObjectNode> searchObjectNodes;
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

        getCidsServerSearchProtocolStep().setReexecutor(this);
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
    public GeoSearchProtocolStepImpl(@JsonProperty("wkt") final String wkt,
            @JsonProperty("mode") final String mode,
            @JsonProperty("searchTopicInfos") final Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos,
            @JsonProperty("searchObjects") final Collection<CidsServerSearchMetaObjectNodeWrapper> searchObjects,
            @JsonProperty("searchResults") final List<CidsServerSearchMetaObjectNodeWrapper> searchResults) {
        this.cidsServerSearchProtocolStep = new CidsServerSearchProtocolStepImpl(searchResults);
        this.geometryProtocolStep = new GeometryProtocolStepImpl(wkt);
        this.searchTopicsProtocolStep = new SearchTopicsProtocolStepImpl(searchTopicInfos);

        this.mode = mode;
        this.searchTopicInfos = searchTopicInfos;
        this.searchObjects = searchObjects;

        final List<MetaObjectNode> searchObjectNodes = new ArrayList<MetaObjectNode>();
        if (searchObjects != null) {
            for (final CidsServerSearchMetaObjectNodeWrapper searchObject : searchObjects) {
                searchObjectNodes.add(new MetaObjectNode(
                        searchObject.getDomain(),
                        searchObject.getObjectId(),
                        searchObject.getClassId(),
                        searchObject.getName(),
                        null,
                        null)); // TODO: Check4CashedGeomAndLightweightJson
            }
        }

        this.searchObjectNodes = searchObjectNodes;
        this.wkt = getGeometryProtocolStep().getWkt();
        this.searchResults = getCidsServerSearchProtocolStep().getSearchResults();

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
        this.wkt = getGeometryProtocolStep().getWkt();
        this.searchResults = getCidsServerSearchProtocolStep().getSearchResults();
        this.searchTopicInfos = getSearchTopicsProtocolStep().getSearchTopicInfos();
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new GeoSearchProtocolStepPanel(this);
    }

    @Override
    public boolean isReExecuteSearchEnabled() {
        return true;
    }

    @Override
    public void reExecuteSearch() {
        for (final SearchTopic searchTopic : MetaSearch.instance().getSearchTopics()) {
            searchTopic.setSelected(getSearchTopicsProtocolStep().getSearchTopics().contains(searchTopic));
        }

        final CreateSearchGeometryListener inputListener = (CreateSearchGeometryListener)CismapBroker
                    .getInstance().getMappingComponent().getInputListener(CREATE_SEARCH_POLYGON);
        inputListener.setMode(getMode());
        inputListener.search(new SearchFeature(getGeometry(), CREATE_SEARCH_POLYGON));
    }

    @Override
    public List<MetaObjectNode> getSearchResultNodes() {
        return getCidsServerSearchProtocolStep().getSearchResultNodes();
    }

    @Override
    public Geometry getGeometry() {
        return getGeometryProtocolStep().getGeometry();
    }
}
