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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.cismet.cismap.commons.gui.piccolo.AngleMeasurementDialog;

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
public class AngleMeasurementAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public AngleMeasurementAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                AngleMeasurementAction.class,
                "AngleMeasurementAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(
                AngleMeasurementAction.class,
                "AngleMeasurementAction.name");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(
            ACCELERATOR_KEY,
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_L,
                java.awt.event.InputEvent.CTRL_MASK));
        putValue(
            SMALL_ICON,
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/Angle-Thingy-icon_16.png")));
        putValue(
            LARGE_ICON_KEY,
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/Angle-Thingy-icon_16.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "AngleMeasurementAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        StaticSwingTools.showDialog(AngleMeasurementDialog.getInstance());
    }
}
