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

import edu.umd.cs.piccolo.event.PInputEvent;

import org.apache.log4j.Logger;

import java.awt.Component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

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
public class CreateLinearReferencedLineListener extends CreateLinearReferencedMarksListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateLinearReferencedLineListener.class);
    public static final String CREATE_LINEAR_REFERENCED_LINE_MODE = "CREATE_NEW_STATION_LINE";

    //~ Instance fields --------------------------------------------------------

    private int counter = 0;
    private CreateStationLineListener lineFinishedListener;
    private MetaClass acceptedRoute;
    private float minDistance = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateLinearReferencedLineListener object.
     *
     * @param  mc                        DOCUMENT ME!
     * @param  geometryFinishedListener  DOCUMENT ME!
     * @param  acceptedRoute             DOCUMENT ME!
     * @param  minDistance               DOCUMENT ME!
     */
    public CreateLinearReferencedLineListener(final MappingComponent mc,
            final CreateStationLineListener geometryFinishedListener,
            final MetaClass acceptedRoute,
            final float minDistance) {
        super(mc);
        mcModus = CREATE_LINEAR_REFERENCED_LINE_MODE;
        this.lineFinishedListener = geometryFinishedListener;
        this.acceptedRoute = acceptedRoute;
        this.minDistance = minDistance;
        if (getSelectedLinePFeature() == null) {
            JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(mc),
                "Bevor Sie ein neues Objekt anlegen können, \nmüssen Sie ein Objekt vom Typ "
                        + acceptedRoute.getName()
                        + " markieren.",
                "Keine Route markiert",
                JOptionPane.WARNING_MESSAGE);
            lineFinishedListener.lineFinished(null, null, null, null, 0, 0);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final PInputEvent event) {
        if (event.isLeftMouseButton()) {
            if (counter == 0) {
                super.mouseClicked(event);
            } else if (counter == 1) {
                if (Math.abs(getCurrentPosition() - getMarkPositionsOfSelectedFeature()[0]) > minDistance) {
                    super.mouseClicked(event);
                } else {
                    return;
                }
            }
            ++counter;

            if (counter == 2) {
                final Double[] pos = getMarkPositionsOfSelectedFeature();

                if ((pos != null) && (pos.length == 2)) {
                    final Geometry route = getSelectedLinePFeature().getFeature().getGeometry();
                    final Geometry point1 = LinearReferencedPointFeature.getPointOnLine(pos[0], route);
                    final Geometry point2 = LinearReferencedPointFeature.getPointOnLine(pos[1], route);

                    LengthIndexedLine lil = new LengthIndexedLine(route);
                    Geometry g = lil.extractLine(lil.indexOf(point1.getCoordinate()), lil.indexOf(point2.getCoordinate()));

                    final CidsLayerFeature feature = (CidsLayerFeature)getSelectedLinePFeature().getFeature();
                    lineFinishedListener.lineFinished(feature.getBean(), g, point1, point2, pos[0], pos[1]);
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
