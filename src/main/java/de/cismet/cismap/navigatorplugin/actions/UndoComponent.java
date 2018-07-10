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
public class UndoComponent extends JButton implements CidsUiComponent, Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(UndoComponent.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshAction object.
     */
    public UndoComponent() {
        setAction(new UndoAction());
        setEnabled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (mapC != null) {
            ((Observable)mapC.getMemUndo()).addObserver(this);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void update(final Observable o, final Object arg) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (o.equals(mapC.getMemUndo())) {
            if (arg.equals(MementoInterface.ACTIVATE) && !isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("activate UNDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setEnabled(true);
                        }
                    });
            } else if (arg.equals(MementoInterface.DEACTIVATE) && isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deactivate UNDO button"); // NOI18N
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
        return "UndoAction";
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
    private class UndoAction extends AbstractAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RefreshAction object.
         */
        public UndoAction() {
            final String tooltip = org.openide.util.NbBundle.getMessage(UndoComponent.class,
                    "UndoAction.toolTipText");
            putValue(SHORT_DESCRIPTION, tooltip);
            putValue(SMALL_ICON, new javax.swing.ImageIcon(getClass().getResource("/images/undo.png")));
            putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource("/images/undo.png")));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "UndoAction");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

            if (mapC != null) {
                LOG.info("UNDO"); // NOI18N

                final CustomAction a = mapC.getMemUndo().getLastAction();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("... execute action: " + a.info()); // NOI18N
                }

                try {
                    a.doAction();
                } catch (Exception ex) {
                    LOG.error("Error while executing action", ex); // NOI18N
                }

                final CustomAction inverse = a.getInverse();
                mapC.getMemRedo().addAction(inverse);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("... new action on REDO stack: " + inverse); // NOI18N
                    LOG.debug("... completed");                            // NOI18N
                }
            }
        }
    }
}
