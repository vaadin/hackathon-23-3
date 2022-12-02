package com.vaadin.example.hackathon233.views.credits;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.vaadin.example.hackathon233.data.entity.Credit;
import com.vaadin.example.hackathon233.data.service.CreditService;
import com.vaadin.example.hackathon233.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

@PageTitle("Credits")
@Route(value = "credits/:creditID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@PreserveOnRefresh
public class CreditsView extends Div implements BeforeEnterObserver {

    private final String CREDIT_ID = "creditID";
    private final String CREDIT_EDIT_ROUTE_TEMPLATE = "credits/%s/edit";

    private final Grid<Credit> grid = new Grid<>(Credit.class, false);

    private TextField name;
    private TextField loan;
    private TextField years;
    private TextField interest;
    private DatePicker date;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private Spreadsheet sheet;
    private SplitLayout splitLayout;

    private final BeanValidationBinder<Credit> binder;

    private Tab tabCredits = new Tab("Credits");
    private Tab tabDetails = new Tab("Details");
    private TabSheet tabSheet;

    private Credit credit;

    private final CreditService creditService;

    @Autowired
    public CreditsView(CreditService creditService) {
        this.creditService = creditService;
        addClassNames("credits-view");
        setSizeFull();

        // Create UI
        splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        sheet = createSheet();

        tabSheet = new TabSheet();
        tabSheet.add(tabCredits, splitLayout);
        tabSheet.add(tabDetails, sheet);
        tabSheet.setSizeFull();
        tabDetails.setEnabled(false);

        Tooltip.forComponent(tabCredits).withText("View all Credits").withPosition(Tooltip.TooltipPosition.BOTTOM);
        Tooltip.forComponent(tabDetails).withText("View in a Spreadsheet").withPosition(Tooltip.TooltipPosition.BOTTOM);

        tabSheet.addSelectedChangeListener(e -> {
            if (e.getSelectedTab().equals(tabDetails)) {
                populateSheet();
            }
        });


        add(tabSheet);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(Credit::getLoan,
                NumberFormat.getCurrencyInstance(VaadinService.getCurrentRequest().getLocale()))).setAutoWidth(true)
                .setHeader("loan");

        grid.addColumn("years").setAutoWidth(true);

        grid.addColumn(LitRenderer.<Credit>of("<span>${item.percent} %</span>").withProperty("percent",
                c -> c.getInterest())).setAutoWidth(true).setHeader("interest");
        grid.addColumn("date").setAutoWidth(true);
        grid.setItems(query -> creditService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(CREDIT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CreditsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Credit.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(loan).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("loan");
        binder.forField(years).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("years");
        binder.forField(interest).withConverter(new StringToDoubleConverter("Only numbers are allowed"))
                .bind("interest");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.credit == null) {
                    this.credit = new Credit();
                }
                binder.writeBean(this.credit);
                creditService.update(this.credit);
                clearForm();
                refreshGrid();
                Notification.show("Credit details stored.");
                UI.getCurrent().navigate(CreditsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the credit details.");
            }
        });

        sheet.addCellValueChangeListener(e -> {populateFormFromSheet();});
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.isRefreshEvent()) {
            return;
        }
        Optional<UUID> creditId = event.getRouteParameters().get(CREDIT_ID).map(UUID::fromString);
        if (creditId.isPresent()) {
            Optional<Credit> creditFromBackend = creditService.get(creditId.get());
            if (creditFromBackend.isPresent()) {
                populateForm(creditFromBackend.get());
            } else {
                Notification.show(String.format("The requested credit was not found, ID = %s", creditId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(CreditsView.class);
            }
        }
    }

    private Spreadsheet createSheet() {
        Spreadsheet sheet = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream stream = classLoader.getResourceAsStream("template.xlsx");
            sheet = new Spreadsheet(stream);
            sheet.setSheetSelectionBarVisible(false);
            sheet.setSizeFull();
            sheet.createCell(3, 8, "");
            sheet.createCell(8, 3, "");
            sheet.createCell(13, 6, "");
            sheet.createCell(13, 7, "");
        } catch (Exception i) {
        }
        return sheet;
    }

    private void populateSheet() {
        try {
            binder.writeBean(this.credit);
        } catch (ValidationException e) {
            return;
        }
        sheet.createCell(5, 1, this.credit.getName());
        sheet.createCell(7, 4, this.credit.getLoan().doubleValue());
        sheet.createCell(8, 4, this.credit.getInterest() / 100);
        sheet.createCell(9, 4, this.credit.getYears().doubleValue());
//        LocalDate local = this.credit.getDate();
//        Calendar calendar = Calendar.getInstance();
//        calendar.clear();
//        calendar.set(local.getYear(), local.getMonthValue() - 1, local.getDayOfMonth());
//        sheet.createCell(11, 4, calendar);
    }

    private void populateFormFromSheet() {
        loan.setValue("" + (int)sheet.getCell(7, 4).getNumericCellValue());
        interest.setValue(String.format("%.2g", sheet.getCell(8, 4).getNumericCellValue() * 100));
        years.setValue("" + (int)sheet.getCell(9, 4).getNumericCellValue());

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        loan = new TextField("Loan");
        years = new TextField("Years");
        interest = new TextField("Interest");
        date = new DatePicker("Date");

        formLayout.add(name, loan, years, interest, date);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
        tabDetails.setEnabled(false);
    }

    private void populateForm(Credit value) {
        this.credit = value;
        binder.readBean(this.credit);
        tabDetails.setEnabled(true);

    }
}
