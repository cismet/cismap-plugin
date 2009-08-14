/*
 * AbstractCidsFeatureRenderer.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 1. Juni 2006, 17:18
 *
 */

package de.cismet.cids.featurerenderer;

import de.cismet.cismap.commons.features.FeatureRenderer;
import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public abstract class AbstractCidsFeatureRenderer implements FeatureRenderer{
    protected MetaObject metaObject;
    protected MetaClass metaClass;
    
    public void setMetaObject(MetaObject metaObject) throws ConnectionException {
        this.metaObject=metaObject;
        metaClass=SessionManager.getProxy().getMetaClass(metaObject.getClassKey());
    }
    
    
}
