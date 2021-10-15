package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.AgeClassExt;
import de.regatta_hd.aquarius.model.Offer;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class OffersController extends AbstractBaseController {
	@FXML
	private TableView<Offer> table;

	@FXML
	private Button refreshBtn;

	@FXML
	private Button setDistancesBtn;

	@Inject
	private RegattaDAO regatta;

	@Inject
	private AquariusDB db;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		// add your data to the table here.
		this.table.setItems(getOffers());
	}

	private ObservableList<Offer> getOffers() {
		return FXCollections.observableArrayList(this.regatta.getOffers());
	}

	@FXML
	private void refresh() {
		this.table.refresh();
	}

	@FXML
	private void setDistances() {
		disableButtons(true);

		EntityManager entityManager = this.db.getEntityManager();
		entityManager.getTransaction().begin();

		this.regatta.getOffers().forEach(offer -> {
			AgeClassExt ageClassExt = offer.getAgeClass().getExtension();
			if (ageClassExt != null) {
				short distance = ageClassExt.getDistance();
				if (distance > 0 && distance != offer.getDistance()) {
					offer.setDistance(distance);
					entityManager.merge(offer);
				}
			}
		});

		entityManager.getTransaction().commit();

		refresh();
		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setDistancesBtn.setDisable(disabled);
	}
}
