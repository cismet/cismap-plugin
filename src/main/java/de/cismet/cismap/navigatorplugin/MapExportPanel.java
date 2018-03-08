/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;
import Sirius.navigator.plugin.PluginRegistry;

import org.jdom.Element;

import java.awt.Color;
import java.awt.Component;

import java.util.List;

import javax.swing.Action;

import de.cismet.cismap.navigatorplugin.export_map_actions.ExportGeoPointToClipboardAction;
import de.cismet.cismap.navigatorplugin.export_map_actions.ExportMapDataProvider;
import de.cismet.cismap.navigatorplugin.export_map_actions.ExportMapFileTypes;
import de.cismet.cismap.navigatorplugin.export_map_actions.ExportMapToClipboardAction;
import de.cismet.cismap.navigatorplugin.export_map_actions.ExportMapToFileAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.StayOpenCheckBoxMenuItem;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MapExportPanel extends javax.swing.JPanel implements Configurable,
    ExportMapDataProvider,
    ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GeoSearchButton.class);
    private static final String ACTION_TAG = "cismap.export_map.restricted_dpi";

    //~ Instance fields --------------------------------------------------------

    private final ExportMapToClipboardAction exportMapToClipboardAction;
    private final ExportGeoPointToClipboardAction exportGeoPointToClipboardAction;
    private final ExportMapToFileAction exportMapToFileAction;

    private final ConnectionContext connectionContext;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.tools.gui.JPopupMenuButton btnClipboard;
    private javax.swing.ButtonGroup btngDpi;
    private javax.swing.ButtonGroup btngExportMap;
    private javax.swing.ButtonGroup btngFileFormat;
    private javax.swing.JCheckBoxMenuItem cmniWorldFile;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JRadioButtonMenuItem rmniExportMapClipboard;
    private javax.swing.JRadioButtonMenuItem rmniExportMapFile;
    private javax.swing.JMenuItem rmniExportPointToClipboard;
    private javax.swing.JMenuItem rmniGif;
    private javax.swing.JMenuItem rmniJpeg;
    private javax.swing.JMenuItem rmniPng;
    private javax.swing.JMenuItem rmniTif;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MapExportPanel.
     *
     * @param  connectionContext  DOCUMENT ME!
     */
    public MapExportPanel(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
        exportMapToClipboardAction = new ExportMapToClipboardAction(this);
        exportGeoPointToClipboardAction = new ExportGeoPointToClipboardAction(this);
        exportMapToFileAction = new ExportMapToFileAction(this);
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean checkActionTag() {
        boolean result;
        try {
            result = SessionManager.getConnection()
                        .getConfigAttr(SessionManager.getSession().getUser(), ACTION_TAG, getConnectionContext())
                        != null;
        } catch (ConnectionException ex) {
            LOG.error("Can not check ActionTag!", ex);
            result = false;
        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        rmniExportMapClipboard = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        rmniExportMapFile = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        rmniPng = new StayOpenCheckBoxMenuItem(
                null,
                javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                Color.WHITE);
        rmniTif = new StayOpenCheckBoxMenuItem(
                null,
                javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                Color.WHITE);
        rmniGif = new StayOpenCheckBoxMenuItem(
                null,
                javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                Color.WHITE);
        rmniJpeg = new StayOpenCheckBoxMenuItem(
                null,
                javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                Color.WHITE);
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        cmniWorldFile = new StayOpenCheckBoxMenuItem(
                null,
                javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                Color.WHITE);
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        rmniExportPointToClipboard = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        btngExportMap = new javax.swing.ButtonGroup();
        btngFileFormat = new javax.swing.ButtonGroup();
        btngDpi = new javax.swing.ButtonGroup();
        btnClipboard = new de.cismet.tools.gui.JPopupMenuButton();

        rmniExportMapClipboard.setAction(exportMapToClipboardAction);
        btngExportMap.add(rmniExportMapClipboard);
        rmniExportMapClipboard.setSelected(true);
        jPopupMenu1.add(rmniExportMapClipboard);

        rmniExportMapFile.setAction(exportMapToFileAction);
        btngExportMap.add(rmniExportMapFile);
        jPopupMenu1.add(rmniExportMapFile);
        jPopupMenu1.add(jSeparator1);
        jPopupMenu1.add(jSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(
            rmniPng,
            org.openide.util.NbBundle.getMessage(MapExportPanel.class, "MapExportPanel.rmniPng.text")); // NOI18N
        btngFileFormat.add(rmniPng);
        rmniPng.setSelected(true);
        jPopupMenu1.add(rmniPng);

        org.openide.awt.Mnemonics.setLocalizedText(
            rmniTif,
            org.openide.util.NbBundle.getMessage(MapExportPanel.class, "MapExportPanel.rmniTif.text")); // NOI18N
        btngFileFormat.add(rmniTif);
        jPopupMenu1.add(rmniTif);

        org.openide.awt.Mnemonics.setLocalizedText(
            rmniGif,
            org.openide.util.NbBundle.getMessage(MapExportPanel.class, "MapExportPanel.rmniGif.text")); // NOI18N
        btngFileFormat.add(rmniGif);
        jPopupMenu1.add(rmniGif);

        org.openide.awt.Mnemonics.setLocalizedText(
            rmniJpeg,
            org.openide.util.NbBundle.getMessage(MapExportPanel.class, "MapExportPanel.rmniJpeg.text")); // NOI18N
        btngFileFormat.add(rmniJpeg);
        jPopupMenu1.add(rmniJpeg);
        jPopupMenu1.add(jSeparator3);

        cmniWorldFile.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            cmniWorldFile,
            org.openide.util.NbBundle.getMessage(MapExportPanel.class, "MapExportPanel.cmniWorldFile.text")); // NOI18N
        jPopupMenu1.add(cmniWorldFile);
        jPopupMenu1.add(jSeparator4);

        rmniExportPointToClipboard.setAction(exportGeoPointToClipboardAction);
        btngExportMap.add(rmniExportPointToClipboard);
        jPopupMenu1.add(rmniExportPointToClipboard);

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        btnClipboard.setAction(exportMapToClipboardAction);
        btnClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard.png")));    // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            btnClipboard,
            org.openide.util.NbBundle.getMessage(MapExportPanel.class, "MapExportPanel.btnClipboard.text")); // NOI18N
        btnClipboard.setBorderPainted(false);
        btnClipboard.setContentAreaFilled(false);
        btnClipboard.setHideActionText(true);
        btnClipboard.setPopupMenu(jPopupMenu1);
        add(btnClipboard, java.awt.BorderLayout.CENTER);
    }                                                                                                        // </editor-fold>//GEN-END:initComponents

    @Override
    public void configure(final Element parent) {
    }

    @Override
    public void masterConfigure(final Element parent) {
        Element prefs = parent.getChild("cismapMappingPreferences"); // NOI18N
        prefs = prefs.getChild("exportMap");
        try {
            // load the DPIs from the config file
            final List<Element> dpis = prefs.getChildren("DPI");
            int addAtIndex = 3;
            for (final Element dpi : dpis) {
                final String name = dpi.getAttributeValue("name");
                final String value = dpi.getAttributeValue("value");
                final boolean restricted = "true".equals(dpi.getAttributeValue("restricted"));

                final boolean add = !restricted || (restricted && checkActionTag());

                if (add) {
                    final StayOpenCheckBoxMenuItem item = new StayOpenCheckBoxMenuItem(
                            null,
                            javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground"), // NOI18N
                            Color.WHITE);
                    item.setText(name);
                    item.setSelected(value.equals("72"));
                    item.setActionCommand(value);
                    btngDpi.add(item);
                    jPopupMenu1.add(item, addAtIndex);
                    addAtIndex++;
                }
                jPopupMenu1.revalidate();
            }
        } catch (Throwable t) {
            LOG.error("Error while loading the help urls (" + prefs.getChildren() + ")", t); // NOI18N
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        return null;
    }

    @Override
    public int getDpi() {
        if ((btngDpi == null) || (btngDpi.getSelection() == null)) {
            return 72;
        } else {
            return Integer.parseInt(btngDpi.getSelection().getActionCommand());
        }
    }

    @Override
    public void setLastUsedAction(final Action action) {
        btnClipboard.setAction(action);
    }

    @Override
    public boolean isGenerateWorldFile() {
        return cmniWorldFile.isSelected();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public ExportMapFileTypes getFileType() {
        if (rmniGif.isSelected()) {
            return ExportMapFileTypes.GIF;
        } else if (rmniJpeg.isSelected()) {
            return ExportMapFileTypes.JPEG;
        } else if (rmniPng.isSelected()) {
            return ExportMapFileTypes.PNG;
        } else if (rmniTif.isSelected()) {
            return ExportMapFileTypes.TIF;
        } else {
            LOG.error("No file type selected. This should not happen.");
            return null;
        }
    }

    @Override
    public int getHttpInterfacePort() {
        final CismapPlugin cismapPlugin = (CismapPlugin)PluginRegistry.getRegistry().getPlugin("cismap");
        if (cismapPlugin == null) {
            return 9098;
        } else {
            return cismapPlugin.getHttpInterfacePort();
        }
    }

    @Override
    public final ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
