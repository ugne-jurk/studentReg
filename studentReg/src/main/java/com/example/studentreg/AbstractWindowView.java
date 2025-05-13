package com.example.studentreg;

import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * Abstract class that defines the standard structure for UI tabs/windows in the application
 */
public abstract class AbstractWindowView<T> {

    protected VBox mainPane;
    protected TableView<T> dataTable;

    /**
     * Constructs a new window view with standard padding and spacing
     */
    public AbstractWindowView() {
        mainPane = new VBox(10);
        mainPane.setPadding(new Insets(10));
        dataTable = new TableView<>();
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Creates input fields for data entry
     */
    protected abstract void createInputFields();

    /**
     * Creates action buttons for the view
     */
    protected abstract void createActionButtons();

    /**
     * Sets up the data table with appropriate columns
     */
    protected abstract void setupDataTable();

    /**
     * Sets up event handlers for selections and actions
     */
    protected abstract void setupEventHandlers();

    /**
     * Clears all input fields
     */
    protected abstract void clearInputFields();

    /**
     * Builds the complete window/tab
     * @param title The title of the tab
     * @return The constructed Tab object
     */
    public Tab createTab(String title) {
        // Create standard UI components
        createInputFields();
        createActionButtons();
        setupDataTable();
        setupEventHandlers();

        // Assemble the complete pane
        assembleWindowComponents();

        // Create and return the tab
        Tab tab = new Tab(title);
        tab.setContent(mainPane);
        return tab;
    }

    /**
     * Assembles all components into the main pane
     * Default implementation can be overridden by subclasses if needed
     */
    protected void assembleWindowComponents() {
        // By default, components should be added to mainPane in the implementation
    }
}