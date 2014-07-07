/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.export_map_actions;

import Sirius.navigator.ui.ComponentRegistry;

import java.awt.Image;

import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.ClipboardWaitDialog;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PixelDPICalculator;

import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractExportMapAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final ImageIcon clipboardIcon = new ImageIcon(AbstractExportMapAction.class.getResource(
                "/images/clipboard.png"));
    private static final ImageIcon clipboardIcon16 = new ImageIcon(AbstractExportMapAction.class.getResource(
                "/images/clipboard16.png"));

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AbstractExportMapAction.class);

    //~ Instance fields --------------------------------------------------------

    ClipboardWaitDialog clipboarder;
    ExportMapDataProvider exportMapDataProvider;
    private MappingComponent mapC;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractExportMapAction object.
     *
     * @param  exportMapDataProvider  DOCUMENT ME!
     */
    public AbstractExportMapAction(final ExportMapDataProvider exportMapDataProvider) {
        if (exportMapDataProvider == null) {
            this.exportMapDataProvider = new DefaultExportMapDataProvider();
        } else {
            this.exportMapDataProvider = exportMapDataProvider;
        }
        putValue(SMALL_ICON, clipboardIcon16);
        putValue(LARGE_ICON_KEY, clipboardIcon);
        JFrame mainWindow;
        try {
            mainWindow = ComponentRegistry.getRegistry().getMainWindow();
        } catch (Exception ex) {
            LOG.error("An exception occured in the constructor of AbstractExportMapAction", ex);
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
    Object[] getFutureImageFromMapViaHeadlessMapProvider() {
        final PixelDPICalculator pixelDPICalculator = new PixelDPICalculator(getMapC().getHeight(),
                getMapC().getWidth(),
                72);
        final HeadlessMapProvider headlessMapProvider = HeadlessMapProvider.createHeadlessMapProviderAndAddLayers(
                getMapC());
        headlessMapProvider.setDominatingDimension(HeadlessMapProvider.DominatingDimension.BOUNDINGBOX);
        headlessMapProvider.setBoundingBox((XBoundingBox)getMapC().getCurrentBoundingBoxFromCamera());

        final int newDpi = exportMapDataProvider.getDpi();
        if (newDpi != 72) {
            pixelDPICalculator.setDPI(newDpi);
        }

        final Future<Image> futureImage = headlessMapProvider.getImage(pixelDPICalculator.getWidthPixel(),
                pixelDPICalculator.getHeightPixel());
        return new Object[] { futureImage, headlessMapProvider };
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
}
