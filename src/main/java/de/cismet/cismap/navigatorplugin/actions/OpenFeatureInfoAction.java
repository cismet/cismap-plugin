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
package de.cismet.cismap.navigatorplugin.actions;

import Sirius.navigator.ui.ComponentRegistry;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanelListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.menu.CidsUiAction;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SELECTED_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiAction.class)
public class OpenFeatureInfoAction extends AbstractAction implements CidsUiAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OpenFeatureInfoAction.class);

    //~ Instance fields --------------------------------------------------------

    private JDialog dialog = null;
    private FeatureInfoPanel featureInfoPanel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new OpenFeatureInfoAction object.
     */
    public OpenFeatureInfoAction() {
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                OpenFeatureInfoAction.class,
                "OpenFeatureInfoAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                OpenFeatureInfoAction.class,
                "OpenFeatureInfoAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                OpenFeatureInfoAction.class,
                "OpenFeatureInfoAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(CidsUiAction.CIDS_ACTION_KEY, "OpenFeatureInfo");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        initDialog();
        CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.FEATURE_INFO_MULTI_GEOM);
        putValue(SELECTED_KEY, Boolean.TRUE);
        StaticSwingTools.showDialog(dialog);
    }

    /**
     * Initializes the info dialog, if it is not already initialized.
     */
    private void initDialog() {
        if (dialog == null) {
            dialog = new JDialog(ComponentRegistry.getRegistry().getMainWindow(),
                    NbBundle.getMessage(OpenFeatureInfoAction.class, "OpenFeatureInfoAction.actionPerformed.JDialog"),
                    false);
            featureInfoPanel = new FeatureInfoPanel(CismapBroker.getInstance().getMappingComponent(), null);
            dialog.add(featureInfoPanel);
            dialog.setAlwaysOnTop(true);
            dialog.setSize(350, 550);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(final WindowEvent e) {
                        if (featureInfoPanel.dispose()) {
                            dialog.setVisible(false);
                        }

                        CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.ZOOM);
                    }

                    @Override
                    public void windowClosed(final WindowEvent e) {
                        windowClosing(e);
                    }
                });
        }
    }

    /**
     * opens the info dialog.
     */
    public void showDialog() {
        initDialog();
        StaticSwingTools.showDialog(dialog);
    }

    /**
     * DOCUMENT ME!
     */
    public void showAllFeature() {
        featureInfoPanel.showAllFeatures();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addFeatureInfoPanelListener(final FeatureInfoPanelListener listener) {
        if (featureInfoPanel != null) {
            featureInfoPanel.addFeatureInfoPanelListeners(listener);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        if (featureInfoPanel != null) {
            featureInfoPanel.dispose();
        }
    }
}
