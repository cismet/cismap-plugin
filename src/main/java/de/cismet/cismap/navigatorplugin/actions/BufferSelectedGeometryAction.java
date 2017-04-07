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
package de.cismet.cismap.navigatorplugin.actions;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.menu.CidsUiAction;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class BufferSelectedGeometryAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public BufferSelectedGeometryAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                BufferSelectedGeometryAction.class,
                "BufferSelectedGeometryAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(
                BufferSelectedGeometryAction.class,
                "BufferSelectedGeometryAction.name");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(
            ACCELERATOR_KEY,
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_B,
                java.awt.event.InputEvent.CTRL_MASK));
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "BufferSelectedGeometryAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (mapC != null) {
            final Collection c = mapC.getFeatureCollection().getSelectedFeatures();
            if ((c != null) && (c.size() > 0)) {
                final String s = (String)JOptionPane.showInputDialog(
                        StaticSwingTools.getParentFrame(mapC),
                        org.openide.util.NbBundle.getMessage(
                            BufferSelectedGeometryAction.class,
                            "BufferSelectedGeometryAction.Dialog.text"),  // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            BufferSelectedGeometryAction.class,
                            "BufferSelectedGeometryAction.Dialog.title"), // NOI18N
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "");                                              // NOI18N

                for (final Object o : c) {
                    if (o instanceof Feature) {
                        final int srid = ((Feature)o).getGeometry().getSRID();
                        final Geometry oldG = CrsTransformer.transformToMetricCrs(((Feature)o).getGeometry());
                        Geometry newG = oldG.buffer(Double.parseDouble(s));
                        newG = CrsTransformer.transformToGivenCrs(newG, CrsTransformer.createCrsFromSrid(srid));

                        if (o instanceof PureNewFeature) {
                            ((Feature)o).setGeometry(newG);
                            ((PureNewFeature)o).setGeometryType(PureNewFeature.geomTypes.POLYGON);
                            final PFeature sel = (PFeature)mapC.getPFeatureHM().get(o);

                            // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                            // setzen
                            sel.setCoordArr(newG.getCoordinates());

                            // refresh
                            sel.syncGeometry();

                            final List v = new ArrayList();
                            v.add(sel.getFeature());
                            ((DefaultFeatureCollection)mapC.getFeatureCollection()).fireFeaturesChanged(v);
                        } else {
                            final PureNewFeature pnf = new PureNewFeature(newG);
                            pnf.setGeometryType(PureNewFeature.geomTypes.POLYGON);
                            ((DefaultFeatureCollection)mapC.getFeatureCollection()).addFeature(pnf);
                            ((DefaultFeatureCollection)mapC.getFeatureCollection()).holdFeature(pnf);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                    StaticSwingTools.getParentFrame(mapC),
                    org.openide.util.NbBundle.getMessage(
                        BufferSelectedGeometryAction.class,
                        "BufferSelectedGeometryAction.Dialog.noneselected"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        BufferSelectedGeometryAction.class,
                        "BufferSelectedGeometryAction.Dialog.title"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
