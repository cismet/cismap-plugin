package de.cismet.cismap.cids.geometryeditor;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.navigatorplugin.CidsFeature;
import de.cismet.tools.CurrentStackTrace;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

// End of variables declaration
class CismapGeometryComboModel extends AbstractListModel implements ComboBoxModel,FeatureCollectionListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final DefaultCismapGeometryComboBoxEditor editor;
    private Object selectedItem = null;
    private Feature currentObjectFeature;
    private Vector<Feature> newFeaturesInMap;
    

    public CismapGeometryComboModel(DefaultCismapGeometryComboBoxEditor editor, Feature currentObjectFeature) {        
        this.editor = editor;
        log.debug("xxxlaa editor (con): "+editor);
        this.currentObjectFeature = currentObjectFeature;
        refresh();
    }

    /**
     *
     * Set the selected item. The implementation of this  method should notify
     * all registered <code>ListDataListener</code>s that the contents
     * have changed.
     *
     *
     * @param anItem the list object to select or <code>null</code>
     *        to clear the selection
     */
    public void setSelectedItem(Object anItem) {
        selectedItem = anItem;
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index) {
        if (currentObjectFeature != null) {//&&currentObjectFeature.getGeometry()!=null) {
            if (index == 0) {
                return currentObjectFeature;
            } else if (index == getSize() - 1) {
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
     *
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    public int getSize() {
        if (currentObjectFeature != null) {
            return newFeaturesInMap.size() + 2;
        } else {
            return newFeaturesInMap.size() + 1;
        }
    }

    /**
     *
     * Returns the selected item
     *
     * @return The selected item or <code>null</code> if there is no selection
     */
    public Object getSelectedItem() {

        return selectedItem;
    }

    public void refresh() {
        log.debug("xxxlaaa refreshing: "+editor.getCismap());
        newFeaturesInMap = getAllNewFeatures();
        try {
            this.fireContentsChanged(this, 0, getSize() - 1);
        } catch (Throwable t) {
            log.error("Fehler in refresh()", t);
        }
    }

    private Vector<Feature> getAllNewFeatures() {
        //Vector<Feature> allFeatures = CismapBroker.getInstance().getMappingComponent().getFeatureCollection().getAllFeatures();
        Vector<Feature> allNewFeatures = new Vector<Feature>();
        if (editor.getCismap() != null) {
            Vector<Feature> allFeatures = editor.getCismap().getMappingComponent().getFeatureCollection().getAllFeatures();

            for (Feature f : allFeatures) {
                if (f instanceof PureNewFeature){//||f instanceof CidsFeature) {
                    allNewFeatures.add(f);
                }
            }

        } else {
            log.error("cismap not found. No content in the editor.");
        }
        log.debug("getAllNewFeatures "+allNewFeatures,new CurrentStackTrace());
        return allNewFeatures;
    }

    public void setCurrentObjectFeature(Feature currentObjectFeature) {
        this.currentObjectFeature = currentObjectFeature;
    }


    public void allFeaturesRemoved(FeatureCollectionEvent fce) {
        refresh();
    }

    public void featureCollectionChanged() {
        refresh();
    }

    public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
        refresh();
    }

    public void featureSelectionChanged(FeatureCollectionEvent fce) {
        refresh();
    }

    public void featuresAdded(FeatureCollectionEvent fce) {
        refresh();
    }

    public void featuresChanged(FeatureCollectionEvent fce) {
        refresh();
    }

    public void featuresRemoved(FeatureCollectionEvent fce) {
        refresh();
    }



}