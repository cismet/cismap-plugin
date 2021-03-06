/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * OptionsDialog.java
 *
 * Created on 31.08.2009, 12:16:22
 */
package de.cismet.cismap.navigatorplugin;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class NetworkOptionsDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    /** A return status code - returned if Cancel button has been pressed. */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed. */
    public static final int RET_OK = 1;

    //~ Instance fields --------------------------------------------------------

    private int returnStatus = RET_CANCEL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JTextField chkPort;
    private javax.swing.JCheckBox chkUseProxy;
    private javax.swing.JLabel labHost;
    private javax.swing.JLabel labPort;
    private javax.swing.JPanel panProxy;
    private javax.swing.JTabbedPane tabOptions;
    private javax.swing.JTextField txtHost;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ProxyDialog2.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public NetworkOptionsDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        butCancel = new javax.swing.JButton();
        tabOptions = new javax.swing.JTabbedPane();
        panProxy = new javax.swing.JPanel();
        labHost = new javax.swing.JLabel();
        txtHost = new javax.swing.JTextField();
        chkPort = new javax.swing.JTextField();
        labPort = new javax.swing.JLabel();
        chkUseProxy = new javax.swing.JCheckBox();
        butOk = new javax.swing.JButton();

        setTitle(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.JDialog.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosing(final java.awt.event.WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        butCancel.setText(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.butCancel.text")); // NOI18N
        butCancel.setPreferredSize(new java.awt.Dimension(50, 29));
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });

        labHost.setText(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.labHost.text")); // NOI18N

        txtHost.setMinimumSize(new java.awt.Dimension(100, 27));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkUseProxy,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                txtHost,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkUseProxy,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                chkPort,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        labPort.setText(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.labPort.text")); // NOI18N

        chkUseProxy.setText(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.chkUseProxy.text")); // NOI18N
        chkUseProxy.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkUseProxyActionPerformed(evt);
                }
            });

        final javax.swing.GroupLayout panProxyLayout = new javax.swing.GroupLayout(panProxy);
        panProxy.setLayout(panProxyLayout);
        panProxyLayout.setHorizontalGroup(
            panProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panProxyLayout.createSequentialGroup().addContainerGap().addGroup(
                    panProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        panProxyLayout.createSequentialGroup().addComponent(chkUseProxy).addGap(258, 258, 258))
                                .addGroup(
                                    panProxyLayout.createSequentialGroup().addComponent(labHost).addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                                        txtHost,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        192,
                                        Short.MAX_VALUE).addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(labPort)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(
                                            chkPort,
                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                            77,
                                            javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()))));
        panProxyLayout.setVerticalGroup(
            panProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                panProxyLayout.createSequentialGroup().addContainerGap().addComponent(chkUseProxy).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                    panProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                        labHost).addComponent(
                        txtHost,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                        chkPort,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(labPort)).addContainerGap(
                    25,
                    Short.MAX_VALUE)));

        tabOptions.addTab(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.panProxy.tabTitle"),
            panProxy); // NOI18N

        butOk.setText(org.openide.util.NbBundle.getMessage(
                NetworkOptionsDialog.class,
                "NetworkOptionsDialog.butOK.text")); // NOI18N
        butOk.setPreferredSize(new java.awt.Dimension(50, 29));
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        getRootPane().setDefaultButton(butOk);

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addContainerGap().addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(tabOptions)
                                .addGroup(
                                    layout.createSequentialGroup().addComponent(
                                        butOk,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        90,
                                        javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                                        butCancel,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        90,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))).addGap(14, 14, 14)));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addContainerGap().addComponent(
                    tabOptions,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    129,
                    Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                        butCancel,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                        butOk,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));

        bindingGroup.bind();

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        doClose(RET_CANCEL);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * Closes the dialog.
     *
     * @param  evt  DOCUMENT ME!
     */
    private void closeDialog(final java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }                                                                //GEN-LAST:event_closeDialog

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        doClose(RET_OK);
    }                                                                         //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkUseProxyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkUseProxyActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_chkUseProxyActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  retStatus  DOCUMENT ME!
     */
    private void doClose(final int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final NetworkOptionsDialog dialog = new NetworkOptionsDialog(new javax.swing.JFrame(), true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    dialog.setVisible(true);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  activated  DOCUMENT ME!
     */
    public void setProxyActivated(final boolean activated) {
        chkUseProxy.setSelected(activated);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isProxyActivated() {
        return chkUseProxy.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  host  DOCUMENT ME!
     */
    public void setProxyHost(final String host) {
        txtHost.setText(host);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  port  DOCUMENT ME!
     */
    public void setProxyPort(final int port) {
        chkPort.setText(String.valueOf(port));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProxyHost() {
        return txtHost.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getProxyPort() {
        return Integer.valueOf(chkPort.getText());
    }
}
