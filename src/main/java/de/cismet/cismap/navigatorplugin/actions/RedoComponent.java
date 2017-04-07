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

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.memento.MementoInterface;

import de.cismet.tools.gui.menu.CidsUiAction;
import de.cismet.tools.gui.menu.CidsUiComponent;

import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiComponent.class)
public class RedoComponent extends JButton implements CidsUiComponent, Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RedoComponent.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RedoComponent object.
     */
    public RedoComponent() {
        setAction(new RedoAction());
        setEnabled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (mapC != null) {
            ((Observable)mapC.getMemRedo()).addObserver(RedoComponent.this);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void update(final Observable o, final Object arg) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (o.equals(mapC.getMemRedo())) {
            if (arg.equals(MementoInterface.ACTIVATE) && !isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("activate REDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setEnabled(true);
                        }
                    });
            } else if (arg.equals(MementoInterface.DEACTIVATE) && isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deactivate REDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setEnabled(false);
                        }
                    });
            }
        }
    }

    @Override
    public String getValue(final String key) {
        return "RedoAction";
    }

    @Override
    public Component getComponent() {
        return this;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class RedoAction extends AbstractAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RefreshAction object.
         */
        public RedoAction() {
            final String tooltip = org.openide.util.NbBundle.getMessage(RedoComponent.class,
                    "RedoAction.toolTipText");
            putValue(SHORT_DESCRIPTION, tooltip);
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/redo.png")));
            putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/redo.png")));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                LOG.info("REDO"); // NOI18N

                final CustomAction a = mapC.getMemRedo().getLastAction();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("... execute action: " + a.info()); // NOI18N
                }

                try {
                    a.doAction();
                } catch (Exception ex) {
                    LOG.error("Error while executing an action", ex); // NOI18N
                }

                final CustomAction inverse = a.getInverse();
                mapC.getMemUndo().addAction(inverse);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("... new action on UNDO stack: " + inverse); // NOI18N
                    LOG.debug("... completed");                            // NOI18N
                }
            }
        }
    }
}
