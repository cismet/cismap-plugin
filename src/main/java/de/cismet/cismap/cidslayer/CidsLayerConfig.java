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
package de.cismet.cismap.cidslayer;

import Sirius.server.middleware.types.MetaClass;

import de.cismet.cismap.commons.LayerConfig;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerConfig implements LayerConfig, Comparable<CidsLayerConfig> {

    //~ Instance fields --------------------------------------------------------

    private MetaClass config;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerConfig object.
     *
     * @param  clazz  DOCUMENT ME!
     */
    public CidsLayerConfig(final MetaClass clazz) {
        config = clazz;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public AbstractFeatureService createConfiguredLayer() {
        final CidsLayer layer = new CidsLayer(config);
        return layer;
    }

    @Override
    public String toString() {
        return config.getName();
    }

    @Override
    public String getTitle() {
        return config.getName();
    }

    @Override
    public int compareTo(final CidsLayerConfig o) {
        return getTitle().compareTo(o.getTitle());
    }
}
