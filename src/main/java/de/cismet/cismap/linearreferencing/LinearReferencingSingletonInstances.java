/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.linearreferencing;

import org.apache.log4j.Logger;

import de.cismet.cids.tools.CidsBeanCache;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface LinearReferencingSingletonInstances {

    //~ Instance fields --------------------------------------------------------

    Logger LOG = Logger.getLogger(LinearReferencingConstants.class);

    FeatureRegistry FEATURE_REGISTRY = FeatureRegistry.getInstance();
    PointBeanMergeRegistry MERGE_REGISTRY = PointBeanMergeRegistry.getInstance();
    MappingComponent MAPPING_COMPONENT = CismapBroker.getInstance().getMappingComponent();
    CidsBeanCache CIDSBEAN_CACHE = CidsBeanCache.getInstance();
}
