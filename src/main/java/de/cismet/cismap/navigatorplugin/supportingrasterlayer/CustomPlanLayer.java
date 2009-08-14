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
 *
 * @author thorsten
 */
    public class CustomPlanLayer extends SimpleFeatureSupportingRasterLayer{
   private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    SimpleFeatureSupporterRasterServiceUrl url;
    /**
     * Creates a new instance of SicadShowMapPlSupporter
     */
    public CustomPlanLayer(SimpleFeatureSupporterRasterServiceUrl url) {
        super(url);
        this.url=url;
        log.debug("New SicadShowMapPlSupporter");
    }
    public CustomPlanLayer(CustomPlanLayer s) {
        super(s);
        url=s.url;
        log.debug("New SicadShowMapPlSupporter (Kopierkonstruktor)");
    }

    public void retrieve(boolean forced) {
        try {
            url.setFilter(getLayerString());
            log.fatal(url);
        super.retrieve(forced);
        }
        catch (Exception e) {
            log.error("Nix FeatureSupportingRasterService .-(",e);
        }

    }

    private String getLayerString(){


        String ret="";
        int objectCounter=0;
        int inObjectCounter=0;


        FeatureCollection fc=CismapBroker.getInstance().getMappingComponent().getFeatureCollection();


        
        //fc=getFeatureCollection();
        for (Object f:fc.getAllFeatures() ) {
            if (f instanceof RasterLayerSupportedFeature )  {
                RasterLayerSupportedFeature rlsf=(RasterLayerSupportedFeature)f;
                if (rlsf.getSupportingRasterService()!=null &&rlsf.getSupportingRasterService().equals(this)) {
                    if (inObjectCounter==0) {
                        ret+="&layers=";
                    }
                    ret+=rlsf.getSpecialLayerName()+",";
                    inObjectCounter++;
                    if (inObjectCounter==3) {
                        inObjectCounter=0;
                    }
                }
            }
        }
        ret=ret.substring(0,ret.length()-1);
        return ret;
    }

    public String toString() {
        return getName();//+"("+listeners.size()+")";
    }

    public Object clone() {
        return new CustomPlanLayer(this);
    }
}
