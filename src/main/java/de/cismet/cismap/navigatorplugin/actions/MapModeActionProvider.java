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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.menu.CidsUiAction;
import de.cismet.tools.gui.menu.CidsUiActionProvider;

import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.SELECTED_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiActionProvider.class)
public class MapModeActionProvider implements CidsUiActionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MapModeActionProvider.class);
    private static String cismapDirectory;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigurationActionProvider object.
     */
    public MapModeActionProvider() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cismapDirectory  DOCUMENT ME!
     */
    public static void setCismapDirectory(final String cismapDirectory) {
        MapModeActionProvider.cismapDirectory = cismapDirectory;
    }

    @Override
    public List<CidsUiAction> getActions() {
        final List<CidsUiAction> actionList = new ArrayList<CidsUiAction>();

        actionList.add(new FeatureInfoAction());
        actionList.add(new MoveAction());
        actionList.add(new NewLineStringAction());
        actionList.add(new NewPointAction());
        actionList.add(new NewPolygonAction());
        actionList.add(new PanAction());
        actionList.add(new RemoveAction());
        actionList.add(new SelectionAction());
        actionList.add(new SingleSelectionAction());
        actionList.add(new ZoomAction());
        actionList.add(new NewLinearReferencingAction());

        return actionList;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class SingleSelectionAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public SingleSelectionAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/select.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.SingleSelectionAction.initAction.tooltip"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("Ctrl+L"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "SingleSelectionAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        mapC.setInteractionMode(MappingComponent.SELECT);
                        ((SelectionListener)mapC.getInputListener(MappingComponent.SELECT)).setMode(
                            SelectionListener.RECTANGLE);
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class SelectionAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public SelectionAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/select.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.SelectionAction.initAction.tooltip"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("Ctrl+L"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "SelectionAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        mapC.setInteractionMode(MappingComponent.SELECT);
                        ((SelectionListener)mapC.getInputListener(MappingComponent.SELECT)).setMode(
                            SelectionListener.RECTANGLE);
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class ZoomAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public ZoomAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/zoom.gif")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.ZoomAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "ZoomAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            mapC.setInteractionMode(MappingComponent.ZOOM);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class PanAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public PanAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/pan.gif")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.PanAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "PanAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            mapC.setInteractionMode(MappingComponent.PAN);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class FeatureInfoAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public FeatureInfoAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/featureInfos.gif")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.FeatureInfoAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "FeatureInfoAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            mapC.setInteractionMode(MappingComponent.FEATURE_INFO);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NewPolygonAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NewPolygonAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/newPolygon.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.NewPolygonAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NewPolygonAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

                        if (mapC != null) {
                            ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                                CreateGeometryListenerInterface.POLYGON);
                            mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NewLineStringAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NewLineStringAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/newLinestring.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.NewLineStringAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NewLineStringAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                                CreateGeometryListenerInterface.LINESTRING);
                            mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NewPointAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NewPointAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/newPoint.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.NewPointAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NewPointAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                                CreateGeometryListenerInterface.POINT);
                            mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NewLinearReferencingAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NewLinearReferencingAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/linref.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.NewLinearReferencingAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NewLinearReferencingAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            mapC.setInteractionMode(MappingComponent.LINEAR_REFERENCING);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class MoveAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public MoveAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/move.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.MoveAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "MoveAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            mapC.setInteractionMode(MappingComponent.MOVE_POLYGON);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class RemoveAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public RemoveAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/remove.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    MapModeActionProvider.class,
                    "MapModeActionProvider.RemoveAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "RemoveAction");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
                        if (mapC != null) {
                            mapC.setInteractionMode(MappingComponent.REMOVE_POLYGON);
                        }
                    }
                });
        }
    }
}
