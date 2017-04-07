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

import de.cismet.tools.gui.historybutton.JHistoryButton;
import de.cismet.tools.gui.menu.CidsUiComponent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiComponent.class)
public class JHistoryForwardButton extends JHistoryBackButton implements CidsUiComponent {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JHistoryForwardButton object.
     */
    public JHistoryForwardButton() {
        super.setDirection(JHistoryButton.DIRECTION_FORWARD);
        setIcon(new javax.swing.ImageIcon(this.getClass().getResource("/images/forward.png")));
        setBorderPainted(false);
        setFocusPainted(false);

        if (CismapBroker.getInstance().getMappingComponent() != null) {
            setHistoryModel(CismapBroker.getInstance().getMappingComponent());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void historyActionPerformed() {
        if (CismapBroker.getInstance().getMappingComponent() != null) {
            CismapBroker.getInstance().getMappingComponent().back(true);
        }
    }

    @Override
    public String getValue(final String key) {
        return "JHistoryForwardButton";
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
