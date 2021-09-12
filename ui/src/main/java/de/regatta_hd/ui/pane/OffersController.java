package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Offer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class OffersController extends AbstractBaseController {
	@FXML
	private TableView<Offer> tbData;

	@Inject
	private RegattaDAO regatta;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		// add your data to the table here.
		this.tbData.setItems(getOffers());
	}

	private ObservableList<Offer> getOffers() {
		return FXCollections.observableArrayList(this.regatta.getOffers());
	}
}
