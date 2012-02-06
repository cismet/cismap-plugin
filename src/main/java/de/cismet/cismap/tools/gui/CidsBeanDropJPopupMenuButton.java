/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.tools.gui;

import Sirius.server.middleware.types.MetaObject;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import javax.swing.JToggleButton;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.CidsBeanDropListener;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;

import de.cismet.cismap.navigatorplugin.CidsFeature;

import de.cismet.tools.gui.JPopupMenuButton;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CidsBeanDropJPopupMenuButton extends JPopupMenuButton implements CidsBeanDropListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsBeanDropJPopupMenuButton.class);
    private static final String uiClassID = "ToggleButtonUI";

    //~ Instance fields --------------------------------------------------------

    private String interactionMode;
    private MappingComponent mappingComponent;
    private String searchName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeanDropJPopupMenuButton object.
     *
     * @param  interactionMode   DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  searchName        DOCUMENT ME!
     */
    public CidsBeanDropJPopupMenuButton(final String interactionMode,
            final MappingComponent mappingComponent,
            final String searchName) {
        this.interactionMode = interactionMode;
        this.mappingComponent = mappingComponent;
        this.searchName = searchName;

        setModel(new JToggleButton.ToggleButtonModel());
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public void beansDropped(final ArrayList<CidsBean> beans) {
        mappingComponent.setInteractionMode(interactionMode);
        final CreateSearchGeometryListener searchListener = ((CreateSearchGeometryListener)
                mappingComponent.getInputListener(
                    interactionMode));

        de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<SearchFeature, Void>() {

                @Override
                protected SearchFeature doInBackground() throws Exception {
                    SearchFeature search = null;

                    for (final CidsBean cb : beans) {
                        final MetaObject mo = cb.getMetaObject();
                        final CidsFeature cf = new CidsFeature(mo);
                        if (search == null) {
                            search = new SearchFeature(cf.getGeometry());
                            search.setName(cb.toString());
                            search.setGeometryType(PureNewFeature.geomTypes.POLYGON);
                        } else {
                            search = new SearchFeature(
                                    search.getGeometry().union(cf.getGeometry()));
                            search.setName(searchName);
                        }
                    }
                    return search;
                }

                @Override
                protected void done() {
                    try {
                        final SearchFeature search = get();
                        if (search != null) {
                            searchListener.search(search);
                        }
                    } catch (Exception e) {
                        LOG.error("Exception in Background Thread", e);
                    }
                }
            });
    }
}
