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
package de.cismet.cismap.linearreferencing;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface CreateStationListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  route        DOCUMENT ME!
     * @param  stationGeom  DOCUMENT ME!
     * @param  station      DOCUMENT ME!
     */
    void pointFinished(CidsBean route,
            Geometry stationGeom,
            double station);
}
