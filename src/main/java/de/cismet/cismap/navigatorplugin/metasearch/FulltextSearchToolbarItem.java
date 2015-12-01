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
import Sirius.navigator.ui.RightStickyToolbarItem;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import de.cismet.cids.navigator.utils.CidsClientToolbarItem;

import de.cismet.cids.server.search.SearchResultListener;
import de.cismet.cids.server.search.SearchResultListenerProvider;
import de.cismet.cids.server.search.builtin.FullTextSearch;

import de.cismet.cismap.navigatorplugin.protocol.FulltextSearchProtocolStepImpl;

import de.cismet.commons.gui.protocol.ProtocolHandler;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsClientToolbarItem.class)
public class FulltextSearchToolbarItem extends javax.swing.JPanel implements CidsClientToolbarItem,
    RightStickyToolbarItem {

    //~ Static fields/initializers ---------------------------------------------

    public static final ImageIcon ICON_SEARCH = new ImageIcon(FulltextSearchToolbarItem.class.getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search.png"));
    public static final ImageIcon ICON_SEARCH_CASE = new ImageIcon(FulltextSearchToolbarItem.class.getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search_casesensitive.png"));
    public static final ImageIcon ICON_SEARCH_GEOM = new ImageIcon(FulltextSearchToolbarItem.class.getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search_geom.png"));
    public static final ImageIcon ICON_SEARCH_BOTH = new ImageIcon(FulltextSearchToolbarItem.class.getResource(
                "/de/cismet/cismap/navigatorplugin/metasearch/search_geom_casesensitive.png"));

    //~ Instance fields --------------------------------------------------------

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.tools.gui.JSearchTextField jSearchTextField1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FulltextSearchToolbarItem.
     */
    public FulltextSearchToolbarItem() {
        initComponents();

        final String searchTopicsString = SearchSearchTopicsDialog.instance()
                    .getPnlSearchTopics()
                    .getSelectedClassesString();
        final Collection<String> searchTopics = MetaSearch.instance().getSelectedSearchClassesForQuery();
        final String tooltipText = determineTooltipText(searchTopicsString);
        final String emptyText = determineEmptyText(searchTopics);
        jSearchTextField1.setEmptyText(emptyText);
        jSearchTextField1.setToolTipText(tooltipText);
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
                        final String searchTopicsString = SearchSearchTopicsDialog.instance()
                                    .getPnlSearchTopics()
                                    .getSelectedClassesString();
                        final Collection<String> searchTopics = MetaSearch.instance()
                                    .getSelectedSearchClassesForQuery();
                        final String tooltipText = determineTooltipText(searchTopicsString);
                        final String emptyText = determineEmptyText(searchTopics);
                        jSearchTextField1.setEmptyText(emptyText);
                        jSearchTextField1.setToolTipText(tooltipText);
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
        final java.awt.GridBagConstraints gridBagConstraints;

        jSearchTextField1 = new de.cismet.tools.gui.JSearchTextField();

        setMaximumSize(new java.awt.Dimension(200, 31));
        setMinimumSize(new java.awt.Dimension(200, 27));
        setPreferredSize(new java.awt.Dimension(200, 27));
        setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.weighty = 1.0;
        add(jSearchTextField1, gridBagConstraints);
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
     * @param   searchTopicsString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    final String determineTooltipText(final String searchTopicsString) {
        if (searchTopicsString.isEmpty()) {
            return org.openide.util.NbBundle.getMessage(
                    FulltextSearchToolbarItem.class,
                    "FulltextSearchToolbarItem.notopicsselected.text");
        } else {
            return org.openide.util.NbBundle.getMessage(
                    FulltextSearchToolbarItem.class,
                    "FulltextSearchToolbarItem.searchwithin.text") + searchTopicsString;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchTopics  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    final String determineEmptyText(final Collection searchTopics) {
        if (searchTopics.isEmpty()) {
            return org.openide.util.NbBundle.getMessage(
                    FulltextSearchToolbarItem.class,
                    "FulltextSearchToolbarItem.notopicsselected.text");
        } else if (searchTopics.size() == 1) {
            return org.openide.util.NbBundle.getMessage(
                    FulltextSearchToolbarItem.class,
                    "FulltextSearchToolbarItem.onetopicselected.text");
        } else {
            return searchTopics.size()
                        + org.openide.util.NbBundle.getMessage(
                            FulltextSearchToolbarItem.class,
                            "FulltextSearchToolbarItem.numoftopicsselected.text");
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
            if (fullTextSearch instanceof SearchResultListenerProvider) {
                ((SearchResultListenerProvider)fullTextSearch).setSearchResultListener(new SearchResultListener() {

                        @Override
                        public void searchDone(final List results) {
                            if (ProtocolHandler.getInstance().isRecordEnabled()) {
                                ProtocolHandler.getInstance()
                                        .recordStep(
                                            new FulltextSearchProtocolStepImpl(
                                                fullTextSearch,
                                                MetaSearch.instance().getSelectedSearchTopics(),
                                                results));
                            }
                        }
                    });
            }

            CidsSearchExecutor.searchAndDisplayResultsWithDialog(fullTextSearch);
        }
    } //GEN-LAST:event_jSearchTextField1ActionPerformed

    @Override
    public String getSorterString() {
        return "ZZZ";
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
