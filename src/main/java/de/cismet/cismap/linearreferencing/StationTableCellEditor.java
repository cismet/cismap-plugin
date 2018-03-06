/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.linearreferencing;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.Container;

import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;

import de.cismet.cismap.linearreferencing.tools.StationTableCellEditorInterface;

import de.cismet.connectioncontext.ClientConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = StationTableCellEditorInterface.class)
public class StationTableCellEditor extends AbstractCellEditor implements StationTableCellEditorInterface,
    ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(StationTableCellEditor.class);

    //~ Instance fields --------------------------------------------------------

    private TableStationEditor stat;
    private String columnName;
    private List<LinearReferencingInfo> linRefInfos;
    private LinearReferencingHelper linHelper = FeatureRegistry.getInstance().getLinearReferencingSolver();

    private final ClientConnectionContext connectionContext;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationTableCellEditor object. If this constructor is used, the method
     * {@link #setColumnName(java.lang.String) } must be invoked to set the column name, this editor is used on
     */
    public StationTableCellEditor() {
        this(null, ClientConnectionContext.createDeprecated());
    }

    /**
     * Creates a new StationTableCellEditor object.
     *
     * @param  columnName         DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    public StationTableCellEditor(final String columnName, final ClientConnectionContext connectionContext) {
        this.columnName = columnName;
        this.connectionContext = connectionContext;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTableCellEditorComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column) {
        Container o = table;

        while ((o.getParent() != null)
                    && (!(o.getParent() instanceof AttributeTable) && !(o.getParent() instanceof FeatureInfoPanel))) {
            o = o.getParent();
        }

        if (o.getParent() instanceof AttributeTable) {
            final AttributeTable tab = (AttributeTable)o.getParent();
            final FeatureServiceFeature feature = tab.getFeatureByRow(row);

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                stat = cidsFeature.getStationEditor(getColumnName());
            } else if (feature instanceof JDBCFeature) {
                final JDBCFeature f = (JDBCFeature)feature;
                final TableStationEditor editor = (TableStationEditor)f.getStationEditor(getColumnName());

                if (editor != null) {
                    stat = editor;
                } else {
                    stat = createEditor(f);
                }
            }
        } else if (o.getParent() instanceof FeatureInfoPanel) {
            final FeatureInfoPanel infoPanel = (FeatureInfoPanel)o.getParent();
            final FeatureServiceFeature feature = infoPanel.getSelectedFeature();

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                stat = cidsFeature.getStationEditor(getColumnName());
            }
        }

        stat.requestFocus();
        return stat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private TableStationEditor createEditor(final JDBCFeature feature) {
        final LinearReferencingInfo info = getInfoForColumn(columnName);
        final MetaClass metaClass = ClassCacheMultiple.getMetaClass(info.getDomain(), info.getLinRefReferenceName());
        if (metaClass != null) {
            try {
                final String query = "SELECT %s, %s FROM %s WHERE %s = '%s';";
                final String routeQuery = String.format(
                        query,
                        metaClass.getID(),
                        metaClass.getPrimaryKey(),
                        metaClass.getTableName(),
                        info.getTrgLinRefJoinField(),
                        feature.getProperty(info.getSrcLinRefJoinField()));
                final MetaObject[] mos = SessionManager.getProxy()
                            .getMetaObjectByQuery(SessionManager.getSession().getUser(),
                                routeQuery,
                                getConnectionContext());

                if ((mos != null) && (mos.length == 1)) {
                    final MetaObject routeObject = mos[0];

                    if ((info.getTillField() == null) || info.getTillField().isEmpty()) {
                        // create station
                        final CidsBean stationBean = linHelper.createStationBeanFromRouteBean(routeObject.getBean(),
                                (Double)feature.getProperty(info.getFromField()));
                        final TableStationEditor editor = new TableStationEditor(info.getLinRefReferenceName(),
                                feature,
                                info.getSrcLinRefJoinField());
                        editor.setCidsBean(stationBean);
                        editor.addPropertyChangeListener(feature.getPropertyChangeListener());
                        feature.setStationEditor(getColumnName(), editor);

                        return editor;
                    } else {
                        // create line
                        final CidsBean lineBean = linHelper.createLineBeanFromRouteBean(routeObject.getBean());
                        linHelper.setGeometryToLineBean(feature.getGeometry(), lineBean);
                        final CidsBean fromStation = linHelper.getStationBeanFromLineBean(lineBean, true);
                        linHelper.setLinearValueToStationBean((Double)feature.getProperty(info.getFromField()),
                            fromStation);
                        final CidsBean toStation = linHelper.getStationBeanFromLineBean(lineBean, false);
                        linHelper.setLinearValueToStationBean((Double)feature.getProperty(info.getTillField()),
                            toStation);

                        final TableLinearReferencedLineEditor st = new TableLinearReferencedLineEditor(
                                info.getLinRefReferenceName(),
                                feature,
                                info.getSrcLinRefJoinField());
                        st.setCidsBean(lineBean);

                        final TableStationEditor fromEditor = st.getFromStation();
                        final TableStationEditor toEditor = st.getToStation();

                        st.addPropertyChangeListener(feature.getPropertyChangeListener());
                        feature.setBackgroundColor(st.getLineColor());

                        feature.setStationEditor(info.getGeomField(), st);
                        feature.setStationEditor(info.getFromField(), fromEditor);
                        feature.setStationEditor(info.getTillField(), toEditor);

                        if (getColumnName().equalsIgnoreCase(info.getFromField())) {
                            return fromEditor;
                        } else {
                            return toEditor;
                        }
                    }
                }
            } catch (Exception ex) {
                LOG.error("Error while creating station bean", ex);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   colName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LinearReferencingInfo getInfoForColumn(final String colName) {
        for (final LinearReferencingInfo info : linRefInfos) {
            if (info.getFromField().equals(colName)
                        || ((info.getTillField() != null) && info.getTillField().equals(colName))) {
                return info;
            }
        }

        return null;
    }

    @Override
    public Object getCellEditorValue() {
        return stat.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  columnName  the columnName to set
     */
    @Override
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    @Override
    public void setLinRefInfos(final List<LinearReferencingInfo> linRefInfos) {
        this.linRefInfos = linRefInfos;
    }

    @Override
    public ClientConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
