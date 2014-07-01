/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.linearreferencing;

import java.util.EventListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface LinearReferencedLineEditorListener extends EventListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void linearReferencedLineCreated();

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    void otherLinesPanelVisibilityChange(boolean visible);
}
