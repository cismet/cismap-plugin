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
package de.cismet.cismap.navigatorplugin.metasearch;

import java.util.EventListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface MetaSearchListener extends EventListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void topicAdded(final MetaSearchListenerEvent event);

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void topicsAdded(final MetaSearchListenerEvent event);

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void topicRemoved(final MetaSearchListenerEvent event);

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void topicsRemoved(final MetaSearchListenerEvent event);

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void topicSelectionChanged(final MetaSearchListenerEvent event);
}
