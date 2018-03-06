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

import de.cismet.connectioncontext.ClientConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCidsFeatureRenderer implements FeatureRenderer, ConnectionContextProvider {

    //~ Instance fields --------------------------------------------------------

    protected MetaObject metaObject;
    protected MetaClass metaClass;

    private final ClientConnectionContext connectionContext;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractCidsFeatureRenderer object.
     *
     * @param  connectionContext  DOCUMENT ME!
     */
    public AbstractCidsFeatureRenderer(final ClientConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

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
        metaClass = SessionManager.getProxy().getMetaClass(metaObject.getClassKey(), getConnectionContext());
    }

    @Override
    public final ClientConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
