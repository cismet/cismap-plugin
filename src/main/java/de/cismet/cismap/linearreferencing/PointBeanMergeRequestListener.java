/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.linearreferencing;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface PointBeanMergeRequestListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  fromPoint      DOCUMENT ME!
     * @param  withPointBean  DOCUMENT ME!
     */
    void pointBeanMergeRequest(boolean fromPoint, CidsBean withPointBean);

    /**
     * DOCUMENT ME!
     *
     * @param  fromPoint  DOCUMENT ME!
     */
    void pointBeanSplitRequest(boolean fromPoint);
}
