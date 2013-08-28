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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cids.server.search.builtin.CidsLayerSearchStatement;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

import de.cismet.commons.converter.ConversionException;

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
    Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles;
    MetaClass metaClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  cff  DOCUMENT ME!
     */
    public CidsFeatureFactory(final CidsFeatureFactory cff) {
        super(cff);
        metaClass = cff.metaClass;
    }

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  metaClass        DOCUMENT ME!
     * @param  layerProperties  DOCUMENT ME!
     */
    public CidsFeatureFactory(final MetaClass metaClass, final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
        this.metaClass = metaClass;
    }

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  metaClass        DOCUMENT ME!
     * @param  layerProperties  DOCUMENT ME!
     * @param  styles           DOCUMENT ME!
     */
    public CidsFeatureFactory(final MetaClass metaClass,
            final LayerProperties layerProperties,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) {
        this.layerProperties = layerProperties;
        this.styles = styles;
        this.metaClass = metaClass;
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

        final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getDefaultCrs());
        final BoundingBox boundingBox2 = transformer.transformBoundingBox(
                boundingBox,
                CismapBroker.getInstance().getSrs().getCode());
        // boundingBox.getGeometry(-1);
        query.setSrid(CismapBroker.getInstance().getDefaultCrsAlias());

        query.setX1(boundingBox2.getX1());
        query.setY1(boundingBox2.getY1());
        query.setX2(boundingBox2.getX2());
        query.setY2(boundingBox2.getY2());

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
        final int cid;
        final int oid;
        final Geometry geom;
        final Vector<CidsLayerFeature> features = new Vector<CidsLayerFeature>();
        final List<String> columnNames = new ArrayList<String>(resultArray.get(0).size());
        for (final Object name : resultArray.get(0)) {
            columnNames.add((String)name);
        }
        org.deegree.style.se.unevaluated.Style featureStyle = styles.get("StateBoundary").getFirst();
        for (final org.deegree.style.se.unevaluated.Style style : styles.get("StateBoundary")) {
            if ((style.getFeatureType() != null)
                        && metaClass.getTableName().equals(style.getFeatureType().getLocalPart())) {
                featureStyle = style;
            }
        }
        final Iterator<CidsLayerFeature> lastFeatureIt = lastCreatedfeatureVector.iterator();
        CidsLayerFeature lastFeature = null;

        for (int i = 1; i < resultArray.size(); i++) { // final ArrayList row : resultArray) {
            final HashMap<String, Object> properties = new HashMap<String, Object>(columnNames.size());
            for (int j = resultArray.get(i).size() - 1; j >= 0; j--) {
                if (resultArray.get(i).get(j) instanceof String) {
                    try {
                        properties.put(columnNames.get(j),
                            converter.convertForward((String)resultArray.get(i).get(j), crs));
                    } catch (ConversionException ex) {
                        Logger.getLogger(this.getClass())
                                .info("Tried to parse field " + columnNames.get(j) + " to geom");
                        properties.put(columnNames.get(j), resultArray.get(i).get(j));
                    }
                } else {
                    properties.put(columnNames.get(j), resultArray.get(i).get(j));
                }
            }

            /*oid = (Integer)resultArray.get(i).get(2);
             * cid = (Integer)resultArray.get(i).get(0);geom =
             * converter.convertForward((String)resultArray.get(i).get(1), crs);*/
            // obj = SessionManager.getConnection().getMetaObject(SessionManager.getSession().getUser(), oid, cid,
            // SessionManager.getSession().getUser().getDomain());

            while (lastFeatureIt.hasNext()) {
                lastFeature = lastFeatureIt.next();
                if (lastFeature == null) {
                    break;
                }
                if (lastFeature.getId() == (Integer)properties.get("object_id")) {
                    lastFeature.setProperties(properties);
                    break;
                } else if (lastFeature.getId() > (Integer)properties.get("object_id")) {
                    lastFeature = null;
                    break;
                }
            }
            if (lastFeature == null) {
                lastFeature = new CidsLayerFeature(
                        properties /*oid, cid, geom,*/,
                        metaClass,
                        layerProperties,
                        featureStyle);
            }
            features.add(lastFeature);
            lastFeature = null;
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
