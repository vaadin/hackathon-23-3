package com.vaadin.example.hackathon233.views.spreadsheet;

import com.vaadin.collaborationengine.CollaborationEngine;
import com.vaadin.collaborationengine.FormManager;
import com.vaadin.collaborationengine.PresenceManager;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.example.hackathon233.data.entity.User;
import com.vaadin.example.hackathon233.security.AuthenticatedUser;
import com.vaadin.example.hackathon233.views.MainLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@PageTitle("Spreadsheet")
@Route(value = "spreadsheet", layout = MainLayout.class)
//@RolesAllowed("ADMIN")
@PermitAll
@JsModule("./src/spreadsheet.collab.js")
public class SpreadsheetView extends VerticalLayout implements Receiver {

    private final FormManager formManager;
    private File uploadedFile;
    private File previousFile;
    private final Spreadsheet spreadsheet;

    private int myHighlightedRow = -1;
    private int myHighlightedCell = -1;

    public SpreadsheetView(AuthenticatedUser authenticatedUser) {
        setSizeFull();

        CollaborationEngine collaborationEngine = CollaborationEngine.getInstance();

        spreadsheet = new Spreadsheet();
        MenuBar menuBar = createMenuBar();
        AvatarGroup avatarGroup = new AvatarGroup();
        HorizontalLayout toolbar = new HorizontalLayout(menuBar, avatarGroup);
        add(toolbar, spreadsheet);

        User user = authenticatedUser.get().get();
        UserInfo userInfo = new UserInfo(user.getId().toString(), user.getName());

        PresenceManager manager = new PresenceManager(spreadsheet, userInfo, "spreadsheet");
        manager.markAsPresent(true);
        manager.setPresenceHandler(context -> {
            UserInfo connectedUser = context.getUser();
            AvatarGroupItem avatar = new AvatarGroupItem();
            avatar.setName(connectedUser.getName());
            avatar.setAbbreviation(connectedUser.getAbbreviation());
            avatar.setColorIndex(collaborationEngine.getUserColorIndex(connectedUser));
            avatarGroup.add(avatar);

            return () -> avatarGroup.remove(avatar);
        });

        spreadsheet.getElement().executeJs("window.Vaadin.Flow._spreadsheet_collab.init(this)");

        formManager = new FormManager(spreadsheet, userInfo, "spreadsheet");
        formManager.setHighlightHandler(context -> {
            int uiIdFromEvent = parseUIIdFromEvent(context.getPropertyName());
            if (UI.getCurrent().getUIId() != uiIdFromEvent) {
                UserInfo contextUser = context.getUser();
                int[] coords = parseCellCoordinatesFromEvent(context.getPropertyName());

                spreadsheet.getElement().executeJs("window.Vaadin.Flow._spreadsheet_collab.onSelect(this, $0, $1, $2, $3, $4)",
                        contextUser.getId(),
                        coords[0] + 1,
                        coords[1] + 1,
                        contextUser.getColorIndex() % 2 == 0 ? "red" : "green",
                        contextUser.getName()
                );

                return () -> {
                    spreadsheet.getElement().executeJs("window.Vaadin.Flow._spreadsheet_collab.onDeselect(this, $0)", contextUser.getId());
                };
            }

            return () -> {
            };
        });


        formManager.setPropertyChangeHandler(event -> {
            int uiIdFromEvent = parseUIIdFromEvent(event.getPropertyName());
            if (UI.getCurrent().getUIId() != uiIdFromEvent) {
                Object value = event.getValue();
                int[] coords = parseCellCoordinatesFromEvent(event.getPropertyName());

                Cell cell = spreadsheet.getCell(coords[0], coords[1]);
                String stringValue = value != null ? value.toString() : null;
                if (cell == null) {
                    cell = spreadsheet.createCell(coords[0], coords[1], stringValue);
                } else {
                    cell.setCellValue(stringValue);
                }
                spreadsheet.refreshCells(cell);
            } else {
            }

        });

        spreadsheet.addCellValueChangeListener(event -> {
            Set<CellReference> changedCells = event.getChangedCells();
            if (changedCells.size() > 0) {
                CellReference reference = changedCells.iterator().next();
                String value = spreadsheet.getCell(reference.getRow(), reference.getCol()).getStringCellValue();
                formManager.setValue(createEventInfo(reference.getRow(), reference.getCol()), value);
            }
        });

        spreadsheet.addSelectionChangeListener(event -> {
            CellReference reference = event.getSelectedCellReference();
            if (myHighlightedCell > -1 && myHighlightedRow > -1) {
                formManager.highlight(createEventInfo(myHighlightedRow, myHighlightedCell), false);
            }

            if (reference != null) {
                myHighlightedRow = reference.getRow();
                myHighlightedCell = reference.getCol();

                formManager.highlight(createEventInfo(myHighlightedRow, myHighlightedCell), true);
            }
        });
    }

    private static int[] parseCellCoordinatesFromEvent(String eventInfo) {
        String[] s = eventInfo.split("_");
        return new int[]{Integer.parseInt(s[1]), Integer.parseInt(s[2])};
    }

    private static int parseUIIdFromEvent(String eventInfo) {
        String[] s = eventInfo.split("_");
        return Integer.parseInt(s[3]);
    }

    private static String createEventInfo(int row, int cell) {
        return "cell_" + row + "_" + cell + "_" + UI.getCurrent().getUIId();
    }

//    private void removeActiveCell() {
//        if (myHighlightedCell > -1 && myHighlightedRow > -1) {
//            formManager.highlight("cell_" + myHighlightedRow + "_" + myHighlightedCell, false);
//        }
//
//        if (null != null) {
//            myHighlightedRow = ((CellReference) null).getRow();
//            myHighlightedCell = ((CellReference) null).getCol();
//
//            formManager.highlight("cell_" + myHighlightedRow + "_" + myHighlightedCell, true);
//        }
//    }


    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        try {
            File file = new File(fileName);
            file.deleteOnExit();
            uploadedFile = file;
            return new FileOutputStream(uploadedFile);
        } catch (FileNotFoundException e) {
            getLogger().warn("ERROR reading file " + fileName, e);
        }
        return null;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        AtomicReference<CellRangeAddress> selectedCells = new AtomicReference<>();
        AtomicReference<CellRangeAddress> selectedCellMergedRegion = new AtomicReference<>();
        AtomicReference<CellReference> selectedCellReference = new AtomicReference<>();
        spreadsheet.addSelectionChangeListener(e -> {
            selectedCells.set(e.getCellRangeAddresses().stream().findFirst().orElse(null));
            selectedCellMergedRegion.set(e.getSelectedCellMergedRegion());
            selectedCellReference.set(e.getSelectedCellReference());
        });

        Dialog uploadFileDialog = createUploadDialog();

        MenuItem fileMenu = menuBar.addItem("File");
        SubMenu fileSubMenu = fileMenu.getSubMenu();
        createIconItem(fileSubMenu, VaadinIcon.UPLOAD, "Upload", "Upload", e -> uploadFileDialog.open());
        createIconItem(fileSubMenu, VaadinIcon.DOWNLOAD, "Download", "Download", e -> downloadSpreadsheetFile());

        MenuItem viewMenu = menuBar.addItem("View");
        SubMenu viewSubMenu = viewMenu.getSubMenu();
        createCheckableItem(viewSubMenu, "Grid lines", true,
                e -> spreadsheet.setGridlinesVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Column and row headings", true,
                e -> spreadsheet.setRowColHeadingsVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Top bar", true,
                e -> spreadsheet.setFunctionBarVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Bottom bar", true,
                e -> spreadsheet.setSheetSelectionBarVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Report mode", false,
                e -> spreadsheet.setReportStyle(e.getSource().isChecked()));

        MenuItem formatMenu = menuBar.addItem("Format");
        SubMenu formatSubMenu = formatMenu.getSubMenu();

        createIconItem(formatSubMenu, VaadinIcon.BOLD, "Bold", "Bold",
                e -> changeSelectedCellsFont(font -> font.setBold(!font.getBold())));
        createIconItem(formatSubMenu, VaadinIcon.ITALIC, "Italic", "Italic",
                e -> changeSelectedCellsFont(font -> font.setItalic(!font.getItalic())));

        MenuItem colorMenu = formatSubMenu.addItem("Color");
        SubMenu colorSubMenu = colorMenu.getSubMenu();

        MenuItem textColorMenu = colorSubMenu.addItem("Text");
        textColorMenu.getSubMenu().addItem("Black",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.BLACK, null))));
        textColorMenu.getSubMenu().addItem("Blue",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.BLUE, null))));
        textColorMenu.getSubMenu().addItem("Red",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.RED, null))));
        textColorMenu.getSubMenu().addItem("Green",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.GREEN, null))));
        textColorMenu.getSubMenu().addItem("Orange",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.ORANGE, null))));

        MenuItem backgroundColorMenu = colorSubMenu.addItem("Background");
        backgroundColorMenu.getSubMenu().addItem("Light gray", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.LIGHT_GRAY, null))));
        backgroundColorMenu.getSubMenu().addItem("White", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.WHITE, null))));
        backgroundColorMenu.getSubMenu().addItem("Cyan", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.CYAN, null))));
        backgroundColorMenu.getSubMenu().addItem("Pink", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.PINK, null))));
        backgroundColorMenu.getSubMenu().addItem("Yellow", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.YELLOW, null))));
        backgroundColorMenu.getSubMenu().addItem("Dark gray", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.DARK_GRAY, null))));

        MenuItem mergeMenu = menuBar.addItem("Merge");
        SubMenu mergeSubMenu = mergeMenu.getSubMenu();

        mergeSubMenu.addItem("Merge selected", e -> mergeSelectedCells(selectedCells.get()));
        mergeSubMenu.addItem("Unmerge selected", e -> unmergeSelectedRegion(selectedCellMergedRegion.get()));

        MenuItem miscMenu = menuBar.addItem("Miscellaneous");
        SubMenu miscSubMenu = miscMenu.getSubMenu();
        miscSubMenu.addItem("Add comment", e -> addComment(selectedCellReference.get()));

        MenuItem freezePanesMenu = miscSubMenu.addItem("Freeze panes");
        SubMenu freezePanesSubMenu = freezePanesMenu.getSubMenu();
        freezePanesSubMenu.addItem("Freeze columns to selected", e -> spreadsheet
                .createFreezePane(spreadsheet.getLastFrozenRow(), spreadsheet.getSelectedCellReference().getCol()));
        freezePanesSubMenu.addItem("Freeze rows to selected", e -> spreadsheet
                .createFreezePane(spreadsheet.getSelectedCellReference().getRow(), spreadsheet.getLastFrozenColumn()));
        freezePanesSubMenu.addItem("Unfreeze all", e -> spreadsheet.removeFreezePane());

        MenuItem tableMenu = miscSubMenu.addItem("Table");
        SubMenu tableSubMenu = tableMenu.getSubMenu();
        tableSubMenu.addItem("Create table", e -> createTable(selectedCells.get()));

        return menuBar;
    }

    private Dialog createUploadDialog() {
        Upload uploadSpreadsheet = new Upload(this);

        Dialog uploadFileDialog = new Dialog();
        uploadFileDialog.setHeaderTitle("Upload a spreadsheet file");
        uploadFileDialog.addOpenedChangeListener(e -> {
            uploadSpreadsheet.clearFileList();
        });
        uploadFileDialog.add(uploadSpreadsheet);

        Button openSpreadsheetButton = new Button("Open spreadsheet", ev -> {
            if (uploadedFile != null) {
                try {
                    if (previousFile == null
                            || !previousFile.getAbsolutePath().equals(uploadedFile.getAbsolutePath())) {
                        spreadsheet.read(uploadedFile);
                        previousFile = uploadedFile;
                        uploadFileDialog.close();
                    } else {
                        Notification.show("Please, select a different file.");
                    }
                } catch (Exception e) {
                    getLogger().warn("ERROR reading file " + uploadedFile, e);
                }
            } else {
                Notification.show("Please, select a file to upload first.");
            }
        });
        openSpreadsheetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> uploadFileDialog.close());

        uploadFileDialog.getFooter().add(cancelButton, openSpreadsheetButton);
        return uploadFileDialog;
    }

    private void downloadSpreadsheetFile() {
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            spreadsheet.write(outputStream);
            final StreamResource resource = new StreamResource("file.xlsx",
                    () -> new ByteArrayInputStream(outputStream.toByteArray()));
            final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry()
                    .registerResource(resource);
            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
        } catch (Exception e) {
            getLogger().warn("Error while processing the file to download", e);
        }
    }

    private void changeSelectedCellsFont(Consumer<XSSFFont> fontConsumer) {
        changeSelectedCellsStyle(cellStyle -> {
            XSSFFont cellFont = (XSSFFont) cloneFont(cellStyle);
            fontConsumer.accept(cellFont);
            cellStyle.setFont(cellFont);
        });
    }

    private void changeSelectedCellsStyle(Consumer<XSSFCellStyle> cellStyleConsumer) {
        final ArrayList<Cell> cellsToRefresh = new ArrayList<>();
        spreadsheet.getSelectedCellReferences().forEach(cellReference -> {
            Cell cell = getOrCreateCell(cellReference);
            CellStyle cellStyle = cell.getCellStyle();
            XSSFCellStyle newCellStyle = (XSSFCellStyle) spreadsheet.getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(cellStyle);

            cellStyleConsumer.accept(newCellStyle);

            cell.setCellStyle(newCellStyle);

            cellsToRefresh.add(cell);
        });
        spreadsheet.refreshCells(cellsToRefresh);
    }

    private Cell getOrCreateCell(CellReference cellRef) {
        Cell cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
        if (cell == null) {
            cell = spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(), "");
        }
        return cell;
    }

    private Font cloneFont(CellStyle cellstyle) {
        Font newFont = spreadsheet.getWorkbook().createFont();
        Font originalFont = spreadsheet.getWorkbook().getFontAt(cellstyle.getFontIndex());
        if (originalFont != null) {
            newFont.setBold(originalFont.getBold());
            newFont.setItalic(originalFont.getItalic());
            newFont.setFontHeight(originalFont.getFontHeight());
            newFont.setUnderline(originalFont.getUnderline());
            newFont.setStrikeout(originalFont.getStrikeout());
            // This cast an only be done when using .xlsx files
            XSSFFont originalXFont = (XSSFFont) originalFont;
            XSSFFont newXFont = (XSSFFont) newFont;
            newXFont.setColor(originalXFont.getXSSFColor());
        }
        return newFont;
    }

    private MenuItem createCheckableItem(HasMenuItems menu, String item, boolean checked,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        MenuItem menuItem = menu.addItem(item, clickListener);
        menuItem.setCheckable(true);
        menuItem.setChecked(checked);

        return menuItem;
    }

    private MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName, String label, String ariaLabel,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Icon icon = new Icon(iconName);

        icon.getStyle().set("width", "var(--lumo-icon-size-s)");
        icon.getStyle().set("height", "var(--lumo-icon-size-s)");
        icon.getStyle().set("marginRight", "var(--lumo-space-s)");

        MenuItem item = menu.addItem(icon, clickListener);

        if (ariaLabel != null) {
            item.getElement().setAttribute("aria-label", ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private void mergeSelectedCells(CellRangeAddress selectedCells) {
        if (selectedCells == null) {
            Notification.show("Please select a region of cells to be merged.");
            return;
        }
        spreadsheet.addMergedRegion(selectedCells);
    }

    private void unmergeSelectedRegion(CellRangeAddress selectedCellMergedRegion) {
        if (selectedCellMergedRegion == null) {
            Notification.show("Please select a merged region of cells to be unmerged.");
            return;
        }
        for (int i = 0; i < spreadsheet.getActiveSheet().getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = spreadsheet.getActiveSheet().getMergedRegion(i);
            if (selectedCellMergedRegion.getFirstRow() == mergedRegion.getFirstRow()
                    && selectedCellMergedRegion.getFirstColumn() == mergedRegion.getFirstColumn()) {
                spreadsheet.removeMergedRegion(i);
            }
        }
    }

    private void addComment(CellReference cellReference) {
        Cell cell = getOrCreateCell(cellReference);
        createCellComment(spreadsheet, spreadsheet.getActiveSheet(), cell, cellReference);
        spreadsheet.refreshCells(cell);
        spreadsheet.editCellComment(cellReference);
    }

    private void createCellComment(Spreadsheet spreadsheet, Sheet sheet, Cell cell, CellReference cellRef) {
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("");
        comment.setString(str);

        // Fetch author from provider or fall back to default
        String author = null;
        if (spreadsheet.getCommentAuthorProvider() != null) {
            author = spreadsheet.getCommentAuthorProvider().getAuthorForComment(cellRef);
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Spreadsheet User";
        }
        comment.setAuthor(author);

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    private void createTable(CellRangeAddress cellAddresses) {
        if (cellAddresses == null) {
            Notification.show("Please select a region of cells to create the table.");
            return;
        }
        SpreadsheetTable table = new SpreadsheetFilterTable(spreadsheet, cellAddresses);
        spreadsheet.registerTable(table);
    }
}
