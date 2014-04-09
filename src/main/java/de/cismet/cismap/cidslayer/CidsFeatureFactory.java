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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.postgresql.util.PGobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cids.server.search.builtin.CidsLayerInitStatement;
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
class CidsFeatureFactory extends AbstractFeatureFactory<CidsLayerFeature, String> {

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureServiceAttribute> featureServiceAttributes;

    GeomFromWktConverter converter = new GeomFromWktConverter();
    MetaClass metaClass;
    private Geometry envelope;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param  cff  DOCUMENT ME!
     */
    public CidsFeatureFactory(final CidsFeatureFactory cff) {
        super(cff);
        metaClass = cff.metaClass;
        this.envelope = cff.envelope;
    }

    /**
     * Creates a new CidsFeatureFactory object.
     *
     * @param       metaClass        DOCUMENT ME!
     * @param       layerProperties  DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    public CidsFeatureFactory(final MetaClass metaClass, final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
        this.metaClass = metaClass;
        initLayer();
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
        this.setSLDStyle(styles);
        this.metaClass = metaClass;
        layerName = metaClass.getTableName();
        initLayer();
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
    public List<CidsLayerFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread) throws TooManyFeaturesException, Exception {
        return createFeaturesInternal(query, boundingBox, workerThread, 0, 0, null, true);
    }

    /**
     * DOCUMENT ME!
     */
    private void initLayer() {
        try {
            final CidsLayerInitStatement serverSearch = new CidsLayerInitStatement(metaClass);
            final ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)SessionManager.getProxy()
                        .customServerSearch(SessionManager.getSession().getUser(), serverSearch);
            featureServiceAttributes = new ArrayList<FeatureServiceAttribute>();
            final String crs = CismapBroker.getInstance().getDefaultCrs();

            for (final ArrayList row : resultArray) {
                if (row.size() == 1) {
                    envelope = converter.convertForward((String)row.get(0), crs);
                } else {
                    final String type = String.valueOf(getTypeByTypeName((String)row.get(1)));
                    featureServiceAttributes.add(new FeatureServiceAttribute((String)row.get(0), type, true));
                }
            }
        } catch (Exception e) {
            logger.error("Error while initialiseing the cids layer.", e);
        }
    }

    @Override
    public List<FeatureServiceAttribute> createAttributes(final SwingWorker workerThread)
            throws TooManyFeaturesException, UnsupportedOperationException, Exception {
        if (featureServiceAttributes == null) {
            initLayer();
        }

        if (featureServiceAttributes == null) {
            logger.warn("FeatureServiceAttributes for cids feature factory not found");
        }

        return featureServiceAttributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getTypeByTypeName(final String type) {
        if (type.equalsIgnoreCase("text") || type.equalsIgnoreCase("varchar")) {
            return Types.VARCHAR;
        } else if (type.equalsIgnoreCase("double precision") || type.equalsIgnoreCase("float8")) {
            return Types.DOUBLE;
        } else if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("int")) {
            return Types.INTEGER;
        } else if (type.toLowerCase().contains("timestamp")) {
            return Types.TIMESTAMP;
        } else if (type.toLowerCase().contains("time")) {
            return Types.TIME;
        } else if (type.toLowerCase().contains("date")) {
            return Types.DATE;
        } else {
            return Types.VARCHAR;
        }
    }

    @Override
    public List<CidsLayerFeature> createFeatures(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
        return createFeaturesInternal(query, boundingBox, workerThread, offset, limit, orderBy, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   query              DOCUMENT ME!
     * @param   boundingBox        DOCUMENT ME!
     * @param   workerThread       DOCUMENT ME!
     * @param   offset             DOCUMENT ME!
     * @param   limit              DOCUMENT ME!
     * @param   orderBy            DOCUMENT ME!
     * @param   saveAsLastCreated  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    private List<CidsLayerFeature> createFeaturesInternal(final String query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy,
            final boolean saveAsLastCreated) throws TooManyFeaturesException, Exception {
        if (checkCancelled(workerThread, "creatureFeatures()")) {
            return null;
        }
        final String crs = CismapBroker.getInstance().getDefaultCrs();

        final CrsTransformer transformer = new CrsTransformer(crs);
        final BoundingBox boundingBox2 = transformer.transformBoundingBox(
                boundingBox,
                CismapBroker.getInstance().getSrs().getCode());
        final CidsLayerSearchStatement serverSearch = new CidsLayerSearchStatement(metaClass);
        serverSearch.setSrid(CismapBroker.getInstance().getDefaultCrsAlias());
        String[] orderByStrings = new String[0];

        if (orderBy != null) {
            orderByStrings = new String[orderBy.length];

            for (int i = 0; i < orderBy.length; ++i) {
                orderByStrings[i] = orderBy[i].getName();
            }
        }

        serverSearch.setX1(boundingBox2.getX1());
        serverSearch.setY1(boundingBox2.getY1());
        serverSearch.setX2(boundingBox2.getX2());
        serverSearch.setY2(boundingBox2.getY2());
        serverSearch.setQuery(query);
        serverSearch.setLimit(limit);
        serverSearch.setOffset(offset);
        serverSearch.setOrderBy(orderByStrings);
        if (!saveAsLastCreated) {
            serverSearch.setExactSearch(true);
        }

        if (checkCancelled(workerThread, "PreQuery")) {
            return null;
        }
        final Collection resultCollection = SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(), serverSearch);
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

        CidsLayerFeature lastFeature = null;

        for (int i = 1; i < resultArray.size(); i++) {
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

            if (lastFeature == null) {
                lastFeature = new CidsLayerFeature(
                        properties /*oid, cid, geom,*/,
                        metaClass,
                        layerProperties,
                        getStyle(metaClass.getTableName()));
            }
            features.add(lastFeature);
            lastFeature = null;
        }
        if (checkCancelled(workerThread, "PreReturn()")) {
            return null;
        }

        if (saveAsLastCreated) {
            updateLastCreatedFeatures(
                features,
                boundingBox2.getGeometry(CrsTransformer.extractSridFromCrs(crs)),
                query);
        }

        return features;
    }

    @Override
    public int getFeatureCount(final BoundingBox bb) {
        try {
            final String crs = CismapBroker.getInstance().getDefaultCrs();

            final CrsTransformer transformer = new CrsTransformer(crs);
            final BoundingBox boundingBox2 = transformer.transformBoundingBox(
                    bb,
                    CismapBroker.getInstance().getSrs().getCode());
            final CidsLayerSearchStatement serverSearch = new CidsLayerSearchStatement(metaClass);
            serverSearch.setSrid(CismapBroker.getInstance().getDefaultCrsAlias());

            serverSearch.setX1(boundingBox2.getX1());
            serverSearch.setY1(boundingBox2.getY1());
            serverSearch.setX2(boundingBox2.getX2());
            serverSearch.setCountOnly(true);

            final Collection resultCollection = SessionManager.getProxy()
                        .customServerSearch(SessionManager.getSession().getUser(), serverSearch);

            final ArrayList<ArrayList> resultArray = (ArrayList<ArrayList>)resultCollection;

            if ((resultArray != null) && (resultArray.size() > 0) && (resultArray.get(0).size() > 0)) {
                return (Integer)resultArray.get(0).get(0);
            }
        } catch (Exception e) {
            logger.error("Cannot determine the feature count", e);
        }

        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the envelope
     */
    public Geometry getEnvelope() {
        return envelope;
    }
}
