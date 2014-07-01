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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import de.cismet.cismap.commons.BoundingBox;

import de.cismet.cismap.navigatorplugin.GeoLinkUrl;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ExportGeoPointToClipboardAction extends AbstractExportMapAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportGeoPointToClipboardAction object.
     *
     * @param  exportMapDataProvider  DOCUMENT ME!
     */
    public ExportGeoPointToClipboardAction(final ExportMapDataProvider exportMapDataProvider) {
        super(exportMapDataProvider);
        putValue(
            NAME,
            NbBundle.getMessage(
                ExportGeoPointToClipboardAction.class,
                "ExportGeoPointToClipboardAction.name"));
        putValue(
            SHORT_DESCRIPTION,
            NbBundle.getMessage(
                ExportGeoPointToClipboardAction.class,
                "ExportGeoPointToClipboardAction.tooltip"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        exportMapDataProvider.setLastUsedAction(this);
        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final BoundingBox bb = getMapC().getCurrentBoundingBoxFromCamera();
                        final String u = "http://localhost:" + exportMapDataProvider.getHttpInterfacePort()
                                    + "/gotoBoundingBox?x1=" + bb.getX1() + "&y1=" + bb.getY1() + "&x2=" + bb.getX2()
                                    + "&y2=" + bb.getY2(); // NOI18N
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
