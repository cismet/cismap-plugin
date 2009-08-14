/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.plugin.PluginRegistry;
import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
import Sirius.navigator.ui.ComponentRegistry;
import Sirius.server.middleware.types.MetaObjectNode;
import de.cismet.cids.utils.interfaces.DefaultMetaTreeNodeVisualizationService;
import java.util.Collection;
import java.util.Vector;

/**
 *
 * @author thorsten
 */
@org.openide.util.lookup.ServiceProvider(service = DefaultMetaTreeNodeVisualizationService.class)
public class DefaultMetaTreeNodeVisualizationServiceForNavigator implements DefaultMetaTreeNodeVisualizationService {


    public void addVisualization(DefaultMetaTreeNode dmtn) throws Exception {
        Vector<DefaultMetaTreeNode> v = new Vector<DefaultMetaTreeNode>();
        v.add(dmtn);
        addVisualization(v);
    }

    public void addVisualization(Collection<DefaultMetaTreeNode> c) throws Exception {
        getPlugin().showInMap(c, false);
    }

    public void removeVisualization(DefaultMetaTreeNode dmtn) throws Exception {
        CidsFeature cf = new CidsFeature((MetaObjectNode) dmtn.getNode());
        getPlugin().getMappingComponent().getFeatureCollection().removeFeature(cf);
    }

    public void removeVisualization(Collection<DefaultMetaTreeNode> c) throws Exception {
        for (DefaultMetaTreeNode dmtn : c) {
            removeVisualization(dmtn);
        }
    }


    private CismapPlugin getPlugin(){
        Object o=PluginRegistry.getRegistry().getPlugin("cismap");
        return (CismapPlugin)o;
    }
}
