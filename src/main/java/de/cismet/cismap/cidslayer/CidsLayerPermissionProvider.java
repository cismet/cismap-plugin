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
package de.cismet.cismap.cidslayer;

import Sirius.server.newuser.User;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface CidsLayerPermissionProvider {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   u        DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean getCustomCidsLayerWritePermissionDecisionforUser(final User u, CidsLayerFeature feature);
}
