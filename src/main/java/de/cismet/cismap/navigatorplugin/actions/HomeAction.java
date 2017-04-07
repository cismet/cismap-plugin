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
public class HomeAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HomeAction object.
     */
    public HomeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                HomeAction.class,
                "HomeAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(
                HomeAction.class,
                "HomeAction.name");
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(NAME, name);
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/home16.gif")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/home.gif")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "HomeAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().gotoInitialBoundingBox();
        }
    }
}
