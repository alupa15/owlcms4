/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package app.owlcms.ui.group;

import com.github.appreciated.app.layout.behaviour.AppLayout;
import com.github.appreciated.layout.FlexibleGridLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import app.owlcms.components.NavigationPage;
import app.owlcms.data.group.Group;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.state.FieldOfPlayState;
import app.owlcms.ui.home.BaseNavigationContent;
import app.owlcms.ui.home.HomeNavigationContent;
import app.owlcms.ui.home.NavigationLayout;

/**
 * The Class GroupNavigationContent.
 */
@SuppressWarnings("serial")
@Route(value = "group", layout = NavigationLayout.class)
public class GroupNavigationContent extends BaseNavigationContent implements NavigationPage {

	/**
	 * Instantiates a new lifting navigation content.
	 */
	public GroupNavigationContent() {
		VerticalLayout intro = new VerticalLayout();
		addParagraph(intro, "Use the dropdown to select the platform where the display is located.");
		addParagraph(intro, "At the beginning of each competition group, select the group. "+
				"Changing the group changes it for all displays and screens connected to this platform "+
				"(announcer, timekeeper, marshall, results, attempt board, jury, etc.");
		addParagraph(intro, "Use one of the buttons below to start one of the technical official screens.");
		intro.getElement().getStyle().set("margin-bottom", "0");
		
		Button announcer = new Button("Announcer",
				buttonClickEvent -> UI.getCurrent()
					.navigate(AnnouncerContent.class));
		Button marshall = new Button("Marshall",
				buttonClickEvent -> UI.getCurrent()
					.navigate(MarshallContent.class));
		Button timekeeper = new Button("Timekeeper",
				buttonClickEvent -> UI.getCurrent()
					.navigate(TimekeeperContent.class));
		Button results = new Button("Results",
				buttonClickEvent -> UI.getCurrent()
					.navigate(ResultsContent.class));
		
		FlexibleGridLayout grid = HomeNavigationContent.navigationGrid(
			announcer,
			marshall,
			timekeeper,
			results
			);
		
		fillH(intro, this);
		fillH(grid, this);
	}
	
	/* (non-Javadoc)
	 * @see app.owlcms.ui.home.BaseNavigationContent#configureTopBar(java.lang.String, com.github.appreciated.app.layout.behaviour.AppLayout)
	 */
	@Override
	protected void configureTopBar(String title, AppLayout appLayout) {
		super.configureTopBar("Run Lifting Group", appLayout);
	}

	@Override
	protected HorizontalLayout createTopBarFopField(String label, String placeHolder) {
		Label fopLabel = new Label(label);
		formatLabel(fopLabel);

		ComboBox<FieldOfPlayState> fopSelect = createFopSelect(placeHolder);
		OwlcmsSession.withFop((fop) -> {
			fopSelect.setValue(fop);
		});
		fopSelect.addValueChangeListener(e -> {
			OwlcmsSession.setFop(e.getValue());
			OwlcmsSession.withFop((fop) -> {
				Group group = e.getValue().getGroup();
				Group currentGroup = fop.getGroup();
				if (group == null) {
					fop.switchGroup(null);
				} else if (!group.equals(currentGroup)) {
					fop.switchGroup(group);
				}
			});
		});

		HorizontalLayout fopField = new HorizontalLayout(fopLabel, fopSelect);
		fopField.setAlignItems(Alignment.CENTER);
		return fopField;
	}


}
