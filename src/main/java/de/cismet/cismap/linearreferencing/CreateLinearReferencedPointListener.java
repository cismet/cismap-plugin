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
package de.cismet.cismap.linearreferencing;

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import edu.umd.cs.piccolo.event.PInputEvent;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.StationCreationCheck;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateLinearReferencedMarksListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CreateLinearReferencedPointListener extends CreateLinearReferencedMarksListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateLinearReferencedPointListener.class);
    public static final String CREATE_LINEAR_REFERENCED_POINT_MODE = "CREATE_NEW_STATION";

    //~ Instance fields --------------------------------------------------------

    private final CreateStationListener pointFinishedListener;
    private final MetaClass acceptedRoute;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateLinearReferencedLineListener object.
     *
     * @param  mc                     DOCUMENT ME!
     * @param  pointFinishedListener  DOCUMENT ME!
     * @param  acceptedRoute          DOCUMENT ME!
     * @param  routeName              DOCUMENT ME!
     * @param  check                  DOCUMENT ME!
     */
    public CreateLinearReferencedPointListener(final MappingComponent mc,
            final CreateStationListener pointFinishedListener,
            final MetaClass acceptedRoute,
            final String routeName,
            final StationCreationCheck check) {
        super(mc);
        mcModus = CREATE_LINEAR_REFERENCED_POINT_MODE;
        this.pointFinishedListener = pointFinishedListener;
        this.acceptedRoute = acceptedRoute;

        if (getSelectedLinePFeature() == null) {
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(mc),
                "Sie müssen genau ein "
                        + routeName
                        + " wählen.",
                "Fehler Thema-/Gewässerwahl",
                JOptionPane.WARNING_MESSAGE);
            pointFinishedListener.pointFinished(null, null, 0);
        } else {
            if ((check != null) && !check.isRouteValid(getSelectedLinePFeature())) {
                pointFinishedListener.pointFinished(null, null, 0);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);

        if (event.isLeftMouseButton()) {
            final Double[] pos = getMarkPositionsOfSelectedFeature();

            if ((pos != null) && (pos.length == 1)) {
                final Geometry route = getSelectedLinePFeature().getFeature().getGeometry();
                final Geometry point1 = LinearReferencedPointFeature.getPointOnLine(pos[0], route);

                final CidsLayerFeature feature = (CidsLayerFeature)getSelectedLinePFeature().getFeature();
                pointFinishedListener.pointFinished(feature.getBean(), point1, pos[0]);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public PFeature getSelectedLinePFeature() {
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> fl = sl.getAllSelectedPFeatures();
        final List<PFeature> acceptedFeatures = new ArrayList<PFeature>();

        for (final PFeature f : fl) {
            final Feature feature = f.getFeature();

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;

                if (cidsFeature.getBean().getMetaObject().getMetaClass().equals(acceptedRoute)) {
                    acceptedFeatures.add(f);
                }
            }
        }

        if (acceptedFeatures.size() == 1) {
            final Geometry geom = acceptedFeatures.get(0).getFeature().getGeometry();

            if ((geom != null) || (geom instanceof MultiLineString) || (geom instanceof LineString)) {
                return acceptedFeatures.get(0);
            }
        }

        return null;
    }
}
