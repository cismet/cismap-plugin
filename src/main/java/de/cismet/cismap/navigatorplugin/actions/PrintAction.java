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

import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

import de.cismet.tools.gui.menu.CidsUiAction;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class PrintAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrintAction object.
     */
    public PrintAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                PrintAction.class,
                "PrintAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/frameprint.png")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/frameprint.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "PrintAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
        if (mapC != null) {
            mapC.showPrintingSettingsDialog();
        }
    }
}
