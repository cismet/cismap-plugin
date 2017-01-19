/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 jruiz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.linearreferencing;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class LinearReferencedLineRenderer extends LinearReferencedLineEditor {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinearReferencedLineRenderer object.
     *
     * @param  routeMetaClassname  DOCUMENT ME!
     */
    public LinearReferencedLineRenderer(final String routeMetaClassname) {
        super(false, routeMetaClassname);
    }

    /**
     * Creates a new LinearReferencedLineRenderer object.
     *
     * @param  isDrawingFeaturesEnabled  DOCUMENT ME!
     * @param  routeMetaClassname        DOCUMENT ME!
     */
    public LinearReferencedLineRenderer(final boolean isDrawingFeaturesEnabled, final String routeMetaClassname) {
        super(false, isDrawingFeaturesEnabled, false, routeMetaClassname);
    }
}
