/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.cidslayer;

import Sirius.navigator.tools.MetaObjectChangeEvent;
import Sirius.navigator.tools.MetaObjectChangeListener;
import Sirius.navigator.tools.MetaObjectChangeSupport;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaClassStore;

import org.apache.log4j.Logger;

import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.Validator;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.Component;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingWorker;

import de.cismet.cids.editors.Bindable;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

import de.cismet.commons.ref.PurgingCache;

import de.cismet.tools.Calculator;
import de.cismet.tools.CismetThreadPool;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DefaultCidsLayerBindableReferenceCombo extends JComboBox implements Bindable,
    MetaClassStore,
    Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultCidsLayerBindableReferenceCombo.class);

    protected static final Comparator<CidsLayerFeature> BEAN_TOSTRING_COMPARATOR = new FeatureToStringComparator();
    private static final PurgingCache<CatalogueDefinition, List<CidsLayerFeature>> cache =
        new PurgingCache<CatalogueDefinition, List<CidsLayerFeature>>(
            new Calculator<CatalogueDefinition, List<CidsLayerFeature>>() {

                @Override
                public List<CidsLayerFeature> calculate(final CatalogueDefinition input) throws Exception {
                    final ClassAttribute ca = input.getMc().getClassAttribute("sortingColumn"); // NOI18N
                    final CidsLayer layer = new CidsLayer(input.getMc());
                    layer.initAndWait();
                    final Map<String, FeatureServiceAttribute> attributeMap = layer.getFeatureServiceAttributes();
                    List<CidsLayerFeature> featureList = new ArrayList<CidsLayerFeature>();
                    FeatureServiceAttribute[] attrArray = null;
                    String[] orderBy = null;                                                    // NOI18N

                    // determine sort order
                    if (input.getSortingColumn() == null) {
                        if (ca != null) {
                            final String value = ca.getValue().toString();
                            orderBy = value.split(",");
                        }
                    } else {
                        orderBy = input.getSortingColumn().split(",");
                    }

                    if ((orderBy != null) && (orderBy.length > 0)) {
                        final List<FeatureServiceAttribute> attributeList = new ArrayList<FeatureServiceAttribute>();

                        for (final String attributeName : orderBy) {
                            final FeatureServiceAttribute attr = attributeMap.get(attributeName);

                            if (attr != null) {
                                attributeList.add(attr);
                            }
                        }

                        if (!attributeList.isEmpty()) {
                            attrArray = attributeList.toArray(new FeatureServiceAttribute[attributeList.size()]);
                        }
                    }

                    // load data
                    try {
                        featureList = layer.getFeatureFactory()
                                        .createFeatures(layer.getQuery(), null, null, 0, 0, attrArray);
                    } catch (final Exception ex) {
                        LOG.warn("cache could not come up with appropriate objects", ex); // NOI18N
                        throw ex;
                    }

                    // add null value, if required
                    if (input.isNullable()) {
                        final List<CidsLayerFeature> tmpList = new ArrayList<CidsLayerFeature>();
                        tmpList.add(null);
                        tmpList.addAll(featureList);
                        featureList = tmpList;
                    }

                    return featureList;
                }
            },
            300000L,
            3000000);

    //~ Instance fields --------------------------------------------------------

    protected CidsLayerFeature cidsBean;

    private final transient MetaObjectChangeListener mocL;
    private MetaClass metaClass;
    private boolean fakeModel;
    private boolean nullable;
    private Comparator<CidsLayerFeature> comparator;
    private String nullValueRepresentation;
    private String sortingColumn;
    private CidsLayerFeatureFilter beanFilter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     */
    public DefaultCidsLayerBindableReferenceCombo() {
        this(null, false, BEAN_TOSTRING_COMPARATOR);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  nullable  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final boolean nullable) {
        this(null, nullable, BEAN_TOSTRING_COMPARATOR);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  comparator  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final Comparator<CidsLayerFeature> comparator) {
        this(null, false, comparator);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final MetaClass mc) {
        this(mc, false, BEAN_TOSTRING_COMPARATOR);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  mc             DOCUMENT ME!
     * @param  sortingcolumn  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final MetaClass mc, final String sortingcolumn) {
        this(mc, false, BEAN_TOSTRING_COMPARATOR, sortingcolumn);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  mc        DOCUMENT ME!
     * @param  nullable  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final MetaClass mc, final boolean nullable) {
        this(mc, nullable, BEAN_TOSTRING_COMPARATOR);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  mc          DOCUMENT ME!
     * @param  nullable    DOCUMENT ME!
     * @param  comparator  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final MetaClass mc,
            final boolean nullable,
            final Comparator<CidsLayerFeature> comparator) {
        this(mc, nullable, BEAN_TOSTRING_COMPARATOR, null);
    }

    /**
     * Creates a new DefaultBindableReferenceCombo object.
     *
     * @param  mc             DOCUMENT ME!
     * @param  nullable       DOCUMENT ME!
     * @param  comparator     DOCUMENT ME!
     * @param  sortingColumn  DOCUMENT ME!
     */
    public DefaultCidsLayerBindableReferenceCombo(final MetaClass mc,
            final boolean nullable,
            final Comparator<CidsLayerFeature> comparator,
            final String sortingColumn) {
        final String[] s = new String[] {
                NbBundle.getMessage(
                    DefaultCidsLayerBindableReferenceCombo.class,
                    "DefaultCidsLayerBindableReferenceCombo.loading")
            };
        setModel(new DefaultComboBoxModel(s));

        this.nullable = nullable;
        this.comparator = comparator;
        this.nullValueRepresentation = " ";
        this.sortingColumn = sortingColumn;
        this.mocL = new MetaObjectChangeListenerImpl();
        this.metaClass = mc;

        this.setRenderer(new DefaultBindableReferenceComboRenderer());

        init(mc, false);

        final MetaObjectChangeSupport mocSupport = MetaObjectChangeSupport.getDefault();
        mocSupport.addMetaObjectChangeListener(WeakListeners.create(MetaObjectChangeListener.class, mocL, mocSupport));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  sortingColumn  DOCUMENT ME!
     */
    public void setSortingColumn(final String sortingColumn) {
        this.sortingColumn = sortingColumn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSortingColumn() {
        return sortingColumn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mc           DOCUMENT ME!
     * @param  forceReload  DOCUMENT ME!
     */
    protected void init(final MetaClass mc, final boolean forceReload) {
        if (!isFakeModel() && (mc != null)) {
            CismetThreadPool.execute(new SwingWorker<DefaultComboBoxModel, Void>() {

                    @Override
                    protected DefaultComboBoxModel doInBackground() throws Exception {
                        Thread.currentThread().setName("DefaultBindableReferenceCombo init()");
                        return getModelByMetaClass(mc, nullable, comparator, forceReload, sortingColumn);
                    }

                    @Override
                    protected void done() {
                        try {
                            final DefaultComboBoxModel tmp = get();
                            tmp.setSelectedItem(cidsBean);
                            setModel(tmp);
                        } catch (InterruptedException interruptedException) {
                        } catch (ExecutionException executionException) {
                            LOG.error("Error while initializing the model of a referenceCombo", executionException); // NOI18N
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   forceReload  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public void reload(final boolean forceReload) {
        if (metaClass == null) {
            throw new IllegalStateException("the metaclass has not been set yet"); // NOI18N
        }

        init(metaClass, forceReload);
    }

    @Override
    public String getBindingProperty() {
        return "selectedItem"; // NOI18N
    }

    @Override
    public Validator getValidator() {
        return null;
    }

    @Override
    public Converter getConverter() {
        return null;
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if (isFakeModel()) {
            setModel(new DefaultComboBoxModel(new Object[] { anObject }));
        }
        if (!(anObject instanceof CidsLayerFeature)) {
            anObject = null;
        }
        super.setSelectedItem(anObject);
        cidsBean = (CidsLayerFeature)anObject;
    }

    @Override
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        this.metaClass = metaClass;
        init(metaClass, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFakeModel() {
        return fakeModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fakeModel  DOCUMENT ME!
     */
    public void setFakeModel(final boolean fakeModel) {
        this.fakeModel = fakeModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getNullValueRepresentation() {
        return nullValueRepresentation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nullValueRepresentation  DOCUMENT ME!
     */
    public void setNullValueRepresentation(final String nullValueRepresentation) {
        this.nullValueRepresentation = nullValueRepresentation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nullable  DOCUMENT ME!
     */
    public void setNullable(final boolean nullable) {
        if (this.nullable != nullable) {
            this.nullable = nullable;

            if (metaClass != null) {
                init(metaClass, false);
            }
        }
    }

    @Override
    public Object getNullSourceValue() {
        return null;
    }

    @Override
    public Object getErrorSourceValue() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Comparator<CidsLayerFeature> getComparator() {
        return comparator;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc           DOCUMENT ME!
     * @param   nullable     DOCUMENT ME!
     * @param   onlyUsed     DOCUMENT ME!
     * @param   comparator   DOCUMENT ME!
     * @param   forceReload  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
            final boolean nullable,
            final boolean onlyUsed,
            final Comparator<CidsLayerFeature> comparator,
            final boolean forceReload) {
        return getModelByMetaClass(mc, nullable, comparator, forceReload, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc             DOCUMENT ME!
     * @param   nullable       DOCUMENT ME!
     * @param   comparator     DOCUMENT ME!
     * @param   forceReload    DOCUMENT ME!
     * @param   sortingColumn  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
            final boolean nullable,
            final Comparator<CidsLayerFeature> comparator,
            final boolean forceReload,
            final String sortingColumn) {
        if (mc != null) {
            if (forceReload) {
                cache.clear();
            }

            final List<CidsLayerFeature> featureList = cache.get(new CatalogueDefinition(mc, nullable, sortingColumn));

            if ((sortingColumn == null) && (featureList != null) && (mc.getClassAttribute("sortingColumn") == null)) {
                // Sorts the model using String comparison on the bean's toString()
                Collections.sort(featureList, comparator);
            }

            return new DefaultComboBoxModel(featureList.toArray());
        } else {
            return new DefaultComboBoxModel();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc          DOCUMENT ME!
     * @param   nullable    DOCUMENT ME!
     * @param   onlyUsed    DOCUMENT ME!
     * @param   comparator  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
            final boolean nullable,
            final boolean onlyUsed,
            final Comparator<CidsLayerFeature> comparator) throws Exception {
        return getModelByMetaClass(mc, nullable, onlyUsed, comparator, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc        DOCUMENT ME!
     * @param   nullable  DOCUMENT ME!
     * @param   onlyUsed  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
            final boolean nullable,
            final boolean onlyUsed) throws Exception {
        return getModelByMetaClass(mc, nullable, onlyUsed, BEAN_TOSTRING_COMPARATOR, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mc        DOCUMENT ME!
     * @param   nullable  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc, final boolean nullable)
            throws Exception {
        return getModelByMetaClass(mc, nullable, false, BEAN_TOSTRING_COMPARATOR, false);
    }

    /**
     * The beanFilter is a filter that can be used to choose, which beans should be shown.
     *
     * @return  the beanFilter
     */
    public CidsLayerFeatureFilter getBeanFilter() {
        return beanFilter;
    }

    /**
     * Set a filter that can be used to choose, which beans should be shown.
     *
     * @param  beanFilter  the beanFilter to set
     */
    public synchronized void setBeanFilter(final CidsLayerFeatureFilter beanFilter) {
        this.beanFilter = beanFilter;

        final ComboBoxModel model = getModel();

        if (model != null) {
            setModel(model);
        }
    }

    @Override
    public synchronized void setModel(final ComboBoxModel aModel) {
        if (beanFilter == null) {
            super.setModel(aModel);
        } else {
            super.setModel(filterModel(aModel));
        }
    }

    /**
     * Use the bean filter to filter the given data model.
     *
     * @param   aModel  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ComboBoxModel filterModel(final ComboBoxModel aModel) {
        final List<CidsLayerFeature> beanList = new ArrayList<CidsLayerFeature>();

        if (beanFilter == null) {
            return aModel;
        }

        for (int i = 0; i < aModel.getSize(); ++i) {
            if ((aModel.getElementAt(i) instanceof CidsLayerFeature) || (aModel.getElementAt(i) == null)) {
                final CidsLayerFeature bean = (CidsLayerFeature)aModel.getElementAt(i);

                if (beanFilter.accept(bean)) {
                    beanList.add(bean);
                }
            }
        }

        final DefaultComboBoxModel model = new DefaultComboBoxModel<CidsLayerFeature>(beanList.toArray(
                    new CidsLayerFeature[beanList.size()]));
        model.setSelectedItem(aModel.getSelectedItem());

        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mc             DOCUMENT ME!
     * @param  nullable       DOCUMENT ME!
     * @param  sortingColumn  DOCUMENT ME!
     */
    public static void preloadData(final MetaClass mc, final boolean nullable, final String sortingColumn) {
        cache.get(new CatalogueDefinition(mc, nullable, sortingColumn));
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static final class FeatureToStringComparator implements Comparator<CidsLayerFeature> {

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final CidsLayerFeature o1, final CidsLayerFeature o2) {
            final String s1 = (o1 == null) ? "" : o1.toString(); // NOI18N
            final String s2 = (o2 == null) ? "" : o2.toString(); // NOI18N

            return (s1).compareToIgnoreCase(s2);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class MetaObjectChangeListenerImpl implements MetaObjectChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void metaObjectAdded(final MetaObjectChangeEvent moce) {
            // we're only registered to the DefaultMetaObjectChangeSupport that asserts of proper initialisation of
            // events
            if ((metaClass != null) && metaClass.equals(moce.getNewMetaObject().getMetaClass())) {
                init(metaClass, true);
            }
        }

        @Override
        public void metaObjectChanged(final MetaObjectChangeEvent moce) {
            // we're only registered to the DefaultMetaObjectChangeSupport that asserts of proper initialisation of
            // events
            if ((metaClass != null) && metaClass.equals(moce.getNewMetaObject().getMetaClass())) {
                init(metaClass, true);
            }
        }

        @Override
        public void metaObjectRemoved(final MetaObjectChangeEvent moce) {
            // we're only registered to the DefaultMetaObjectChangeSupport that asserts of proper initialisation of
            // events
            if ((metaClass != null) && metaClass.equals(moce.getOldMetaObject().getMetaClass())) {
                init(metaClass, true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DefaultBindableReferenceComboRenderer extends DefaultListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final Component ret = super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);
            if ((value == null) && (ret instanceof JLabel)) {
                ((JLabel)ret).setText(getNullValueRepresentation());
            }

            return ret;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class CatalogueDefinition {

        //~ Instance fields ----------------------------------------------------

        private final MetaClass mc;
        private final boolean nullable;
        private final String sortingColumn;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CatalogueDefinition object.
         *
         * @param  mc             DOCUMENT ME!
         * @param  nullable       DOCUMENT ME!
         * @param  sortingColumn  DOCUMENT ME!
         */
        public CatalogueDefinition(final MetaClass mc, final boolean nullable, final String sortingColumn) {
            this.mc = mc;
            this.nullable = nullable;
            this.sortingColumn = sortingColumn;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the mc
         */
        public MetaClass getMc() {
            return mc;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the nullable
         */
        public boolean isNullable() {
            return nullable;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getSortingColumn() {
            return sortingColumn;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof CatalogueDefinition) {
                final CatalogueDefinition other = (CatalogueDefinition)obj;
                final String otherSort = (other.getSortingColumn() == null) ? "" : other.getSortingColumn();
                final String sort = (getSortingColumn() == null) ? "" : getSortingColumn();

                return otherSort.equals(sort) && other.getMc().equals(mc) && (other.isNullable() == nullable);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = (29 * hash) + ((this.mc != null) ? this.mc.hashCode() : 0);
            hash = (29 * hash) + (this.nullable ? 1 : 0);
            hash = (29 * hash) + ((this.sortingColumn != null) ? this.sortingColumn.hashCode() : 0);
            return hash;
        }
    }
}
