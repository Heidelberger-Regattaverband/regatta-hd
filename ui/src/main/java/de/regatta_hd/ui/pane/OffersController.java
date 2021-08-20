package de.regatta_hd.ui.pane;

import com.google.inject.Inject;
import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.model.Offer;
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
	public TableColumn<Offer, String> raceNumber;
	@FXML
	public TableColumn<Offer, String> labelShort;
	@FXML
	public TableColumn<Offer, String> labelLong;
	@FXML
	public TableColumn<Offer, String> distance;

	@Inject
	private RegattaDAO regatta;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.raceNumber.setCellValueFactory(new PropertyValueFactory<>("raceNumber"));
		this.raceNumber.setStyle("-fx-alignment: CENTER;");
		this.labelShort.setCellValueFactory(new PropertyValueFactory<>("shortLabel"));
		this.labelLong.setCellValueFactory(new PropertyValueFactory<>("longLabel"));
		this.distance.setCellValueFactory(new PropertyValueFactory<>("distance"));
		this.distance.setStyle("-fx-alignment: CENTER-RIGHT;");

		// add your data to the table here.
		this.tbData.setItems(getOffers());
	}

	private ObservableList<Offer> getOffers() {
		return FXCollections.observableArrayList(this.regatta.getOffers());
	}
}
