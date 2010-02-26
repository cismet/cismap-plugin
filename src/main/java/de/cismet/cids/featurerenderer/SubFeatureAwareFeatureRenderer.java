/*
 *  Copyright (C) 2010 thorsten
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cids.featurerenderer;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.FeatureRenderer;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.navigatorplugin.CidsFeature;
import javax.swing.JComponent;

/**
 *
 * @author thorsten
 */
public interface SubFeatureAwareFeatureRenderer extends FeatureRenderer {

    public java.awt.Stroke getLineStyle(CidsFeature subFeature);

    public java.awt.Paint getLinePaint(CidsFeature subFeature);

    public java.awt.Paint getFillingStyle(CidsFeature subFeature);

    public float getTransparency(CidsFeature subFeature);

    public FeatureAnnotationSymbol getPointSymbol(CidsFeature subFeature);

    public JComponent getInfoComponent(Refreshable refresh, CidsFeature subFeature);
}
