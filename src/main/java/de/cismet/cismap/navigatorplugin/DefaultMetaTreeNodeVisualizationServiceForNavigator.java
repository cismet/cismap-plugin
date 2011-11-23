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
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.plugin.PluginRegistry;
import Sirius.navigator.types.treenode.DefaultMetaTreeNode;

import Sirius.server.middleware.types.MetaObjectNode;

import java.util.Collection;
import java.util.Vector;

import de.cismet.cids.utils.interfaces.DefaultMetaTreeNodeVisualizationService;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = DefaultMetaTreeNodeVisualizationService.class)
public class DefaultMetaTreeNodeVisualizationServiceForNavigator implements DefaultMetaTreeNodeVisualizationService {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void addVisualization(final DefaultMetaTreeNode dmtn) throws Exception {
        final Vector<DefaultMetaTreeNode> v = new Vector<DefaultMetaTreeNode>();
        v.add(dmtn);
        addVisualization(v);
    }

    @Override
    public void addVisualization(final Collection<DefaultMetaTreeNode> c) throws Exception {
        getPlugin().showInMap(c, false);
    }

    @Override
    public void removeVisualization(final DefaultMetaTreeNode dmtn) throws Exception {
        final CidsFeature cf = new CidsFeature((MetaObjectNode)dmtn.getNode());
        getPlugin().getMappingComponent().getFeatureCollection().removeFeature(cf);
    }

    @Override
    public void removeVisualization(final Collection<DefaultMetaTreeNode> c) throws Exception {
        for (final DefaultMetaTreeNode dmtn : c) {
            removeVisualization(dmtn);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CismapPlugin getPlugin() {
        final Object o = PluginRegistry.getRegistry().getPlugin("cismap"); // NOI18N
        return (CismapPlugin)o;
    }
}
