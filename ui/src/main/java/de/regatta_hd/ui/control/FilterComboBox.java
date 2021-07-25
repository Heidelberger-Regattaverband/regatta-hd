package de.regatta_hd.ui.control;

import java.util.ArrayList;
import java.util.Collection;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * A control which provides a filtered combo box. As the user enters values into
 * the combo editor the list is filtered automatically.
 *
 * @param <T> the object type that is held by this FilteredComboBox
 */
public class FilterComboBox<T> extends ComboBox<T> {
	/**
	 * The default / initial list that is in the combo when nothing is entered in
	 * the editor.
	 */
	private Collection<T> initialList = new ArrayList<>();

	/**
	 * Check type. True if this is startsWith, false if it is contains.
	 */
	private final boolean startsWithCheck;

	public FilterComboBox() {
		this(false);
	}

	/**
	 * Constructs a new FilterComboBox with the given parameters.
	 *
	 * @param startsWithCheck <code>true</code> if this is a 'startsWith' check
	 *                        <code>false</code> if it is 'contains' check
	 */
	public FilterComboBox(@NamedArg("startsWithCheck") boolean startsWithCheck) {
		this.startsWithCheck = startsWithCheck;
		setEditable(true);
		configAutoFilterListener();
	}

	/**
	 * Constructs a new FilterComboBox with the given parameters.
	 *
	 * @param items           The initial items
	 * @param startsWithCheck true if this is a 'startsWith' check false if it is
	 *                        'contains' check
	 */
	public FilterComboBox(@NamedArg("items") ObservableList<T> items,
			@NamedArg("startsWithCheck") boolean startsWithCheck) {
		super(items);
		this.startsWithCheck = startsWithCheck;
		this.initialList = items;
		setEditable(true);
		configAutoFilterListener();
	}

	/**
	 * Set the initial list of items into this combo box.
	 *
	 * @param initial The initial list
	 */
	public void setInitialItems(Collection<T> initial) {
		getItems().clear();
		getItems().addAll(initial);
		this.initialList = initial;
	}

	/**
	 * Set up the auto filter on the combo.
	 */
	private void configAutoFilterListener() {
		getEditor().textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			T selected = getSelectionModel().getSelectedItem();

			if (selected == null || !getConverter().toString(selected).equals(getEditor().getText())) {
				filterItems(newValue);

				if (getItems().size() == 1) {
					setUserInputToOnlyOption();
					hide();
				} else if (!getItems().isEmpty()) {
					show();
					getEditor().setText(newValue);
				}
			}
		});
	}

	/**
	 * Method to filter the items and update the combo.
	 *
	 * @param filter The filter string to use.
	 */
	private void filterItems(String filter) {
		ObservableList<T> filteredList = FXCollections.observableArrayList();
		for (T item : this.initialList) {
			String itemString = getConverter().toString(item).toLowerCase();
			if (this.startsWithCheck && itemString.startsWith(filter.toLowerCase())) {
				filteredList.add(item);
			} else if (itemString.contains(filter.toLowerCase())) {
				filteredList.add(item);
			}
		}

		setItems(filteredList);
	}

	/**
	 * If there is only one item left in the combo then we assume this is correct.
	 * Put the item into the editor but select the end of the string that the user
	 * hasn't actually entered.
	 */
	private void setUserInputToOnlyOption() {
		String onlyOption = getConverter().toString(getItems().get(0));
		String currentText = getEditor().getText();
		if (onlyOption.length() > currentText.length()) {
			getEditor().setText(onlyOption);
			Platform.runLater(() -> getEditor().selectAll());
		}
	}
}
