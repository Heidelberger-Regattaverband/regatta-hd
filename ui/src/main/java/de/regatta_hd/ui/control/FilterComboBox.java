package de.regatta_hd.ui.control;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;

import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * A control which provides a filtered combo box. As the user enters values into the combo editor the list is filtered
 * automatically.
 *
 * @param <T> the object type that is held by this FilteredComboBox
 */
public class FilterComboBox<T> extends ComboBox<T> { // NOSONAR
	/**
	 * The default / initial list that is in the combo when nothing is entered in the editor.
	 */
	private Collection<T> initialList = new ArrayList<>();

	/**
	 * Check type. True if this is startsWith, false if it is contains.
	 */
	private final boolean startsWithCheck;

	private final TextChangeListener textChangeListener = new TextChangeListener();

	public FilterComboBox() {
		this(false);
	}

	/**
	 * Constructs a new FilterComboBox with the given parameters.
	 *
	 * @param startsWithCheck <code>true</code> if this is a 'startsWith' check <code>false</code> if it is 'contains'
	 *                        check
	 */
	public FilterComboBox(@NamedArg("startsWithCheck") boolean startsWithCheck) {
		this.startsWithCheck = startsWithCheck;
		setEditable(true);
		addTextChangeListener();
	}

	/**
	 * Constructs a new FilterComboBox with the given parameters.
	 *
	 * @param items           The initial items
	 * @param startsWithCheck true if this is a 'startsWith' check false if it is 'contains' check
	 */
	public FilterComboBox(@NamedArg("items") ObservableList<T> items,
			@NamedArg("startsWithCheck") boolean startsWithCheck) {
		super(items);
		this.startsWithCheck = startsWithCheck;
		this.initialList = items;
		setEditable(true);
		addTextChangeListener();
	}

	/**
	 * Set the initial list of items into this combo box.
	 *
	 * @param initial The initial list
	 */
	public void setInitialItems(Collection<T> initialItems) {
		requireNonNull(initialItems, "initialItems must not be null");
		this.initialList = new ArrayList<>(initialItems);
		getItems().clear();
		getItems().addAll(this.initialList);
	}

	private void addTextChangeListener() {
		getEditor().textProperty().addListener(this.textChangeListener);
	}

	private class TextChangeListener implements ChangeListener<String> {

		@Override
		public synchronized void changed(ObservableValue<? extends String> observable, String oldValue,
				String newValue) {
			T selected = getSelectionModel().getSelectedItem();

			if (selected == null || !getConverter().toString(selected).equals(getEditor().getText())) {
				filterItems(newValue);

				if (getItems().size() == 1) {
					// only one item satisfies filter -> select it
					setUserInputToOnlyOption();

					// hide drop down list as item is selected
					hide();
				} else if (!getItems().isEmpty()) {
					Platform.runLater(() -> {
						getEditor().textProperty().removeListener(FilterComboBox.this.textChangeListener);

						// several items satisfy filter -> clear current selection and show drop down list
						selectionModelProperty().getValue().clearSelection();

						addTextChangeListener();

						// show drop down list for selection
						show();
					});
				}
			}
		}

		/**
		 * Method to filter the items and update the combo.
		 *
		 * @param filter The filter string to use.
		 */
		private void filterItems(String filter) {
			ObservableList<T> filteredList = FXCollections.observableArrayList();
			for (T item : FilterComboBox.this.initialList) {
				String itemString = getConverter().toString(item).toLowerCase();
				if (FilterComboBox.this.startsWithCheck && itemString.startsWith(filter.toLowerCase())
						|| itemString.contains(filter.toLowerCase())) {
					filteredList.add(item);
				}
			}

			setItems(filteredList);
		}

		/**
		 * If there is only one item left in the combo, then we assume this is correct. Put the item into the editor but
		 * select the end of the string that the user hasn't actually entered.
		 */
		private void setUserInputToOnlyOption() {
			String onlyOption = getConverter().toString(getItems().get(0));
			String currentText = getEditor().getText();
			if (onlyOption.length() > currentText.length()) {
				Platform.runLater(() -> {
					getEditor().setText(onlyOption);
					getEditor().selectAll();
				});
			}
		}
	}
}
