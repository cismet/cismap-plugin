/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.metasearch;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class SearchTopic extends AbstractAction implements Comparable<SearchTopic> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SearchTopic.class);

    public static final String SELECTED = "selected";

    //~ Instance fields --------------------------------------------------------

    private final Collection<SearchClass> searchClasses;
    private final String name;
    private final String description;
    private final String key;
    private final String iconName;
    private final ImageIcon icon;
    private boolean selected;
    private final Collection<SearchTopicListener> listeners = new ArrayList<SearchTopicListener>();
    private final ListenerHandler listenerHandler = new ListenerHandler();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchTopic object.
     *
     * @param  name         DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  key          DOCUMENT ME!
     * @param  iconName     DOCUMENT ME!
     * @param  selected     DOCUMENT ME!
     */
    public SearchTopic(final String name,
            final String description,
            final String key,
            final String iconName,
            final boolean selected) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.iconName = iconName;
        this.selected = selected;

        searchClasses = new LinkedHashSet<SearchClass>();

        URL urlToIcon = null;
        if (this.iconName != null) {
            urlToIcon = getClass().getResource(this.iconName);
        }

        if (urlToIcon == null) {
            LOG.warn("The given icon '" + this.iconName + "' can not be loaded.");
            urlToIcon = getClass().getResource("/de/cismet/cismap/navigatorplugin/metasearch/search.png");
        }

        if (urlToIcon != null) {
            this.icon = new ImageIcon(urlToIcon);
        } else {
            LOG.error("Neither given icon '" + this.iconName
                        + "' nor '/de/cismet/cismap/navigatorplugin/metasearch/search.png' exist. There will be problems with the search.");
            this.icon = new ImageIcon();
        }

        putValue(SMALL_ICON, this.icon);
        putValue(SHORT_DESCRIPTION, this.description);
        putValue(ACTION_COMMAND_KEY, this.key);
        putValue(NAME, this.name);
        putValue(SELECTED_KEY, this.selected);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addSearchTopicListener(final SearchTopicListener listener) {
        listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removeSearchTopicListener(final SearchTopicListener listener) {
        listeners.remove(listener);
    }

    /**
     * Add a new search class to this search topic. The given search class won't be added if it's already added to this
     * search topic.
     *
     * @param  searchClass  The search class to add.
     */
    public void insert(final SearchClass searchClass) {
        if (searchClass == null) {
            return;
        }

        if (!searchClasses.contains(searchClass)) {
            searchClasses.add(searchClass);
        } else {
            LOG.warn("Search class with domain '" + searchClass.getCidsDomain() + "' and table '"
                        + searchClass.getCidsClass() + "' already exists in search topic '" + getName()
                        + "'. The search class won't be added twice.");
        }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() instanceof AbstractButton) {
            setSelected(((AbstractButton)event.getSource()).isSelected());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIconName() {
        return iconName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKey() {
        return key;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchClass> getSearchClasses() {
        return searchClasses;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selected  DOCUMENT ME!
     */
    public void setSelected(final boolean selected) {
        final boolean oldValue = selected;
        this.selected = selected;
        putValue(SELECTED_KEY, this.selected);
        listenerHandler.selectionChanged(new SearchTopicListenerEvent(
                this,
                SearchTopicListenerEvent.SELECTION_CHANGED,
                this.selected));
        firePropertyChange(SELECTED, oldValue, selected);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SearchTopic)) {
            return false;
        }

        final SearchTopic other = (SearchTopic)obj;

        if ((this.name == null) ? (other.name != null) : (!this.name.equals(other.name))) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : (!this.description.equals(other.description))) {
            return false;
        }
        if ((this.key == null) ? (other.key != null) : (!this.key.equals(other.key))) {
            return false;
        }
        if ((this.iconName == null) ? (other.iconName != null) : (!this.iconName.equals(other.iconName))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (11 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        hash = (11 * hash) + ((this.description != null) ? this.description.hashCode() : 0);
        hash = (11 * hash) + ((this.key != null) ? this.key.hashCode() : 0);
        hash = (11 * hash) + ((this.iconName != null) ? this.iconName.hashCode() : 0);

        return hash;
    }

    @Override
    public int compareTo(final SearchTopic o) {
        if (o == null) {
            return 1;
        }

        return name.compareTo(o.name);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class ListenerHandler implements SearchTopicListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void selectionChanged(final SearchTopicListenerEvent event) {
            for (final SearchTopicListener listener : listeners) {
                listener.selectionChanged(event);
            }
        }
    }
}
