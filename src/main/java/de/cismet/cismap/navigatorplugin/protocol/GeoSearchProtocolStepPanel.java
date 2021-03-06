/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.protocol;

import Sirius.navigator.search.CidsServerSearchMetaObjectNodeWrapper;
import Sirius.navigator.types.treenode.ObjectTreeNode;

import Sirius.server.middleware.types.MetaObjectNode;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;

import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class GeoSearchProtocolStepPanel extends AbstractProtocolStepPanel<GeoSearchProtocolStep>
        implements ConnectionContextProvider {

    //~ Instance fields --------------------------------------------------------

    private final ConnectionContext connectionContext = ConnectionContext.createDummy();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Sirius.navigator.search.CidsServerSearchProtocolStepPanel cidsServerSearchProtocolStepPanel1;
    private de.cismet.cismap.navigatorplugin.protocol.GeometryProtocolStepPanel geomSearchProtocolStepPanel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXHyperlink jXHyperlink1;
    private javax.swing.JLabel lblIconNone;
    private javax.swing.JLabel lblTitle;
    private de.cismet.cismap.navigatorplugin.protocol.MetaObjectNodeTree metaSearchProtocolStepPanelSearchObjectsTree1;
    private de.cismet.cismap.navigatorplugin.protocol.SearchTopicsProtocolStepPanel searchTopicsProtocolStepPanel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSearchProtocolStepPanel object.
     */
    public GeoSearchProtocolStepPanel() {
        this(new GeoSearchProtocolStepImpl(
                "",
                "",
                new HashSet<GeoSearchProtocolStepSearchTopic>(),
                new ArrayList<CidsServerSearchMetaObjectNodeWrapper>(),
                new ArrayList<CidsServerSearchMetaObjectNodeWrapper>()));
    }

    /**
     * Creates new form MetaSearchProtocolStepPanel.
     *
     * @param  geoSearchProtocolStep  DOCUMENT ME!
     */
    public GeoSearchProtocolStepPanel(final GeoSearchProtocolStep geoSearchProtocolStep) {
        super(geoSearchProtocolStep);
        initComponents();

        if (!metaSearchProtocolStepPanelSearchObjectsTree1.getResultNodes().isEmpty()) {
            final int maxSize = 10;

            final JLabel dummy = (JLabel)metaSearchProtocolStepPanelSearchObjectsTree1.getCellRenderer()
                        .getTreeCellRendererComponent(
                                metaSearchProtocolStepPanelSearchObjectsTree1,
                                new ObjectTreeNode(
                                    (MetaObjectNode)metaSearchProtocolStepPanelSearchObjectsTree1.getResultNodes().get(
                                        0),
                                    getConnectionContext()),
                                false,
                                false,
                                false,
                                0,
                                false);
            final int height;

            if (metaSearchProtocolStepPanelSearchObjectsTree1.getResultNodes().size() > maxSize) {
                height = 4 + (dummy.getPreferredSize().height * maxSize);
            } else {
                height = 4
                            + (dummy.getPreferredSize().height
                                * metaSearchProtocolStepPanelSearchObjectsTree1.getResultNodes().size());
            }

            jScrollPane2.setPreferredSize(new Dimension((int)jScrollPane2.getPreferredSize().getWidth(), height));
        }

        setSearchObjectsPanelVisible(false);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<MetaObjectNode> getSearchObjectNodesList() {
        if (getProtocolStep() != null) {
            final Collection<MetaObjectNode> searchObjectNodes = ((GeoSearchProtocolStep)getProtocolStep())
                        .getSearchObjectNodes();
            if (searchObjectNodes != null) {
                return searchObjectNodes;
            }
        }
        return new ArrayList<MetaObjectNode>();
    }

    @Override
    public Component getIconComponent() {
        return lblIconNone;
    }

    @Override
    public Component getTitleComponent() {
        return lblTitle;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblIconNone = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        cidsServerSearchProtocolStepPanel1 = new Sirius.navigator.search.CidsServerSearchProtocolStepPanel(
                ((GeoSearchProtocolStepImpl)getProtocolStep()).getCidsServerSearchProtocolStep(),
                getConnectionContext());
        jPanel1 = new javax.swing.JPanel();
        jXHyperlink1 = new org.jdesktop.swingx.JXHyperlink();
        jScrollPane2 = new javax.swing.JScrollPane();
        try {
            metaSearchProtocolStepPanelSearchObjectsTree1 =
                new de.cismet.cismap.navigatorplugin.protocol.MetaObjectNodeTree(getConnectionContext());
        } catch (java.lang.Exception e1) {
            e1.printStackTrace();
        }
        searchTopicsProtocolStepPanel1 = new de.cismet.cismap.navigatorplugin.protocol.SearchTopicsProtocolStepPanel(
                ((GeoSearchProtocolStepImpl)getProtocolStep()).getSearchTopicsProtocolStep());
        geomSearchProtocolStepPanel1 = new de.cismet.cismap.navigatorplugin.protocol.GeometryProtocolStepPanel(
                ((GeoSearchProtocolStepImpl)getProtocolStep()).getGeometryProtocolStep());

        lblIconNone.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIconNone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearch.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            lblIconNone,
            org.openide.util.NbBundle.getMessage(
                GeoSearchProtocolStepPanel.class,
                "GeoSearchProtocolStepPanel.lblIconNone.text"));                                            // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(
                GeoSearchProtocolStepPanel.class,
                "GeoSearchProtocolStepPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                GeoSearchProtocolStepPanel.class,
                "GeoSearchProtocolStepPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(
                GeoSearchProtocolStepPanel.class,
                "GeoSearchProtocolStepPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            lblTitle,
            org.openide.util.NbBundle.getMessage(
                GeoSearchProtocolStepPanel.class,
                "GeoSearchProtocolStepPanel.lblTitle.text")); // NOI18N

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(cidsServerSearchProtocolStepPanel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jXHyperlink1,
            org.openide.util.NbBundle.getMessage(
                GeoSearchProtocolStepPanel.class,
                "GeoSearchProtocolStepPanel.jXHyperlink1.text_hide_multi")); // NOI18N
        jXHyperlink1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jXHyperlink1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jXHyperlink1, gridBagConstraints);

        metaSearchProtocolStepPanelSearchObjectsTree1.setResultNodes(getSearchObjectNodesList().toArray(
                new MetaObjectNode[0]));
        jScrollPane2.setViewportView(metaSearchProtocolStepPanelSearchObjectsTree1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(jScrollPane2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(searchTopicsProtocolStepPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(geomSearchProtocolStepPanel1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jXHyperlink1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jXHyperlink1ActionPerformed
        toggleSearchObjectsPanelVisibility();
    }                                                                                //GEN-LAST:event_jXHyperlink1ActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void toggleSearchObjectsPanelVisibility() {
        setSearchObjectsPanelVisible(!jScrollPane2.isVisible());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    private void setSearchObjectsPanelVisible(final boolean visible) {
        jScrollPane2.setVisible(visible);

        final int size;
        if (getProtocolStep() != null) {
            if ((getProtocolStep().getSearchObjectNodes() == null)
                        || getProtocolStep().getSearchObjectNodes().isEmpty()) {
                size = 0;
            } else {
                size = getProtocolStep().getSearchObjectNodes().size();
            }

            jPanel1.setVisible(size > 0);

            if (size == 0) {
                Mnemonics.setLocalizedText(
                    jXHyperlink1,
                    NbBundle.getMessage(
                        GeoSearchProtocolStepPanel.class,
                        "GeoSearchProtocolStepPanel.jXHyperlink1.text_empty"));
            } else {
                if (visible) {
                    if (size > 1) {
                        Mnemonics.setLocalizedText(
                            jXHyperlink1,
                            NbBundle.getMessage(
                                GeoSearchProtocolStepPanel.class,
                                "GeoSearchProtocolStepPanel.jXHyperlink1.text_hide_multi",
                                String.valueOf(size)));
                    } else {
                        Mnemonics.setLocalizedText(
                            jXHyperlink1,
                            NbBundle.getMessage(
                                GeoSearchProtocolStepPanel.class,
                                "GeoSearchProtocolStepPanel.jXHyperlink1.text_hide_single",
                                String.valueOf(size)));
                    }
                } else {
                    if (size > 1) {
                        Mnemonics.setLocalizedText(
                            jXHyperlink1,
                            NbBundle.getMessage(
                                GeoSearchProtocolStepPanel.class,
                                "GeoSearchProtocolStepPanel.jXHyperlink1.text_show_multi",
                                String.valueOf(size)));
                    } else {
                        Mnemonics.setLocalizedText(
                            jXHyperlink1,
                            NbBundle.getMessage(
                                GeoSearchProtocolStepPanel.class,
                                "GeoSearchProtocolStepPanel.jXHyperlink1.text_show_single",
                                String.valueOf(size)));
                    }
                }
            }
        }

        revalidate();
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
