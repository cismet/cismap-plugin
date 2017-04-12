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
import javax.swing.JButton;
import javax.swing.JToggleButton;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.menu.CidsUiAction;

import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class SnapAction extends AbstractAction implements CidsUiAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public SnapAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(SnapAction.class,
                "SnapAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/snap.png")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/snap.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "SnapAction");

//        cmdSnap.setMaximumSize(new java.awt.Dimension(29, 29));
//        cmdSnap.setMinimumSize(new java.awt.Dimension(29, 29));
//        cmdSnap.setPreferredSize(new java.awt.Dimension(29, 29));
//        cmdSnap.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap_selected.png"))); // NOI18N
//        cmdSnap.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap_selected.png")));         // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            if (e.getSource() instanceof JToggleButton) {
                final JToggleButton button = (JToggleButton)e.getSource();
                CismapBroker.getInstance().getMappingComponent().setSnappingEnabled(button.isSelected());
            }
        }
    }
}
