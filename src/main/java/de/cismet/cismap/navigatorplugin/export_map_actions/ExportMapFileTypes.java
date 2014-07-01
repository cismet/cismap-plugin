/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.export_map_actions;

import org.openide.util.NbBundle;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public enum ExportMapFileTypes {

    //~ Enum constants ---------------------------------------------------------

    GIF(NbBundle.getMessage(
            ExportMapToFileAction.class,
            "ExportMapFileTypes.getFileDescription.gif"),
        ".gif",
        ".gfw",
        new String[] { ".gif" }),
    JPEG(NbBundle.getMessage(
            ExportMapToFileAction.class,
            "ExportMapFileTypes.getFileDescription.jpg"),
        ".jpg",
        ".jgw",
        new String[] { ".jpg", ".jpeg" }),
    PNG(NbBundle.getMessage(
            ExportMapToFileAction.class,
            "ExportMapFileTypes.getFileDescription.png"),
        ".png",
        ".pgw",
        new String[] { ".png" }),
    TIF(NbBundle.getMessage(
            ExportMapToFileAction.class,
            "ExportMapFileTypes.getFileDescription.tif"),
        ".tif",
        ".tfw",
        new String[] { ".tif", ".tiff" });

    //~ Instance fields --------------------------------------------------------

    private final String description;
    private final String imageFileExtension;
    private final String worldFileExtension;
    private final String[] filterExpressions;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportMapFileTypes object.
     *
     * @param  description         DOCUMENT ME!
     * @param  imageFileExtension  DOCUMENT ME!
     * @param  worldFileExtension  DOCUMENT ME!
     * @param  filterExpressions   DOCUMENT ME!
     */
    private ExportMapFileTypes(final String description,
            final String imageFileExtension,
            final String worldFileExtension,
            final String[] filterExpressions) {
        this.description = description;
        this.imageFileExtension = imageFileExtension;
        this.worldFileExtension = worldFileExtension;
        this.filterExpressions = filterExpressions;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getImageFileExtension() {
        return imageFileExtension;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getWorldFileExtension() {
        return worldFileExtension;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getFilterExtensions() {
        return filterExpressions;
    }
}
