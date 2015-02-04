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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.FeatureLockingInterface;
import de.cismet.cismap.commons.gui.attributetable.LockAlreadyExistsException;

/**
 * Locks CidsLayerFeature objects.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = FeatureLockingInterface.class)
public class CidsLayerLocker extends CidsBeanLocker implements FeatureLockingInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object lock(final Feature feature) throws LockAlreadyExistsException, Exception {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cidsLayerFeature = (CidsLayerFeature)feature;

            return lock(cidsLayerFeature.getBean());
        }

        throw new IllegalArgumentException("Only CidsLayerFeature are supported");
    }

    @Override
    public Object lock(final AbstractFeatureService service) throws LockAlreadyExistsException, Exception {
        if (service instanceof CidsLayer) {
            final CidsLayer layer = (CidsLayer)service;

            return lock(layer.getMetaClass());
        }

        throw new IllegalArgumentException("Only CidsLayerFeature are supported");
    }

    @Override
    public void unlock(final Object lockObject) throws Exception {
        if (lockObject instanceof CidsBean) {
            final CidsBean bean = (CidsBean)lockObject;
            final MetaClass lockMc = getLockMetaClassForBean(bean.getMetaObject().getMetaClass().getDomain());

            if (bean.getMetaObject().getMetaClass().equals(lockMc)) {
                unlock(bean);
            } else {
                throw new IllegalArgumentException("The locking object must be of the type " + lockMc.toString());
            }
        } else {
            throw new IllegalArgumentException("The locking object must be a cids bean");
        }
    }

    @Override
    public Class[] getSupportedFeatureServiceClasses() {
        return new Class[] { CidsLayer.class };
    }
}
