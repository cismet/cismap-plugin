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
package de.cismet.cismap.navigatorplugin;

import com.vividsolutions.jts.geom.Geometry;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.feature.DefaultCacheGeometryProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = DefaultCacheGeometryProvider.class)
public class CidsFeatureBasedCachedGeometryProvider implements DefaultCacheGeometryProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry getCacheGeometry(final CidsBean bean) {
        final CidsFeature cf = new CidsFeature(bean.getMetaObject());
        return cf.getGeometry();
    }
}
