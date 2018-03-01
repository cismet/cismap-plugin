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
package de.cismet.cismap.cidslayer;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.cismet.cids.server.connectioncontext.ClientConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContextProvider;

import de.cismet.cismap.commons.gui.capabilitywidget.StringFilter;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerTreeModel implements TreeModel, StringFilter, ConnectionContextProvider {

    //~ Instance fields --------------------------------------------------------

    private Logger LOG = Logger.getLogger(CidsLayerTreeModel.class);

    private String domain;
    private String title;
    private List<Object> classes = new ArrayList<Object>();
    private String filterString;

    private final ClientConnectionContext connectionContext = ClientConnectionContext.create(getClass()
                    .getSimpleName());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerTreeModel object.
     *
     * @param  domain  DOCUMENT ME!
     * @param  title   DOCUMENT ME!
     */
    public CidsLayerTreeModel(final String domain, final String title) {
        this.domain = domain;
        this.title = title;
        try {
            final MetaClass[] mc = SessionManager.getProxy().getClasses(domain, getConnectionContext());

            for (final MetaClass clazz : mc) {
                final Collection attributes = clazz.getAttributeByName("cidsLayer");
                final Collection hidden = clazz.getAttributeByName("hidden");
                if ((attributes == null) || attributes.isEmpty()
                            || ((hidden != null) && !hidden.isEmpty() && hidden.toArray()[0].toString().equals(
                                    "true"))) {
                    continue;
                }
                final Collection folderAttributes = clazz.getAttributeByName("cidsLayerFolder");
                final List<String> folderNames = new ArrayList<String>();

                if ((folderAttributes != null) && !folderAttributes.isEmpty()) {
                    final String name = folderAttributes.toArray()[0].toString();
                    final String[] st = name.split("->");

                    for (final String tmp : st) {
                        folderNames.add(tmp);
                    }
                }

                if (!folderNames.isEmpty()) {
                    List currentFolder = this.classes;

                    for (final String folderObject : folderNames) {
                        final String folder = folderObject;

                        TreeFolder tf = new TreeFolder(folder);
                        final int index = currentFolder.indexOf(tf);

                        if (index == -1) {
                            currentFolder.add(tf);
                        } else {
                            tf = (TreeFolder)currentFolder.get(index);
                        }

                        currentFolder = tf;
                    }

                    if (currentFolder != null) {
                        currentFolder.add(new CidsLayerConfig(clazz));
                    }
                } else {
                    this.classes.add(new CidsLayerConfig(clazz));
                }
            }
        } catch (ConnectionException ex) {
            LOG.error("Error while creating cids layer tree", ex);
        }
        sortList(classes);
        removeSortExpression(classes);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  listToSort  DOCUMENT ME!
     */
    private void sortList(final List listToSort) {
        Collections.sort(listToSort, new Comparator<Object>() {

                @Override
                public int compare(final Object o1, final Object o2) {
                    if (!(o1 instanceof CidsLayerConfig) && !(o2 instanceof CidsLayerConfig)) {
                        return o1.toString().compareTo(o2.toString());
                    } else if (!(o1 instanceof CidsLayerConfig)) {
                        return -1;
                    } else if (!(o2 instanceof CidsLayerConfig)) {
                        return 1;
                    } else {
                        return ((CidsLayerConfig)o1).compareTo((CidsLayerConfig)o2);
                    }
                }
            });

        for (final Object o : listToSort) {
            if (o instanceof List) {
                sortList((List)o);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listToSort  DOCUMENT ME!
     */
    private void removeSortExpression(final List listToSort) {
        for (final Object o : listToSort) {
            if (o instanceof List) {
                final TreeFolder tf = (TreeFolder)o;
                String newName = tf.getName();
                newName = newName.replaceAll("sort:[0123456789]* ", "");
                tf.setName(newName);
                removeSortExpression((List)o);
            }
        }
    }

    @Override
    public Object getRoot() {
        return title;
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        if ((parent != null) && parent.equals(title)) {
            return filteredChildren(classes).get(index);
        } else if (parent instanceof TreeFolder) {
            return filteredChildren((TreeFolder)parent).get(index);
        }

        return null;
    }

    @Override
    public int getChildCount(final Object parent) {
        if ((parent != null) && parent.equals(title)) {
            return filteredChildren(classes).size();
        } else if (parent instanceof TreeFolder) {
            return filteredChildren((TreeFolder)parent).size();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(final Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        return 0;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
    }

    @Override
    public void setFilterString(final String filterString) {
        this.filterString = filterString;
    }

    /**
     * Creates a new list with all elements of the given list, which matchs the filter string.
     *
     * @param   objectList  entryList parent the folder, its children should be determined
     *
     * @return  a list with all children, which considers the filter string
     */
    private List<Object> filteredChildren(final List<Object> objectList) {
        final List<Object> entries = new ArrayList<Object>();

        for (final Object entry : objectList) {
            if (fulfilFilterRequirements(entry)) {
                entries.add(entry);
            }
        }

        return entries;
    }

    /**
     * Checks, if the given entry fulfils the filter requirement.
     *
     * @param   entry  the entry to check
     *
     * @return  true, iff the filter requirement is fulfilled
     */
    private boolean fulfilFilterRequirements(final Object entry) {
        if (entry instanceof TreeFolder) {
            if ((filterString == null)
                        || ((TreeFolder)entry).getName().toLowerCase().contains(filterString.toLowerCase())) {
                return true;
            } else {
                for (final Object o : (TreeFolder)entry) {
                    if (fulfilFilterRequirements(o)) {
                        return true;
                    }
                }
            }
        } else {
            return (((filterString == null)
                                || ((CidsLayerConfig)entry).getTitle().toLowerCase().contains(
                                    filterString.toLowerCase())));
        }

        return false;
    }

    @Override
    public final ClientConnectionContext getConnectionContext() {
        return connectionContext;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class TreeFolder extends ArrayList<Object> {

        //~ Instance fields ----------------------------------------------------

        private String name;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TreeFolder object.
         *
         * @param  name  DOCUMENT ME!
         */
        public TreeFolder(final String name) {
            this.name = name;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof TreeFolder) {
                return ((TreeFolder)obj).name.equals(name);
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = (37 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
            return hash;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the name
         */
        public String getName() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  name  the name to set
         */
        public void setName(final String name) {
            this.name = name;
        }
    }
}
