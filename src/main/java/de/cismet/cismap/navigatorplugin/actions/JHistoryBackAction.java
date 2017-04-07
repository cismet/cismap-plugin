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

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.historybutton.HistoryModelListener;
import de.cismet.tools.gui.menu.CidsUiAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class JHistoryBackAction extends AbstractAction implements CidsUiAction, HistoryModelListener {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JHistoryBackButton object.
     */
    public JHistoryBackAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                HomeAction.class,
                "JHistoryBackAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(
                HomeAction.class,
                "JHistoryBackAction.name");
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(NAME, name);
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/back16.png")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/back.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "JHistoryBackAction");

        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().addHistoryModelListener(this);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void historyActionPerformed() {
//        if (CismapBroker.getInstance().getMappingComponent() != null) {
//            CismapBroker.getInstance().getMappingComponent().back(true);
//        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().back(true);
        }
    }

    @Override
    public void backStatusChanged() {
        this.setEnabled(CismapBroker.getInstance().getMappingComponent().isBackPossible());
    }

    @Override
    public void forwardStatusChanged() {
    }

    @Override
    public void historyChanged() {
    }
}
