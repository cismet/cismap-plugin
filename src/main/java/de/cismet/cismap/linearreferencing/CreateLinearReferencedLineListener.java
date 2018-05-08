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
import com.vividsolutions.jts.linearref.LengthIndexedLine;

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
public class CreateLinearReferencedLineListener extends CreateLinearReferencedMarksListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateLinearReferencedLineListener.class);
    public static final String CREATE_LINEAR_REFERENCED_LINE_MODE = "CREATE_NEW_STATION_LINE";

    //~ Instance fields --------------------------------------------------------

    private int counter = 0;
    private final CreateStationLineListener lineFinishedListener;
    private final MetaClass acceptedRoute;
    private float minDistance = 0;
    private float maxDistance = Float.MAX_VALUE;
    private String routeName = null;
    private boolean resumed = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateLinearReferencedLineListener object.
     *
     * @param  mc                        DOCUMENT ME!
     * @param  geometryFinishedListener  DOCUMENT ME!
     * @param  acceptedRoute             DOCUMENT ME!
     * @param  routeName                 DOCUMENT ME!
     * @param  minDistance               DOCUMENT ME!
     * @param  maxDistance               DOCUMENT ME!
     */
    public CreateLinearReferencedLineListener(final MappingComponent mc,
            final CreateStationLineListener geometryFinishedListener,
            final MetaClass acceptedRoute,
            final String routeName,
            final float minDistance,
            final float maxDistance) {
        this(mc, geometryFinishedListener, acceptedRoute, routeName, minDistance, maxDistance, null);
    }

    /**
     * Creates a new CreateLinearReferencedLineListener object.
     *
     * @param  mc                        DOCUMENT ME!
     * @param  geometryFinishedListener  DOCUMENT ME!
     * @param  acceptedRoute             DOCUMENT ME!
     * @param  routeName                 DOCUMENT ME!
     * @param  minDistance               DOCUMENT ME!
     * @param  maxDistance               DOCUMENT ME!
     * @param  check                     DOCUMENT ME!
     */
    public CreateLinearReferencedLineListener(final MappingComponent mc,
            final CreateStationLineListener geometryFinishedListener,
            final MetaClass acceptedRoute,
            final String routeName,
            final float minDistance,
            final float maxDistance,
            final StationCreationCheck check) {
        super(mc);
        mcModus = CREATE_LINEAR_REFERENCED_LINE_MODE;
        this.lineFinishedListener = geometryFinishedListener;
        this.acceptedRoute = acceptedRoute;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.routeName = routeName;
        final PFeature lineFeature = getSelectedLinePFeature();

        if (lineFeature == null) {
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(mc),
                "Sie müssen genau ein "
                        + routeName
                        + " wählen.",
                "Fehler Thema-/Gewässerwahl",
                JOptionPane.WARNING_MESSAGE);
            lineFinishedListener.lineFinished(null, null, null, null, 0, 0);
        } else {
            if ((check != null) && !check.isRouteValid(lineFeature)) {
                lineFinishedListener.lineFinished(null, null, null, null, 0, 0);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final PInputEvent event) {
        if (event.isLeftMouseButton()) {
            if (counter == 0) {
                super.mouseClicked(event);
            } else if (counter == 1) {
                if ((Math.abs(getCurrentPosition() - getMarkPositionsOfSelectedFeature()[0]) > minDistance)
                            && (Math.abs(getCurrentPosition() - getMarkPositionsOfSelectedFeature()[0]) <= maxDistance)) {
                    super.mouseClicked(event);
                } else {
                    return;
                }
            }
            ++counter;

            if (counter == 2) {
                final Double[] pos = getMarkPositionsOfSelectedFeature();

                if ((pos != null) && (pos.length == 2)) {
                    try {
                        final Geometry route = getSelectedLinePFeature().getFeature().getGeometry();
                        final Geometry point1 = LinearReferencedPointFeature.getPointOnLine(pos[0], route);
                        final Geometry point2 = LinearReferencedPointFeature.getPointOnLine(pos[1], route);

                        final LengthIndexedLine lil = new LengthIndexedLine(route);
                        final Geometry g = lil.extractLine(lil.indexOf(point1.getCoordinate()),
                                lil.indexOf(point2.getCoordinate()));

                        final CidsLayerFeature feature = (CidsLayerFeature)getSelectedLinePFeature().getFeature();
                        lineFinishedListener.lineFinished(feature.getBean(), g, point1, point2, pos[0], pos[1]);
                    } finally {
                        counter = 0;
                        removeAllMarks();
                    }
                } else if (pos.length > 2) {
                    counter = 0;
                    removeAllMarks();
                } else {
                    // the last click was not valid. So restore the counter
                    --counter;
                }
            } else {
                final Double[] pos = getMarkPositionsOfSelectedFeature();

                if (pos.length != counter) {
                    counter = 0;
                    removeAllMarks();
                }
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
        final PFeature line = getSelectedLinePFeature(mc, acceptedRoute);

        if ((line == null) && resumed) {
            resumed = false;
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(mc),
                "Sie müssen genau ein "
                        + routeName
                        + " wählen.",
                "Fehler Thema-/Gewässerwahl",
                JOptionPane.WARNING_MESSAGE);
            lineFinishedListener.lineFinished(null, null, null, null, 0, 0);
        }

        return line;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc             DOCUMENT ME!
     * @param   acceptedRoute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PFeature getSelectedLinePFeature(final MappingComponent mc, final MetaClass acceptedRoute) {
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

        return null;
    }

    /**
     * DOCUMENT ME!
     */
    public void resumed() {
        resumed = true;
    }
}
