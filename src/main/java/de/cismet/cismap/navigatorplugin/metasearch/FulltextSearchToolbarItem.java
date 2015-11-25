/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.metasearch;

import Sirius.navigator.resource.PropertyManager;
import Sirius.navigator.search.CidsSearchExecutor;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import de.cismet.cids.navigator.utils.CidsClientToolbarItem;

import de.cismet.cids.server.search.builtin.FullTextSearch;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsClientToolbarItem.class)
public class FulltextSearchToolbarItem extends javax.swing.JPanel implements CidsClientToolbarItem {

    //~ Static fields/initializers ---------------------------------------------

    public static final ImageIcon ICON_SEARCH = new ImageIcon(FulltextSearchToolbarItem.class.getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search.png"));

    //~ Instance fields --------------------------------------------------------

    private final ImageIcon ICON_SEARCH_CASE = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search_casesensitive.png"));
    private final ImageIcon ICON_SEARCH_GEOM = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search_geom.png"));
    private final ImageIcon ICON_SEARCH_BOTH = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search_geom_casesensitive.png"));

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private de.cismet.tools.gui.JSearchTextField jSearchTextField1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FulltextSearchToolbarItem.
     */
    public FulltextSearchToolbarItem() {
        initComponents();

        final String searchTopics = SearchSearchTopicsDialog.instance().getPnlSearchTopics().getSelectedClassesString();
        final String tooltip = determineSearchTopicText(searchTopics);
        jSearchTextField1.setToolTipText(tooltip);
        jSearchTextField1.setEmptyText(tooltip);
        jSearchTextField1.setEnabled(!searchTopics.isEmpty());

        final ImageIcon icon = determineSearchIcon(
                SearchSearchTopicsDialog.instance().getModel().isCaseSensitiveEnabled(),
                SearchSearchTopicsDialog.instance().getModel().isSearchGeometryEnabled());
        jSearchTextField1.setSearchIcon(icon);

        SearchSearchTopicsDialog.instance().getModel().addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    final String prop = evt.getPropertyName();
                    if (SearchTopicsDialogModel.PROPERTY_CASESENSITIVE.equals(prop)) {
                        final ImageIcon icon = determineSearchIcon(
                                SearchSearchTopicsDialog.instance().getModel().isCaseSensitiveEnabled(),
                                SearchSearchTopicsDialog.instance().getModel().isSearchGeometryEnabled());
                        jSearchTextField1.setSearchIcon(icon);
                        jSearchTextField1.repaint();
                    } else if (SearchTopicsDialogModel.PROPERTY_SEARCHCLASSESSTRING.equals(prop)) {
                        final String searchTopics = SearchSearchTopicsDialog.instance()
                                    .getPnlSearchTopics()
                                    .getSelectedClassesString();
                        final String tooltip = determineSearchTopicText(searchTopics);
                        jSearchTextField1.setToolTipText(tooltip);
                        jSearchTextField1.setEmptyText(tooltip);
                        jSearchTextField1.setEnabled(!searchTopics.isEmpty());
                        jSearchTextField1.repaint();
                    } else if (SearchTopicsDialogModel.PROPERTY_SEARCHGEOMETRY.equals(prop)) {
                        final ImageIcon icon = determineSearchIcon(
                                SearchSearchTopicsDialog.instance().getModel().isCaseSensitiveEnabled(),
                                SearchSearchTopicsDialog.instance().getModel().isSearchGeometryEnabled());
                        jSearchTextField1.setSearchIcon(icon);
                        jSearchTextField1.repaint();
                    } else if (SearchTopicsDialogModel.PROPERTY_SEARCHTEXT.equals(prop)) {
                        jSearchTextField1.setText((String)evt.getNewValue());
                        jSearchTextField1.repaint();
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jSearchTextField1 = new de.cismet.tools.gui.JSearchTextField();

        setMaximumSize(new java.awt.Dimension(200, 31));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(200, 31));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jSearchTextField1.setText(org.openide.util.NbBundle.getMessage(
                FulltextSearchToolbarItem.class,
                "FulltextSearchToolbarItem.jSearchTextField1.text"));                                      // NOI18N
        jSearchTextField1.setAbortIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/navigatorplugin/metasearch/search_abort.png"))); // NOI18N
        jSearchTextField1.setSearchIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/navigatorplugin/metasearch/search.png")));       // NOI18N
        jSearchTextField1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jSearchTextField1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jSearchTextField1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        add(jPanel1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param   caseSensitiveEnabled  DOCUMENT ME!
     * @param   mapSearchEnabled      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ImageIcon determineSearchIcon(final boolean caseSensitiveEnabled, final boolean mapSearchEnabled) {
        if (caseSensitiveEnabled && mapSearchEnabled) {
            return ICON_SEARCH_BOTH;
        } else if (caseSensitiveEnabled && !mapSearchEnabled) {
            return ICON_SEARCH_CASE;
        } else if (mapSearchEnabled) {
            return ICON_SEARCH_GEOM;
        } else {
            return ICON_SEARCH;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchTopics  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    final String determineSearchTopicText(final String searchTopics) {
        if (searchTopics.isEmpty()) {
            return org.openide.util.NbBundle.getMessage(
                    FulltextSearchToolbarItem.class,
                    "FulltextSearchToolbarItem.notopicsselected.text");
        } else {
            return org.openide.util.NbBundle.getMessage(
                    FulltextSearchToolbarItem.class,
                    "FulltextSearchToolbarItem.searchwithin.text") + searchTopics;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jSearchTextField1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jSearchTextField1ActionPerformed
        final String searchText = jSearchTextField1.getText();

        final Collection<String> searchTopics = MetaSearch.instance().getSelectedSearchClassesForQuery();
        if ((searchText != null) && !searchText.trim().isEmpty() && (searchTopics.size() > 0)) {
            final SearchTopicsDialogModel model = SearchSearchTopicsDialog.instance().getModel();

            // default search is always present
            final FullTextSearch fullTextSearch = Lookup.getDefault().lookup(FullTextSearch.class);
            fullTextSearch.setSearchText(jSearchTextField1.getText());
            fullTextSearch.setCaseSensitive(model.isCaseSensitiveEnabled());
            fullTextSearch.setGeometry(SearchSearchTopicsDialog.instance().createSearchGeometry());
            fullTextSearch.setValidClassesFromStrings(searchTopics);

            CidsSearchExecutor.searchAndDisplayResultsWithDialog(fullTextSearch);
        }
    } //GEN-LAST:event_jSearchTextField1ActionPerformed

    @Override
    public String getSorterString() {
        return "001";
    }

    @Override
    public boolean isVisible() {
        return PropertyManager.getManager().isFulltextSearchToolbarItemEnabled();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final JFrame frame = new JFrame();
        frame.setContentPane(new FulltextSearchToolbarItem());
        frame.setVisible(true);
    }
}
