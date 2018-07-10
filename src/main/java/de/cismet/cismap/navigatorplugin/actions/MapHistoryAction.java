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

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.historybutton.HistoryModelListener;
import de.cismet.tools.gui.menu.CidsUiMenuProvider;
import de.cismet.tools.gui.menu.CidsUiMenuProviderEvent;
import de.cismet.tools.gui.menu.CidsUiMenuProviderListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiMenuProvider.class)
public class MapHistoryAction extends AbstractAction implements CidsUiMenuProvider, HistoryModelListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final javax.swing.ImageIcon ICON_BACK = new javax.swing.ImageIcon(MapHistoryAction.class.getResource(
                "/images/miniBack.png"));                 // NOI18N
    private static final javax.swing.ImageIcon ICON_FORWARD = new javax.swing.ImageIcon(MapHistoryAction.class
                    .getResource(
                        "/images/miniForward.png"));      // NOI18N
    private static final javax.swing.ImageIcon ICON_CURRENT = new javax.swing.ImageIcon(MapHistoryAction.class
                    .getResource("/images/current.png")); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final JMenu menu = new JMenu();
    private final List<JMenuItem> itemList = new ArrayList<JMenuItem>();

    private final List<CidsUiMenuProviderListener> listeners = new ArrayList<CidsUiMenuProviderListener>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HomeAction object.
     */
    public MapHistoryAction() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
    }

    @Override
    public String getMenuKey() {
        return "mapHistory";
    }

    @Override
    public JMenu getMenu() {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().addHistoryModelListener(this);
        }
        return menu;
    }

    @Override
    public void historyChanged() {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
        final List backPos = mapC.getBackPossibilities();
        final List forwPos = mapC.getForwardPossibilities();
        CidsUiMenuProviderEvent e;

        if (mapC.getCurrentElement() != null) {
            while (itemList.size() > 0) {
                final JMenuItem item = itemList.get(0);
                e = new CidsUiMenuProviderEvent(item, 0, this);
                itemList.remove(item);
                fireMenuItemRemoved(e);
            }
            int counter = 0;

            int start = 0;

            if ((backPos.size() - 10) > 0) {
                start = backPos.size() - 10;
            }
            int itemPosition = 0;

            for (int index = start; index < backPos.size(); ++index) {
                final Object elem = backPos.get(index);
                final JMenuItem item = new JMenuItem(elem.toString());

                item.setIcon(ICON_BACK);

                final int pos = backPos.size() - 1 - index;
                item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            for (int i = 0; i < pos; ++i) {
                                mapC.back(false);
                            }

                            mapC.back(true);
                        }
                    });
                itemList.add(item);
                e = new CidsUiMenuProviderEvent(item, itemPosition++, this);
                fireMenuItemAdded(e);
            }

            final JMenuItem currentItem = new JMenuItem(mapC.getCurrentElement().toString());
            currentItem.setEnabled(false);
            currentItem.setIcon(ICON_CURRENT);
            itemList.add(currentItem);
            e = new CidsUiMenuProviderEvent(currentItem, itemPosition++, this);
            fireMenuItemAdded(e);
            counter = 0;

            for (int index = forwPos.size() - 1; index >= 0; --index) {
                final Object elem = forwPos.get(index);
                final JMenuItem item = new JMenuItem(elem.toString()); // +":"+new Integer(forwPos.size()-1-index));

                item.setIcon(ICON_FORWARD);

                final int pos = forwPos.size() - 1 - index;
                item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            for (int i = 0; i < pos; ++i) {
                                mapC.forward(false);
                            }

                            mapC.forward(true);
                        }
                    });

                itemList.add(item);
                e = new CidsUiMenuProviderEvent(item, itemPosition++, this);
                fireMenuItemAdded(e);

                if (counter++ > 10) {
                    break;
                }
            }
        }
    }

    @Override
    public void backStatusChanged() {
    }

    @Override
    public void forwardStatusChanged() {
    }

    @Override
    public void historyActionPerformed() {
    }

    @Override
    public void addCidsUiMenuProviderListener(final CidsUiMenuProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCidsUiMenuProviderListener(final CidsUiMenuProviderListener listener) {
        listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    private void fireMenuItemAdded(final CidsUiMenuProviderEvent e) {
        for (final CidsUiMenuProviderListener listener : listeners) {
            listener.menuItemAdded(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    private void fireMenuItemRemoved(final CidsUiMenuProviderEvent e) {
        for (final CidsUiMenuProviderListener listener : listeners) {
            listener.menuItemRemoved(e);
        }
    }
}
