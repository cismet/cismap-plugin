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

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerDropUtils;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.menu.CidsUiAction;
import de.cismet.tools.gui.menu.CidsUiActionProvider;

import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.LARGE_ICON_KEY;
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
public class ConfigurationActionProvider implements CidsUiActionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ConfigurationActionProvider.class);
    private static String cismapDirectory;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigurationActionProvider object.
     */
    public ConfigurationActionProvider() {
        final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();

        if (navigator != null) {
            cismapDirectory = navigator.getCismapDirectory();
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public List<CidsUiAction> getActions() {
        final List<CidsUiAction> actionList = new ArrayList<CidsUiAction>();

        final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();

        if (navigator != null) {
        }

        actionList.add(new LoadConfigurationAction());
        actionList.add(new LoadConfigurationFromServerAction());
        actionList.add(new SaveConfigurationAction());
        actionList.add(new LoadShapeAction());

        return actionList;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class LoadConfigurationAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public LoadConfigurationAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/config.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    ConfigurationActionProvider.class,
                    "ConfigurationActionProvider.LoadConfigurationAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    ConfigurationActionProvider.class,
                    "ConfigurationActionProvider.LoadConfigurationAction.initAction.tooltip"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl L"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.load.config");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            JFileChooser fc;

            try {
                fc = new JFileChooser(cismapDirectory);
            } catch (Exception bug) {
                // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
                fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
            }

            fc.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(final File f) {
                        return f.isDirectory()
                                    || f.getName().toLowerCase().endsWith(".xml"); // NOI18N
                    }

                    @Override
                    public String getDescription() {
                        return org.openide.util.NbBundle.getMessage(
                                LoadConfigurationAction.class,
                                "ConfigurationActionProvider.LoadConfigurationAction.FileFiltergetDescription.return"); // NOI18N
                    }
                });

            final int state = fc.showOpenDialog(ComponentRegistry.getRegistry().getMainWindow());

            if (state == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                final String name = file.getAbsolutePath();
                final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();

                if (name.endsWith(".xml")) {                                            // NOI18N
                    ((ActiveLayerModel)CismapBroker.getInstance().getMappingComponent().getMappingModel())
                            .removeAllLayers();
                    CismapBroker.getInstance().getMappingComponent().getRasterServiceLayer().removeAllChildren();
                    navigator.getCismapConfigurationManager().configure(name);
                } else {
                    ((ActiveLayerModel)CismapBroker.getInstance().getMappingComponent().getMappingModel())
                            .removeAllLayers();
                    CismapBroker.getInstance().getMappingComponent().getRasterServiceLayer().removeAllChildren();
                    navigator.getCismapConfigurationManager().configure(name + ".xml"); // NOI18N
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class SaveConfigurationAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SaveConfigurationAction object.
         */
        public SaveConfigurationAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/config.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    ConfigurationActionProvider.class,
                    "ConfigurationActionProvider.SaveConfigurationAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    ConfigurationActionProvider.class,
                    "ConfigurationActionProvider.SaveConfigurationAction.initAction.tooltip"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl K"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.save.config");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            JFileChooser fc;

            try {
                fc = new JFileChooser(cismapDirectory);
            } catch (Exception bug) {
                // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
                fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
            }

            fc.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(final File f) {
                        return f.isDirectory()
                                    || f.getName().toLowerCase().endsWith(".xml"); // NOI18N
                    }

                    @Override
                    public String getDescription() {
                        return org.openide.util.NbBundle.getMessage(
                                LoadConfigurationAction.class,
                                "ConfigurationActionProvider.SaveConfigurationAction.FileFilter.getDescription.return"); // NOI18N
                    }
                });

            final int state = fc.showSaveDialog(ComponentRegistry.getRegistry().getMainWindow());
            if (LOG.isDebugEnabled()) {
                LOG.debug("state:" + state); // NOI18N
            }

            if (state == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                final String name = file.getAbsolutePath();
                final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();

                if (name.endsWith(".xml")) {                                                     // NOI18N
                    navigator.getCismapConfigurationManager().writeConfiguration(name);
                } else {
                    navigator.getCismapConfigurationManager().writeConfiguration(name + ".xml"); // NOI18N
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class LoadConfigurationFromServerAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationFromServerAction object.
         */
        public LoadConfigurationFromServerAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/config.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    ConfigurationActionProvider.class,
                    "ConfigurationActionProvider.LoadConfigurationFromServerAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    ConfigurationActionProvider.class,
                    "ConfigurationActionProvider.LoadConfigurationFromServerAction.initAction.tooltip"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("Ctrl+L"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.load.server.config");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            ((ActiveLayerModel)CismapBroker.getInstance().getMappingComponent().getMappingModel()).removeAllLayers();
            CismapBroker.getInstance().getMappingComponent().getMapServiceLayer().removeAllChildren();
            CismapBroker.getInstance().getMappingComponent().lock();
            final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();

            navigator.getCismapConfigurationManager().configureFromClasspath();

            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        CismapBroker.getInstance().getMappingComponent().unlock();
                    }
                });
        }
    }

    /**
     * /** * DOCUMENT ME! * * @param serverFirst DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class LoadShapeAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

// private static final Logger LOG = Logger.getLogger(de.cismet.cismap.navigatorplugin.actions.LoadShapeAction.class);

        /**
         * Creates a new RefreshAction object.
         */
        public LoadShapeAction() {
            final String tooltip = org.openide.util.NbBundle.getMessage(
                    LoadShapeAction.class,
                    "ConfigurationActionProvider.LoadShapeAction.toolTipText");
            final String name = org.openide.util.NbBundle.getMessage(
                    LoadShapeAction.class,
                    "ConfigurationActionProvider.LoadShapeAction.text");
            putValue(SHORT_DESCRIPTION, tooltip);
            putValue(NAME, name);
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/icon-importfile.png")));
            putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/icon-importfile.png")));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "cismap.load.shape");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            JFileChooser fc;
            final Component parent = ComponentRegistry.getRegistry().getMainWindow();

            try {
                fc = new JFileChooser(cismapDirectory);
            } catch (Exception bug) {
                // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
                fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
            }

            fc.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(final File f) {
                        return f.isDirectory()
                                    || f.getName().toLowerCase().endsWith(".shp"); // NOI18N
                    }

                    @Override
                    public String getDescription() {
                        return org.openide.util.NbBundle.getMessage(
                                ConfigurationActionProvider.class,
                                "ConfigurationActionProvider.LoadShapeAction.FileFiltergetDescription.return"); // NOI18N
                    }
                });

            final int state = fc.showOpenDialog(parent);

            if (state == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                final String name = file.getAbsolutePath();

                final ActiveLayerModel model = (ActiveLayerModel)CismapBroker.getInstance().getMappingComponent()
                            .getMappingModel();
                LayerDropUtils.handleFiles(Collections.nCopies(1, file), model, 0, parent);
            }
        }
    }
}
