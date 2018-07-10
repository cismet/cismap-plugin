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
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.menu.CidsUiMenuProvider;
import de.cismet.tools.gui.menu.CidsUiMenuProviderListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiMenuProvider.class)
public class ScaleMenuProvider implements CidsUiMenuProvider {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JHistoryBackButton object.
     */
    public ScaleMenuProvider() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public JMenu getMenu() {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
        final JMenu menu = new JMenu();

        if (mapC.getScales() != null) {
            for (final Scale s : mapC.getScales()) {
                if (s.getDenominator() > 0) {
                    menu.add(getScaleMenuItem(s.getText(), s.getDenominator(), mapC));
                }
            }
        }

        return menu;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   t     DOCUMENT ME!
     * @param   d     DOCUMENT ME!
     * @param   mapC  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JMenuItem getScaleMenuItem(final String t, final int d, final MappingComponent mapC) {
        final JMenuItem jmi = new JMenuItem(t);
        jmi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    mapC.gotoBoundingBoxWithHistory(mapC.getBoundingBoxFromScale(d));
                }
            });

        return jmi;
    }

    @Override
    public String getMenuKey() {
        return "ScaleMenu";
    }

    @Override
    public void addCidsUiMenuProviderListener(final CidsUiMenuProviderListener listener) {
    }

    @Override
    public void removeCidsUiMenuProviderListener(final CidsUiMenuProviderListener listener) {
    }
}
