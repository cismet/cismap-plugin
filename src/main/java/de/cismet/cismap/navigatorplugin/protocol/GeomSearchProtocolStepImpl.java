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

import Sirius.navigator.search.CidsServerSearchMetaObjectNodeWrapper;
import Sirius.navigator.search.CidsServerSearchProtocolStepImpl;

import Sirius.server.middleware.types.MetaObjectNode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vividsolutions.jts.geom.Geometry;

import lombok.Getter;

import java.util.List;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

import de.cismet.commons.converter.ConversionException;

import de.cismet.commons.gui.protocol.ProtocolHandler;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class GeomSearchProtocolStepImpl extends CidsServerSearchProtocolStepImpl
        implements GeomSearchProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    public static ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "GeomSearchProtocolStep",
            "GeomSearchProtocolStep desc");

    private static GeomFromWktConverter GEOM_EWKT_CONVERTER = new GeomFromWktConverter();

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonProperty(required = true)
    protected String wkt;

    @Getter
    @JsonIgnore
    private final transient Geometry geometry;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeomSearchProtocolStep object.
     *
     * @param  wkt            DOCUMENT ME!
     * @param  searchResults  DOCUMENT ME!
     */
    public GeomSearchProtocolStepImpl(final String wkt,
            final List<CidsServerSearchMetaObjectNodeWrapper> searchResults) {
        super(searchResults);

        this.wkt = wkt;

        Geometry geometry = null;
        if (wkt != null) {
            try {
                geometry = GEOM_EWKT_CONVERTER.convertForward(wkt, CismapBroker.getInstance().getDefaultCrs());
            } catch (final ConversionException ex) {
            }
        }
//        try {
//            geometry = new WKTReader().read(wkt);
//        } catch (final ParseException ex) {
//            geometry = null;
//        }
        this.geometry = geometry;
    }

    /**
     * Creates a new GeomSearchProtocolStep object.
     *
     * @param  search       DOCUMENT ME!
     * @param  geometry     DOCUMENT ME!
     * @param  resultNodes  DOCUMENT ME!
     */
    public GeomSearchProtocolStepImpl(final CidsServerSearch search,
            final Geometry geometry,
            final List<MetaObjectNode> resultNodes) {
        super(search, resultNodes);

        this.geometry = geometry;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public GeomSearchProtocolStepConfiguration getConfiguration() {
        return (GeomSearchProtocolStepConfiguration)ProtocolHandler.getInstance()
                    .getProtocolStepConfiguration(META_INFO.getKey());
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
//            this.wkt = new WKTWriter().write(geometry);
        } else {
            this.wkt = null;
        }
    }
}
