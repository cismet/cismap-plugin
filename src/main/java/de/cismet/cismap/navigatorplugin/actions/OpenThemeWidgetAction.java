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

import Sirius.navigator.NavigatorX;
import Sirius.navigator.plugin.PluginRegistry;
import Sirius.navigator.ui.ComponentRegistry;

import net.infonode.docking.View;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

import de.cismet.tools.gui.menu.CidsUiAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class OpenThemeWidgetAction extends AbstractAction implements CidsUiAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OpenThemeWidgetAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new OpenFeatureInfoAction object.
     */
    public OpenThemeWidgetAction() {
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                OpenThemeWidgetAction.class,
                "OpenThemeWidgetAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                OpenThemeWidgetAction.class,
                "OpenThemeWidgetAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                OpenThemeWidgetAction.class,
                "OpenThemeWidgetAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(CidsUiAction.CIDS_ACTION_KEY, "OpenThemeWidgetAction");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final CismapPlugin cismap = (CismapPlugin)PluginRegistry.getRegistry().getPlugin("cismap");

        if (cismap != null) {
            final View v = cismap.getView("themelayer");

            if (v.isClosable()) {
                v.close();
            } else {
                v.restore();
            }
        } else {
            final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();
            navigator.select("de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget");
        }
    }
}
