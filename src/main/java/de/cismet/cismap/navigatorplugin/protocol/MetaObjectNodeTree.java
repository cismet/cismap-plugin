/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.ui.tree.SearchResultsTree;

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
     * @throws  Exception  DOCUMENT ME!
     */
    public MetaObjectNodeTree() throws Exception {
    }

    /**
     * Creates a new GeoSearchProtocolStepPanelSearchObjectsTree object.
     *
     * @param   useThread       DOCUMENT ME!
     * @param   maxThreadCount  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public MetaObjectNodeTree(final boolean useThread, final int maxThreadCount) throws Exception {
        super(useThread, maxThreadCount);
    }
}
