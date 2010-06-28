/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.supportingrasterlayer;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupporterRasterServiceUrl;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SicadShowMapPlSupporter extends SimpleFeatureSupportingRasterLayer {

    //~ Instance fields --------------------------------------------------------

    SimpleFeatureSupporterRasterServiceUrl url;
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SicadShowMapPlSupporter object.
     *
     * @param  s  DOCUMENT ME!
     */
    public SicadShowMapPlSupporter(final SicadShowMapPlSupporter s) {
        super(s);
        url = s.url;
        if (log.isDebugEnabled()) {
            log.debug("New SicadShowMapPlSupporter (copy constructor)"); // NOI18N
        }
    }
    /**
     * Creates a new instance of SicadShowMapPlSupporter.
     *
     * @param  url  DOCUMENT ME!
     */
    public SicadShowMapPlSupporter(final SimpleFeatureSupporterRasterServiceUrl url) {
        super(url);
        this.url = url;
        if (log.isDebugEnabled()) {
            log.debug("New SicadShowMapPlSupporter"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void retrieve(final boolean forced) {
        try {
            url.setFilter(getFilter());

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
    private String getFilter() {
        String ret = ""; // NOI18N
        int objectCounter = 0;
        int inObjectCounter = 0;

        final FeatureCollection fc = CismapBroker.getInstance().getMappingComponent().getFeatureCollection();
        if (log.isDebugEnabled()) {
            log.debug("in getFilter(): getFeatureCollection():" + getFeatureCollection()); // NOI18N
        }
        // fc=getFeatureCollection();
        for (final Object f : fc.getAllFeatures()) {
            if (f instanceof RasterLayerSupportedFeature) {
                final RasterLayerSupportedFeature rlsf = (RasterLayerSupportedFeature)f;
                if ((rlsf.getSupportingRasterService() != null) && rlsf.getSupportingRasterService().equals(this)) {
                    if (inObjectCounter == 0) {
                        objectCounter++;
                        ret += "&object" + objectCounter + "="; // NOI18N
                    }
                    ret += rlsf.getFilterPart();
                    inObjectCounter++;
                    if (inObjectCounter == 3) {
                        inObjectCounter = 0;
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return getName(); // +"("+listeners.size()+")";
    }

    @Override
    public Object clone() {
        return new SicadShowMapPlSupporter(this);
    }
}
