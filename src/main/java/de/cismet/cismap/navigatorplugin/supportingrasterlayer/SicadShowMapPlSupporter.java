/*
 * SicadShowMapPlSupporter.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 18. Juli 2006, 16:59
 *
 */

package de.cismet.cismap.navigatorplugin.supportingrasterlayer;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupporterRasterServiceUrl;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SicadShowMapPlSupporter extends SimpleFeatureSupportingRasterLayer{
    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    SimpleFeatureSupporterRasterServiceUrl url;
    /**
     * Creates a new instance of SicadShowMapPlSupporter
     */
    public SicadShowMapPlSupporter(SimpleFeatureSupporterRasterServiceUrl url) {
        super(url);
        this.url=url;
        log.debug("New SicadShowMapPlSupporter");
    }
    public SicadShowMapPlSupporter(SicadShowMapPlSupporter s) {
        super(s);
        url=s.url;
        log.debug("New SicadShowMapPlSupporter (Kopierkonstruktor)");
    }

    public void retrieve(boolean forced) {
        try {
            url.setFilter(getFilter());
        
        super.retrieve(forced);
        }
        catch (Exception e) {
            log.error("Nix FeatureSupportingRasterService .-(",e);
        }
                
    }
    
    private String getFilter(){
        
        
        String ret="";
        int objectCounter=0;
        int inObjectCounter=0;
        
        
        FeatureCollection fc=CismapBroker.getInstance().getMappingComponent().getFeatureCollection();
        
        log.debug("in getFilter(): getFeatureCollection():"+getFeatureCollection());
        //fc=getFeatureCollection();
        for (Object f:fc.getAllFeatures() ) {
            if (f instanceof RasterLayerSupportedFeature )  {
                RasterLayerSupportedFeature rlsf=(RasterLayerSupportedFeature)f;
                if (rlsf.getSupportingRasterService()!=null &&rlsf.getSupportingRasterService().equals(this)) {
                    if (inObjectCounter==0) {
                        objectCounter++;
                        ret+="&object"+objectCounter+"=";
                    }
                    ret+=rlsf.getFilterPart();
                    inObjectCounter++;
                    if (inObjectCounter==3) {
                        inObjectCounter=0;
                    }
                }
            }
        }
        return ret;
    }

    public String toString() {
        return getName();//+"("+listeners.size()+")";
    }
    
    public Object clone() {
        return new SicadShowMapPlSupporter(this);
    }


}
