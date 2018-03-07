/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.ui.tree.SearchResultsTree;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MetaObjectNodeTree extends SearchResultsTree {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSearchProtocolStepPanelSearchObjectsTree object.
     *
     * @param   connectionContext  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public MetaObjectNodeTree(final ConnectionContext connectionContext) throws Exception {
        super(connectionContext);
    }

    /**
     * Creates a new GeoSearchProtocolStepPanelSearchObjectsTree object.
     *
     * @param   useThread          DOCUMENT ME!
     * @param   maxThreadCount     DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public MetaObjectNodeTree(final boolean useThread,
            final int maxThreadCount,
            final ConnectionContext connectionContext) throws Exception {
        super(useThread, maxThreadCount, connectionContext);
    }
}
