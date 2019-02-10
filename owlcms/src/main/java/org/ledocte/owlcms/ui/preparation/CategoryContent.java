package org.ledocte.owlcms.ui.preparation;

import java.util.Collection;

import org.ledocte.owlcms.data.AgeDivision;
import org.ledocte.owlcms.data.category.Category;
import org.ledocte.owlcms.data.category.CategoryRepository;
import org.ledocte.owlcms.ui.crudui.OwlcmsCrudLayout;
import org.ledocte.owlcms.ui.crudui.OwlcmsGridCrud;
import org.vaadin.crudui.crud.CrudListener;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.CrudFormFactory;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * @author Alejandro Duarte
 */
@SuppressWarnings("serial")
@Route(value = "preparation/categories", layout = CategoryLayout.class)
public class CategoryContent extends VerticalLayout implements CrudListener<Category> { // or implements LazyCrudListener<Category>
	
    private ComboBox<AgeDivision> ageDivisionFilter = new ComboBox<>();

    public CategoryContent() {
        setSizeFull();
        GridCrud<Category> crud = getMinimal();
		add(crud);
    }

    private GridCrud<Category> getMinimal() {
        GridCrud<Category> crud = new OwlcmsGridCrud<Category>(Category.class, new OwlcmsCrudLayout(Category.class));
        crud.setCrudListener(this);
        
        CrudFormFactory<Category> crudFormFactory = crud.getCrudFormFactory();
        crudFormFactory.setVisibleProperties("name", "enumAgeDivision", "enumGender", "minimumWeight", "maximumWeight", "wr", "active");
        crudFormFactory.setFieldCaptions("Name", "Age Division", "Gender", "Minimum Weight", "Maximum Weight", "World Record", "Active");

        Grid<Category> grid = crud.getGrid();
		grid.setColumns("name", "enumAgeDivision", "enumGender", "minimumWeight", "maximumWeight", "active");
		grid.getColumnByKey("name").setHeader("nom");
		grid.getColumnByKey("enumAgeDivision").setHeader("Division");
		grid.getColumnByKey("enumGender").setHeader("Gender");
        crud.setClickRowToUpdate(true);
        
        ageDivisionFilter.setPlaceholder("Age Division");
        ageDivisionFilter.setItems(AgeDivision.findAll());
        ageDivisionFilter.setItemLabelGenerator(AgeDivision::name);
        ageDivisionFilter.addValueChangeListener(e -> crud.refreshGrid());
        crud.getCrudLayout().addFilterComponent(ageDivisionFilter);
        //crud.getCrudLayout().addToolbarComponent(new Label("toolbar stuff goes here"));
        return crud;
    }

//    public Component getConfiguredCrud() {
//        GridCrud<Category> crud = new GridCrud<>(Category.class, new HorizontalSplitCrudLayout());
//        crud.setCrudListener(this);
//
//        DefaultCrudFormFactory<Category> formFactory = new DefaultCrudFormFactory<>(Category.class);
//        crud.setCrudFormFactory(formFactory);
//
//        formFactory.setUseBeanValidation(true);
//
//        formFactory.setErrorListener(e -> {
//            Notification.show("Custom error message");
//            e.printStackTrace();
//        });
//
//        formFactory.setVisibleProperties("name", "birthDate", "email", "phoneNumber",
//                "maritalStatus", "groups", "active", "mainGroup");
//        formFactory.setVisibleProperties(CrudOperation.DELETE, "name", "email", "mainGroup");
//
//        formFactory.setDisabledProperties("id");
//
//        crud.getGrid().setColumns("name", "email", "phoneNumber", "active");
//        crud.getGrid().addColumn(new LocalDateRenderer<>(
//                Category -> Category.getBirthDate(),
//                DateTimeFormatter.ISO_LOCAL_DATE))
//                .setHeader("Birthdate");
//
//        crud.getGrid().addColumn(new TextRenderer<>(Category -> Category == null ? "" : Category.getMainGroup().getName()))
//                .setHeader("Main group");
//
//        crud.getGrid().setColumnReorderingAllowed(true);
//
//        formFactory.setFieldType("password", PasswordField.class);
//        formFactory.setFieldProvider("birthDate", () -> {
//            DatePicker datePicker = new DatePicker();
//            datePicker.setMax(LocalDate.now());
//            return datePicker;
//        });
//
//        formFactory.setFieldProvider("maritalStatus", new RadioButtonGroupProvider<>(Arrays.asList(MaritalStatus.values())));
//        formFactory.setFieldProvider("groups", new CheckBoxGroupProvider<>("Groups", CategoryRepository.findAll(), new TextRenderer<>(Group::getName)));
//        formFactory.setFieldProvider("mainGroup",
//                new ComboBoxProvider<>("Main Group", CategoryRepository.findAll(), new TextRenderer<>(Group::getName), Group::getName));
//
//        formFactory.setButtonCaption(CrudOperation.ADD, "Add new Category");
//        crud.setRowCountCaption("%d Category(s) found");
//
//        crud.setClickRowToUpdate(true);
//        crud.setUpdateOperationVisible(false);
//
//
//        nameFilter.setPlaceholder("filter by name...");
//        nameFilter.addValueChangeListener(e -> crud.refreshGrid());
//        crud.getCrudLayout().addFilterComponent(nameFilter);
//
//        groupFilter.setPlaceholder("Group");
//        groupFilter.setItems(CategoryRepository.findAll());
//        groupFilter.setItemLabelGenerator(Group::getName);
//        groupFilter.addValueChangeListener(e -> crud.refreshGrid());
//        crud.getCrudLayout().addFilterComponent(groupFilter);
//
//        Button clearFilters = new Button(null, VaadinIcon.ERASER.create());
//        clearFilters.addClickListener(event -> {
//            nameFilter.clear();
//            groupFilter.clear();
//        });
//        crud.getCrudLayout().addFilterComponent(clearFilters);
//
//        crud.setFindAllOperation(
//                DataProvider.fromCallbacks(
//                        query -> CategoryRepository.findByNameLike(nameFilter.getValue(), groupFilter.getValue(), query.getOffset(), query.getLimit()).stream(),
//                        query -> CategoryRepository.countByNameLike(nameFilter.getValue(), groupFilter.getValue()))
//        );
//        return crud;
//    }

    @Override
    public Category add(Category Category) {
        CategoryRepository.save(Category);
        return Category;
    }

    @Override
    public Category update(Category Category) {
        if (Category.getId().equals(5l)) {
            throw new RuntimeException("A simulated error has occurred");
        }
        return CategoryRepository.save(Category);
    }

    @Override
    public void delete(Category Category) {
        CategoryRepository.delete(Category);
    }

    @Override
    public Collection<Category> findAll() {
        return CategoryRepository.findAll();
    }

    /* if this implements LazyCrudListener<Category>:
    @Override
    public DataProvider<Category, Void> getDataProvider() {
        return DataProvider.fromCallbacks(
                query -> CategoryRepository.findByNameLike(nameFilter.getValue(), groupFilter.getValue(), query.getOffset(), query.getLimit()).stream(),
                query -> CategoryRepository.countByNameLike(nameFilter.getValue(), groupFilter.getValue()));
    }*/

}
