/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.cismet.cismap.navigatorplugin.metasearch.MetaSearch;
import de.cismet.cismap.navigatorplugin.metasearch.SearchTopic;

import de.cismet.commons.gui.protocol.AbstractProtocolStep;
import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class SearchTopicsProtocolStepImpl extends AbstractProtocolStep implements SearchTopicsProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    public static final ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "SearchTopics",
            "SearchTopicsProtocolStepImpl");

    //~ Instance fields --------------------------------------------------------

    @Getter @JsonIgnore private final transient Collection<SearchTopic> searchTopics;

    @Getter
    @JsonProperty(required = true)
    private Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchTopicsProtocolStepImpl object.
     *
     * @param  searchTopics  DOCUMENT ME!
     */
    public SearchTopicsProtocolStepImpl(final Collection<SearchTopic> searchTopics) {
        this.searchTopics = searchTopics;
    }

    /**
     * Creates a new SearchTopicsProtocolStepImpl object.
     *
     * @param  searchTopicInfos  DOCUMENT ME!
     */
    public SearchTopicsProtocolStepImpl(
            @JsonProperty("searchTopicInfos") final Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos) {
        this.searchTopicInfos = searchTopicInfos;

        final Set<String> searchTopicInfoKeys = new HashSet<String>();
        for (final GeoSearchProtocolStepSearchTopic searchTopicInfo : searchTopicInfos) {
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
    public void initParameters() {
        super.initParameters();

        final Set<GeoSearchProtocolStepSearchTopic> searchTopicInfos = new HashSet<GeoSearchProtocolStepSearchTopic>();
        for (final SearchTopic topic
                    : MetaSearch.instance().getSelectedSearchTopics()) {
            searchTopicInfos.add(new GeoSearchProtocolStepSearchTopic(
                    topic.getKey(),
                    topic.getName(),
                    topic.getIconName()));
        }

        this.searchTopicInfos = searchTopicInfos;
    }

    @Override
    protected ProtocolStepMetaInfo createMetaInfo() {
        return META_INFO;
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new SearchTopicsProtocolStepPanel(this);
    }
}
