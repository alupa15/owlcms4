/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.ui.home;

import java.time.LocalDate;

import org.slf4j.LoggerFactory;

import com.github.appreciated.app.layout.component.applayout.AbstractLeftAppLayoutBase;
import com.github.appreciated.css.grid.GridLayoutComponent.AutoFlow;
import com.github.appreciated.css.grid.GridLayoutComponent.Overflow;
import com.github.appreciated.css.grid.sizes.Flex;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.MinMax;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.github.appreciated.layout.FlexibleGridLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.Route;

import app.owlcms.components.NavigationPage;
import app.owlcms.i18n.Translator;
import app.owlcms.init.OwlcmsFactory;
import app.owlcms.ui.shared.BaseNavigationContent;
import app.owlcms.ui.shared.OwlcmsRouterLayout;
import app.owlcms.utils.DebugUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * The Class HomeNavigationContent.
 */
/**
 * @author owlcms
 *
 */
@SuppressWarnings("serial")
@Route(value = "info", layout = OwlcmsRouterLayout.class)
public class InfoNavigationContent extends BaseNavigationContent implements NavigationPage, HasDynamicTitle {

    final private static Logger logger = (Logger) LoggerFactory.getLogger(InfoNavigationContent.class);
    static {
        logger.setLevel(Level.INFO);
    }

    /**
     * Navigation crudGrid.
     *
     * @param items the items
     * @return the flexible crudGrid layout
     */
    public static FlexibleGridLayout navigationGrid(Component... items) {
        FlexibleGridLayout layout = new FlexibleGridLayout();
        layout.withColumns(Repeat.RepeatMode.AUTO_FILL, new MinMax(new Length("300px"), new Flex(1)))
                .withAutoRows(new Length("1fr")).withItems(items).withGap(new Length("2vmin"))
                .withOverflow(Overflow.AUTO).withAutoFlow(AutoFlow.ROW).withMargin(false).withPadding(true)
                .withSpacing(false);
        layout.setSizeUndefined();
        layout.setWidth("80%");
        layout.setBoxSizing(BoxSizing.BORDER_BOX);
        return layout;
    }

    /**
     * Instantiates a new main navigation content.
     */
    public InfoNavigationContent() {
        VerticalLayout license = buildLicense();
        fillH(license, this);

        DebugUtils.gc();
    }

    private VerticalLayout buildLicense() {
        VerticalLayout license = new VerticalLayout();
        license.add(
                new H3(getTranslation("OwlcmsBuild", OwlcmsFactory.getVersion(), OwlcmsFactory.getBuildTimestamp())));
        license.add(new H3(getTranslation("CopyrightLicense")));
        addP(license, getTranslation("Copyright2009") + LocalDate.now().getYear() + " " + getTranslation("JFL"));
        addP(license, getTranslation("LicenseUsed"));
        license.add(new H3(getTranslation("SourceDocumentation")));
        addP(license, getTranslation("ProjectRepository") + getTranslation("Documentation")
                + getTranslation("InstallationConfiguration"));

        license.add(new H3(getTranslation("Notes")));
        addP(license, getTranslation("TCRRCompliance") + getTranslation("AtTimeOfRelease")
                + getTranslation("UseAtYourOwnRisk"));

        license.add(new H3(getTranslation("Credits")));
        addP(license, getTranslation("WrittenJFL") + getTranslation("ThanksToAll") + getTranslation("ThanksToFHQ"));

        Button resetTranslation = new Button(getTranslation("reloadTranslation"),
                buttonClickEvent -> Translator.reset());
        FlexibleGridLayout grid1 = HomeNavigationContent.navigationGrid(resetTranslation);

        license.add(new H3(getTranslation("Translation")));
        addP(license, getTranslation("TranslationDocumentation"));
        doGroup(getTranslation("reloadTranslationInfo"), grid1, license);

        return license;
    }

    /**
     * The left part of the top bar.
     * 
     * @see app.owlcms.ui.shared.BaseNavigationContent#configureTopBarTitle(java.lang.String)
     */
    @Override
    protected void configureTopBarTitle(String topBarTitle) {
        AbstractLeftAppLayoutBase appLayout = getAppLayout();
        appLayout.getTitleWrapper().getElement().getStyle().set("flex", "0 1 40em");
        Label label = new Label(getTitle());
        appLayout.setTitleComponent(label);
    }

    /**
     * @see app.owlcms.ui.shared.BaseNavigationContent#createTopBarFopField(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected HorizontalLayout createTopBarFopField(String label, String placeHolder) {
        return null;
    }

    /**
     * @see app.owlcms.ui.shared.BaseNavigationContent#createTopBarGroupField(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected HorizontalLayout createTopBarGroupField(String label, String placeHolder) {
        return null;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public UI getLocationUI() {
        return this.locationUI;
    }

    /**
     * @see com.vaadin.flow.router.HasDynamicTitle#getPageTitle()
     */
    @Override
    public String getPageTitle() {
        return getTranslation("OWLCMS_Info");
    }

    /**
     * @see app.owlcms.ui.shared.BaseNavigationContent#getTitle()
     */
    @Override
    protected String getTitle() {
        return getTranslation("OWLCMS_Top");
    }

    /**
     * @see app.owlcms.ui.shared.QueryParameterReader#isIgnoreFopFromURL()
     */
    @Override
    public boolean isIgnoreFopFromURL() {
        return true;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public void setLocationUI(UI locationUI) {
        this.locationUI = locationUI;
    }

}