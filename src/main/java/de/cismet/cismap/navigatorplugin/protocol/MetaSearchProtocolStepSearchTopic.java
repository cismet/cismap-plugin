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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
public class MetaSearchProtocolStepSearchTopic {

    //~ Instance fields --------------------------------------------------------

    @JsonProperty(required = true)
    private final String key;

    @JsonProperty(required = true)
    private final String name;

    @JsonProperty(required = true)
    private final String iconName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSearchProtocolStepSearchTopic object.
     *
     * @param  key       DOCUMENT ME!
     * @param  name      DOCUMENT ME!
     * @param  iconName  DOCUMENT ME!
     */
    @JsonCreator
    public MetaSearchProtocolStepSearchTopic(@JsonProperty("key") final String key,
            @JsonProperty("name") final String name,
            @JsonProperty("iconName") final String iconName) {
        this.key = key;
        this.name = name;
        this.iconName = iconName;
    }
}
