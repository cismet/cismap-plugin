/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import org.jdom.Element;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class Properties {

    //~ Instance fields --------------------------------------------------------

    Element root = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Properties.
     *
     * @param  root  DOCUMENT ME!
     */
    public Properties(final Element root) {
        this.root = root;
    }
}
