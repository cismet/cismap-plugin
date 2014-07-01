/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.export_map_actions;

import java.awt.Component;

import javax.swing.Action;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public interface ExportMapDataProvider {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getDpi();

    /**
     * DOCUMENT ME!
     *
     * @param  action  DOCUMENT ME!
     */
    void setLastUsedAction(Action action);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isGenerateWorldFile();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Component getComponent();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getAllowedFileExtensions();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getWorldFileExtension();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getFileDescription();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getHttpInterfacePort();
}
