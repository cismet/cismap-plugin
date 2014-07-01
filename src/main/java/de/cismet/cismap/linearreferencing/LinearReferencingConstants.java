/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 jruiz
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
public interface LinearReferencingConstants {

    //~ Instance fields --------------------------------------------------------

    String CN_STATIONLINE = "station_linie"; // NOI18N
    String CN_STATION = "station";           // NOI18N
    String CN_ROUTE = "route";               // NOI18N
    String CN_GEOM = "geom";                 // NOI18N

    String PROP_ID = "id"; // NOI18N

    String PROP_STATIONLINIE_FROM = "von";  // NOI18N
    String PROP_STATIONLINIE_TO = "bis";    // NOI18N
    String PROP_STATIONLINIE_GEOM = "geom"; // NOI18N

    String PROP_STATION_VALUE = "wert";      // NOI18N
    String PROP_STATION_ROUTE = "route";     // NOI18N
    String PROP_STATION_GEOM = "real_point"; // NOI18N

    String PROP_ROUTE_GWK = "gwk";   // NOI18N
    String PROP_ROUTE_GEOM = "geom"; // NOI18N

    String PROP_GEOM_GEOFIELD = "geo_field"; // NOI18N

    boolean FROM = true;
    boolean TO = false;
}
