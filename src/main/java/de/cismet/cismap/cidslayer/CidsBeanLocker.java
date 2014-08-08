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

import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.gui.attributetable.LockAlreadyExistsException;

/**
 * Can be used to lock cids beans. It will be assumed that the cids system contains a cids class with the name cs_locks
 * in the same domain as the cids bean to lock.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsBeanLocker {

    //~ Static fields/initializers ---------------------------------------------

    public static final String CS_LOCKS_TN = "cs_locks";

    private static final Logger LOG = Logger.getLogger(CidsBeanLocker.class);
    private static final String LOCK_QUERY = "SELECT DISTINCT %1$s, %2$s "
                + " FROM %3$s WHERE class_id = %4$s and object_id = %5$s;";

    //~ Instance fields --------------------------------------------------------

    private final Map<String, MetaClass> LOCK_MC_MAP = new HashMap<String, MetaClass>();

    //~ Methods ----------------------------------------------------------------

    /**
     * Locks the given bean.
     *
     * @param   bean  the cids bean to lock
     *
     * @return  the lock bean. This bean can be used to unlock the given bean
     *
     * @throws  LockAlreadyExistsException  if the given bean is already locked
     * @throws  Exception                   DOCUMENT ME!
     */
    public CidsBean lock(final CidsBean bean) throws LockAlreadyExistsException, Exception {
        try {
            final MetaClass lockMc = getLockMetaClassForBean(bean);

            // Check, if the lock already exists
            final String query = String.format(
                    LOCK_QUERY,
                    lockMc.getID(),
                    lockMc.getPrimaryKey(),
                    lockMc.getTableName(),
                    bean.getMetaObject().getMetaClass().getID(),
                    bean.getMetaObject().getID());
            final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0);

            if ((mos != null) && (mos.length > 0)) {
                final LockAlreadyExistsException ex = new LockAlreadyExistsException(
                        "The lock does already exists",
                        String.valueOf(mos[0].getBean().getProperty("user_string")));

                throw ex;
            }

            // create lock
            final String userString = NbBundle.getMessage(
                    CidsBeanLocker.class,
                    "CidsLayerLocker.lock(CidsBean).userString",
                    SessionManager.getSession().getUser().toString());
            CidsBean lockBean = lockMc.getEmptyInstance().getBean();
            lockBean.setProperty("class_id", bean.getMetaObject().getMetaClass().getID());
            lockBean.setProperty("object_id", bean.getMetaObject().getId());
            lockBean.setProperty("user_string", userString);
            lockBean = lockBean.persist();

            return lockBean;
        } catch (LockAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while creating lock object", e);
            throw new Exception("Cannot lock object");
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
            bean.delete();
            bean.persist();
        } catch (Exception e) {
            LOG.error("Cannot remove lock with id " + bean.getProperty("id"));
            throw e;
        }
    }

    /**
     * Determines the meta class of the cs_locks cids class, that should be used to lock the given bean.
     *
     * @param   bean  the bean to lock
     *
     * @return  the meta class of the cs_locks cids class, that should be used to lock the given bean
     *
     * @throws  Exception  if the meta class cannot be determined
     */
    protected MetaClass getLockMetaClassForBean(final CidsBean bean) throws Exception {
        // determine the cs_locks meta class
        final String domain = bean.getMetaObject().getDomain();
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
}
