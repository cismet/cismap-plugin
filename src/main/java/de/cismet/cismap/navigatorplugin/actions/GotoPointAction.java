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

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.GotoPointDialog;

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
public class GotoPointAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public GotoPointAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                GotoPointAction.class,
                "GotoPointAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(GotoPointAction.class,
                "GotoPointAction.name");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(
            ACCELERATOR_KEY,
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_G,
                java.awt.event.InputEvent.CTRL_MASK));
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/goto.png")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/goto.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "GotoPointAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (mapC != null) {
            final GotoPointDialog gotoPointDialog = GotoPointDialog.getInstance();
            StaticSwingTools.showDialog(mapC, gotoPointDialog, true);
        }
    }
}
