/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.search.CidsServerSearchProtocolStep;

import Sirius.server.middleware.types.MetaObjectNode;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface MetaSearchProtocolStep extends CidsServerSearchProtocolStep,
    GeomSearchProtocolStep,
    SearchTopicsProtocolStep {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<MetaObjectNode> getSearchObjectNodes();
}
