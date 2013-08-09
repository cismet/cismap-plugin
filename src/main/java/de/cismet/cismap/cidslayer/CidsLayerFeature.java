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
package de.cismet.cismap.cidslayer;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.util.concurrent.ExecutorService;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;

import de.cismet.commons.concurrency.CismetConcurrency;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerFeature extends DefaultFeatureServiceFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static transient Logger LOG = Logger.getLogger(CidsLayerFeature.class);

    //~ Instance fields --------------------------------------------------------

    private final int classId;
    private MetaObject metaObject;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerFeature object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public CidsLayerFeature(final CidsLayerFeature feature) {
        super(feature);
        classId = feature.classId;
        if (feature.metaObject != null) {
            metaObject = feature.metaObject;
        } else {
            final ExecutorService executor = CismetConcurrency.getInstance("CidsLayerFeature").getDefaultExecutor();
            executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            metaObject = SessionManager.getConnection()
                                        .getMetaObject(
                                                SessionManager.getSession().getUser(),
                                                getId(),
                                                classId,
                                                "WRRL_DB_MV");
                            LOG.info("MetaObject geladen: " + getId());
                        } catch (ConnectionException ex) {
                            LOG.error("Could not load the metaObject", ex);
                        }
                    }
                });
        }
    }

    /**
     * Creates a new CidsLayerFeature object.
     *
     * @param  oid              DOCUMENT ME!
     * @param  cid              DOCUMENT ME!
     * @param  geometry         DOCUMENT ME!
     * @param  layerProperties  DOCUMENT ME!
     */
    public CidsLayerFeature(final int oid,
            final int cid,
            final Geometry geometry,
            final LayerProperties layerProperties) {
        super(oid, geometry, layerProperties);
        classId = cid;
        final ExecutorService executor = CismetConcurrency.getInstance("CidsLayerFeature").getDefaultExecutor();
        executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        metaObject = SessionManager.getConnection()
                                    .getMetaObject(SessionManager.getSession().getUser(), oid, cid, "WRRL_DB_MV");
                        LOG.info("MetaObject geladen: " + oid);
                    } catch (ConnectionException ex) {
                        LOG.error("Could not load the metaObject", ex);
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object clone() {
        return new CidsLayerFeature(this);
    }
}
