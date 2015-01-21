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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.StaticDecimalTools;

import de.cismet.tools.gui.StaticSwingTools;

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

    private MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ImageIcon mark = new javax.swing.ImageIcon(getClass().getResource(
                "/images/markPoint.png")); // NOI18N
    private FixedPImage pMark = new FixedPImage(mark.getImage());
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnPosition;
    private javax.swing.JCheckBox cbMarkPoint;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JTextField tfCoordinates;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form GotoPointDialog.
     */
    private GotoPointDialog() {
        super();
        initComponents();
        pMark.setVisible(true);
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
        instance.setTfCoordinatesTextToCenterOfCamera();
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
        tfCoordinates = new javax.swing.JTextField();
        cbMarkPoint = new javax.swing.JCheckBox();
        lblMessage = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();
        lblImage = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnPosition = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.title")); // NOI18N
        setIconImage(null);
        setModal(true);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        tfCoordinates.setText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.tfCoordinates.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 233;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel1.add(tfCoordinates, gridBagConstraints);

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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel1.add(cbMarkPoint, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel1.add(lblMessage, gridBagConstraints);

        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/markPoint.png")));                  // NOI18N
        lblIcon.setText(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.lblIcon.text")); // NOI18N
        lblIcon.setToolTipText(org.openide.util.NbBundle.getMessage(
                GotoPointDialog.class,
                "GotoPointDialog.cbMarkPoint.toolTipText"));                                                          // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel1.add(lblIcon, gridBagConstraints);

        lblImage.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        lblImage.setText(org.openide.util.NbBundle.getMessage(GotoPointDialog.class, "GotoPointDialog.lblImage.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 8);
        jPanel1.add(lblImage, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

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
        jPanel2.add(btnPosition);

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
        jPanel2.add(btnCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 3, 3, 3);
        jPanel1.add(jPanel2, gridBagConstraints);

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
            final String[] sa = tfCoordinates.getText().split(",");                 // NOI18N
            final Double gotoX = new Double(sa[0]);
            final Double gotoY = new Double(sa[1]);
            final BoundingBox bb = new BoundingBox(gotoX, gotoY, gotoX, gotoY);
            mapC.gotoBoundingBox(bb, true, false, mapC.getAnimationDuration());
            visualizePosition(gotoX, gotoY);
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
     */
    private void setTfCoordinatesTextToCenterOfCamera() {
        final BoundingBox c = mapC.getCurrentBoundingBoxFromCamera();
        final double x = (c.getX1() + c.getX2()) / 2;
        final double y = (c.getY1() + c.getY2()) / 2;
        final String pattern = (CismapBroker.getInstance().getSrs().isMetric() ? "0.00" : "0.000000");

        tfCoordinates.setText(StaticDecimalTools.round(pattern, x) + "," + StaticDecimalTools.round(pattern, y));
    }

    /**
     * DOCUMENT ME!
     */
    private void visualizePosition() {
        final BoundingBox c = mapC.getCurrentBoundingBoxFromCamera();
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
        mapC.getHighlightingLayer().removeAllChildren();
        if (cbMarkPoint.isSelected()) {
            mapC.getHighlightingLayer().addChild(pMark);
            mapC.addStickyNode(pMark);

            final double screenx = mapC.getWtst().getScreenX(x);
            final double screeny = mapC.getWtst().getScreenY(y);
            pMark.setOffset(screenx, screeny);

            mapC.rescaleStickyNodes();
        }
    }
}
