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

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cids.server.search.builtin.CidsLayerSearchStatement;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
class CidsFeatureFactory extends AbstractFeatureFactory<CidsLayerFeature, CidsLayerSearchStatement> {

    //~ Instance fields --------------------------------------------------------

    GeomFromWktConverter converter = new GeomFromWktConverter();
    // private BoundingBox lastBB = null;
    // private BoundingBox diff = null;
    // private final WKTReader reader;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  cff  DOCUMENT ME!
     */
    public CidsFeatureFactory(final CidsFeatureFactory cff) {
        super(cff);
    }

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  layerProperties  DOCUMENT ME!
     */
    public CidsFeatureFactory(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected boolean isGenerateIds() {
        return false;
    }

    @Override
    public CidsFeatureFactory clone() {
        return new CidsFeatureFactory(this);
    }

    @Override
    public List<CidsLayerFeature> createFeatures(final CidsLayerSearchStatement query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread) throws TooManyFeaturesException, Exception {
        if (checkCancelled(workerThread, "creatureFeatures()")) {
            return null;
        }
        final String crs = CismapBroker.getInstance().getDefaultCrs().toString();

        query.setX1(boundingBox.getX1());
        query.setY1(boundingBox.getY1());
        query.setX2(boundingBox.getX2());
        query.setY2(boundingBox.getY2());

        /*if (lastBB != null) {
         *  query.setOldX1(lastBB.getX1()); query.setOldY1(lastBB.getY1()); query.setOldX2(lastBB.getX2());
         * query.setOldY2(lastBB.getY2());}*/

        /*query.setCountOnly(true);
         *
         * long count = 0; Collection resultCollection = SessionManager.getProxy()
         * .customServerSearch(SessionManager.getSession().getUser(), query);
         *
         *
         * ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>) resultCollection;
         *
         * if (resultArray.size() != 1) { count= -1; } else { if (resultArray.get(0).size() != 1) { count= -1; } else {
         * count= (Long) resultArray.get(0).get(0); } }
         *
         *
         * if(this.logger.isDebugEnabled()) { this.logger.debug("CidsFeatureFactory["+workerThread+"]: "+ count + "
         * matching features in selected bounding box"); }*/

        // query.setCountOnly(false);
        // query.setClassId("wk_sg");

        if (checkCancelled(workerThread, "PreQuery")) {
            return null;
        }
        final Collection resultCollection = SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(), query);
        if (checkCancelled(workerThread, "PostQuery")) {
            return null;
        }
        final ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)resultCollection;
        int cid;
        int oid;
        Geometry geom;
        final Vector<CidsLayerFeature> features = new Vector<CidsLayerFeature>();

        for (final ArrayList row : resultArray) {
            oid = (Integer)row.get(2);
            cid = (Integer)row.get(0);
            geom = converter.convertForward((String)row.get(1), crs);
            // obj = SessionManager.getConnection().getMetaObject(SessionManager.getSession().getUser(), oid, cid,
            // "WRRL_DB_MV");
            features.add(new CidsLayerFeature(oid, cid, geom, layerProperties));
        }
        if (checkCancelled(workerThread, "PreReturn()")) {
            return null;
        }
        lastCreatedfeatureVector = features;
        // lastBB = boundingBox;

        return features;
    }

    @Override
    public List<FeatureServiceAttribute> createAttributes(final SwingWorker workerThread)
            throws TooManyFeaturesException, UnsupportedOperationException, Exception {
        final List<FeatureServiceAttribute> featureServiceAttributes = new LinkedList<FeatureServiceAttribute>();

        return featureServiceAttributes;
    }
}
