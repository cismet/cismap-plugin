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
import java.awt.event.ActionEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import de.cismet.cismap.navigatorplugin.ImageSelection;

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

                        Image image;
                        final int dpi = exportMapDataProvider.getDpi();
                        if (dpi == 72) {
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
