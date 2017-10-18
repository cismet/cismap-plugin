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

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.StationCreationCheck;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateLinearReferencedMarksListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

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
    private String routeName = null;
    private boolean resumed = false;

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
        this.routeName = routeName;

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
        final List<Feature> fl = SelectionManager.getInstance().getSelectedFeatures();
        final List<Feature> acceptedFeatures = new ArrayList<Feature>();

        for (final Feature feature : fl) {
            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;

                if (cidsFeature.getBean().getMetaObject().getMetaClass().equals(acceptedRoute)) {
                    acceptedFeatures.add(feature);
                }
            }
        }

        if (acceptedFeatures.size() == 1) {
            final Geometry geom = acceptedFeatures.get(0).getGeometry();

            if ((geom != null) || (geom instanceof MultiLineString) || (geom instanceof LineString)) {
                final Feature feature = acceptedFeatures.get(0);
                PFeature f = CismapBroker.getInstance().getMappingComponent().getPFeatureHM().get(feature);

                if ((f == null) && (feature instanceof FeatureServiceFeature)) {
                    final LayerProperties lp = ((FeatureServiceFeature)feature).getLayerProperties();

                    if ((lp != null) && (lp.getFeatureService() != null)
                                && (lp.getFeatureService().getPNode() != null)) {
                        final PNode node = lp.getFeatureService().getPNode();

                        for (int i = 0; i < node.getChildrenCount(); ++i) {
                            if (node.getChild(i) instanceof PFeature) {
                                final Object pfeature = node.getChild(i);

                                if ((pfeature instanceof PFeature)
                                            && ((PFeature)pfeature).getFeature().equals(feature)) {
                                    f = (PFeature)pfeature;
                                    break;
                                }
                            }
                        }
                    }
                }

                return f;
            }
        }

        if (resumed) {
            resumed = false;
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(mc),
                "Sie müssen genau ein "
                        + routeName
                        + " wählen.",
                "Fehler Thema-/Gewässerwahl",
                JOptionPane.WARNING_MESSAGE);
            pointFinishedListener.pointFinished(null, null, 0);
        }

        return null;
    }
    /**
     * DOCUMENT ME!
     */
    public void resumed() {
        resumed = true;
    }
}
