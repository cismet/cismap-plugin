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

import lombok.Getter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
public class SearchTopicsDialogModel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROPERTY_SEARCHTEXT = "searchText";
    public static final String PROPERTY_CASESENSITIVE = "caseSensitiveEnabled";
    public static final String PROPERTY_SEARCHGEOMETRY = "searchGeometryEnabled";
    public static final String PROPERTY_SEARCHCLASSESSTRING = "searchClassesString";

    //~ Instance fields --------------------------------------------------------

    private String searchText;
    private boolean caseSensitiveEnabled;
    private boolean searchGeometryEnabled;
    private String searchClassesString;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    public void setSearchText(final String newValue) {
        final Object oldValue = this.searchText;
        this.searchText = newValue;
        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCHTEXT, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    public void setSearchClassesString(final String newValue) {
        final Object oldValue = this.searchClassesString;
        this.searchClassesString = newValue;
        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCHCLASSESSTRING, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    public void setCaseSensitiveEnabled(final boolean newValue) {
        final Object oldValue = this.caseSensitiveEnabled;
        this.caseSensitiveEnabled = newValue;
        propertyChangeSupport.firePropertyChange(PROPERTY_CASESENSITIVE, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    public void setSearchGeometryEnabled(final boolean newValue) {
        final Object oldValue = this.searchGeometryEnabled;
        this.searchGeometryEnabled = newValue;
        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCHGEOMETRY, oldValue, newValue);
    }
}
