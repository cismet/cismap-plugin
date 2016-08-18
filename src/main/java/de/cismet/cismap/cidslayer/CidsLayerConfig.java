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

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.types.MetaClass;

import org.apache.log4j.Logger;

import de.cismet.cismap.commons.LayerConfig;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerConfig implements LayerConfig, Comparable<CidsLayerConfig> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerConfig.class);
    public static final String LAYER_TITLE = "cidsLayerTitle";
    private static final String LAYER_POSITION = "cidsLayerPosition";

    //~ Instance fields --------------------------------------------------------

    private final MetaClass config;
    private final String title;
    private int position = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerConfig object.
     *
     * @param  clazz  DOCUMENT ME!
     */
    public CidsLayerConfig(final MetaClass clazz) {
        config = clazz;
        final ClassAttribute titleAttribute = config.getClassAttribute(LAYER_TITLE);

        if ((titleAttribute != null) && (titleAttribute.getValue() != null)) {
            title = titleAttribute.getValue().toString();
        } else {
            title = config.getName();
        }

        final ClassAttribute positionAttribute = config.getClassAttribute(LAYER_POSITION);

        if ((positionAttribute != null) && (positionAttribute.getValue() != null)) {
            try {
                position = Integer.parseInt(positionAttribute.getValue().toString());
            } catch (NumberFormatException ex) {
                LOG.warn("The position attribute of the cidsLayer " + title + " does not contain a valid number", ex);
            }
        }
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
        return title;
    }

    @Override
    public int compareTo(final CidsLayerConfig o) {
        if (position == o.position) {
            return getTitle().compareTo(o.getTitle());
        } else {
            return position - o.position;
        }
    }
}
