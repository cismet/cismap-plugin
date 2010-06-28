/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.featurerenderer;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import de.cismet.cismap.commons.features.FeatureRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCidsFeatureRenderer implements FeatureRenderer {

    //~ Instance fields --------------------------------------------------------

    protected MetaObject metaObject;
    protected MetaClass metaClass;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject  DOCUMENT ME!
     *
     * @throws  ConnectionException  DOCUMENT ME!
     */
    public void setMetaObject(final MetaObject metaObject) throws ConnectionException {
        this.metaObject = metaObject;
        metaClass = SessionManager.getProxy().getMetaClass(metaObject.getClassKey());
    }
}
