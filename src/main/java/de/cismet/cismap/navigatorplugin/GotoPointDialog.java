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
package de.cismet.cismap.navigatorplugin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import org.deegree.model.crs.UnknownCRSException;

import java.awt.Component;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.StaticDecimalTools;

/**
 * DOCUMENT ME!
 *
 * @author   gbaatz
 * @version  $Revision$, $Date$
 */
public class GotoPointDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static volatile GotoPointDialog instance = null;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ImageIcon mark = new javax.swing.ImageIcon(getClass().getResource(
                "/images/markPoint.png")); // NOI18N
    private final java.util.Properties sweetSpotOfpMark = new java.util.Properties();
    private FixedPImage pMark = new FixedPImage(mark.getImage());

    private Point gotoPoint = null;

    private HashMap<JTextField, Crs> crsByTextfieldHM = new HashMap<>();

    /** DOCUMENT ME! */
    private boolean docListenersEnabled = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnPosition;
    private javax.swing.JCheckBox cbMarkPoint;
    private javax.swing.JComboBox<String> cboAdditionalSRS;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblSRS1;
    private javax.swing.JLabel lblSRS2;
    private javax.swing.JLabel lblSRS3;
    private javax.swing.JPanel panActions;
    private javax.swing.JPanel panMarkPoint;
    private javax.swing.JTextField tfCoordinatesSRS0;
    private javax.swing.JTextField tfCoordinatesSRS1;
    private javax.swing.JTextField tfCoordinatesSRS2;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form GotoPointDialog.
     */
    private GotoPointDialog() {
        super();
        try {
            sweetSpotOfpMark.load(this.getClass().getResourceAsStream("/images/markPointSweetSpot.properties"));
            pMark.setSweetSpotX(Double.valueOf(sweetSpotOfpMark.getProperty("x", "0")));
            pMark.setSweetSpotY(Double.valueOf(sweetSpotOfpMark.getProperty("y", "0")));
        } catch (IOException iox) {
            log.warn("Problem when loading the markPointSweetSpot.properties", iox);
        }
        initComponents();

        tfCoordinatesSRS0.getDocument().addDocumentListener(new CoordDocumentListener(tfCoordinatesSRS0));
        tfCoordinatesSRS1.getDocument().addDocumentListener(new CoordDocumentListener(tfCoordinatesSRS1));
        tfCoordinatesSRS2.getDocument().addDocumentListener(new CoordDocumentListener(tfCoordinatesSRS2));

        cboAdditionalSRS.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList<?> list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    if (value instanceof Crs) {
                        final Component c = super.getListCellRendererComponent(
                                list,
                                ((Crs)value).getShortname(),
                                index,
                                isSelected,
                                cellHasFocus);
                        if (c instanceof JLabel) {
                            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                        }
                        return c;
                    } else {
                        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    }
                }
            });
        pMark.setVisible(true);

        final List<Crs> crss = CismapBroker.getInstance().getMappingComponent().getCrsList();
        if (crss.size() > 3) {
            crsByTextfieldHM.put(tfCoordinatesSRS0, crss.get(0));
            crsByTextfieldHM.put(tfCoordinatesSRS1, crss.get(1));
            lblSRS3.setVisible(false);
            lblSRS1.setText(crss.get(0).getShortname());
            lblSRS2.setText(crss.get(1).getShortname());
            final DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (final Crs s : crss.subList(2, crss.size())) {
                model.addElement(s);
            }
            cboAdditionalSRS.setModel(model);
            crsByTextfieldHM.put(tfCoordinatesSRS2, (Crs)cboAdditionalSRS.getSelectedItem());
        } else if (crss.size() == 3) {
            crsByTextfieldHM.put(tfCoordinatesSRS0, crss.get(0));
            crsByTextfieldHM.put(tfCoordinatesSRS1, crss.get(1));
            crsByTextfieldHM.put(tfCoordinatesSRS2, crss.get(2));

            lblSRS1.setText(crss.get(0).getShortname());
            lblSRS2.setText(crss.get(1).getShortname());
            lblSRS3.setText(crss.get(2).getShortname());

            cboAdditionalSRS.setVisible(false);
        } else if (crss.size() == 2) {
            lblSRS3.setVisible(false);
            cboAdditionalSRS.setVisible(false);
            tfCoordinatesSRS2.setVisible(false);
            lblSRS1.setText(crss.get(0).getShortname());
            lblSRS2.setText(crss.get(1).getShortname());
            crsByTextfieldHM.put(tfCoordinatesSRS0, crss.get(0));
            crsByTextfieldHM.put(tfCoordinatesSRS1, crss.get(1));
        } else if (crss.size() == 1) {
            cboAdditionalSRS.setVisible(false);
            tfCoordinatesSRS2.setVisible(false);
            lblSRS3.setVisible(false);
            lblSRS2.setVisible(false);
            tfCoordinatesSRS1.setVisible(false);
            crsByTextfieldHM.put(tfCoordinatesSRS0, crss.get(0));
            lblSRS1.setText(crss.get(0).getShortname());
        } else {
        }
        pack();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static GotoPointDialog getInstance() {
        if (instance == null) {
            synchronized (GotoPointDialog.class) {
                if (instance == null) {
                    instance = new GotoPointDialog();
                }
            }
        }
        try {
            instance.setInternalCenterCoordinatesCenterOfCamera();
        } catch (Exception e) {
        }
        return instance;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        lblMessage = new javax.swing.JLabel();
        panActions = new javax.swing.JPanel();
        btnPosition = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cboAdditionalSRS = new javax.swing.JComboBox<>();
        lblSRS1 = new javax.swing.JLabel();
        lblSRS2 = new javax.swing.JLabel();
        panMarkPoint = new javax.swing.JPanel();
        cbMarkPoint = new javax.swing.JCheckBox();
        lblIcon = new javax.swing.JLabel();
        tfCoordinatesSRS0 = new javax.swing.JTextField();
        tfCoordinatesSRS1 = new javax.swing.JTextField();
        tfCoordinatesSRS2 = new javax.swing.JTextField();
        lblSRS3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.title")); // NOI18N
        setIconImage(null);
        setModal(true);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        lblMessage.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.lblMessage.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 9, 3, 3);
        jPanel1.add(lblMessage, gridBagConstraints);

        panActions.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        btnPosition.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.btnPosition.text")); // NOI18N
        btnPosition.setMaximumSize(new java.awt.Dimension(100, 25));
        btnPosition.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnPositionActionPerformed(evt);
                }
            });
        panActions.add(btnPosition);

        btnCancel.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.btnCancel.text")); // NOI18N
        btnCancel.setMaximumSize(new java.awt.Dimension(90, 25));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnCancelActionPerformed(evt);
                }
            });
        panActions.add(btnCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 13, 14);
        jPanel1.add(panActions, gridBagConstraints);

        cboAdditionalSRS.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAdditionalSRS.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cboAdditionalSRSActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 14, 3, 3);
        jPanel1.add(cboAdditionalSRS, gridBagConstraints);

        lblSRS1.setText(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.lblSRS1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(14, 14, 3, 3);
        jPanel1.add(lblSRS1, gridBagConstraints);

        lblSRS2.setText(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.lblSRS2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 14, 3, 3);
        jPanel1.add(lblSRS2, gridBagConstraints);

        cbMarkPoint.setSelected(true);
        cbMarkPoint.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.cbMarkPoint.text"));        // NOI18N
        cbMarkPoint.setToolTipText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.cbMarkPoint.toolTipText")); // NOI18N
        cbMarkPoint.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbMarkPointItemStateChanged(evt);
                }
            });
        panMarkPoint.add(cbMarkPoint);

        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/markPoint.png")));                  // NOI18N
        lblIcon.setText(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.lblIcon.text")); // NOI18N
        lblIcon.setToolTipText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.cbMarkPoint.toolTipText"));                                                          // NOI18N
        panMarkPoint.add(lblIcon);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 10, 6);
        jPanel1.add(panMarkPoint, gridBagConstraints);

        tfCoordinatesSRS0.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.tfCoordinatesSRS0.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(14, 3, 3, 13);
        jPanel1.add(tfCoordinatesSRS0, gridBagConstraints);

        tfCoordinatesSRS1.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.tfCoordinatesSRS1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 13);
        jPanel1.add(tfCoordinatesSRS1, gridBagConstraints);

        tfCoordinatesSRS2.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.tfCoordinatesSRS2.text")); // NOI18N
        tfCoordinatesSRS2.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    tfCoordinatesSRS2ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 13);
        jPanel1.add(tfCoordinatesSRS2, gridBagConstraints);

        lblSRS3.setText(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.lblSRS3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 14, 3, 3);
        jPanel1.add(lblSRS3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 6, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnPositionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnPositionActionPerformed
        try {
            final Point gotoPointInTheRightCRS = CrsTransformer.transformToCurrentCrs(gotoPoint);
            final BoundingBox bb = new BoundingBox(gotoPointInTheRightCRS.getX(),
                    gotoPointInTheRightCRS.getY(),
                    gotoPointInTheRightCRS.getX(),
                    gotoPointInTheRightCRS.getY());
            CismapBroker.getInstance()
                    .getMappingComponent()
                    .gotoBoundingBox(
                        bb,
                        true,
                        false,
                        CismapBroker.getInstance().getMappingComponent().getAnimationDuration());
            visualizePosition(gotoPointInTheRightCRS.getX(), gotoPointInTheRightCRS.getY());
        } catch (final Exception skip) {
            log.error("Error in mniGotoPointActionPerformed", skip);                // NOI18N
        } finally {
            setVisible(false);
        }
    }                                                                               //GEN-LAST:event_btnPositionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
    }                                                                             //GEN-LAST:event_btnCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbMarkPointItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbMarkPointItemStateChanged
        visualizePosition();
    }                                                                              //GEN-LAST:event_cbMarkPointItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tfCoordinatesSRS2ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_tfCoordinatesSRS2ActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_tfCoordinatesSRS2ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboAdditionalSRSActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboAdditionalSRSActionPerformed
        crsByTextfieldHM.put(tfCoordinatesSRS2, (Crs)cboAdditionalSRS.getSelectedItem());
        syncPosition(null);
    }                                                                                    //GEN-LAST:event_cboAdditionalSRSActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @throws  UnknownCRSException  DOCUMENT ME!
     */
    private void setInternalCenterCoordinatesCenterOfCamera() throws UnknownCRSException {
        final BoundingBox c = CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera();
        final double x = (c.getX1() + c.getX2()) / 2;
        final double y = (c.getY1() + c.getY2()) / 2;
        final CoordinateSequence coordSeq = new CoordinateArraySequence(new Coordinate[] { new Coordinate(x, y) });
        final GeometryFactory gfactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
        gotoPoint = new Point(coordSeq, gfactory);
        syncPosition(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  origin  DOCUMENT ME!
     */
    private void syncPosition(final JTextField origin) {
        if (origin != null) {
            try {
                final String[] sa = origin.getText().split(","); // NOI18N
                final double x = Double.parseDouble(sa[0]);
                final double y = Double.parseDouble(sa[1]);
                final CoordinateSequence coordSeq = new CoordinateArraySequence(
                        new Coordinate[] { new Coordinate(x, y) });
                final GeometryFactory gfactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        CrsTransformer.extractSridFromCrs(crsByTextfieldHM.get(origin).getCode()));
                gotoPoint = new Point(coordSeq, gfactory);
            } catch (Exception e) {
            }
        }
        if (gotoPoint != null) {
            disableAllDocumentListeners();
            for (final JTextField tf : crsByTextfieldHM.keySet()) {
                if (tf != origin) {
                    fillCoordinateToTextField(
                        tf,
                        CrsTransformer.transformToGivenCrs(gotoPoint, crsByTextfieldHM.get(tf).getCode()));
                }
            }
            enableAllDocumentListeners();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void disableAllDocumentListeners() {
        docListenersEnabled = false;
    }

    /**
     * DOCUMENT ME!
     */
    private void enableAllDocumentListeners() {
        docListenersEnabled = true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tf  DOCUMENT ME!
     * @param  p   DOCUMENT ME!
     */
    private void fillCoordinateToTextField(final JTextField tf, final Point p) {
        final double x = p.getX();
        final double y = p.getY();
        final String pattern =
            CismapBroker.getInstance().crsFromCode(CrsTransformer.createCrsFromSrid(p.getSRID())).isMetric()
            ? "0.00" : "0.000000";
        tf.setText(StaticDecimalTools.round(pattern, x) + "," + StaticDecimalTools.round(pattern, y));
    }

    /**
     * DOCUMENT ME!
     */
    private void visualizePosition() {
        final BoundingBox c = CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera();
        final double x = (c.getX1() + c.getX2()) / 2;
        final double y = (c.getY1() + c.getY2()) / 2;
        visualizePosition(x, y);
    }

    /**
     * drafted from AbstractWFSForm visualizePosition().
     *
     * @param  x  DOCUMENT ME!
     * @param  y  DOCUMENT ME!
     */
    private void visualizePosition(final double x, final double y) {
        CismapBroker.getInstance().getMappingComponent().getHighlightingLayer().removeAllChildren();
        if (cbMarkPoint.isSelected()) {
            CismapBroker.getInstance().getMappingComponent().getHighlightingLayer().addChild(pMark);
            CismapBroker.getInstance().getMappingComponent().addStickyNode(pMark);

            final double screenx = CismapBroker.getInstance().getMappingComponent().getWtst().getScreenX(x);
            final double screeny = CismapBroker.getInstance().getMappingComponent().getWtst().getScreenY(y);
            pMark.setOffset(screenx, screeny);

            CismapBroker.getInstance().getMappingComponent().rescaleStickyNodes();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CoordDocumentListener implements DocumentListener {

        //~ Instance fields ----------------------------------------------------

        private JTextField tf;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CoordDocumentListener object.
         *
         * @param  tf  DOCUMENT ME!
         */
        public CoordDocumentListener(final JTextField tf) {
            this.tf = tf;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            if (docListenersEnabled) {
                syncPosition(tf);
            }
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            if (docListenersEnabled) {
                syncPosition(tf);
            }
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            if (docListenersEnabled) {
                syncPosition(tf);
            }
        }
    }
}
