/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin.metasearch;

import java.util.EventObject;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class SearchTopicListenerEvent extends EventObject {

    //~ Static fields/initializers ---------------------------------------------

    public static final int SELECTION_CHANGED = 1;

    //~ Instance fields --------------------------------------------------------

    private final int type;
    private final Object object;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchTopicListenerEvent object.
     *
     * @param  source  DOCUMENT ME!
     * @param  type    DOCUMENT ME!
     */
    public SearchTopicListenerEvent(final SearchTopic source, final int type) {
        this(source, type, null);
    }

    /**
     * Creates a new SearchTopicListenerEvent object.
     *
     * @param  source  DOCUMENT ME!
     * @param  type    DOCUMENT ME!
     * @param  object  DOCUMENT ME!
     */
    public SearchTopicListenerEvent(final SearchTopic source, final int type, final Object object) {
        super(source);

        this.type = type;
        this.object = object;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public SearchTopic getSource() {
        return (SearchTopic)super.getSource();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getObject() {
        return object;
    }
}
