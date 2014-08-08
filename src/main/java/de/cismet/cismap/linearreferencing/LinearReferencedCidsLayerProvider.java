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
package de.cismet.cismap.linearreferencing;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.cismap.linearreferencing.tools.LinearReferencedGeomProvider;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = LinearReferencedGeomProvider.class)
public class LinearReferencedCidsLayerProvider implements LinearReferencedGeomProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(LinearReferencedCidsLayerProvider.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public List<AbstractFeatureService> getLinearReferencedGeomServices() {
        final List<AbstractFeatureService> validLinRefClasses = new ArrayList<AbstractFeatureService>();
        final LinearReferencingHelper helper = FeatureRegistry.getInstance().getLinearReferencingSolver();

        for (final String domain : helper.getAllUsedDomains()) {
            try {
                final MetaClass[] classes = SessionManager.getProxy().getClasses(domain);

                for (final MetaClass clazz : classes) {
                    final Collection attributes = clazz.getAttributeByName("LinRefBaseGeom");
                    if ((attributes == null) || attributes.isEmpty()) {
                        continue;
                    }
                    final CidsLayer cidsLayer = new CidsLayer(clazz);
                    cidsLayer.setName(clazz.getDomain() + ":" + clazz.getTableName());
                    validLinRefClasses.add(cidsLayer);
                }
            } catch (ConnectionException ex) {
                LOG.error("Error while retrieving all classes from domain " + domain, ex);
            }
        }

        return validLinRefClasses;
    }
}
