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

import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiActionProvider.class)
public class NodeActionProvider implements CidsUiActionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(NodeActionProvider.class);
    private static String cismapDirectory;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigurationActionProvider object.
     */
    public NodeActionProvider() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cismapDirectory  DOCUMENT ME!
     */
    public static void setCismapDirectory(final String cismapDirectory) {
        NodeActionProvider.cismapDirectory = cismapDirectory;
    }

    @Override
    public List<CidsUiAction> getActions() {
        final List<CidsUiAction> actionList = new ArrayList<CidsUiAction>();

        actionList.add(new NodeAddAction());
        actionList.add(new NodeMoveAction());
        actionList.add(new NodeReflectAction());
        actionList.add(new NodeRemoveAction());
        actionList.add(new NodeRotateAction());

        return actionList;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NodeMoveAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NodeMoveAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/moveNodes.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    NodeActionProvider.class,
                    "NodeActionProvider.NodeMoveAction.initAction.tooltip"));
            putValue(SELECTED_KEY, true);
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NodeMoveAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
                mapC.setInteractionMode(MappingComponent.SELECT);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NodeAddAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NodeAddAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/insertNodes.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    NodeActionProvider.class,
                    "NodeActionProvider.NodeAddAction.initAction.tooltip"));
            putValue(SELECTED_KEY, true);
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NodeAddAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.setHandleInteractionMode(MappingComponent.ADD_HANDLE);
                mapC.setInteractionMode(MappingComponent.SELECT);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NodeRemoveAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NodeRemoveAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/removeNodes.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    NodeActionProvider.class,
                    "NodeActionProvider.NodeRemoveAction.initAction.tooltip"));
            putValue(SELECTED_KEY, true);
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NodeRemoveAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.setHandleInteractionMode(MappingComponent.REMOVE_HANDLE);
                mapC.setInteractionMode(MappingComponent.SELECT);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NodeRotateAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NodeRotateAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/rotate.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    NodeActionProvider.class,
                    "NodeActionProvider.NodeRotateAction.initAction.tooltip"));
            putValue(SELECTED_KEY, true);
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NodeRotateAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
                mapC.setInteractionMode(MappingComponent.SELECT);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NodeReflectAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NodeReflectAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/mirror.png")));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    NodeActionProvider.class,
                    "NodeActionProvider.NodeReflectAction.initAction.tooltip"));
            putValue(SELECTED_KEY, true);
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NodeReflectAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                mapC.setHandleInteractionMode(MappingComponent.REFLECT_POLYGON);
                mapC.setInteractionMode(MappingComponent.SELECT);
            }
        }
    }
}
