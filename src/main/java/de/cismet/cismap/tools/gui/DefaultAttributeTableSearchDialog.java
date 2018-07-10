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
package de.cismet.cismap.tools.gui;

import javax.swing.JDialog;

import de.cismet.cids.search.QuerySearch;
import de.cismet.cids.search.QuerySearchMethod;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableSearchPanel;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = AttributeTableSearchPanel.class)
public class DefaultAttributeTableSearchDialog implements AttributeTableSearchPanel {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void openPanel(final AttributeTable table, final AbstractFeatureService service) {
        final SelectInAttributeTableSearchMethod selectionMethod = new SelectInAttributeTableSearchMethod(table);
        final AddToSelectionAttributeTableSearchMethod addToSelectionMethod =
            new AddToSelectionAttributeTableSearchMethod(table);
        final RemoveFromSelectionAttributeTableSearchMethod removeFromSelectionMethod =
            new RemoveFromSelectionAttributeTableSearchMethod(table);
        final SelectInSelectionAttributeTableSearchMethod selectFromSelectionMethod =
            new SelectInSelectionAttributeTableSearchMethod(table);

        final QuerySearch search = new QuerySearch(
                null,
                new String[] {},
                new AbstractFeatureService[] { service },
                new QuerySearchMethod[] {
                    selectionMethod,
                    addToSelectionMethod,
                    removeFromSelectionMethod,
                    selectFromSelectionMethod
                });
        search.initWithConnectionContext(ConnectionContext.createDummy());
        search.enableLineWrap(true);

        final JDialog dialog = new JDialog(StaticSwingTools.getParentFrame(table), false);
        initDialog(dialog, search, service);
        dialog.setSize(430, 500);
        StaticSwingTools.showDialog(dialog);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dialog   DOCUMENT ME!
     * @param  search   DOCUMENT ME!
     * @param  service  DOCUMENT ME!
     */
    private void initDialog(final JDialog dialog, final QuerySearch search, final AbstractFeatureService service) {
        final java.awt.GridBagConstraints gridBagConstraints;

        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setTitle(org.openide.util.NbBundle.getMessage(
                DefaultAttributeTableSearchDialog.class,
                "DefaultAttributeTableSearchDialog.title")); // NOI18N
        dialog.getContentPane().setLayout(new java.awt.GridBagLayout());
        dialog.getContentPane().removeAll();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        dialog.getContentPane().add(search, gridBagConstraints);
    }
}
