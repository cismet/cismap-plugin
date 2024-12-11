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
package de.cismet.cismap.cids.geometryeditor;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClassStore;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.jdesktop.beansbinding.Converter;

import de.cismet.cids.dynamics.Disposable;

import de.cismet.cids.editors.Bindable;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.CidsFeature;

import de.cismet.connectioncontext.ConnectionContextProvider;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DefaultCismapSimpleGeomComboBoxEditor extends DefaultCismapGeometryComboBoxEditor implements Bindable,
    MetaClassStore,
    Disposable,
    ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultCismapSimpleGeomComboBoxEditor.class);

    //~ Instance fields --------------------------------------------------------

    private Geometry geometry;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultCismapGeometryComboBoxEditor object.
     */
    public DefaultCismapSimpleGeomComboBoxEditor() {
        this(true);
    }

    /**
     * Creates a new DefaultCismapGeometryComboBoxEditor object.
     *
     * @param  editable  DOCUMENT ME!
     */
    public DefaultCismapSimpleGeomComboBoxEditor(final boolean editable) {
        super(editable);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Converter getConverter() {
        return new Converter<Geometry, Feature>() {

                @Override
                public Feature convertForward(final Geometry value) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("convertForward", new CurrentStackTrace()); // NOI18N
                        }

                        geometry = value;
                        if (value != null) {
                            MetaObject cidsFeatureMetaObject = null;

                            if (cidsMetaObject != null) {
                                cidsFeatureMetaObject = cidsMetaObject;
                            } else if (metaObjectNode != null) {
                                cidsFeatureMetaObject = metaObjectNode.getObject();
                            }

                            ObjectAttribute oAttr = null;

                            if (localRenderFeatureString != null) {
                                oAttr = cidsFeatureMetaObject.getAttributeByFieldName(localRenderFeatureString);
                            }

                            cidsFeature = new CidsFeature(cidsFeatureMetaObject, oAttr) {

                                    private Geometry lastGeom = ((geometry != null) ? geometry : null);

                                    @Override
                                    public void setGeometry(final Geometry geom) {
                                        if (geom == null) {
                                            LOG.warn("ATTENTION geom=null"); // NOI18N
                                        }
                                        final Geometry oldValue = lastGeom;
                                        super.setGeometry(geom);
                                        try {
                                            if (((oldValue == null) && (geom != null))
                                                        || ((oldValue != null) && !oldValue.equalsExact(geom))) {
                                                geometry = geom;

                                                if (geom != null) {
                                                    lastGeom = (Geometry)geom.clone();
                                                } else {
                                                    lastGeom = null;
                                                }
                                            }
                                        } catch (final Exception e) {
                                            LOG.error("Error when setting the geometry.", e); // NOI18N
                                        }
                                    }
                                };
                            selectedFeature = cidsFeature;

                            comboModel.setCurrentObjectFeature(selectedFeature);
                            final FeatureCollection cismapFeatures = CismapBroker.getInstance()
                                        .getMappingComponent()
                                        .getFeatureCollection();

                            if (cismapFeatures.getAllFeatures().contains(selectedFeature)) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Feature already exists. Remove it from map."); // NOI18N
                                }

                                // As you see some lines above, selectedFeature contains a recently created object.
                                // CidsFeature.equals(Object) only compares some essential attributes, and so tells
                                // that selectedFeature is in the map. Without replacing the feature in the map with
                                // the selectedFeature all following invocations on selectedFeature won't have an
                                // effect on the feature in the map, especially setEditable(true) would be useless.
                                cismapFeatures.removeFeature(selectedFeature);
                            }

                            if (selectedFeature.getGeometry() == null) {
                                selectedFeature.setGeometry(value);
                            }

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Add selectedFeature '" + selectedFeature + "' with geometry '" // NOI18N
                                            + selectedFeature.getGeometry() + "' to feature collection.");
                            }
                            cismapFeatures.addFeature(selectedFeature);

                            selectedFeature.setEditable(true);

                            cismapFeatures.holdFeature(selectedFeature);
                            cismapFeatures.select(selectedFeature);

                            CismapBroker.getInstance().getMappingComponent().showHandles(true);
                            if (selectedFeature.getGeometry() != null) {
                                CismapBroker.getInstance()
                                        .getMappingComponent()
                                        .gotoBoundingBox(new XBoundingBox(selectedFeature.getGeometry()),
                                            false,
                                            true,
                                            0);
                            }

                            setSelectedItem(selectedFeature);
                        }
                        return selectedFeature;
                    } catch (final Exception e) {
                        LOG.error("Error in convertForward", e); // NOI18N
                        return null;
                    }
                }

                @Override
                public Geometry convertReverse(final Feature value) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("convertReverse: " + value); // NOI18N
                    }

                    if (value == null) {
                        return null;
                    } else {
                        try {
//                            if (geometryBean == null) {
//                                geometryBean = metaClass.getEmptyInstance(getConnectionContext()).getBean();
//                            }
                            final Geometry oldValue = geometry;
                            final Geometry geom = value.getGeometry();

                            if (((oldValue == null) && (geom != null))
                                        || ((oldValue != null) && !oldValue.equalsExact(geom))) {
                                geometry = value.getGeometry();
                            }
                        } catch (final Exception ex) {
                            LOG.error("Error during set geo_field", ex); // NOI18N
                        }
                        return geometry;
                    }
                }
            };
    }

    /**
     * If you want to use this combo box for different cids beans, then you should invoke this method before you bind a
     * new cids bean and after an invocation of the dispose()-method and the unbind-method of the corresponding
     * BindingGroup object.
     */
    @Override
    public void initForNewBinding() {
        // normally, the dispose()-method was invoked before the CidsMetaObject changes
        // and in this case, the feature collection listener must be registered.
        CismapBroker.getInstance()
                .getMappingComponent()
                .getFeatureCollection()
                .removeFeatureCollectionListener(comboModel);

        geometry = null;
        selectedFeature = null;
        cidsFeature = null;

        comboModel = new CismapGeometryComboModel(DefaultCismapSimpleGeomComboBoxEditor.this, selectedFeature);
        setModel(comboModel);

        CismapBroker.getInstance()
                .getMappingComponent()
                .getFeatureCollection()
                .addFeatureCollectionListener(comboModel);
    }
}
