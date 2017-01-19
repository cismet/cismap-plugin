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
package de.cismet.cismap.navigatorplugin.supportingrasterlayer;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupporterRasterServiceUrl;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class RasterFariLayer extends SimpleFeatureSupportingRasterLayer {

    //~ Instance fields --------------------------------------------------------

    SimpleFeatureSupporterRasterServiceUrl url;
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CustomPlanLayer object.
     *
     * @param  s  DOCUMENT ME!
     */
    public RasterFariLayer(final RasterFariLayer s) {
        super(s);
        url = s.url;
        if (log.isDebugEnabled()) {
            log.debug("New CustomPlanLayer (copy constructor)"); // NOI18N
        }
    }
    /**
     * Creates a new instance of SicadShowMapPlSupporter.
     *
     * @param  url  DOCUMENT ME!
     */
    public RasterFariLayer(final SimpleFeatureSupporterRasterServiceUrl url) {
        super(url);
        this.url = url;
        if (log.isDebugEnabled()) {
            log.debug("New RastaFariLayer"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void retrieve(final boolean forced) {
        try {
            url.setFilter(getLayerString());
            if (log.isDebugEnabled()) {
                log.debug("FeaturesupportingRasterServiceRequest:" + url);
            }
            super.retrieve(forced);
        } catch (Exception e) {
            log.error("No FeatureSupportingRasterService .-(", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getLayerString() {
        String ret = ""; // NOI18N
        int inObjectCounter = 0;

        final FeatureCollection fc = CismapBroker.getInstance().getMappingComponent().getFeatureCollection();

        // fc=getFeatureCollection();
        for (final Object f : fc.getAllFeatures()) {
            if (f instanceof RasterLayerSupportedFeature) {
                final RasterLayerSupportedFeature rlsf = (RasterLayerSupportedFeature)f;
                if ((rlsf.getSupportingRasterService() != null) && rlsf.getSupportingRasterService().equals(this)) {
                    if (inObjectCounter == 0) {
                        ret += "&LAYERS=";                   // NOI18N
                    }
                    ret += rlsf.getSpecialLayerName() + ","; // NOI18N
                    inObjectCounter++;
                }
            }
        }
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    @Override
    public String toString() {
        return getName(); // +"("+listeners.size()+")";
    }

    @Override
    public Object clone() {
        return new RasterFariLayer(this);
    }
}
