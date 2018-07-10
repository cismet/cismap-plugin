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
package de.cismet.cismap.navigatorplugin.actions;

import Sirius.navigator.NavigatorX;
import Sirius.navigator.ui.ComponentRegistry;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.menu.CidsUiAction;
import de.cismet.tools.gui.menu.CidsUiActionProvider;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsUiActionProvider.class)
public class HelpMenuActionProvider implements CidsUiActionProvider, Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(HelpMenuActionProvider.class);
    private static String cismapDirectory;
    private static String helpUrl = null;
    private static String newsUrl = null;

    //~ Instance fields --------------------------------------------------------

    private boolean helpEnabled = true;
    private boolean newsEnabled = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigurationActionProvider object.
     */
    public HelpMenuActionProvider() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public List<CidsUiAction> getActions() {
        final List<CidsUiAction> actionList = new ArrayList<CidsUiAction>();

        final NavigatorX navigator = (NavigatorX)ComponentRegistry.getRegistry().getMainWindow();
        final ConfigurationManager configManager = navigator.getCismapConfigurationManager();

        configManager.configure(this);

        if (helpEnabled) {
            actionList.add(new OnlineHelpAction());
        }

        if (newsEnabled) {
            actionList.add(new NewsAction());
        }

        return actionList;
    }

    @Override
    public void masterConfigure(final Element e) {
        final Element prefs = e.getChild("cismapPluginUIPreferences"); // NOI18N

        try {
            final Element help_url_element = prefs.getChild("help_url"); // NOI18N
            final Element news_url_element = prefs.getChild("news_url"); // NOI18N

            helpUrl = help_url_element.getText();
            if (LOG.isDebugEnabled()) {
                LOG.debug("helpUrl:" + helpUrl); // NOI18N
            }

            newsUrl = news_url_element.getText();
        } catch (Throwable t) {
            LOG.error("Error while loading the help urls (" + prefs.getChildren() + "), disabling menu items", t); // NOI18N
        }

        // enable or disable help urls
        helpEnabled = helpUrl != null;
        newsEnabled = newsUrl != null;
    }

    @Override
    public void configure(final Element elmnt) {
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    private static void openUrlInExternalBrowser(final String url) {
        try {
            de.cismet.tools.BrowserLauncher.openURL(url);
        } catch (final Exception e) {
            LOG.warn("Error while opening: " + url + ". Try again", e); // NOI18N

            // Nochmal zur Sicherheit mit dem BrowserLauncher probieren
            try {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } catch (final Exception e2) {
                LOG.warn("The second time failed, too. Error while trying to open: " + url + " last attempt", e2); // NOI18N

                try {
                    de.cismet.tools.BrowserLauncher.openURL("file://" + url); // NOI18N
                } catch (Exception e3) {
                    LOG.error("3rd time fail:file://" + url, e3);             // NOI18N
                }
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class OnlineHelpAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public OnlineHelpAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(
                SMALL_ICON,
                new javax.swing.ImageIcon(
                    getClass().getResource("/images/help.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    HelpMenuActionProvider.class,
                    "HelpMenuActionProvider.OnlineHelpAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    HelpMenuActionProvider.class,
                    "HelpMenuActionProvider.OnlineHelpAction.initAction.tooltip"));
            putValue(ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "OnlineHelpAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            openUrlInExternalBrowser(helpUrl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class NewsAction extends AbstractAction implements CidsUiAction {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadConfigurationAction object.
         */
        public NewsAction() {
            initAction();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initAction() {
            putValue(
                SMALL_ICON,
                new javax.swing.ImageIcon(getClass().getResource("/images/news.png")));
            putValue(
                NAME,
                org.openide.util.NbBundle.getMessage(
                    HelpMenuActionProvider.class,
                    "HelpMenuActionProvider.NewsAction.initAction.title"));
            putValue(
                SHORT_DESCRIPTION,
                org.openide.util.NbBundle.getMessage(
                    HelpMenuActionProvider.class,
                    "HelpMenuActionProvider.NewsAction.initAction.tooltip"));
            putValue(CidsUiAction.CIDS_ACTION_KEY, "NewsAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            openUrlInExternalBrowser(newsUrl);
        }
    }
}
