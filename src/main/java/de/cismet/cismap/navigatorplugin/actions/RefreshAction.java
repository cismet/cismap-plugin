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
public class RefreshAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public RefreshAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                RefreshAction.class,
                "RefreshAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(
                RefreshAction.class,
                "RefreshAction.name");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/reload16.gif")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/reload.gif")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "RefreshAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().refresh();
        }
    }
}
