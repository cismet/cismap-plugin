/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.plugin.listener.MetaNodeSelectionListener;
import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
import Sirius.navigator.types.treenode.ObjectTreeNode;
import Sirius.navigator.ui.ComponentRegistry;
import Sirius.navigator.ui.ShowObjectsInGuiMethod;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;

import org.apache.log4j.Logger;

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionAdapter;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureGroup;
import de.cismet.cismap.commons.features.FeatureGroups;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.ext.CExtContext;
import de.cismet.ext.CExtManager;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   $author$
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ShowObjectsInGuiMethod.class)
public class ShowObjectsMethod implements ShowObjectsInGuiMethod {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShowObjectsMethod.class);

    //~ Instance fields --------------------------------------------------------

    private ShowObjectsWaitDialog showObjectsWaitDialog;
    private final Map<DefaultMetaTreeNode, Feature> featuresInMap = new HashMap<DefaultMetaTreeNode, Feature>();
    private final Map<Feature, DefaultMetaTreeNode> featuresInMapReverse = new HashMap<Feature, DefaultMetaTreeNode>();
    private MappingComponent mapC;
    private boolean nodeSelectionEventBlocker = false;
    private boolean featureCollectionEventBlocker = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShowObjectsMethod object.
     */
    public ShowObjectsMethod() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void init() {
        mapC = CismapBroker.getInstance().getMappingComponent();
        mapC.getFeatureCollection().addFeatureCollectionListener(new CatalogueFeatureCollectionListener());
        final NodeChangeListener nodeChangeListener = new NodeChangeListener();

        if ((ComponentRegistry.getRegistry().getCatalogueTree() != null)
                    && (ComponentRegistry.getRegistry().getSearchResultsTree() != null)) {
            ComponentRegistry.getRegistry().getCatalogueTree().addTreeSelectionListener(nodeChangeListener);
            ComponentRegistry.getRegistry().getSearchResultsTree().addTreeSelectionListener(nodeChangeListener);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void invoke() throws Exception {
        final Collection selectedNodes = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNodes();
        invoke(selectedNodes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodes  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void invoke(final Collection nodes) throws Exception {
        invoke(nodes, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mo        DOCUMENT ME!
     * @param   editable  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public synchronized CidsFeature invoke(final MetaObject mo, final boolean editable) throws Exception {
        final CidsFeature cidsFeature = new CidsFeature(mo);
        invoke(cidsFeature, editable);

        return cidsFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsFeature  DOCUMENT ME!
     * @param   editable     DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void invoke(final CidsFeature cidsFeature, final boolean editable) throws Exception {
        final List<Feature> v = new ArrayList<Feature>();
        cidsFeature.setEditable(editable);
        v.add(cidsFeature);
        if (LOG.isDebugEnabled()) {
            LOG.debug("mapC.getFeatureCollection().getAllFeatures():" // NOI18N
                        + mapC.getFeatureCollection().getAllFeatures());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("cidsFeature:" + cidsFeature);            // NOI18N
            LOG.debug("mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature):"
                        + mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature)); // NOI18N
        }
        mapC.getFeatureLayer().setVisible(true);
        mapC.getFeatureCollection().removeFeature(cidsFeature);
        if (LOG.isDebugEnabled()) {
            LOG.debug("mapC.getFeatureCollection().getAllFeatures():" // NOI18N
                        + mapC.getFeatureCollection().getAllFeatures());
        }

        mapC.getFeatureCollection().substituteFeatures(v);

        if (editable) {
            mapC.getFeatureCollection().select(v);
        }

        if (!mapC.isFixedMapExtent()) {
            mapC.zoomToFeatureCollection(mapC.isFixedMapScale());
            mapC.showHandles(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node      DOCUMENT ME!
     * @param   oAttr     DOCUMENT ME!
     * @param   editable  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public synchronized CidsFeature invoke(final DefaultMetaTreeNode node,
            final ObjectAttribute oAttr,
            final boolean editable) throws Exception {
        final MetaObject loader = ((ObjectTreeNode)node).getMetaObject();
        final MetaObjectNode mon = ((ObjectTreeNode)node).getMetaObjectNode();
        CidsFeature cidsFeature = invoke(loader, editable);

        if (oAttr != null) {
            cidsFeature = new CidsFeature(mon, oAttr);
        } else {
            cidsFeature = new CidsFeature(mon);
        }

        featuresInMap.put(node, cidsFeature);
        featuresInMapReverse.put(cidsFeature, node);
        invoke(cidsFeature, editable);

        return cidsFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodes     DOCUMENT ME!
     * @param   editable  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public synchronized void invoke(final Collection<DefaultMetaTreeNode> nodes, final boolean editable)
            throws Exception {
        LOG.info("invoke shows objects in the map"); // NOI18N

        final Runnable showWaitRunnable = new Runnable() {

                @Override
                public void run() {
                    if (showObjectsWaitDialog == null) {
                        showObjectsWaitDialog = new ShowObjectsWaitDialog(ComponentRegistry.getRegistry()
                                        .getMainWindow(),
                                false);
                    }
                    StaticSwingTools.showDialog(showObjectsWaitDialog);

                    final SwingWorker<List<Feature>, Void> addToMapWorker = new SwingWorker<List<Feature>, Void>() {

                            private Map<DefaultMetaTreeNode, Feature> tmpFeaturesInMap = null;
                            private Map<Feature, DefaultMetaTreeNode> tmpFeaturesInMapReverse = null;

                            @Override
                            protected List<Feature> doInBackground() throws Exception {
                                Thread.currentThread().setName("ShowObjectsMethod addToMapWorker");
                                final Iterator<DefaultMetaTreeNode> mapIter = featuresInMap.keySet().iterator();

                                while (mapIter.hasNext()) {
                                    final DefaultMetaTreeNode node = mapIter.next();
                                    final Feature f = featuresInMap.get(node);

                                    if (!mapC.getFeatureCollection().isHoldFeature(f)) {
                                        mapIter.remove();
                                        featuresInMapReverse.remove(f);
                                    }
                                }

                                final List<Feature> features = new ArrayList<Feature>();

                                for (final DefaultMetaTreeNode node : nodes) {
                                    final MetaObjectNode mon = ((ObjectTreeNode)node).getMetaObjectNode();
                                    // TODO: Check4CashedGeomAndLightweightJson
                                    MetaObject mo = mon.getObject();

                                    if (mo == null) {
                                        mo = ((ObjectTreeNode)node).getMetaObject();
                                    }

                                    final CExtContext context = new CExtContext(
                                            CExtContext.CTX_REFERENCE,
                                            mo.getBean());
                                    // there always is a default
                                    final MapVisualisationProvider mvp = CExtManager.getInstance()
                                                .getExtension(MapVisualisationProvider.class, context);

                                    final Feature feature = mvp.getFeature(mo.getBean());
                                    if (feature == null) {
                                        // no map visualisation available, ignore
                                        continue;
                                    }

                                    feature.setEditable(editable);

                                    final List<Feature> allFeaturesToAdd;
                                    if (feature instanceof FeatureGroup) {
                                        final FeatureGroup fg = (FeatureGroup)feature;
                                        allFeaturesToAdd = new ArrayList<Feature>(FeatureGroups.expandAll(fg));
                                    } else {
                                        allFeaturesToAdd = Arrays.asList(feature);
                                    }

                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("allFeaturesToAdd:" + allFeaturesToAdd); // NOI18N
                                    }

                                    if (!(featuresInMap.containsValue(feature))) {
                                        features.addAll(allFeaturesToAdd);

                                        // node -> masterfeature
                                        featuresInMap.put(node, feature);

                                        for (final Feature f : allFeaturesToAdd) {
                                            // master and all subfeatures -> node
                                            featuresInMapReverse.put(f, node);
                                        }
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug("featuresInMap.put(node,cidsFeature):" + node + "," // NOI18Ns
                                                        + feature);
                                        }
                                    }
                                }
                                tmpFeaturesInMap = new HashMap<DefaultMetaTreeNode, Feature>(featuresInMap);
                                tmpFeaturesInMapReverse = new HashMap<Feature, DefaultMetaTreeNode>(
                                        featuresInMapReverse);

                                return features;
                            }

                            @Override
                            protected void done() {
                                try {
                                    showObjectsWaitDialog.setVisible(false);
                                    final List<Feature> features = get();

                                    mapC.getFeatureLayer().setVisible(true);
                                    mapC.getFeatureCollection().substituteFeatures(features);
                                    featuresInMap.clear();
                                    featuresInMap.putAll(tmpFeaturesInMap);
                                    featuresInMapReverse.clear();
                                    featuresInMapReverse.putAll(tmpFeaturesInMapReverse);

                                    if (!mapC.isFixedMapExtent()) {
                                        mapC.zoomToFeatureCollection(mapC.isFixedMapScale());
                                    }
                                } catch (final InterruptedException e) {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(e, e);
                                    }
                                } catch (final Exception e) {
                                    LOG.error("Error while displaying objects:", e); // NOI18N
                                }
                            }
                        };
                    CismetThreadPool.execute(addToMapWorker);
                }
            };

        if (EventQueue.isDispatchThread()) {
            showWaitRunnable.run();
        } else {
            EventQueue.invokeLater(showWaitRunnable);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getId() {
        return this.getClass().getName();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CatalogueFeatureCollectionListener extends FeatureCollectionAdapter {

        //~ Methods ------------------------------------------------------------

        @Override
        public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
            for (final Feature feature : fce.getEventFeatures()) {
                final DefaultMetaTreeNode node = featuresInMapReverse.get(feature);
                if (node != null) {
                    featuresInMapReverse.remove(feature);
                    featuresInMap.remove(node);
                }
            }
        }

        @Override
        public void featuresRemoved(final FeatureCollectionEvent fce) {
            featuresInMap.clear();
            featuresInMapReverse.clear();
        }

        @Override
        public void featureSelectionChanged(final FeatureCollectionEvent fce) {
            if (!featureCollectionEventBlocker) {
                final Collection<Feature> fc = new ArrayList<Feature>(mapC.getFeatureCollection()
                                .getSelectedFeatures());
                final List<DefaultMutableTreeNode> nodeVector = new ArrayList<DefaultMutableTreeNode>();

                for (final Feature f : fc) {
                    if ((f instanceof CidsFeature) || (f instanceof FeatureGroup)) {
                        nodeVector.add(featuresInMapReverse.get(f));
                    }
                }

                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            nodeSelectionEventBlocker = true;
                            // Baumselektion wird hier propagiert
                            if (ComponentRegistry.getRegistry().getActiveCatalogue() != null) {
                                ComponentRegistry.getRegistry().getActiveCatalogue().setSelectedNodes(nodeVector, true);
                            }
                            nodeSelectionEventBlocker = false;
                        }
                    });
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author   $author$
     * @version  $Revision$, $Date$
     */
    private class NodeChangeListener extends MetaNodeSelectionListener {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NodeChangeListener object.
         */
        private NodeChangeListener() {
        }

        //~ Methods ------------------------------------------------------------

        // TODO: WTF?
        /**
         * DOCUMENT ME!
         *
         * @param  wirdNichtGebrauchtWeilScheissevonPascalgefuelltCollection  DOCUMENT ME!
         */
        @Override
        protected void nodeSelectionChanged(
                final Collection wirdNichtGebrauchtWeilScheissevonPascalgefuelltCollection) {
            if (!nodeSelectionEventBlocker) {
                try {
                    final Collection c = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNodes();

                    if ((c != null) && !(c.isEmpty())) {
                        final Object[] nodes = c.toArray();
                        boolean oneHit = false;
                        final List<Feature> features = new ArrayList<Feature>();

                        for (final Object o : nodes) {
                            if (o instanceof DefaultMetaTreeNode) {
                                final DefaultMetaTreeNode node = (DefaultMetaTreeNode)o;

                                if (featuresInMap.containsKey(node)) {
                                    oneHit = true;
                                    features.add(featuresInMap.get(node));
                                }
                            }
                        }

                        if (oneHit) {
                            featureCollectionEventBlocker = true;
                            mapC.getFeatureCollection().select(features);
                            featureCollectionEventBlocker = false;
                        } else {
                            featureCollectionEventBlocker = true;
                            mapC.getFeatureCollection().unselectAll();
                            featureCollectionEventBlocker = false;
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("featuresInMap:" + featuresInMap); // NOI18N
                            }
                        }
                    }
                } catch (final Exception t) {
                    LOG.error("Error in WizardMode:", t);                    // NOI18N
                }
            }
        }
    }
}
