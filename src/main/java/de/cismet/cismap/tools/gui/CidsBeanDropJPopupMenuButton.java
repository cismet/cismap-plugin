/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.tools.gui;

import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.apache.log4j.Logger;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
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
public class CidsBeanDropJPopupMenuButton extends JPopupMenuButton implements CidsBeanDropListener, DropTargetListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsBeanDropJPopupMenuButton.class);
    private static final String uiClassID = "ToggleButtonUI";

    //~ Instance fields --------------------------------------------------------

    private String interactionMode;
    private MappingComponent mappingComponent;
    private String searchName;
    private Icon defaultIcon = null;
    private Icon targetIcon = null;

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
                    final Collection<Geometry> searchGeoms = new ArrayList<Geometry>();
                    for (final CidsBean cb : beans) {
                        final MetaObject mo = cb.getMetaObject();
                        final CidsFeature cf = new CidsFeature(mo);
                        searchGeoms.add(cf.getGeometry());
                    }
                    final Geometry[] searchGeomsArr = searchGeoms.toArray(
                            new Geometry[0]);
                    final GeometryCollection coll = new GeometryFactory().createGeometryCollection(searchGeomsArr);

                    final Geometry newG = coll.buffer(0.1d);
                    search = new SearchFeature(newG);
                    search.setGeometryType(PureNewFeature.geomTypes.POLYGON);
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
        super.setIcon(defaultIcon);
    }

    @Override
    public void setIcon(final Icon defaultIcon) {
        super.setIcon(defaultIcon);
        this.defaultIcon = defaultIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  targetIcon  DOCUMENT ME!
     */
    public void setTargetIcon(final Icon targetIcon) {
        this.targetIcon = targetIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Icon getTargetIcon() {
        return targetIcon;
    }

    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
        if (isSelected()) {
            super.setSelectedIcon(targetIcon);
        } else {
            super.setIcon(targetIcon);
        }
    }

    @Override
    public void dragExit(final DropTargetEvent dte) {
        if (isSelected()) {
            super.setSelectedIcon(defaultIcon);
        } else {
            super.setIcon(defaultIcon);
        }
    }

    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
        if (isSelected()) {
            super.setSelectedIcon(targetIcon);
        } else {
            super.setIcon(targetIcon);
        }
    }

    @Override
    public void drop(final DropTargetDropEvent dtde) {
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
    }
}
