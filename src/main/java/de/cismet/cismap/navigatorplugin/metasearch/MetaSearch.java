/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin.metasearch;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import org.jdom.Element;

import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchFacade;
import de.cismet.connectioncontext.AbstractConnectionContext;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class MetaSearch implements Configurable, MetaSearchFacade, ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MetaSearch.class);

    private static final String CONF_METASEARCH = "metaSearch";
    private static final String CONF_SEARCHTOPIC = "searchTopic";
    private static final String CONF_SEARCHTOPIC_ATTR_NAME = "name";
    private static final String CONF_SEARCHTOPIC_ATTR_DESCRIPTION = "description";
    private static final String CONF_SEARCHTOPIC_ATTR_KEY = "key";
    private static final String CONF_SEARCHTOPIC_ATTR_ICON = "icon";
    private static final String CONF_SEARCHTOPIC_ATTR_SELECTED = "selected";
    private static final String CONF_SEARCHTOPIC_ATTR_CHECKACTIONTAG = "check-action-tag";
    private static final String CONF_SEARCHCLASS = "searchClass";
    private static final String CONF_SEARCHCLASS_ATTR_DOMAIN = "domain";
    private static final String CONF_SEARCHCLASS_ATTR_CIDSCLASS = "cidsClass";

    private static MetaSearch instance;

    //~ Instance fields --------------------------------------------------------

    private final Collection<SearchTopic> searchTopics = new LinkedList<>();
    private final MetaClassCacheService metaClassCacheService;
    private final Collection<MetaSearchListener> listeners = new ArrayList<>();
    private final ListenerHandler listenerHandler = new ListenerHandler();
    private final SearchTopicListener searchTopicListener = new SearchTopicListenerImpl();

    private final ConnectionContext connectionContext;
                    

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaSearch object.
     */
    private MetaSearch(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
        metaClassCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MetaSearch instance() {
        if (instance == null) {
            instance = new MetaSearch(ConnectionContext.create(AbstractConnectionContext.Category.INSTANCE, MetaSearch.class.getSimpleName()));
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addMetaSearchListener(final MetaSearchListener listener) {
        listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removeMetaSearchListener(final MetaSearchListener listener) {
        listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchTopic> getSearchTopics() {
        return searchTopics;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchTopic> getSelectedSearchTopics() {
        final List<SearchTopic> result = new LinkedList<>();

        for (final SearchTopic searchTopic : searchTopics) {
            if (searchTopic.isSelected()) {
                result.add(searchTopic);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchClass> getSelectedSearchClasses() {
        final List<SearchClass> result = new LinkedList<>();

        for (final SearchTopic searchTopic : searchTopics) {
            if (searchTopic.isSelected()) {
                for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
                    result.add(searchClass);
                }
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<String> getSelectedSearchClassesForQuery() {
        final List<String> result = new LinkedList<>();

        if (metaClassCacheService == null) {
            LOG.error(
                "There is no MetaClassCacheService available. Conversion from table names to CidsClass ids is not possible.");
            return result;
        }

        for (final SearchTopic searchTopic : searchTopics) {
            if (!searchTopic.isSelected()) {
                continue;
            }

            for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
                final MetaClass metaClass = metaClassCacheService.getMetaClass(searchClass.getCidsDomain(),
                        searchClass.getCidsClass());
                if (metaClass != null) {
                    result.add(metaClass.getKey().toString());
                } else {
                    LOG.warn("Could not convert searchClass '" + searchClass.toString()
                                + "' to a MetaClass. This searchClass will not be included in the search.");
                }
            }
        }

        return result;
    }

    @Override
    public boolean hasSearchTopics() {
        return !searchTopics.isEmpty();
    }

    @Override
    public boolean hasSelectedSearchTopics() {
        return !getSelectedSearchTopics().isEmpty();
    }

    @Override
    public PNode generatePointerAnnotationForSelectedSearchTopics() {
        return new MetaSearchTooltip(getSelectedSearchTopics());
    }

    @Override
    public boolean isSearchTopicSelectedEvent(final String changedPropertyName) {
        return SearchTopic.SELECTED.equalsIgnoreCase(changedPropertyName);
    }

    @Override
    public JDialog getSearchDialog() {
        return SearchSearchTopicsDialog.instance();
    }

    @Override
    public void configure(final Element parent) {
        if (parent == null) {
            LOG.info("There is no local configuration for meta search.");
            return;
        }

        final Element metaSearch = parent.getChild(CONF_METASEARCH);
        if (metaSearch == null) {
            LOG.info("There is no local configuration for meta search.");
            return;
        }

        final List<Element> searchTopicElements = getChildren(metaSearch, CONF_SEARCHTOPIC);
        if (searchTopicElements.isEmpty()) {
            LOG.info("There is no local configuration for meta search.");
            return;
        }

        for (final Element searchTopicElement : searchTopicElements) {
            final String name = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_NAME);
            final String description = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_DESCRIPTION);
            final String key = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_KEY);
            final String icon = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_ICON);
            final String selected = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_SELECTED);

            if ((name == null) || (name.trim().length() == 0)) {
                LOG.info("There is a search topic without a valid name. Description: '" + description + "', key: '"
                            + key + "', icon: '" + icon + "', selected: '" + selected + "'.");
                continue;
            }

            boolean isSelected = true;
            if ((selected != null) && (selected.trim().length() > 0)
                        && ("false".equalsIgnoreCase(selected) || "0".equalsIgnoreCase(selected))) {
                isSelected = false;
            }

            final SearchTopic searchTopic = new SearchTopic(name, description, key, icon, isSelected);

            for (final SearchTopic searchTopicFromServer : searchTopics) {
                if (searchTopicFromServer.equals(searchTopic)) {
                    searchTopicFromServer.setSelected(isSelected);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void clearTopics() {
        final Collection<SearchTopic> removedTopics = new ArrayList<>(searchTopics);
        searchTopics.clear();
        for (final SearchTopic topic : removedTopics) {
            topic.removeSearchTopicListener(searchTopicListener);
        }
        listenerHandler.topicsRemoved(new MetaSearchListenerEvent(
                this,
                MetaSearchListenerEvent.TOPICS_REMOVED,
                removedTopics));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  topic  DOCUMENT ME!
     */
    private void removeTopic(final SearchTopic topic) {
        if (searchTopics.remove(topic)) {
            listenerHandler.topicRemoved(new MetaSearchListenerEvent(
                    this,
                    MetaSearchListenerEvent.TOPIC_REMOVED,
                    topic));
            topic.removeSearchTopicListener(searchTopicListener);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  topic  DOCUMENT ME!
     */
    private void addTopic(final SearchTopic topic) {
        if (searchTopics.add(topic)) {
            topic.addSearchTopicListener(searchTopicListener);
            listenerHandler.topicAdded(new MetaSearchListenerEvent(this, MetaSearchListenerEvent.TOPIC_ADDED, topic));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  topics  DOCUMENT ME!
     */
    private void addTopics(final Collection<SearchTopic> topics) {
        if (searchTopics.addAll(topics)) {
            for (final SearchTopic topic : topics) {
                topic.addSearchTopicListener(searchTopicListener);
            }
            listenerHandler.topicsAdded(new MetaSearchListenerEvent(
                    this,
                    MetaSearchListenerEvent.TOPICS_ADDED,
                    topics));
        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        clearTopics();
        if (metaClassCacheService == null) {
            LOG.info(
                "There is no MetaClassCacheService available. It's not possible to check if the current user is allowed to search for the specified classes.");
        }

        final User currentUser;
        if (SessionManager.isInitialized() && SessionManager.isConnected()) {
            currentUser = SessionManager.getSession().getUser();
        } else {
            LOG.info("Could not determine current user. All search classes will be added to search.");
            currentUser = null;
        }

        if (parent == null) {
            LOG.info("The meta search isn't configured.");
            return;
        }

        final Element metaSearch = parent.getChild(CONF_METASEARCH);
        if (metaSearch == null) {
            LOG.info("The meta search isn't configured.");
            return;
        }

        final List<Element> searchTopicElements = getChildren(metaSearch, CONF_SEARCHTOPIC);
        if (searchTopicElements.isEmpty()) {
            LOG.info("There are no search topics specified. Add '" + CONF_SEARCHTOPIC
                        + "' tags to specify search topics.");
            return;
        }

        final Collection<SearchTopic> searchTopics = new ArrayList<>();

        for (final Element searchTopicElement : searchTopicElements) {
            final String name = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_NAME);
            final String description = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_DESCRIPTION);
            final String key = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_KEY);
            final String icon = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_ICON);
            final String selected = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_SELECTED);
            final String checkActionTag = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_CHECKACTIONTAG);

            if ((name == null) || (name.trim().length() == 0)) {
                LOG.info("There is a search topic without a valid name. Description: '" + description + "', key: '"
                            + key + "', icon: '" + icon + "', selected: '" + selected + "'.");
                continue;
            }

            boolean isSelected = false;
            if ((selected != null) && (selected.trim().length() > 0)
                        && ("true".equalsIgnoreCase(selected) || "1".equalsIgnoreCase(selected))) {
                isSelected = true;
            }

            final SearchTopic searchTopic = new SearchTopic(name, description, key, icon, isSelected);

            final List<Element> searchClassElements = getChildren(searchTopicElement, CONF_SEARCHCLASS);
            if (searchClassElements.isEmpty()) {
                LOG.info("There are no search classes specified for search topic '" + searchTopic.getName()
                            + "'. This topic will be skipped.");
                continue;
            }

            for (final Element searchClassElement : searchClassElements) {
                final String domain = searchClassElement.getAttributeValue(CONF_SEARCHCLASS_ATTR_DOMAIN);
                final String table = searchClassElement.getAttributeValue(CONF_SEARCHCLASS_ATTR_CIDSCLASS);

                if ((domain == null) || (domain.trim().length() == 0) || (table == null)
                            || (table.trim().length() == 0)) {
                    LOG.info("Search topic '" + searchTopic.getName()
                                + "' contains at least one invalid search class. Domain: '" + domain + "', table: "
                                + table + "'. This search class will be skipped.");
                    continue;
                }

                final SearchClass searchClass = new SearchClass(domain, table);

                if ((metaClassCacheService != null) && (currentUser != null)) {
                    final MetaClass metaClass = metaClassCacheService.getMetaClass(searchClass.getCidsDomain(),
                            searchClass.getCidsClass());

                    if ((metaClass != null) && (metaClass.getPermissions() != null)
                                && metaClass.getPermissions().hasReadPermission(currentUser)) {
                        searchTopic.insert(searchClass);
                    } else {
                        LOG.info("Could not determine if user '" + currentUser
                                    + "' has read permission on '" + searchClass + "'.");
                    }
                }
            }

            final String actionTag = "st://" + key;

            boolean isEnabled = true;
            if ((checkActionTag != null) && (checkActionTag.trim().length() > 0)) {
                try {
                    final boolean actionCheck = SessionManager.getConnection()
                                .hasConfigAttr(SessionManager.getSession().getUser(),
                                    actionTag,
                                    getConnectionContext());
                    if (("enable".equalsIgnoreCase(checkActionTag) || "1".equalsIgnoreCase(checkActionTag))) {
                        isEnabled = actionCheck;
                    } else if (("disable".equalsIgnoreCase(checkActionTag) || "0".equalsIgnoreCase(checkActionTag))) {
                        isEnabled = !actionCheck;
                    } else {
                        LOG.warn(CONF_SEARCHTOPIC_ATTR_CHECKACTIONTAG + " neither enable nor disable: " + actionTag
                                    + ". searchtopic is disabled.");
                        isEnabled = false;
                    }
                } catch (ConnectionException ex) {
                    LOG.warn("could not check action tag " + actionTag + ". searchtopic is disabled.", ex);
                    isEnabled = false;
                }
            }

            if (isEnabled && !searchTopics.contains(searchTopic) && !searchTopic.getSearchClasses().isEmpty()) {
                searchTopics.add(searchTopic);
            } else {
                LOG.info("Search topic '" + searchTopic.getName()
                            + "' already exists or the user isn't allowed to read its classes. The search topic won't be added.");
            }
        }
        addTopics(searchTopics);
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element result = new Element(CONF_METASEARCH);

        for (final SearchTopic searchTopic : searchTopics) {
            final Element searchTopicElement = new Element(CONF_SEARCHTOPIC);

            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_NAME, searchTopic.getName());
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_DESCRIPTION, searchTopic.getDescription());
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_KEY, searchTopic.getKey());
            searchTopicElement.setAttribute(
                CONF_SEARCHTOPIC_ATTR_ICON,
                ((searchTopic.getIconName() != null) ? searchTopic.getIconName() : ""));
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_SELECTED, Boolean.toString(searchTopic.isSelected()));

            result.addContent(searchTopicElement);
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parent       DOCUMENT ME!
     * @param   childrenTag  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<Element> getChildren(final Element parent, final String childrenTag) {
        final List<Element> result = new LinkedList<>();

        final List children = parent.getChildren(childrenTag);
        if (children != null) {
            for (final Object child : children) {
                if (child instanceof Element) {
                    result.add((Element)child);
                }
            }
        }

        return result;
    }

    @Override
    public final ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class ListenerHandler implements MetaSearchListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void topicAdded(final MetaSearchListenerEvent event) {
            for (final MetaSearchListener listener : listeners) {
                listener.topicAdded(event);
            }
        }

        @Override
        public void topicsAdded(final MetaSearchListenerEvent event) {
            for (final MetaSearchListener listener : listeners) {
                listener.topicsAdded(event);
            }
        }

        @Override
        public void topicRemoved(final MetaSearchListenerEvent event) {
            for (final MetaSearchListener listener : listeners) {
                listener.topicRemoved(event);
            }
        }

        @Override
        public void topicsRemoved(final MetaSearchListenerEvent event) {
            for (final MetaSearchListener listener : listeners) {
                listener.topicsRemoved(event);
            }
        }

        @Override
        public void topicSelectionChanged(final MetaSearchListenerEvent event) {
            for (final MetaSearchListener listener : listeners) {
                listener.topicSelectionChanged(event);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class SearchTopicListenerImpl implements SearchTopicListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void selectionChanged(final SearchTopicListenerEvent event) {
            listenerHandler.topicSelectionChanged(new MetaSearchListenerEvent(
                    MetaSearch.this,
                    MetaSearchListenerEvent.TOPIC_SELECTION_CHANGED,
                    (SearchTopic)event.getSource()));
        }
    }
}
