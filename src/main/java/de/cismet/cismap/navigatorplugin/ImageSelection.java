/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   $author$
 * @version  $Revision$, $Date$
 */
public class ImageSelection implements Transferable {

    //~ Instance fields --------------------------------------------------------

    private Image image;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageSelection object.
     *
     * @param  image  DOCUMENT ME!
     */
    public ImageSelection(final Image image) {
        this.image = image;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns supported flavors.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    /**
     * Returns true if flavor is supported.
     *
     * @param   flavor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    /**
     * Returns image.
     *
     * @param   flavor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedFlavorException  DOCUMENT ME!
     * @throws  IOException                 DOCUMENT ME!
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw (new UnsupportedFlavorException(flavor));
        }

        return image;
    }
}
