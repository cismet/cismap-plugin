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

import org.openide.util.Exceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerTreeModel implements TreeModel {

    //~ Instance fields --------------------------------------------------------

    private String domain;
    private Vector<Object> classes = new Vector<Object>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerTreeModel object.
     *
     * @param  domain  DOCUMENT ME!
     */
    public CidsLayerTreeModel(final String domain) {
        this.domain = domain;
        try {
            final MetaClass[] classes = SessionManager.getProxy().getClasses(domain);

            for (final MetaClass clazz : classes) {
                final Collection attributes = clazz.getAttributeByName("cidsLayer");
                if ((attributes == null) || attributes.isEmpty()) {
                    continue;
                }
                final Collection folderAttributes = clazz.getAttributeByName("cidsLayerFolder");
                final List<String> folderNames = new ArrayList<String>();

                if ((folderAttributes != null) && !folderAttributes.isEmpty()) {
                    final String name = folderAttributes.toArray()[0].toString();
                    final StringTokenizer st = new StringTokenizer(name, "->");

                    while (st.hasMoreTokens()) {
                        folderNames.add(st.nextToken());
                    }
                }

                if (!folderNames.isEmpty()) {
                    List currentFolder = this.classes;

                    for (final String folderObject : folderNames) {
                        final String folder = folderObject;

                        TreeFolder tf = new TreeFolder(folder);
                        int index = currentFolder.indexOf(tf);

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
            Exceptions.printStackTrace(ex);
        }
        Collections.sort(this.classes, new Comparator<Object>() {

                @Override
                public int compare(final Object o1, final Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getRoot() {
        return domain;
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        if ((parent != null) && parent.equals(domain)) {
            return classes.get(index);
        } else if (parent instanceof TreeFolder) {
            return ((TreeFolder)parent).get(index);
        }

        return null;
    }

    @Override
    public int getChildCount(final Object parent) {
        if ((parent != null) && parent.equals(domain)) {
            return classes.size();
        } else if (parent instanceof TreeFolder) {
            return ((TreeFolder)parent).size();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(final Object node) {
        if (node instanceof CidsLayerConfig) {
            return true;
        } else if (node instanceof TreeFolder) {
            return ((TreeFolder)node).size() == 0;
        } else {
            return false;
        }
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class TreeFolder extends ArrayList<Object> {

        //~ Instance fields ----------------------------------------------------

        String name;

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

        /**
         * DOCUMENT ME!
         *
         * @param   tf  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public TreeFolder getFolder(final TreeFolder tf) {
            if (contains(tf)) {
                return (TreeFolder)get(indexOf(tf));
            } else {
                return null;
            }
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
    }
}
