/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vividsolutions.jts.geom.Geometry;

import lombok.Getter;

import de.cismet.commons.gui.protocol.AbstractProtocolStep;
import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;
import de.cismet.commons.gui.protocol.ProtocolStepMetaInfo;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class GeoContextProtocolStepImpl extends AbstractProtocolStep implements GeoContextProtocolStep {

    //~ Static fields/initializers ---------------------------------------------

    public static final ProtocolStepMetaInfo META_INFO = new ProtocolStepMetaInfo(
            "GeoContext",
            "GeoContextProtocolStep");

    //~ Instance fields --------------------------------------------------------

    @Getter
    @JsonProperty(required = true)
    protected String wkt;

    @Getter
    @JsonIgnore
    private final GeometryProtocolStepImpl geometryProtocolStep;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoContextProtocolStepImpl object.
     *
     * @param  geometry  DOCUMENT ME!
     */
    public GeoContextProtocolStepImpl(final Geometry geometry) {
        this.geometryProtocolStep = new GeometryProtocolStepImpl(geometry);
    }

    /**
     * Creates a new GeoContextProtocolStepImpl object.
     *
     * @param  wkt  DOCUMENT ME!
     */
    @JsonCreator
    public GeoContextProtocolStepImpl(@JsonProperty("wkt") final String wkt) {
        this.geometryProtocolStep = new GeometryProtocolStepImpl(wkt);

        this.wkt = getGeometryProtocolStep().getWkt();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initParameters() {
        super.initParameters();
        geometryProtocolStep.initParameters();

        this.wkt = getGeometryProtocolStep().getWkt();
    }

    @Override
    protected ProtocolStepMetaInfo createMetaInfo() {
        return META_INFO;
    }

    @Override
    public AbstractProtocolStepPanel visualize() {
        return new GeoContextProtocolStepPanel(this);
    }

    @Override
    public Geometry getGeometry() {
        return geometryProtocolStep.getGeometry();
    }
}
