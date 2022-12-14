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
package de.cismet.cismap.navigatorplugin;

import Sirius.navigator.tools.StaticNavigatorTools;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URI;
import java.net.URL;

import java.nio.file.Paths;

import java.util.Properties;

import de.cismet.netutil.ProxyProperties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class ConfigProperties extends Properties {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ConfigProperties.class);

    //~ Instance fields --------------------------------------------------------

    private final ProxyProperties proxyProperties = new ProxyProperties();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   cfgFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ConfigProperties load(final String cfgFile) throws Exception {
        load(StaticNavigatorTools.getInputStreamFromFileOrUrl(cfgFile));

        final String proxyConfig = getProxyConfig();

        if ((proxyConfig != null) && !proxyConfig.isEmpty()) {
            try {
                final String cfgFileName = Paths.get(new URI(cfgFile).getPath()).getFileName().toString();
                final String cfgDirname = cfgFile.substring(0, cfgFile.lastIndexOf(cfgFileName));
                proxyProperties.load(StaticNavigatorTools.getInputStreamFromFileOrUrl(cfgDirname + proxyConfig));
            } catch (final Exception ex) {
                LOG.warn(String.format("error while loading proxy properties from %s", proxyConfig), ex);
            }
        }
        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCallserverUrl() {
        return getProperty("callserverUrl");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCompressionEnabled() {
        try {
            return Boolean.parseBoolean(getProperty("compressionEnabled"));
        } catch (final Exception ex) {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return getProperty("domain");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUsername() {
        return getProperty("username");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPassword() {
        return getProperty("password");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClientName() {
        return getProperty("clientName");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isIntranetUse() {
        try {
            return Boolean.parseBoolean(getProperty("intranetUse"));
        } catch (final Exception ex) {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProxyConfig() {
        return getProperty("proxy.config");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConfigProperties getInstance() {
        return LazyInitializer.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient ConfigProperties INSTANCE = new ConfigProperties();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
