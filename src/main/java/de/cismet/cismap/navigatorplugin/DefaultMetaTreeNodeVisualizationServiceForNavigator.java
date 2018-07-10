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

import Sirius.navigator.NavigatorX;
import Sirius.navigator.plugin.PluginRegistry;
import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
import Sirius.navigator.ui.ComponentRegistry;

import Sirius.server.middleware.types.MetaObjectNode;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JFrame;

import de.cismet.cids.utils.interfaces.DefaultMetaTreeNodeVisualizationService;

import de.cismet.cismap.commons.interaction.CismapBroker;

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
        if (getPlugin() == null) {
            final JFrame frame = ComponentRegistry.getRegistry().getMainWindow();

            if (frame instanceof NavigatorX) {
                final NavigatorX navigator = (NavigatorX)frame;

                navigator.showObjectInGui(c, false);
            }
        } else {
            getPlugin().showInMap(c, false);
        }
    }

    @Override
    public void removeVisualization(final DefaultMetaTreeNode dmtn) throws Exception {
        final CidsFeature cf = new CidsFeature((MetaObjectNode)dmtn.getNode());
        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(cf);
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
