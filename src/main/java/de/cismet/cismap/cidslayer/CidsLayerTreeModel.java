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
import Sirius.server.newuser.permission.Permission;

import org.openide.util.Exceptions;

import java.util.Collection;
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
    private Vector<CidsLayerConfig> classes = new Vector<CidsLayerConfig>();

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
                this.classes.add(new CidsLayerConfig(clazz));
            }
        } catch (ConnectionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getRoot() {
        return domain;
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        return classes.get(index);
    }

    @Override
    public int getChildCount(final Object parent) {
        return classes.size();
    }

    @Override
    public boolean isLeaf(final Object node) {
        return node instanceof CidsLayerConfig;
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        return 0;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose
        // Tools | Templates.
    }
}
