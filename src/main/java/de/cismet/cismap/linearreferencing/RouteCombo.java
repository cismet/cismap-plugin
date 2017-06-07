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
package de.cismet.cismap.linearreferencing;

import Sirius.server.middleware.types.MetaClass;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import java.awt.Component;

import java.lang.ref.SoftReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import static de.cismet.cismap.linearreferencing.LinearReferencingSingletonInstances.LOG;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RouteCombo extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static Map<XBoundingBox, SoftReference<List<Feature>>> cache =
        new HashMap<XBoundingBox, SoftReference<List<Feature>>>();

    //~ Instance fields --------------------------------------------------------

    private LinearReferencingHelper linearReferencingHelper = FeatureRegistry.getInstance()
                .getLinearReferencingSolver();
    private final String routeMetaClassName;
    private boolean routesComboInitialised = false;
    private String routeNamePropertyName;
    private String routeQuery;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbPossibleRoute;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form RouteCombo.
     *
     * @param  routeMetaClassName  DOCUMENT ME!
     * @param  value               DOCUMENT ME!
     */
    public RouteCombo(final String routeMetaClassName, final Object value) {
        initComponents();
        this.routeMetaClassName = routeMetaClassName;
        routeNamePropertyName = linearReferencingHelper.getRouteNamePropertyFromRouteByClassName(
                routeMetaClassName);
        AutoCompleteDecorator.decorate(cbPossibleRoute, new ObjectToStringConverter() {

                @Override
                public String getPreferredStringForItem(final Object o) {
                    if (o instanceof CidsLayerFeature) {
                        final Object prop = ((CidsLayerFeature)o).getProperty(routeNamePropertyName);

                        if (prop == null) {
                            return "";
                        } else {
                            return String.valueOf(prop);
                        }
                    } else {
                        if (o == null) {
                            return "";
                        } else {
                            return o.toString();
                        }
                    }
                }
            });
        cbPossibleRoute.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    Object newValue = value;

                    if (value instanceof CidsLayerFeature) {
                        final Object prop = ((CidsLayerFeature)value).getProperty(routeNamePropertyName);

                        if (prop != null) {
                            newValue = String.valueOf(prop);
                        }
                    } else {
                        if (value != null) {
                            newValue = value.toString();
                        } else {
                            newValue = " ";
                        }
                    }

                    return super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus);
                }
            });

        fillRoutesCombo(value);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the routeQuery
     */
    public String getRouteQuery() {
        return routeQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  routeQuery  the routeQuery to set
     */
    public void setRouteQuery(final String routeQuery) {
        this.routeQuery = routeQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void fillRoutesCombo(final Object value) {
        routesComboInitialised = true;
        cbPossibleRoute.setModel(new DefaultComboBoxModel(new Object[] { "Lade" }));
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                linearReferencingHelper.getDomainOfRouteTable(routeMetaClassName)[0],
                routeMetaClassName);
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
//        final XBoundingBox currentBoundingBox = (XBoundingBox)mc.getCurrentBoundingBoxFromCamera();
        final XBoundingBox currentBoundingBox = new XBoundingBox(1, 1, 2, 2, null, true);
        final SoftReference<List<Feature>> sr = cache.get(currentBoundingBox);
        List<Feature> featureList = null;

        if (sr != null) {
            featureList = sr.get();
        }

        if (featureList != null) {
            fillComboBox(featureList, value);
        } else {
            final CidsLayer layer = new CidsLayer(routeMc);
            try {
//                layer.setBoundingBox(currentBoundingBox);
                if (routeQuery != null) {
                    layer.setQuery(routeQuery);
                }
                layer.setBoundingBox(null);
                layer.addRetrievalListener(new RetrievalListener() {

                        @Override
                        public void retrievalStarted(final RetrievalEvent e) {
                        }

                        @Override
                        public void retrievalProgress(final RetrievalEvent e) {
                        }

                        @Override
                        public void retrievalComplete(final RetrievalEvent e) {
                            if (!e.isInitialisationEvent()) {
                                complete((List)e.getRetrievedObject());
                            }
                        }

                        @Override
                        public void retrievalAborted(final RetrievalEvent e) {
                            complete((List)e.getRetrievedObject());
                        }

                        @Override
                        public void retrievalError(final RetrievalEvent e) {
                            complete((List)e.getRetrievedObject());
                        }

                        private void complete(final List features) {
                            cache.clear();
                            cache.put(currentBoundingBox, new SoftReference<List<Feature>>(features));
                            fillComboBox(features, value);
                        }
                    });

                layer.retrieve(true);
            } catch (Exception e) {
                LOG.error("Error while retrieving features", e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  features    DOCUMENT ME!
     * @param  defaultVal  DOCUMENT ME!
     */
    private void fillComboBox(final List features, final Object defaultVal) {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                linearReferencingHelper.getDomainOfRouteTable(routeMetaClassName)[0],
                routeMetaClassName);
        final List<Feature> routes = new ArrayList<Feature>();
        boolean hasSelectedItem = (defaultVal == null);
        boolean addSelectedItem = (defaultVal != null);
        routes.add(null);

        if (features instanceof List) {
            final List fl = (List)features;

            for (final Object o : fl) {
                if (!hasSelectedItem && (o instanceof CidsLayerFeature)
                            && ((CidsLayerFeature)o).getProperty(routeNamePropertyName).equals(defaultVal)) {
                    hasSelectedItem = true;
                    addSelectedItem = false;
                }
                routes.add((Feature)o);
            }
        }

        if (addSelectedItem) {
            if (defaultVal instanceof CidsLayerFeature) {
                routes.add((Feature)defaultVal);
            } else {
                routes.add(retrieveFeature(routeNamePropertyName, String.valueOf(defaultVal), routeMc));
            }
        }
        Collections.sort(routes, new Comparator<Feature>() {

                @Override
                public int compare(final Feature o1, final Feature o2) {
                    return featureToString(o1).compareTo(featureToString(o2));
                }
            });
        cbPossibleRoute.setModel(new DefaultComboBoxModel(routes.toArray()));

        if (defaultVal != null) {
            for (final Feature f : routes) {
                if (f instanceof CidsLayerFeature) {
                    if (((CidsLayerFeature)f).getProperty(routeNamePropertyName).equals(defaultVal)) {
                        cbPossibleRoute.setSelectedItem(f);
                        break;
                    }
                }
            }
        } else {
            cbPossibleRoute.setSelectedItem(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   property       DOCUMENT ME!
     * @param   propertyValue  DOCUMENT ME!
     * @param   mc             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureServiceFeature retrieveFeature(final String property,
            final String propertyValue,
            final MetaClass mc) {
        try {
            final CidsLayer service = new CidsLayer(mc);
            final String query = service.decoratePropertyName(property) + " = "
                        + service.decoratePropertyValue(property, propertyValue);
            service.initAndWait();
            final List<FeatureServiceFeature> features = service.getFeatureFactory()
                        .createFeatures(query, null, null, 0, 1, null);

            if (features.size() == 1) {
                return features.get(0);
            }
        } catch (Exception e) {
            LOG.error("Error while reloading feature from server", e);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String featureToString(final Object value) {
        if (value instanceof CidsLayerFeature) {
            final Object prop = ((CidsLayerFeature)value).getProperty(routeNamePropertyName);

            if (prop != null) {
                return String.valueOf(prop);
            }
        } else {
            if (value != null) {
                return value.toString();
            } else {
                return " ";
            }
        }

        return String.valueOf(value);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Feature> getPossibleRoutes() {
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> featureList = sl.getAllSelectedPFeatures();
        final List<Feature> possibleRoutes = new ArrayList<Feature>();
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                linearReferencingHelper.getDomainOfRouteTable(routeMetaClassName)[0],
                routeMetaClassName);

        for (final PFeature f : featureList) {
            final Feature selectedFeature = f.getFeature();

            if (selectedFeature instanceof CidsLayerFeature) {
                final CidsLayerFeature clFeature = (CidsLayerFeature)selectedFeature;

                if (routeMc.equals(clFeature.getBean().getMetaObject().getMetaClass())) {
                    possibleRoutes.add(selectedFeature);
                }
            }
        }

        return possibleRoutes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getValue() {
        return cbPossibleRoute.getSelectedItem();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        final java.awt.GridBagConstraints gridBagConstraints;

        cbPossibleRoute = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        cbPossibleRoute.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPossibleRoute.setPreferredSize(new java.awt.Dimension(300, 20));
        cbPossibleRoute.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbPossibleRouteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(cbPossibleRoute, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbPossibleRouteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbPossibleRouteActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cbPossibleRouteActionPerformed
}
