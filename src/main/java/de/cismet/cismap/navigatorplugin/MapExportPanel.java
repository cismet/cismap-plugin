/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.ui.ComponentRegistry;

import org.apache.commons.io.FilenameUtils;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.ClipboardWaitDialog;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.ImageDownload;
import de.cismet.cismap.commons.tools.PixelDPICalculator;
import de.cismet.cismap.commons.tools.WorldFileDownload;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.ConfirmationJFileChooser;
import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.StayOpenCheckBoxMenuItem;
import de.cismet.tools.gui.downloadmanager.Download;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.MultipleDownload;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MapExportPanel extends javax.swing.JPanel implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final ImageIcon clipboardIcon = new ImageIcon(MapExportPanel.class.getResource(
                "/images/clipboard.png"));
    private static final ImageIcon clipboardIcon16 = new ImageIcon(MapExportPanel.class.getResource(
                "/images/clipboard16.png"));
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GeoSearchButton.class);

    //~ Instance fields --------------------------------------------------------

    int httpInterfacePort = 9098;
    private ClipboardWaitDialog clipboarder;
    private MappingComponent mapC;
    private final ExportMapToClipboardAction exportMapToClipboardAction = new ExportMapToClipboardAction();
    private final ExportGeoPointToClipboardAction exportGeoPointToClipboardAction =
        new ExportGeoPointToClipboardAction();
    private final ExportMapToFileAction exportMapToFileAction = new ExportMapToFileAction();

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
     */
    public MapExportPanel() {
        initComponents();
        JFrame mainWindow;
        try {
            mainWindow = ComponentRegistry.getRegistry().getMainWindow();
        } catch (Exception ex) {
            LOG.error("An exception occured in the constructor of MapExportPanel", ex);
            mainWindow = null;
        }
        clipboarder = new ClipboardWaitDialog(mainWindow, true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean checkActionTag() {
        return false;
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
        btnClipboard.setToolTipText(org.openide.util.NbBundle.getMessage(
                MapExportPanel.class,
                "MapExportPanel.btnClipboard.toolTipText"));                                                 // NOI18N
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
        final Element prefs = parent.getChild("cismapPluginUIPreferences");               // NOI18N
        try {
            final Element httpInterfacePortElement = prefs.getChild("httpInterfacePort"); // NOI18N

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

            try {
                httpInterfacePort = new Integer(httpInterfacePortElement.getText());
            } catch (Throwable t) {
                LOG.warn("httpInterface was not configured. Set default value: " + httpInterfacePort, t); // NOI18N
            }
        } catch (Throwable t) {
            LOG.error("Error while loading the help urls (" + prefs.getChildren() + ")", t);              // NOI18N
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        LOG.fatal("MapExportPanel.getConfiguration: Not supported yet.", new Exception()); // NOI18N
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMapC() {
        if (mapC == null) {
            mapC = CismapBroker.getInstance().getMappingComponent();
        }
        return mapC;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object[] getFutureImageFromMapViaHeadlessMapProvider() {
        final PixelDPICalculator pixelDPICalculator = new PixelDPICalculator(getMapC().getHeight(),
                getMapC().getWidth(),
                72);
        final HeadlessMapProvider headlessMapProvider = HeadlessMapProvider.createHeadlessMapProviderAndAddLayers(
                getMapC());
        headlessMapProvider.setDominatingDimension(HeadlessMapProvider.DominatingDimension.BOUNDINGBOX);
        headlessMapProvider.setBoundingBox((XBoundingBox)getMapC().getCurrentBoundingBoxFromCamera());

        final int newDpi = Integer.parseInt(btngDpi.getSelection().getActionCommand());
        if (newDpi != 72) {
            pixelDPICalculator.setDPI(newDpi);
        }

        final Future<Image> futureImage = headlessMapProvider.getImage(pixelDPICalculator.getWidthPixel(),
                pixelDPICalculator.getHeightPixel());
        return new Object[] { futureImage, headlessMapProvider };
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExportMapToClipboardAction extends AbstractAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ExportMapToClipboardAction object.
         */
        public ExportMapToClipboardAction() {
            putValue(
                NAME,
                NbBundle.getMessage(
                    ExportMapToClipboardAction.class,
                    "MapExportPanel.ExportMapToClipboardAction.name"));
            putValue(
                SHORT_DESCRIPTION,
                NbBundle.getMessage(
                    ExportMapToClipboardAction.class,
                    "MapExportPanel.ExportMapToClipboardAction.tooltip"));
            putValue(SMALL_ICON, clipboardIcon16);
            putValue(LARGE_ICON_KEY, clipboardIcon);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            btnClipboard.setAction(this);

            final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        StaticSwingTools.showDialog(clipboarder);
                                    }
                                });

                            Image image;
                            if ("72".equals(btngDpi.getSelection().getActionCommand())) {
                                // for the default DPI 72 get the image from the map directly as it is much faster
                                image = getMapC().getImage();
                            } else {
                                try {
                                    image = ((Future<Image>)getFutureImageFromMapViaHeadlessMapProvider()[0]).get();
                                } catch (InterruptedException ex) {
                                    LOG.error(ex);
                                    image = getMapC().getImage();
                                } catch (ExecutionException ex) {
                                    LOG.error(ex);
                                    image = getMapC().getImage();
                                }
                            }
                            final ImageSelection imgSel = new ImageSelection(image);
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);

                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        clipboarder.dispose();
                                    }
                                });
                        }
                    });
            t.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExportMapToFileAction extends AbstractAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ExportMapToFileAction object.
         */
        public ExportMapToFileAction() {
            putValue(
                NAME,
                NbBundle.getMessage(ExportMapToFileAction.class, "MapExportPanel.ExportMapToFileAction.name"));
            putValue(
                SHORT_DESCRIPTION,
                NbBundle.getMessage(ExportMapToFileAction.class, "MapExportPanel.ExportMapToFileAction.tooltip"));
            putValue(SMALL_ICON, clipboardIcon16);
            putValue(LARGE_ICON_KEY, clipboardIcon);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            btnClipboard.setAction(this);
            final Object[] o = getFutureImageFromMapViaHeadlessMapProvider();
            final Future<Image> futureImage = (Future<Image>)o[0];
            final HeadlessMapProvider headlessMapProvider = (HeadlessMapProvider)o[1];

            final File file = chooseFile();

            if (file != null) {
                final ArrayList<Download> downloads = new ArrayList<Download>();
                final String imageFilePath = file.getAbsolutePath();
                final ImageDownload imageDownload = new ImageDownload(
                        FilenameUtils.getBaseName(imageFilePath),
                        FilenameUtils.getExtension(imageFilePath),
                        NbBundle.getMessage(
                            ExportMapToFileAction.class,
                            "MapExportPanel.ExportMapToFileAction.downloadTitle.map"),
                        file,
                        futureImage);
                downloads.add(imageDownload);
                if (cmniWorldFile.isSelected()) {
                    final String worldFileName = FilenameUtils.getFullPath(imageFilePath)
                                + FilenameUtils.getBaseName(imageFilePath)
                                + getWorldFileExtension();
                    final WorldFileDownload worldFileDownload = new WorldFileDownload(
                            NbBundle.getMessage(
                                ExportMapToFileAction.class,
                                "MapExportPanel.ExportMapToFileAction.downloadTitle.worldFile"),
                            futureImage,
                            headlessMapProvider.getCurrentBoundingBoxFromMap(),
                            worldFileName);
                    downloads.add(worldFileDownload);
                }
                if (downloads.size() > 1) {
                    final MultipleDownload multipleDownload = new MultipleDownload(
                            downloads,
                            NbBundle.getMessage(
                                ExportMapToFileAction.class,
                                "MapExportPanel.ExportMapToFileAction.downloadTitle.multipleDownload"));
                    DownloadManager.instance().add(multipleDownload);
                } else {
                    DownloadManager.instance().add(downloads.get(0));
                }
            }
        }

        /**
         * Opens a JFileChooser with a filter for the selected file format and checks if the chosen file has the right
         * extension. If not the right extension is added.
         *
         * @return  DOCUMENT ME!
         */
        private File chooseFile() {
            JFileChooser fc;
            try {
                fc = new ConfirmationJFileChooser(DownloadManager.instance().getDestinationDirectory());
            } catch (Exception bug) {
                // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
                fc = new JFileChooser(DownloadManager.instance().getDestinationDirectory(),
                        new RestrictedFileSystemView());
            }

            final String[] allowedExtensions = getAllowedFileExtensions();
            final String mainFileExtension = allowedExtensions[0];

            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(final File f) {
                        return f.isDirectory()
                                    || stringEndsWithArray(f.getName().toLowerCase(), allowedExtensions);
                    }

                    @Override
                    public String getDescription() {
                        return getFileDescription();
                    }
                });

            final int state = fc.showSaveDialog(StaticSwingTools.getParentFrameIfNotNull(MapExportPanel.this));
            if (LOG.isDebugEnabled()) {
                LOG.debug("state:" + state); // NOI18N
            }

            if (state == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                final String name = file.getAbsolutePath();

                if (!stringEndsWithArray(name.toLowerCase(), allowedExtensions)) { // NOI18N
                    file = new File(file.getAbsolutePath() + mainFileExtension);
                }
                return file;
            } else {
                return null;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   s    DOCUMENT ME!
         * @param   arr  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private boolean stringEndsWithArray(final String s, final String[] arr) {
            for (final String arrElement : arr) {
                final boolean endsWith = s.endsWith(arrElement);
                if (endsWith) {
                    return true;
                }
            }
            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String[] getAllowedFileExtensions() {
            if (rmniGif.isSelected()) {
                return new String[] { ".gif" };
            } else if (rmniJpeg.isSelected()) {
                return new String[] { ".jpg", ".jpeg" };
            } else if (rmniPng.isSelected()) {
                return new String[] { ".png" };
            } else if (rmniTif.isSelected()) {
                return new String[] { ".tif", ".tiff" };
            }
            return new String[] { "" };
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getWorldFileExtension() {
            if (rmniGif.isSelected()) {
                return ".gfw";
            } else if (rmniJpeg.isSelected()) {
                return ".jgw";
            } else if (rmniPng.isSelected()) {
                return "pgw";
            } else if (rmniTif.isSelected()) {
                return "tfw";
            }
            return "";
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getFileDescription() {
            if (rmniGif.isSelected()) {
                return NbBundle.getMessage(
                        ExportMapToFileAction.class,
                        "MapExportPanel.ExportMapToFileAction.getFileDescription.gif");
            } else if (rmniJpeg.isSelected()) {
                return NbBundle.getMessage(
                        ExportMapToFileAction.class,
                        "MapExportPanel.ExportMapToFileAction.getFileDescription.jpg");
            } else if (rmniPng.isSelected()) {
                return NbBundle.getMessage(
                        ExportMapToFileAction.class,
                        "MapExportPanel.ExportMapToFileAction.getFileDescription.png");
            } else if (rmniTif.isSelected()) {
                return NbBundle.getMessage(
                        ExportMapToFileAction.class,
                        "MapExportPanel.ExportMapToFileAction.getFileDescription.tif");
            }
            return "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExportGeoPointToClipboardAction extends AbstractAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ExportGeoPointToClipboardAction object.
         */
        public ExportGeoPointToClipboardAction() {
            putValue(
                NAME,
                NbBundle.getMessage(
                    ExportGeoPointToClipboardAction.class,
                    "MapExportPanel.ExportGeoPointToClipboardAction.name"));
            putValue(
                SHORT_DESCRIPTION,
                NbBundle.getMessage(
                    ExportGeoPointToClipboardAction.class,
                    "MapExportPanel.ExportGeoPointToClipboardAction.tooltip"));
            putValue(SMALL_ICON, clipboardIcon16);
            putValue(LARGE_ICON_KEY, clipboardIcon);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            btnClipboard.setAction(this);
            final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            final BoundingBox bb = getMapC().getCurrentBoundingBoxFromCamera();
                            final String u = "http://localhost:" + httpInterfacePort + "/gotoBoundingBox?x1="
                                        + bb.getX1()                                                       // NOI18N
                                        + "&y1=" + bb.getY1() + "&x2=" + bb.getX2() + "&y2=" + bb.getY2(); // NOI18N
                            final GeoLinkUrl url = new GeoLinkUrl(u);
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(url, null);
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        clipboarder.dispose();
                                    }
                                });
                        }
                    });
            t.start();
        }
    }
}
