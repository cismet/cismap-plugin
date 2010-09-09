/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.cids.geometryeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.PureNewFeature;

import de.cismet.tools.CurrentStackTrace;
/**
 * End of variables declaration.
 *
 * @version  $Revision$, $Date$
 */
class CismapGeometryComboModel extends AbstractListModel implements ComboBoxModel, FeatureCollectionListener {

    //~ Static fields/initializers ---------------------------------------------


    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final DefaultCismapGeometryComboBoxEditor editor;
    private Object selectedItem = null;
    private Feature currentObjectFeature;
    private List<Feature> newFeaturesInMap;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CismapGeometryComboModel object.
     *
     * @param  editor                DOCUMENT ME!
     * @param  currentObjectFeature  DOCUMENT ME!
     */
    public CismapGeometryComboModel(final DefaultCismapGeometryComboBoxEditor editor,
            final Feature currentObjectFeature) {
        this.editor = editor;
        if (log.isDebugEnabled()) {
            log.debug("editor (con): " + editor); // NOI18N
        }
        this.currentObjectFeature = currentObjectFeature;
        refresh();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Set the selected item. The implementation of this method should notify all registered <code>
     * ListDataListener</code>s that the contents have changed.
     *
     * @param  anItem  the list object to select or <code>null</code> to clear the selection
     */
    @Override
    public void setSelectedItem(final Object anItem) {
        selectedItem = anItem;
    }

    /**
     * Returns the value at the specified index.
     *
     * @param   index  the requested index
     *
     * @return  the value at <code>index</code>
     */
    @Override
    public Object getElementAt(final int index) {
        if (currentObjectFeature != null) { // &&currentObjectFeature.getGeometry()!=null) {
            if (index == 0) {
                return currentObjectFeature;
            } else if (index == (getSize() - 1)) {
                return null;
            } else {
                return newFeaturesInMap.get(index - 1);
            }
        } else {
            if (index == 0) {
                return currentObjectFeature;
            } else {
                return newFeaturesInMap.get(index - 1);
            }
        }
    }

//    /**
//     * Removes a listener from the list that's notified each time a
//     * change to the data model occurs.
//     *
//     * @param l the <code>ListDataListener</code> to be removed
//     */
//    public void removeListDataListener(ListDataListener l) {
//        listeners.remove(l);
//    }
//
//    /**
//     * Adds a listener to the list that's notified each time a change
//     * to the data model occurs.
//     *
//     * @param l the <code>ListDataListener</code> to be added
//     */
//    public void addListDataListener(ListDataListener l) {
//        listeners.add(l);
//    }
    /**
     * Returns the length of the list.
     *
     * @return  the length of the list
     */
    @Override
    public int getSize() {
        if (currentObjectFeature != null) {
            return newFeaturesInMap.size() + 2;
        } else {
            return newFeaturesInMap.size() + 1;
        }
    }

    /**
     * Returns the selected item.
     *
     * @return  The selected item or <code>null</code> if there is no selection
     */
    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    /**
     * DOCUMENT ME!
     */
    public void refresh() {
        if (log.isDebugEnabled()) {
            log.debug("refreshing: " + editor.getCismap()); // NOI18N
        }
        newFeaturesInMap = getAllNewFeatures();
        try {
            this.fireContentsChanged(this, 0, getSize() - 1);
        } catch (Throwable t) {
            log.error("Error in refresh()", t);             // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<Feature> getAllNewFeatures() {
        // Vector<Feature> allFeatures =
        // CismapBroker.getInstance().getMappingComponent().getFeatureCollection().getAllFeatures();
        final List<Feature> allNewFeatures = new ArrayList<Feature>();
        if (editor.getCismap() != null) {
            final Vector<Feature> allFeatures = editor.getCismap()
                        .getMappingComponent()
                        .getFeatureCollection()
                        .getAllFeatures();

            for (final Feature f : allFeatures) {
                if (f instanceof PureNewFeature) {                                     // ||f instanceof CidsFeature) {
                    allNewFeatures.add(f);
                }
            }
        } else {
            log.error("cismap not found. No content in the editor.");                  // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("getAllNewFeatures " + allNewFeatures, new CurrentStackTrace()); // NOI18N
        }
        return allNewFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  currentObjectFeature  DOCUMENT ME!
     */
    public void setCurrentObjectFeature(final Feature currentObjectFeature) {
        this.currentObjectFeature = currentObjectFeature;
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
        refresh();
    }

    @Override
    public void featureCollectionChanged() {
        refresh();
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
        refresh();
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        refresh();
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        refresh();
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        refresh();
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        refresh();
    }
}
