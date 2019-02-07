/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.export_map_actions;

import org.apache.commons.io.FilenameUtils;

import org.openide.util.NbBundle;

import java.awt.Image;
import java.awt.event.ActionEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.tools.FutureImageDownload;
import de.cismet.cismap.commons.tools.WorldFileDownload;

import de.cismet.tools.gui.ConfirmationJFileChooser;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.Download;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.MultipleDownload;
import de.cismet.tools.gui.menu.CidsUiAction;

import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class ExportMapToFileAction extends AbstractExportMapAction implements CidsUiAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ExportMapToFileAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportMapToFileAction object.
     */
    public ExportMapToFileAction() {
        this(null);
    }

    /**
     * Creates a new ExportMapToFileAction object.
     *
     * @param  exportMapDataProvider  DOCUMENT ME!
     */
    public ExportMapToFileAction(final ExportMapDataProvider exportMapDataProvider) {
        super(exportMapDataProvider);
        putValue(NAME, NbBundle.getMessage(ExportMapToFileAction.class, "ExportMapToFileAction.name"));
        putValue(
            SHORT_DESCRIPTION,
            NbBundle.getMessage(ExportMapToFileAction.class, "ExportMapToFileAction.tooltip"));
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/clipboard16.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "ExportMapToFileAction");
        putValue(
            SMALL_ICON,
            new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/navigatorplugin/res/legend.png")));
//            new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/navigatorplugin/res/icons16/icon-exportfile.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "ExportMapToFileAction");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        exportMapDataProvider.setLastUsedAction(this);
        final Object[] o = getFutureImageFromMapViaHeadlessMapProvider();
        final Future<Image> futureImage = (Future<Image>)o[0];
        final HeadlessMapProvider headlessMapProvider = (HeadlessMapProvider)o[1];
        final File file = chooseFile();
        if (file != null) {
            final ArrayList<Download> downloads = new ArrayList<Download>();
            final String imageFilePath = file.getAbsolutePath();
            final FutureImageDownload imageDownload = new FutureImageDownload(FilenameUtils.getBaseName(imageFilePath),
                    FilenameUtils.getExtension(imageFilePath),
                    NbBundle.getMessage(
                        ExportMapToFileAction.class,
                        "ExportMapToFileAction.downloadTitle.map"),
                    file,
                    futureImage);
            downloads.add(imageDownload);
            if (exportMapDataProvider.isGenerateWorldFile()) {
                final String worldFileName = FilenameUtils.getFullPath(imageFilePath)
                            + FilenameUtils.getBaseName(imageFilePath) + "."
                            + exportMapDataProvider.getFileType().getWorldFileExtension();
                final WorldFileDownload worldFileDownload = new WorldFileDownload(NbBundle.getMessage(
                            ExportMapToFileAction.class,
                            "ExportMapToFileAction.downloadTitle.worldFile"),
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
                            "ExportMapToFileAction.downloadTitle.multipleDownload"));
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
            fc = new JFileChooser(DownloadManager.instance().getDestinationDirectory(), new RestrictedFileSystemView());
        }
        final String[] allowedExtensions = exportMapDataProvider.getFileType().getFilterExtensions();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.isDirectory() || stringEndsWithArray(f.getName().toLowerCase(), allowedExtensions);
                }

                @Override
                public String getDescription() {
                    return exportMapDataProvider.getFileType().getDescription();
                }
            });

        final int state = fc.showSaveDialog(StaticSwingTools.getParentFrameIfNotNull(
                    exportMapDataProvider.getComponent()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("state:" + state); // NOI18N
        }
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            final String name = file.getAbsolutePath();
            if (!stringEndsWithArray(name.toLowerCase(), allowedExtensions)) {
                // NOI18N
                file = new File(file.getAbsolutePath()
                                + "." + exportMapDataProvider.getFileType().getImageFileExtension());
            }
            return file;
        } else {
            return null;
        }
    }

    /**
     * Checks if the end of the String s is an element of the array arr.
     *
     * @param   s    DOCUMENT ME!
     * @param   arr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean stringEndsWithArray(final String s, final String[] arr) {
        for (final String arrElement : arr) {
            if (s.endsWith("." + arrElement)) {
                return true;
            }
        }
        return false;
    }
}
