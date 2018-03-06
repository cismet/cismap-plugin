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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.FeatureLockingInterface;
import de.cismet.cismap.commons.gui.attributetable.LockAlreadyExistsException;
import de.cismet.cismap.commons.gui.attributetable.LockFromSameUserAlreadyExistsException;

import de.cismet.connectioncontext.ClientConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * Locks CidsLayerFeature objects.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = FeatureLockingInterface.class)
public class CidsLayerLocker implements FeatureLockingInterface, ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    public static final String CS_LOCKS_TN = "lock";
    public static final String CS_LOCK_GROUP_TN = "lock_group";

    private static final Logger LOG = Logger.getLogger(CidsBeanLocker.class);
//    private static final String LOCK_QUERY = "SELECT DISTINCT %1$s, %2$s "
//                + " FROM %3$s WHERE class_id = %4$s and (object_id = %5$s or object_id is null);";
//    private static final String LOCK_MC_QUERY = "SELECT DISTINCT %1$s, %2$s "
//                + " FROM %3$s WHERE class_id = %4$s limit 1;";
    private static final String LOCK_QUERY = "SELECT DISTINCT %1$s, g.%2$s "
                + " FROM %3$s g join lock_lock_group lg on (g.objects = lg.lock_group_reference) join "
                + " lock l on (lg.lock = l.id) WHERE l.class_id = %4$s and l.object_id = any(ARRAY[%5$s]) limit 1;";

    //~ Instance fields --------------------------------------------------------

    private final Map<String, MetaClass> LOCK_MC_MAP = new HashMap<String, MetaClass>();
    private final Map<String, MetaClass> LOCK_GROUP_MC_MAP = new HashMap<String, MetaClass>();

    private final ClientConnectionContext connectionContext = ClientConnectionContext.create(getClass()
                    .getSimpleName());

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object lock(final Feature feature, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cidsLayerFeature = (CidsLayerFeature)feature;
            final List<Feature> l = Collections.nCopies(1, feature);

            return lock(l, multiLockForSameUserAllowed);
        }

        throw new IllegalArgumentException("Only CidsLayerFeature are supported");
    }

    @Override
    public Object lock(final List<Feature> features, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
        try {
            if ((features == null) || features.isEmpty()) {
                return new FakeLock();
            }
            final MetaObject mo = ((CidsLayerFeature)features.get(0)).getBean().getMetaObject();
            final MetaClass lockGroupMc = getLockGroupMetaClassForBean(((CidsLayerFeature)features.get(0)).getBean()
                            .getMetaObject().getDomain());
            final MetaClass lockMc = getLockMetaClassForBean(((CidsLayerFeature)features.get(0)).getBean()
                            .getMetaObject().getDomain());
            final String userString = NbBundle.getMessage(
                    CidsBeanLocker.class,
                    "CidsLayerLocker.lock(CidsBean).userString",
                    SessionManager.getSession().getUser().getName());

            boolean hasNotNewObject = false;

            for (final Feature f : features) {
                if (((CidsLayerFeature)f).getId() > 0) {
                    hasNotNewObject = true;
                }
            }
            if (!hasNotNewObject) {
                // object with id -1 are new objects, which cannot be locked, because they do not exists on the server
                return new FakeLock();
            }

            // Check, if the lock already exists
            final String query = String.format(
                    LOCK_QUERY,
                    lockGroupMc.getID(),
                    lockGroupMc.getPrimaryKey(),
                    lockGroupMc.getTableName(),
                    mo.getMetaClass().getID(),
                    getIds(features));
            final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0, getConnectionContext());

            if ((mos != null) && (mos.length > 0)) {
                for (final MetaObject metaObject : mos) {
                    if ((metaObject.getBean().getProperty("user_string") == null)
                                && metaObject.getBean().getProperty("user_string").equals(userString)) {
                        if (!multiLockForSameUserAllowed) {
                            final LockAlreadyExistsException ex = new LockFromSameUserAlreadyExistsException(
                                    "The lock does already exists",
                                    String.valueOf(mos[0].getBean().getProperty("user_string")));

                            throw ex;
                        }
                    } else {
                        final LockAlreadyExistsException ex = new LockAlreadyExistsException(
                                "The lock does already exists",
                                String.valueOf(mos[0].getBean().getProperty("user_string")));

                        throw ex;
                    }
                }
            }

            // create lock
            CidsBean lockGroupBean = lockGroupMc.getEmptyInstance().getBean();

            for (final Feature f : features) {
                final CidsBean lockBean = lockMc.getEmptyInstance().getBean();
                lockBean.setProperty("class_id", mo.getMetaClass().getID());
                lockBean.setProperty("object_id", ((CidsLayerFeature)f).getId());
                lockGroupBean.addCollectionElement("objects", lockBean);
            }
            try {
                final InetAddress addr = InetAddress.getLocalHost();
                lockGroupBean.setProperty("additional_info", addr.getHostName());
            } catch (UnknownHostException e) {
                LOG.error("cnnot determine the computer name", e);
            }
            lockGroupBean.setProperty("user_string", userString);
            lockGroupBean = lockGroupBean.persist(getConnectionContext());

            return lockGroupBean;
        } catch (LockAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while creating lock object", e);
            throw new Exception("Cannot lock object");
        }
    }

//    @Override
//    public Object lock(final List<Feature> feature, final boolean multiLockForSameUserAllowed)
//            throws LockAlreadyExistsException, Exception {
//        if (feature instanceof CidsLayerFeature) {
//            final CidsLayerFeature cidsLayerFeature = (CidsLayerFeature)feature;
//
//            return lock(cidsLayerFeature.getBean(), multiLockForSameUserAllowed);
//        }
//
//        throw new IllegalArgumentException("Only CidsLayerFeature are supported");
//    }

    @Override
    public Object lock(final AbstractFeatureService service, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
//        if (service instanceof CidsLayer) {
//            final CidsLayer layer = (CidsLayer)service;
//
//            return lock(layer.getMetaClass(), multiLockForSameUserAllowed);
//        }

        throw new IllegalArgumentException("Only CidsLayerFeature are supported");
    }

    @Override
    public void unlock(final Object lockObject) throws Exception {
        if (lockObject instanceof FakeLock) {
            return;
        } else if (lockObject instanceof CidsBean) {
            final CidsBean bean = (CidsBean)lockObject;
            final MetaClass lockMc = getLockGroupMetaClassForBean(bean.getMetaObject().getMetaClass().getDomain());

            if (bean.getMetaObject().getMetaClass().equals(lockMc)) {
                unlock(bean);
            } else {
                throw new IllegalArgumentException("The locking object must be of the type " + lockMc.toString());
            }
        } else if (lockObject instanceof List) {
            for (final Object o : (List)lockObject) {
                unlock(o);
            }
        } else {
            throw new IllegalArgumentException("The locking object must be a cids bean");
        }
    }

    /**
     * unlocks the the cids bean, that is referenced by the given lock bean.
     *
     * @param   bean  a lock bean see {@link #lock(de.cismet.cids.dynamics.CidsBean)}
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void unlock(final CidsBean bean) throws Exception {
        try {
            if (bean instanceof FakeLock) {
                return;
            }
            bean.delete();
            bean.persist(getConnectionContext());
        } catch (Exception e) {
            LOG.error("Cannot remove lock with id " + bean.getProperty("id"));
            throw e;
        }
    }

    @Override
    public Class[] getSupportedFeatureServiceClasses() {
        return new Class[] { CidsLayer.class };
    }

    /**
     * Determines the meta class of the cs_locks cids class, that should be used to lock the given bean.
     *
     * @param   domain  the domain to lock in
     *
     * @return  the meta class of the cs_locks cids class, that should be used to lock a bean of the given domain
     *
     * @throws  Exception  if the meta class cannot be determined
     */
    protected MetaClass getLockMetaClassForBean(final String domain) throws Exception {
        // determine the cs_locks meta class
        MetaClass lockMc = LOCK_MC_MAP.get(domain);

        if (lockMc == null) {
            lockMc = ClassCacheMultiple.getMetaClass(domain, CS_LOCKS_TN);

            if (lockMc == null) {
                throw new Exception("The cids class " + CS_LOCKS_TN + " does not exist in the domain " + domain);
            }
            LOCK_MC_MAP.put(domain, lockMc);
        }

        return lockMc;
    }

    /**
     * Determines the meta class of the cs_locks cids class, that should be used to lock the given bean.
     *
     * @param   domain  the domain to lock in
     *
     * @return  the meta class of the cs_locks cids class, that should be used to lock a bean of the given domain
     *
     * @throws  Exception  if the meta class cannot be determined
     */
    protected MetaClass getLockGroupMetaClassForBean(final String domain) throws Exception {
        // determine the cs_locks meta class
        MetaClass lockMc = LOCK_GROUP_MC_MAP.get(domain);

        if (lockMc == null) {
            lockMc = ClassCacheMultiple.getMetaClass(domain, CS_LOCK_GROUP_TN);

            if (lockMc == null) {
                throw new Exception("The cids class " + CS_LOCK_GROUP_TN + " does not exist in the domain " + domain);
            }
            LOCK_GROUP_MC_MAP.put(domain, lockMc);
        }

        return lockMc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getIds(final List<Feature> features) {
        StringBuilder sb = null;

        for (final Feature f : features) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(",");
            }

            sb.append(((CidsLayerFeature)f).getId());
        }

        return ((sb == null) ? "" : sb.toString());
    }

    @Override
    public final ClientConnectionContext getConnectionContext() {
        return connectionContext;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class FakeLock extends CidsBean {
    }
}
