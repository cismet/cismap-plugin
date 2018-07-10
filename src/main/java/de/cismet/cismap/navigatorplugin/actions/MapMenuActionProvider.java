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

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.menu.CidsUiAction;
import de.cismet.tools.gui.menu.CidsUiActionProvider;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiActionProvider.class)
public class MapMenuActionProvider implements CidsUiActionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MapMenuActionProvider.class);
    private static String cismapDirectory;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigurationActionProvider object.
     */
    public MapMenuActionProvider() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cismapDirectory  DOCUMENT ME!
     */
    public static void setCismapDirectory(final String cismapDirectory) {
        MapMenuActionProvider.cismapDirectory = cismapDirectory;
    }

    @Override
    public List<CidsUiAction> getActions() {
        final List<CidsUiAction> actionList = new ArrayList<CidsUiAction>();

        actionList.add(new RemoveAllObjectsAction());
        actionList.add(new RemoveSelectedObjectAction());
        actionList.add(new ZoomToAllAction());
        actionList.add(new ZoomToSelectionAction());

        return actionList;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class ZoomToSelectionAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public ZoomToSelectionAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(
                SMALL_ICON,
                new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToSelection.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.ZoomToSelectionAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.ZoomToSelectionAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.zoom.selection");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.zoomToSelection();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class ZoomToAllAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public ZoomToAllAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(
                SMALL_ICON,
                new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToAll.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.ZoomToAllAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.ZoomToAllAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.zoom.all");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.zoomToFeatureCollection();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class RemoveSelectedObjectAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public RemoveSelectedObjectAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(
                SMALL_ICON,
                new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/removerow.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.RemoveSelectedObjectAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.RemoveSelectedObjectAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.remove.selection");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                final List v = new ArrayList(mapC.getFeatureCollection().getSelectedFeatures());
                mapC.getFeatureCollection().removeFeatures(v);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class RemoveAllObjectsAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public RemoveAllObjectsAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(
                SMALL_ICON,
                new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/removeAll.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.RemoveAllObjectsAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapMenuActionProvider.class,
                    "MapMenuActionProvider.RemoveAllObjectsAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.remove.all");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                final List v = new ArrayList(mapC.getFeatureCollection().getAllFeatures());
                mapC.getFeatureCollection().removeFeatures(v);
            }
        }
    }
}
