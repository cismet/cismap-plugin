/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.navigatorplugin;

import org.openide.util.lookup.ServiceProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.cismet.ext.CExtContext;
import de.cismet.ext.CExtProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CExtProvider.class)
public final class MapVisualisationCExtProvider implements CExtProvider<MapVisualisationProvider> {

    //~ Instance fields --------------------------------------------------------

    private final String ifaceClass;
    private final String concreteClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MapVisualisationCExtProvider object.
     */
    public MapVisualisationCExtProvider() {
        ifaceClass = "de.cismet.cismap.navigatorplugin.MapVisualisationProvider";           // NOI18N
        concreteClass = "de.cismet.cismap.navigatorplugin.DefaultMapVisualisationProvider"; // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<? extends MapVisualisationProvider> provideExtensions(final CExtContext context) {
        final List<? extends MapVisualisationProvider> p = Arrays.asList(new DefaultMapVisualisationProvider());

        return p;
    }

    @Override
    public Class<MapVisualisationProvider> getType() {
        return MapVisualisationProvider.class;
    }

    @Override
    public boolean canProvide(final Class<?> c) {
        final String cName = c.getCanonicalName();

        return (cName == null) ? false : (ifaceClass.equals(cName) || concreteClass.equals(cName));
    }
}
