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
package de.cismet.cismap.navigatorplugin.protocol;

import lombok.Getter;

import org.jdom.Attribute;
import org.jdom.Element;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.commons.gui.protocol.ProtocolStepConfiguration;

import de.cismet.tools.configuration.NoWriteError;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@ServiceProvider(service = ProtocolStepConfiguration.class)
public class GeometryProtocolStepConfiguration implements ProtocolStepConfiguration {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROTOCOL_STEP_KEY = "GeometryProtocolStep";

    private static final String DEFAULT_WMS_MAP_URL =
        "http://www3.demis.nl/wms/wms.asp?wms=WorldMap&&VERSION=1.1.0&REQUEST=GetMap&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS=EPSG:4326&FORMAT=image/png&TRANSPARENT=TRUE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=Ocean%20features,Airports,Spot%20elevations,Settlements,Cities,Borders,Trails,Roads,Highways,Railroads,Streams,Rivers,Inundated,Waterbodies,Coastlines,Builtup%20areas,Hillshading,Topography,Countries,Bathymetry&STYLES=";

    private static final String DEFAULT_SRS = "EPSG:4326";
    private static final double DEFAULT_ZOOM_FACTOR = 0.2;

    //~ Instance fields --------------------------------------------------------

    private String wmsMapUrl = DEFAULT_WMS_MAP_URL;
    private String srs = DEFAULT_SRS;
    private double zoomFactor = DEFAULT_ZOOM_FACTOR;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getProtocolStepKey() {
        return PROTOCOL_STEP_KEY;
    }

    @Override
    public void configure(final Element parent) {
        final Element root = parent.getChild(getProtocolStepKey());

        try {
            final Element backgroundlayer = root.getChild("backgroundlayer");
            final String wmsMapUrl = backgroundlayer.getAttributeValue("wmsMapUrl");
            final String srs = backgroundlayer.getAttributeValue("srs");

            this.wmsMapUrl = wmsMapUrl;
            this.srs = srs;
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        try {
            final double zoomFactor = Double.valueOf(root.getChildText("zoomFactor"));

            this.zoomFactor = zoomFactor;
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        configure(parent);
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element root = new Element(getProtocolStepKey());

        final Element backgroundlayerElement = new Element("backgroundlayer");
        backgroundlayerElement.setAttribute(new Attribute("wmsMapUrl", wmsMapUrl));
        backgroundlayerElement.setAttribute(new Attribute("srs", srs));
        root.addContent(backgroundlayerElement);

        final Element zoomFactorElement = new Element("zoomFactor");
        zoomFactorElement.setText(Double.toString(zoomFactor));
        root.addContent(zoomFactorElement);

        return root;
    }
}
