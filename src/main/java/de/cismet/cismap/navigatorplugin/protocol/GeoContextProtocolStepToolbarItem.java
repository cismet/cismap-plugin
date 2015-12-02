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

import com.vividsolutions.jts.geom.Geometry;

import org.openide.util.lookup.ServiceProvider;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.gui.protocol.AbstractProtocolStepToolbarItemAction;
import de.cismet.commons.gui.protocol.ProtocolHandler;
import de.cismet.commons.gui.protocol.ProtocolStepToolbarItem;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = ProtocolStepToolbarItem.class)
public class GeoContextProtocolStepToolbarItem extends AbstractProtocolStepToolbarItemAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final String NAME = "";
    private static final String TOOLTIP = org.openide.util.NbBundle.getMessage(
            GeoContextProtocolStepPanel.class,
            "GeoContextProtocolStepToolbarItem.tooltip");
    private static final ImageIcon ICON = new ImageIcon(GeoContextProtocolStepToolbarItem.class.getResource(
                "/de/cismet/cismap/navigatorplugin/protocol/GeoContext.png"));

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CommentProtocolStepToolbarItem object.
     */
    public GeoContextProtocolStepToolbarItem() {
        super(NAME, TOOLTIP, null, ICON);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getSorterString() {
        return "ZZZ";
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Geometry geometry =
            ((XBoundingBox)CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera())
                    .getGeometry();
        ProtocolHandler.getInstance().recordStep(new GeoContextProtocolStepImpl(geometry), false);
    }
}
