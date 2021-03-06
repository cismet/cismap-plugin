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

import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.SearchFeature;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CidsBeansSearchFeature extends SearchFeature {

    //~ Instance fields --------------------------------------------------------

    private final Collection<CidsBean> beans;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeansSearchFeature object.
     *
     * @param  geom               DOCUMENT ME!
     * @param  beans              DOCUMENT ME!
     * @param  inputListenerName  DOCUMENT ME!
     */
    public CidsBeansSearchFeature(final Geometry geom,
            final Collection<CidsBean> beans,
            final String inputListenerName) {
        super(geom, inputListenerName);

        this.beans = beans;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   beans            DOCUMENT ME!
     * @param   interactionmode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CidsBeansSearchFeature createFromBeans(final Collection<CidsBean> beans,
            final String interactionmode) {
        final Collection<Geometry> searchGeoms = new ArrayList<Geometry>();
        Integer origSrid = null;
        String metricCrs = null;

        for (final CidsBean cb : beans) {
            final MetaObject mo = cb.getMetaObject();
            final CidsFeature cf = new CidsFeature(mo);

            if (origSrid == null) {
                origSrid = cf.getGeometry().getSRID();
                final int metricSrid = CrsTransformer.transformToMetricCrs(cf.getGeometry()).getSRID();
                metricCrs = CrsTransformer.createCrsFromSrid(metricSrid);
            }

            searchGeoms.add(CrsTransformer.transformToGivenCrs(cf.getGeometry(), metricCrs));
        }

        final Geometry[] searchGeomsArr = searchGeoms.toArray(
                new Geometry[0]);
        final GeometryCollection coll = new GeometryFactory().createGeometryCollection(searchGeomsArr);

        Geometry newG = coll.buffer(0.1d);
        newG.setSRID(CrsTransformer.extractSridFromCrs(metricCrs));

        if (origSrid != null) {
            newG = CrsTransformer.transformToGivenCrs(newG, CrsTransformer.createCrsFromSrid(origSrid));
            newG.setSRID(origSrid);
        }

        return new CidsBeansSearchFeature(newG, beans, interactionmode);
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<CidsBean> getBeans() {
        return beans;
    }
}
