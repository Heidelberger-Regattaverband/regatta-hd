package de.regatta_hd.ui.pane;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Offer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class OffersController extends AbstractBaseController {
	@FXML
	private TableView<Offer> tbData;

	@FXML
	public TableColumn<Offer, String> raceNumberCol;
	@FXML
	public TableColumn<Offer, String> labelShortCol;
	@FXML
	public TableColumn<Offer, String> labelLongCol;
	@FXML
	public TableColumn<Offer, String> distanceCol;

	@Inject
	private RegattaDAO regatta;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.raceNumberCol.setCellValueFactory(new PropertyValueFactory<>("raceNumber"));
		this.raceNumberCol.setStyle("-fx-alignment: CENTER;");
		this.labelShortCol.setCellValueFactory(new PropertyValueFactory<>("shortLabel"));
		this.labelLongCol.setCellValueFactory(new PropertyValueFactory<>("longLabel"));
		this.distanceCol.setCellValueFactory(new PropertyValueFactory<>("distance"));
		this.distanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");

		// add your data to the table here.
		this.tbData.setItems(getOffers());
	}

	private ObservableList<Offer> getOffers() {
		return FXCollections.observableArrayList(this.regatta.getOffers());
	}
}
