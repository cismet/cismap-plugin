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
package de.cismet.cismap.navigatorplugin;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;

import java.io.File;

import java.net.URI;

import java.util.List;

import javax.swing.JButton;

import de.cismet.cismap.commons.util.DnDUtils;

import de.cismet.commons.cismap.io.AddGeometriesToMapWizardAction;

import de.cismet.tools.gui.menu.CidsUiComponent;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiComponent.class)
public class AddGeomDnDButton extends JButton implements DropTargetListener, CidsUiComponent {

    //~ Instance fields --------------------------------------------------------

    private final transient DropTarget dropTarget = new DropTarget(this, this);

    //~ Methods ----------------------------------------------------------------

    @Override
    public AddGeometriesToMapWizardAction getAction() {
        return (AddGeometriesToMapWizardAction)super.getAction();
    }

    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)
                    || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)
                    || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)
                    || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragExit(final DropTargetEvent dte) {
        // noop
    }

    @Override
    public void drop(final DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            boolean performAction = false;
            if (dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                // unix drop
                final String uriList = (String)dtde.getTransferable().getTransferData(DnDUtils.URI_LIST_FLAVOR);
                final String[] uris = uriList.split(System.getProperty("line.separator"));      // NOI18N
                if (uris.length == 1) {
                    final File file = new File(new URI(uris[0].replaceFirst("localhost", ""))); // NOI18N
                    dtde.dropComplete(true);
                    getAction().setInputFile(file);
                    performAction = true;
                } else {
                    dtde.dropComplete(false);
                }
            } else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // win drop
                @SuppressWarnings("unchecked")
                final List<File> data = (List)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (data.size() == 1) {
                    final File file = data.get(0);
                    dtde.dropComplete(true);
                    getAction().setInputFile(file);
                    performAction = true;
                } else {
                    dtde.dropComplete(false);
                }
            } else {
                final String data = (String)dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                dtde.dropComplete(true);

                // String drop, empty file
                getAction().setInputData(data);
                performAction = true;
            }

            if (performAction) {
                final ActionEvent ae = new ActionEvent(this, 1, "data_dnd");

                // move the actual execution of the action to the end of the eventqueue so that the OS may complete
                // the DnD action
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            getAction().actionPerformed(ae);
                        }
                    });
            }
        } catch (final Exception e) {
            dtde.dropComplete(false);
        }
    }

    @Override
    public String getValue(final String string) {
        return "AddGeomDnDButton";
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
