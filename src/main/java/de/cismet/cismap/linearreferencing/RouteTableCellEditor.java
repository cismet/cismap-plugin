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

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.Container;

import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.cidslayer.StationInfo;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RouteTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RouteTableCellEditor.class);

    //~ Instance fields --------------------------------------------------------

    private RouteCombo stat;
    private final String columnName;
    private List<LinearReferencingInfo> linRefInfos;
    private final LinearReferencingHelper linHelper = FeatureRegistry.getInstance().getLinearReferencingSolver();
    private final String routeName;
    private String oldValue;
    private CidsLayerFeature cidsFeature;
    private final boolean line;
    private String routeQuery = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RouteTableCellEditor object.
     *
     * @param  routeName   DOCUMENT ME!
     * @param  columnName  DOCUMENT ME!
     * @param  line        DOCUMENT ME!
     */
    public RouteTableCellEditor(final String routeName, final String columnName, final boolean line) {
        this.routeName = routeName;
        this.columnName = columnName;
        this.line = line;
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

// /**
// * Creates a new StationTableCellEditor object.
// *
// * @param  columnName  DOCUMENT ME!
// */
// public RouteTableCellEditor(final String columnName) {
// this.columnName = columnName;
// }

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
                cidsFeature = (CidsLayerFeature)feature;
//                stat = cidsFeature.getStationEditor(getColumnName());
                oldValue = (String)value;
                stat = new RouteCombo(routeName, value);
            }
        } else if (o.getParent() instanceof FeatureInfoPanel) {
            final FeatureInfoPanel infoPanel = (FeatureInfoPanel)o.getParent();
            final FeatureServiceFeature feature = infoPanel.getSelectedFeature();

            if (feature instanceof CidsLayerFeature) {
                cidsFeature = (CidsLayerFeature)feature;
                stat = new RouteCombo(routeName, value);
//                stat = cidsFeature.getStationEditor(getColumnName());
                oldValue = (String)value;
                stat = new RouteCombo(routeName, value);
            }
        }

        return stat;
    }

    @Override
    public Object getCellEditorValue() {
        final String routeNamePropName = linHelper.getRouteNamePropertyFromRouteByClassName(routeName);
        final Object newValue = stat.getValue();
        String newValueName;

        if (newValue instanceof String) {
            newValueName = oldValue;
        } else {
            newValueName = ((newValue != null)
                    ? String.valueOf(((CidsLayerFeature)newValue).getProperty(routeNamePropName)) : "null");
        }

        if ((newValue == null) && (oldValue != null)) {
            // remove station
            try {
                cidsFeature.removeStations();
                cidsFeature.setProperty(columnName, null);
                cidsFeature.getBean().setProperty(columnName, null);
                final CidsLayerInfo info = cidsFeature.getLayerInfo();

                for (final String colName : info.getColumnNames()) {
                    if (info.isStation(colName)) {
                        if (info.getStationInfo(colName).getRouteTable().equals(routeName)) {
                            cidsFeature.setProperty(colName, null);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while deleting property", e);
            }
        } else if ((newValue != oldValue) && !newValueName.equals(oldValue)) {
            cidsFeature.removeStations();
            CidsBean bean = (CidsBean)cidsFeature.getBean().getProperty(columnName);
            final CidsLayerFeature routeFeature = (CidsLayerFeature)newValue;
            final CidsBean routeBean = routeFeature.getBean();

            if (bean == null) {
                if (line) {
                    bean = linHelper.createLineBeanFromRouteBean(routeBean);
                } else {
                    bean = linHelper.createStationBeanFromRouteBean(routeBean);
                }

                try {
                    cidsFeature.setProperty(columnName, bean);
                    cidsFeature.getBean().setProperty(columnName, bean);
                } catch (Exception e) {
                    LOG.error("Set new Bean", e);
                }
            }

            if (bean != null) {
                if (line) {
                    // is line bean
                    final CidsBean start = linHelper.getStationBeanFromLineBean(bean, true);
                    final CidsBean end = linHelper.getStationBeanFromLineBean(bean, false);

                    try {
                        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                                CismapBroker.getInstance().getDefaultCrsAlias());
                        final LengthIndexedLine indexedLine = new LengthIndexedLine(routeFeature.getGeometry());
                        Geometry exPoint;
                        Geometry exEndPoint;
                        final Geometry formerGeom = cidsFeature.getGeometry();

                        if ((formerGeom != null) && (formerGeom instanceof Polygon)) {
                            final Geometry polGeom = (Geometry)formerGeom;
                            final Coordinate[] coords = new MinimumBoundingCircle(polGeom).getExtremalPoints();

                            if ((coords != null) && (coords.length == 2)) {
                                exPoint = polGeom.getFactory().createPoint(coords[0]);
                                exEndPoint = polGeom.getFactory().createPoint(coords[1]);
                            } else {
                                exPoint = linHelper.getPointGeometryFromStationBean(end);
                                exEndPoint = linHelper.getPointGeometryFromStationBean(start);
                            }
                        } else if ((formerGeom == null) || !(formerGeom instanceof LineString)) {
                            exPoint = linHelper.getPointGeometryFromStationBean(end);
                            exEndPoint = linHelper.getPointGeometryFromStationBean(start);
                        } else {
                            exPoint = factory.createPoint(formerGeom.getCoordinates()[0]);
                            exEndPoint = factory.createPoint(
                                    formerGeom.getCoordinates()[formerGeom.getCoordinates().length - 1]);
                        }

                        double val = indexedLine.project(exPoint.getCoordinate());
                        double endVal = indexedLine.project(exEndPoint.getCoordinate());

                        if (val > endVal) {
                            final double tmp = val;
                            val = endVal;
                            endVal = tmp;
                        }

                        if (endVal == val) {
                            if (val == 0) {
                                endVal = 1;
                            } else {
                                val = endVal - 1;

                                if (val < 0) {
                                    val = 0;
                                }
                            }
                        }
                        final Geometry point = factory.createPoint(indexedLine.extractPoint(val));
                        final Geometry endPoint = factory.createPoint(indexedLine.extractPoint(endVal));

                        linHelper.setRouteBeanToStationBean(routeBean, start);
                        linHelper.setLinearValueToStationBean(val, start);
                        linHelper.setPointGeometryToStationBean(point, start);

                        linHelper.setRouteBeanToStationBean(routeBean, end);
                        linHelper.setLinearValueToStationBean(endVal, end);
                        linHelper.setPointGeometryToStationBean(endPoint, end);

                        final Geometry line = indexedLine.extractLine(val, endVal);
                        line.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                        linHelper.setGeometryToLineBean(line, bean);
                    } catch (Exception e) {
                        LOG.error("Error while setting new line", e);
                    }
                } else {
                    // is station bean
                    try {
                        linHelper.setRouteBeanToStationBean(routeBean, bean);
                        Geometry point;
                        final Geometry formerGeom = cidsFeature.getGeometry();

                        if ((formerGeom == null) || !(formerGeom instanceof Point)) {
                            point = linHelper.getPointGeometryFromStationBean(bean);
                        } else {
                            point = (Geometry)formerGeom.clone();
                            point.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                        }

                        final LengthIndexedLine indexedLine = new LengthIndexedLine(routeFeature.getGeometry());
                        final double val = indexedLine.project(point.getCoordinate());
                        linHelper.setLinearValueToStationBean(val, bean);
                        linHelper.setPointGeometryToStationBean(point, bean);
                    } catch (Exception e) {
                        LOG.error("Error while setting new route", e);
                    }
                }
            }

            if (newValue != null) {
                cidsFeature.initStations();
            }
        }

        if (newValue == null) {
            return null;
        } else {
            if (newValue instanceof String) {
                // the routes are still loading
                return oldValue;
            } else {
                return String.valueOf(((CidsLayerFeature)newValue).getProperty(routeNamePropName));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the columnName
     */
    public String getColumnName() {
        return columnName;
    }

//    /**
//     * DOCUMENT ME!
//     *
//     * @param  columnName  the columnName to set
//     */
//    @Override
//    public void setColumnName(final String columnName) {
//        this.columnName = columnName;
//    }
}
