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
package de.cismet.cismap.cidslayer;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;

import de.cismet.cids.tools.CidsLayerUtil;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.DefaultQueryButtonAction;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

import de.cismet.commons.converter.ConversionException;

import de.cismet.connectioncontext.ClientConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

import de.cismet.tools.gui.StaticSwingTools;

import static de.cismet.cids.search.QuerySearch.PROP_ATTRIBUTES;
import static de.cismet.cids.search.QuerySearch.PROP_VALUES;

/**
 * This dialog allows to use a formula to set the values for a AttributeTable row.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FieldCalculatorDialog extends javax.swing.JDialog implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FieldCalculatorDialog.class);
    public static final List<DefaultQueryButtonAction> SQL_QUERY_BUTTONS = new ArrayList<DefaultQueryButtonAction>();
    private static final GeomFromWktConverter converter = new GeomFromWktConverter();
    private static final String RECEIVE_CATALOGUE_QUERY = "SELECT DISTINCT %1$s, %2$s FROM %3$s;";
    private static final DecimalFormat formatter;

    static {
        formatter = new java.text.DecimalFormat();
//        final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
//        symbols.setDecimalSeparator(',');
//        symbols.setGroupingSeparator('.');
//        formatter.setDecimalFormatSymbols(symbols);
        formatter.setGroupingUsed(false);

        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(".x", "x") {

                {
                    startWithSpace = false;
                }
            });
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("+", "++"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("+"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(".y", "y") {

                {
                    startWithSpace = false;
                }
            });
        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction(".left", "left()"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("-"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("()") {

                {
                    posCorrection = -1;
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (queryTextArea.getSelectionEnd() == 0) {
                        super.actionPerformed(e);
                    } else {
                        final int start = queryTextArea.getSelectionStart();
                        final int end = queryTextArea.getSelectionEnd();
                        queryTextArea.insert("(", start);
                        queryTextArea.insert(")", end + 1);
                        if (start == end) {
                            CorrectCarret(posCorrection);
                        } else {
                            CorrectCarret((short)2);
                        }
                    }
                }
            });
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(".geoLength", "length") {

                {
                    startWithSpace = false;
                }
            });
        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction(".right", "right()"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("*"));
        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction("Math.round", "Round"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(".area", "area") {

                {
                    startWithSpace = false;
                }
            });
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(".length", "count") {

                {
                    startWithSpace = false;
                }
            });
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("/"));
        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction("parseInt", "Integer"));
//        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction("parseDouble", "Double"));
//        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction("Boolean", "Boolean"));
//        SQL_QUERY_BUTTONS.add(new MethodQueryButtonAction("String", "String"));
//        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("%"));
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private enum SubstitutionFunctions {

        //~ Enum constants -----------------------------------------------------

        LENGTH, AREA, X, Y
    }

    //~ Instance fields --------------------------------------------------------

    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private List<? extends Object> attributes;
    private AbstractFeatureService service;
    private FeatureServiceAttribute currentlyExpandedAttribute;
    private List<Object> values;
    private FeatureServiceAttribute attribute;
    private List<FeatureServiceFeature> featureList;
    private AttributeTable table;
    private boolean calculationStarted = false;

    private final ClientConnectionContext connectionContext;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel HintPanel;
    private javax.swing.JButton btnSearchCancel;
    private javax.swing.JList jAttributesLi;
    private javax.swing.JLabel jCommandLb;
    private javax.swing.JButton jGetValuesBn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelTasten;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList jValuesLi;
    private javax.swing.JLabel jlShowIndividualValues;
    private javax.swing.JLabel labMathLink;
    private javax.swing.JLabel labStringLink;
    private org.jdesktop.swingx.JXBusyLabel lblBusyIcon;
    private org.jdesktop.swingx.JXBusyLabel lblBusyValueIcon;
    private javax.swing.JPanel panCommand;
    private javax.swing.Box.Filler strGap;
    private javax.swing.JTextArea taQuery;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form LinearReferencingDialog.
     *
     * @param  table              DOCUMENT ME!
     * @param  modal              DOCUMENT ME!
     * @param  service            DOCUMENT ME!
     * @param  attribute          DOCUMENT ME!
     * @param  featureList        DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    public FieldCalculatorDialog(final AttributeTable table,
            final boolean modal,
            final AbstractFeatureService service,
            final FeatureServiceAttribute attribute,
            final List<FeatureServiceFeature> featureList,
            final ClientConnectionContext connectionContext) {
        super(StaticSwingTools.getParentFrame(table), modal);
        this.service = service;
        this.attribute = attribute;
        this.featureList = featureList;
        this.table = table;
        this.connectionContext = connectionContext;
        initComponents();
        jAttributesLi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jAttributesLi.addMouseListener(new MouseAdapterImpl());
        jValuesLi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jValuesLi.addMouseListener(new MouseAdapterImpl());

        jAttributesLi.setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList<?> list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    String text = value.toString();

                    if (value instanceof FeatureServiceAttribute) {
                        text = ((FeatureServiceAttribute)value).getName();
                    }

                    return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                }
            });

        jAttributesLi.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }
                    if (!((attributes == null) || (jAttributesLi.getSelectedIndex() == -1))) {
                        jGetValuesBn.setEnabled(true);
                    } else {
                        jGetValuesBn.setEnabled(false);
                    }
                }
            });

        jValuesLi.setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final Component c = super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus);

                    if ((value instanceof MetaObject) && (c instanceof JLabel)) {
                        final MetaObject mo = (MetaObject)value;
                        ((JLabel)c).setText(mo.getID() + " - " + mo.toString());
                    } else {
                        ((JLabel)c).setText(featureValueToString(value));
                    }

                    return c;
                }
            });

        init();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public final ClientConnectionContext getConnectionContext() {
        return connectionContext;
    }

    /**
     * Converts the given value to a string.
     *
     * @param   value  the value to convert to a string
     *
     * @return  the converted value
     */
    private String featureValueToString(final Object value) {
        if ((value instanceof Float) || (value instanceof Double)) {
            final Number valueNumber = (Number)value;

            return FeatureTools.FORMATTER.format(valueNumber);
        }

        return ((value == null) ? "" : String.valueOf(value));
    }

    /**
     * Initialises the GUI elements.
     */
    private void init() {
        final AbstractFeatureService afs = (AbstractFeatureService)service;
        final Map<String, FeatureServiceAttribute> newAttribMap = afs.getFeatureServiceAttributes();
        final List<FeatureServiceAttribute> newAttributes = new ArrayList<FeatureServiceAttribute>();

        for (final String attr : (List<String>)afs.getOrderedFeatureServiceAttributes()) {
            final FeatureServiceAttribute fsa = newAttribMap.get(attr);

            if (attr != null) {
                newAttributes.add(fsa);
            }
        }

        List<? extends Object> old = attributes;
        attributes = newAttributes;

        if (attributes != old) {
            firePropertyChange(PROP_ATTRIBUTES, old, attributes);
            final DefaultListModel model = (DefaultListModel)jAttributesLi.getModel();

            model.clear();
            for (final Object tmp : attributes) {
                model.addElement(tmp);
            }
        }

        jlShowIndividualValues.setText("");
        old = values;
        values = new LinkedList<Object>();
        firePropertyChange(PROP_VALUES, old, values);

        fillButtonPanel();
    }

    /**
     * fills the buttons panel with the buttons from the seleced service.
     */
    private void fillButtonPanel() {
        final List<DefaultQueryButtonAction> queryButtons;
        int x = 0;
        int y = 0;
//        jPanelTasten.removeAll();

        queryButtons = SQL_QUERY_BUTTONS;

        for (final DefaultQueryButtonAction buttonAction : queryButtons) {
            final JButton button = new JButton(buttonAction.getText());
            button.addActionListener(buttonAction);
            final GridBagConstraints constraint = new GridBagConstraints(
                    x,
                    y,
                    buttonAction.getWidth(),
                    1,
                    1,
                    0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.HORIZONTAL,
                    new Insets(2, 2, 2, 2),
                    0,
                    0);
            jPanelTasten.add(button, constraint);
            buttonAction.setQueryTextArea(taQuery);
            x += buttonAction.getWidth();
            if (x > 6) {
                x = 0;
                ++y;
            }
        }

        final GridBagConstraints constraint = new GridBagConstraints(
                x,
                y,
                7, // width
                1,
                1,
                0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2),
                0,
                0);
        jPanelTasten.add(HintPanel, constraint);

        jPanelTasten.invalidate();
        jPanelTasten.revalidate();
        jPanelTasten.repaint();
    }

    /**
     * Appends the given string to the query field.
     *
     * @param  str  The string to append
     */
    private void AppendString(String str) {
        if ((taQuery.getText() != null) && !taQuery.getText().isEmpty()) {
            try {
                if (!taQuery.getText(taQuery.getCaretPosition() - 1, 1).contains("(")) {
                    str = " " + str;
                }
            } catch (BadLocationException ex) {
                LOG.error("Error while appending string", ex);
                str = " " + str;
            }
        }
        taQuery.insert(str, taQuery.getCaretPosition());
    }

    /**
     * Receives all features from the current feature service.
     *
     * @return  A list with all features from the current feature service
     */
    private List<FeatureServiceFeature> getAllFeaturesFromService() {
        List<FeatureServiceFeature> allFeatures;

        try {
            final Geometry g = ZoomToLayerWorker.getServiceBounds(service);
            XBoundingBox bounds = null;

            if (g != null) {
                bounds = new XBoundingBox(g);
                final String crs = CismapBroker.getInstance().getSrs().getCode();
                final CrsTransformer trans = new CrsTransformer(crs);
                bounds = trans.transformBoundingBox(bounds);
            }
            allFeatures = service.getFeatureFactory().createFeatures(service.getQuery(), bounds, null, 0, 0, null);
        } catch (Exception e) {
            LOG.error("Cannot retrieve all features", e);
            allFeatures = service.getFeatureFactory().getLastCreatedFeatures();
        }

        return allFeatures;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        HintPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        labMathLink = new javax.swing.JLabel();
        labStringLink = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttributesLi = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jValuesLi = new javax.swing.JList();
        jPanelTasten = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        taQuery = new javax.swing.JTextArea();
        jCommandLb = new javax.swing.JLabel();
        jGetValuesBn = new javax.swing.JButton();
        panCommand = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblBusyIcon = new org.jdesktop.swingx.JXBusyLabel(new java.awt.Dimension(20, 20));
        strGap = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0),
                new java.awt.Dimension(5, 25),
                new java.awt.Dimension(5, 32767));
        btnSearchCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblBusyValueIcon = new org.jdesktop.swingx.JXBusyLabel(new java.awt.Dimension(20, 20));
        jlShowIndividualValues = new javax.swing.JLabel();

        HintPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.jLabel1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        HintPanel.add(jLabel1, gridBagConstraints);

        labMathLink.setForeground(new java.awt.Color(64, 64, 255));
        labMathLink.setText(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.labMathLink.text",
                new Object[] {})); // NOI18N
        labMathLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labMathLink.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(final java.awt.event.MouseEvent evt) {
                    labMathLinkMouseClicked(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        HintPanel.add(labMathLink, gridBagConstraints);

        labStringLink.setForeground(new java.awt.Color(64, 64, 255));
        labStringLink.setText(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.labStringLink.text",
                new Object[] {})); // NOI18N
        labStringLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labStringLink.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(final java.awt.event.MouseEvent evt) {
                    labStringLinkMouseClicked(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        HintPanel.add(labStringLink, gridBagConstraints);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.title",
                new Object[] {
                    (((attribute.getAlias() != null) && !attribute.getAlias().equals("")) ? attribute.getAlias()
                                                                                          : attribute.getName())
                })); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setMinimumSize(new java.awt.Dimension(258, 40));

        jAttributesLi.setModel(new DefaultListModel());
        jAttributesLi.setVisibleRowCount(0);
        jScrollPane1.setViewportView(jAttributesLi);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(240, 40));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(240, 40));

        jValuesLi.setModel(new DefaultListModel());
        jValuesLi.setVisibleRowCount(0);
        jScrollPane2.setViewportView(jValuesLi);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jPanelTasten.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.weighty = 1.0;
        jPanelTasten.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanelTasten, gridBagConstraints);

        jScrollPane3.setMinimumSize(new java.awt.Dimension(262, 87));

        taQuery.setColumns(20);
        taQuery.setRows(5);
        jScrollPane3.setViewportView(taQuery);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jScrollPane3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jCommandLb, gridBagConstraints);

        jGetValuesBn.setText(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.jGetValuesBn.text")); // NOI18N
        jGetValuesBn.setEnabled(false);
        jGetValuesBn.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jGetValuesBnActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(jGetValuesBn, gridBagConstraints);

        panCommand.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jPanel2.setMinimumSize(new java.awt.Dimension(125, 25));
        jPanel2.setPreferredSize(new java.awt.Dimension(185, 30));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 0, 0));

        lblBusyIcon.setEnabled(false);
        jPanel2.add(lblBusyIcon);
        jPanel2.add(strGap);

        btnSearchCancel.setText(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.btnSearchCancel.text"));        // NOI18N
        btnSearchCancel.setToolTipText(org.openide.util.NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.btnSearchCancel.toolTipText")); // NOI18N
        btnSearchCancel.setMaximumSize(new java.awt.Dimension(100, 25));
        btnSearchCancel.setMinimumSize(new java.awt.Dimension(100, 25));
        btnSearchCancel.setPreferredSize(new java.awt.Dimension(100, 25));
        btnSearchCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnSearchCancelActionPerformed(evt);
                }
            });
        jPanel2.add(btnSearchCancel);

        panCommand.add(jPanel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        getContentPane().add(panCommand, gridBagConstraints);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblBusyValueIcon.setEnabled(false);
        jPanel1.add(lblBusyValueIcon);
        jPanel1.add(jlShowIndividualValues);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jGetValuesBnActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jGetValuesBnActionPerformed
        if (jAttributesLi.getSelectedValue() == null) {
            return;
        }

        final Object attributeObject = attributes.get(jAttributesLi.getSelectedIndex());

        lblBusyValueIcon.setEnabled(true);
        lblBusyValueIcon.setBusy(true);
        final FeatureServiceAttribute attributeInfo = (FeatureServiceAttribute)attributeObject;
        jGetValuesBn.setEnabled(false);

        threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    final List allFeatures;
                    final TreeSet set = new TreeSet();
                    boolean setAlreadyFilled = false;

                    if (service instanceof CidsLayer) {
                        try {
                            final FeatureServiceFeature firstFeature = featureList.get(0);
                            final CidsLayer cidsLayer = (CidsLayer)service;
                            final CidsLayerInfo layerInfo = CidsLayerUtil.getCidsLayerInfo(
                                    cidsLayer.getMetaClass(),
                                    SessionManager.getSession().getUser());

                            if (layerInfo.isCatalogue(attributeInfo.getName())) {
                                final CidsLayerFeature cf = (CidsLayerFeature)firstFeature;

                                if (cf.getCatalogueCombo(attribute.getName()) != null) {
                                    final ComboBoxModel model = cf.getCatalogueCombo(attribute.getName()).getModel();

                                    waitForModel(model);

                                    for (int i = 0; i < model.getSize(); ++i) {
                                        if (model.getElementAt(i) != null) {
                                            set.add(model.getElementAt(i).toString());
                                        }
                                    }
                                } else {
                                    final int referencedForeignClassId = layerInfo.getCatalogueClass(
                                            attribute.getName());

                                    final MetaClass foreignClass = getMetaClass(
                                            referencedForeignClassId,
                                            cf.getBean().getMetaObject().getMetaClass());
                                    final DefaultCidsLayerBindableReferenceCombo catalogueEditor =
                                        new DefaultCidsLayerBindableReferenceCombo(
                                            foreignClass,
                                            true);
                                    final ComboBoxModel model = catalogueEditor.getModel();

                                    waitForModel(model);

                                    for (int i = 0; i < model.getSize(); ++i) {
                                        if (model.getElementAt(i) != null) {
                                            set.add(model.getElementAt(i).toString());
                                        }
                                    }
                                }
                                setAlreadyFilled = true;
                            }
                        } catch (Exception e) {
                            LOG.error("Cannot load features", e);
                        }
                    }

                    if (!setAlreadyFilled) {
                        allFeatures = getAllFeaturesFromService();

                        for (final Object tmp : allFeatures) {
                            final FeatureServiceFeature tmpFeature = (FeatureServiceFeature)tmp;
                            final Object attrValue = tmpFeature.getProperty(attributeInfo.getName());

                            if (attrValue != null) {
                                set.add(attrValue);
                            }
                        }
                    }

                    SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                final List<Object> old = values;
                                values = new ArrayList<Object>(set);
                                lblBusyValueIcon.setEnabled(false);
                                lblBusyValueIcon.setBusy(false);
                                jGetValuesBn.setEnabled(true);
                                jPanel1.repaint();
                                jScrollPane1.repaint();
                                jValuesLi.repaint();
                                doLayout();
                                repaint();
                                final DefaultListModel model = (DefaultListModel)jValuesLi.getModel();

                                model.clear();
                                for (final Object tmp : values) {
                                    model.addElement(tmp);
                                }
                                firePropertyChange(PROP_VALUES, old, values);
                            }
                        });
                }
            });

        currentlyExpandedAttribute = attributeInfo;

        jlShowIndividualValues.setText(NbBundle.getMessage(
                FieldCalculatorDialog.class,
                "FieldCalculatorDialog.jGetValuesBnActionPerformed().jlShowIndividualValues.text",
                currentlyExpandedAttribute.getName()));
    } //GEN-LAST:event_jGetValuesBnActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnSearchCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSearchCancelActionPerformed
        lblBusyIcon.setEnabled(true);
        lblBusyIcon.setBusy(true);

        threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    final ScriptEngineManager manager = new ScriptEngineManager();
                    final ScriptEngine engine = manager.getEngineByName("js");
                    final Map<FeatureServiceFeature, Object> temporaryObjectMap =
                        new HashMap<FeatureServiceFeature, Object>(featureList.size());

                    try {
                        final FeatureServiceFeature firstFeature = featureList.get(0);
                        List<Object> catElements = null;

                        if (firstFeature instanceof CidsLayerFeature) {
                            final CidsLayerInfo layerInfo = ((CidsLayerFeature)firstFeature).getLayerInfo();

                            if (layerInfo.isCatalogue(attribute.getName())) {
                                final CidsLayerFeature cf = (CidsLayerFeature)firstFeature;
                                catElements = new ArrayList<Object>();

                                if (cf.getCatalogueCombo(attribute.getName()) != null) {
                                    final ComboBoxModel model = cf.getCatalogueCombo(attribute.getName()).getModel();

                                    waitForModel(model);

                                    for (int i = 0; i < model.getSize(); ++i) {
                                        catElements.add(model.getElementAt(i));
                                    }
                                } else {
                                    final int referencedForeignClassId = layerInfo.getCatalogueClass(
                                            attribute.getName());

                                    final MetaClass foreignClass = getMetaClass(
                                            referencedForeignClassId,
                                            cf.getBean().getMetaObject().getMetaClass());
                                    final DefaultCidsLayerBindableReferenceCombo catalogueEditor =
                                        new DefaultCidsLayerBindableReferenceCombo(
                                            foreignClass,
                                            true);
                                    final ComboBoxModel model = catalogueEditor.getModel();
                                    catElements = new ArrayList<Object>();

                                    waitForModel(model);

                                    for (int i = 0; i < model.getSize(); ++i) {
                                        catElements.add(model.getElementAt(i));
                                    }
                                }
                            }
                        }
                        for (final FeatureServiceFeature feature : featureList) {
                            final String dataDefinition = toVariableString(feature);
                            String code = taQuery.getText();

                            code = substituteLength(code, feature);
                            code = substituteArea(code, feature);
                            code = substituteX(code, feature);
                            code = substituteY(code, feature);
                            code = substituteLeft(code, feature);
                            code = substituteRight(code, feature);
                            code = dataDefinition + "\n " + code;
                            Object result = engine.eval(code);

                            if (catElements != null) {
                                if (result instanceof Double) {
                                    result = formatter.format(result);
                                }
                                for (final Object element : catElements) {
                                    if (((element != null) && (result != null)
                                                    && element.toString().equals(result.toString()))
                                                || ((element == null) && (result == null))) {
                                        temporaryObjectMap.put(feature, element);
                                    }
                                }
                            } else {
                                temporaryObjectMap.put(
                                    feature,
                                    FeatureTools.convertObjectToClass(result, FeatureTools.getClass(attribute)));
                            }
                        }

                        for (final FeatureServiceFeature feature : temporaryObjectMap.keySet()) {
                            if ((temporaryObjectMap.get(feature) instanceof CidsLayerFeature)
                                        && (feature instanceof CidsLayerFeature)) {
                                if (((CidsLayerFeature)feature).getCatalogueCombo(attribute.getName()) != null) {
                                    ((CidsLayerFeature)feature).getCatalogueCombo(attribute.getName())
                                            .setSelectedItem(temporaryObjectMap.get(feature));
                                }
                            }
                            feature.setProperty(attribute.getName(), temporaryObjectMap.get(feature));
                            table.addModifiedFeature((DefaultFeatureServiceFeature)feature);
                        }
                    } catch (Exception e) {
                        LOG.error("invalid fromula: " + taQuery.getText(), e);
                        JOptionPane.showMessageDialog(
                            FieldCalculatorDialog.this,
                            e.getMessage(),
                            NbBundle.getMessage(
                                FieldCalculatorDialog.class,
                                "FieldCalculatorDialog.btnSearchCancelActionPerformed().error.title"),
                            JOptionPane.ERROR_MESSAGE,
                            null);
                    }

                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                calculationStarted = true;
                                final List selectedFeatures = table.getSelectedFeatures();
                                table.refresh();

                                if ((selectedFeatures != null) && !selectedFeatures.isEmpty()) {
                                    table.applySelection(this, selectedFeatures, false);
                                }
                                SelectionManager.getInstance().addSelectedFeatures(featureList);
                                lblBusyIcon.setEnabled(false);
                                lblBusyIcon.setBusy(false);
                            }
                        });
                }
            });
    } //GEN-LAST:event_btnSearchCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void labMathLinkMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_labMathLinkMouseClicked
        try {
            de.cismet.tools.BrowserLauncher.openURL("https://wiki.selfhtml.org/wiki/JavaScript/Objekte/Math");
        } catch (Exception ex) {
            LOG.error("Error while trying to open a link", ex);
        }
    }                                                                           //GEN-LAST:event_labMathLinkMouseClicked

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void labStringLinkMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_labStringLinkMouseClicked
        try {
            de.cismet.tools.BrowserLauncher.openURL("https://wiki.selfhtml.org/wiki/JavaScript/Objekte/String");
        } catch (Exception ex) {
            LOG.error("Error while trying to open a link", ex);
        }
    }                                                                             //GEN-LAST:event_labStringLinkMouseClicked

    /**
     * DOCUMENT ME!
     *
     * @param  model  DOCUMENT ME!
     */
    private void waitForModel(final ComboBoxModel model) {
        final String s = NbBundle.getMessage(
                DefaultCidsLayerBindableReferenceCombo.class,
                "DefaultCidsLayerBindableReferenceCombo.loading");

        while ((model.getSize() == 1) && model.getElementAt(0).equals(s)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
    }

    /**
     * The delivered meta class is always from the same domain as the meta object of this feature.
     *
     * @param   classId    the id of the meta class
     * @param   metaClass  DOCUMENT ME!
     *
     * @return  the meta class object of the class with the given class id
     *
     * @throws  ConnectionException  DOCUMENT ME!
     */
    private MetaClass getMetaClass(final int classId, final MetaClass metaClass) throws ConnectionException {
        return SessionManager.getConnection()
                    .getMetaClass(SessionManager.getSession().getUser(),
                        classId,
                        metaClass.getDomain(),
                        getConnectionContext());
    }

    /**
     * Solve all length properties.
     *
     * @param   code     the code that possibly contains invocations of the length property
     * @param   feature  the feature with the properties which are use in the code parameter
     *
     * @return  the new code
     */
    private String substituteLength(final String code, final FeatureServiceFeature feature) {
        return preEvalProperty(code, feature, "geoLength", SubstitutionFunctions.LENGTH);
    }

    /**
     * Solve all area properties.
     *
     * @param   code     the code that possibly contains invocations of the area property
     * @param   feature  the feature with the properties which are use in the code parameter
     *
     * @return  the new code
     */
    private String substituteArea(final String code, final FeatureServiceFeature feature) {
        return preEvalProperty(code, feature, "area", SubstitutionFunctions.AREA);
    }

    /**
     * Solve all area properties.
     *
     * @param   code     the code that possibly contains invocations of the area property
     * @param   feature  the feature with the properties which are use in the code parameter
     *
     * @return  the new code
     */
    private String substituteX(final String code, final FeatureServiceFeature feature) {
        return preEvalProperty(code, feature, "x", SubstitutionFunctions.X);
    }

    /**
     * Solve all area properties.
     *
     * @param   code     the code that possibly contains invocations of the area property
     * @param   feature  the feature with the properties which are use in the code parameter
     *
     * @return  the new code
     */
    private String substituteY(final String code, final FeatureServiceFeature feature) {
        return preEvalProperty(code, feature, "y", SubstitutionFunctions.Y);
    }

    /**
     * Solve all left() method calls.
     *
     * @param   code     the code that possibly contains invocations of the left method.
     * @param   feature  the feature with the properties which are use in the code parameter
     *
     * @return  the new code
     */
    private String substituteLeft(final String code, final FeatureServiceFeature feature) {
        return preEvalMethod(code, feature, "left", true);
    }

    /**
     * Solve all right() method calls.
     *
     * @param   code     the code that possibly contains invocations of the right method
     * @param   feature  the feature with the properties which are use in the code parameter
     *
     * @return  the new code
     */
    private String substituteRight(final String code, final FeatureServiceFeature feature) {
        return preEvalMethod(code, feature, "right", false);
    }

    /**
     * Solves method invocations.
     *
     * @param   code        the code that possibly contains method invocations
     * @param   feature     the feature with the properties which are use in the code parameter
     * @param   methodName  the name of the method that should be solved
     * @param   left        true, iff the given method should be substituted by the left() method
     *
     * @return  the new code
     */
    private String preEvalMethod(final String code,
            final FeatureServiceFeature feature,
            final String methodName,
            final boolean left) {
        final Pattern p = Pattern.compile("([^\\s]+\\." + methodName + "\\([^\\)]+\\))");
        final Matcher matcher = p.matcher(code);
        final StringBuilder c = new StringBuilder(code);
        int offset = 0;

        while (matcher.find()) {
            final String lengthString = matcher.group(0);
            String object = lengthString.substring(0, lengthString.lastIndexOf("."));
            final String parameter = lengthString.substring(lengthString.lastIndexOf("(") + 1,
                    lengthString.length()
                            - 1);

            if (left) {
                object += ".slice(0, " + parameter + ")";
            } else {
                object += ".slice(" + object + ".length - " + parameter + ", " + object + ".length)";
            }

            c.replace(matcher.start(0) + offset, matcher.end(0) + offset, object);
            offset += object.length() - (matcher.end(0) - matcher.start(0));
        }

        return c.toString();
    }

    /**
     * Solves propertie invocations.
     *
     * @param   code          the code that possibly contains property invocations
     * @param   feature       the feature with the properties which are use in the code parameter
     * @param   propertyName  methodName the name of the property that should be substituted
     * @param   function      lengthFunction left true, iff the given property should be substituted by the length of
     *                        the geometry
     *
     * @return  the new code
     */
    private String preEvalProperty(final String code,
            final FeatureServiceFeature feature,
            final String propertyName,
            final SubstitutionFunctions function) {
//        final Pattern p = Pattern.compile("([^\\s]+\\." + propertyName + "\\s)");
        final Pattern p = Pattern.compile("([^\\s]+\\." + propertyName + ")");
        final Matcher matcher = p.matcher(code);
        final StringBuilder c = new StringBuilder(code);
        int offset = 0;

        while (matcher.find()) {
            final String lengthString = matcher.group(0);
            String object = lengthString.substring(0, lengthString.lastIndexOf("."));

            if ((object.startsWith("'") && object.endsWith("'"))
                        || (object.startsWith("\"") && object.endsWith("\""))) {
                final String geometryString = object.substring(1, object.length() - 1);

                try {
                    final Geometry geom = converter.convertForward(
                            geometryString,
                            CismapBroker.getInstance().getSrs().getCode());
                    if (function == SubstitutionFunctions.LENGTH) {
                        object = FeatureTools.FORMATTER.format(round(geom.getLength(), 2));
                    } else if (function == SubstitutionFunctions.AREA) {
                        object = FeatureTools.FORMATTER.format(round(geom.getArea(), 2));
                    } else if (function == SubstitutionFunctions.X) {
                        if (geom.getGeometryType().equalsIgnoreCase("Point")) {
                            object = FeatureTools.FORMATTER.format(geom.getCoordinate().x);
                        } else {
                            object = FeatureTools.FORMATTER.format(geom.getCentroid().getCoordinate().x);
                        }
                    } else if (function == SubstitutionFunctions.Y) {
                        if (geom.getGeometryType().equalsIgnoreCase("Point")) {
                            object = FeatureTools.FORMATTER.format(geom.getCoordinate().y);
                        } else {
                            object = FeatureTools.FORMATTER.format(geom.getCentroid().getCoordinate().y);
                        }
                    }
                } catch (ConversionException e) {
                    JOptionPane.showMessageDialog(
                        this,
                        object
                                + " ist keine Geometrie",
                        "Ungültige Geometrie",
                        JOptionPane.ERROR_MESSAGE);
                    object = "";
                }
            } else {
                Object geometryObject = feature.getProperty(object);
                if (geometryObject instanceof org.deegree.model.spatialschema.Geometry) {
                    try {
                        geometryObject = JTSAdapter.export((org.deegree.model.spatialschema.Geometry)geometryObject);
                    } catch (GeometryException ex) {
                        LOG.error("Error while converting deegree geometry to jts geometry", ex);
                    }
                }

                if (geometryObject instanceof Geometry) {
                    final Geometry geometry = (Geometry)geometryObject;
                    if (function == SubstitutionFunctions.LENGTH) {
                        object = FeatureTools.FORMATTER.format(round(geometry.getLength(), 2));
                    } else if (function == SubstitutionFunctions.AREA) {
                        object = FeatureTools.FORMATTER.format(round(geometry.getArea(), 2));
                    } else if (function == SubstitutionFunctions.X) {
                        if (geometry.getGeometryType().equalsIgnoreCase("Point")) {
                            object = FeatureTools.FORMATTER.format(geometry.getCoordinate().x);
                        } else {
                            object = FeatureTools.FORMATTER.format(geometry.getCentroid().getCoordinate().x);
                        }
                    } else if (function == SubstitutionFunctions.Y) {
                        if (geometry.getGeometryType().equalsIgnoreCase("Point")) {
                            object = FeatureTools.FORMATTER.format(geometry.getCoordinate().y);
                        } else {
                            object = FeatureTools.FORMATTER.format(geometry.getCentroid().getCoordinate().y);
                        }
                    }
                }
            }

            c.replace(matcher.start(0) + offset, matcher.end(0) + offset, object);
            offset += object.length() - (matcher.end(0) - matcher.start(0));
        }

        return c.toString();
    }

    /**
     * Rounds the given value.
     *
     * @param   value   the value to round
     * @param   digits  the decimal places, the value should be round to
     *
     * @return  the rounded value
     */
    private double round(final double value, final int digits) {
        final BigDecimal tmpValue = new BigDecimal(value);
        return tmpValue.setScale(digits, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Creates a javascript string that contains all properties of the given feature as variable.
     *
     * @param   feature  the feature, that should be translated to javascript
     *
     * @return  the created string
     */
    private String toVariableString(final FeatureServiceFeature feature) {
        final StringBuilder vars = new StringBuilder("");
        final HashMap<String, Object> props = feature.getProperties();

        for (final String propName : props.keySet()) {
            final FeatureServiceAttribute attr = (FeatureServiceAttribute)feature.getLayerProperties()
                        .getFeatureService()
                        .getFeatureServiceAttributes()
                        .get(propName);
            final Class cl = FeatureTools.getClass(attr);
            Object value = props.get(propName);

            if (value instanceof org.deegree.model.spatialschema.Geometry) {
                try {
                    value = JTSAdapter.export((org.deegree.model.spatialschema.Geometry)value);
                } catch (GeometryException ex) {
                    LOG.error("Error while converting deegree geometry to jts geometry", ex);
                }
            }
            if (vars.length() > 0) {
                vars.append(";\n");
            }

            vars.append(propName.replace((CharSequence)"app:", (CharSequence)"")).append("=");
            if ((value != null) && (cl.equals(String.class) || cl.equals(Date.class))) {
                if (cl.equals(String.class)) {
                    vars.append("\"")
                            .append(String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\""))
                            .append("\"");
                } else {
                    vars.append("\"").append(value).append("\"");
                }
            } else {
                vars.append(String.valueOf(value));
            }
        }

        vars.append(";\n");

        return vars.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FieldCalculatorDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FieldCalculatorDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FieldCalculatorDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FieldCalculatorDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the isCalculationStarted
     */
    public boolean isCalculationStarted() {
        return calculationStarted;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class MouseAdapterImpl extends MouseAdapter {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new MouseAdapterImpl object.
         */
        public MouseAdapterImpl() {
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() == 2) {
                final JList source = ((JList)e.getSource());
                Object selectedObject = source.getSelectedValue();
                String value;

                if (e.getSource() == jAttributesLi) {
                    selectedObject = attributes.get(jAttributesLi.getSelectedIndex());
                }

                if (source == jAttributesLi) {
                    final String v = ((FeatureServiceAttribute)selectedObject).getName();
                    value = v;
                } else {
                    final Class cl = FeatureTools.getClass(currentlyExpandedAttribute);

                    if (cl.equals(String.class) || cl.equals(Date.class)) {
                        value = "\"" + selectedObject.toString() + "\"";
                    } else {
                        if ((selectedObject instanceof Float) || (selectedObject instanceof Double)) {
                            value = FeatureTools.FORMATTER.format(selectedObject);
                        } else {
                            value = String.valueOf(selectedObject);
                        }
                    }
                }

                AppendString(value);
            }
        }
    }

    /**
     * Handles QueryButtonActions which ends with ().
     *
     * @version  $Revision$, $Date$
     */
    private static class MethodQueryButtonAction extends DefaultQueryButtonAction {

        //~ Instance fields ----------------------------------------------------

        private final String queryTextWithOutBraces;

        //~ Instance initializers ----------------------------------------------

        {
            posCorrection = -1;
        }

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new MethodQueryButtonAction object.
         *
         * @param  queryText  DOCUMENT ME!
         * @param  text       DOCUMENT ME!
         */
        public MethodQueryButtonAction(final String queryText, final String text) {
            super(queryText + "()", text);
            queryTextWithOutBraces = queryText;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (queryTextArea.getSelectionEnd() == 0) {
                super.actionPerformed(e);
            } else {
                final int start = queryTextArea.getSelectionStart();
                final int end = queryTextArea.getSelectionEnd();
                queryTextArea.insert(queryTextWithOutBraces + "(", start);
                queryTextArea.insert(")", end + ((queryTextWithOutBraces + "(").length()));
                // jTextArea1.setCaretPosition(end + 2);
                if (start == end) {
                    CorrectCarret(posCorrection);
                } else {
                    CorrectCarret((short)2);
                }
            }
        }
    }
}
