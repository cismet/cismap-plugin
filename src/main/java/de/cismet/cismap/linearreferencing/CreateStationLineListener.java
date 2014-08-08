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
public interface CreateStationLineListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  route      DOCUMENT ME!
     * @param  lineGeom   DOCUMENT ME!
     * @param  startGeom  DOCUMENT ME!
     * @param  endGeom    DOCUMENT ME!
     * @param  start      DOCUMENT ME!
     * @param  end        DOCUMENT ME!
     */
    void lineFinished(CidsBean route,
            Geometry lineGeom,
            Geometry startGeom,
            Geometry endGeom,
            double start,
            double end);
}
