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

import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.gui.attributetable.LockAlreadyExistsException;
import de.cismet.cismap.commons.gui.attributetable.LockFromSameUserAlreadyExistsException;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * Can be used to lock cids beans. It will be assumed that the cids system contains a cids class with the name cs_locks
 * in the same domain as the cids bean to lock.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsBeanLocker implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    public static final String CS_LOCKS_TN = "cs_locks";

    private static final Logger LOG = Logger.getLogger(CidsBeanLocker.class);
    private static final String LOCK_QUERY = "SELECT DISTINCT %1$s, %2$s "
                + " FROM %3$s WHERE class_id = %4$s and (object_id = %5$s or object_id is null);";
    private static final String LOCK_MC_QUERY = "SELECT DISTINCT %1$s, %2$s "
                + " FROM %3$s WHERE class_id = %4$s limit 1;";

    //~ Instance fields --------------------------------------------------------

    private final Map<String, MetaClass> LOCK_MC_MAP = new HashMap<String, MetaClass>();

    private final ConnectionContext connectionContext = ConnectionContext.createDummy();
                    

    //~ Methods ----------------------------------------------------------------

    /**
     * Locks the given bean.
     *
     * @param   bean                         the cids bean to lock
     * @param   multiLockForSameUserAllowed  DOCUMENT ME!
     *
     * @return  the lock bean. This bean can be used to unlock the given bean
     *
     * @throws  LockAlreadyExistsException  if the given bean is already locked
     * @throws  Exception                   DOCUMENT ME!
     */
    public CidsBean lock(final CidsBean bean, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
        try {
            final MetaClass lockMc = getLockMetaClassForBean(bean.getMetaObject().getDomain());
            final String userString = NbBundle.getMessage(
                    CidsBeanLocker.class,
                    "CidsLayerLocker.lock(CidsBean).userString",
                    SessionManager.getSession().getUser().getName());

            if (bean.getMetaObject().getMetaClass().getID() == -1) {
                // object with id -1 are new objects, which cannot be locked, because they do not exists on the server
                return new FakeLock();
            }

            // Check, if the lock already exists
            final String query = String.format(
                    LOCK_QUERY,
                    lockMc.getID(),
                    lockMc.getPrimaryKey(),
                    lockMc.getTableName(),
                    bean.getMetaObject().getMetaClass().getID(),
                    bean.getMetaObject().getID());
            final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0, getConnectionContext());

            if ((mos != null) && (mos.length > 0)) {
                if ((mos[0].getBean().getProperty("user_string") == null)
                            && mos[0].getBean().getProperty("user_string").equals(userString)) {
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

            // create lock
            CidsBean lockBean = lockMc.getEmptyInstance().getBean();
            lockBean.setProperty("class_id", bean.getMetaObject().getMetaClass().getID());
            lockBean.setProperty("object_id", bean.getMetaObject().getId());
            lockBean.setProperty("user_string", userString);

            try {
                final InetAddress addr = InetAddress.getLocalHost();
                lockBean.setProperty("additional_info", addr.getHostName());
            } catch (UnknownHostException e) {
                LOG.error("cnnot determine the computer name", e);
            }
            lockBean = lockBean.persist(getConnectionContext());

            return lockBean;
        } catch (LockAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while creating lock object", e);
            throw new Exception("Cannot lock object");
        }
    }

    /**
     * Locks all beans of the given meta class.
     *
     * @param   mc                           the meta class to lock
     * @param   multiLockForSameUserAllowed  DOCUMENT ME!
     *
     * @return  the lock bean. This bean can be used to unlock the given meta class
     *
     * @throws  LockAlreadyExistsException  if at least one bean of the given meta class is already locked
     * @throws  Exception                   DOCUMENT ME!
     */
    public CidsBean lock(final MetaClass mc, final boolean multiLockForSameUserAllowed)
            throws LockAlreadyExistsException, Exception {
        try {
            final MetaClass lockMc = getLockMetaClassForBean(mc.getDomain());
            final String userString = NbBundle.getMessage(
                    CidsBeanLocker.class,
                    "CidsLayerLocker.lock(CidsBean).userString",
                    SessionManager.getSession().getUser().getName());

            // Check, if the lock already exists
            final String query = String.format(
                    LOCK_MC_QUERY,
                    lockMc.getID(),
                    lockMc.getPrimaryKey(),
                    lockMc.getTableName(),
                    mc.getID());
            final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0, getConnectionContext());

            if ((mos != null) && (mos.length > 0)) {
                if ((mos[0].getBean().getProperty("user_string") == null)
                            && mos[0].getBean().getProperty("user_string").equals(userString)) {
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

            // create lock
            CidsBean lockBean = lockMc.getEmptyInstance().getBean();
            lockBean.setProperty("class_id", mc.getID());
            lockBean.setProperty("object_id", null);
            lockBean.setProperty("user_string", userString);
            lockBean.setProperty("additional_info", "locks the whole table");
            lockBean = lockBean.persist(getConnectionContext());

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
            lockMc = ClassCacheMultiple.getMetaClass(domain, CS_LOCKS_TN, getConnectionContext());

            if (lockMc == null) {
                throw new Exception("The cids class " + CS_LOCKS_TN + " does not exist in the domain " + domain);
            }
            LOCK_MC_MAP.put(domain, lockMc);
        }

        return lockMc;
    }

    @Override
    public final ConnectionContext getConnectionContext() {
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
