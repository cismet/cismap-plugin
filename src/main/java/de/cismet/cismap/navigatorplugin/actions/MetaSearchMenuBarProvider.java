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
import Sirius.navigator.ui.ComponentRegistry;

import javax.swing.JMenu;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.MetaSearchHelper;

import de.cismet.tools.gui.menu.CidsUiMenuProvider;
import de.cismet.tools.gui.menu.CidsUiMenuProviderListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiMenuProvider.class)
public class MetaSearchMenuBarProvider implements CidsUiMenuProvider {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JHistoryBackButton object.
     */
    public MetaSearchMenuBarProvider() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public JMenu getMenu() {
        final MetaSearchHelper helper = MetaSearchHelper.getInstance();

        if (!helper.isConfigured()) {
            final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();
            navigator.getCismapConfigurationManager().addConfigurable(helper);
            navigator.getCismapConfigurationManager().configure(helper);
        }

        return helper.getMenSearch();
    }

    @Override
    public String getMenuKey() {
        return "MetaSearchMenu";
    }

    @Override
    public void addCidsUiMenuProviderListener(CidsUiMenuProviderListener listener) {
    }

    @Override
    public void removeCidsUiMenuProviderListener(CidsUiMenuProviderListener listener) {
    }
}
