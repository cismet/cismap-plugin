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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vividsolutions.jts.geom.Geometry;

import lombok.Getter;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

import de.cismet.commons.converter.ConversionException;

import de.cismet.commons.gui.protocol.AbstractProtocolStep;
import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolHandler;
import de.cismet.commons.gui.protocol.ProtocolStepConfiguration;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class GeometryProtocolStepImpl extends AbstractProtocolStep implements GeometryProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    public static ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "Geometry",
            "GeometryProtocolStep");

    private static GeomFromWktConverter GEOM_EWKT_CONVERTER = new GeomFromWktConverter();

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonProperty(required = true)
    protected String wkt;

    @Getter @JsonIgnore private final transient Geometry geometry;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeomSearchProtocolStep object.
     *
     * @param  wkt  DOCUMENT ME!
     */
    public GeometryProtocolStepImpl(final String wkt) {
        this.wkt = wkt;

        Geometry geometry = null;
        if (wkt != null) {
            try {
                geometry = GEOM_EWKT_CONVERTER.convertForward(wkt, CismapBroker.getInstance().getDefaultCrs());
            } catch (final ConversionException ex) {
            }
        }
        this.geometry = geometry;
    }

    /**
     * Creates a new GeomSearchProtocolStep object.
     *
     * @param  geometry  DOCUMENT ME!
     */
    public GeometryProtocolStepImpl(final Geometry geometry) {
        this.geometry = geometry;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public GeometryProtocolStepConfiguration getConfiguration() {
        return (GeometryProtocolStepConfiguration)ProtocolHandler.getInstance()
                    .getProtocolStepConfiguration(GeometryProtocolStepConfiguration.PROTOCOL_STEP_KEY);
    }

    @Override
    protected ProtocolStepMetaInfo createMetaInfo() {
        return META_INFO;
    }

    @Override
    public void initParameters() {
        super.initParameters();

        if (geometry != null) {
            String wkt = null;
            try {
                wkt = GEOM_EWKT_CONVERTER.convertBackward(geometry);
            } catch (ConversionException ex) {
            }
            this.wkt = wkt;
        } else {
            this.wkt = null;
        }
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new GeometryProtocolStepPanel(this);
    }
}
