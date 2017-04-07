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

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.CismapPlugin;
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
public class ChangeScaleAction extends AbstractAction implements CidsUiAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ChangeScaleAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public ChangeScaleAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ChangeScaleAction.class,
                "ChangeScaleAction.toolTipText");
        final String name = org.openide.util.NbBundle.getMessage(ChangeScaleAction.class,
                "ChangeScaleAction.name");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(
            ACCELERATOR_KEY,
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_M,
                java.awt.event.InputEvent.CTRL_MASK));
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/scale.png")));
        putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/scale.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "ChangeScaleAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (mapC != null) {
            try {
                final String s = JOptionPane.showInputDialog(
                        StaticSwingTools.getParentFrame(mapC),
                        org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.scaleManually"),
                        ((int)mapC.getScaleDenominator())
                                + "");                               // NOI18N
                final Integer i = new Integer(s);
                mapC.gotoBoundingBoxWithHistory(mapC.getBoundingBoxFromScale(i));
            } catch (Exception skip) {
                LOG.error("Error in mniScaleActionPerformed", skip); // NOI18N
            }
        }
    }
}
