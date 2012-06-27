/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import org.openide.util.lookup.ServiceProvider;

import java.util.Arrays;
import java.util.Collection;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.CidsBeanLookupInstance;

import de.cismet.cismap.commons.features.Feature;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsBeanLookupInstance.class)
public final class CismapCidsBeanLookupInstance implements CidsBeanLookupInstance {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection getLookupInstances(final CidsBean cidsBean) {
        return Arrays.asList(new DefaultMapVisualisationProvider());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public final class DefaultMapVisualisationProvider implements MapVisualisationProvider {

        //~ Methods ------------------------------------------------------------

        @Override
        public Feature getFeature(final CidsBean bean) {
            return new CidsFeature(bean.getMetaObject());
        }
    }
}
