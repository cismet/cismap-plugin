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

import de.cismet.cismap.navigatorplugin.ImageSelection;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.menu.CidsUiAction;

import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class ExportMapToClipboardAction extends AbstractExportMapAction implements CidsUiAction {

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
        putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/clipboard16.png")));
        putValue(CidsUiAction.CIDS_ACTION_KEY, "ExportMapToClipboardAction");
        putValue(
            ACCELERATOR_KEY,
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
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

                        final Image image = getMapC().getImage();
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
