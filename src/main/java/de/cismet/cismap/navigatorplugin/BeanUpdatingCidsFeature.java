/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 thorsten
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
package de.cismet.cismap.navigatorplugin;

import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class BeanUpdatingCidsFeature extends CidsFeature {

    //~ Instance fields --------------------------------------------------------

    CidsBean cidsBean = null;
    String geoPropertyName;
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BeanUpdatingCidsFeature object.
     *
     * @param   mo  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public BeanUpdatingCidsFeature(final MetaObject mo) throws IllegalArgumentException {
        super(mo);
        cidsBean = mo.getBean();
    }
    /**
     * Creates a new BeanUpdatingCidsFeature object.
     *
     * @param   cidsBean         DOCUMENT ME!
     * @param   geoPropertyName  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public BeanUpdatingCidsFeature(final CidsBean cidsBean, final String geoPropertyName)
            throws IllegalArgumentException {
        this(cidsBean.getMetaObject());
        this.geoPropertyName = geoPropertyName;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setGeometry(final Geometry geom) {
        super.setGeometry(geom);
        try {
            final Geometry oldGeom = (Geometry)cidsBean.getProperty(geoPropertyName);
            // oldGeom !(===) geom
            if (!((oldGeom == geom) || ((oldGeom != null) && oldGeom.equals(geom)))) {
                cidsBean.setProperty(geoPropertyName, geom);
            }
        } catch (Exception e) {
            log.error("error while storing updated geometry in bean", e);
        }
    }
}
