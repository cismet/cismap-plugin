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

import java.awt.Component;

import javax.swing.JButton;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.MetaSearchHelper;

import de.cismet.tools.gui.historybutton.HistoryModel;
import de.cismet.tools.gui.historybutton.HistoryModelListener;
import de.cismet.tools.gui.historybutton.JHistoryButton;
import de.cismet.tools.gui.menu.CidsUiComponent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiComponent.class)
public class MetaSearchToolbarComponent implements CidsUiComponent {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JHistoryBackButton object.
     */
    public MetaSearchToolbarComponent() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getValue(final String key) {
        return "MetaSearchToolbarComponent";
    }

    @Override
    public Component getComponent() {
        final MetaSearchHelper helper = MetaSearchHelper.getInstance();

        if (!helper.isConfigured()) {
            final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();
            navigator.getCismapConfigurationManager().addConfigurable(helper);
            navigator.getCismapConfigurationManager().configure(helper);
        }

        final JButton button = helper.getCmdPluginSearch();
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        return button;
    }
}
