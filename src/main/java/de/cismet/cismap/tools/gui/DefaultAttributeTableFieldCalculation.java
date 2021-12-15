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
package de.cismet.cismap.tools.gui;

import java.util.List;

import de.cismet.cismap.cidslayer.FieldCalculatorDialog;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFieldCalculation;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = AttributeTableFieldCalculation.class)
public class DefaultAttributeTableFieldCalculation implements AttributeTableFieldCalculation, ConnectionContextStore {

    //~ Instance fields --------------------------------------------------------

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public boolean openPanel(final AttributeTable table,
            final AbstractFeatureService service,
            final FeatureServiceAttribute attribute,
            final List<FeatureServiceFeature> featureList) {
        final FieldCalculatorDialog dialog = new FieldCalculatorDialog(
                table,
                true,
                service,
                attribute,
                featureList,
                getConnectionContext());

        dialog.setSize(540, 500);
        StaticSwingTools.showDialog(dialog);

        return dialog.isCalculationStarted();
    }

    @Override
    public boolean openPanel(final AttributeTable table,
            final AbstractFeatureService service,
            final FeatureServiceAttribute attribute,
            final List<FeatureServiceFeature> featureList,
            final List<FeatureServiceFeature> allFeaturesFromService) {
        final FieldCalculatorDialog dialog = new FieldCalculatorDialog(
                table,
                true,
                service,
                attribute,
                featureList,
                getConnectionContext());

        if (allFeaturesFromService != null) {
            dialog.setAllFeaturesFromService(allFeaturesFromService);
        }

        dialog.setSize(540, 500);
        StaticSwingTools.showDialog(dialog);

        return dialog.isCalculationStarted();
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
