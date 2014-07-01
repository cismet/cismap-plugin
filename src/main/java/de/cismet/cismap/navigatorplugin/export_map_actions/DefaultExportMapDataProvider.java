/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.export_map_actions;

import Sirius.navigator.plugin.PluginRegistry;

import java.awt.Component;

import javax.swing.Action;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class DefaultExportMapDataProvider implements ExportMapDataProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getDpi() {
        return 72;
    }

    @Override
    public void setLastUsedAction(final Action action) {
        // do nothing
    }

    @Override
    public boolean isGenerateWorldFile() {
        return false;
    }

    @Override
    public Component getComponent() {
        return CismapBroker.getInstance().getMappingComponent();
    }

    @Override
    public ExportMapFileTypes getFileType() {
        return ExportMapFileTypes.JPEG;
    }

    @Override
    public int getHttpInterfacePort() {
        final CismapPlugin cismapPlugin = (CismapPlugin)PluginRegistry.getRegistry().getPlugin("cismap");
        return cismapPlugin.getHttpInterfacePort();
    }
}
