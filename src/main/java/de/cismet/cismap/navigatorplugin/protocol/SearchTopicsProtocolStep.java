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

import java.util.Set;

import de.cismet.commons.gui.protocol.ProtocolStep;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface SearchTopicsProtocolStep extends ProtocolStep {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Set<MetaSearchProtocolStepSearchTopic> getSearchTopicInfos();
}
