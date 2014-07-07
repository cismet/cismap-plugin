/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.export_map_actions;

import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ExportMapToClipboardAction extends AbstractExportMapAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            ExportMapToClipboardAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportMapToClipboardAction object.
     */
    public ExportMapToClipboardAction() {
        this(null);
    }

    /**
     * Creates a new ExportMapToClipboardAction object.
     *
     * @param  exportMapDataProvider  DOCUMENT ME!
     */
    public ExportMapToClipboardAction(final ExportMapDataProvider exportMapDataProvider) {
        super(exportMapDataProvider);
        putValue(
            NAME,
            NbBundle.getMessage(ExportMapToClipboardAction.class, "ExportMapToClipboardAction.name"));
        putValue(
            SHORT_DESCRIPTION,
            NbBundle.getMessage(ExportMapToClipboardAction.class, "ExportMapToClipboardAction.tooltip"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        exportMapDataProvider.setLastUsedAction(this);
        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    StaticSwingTools.showDialog(clipboarder);
                                }
                            });

                        try {
                            final Image image = ((Future<Image>)getFutureImageFromMapViaHeadlessMapProvider()[0]).get();
                            final ByteArrayOutputStream byteoutarray = new ByteArrayOutputStream();
                            ImageIO.write(
                                (BufferedImage)image,
                                exportMapDataProvider.getFileType().getImageFileExtension(),
                                byteoutarray);

                            final ImageAsByteArraySelection trans = new ImageAsByteArraySelection(
                                    byteoutarray.toByteArray());

                            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(trans, null);
                        } catch (InterruptedException ex) {
                            LOG.error(ex);
                        } catch (ExecutionException ex) {
                            LOG.error(ex);
                        } catch (IOException ex) {
                            LOG.error(ex);
                        }

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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class ImageAsByteArraySelection implements Transferable {

        //~ Instance fields ----------------------------------------------------

        private final byte[] data;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ImageAsByteArraySelection object.
         *
         * @param  data  DOCUMENT ME!
         */
        public ImageAsByteArraySelection(final byte[] data) {
            this.data = data;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return Toolkit.getDefaultToolkit().createImage(data);
        }
    }
}
