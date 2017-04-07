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

import java.awt.Component;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.historybutton.HistoryModel;
import de.cismet.tools.gui.historybutton.HistoryModelListener;
import de.cismet.tools.gui.historybutton.JHistoryButton;
import de.cismet.tools.gui.menu.CidsUiComponent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiComponent.class)
public class JHistoryBackButton extends JHistoryButton implements CidsUiComponent {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JHistoryBackButton object.
     */
    public JHistoryBackButton() {
        super.setDirection(JHistoryButton.DIRECTION_BACKWARD);
        setIcon(new javax.swing.ImageIcon(this.getClass().getResource("/images/back.png")));
        setBorderPainted(false);
        setFocusPainted(false);

        if (CismapBroker.getInstance().getMappingComponent() != null) {
            setHistoryModel(CismapBroker.getInstance().getMappingComponent());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public HistoryModel getHistoryModel() {
        if (super.getHistoryModel() == null) {
            setHistoryModel(CismapBroker.getInstance().getMappingComponent());
        }

        return super.getHistoryModel();
    }

    @Override
    public void historyActionPerformed() {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().back(true);
        }
    }

    @Override
    public String getValue(final String key) {
        return "JHistoryBackButton";
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
