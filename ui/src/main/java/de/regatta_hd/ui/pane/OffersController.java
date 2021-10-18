package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.AgeClassExt;
import de.regatta_hd.aquarius.model.Offer;
import de.regatta_hd.aquarius.model.Offer.GroupMode;
import jakarta.persistence.EntityManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class OffersController extends AbstractBaseController {
	@FXML
	private TableView<Offer> offersTbl;
	@FXML
	private TableColumn<Offer, Offer.GroupMode> groupModeCol;
	@FXML
	private Button refreshBtn;
	@FXML
	private Button setDistancesBtn;
	@FXML
	private Button setMastersAgeClassesBtn;

	@Inject
	private RegattaDAO regatta;

	@Inject
	private AquariusDB db;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.groupModeCol.setCellFactory(TextFieldTableCell.forTableColumn(new GroupModeStringConverter()));

		// add your data to the table here.
		this.offersTbl.setItems(FXCollections.observableArrayList(this.regatta.getOffers()));
	}

	@FXML
	private void refresh() {
		this.db.getEntityManager().clear();
		this.offersTbl.setItems(FXCollections.observableArrayList(this.regatta.getOffers()));
	}

	@FXML
	private void setDistances() {
		disableButtons(true);

		TaskUtils.createAndRunTask(() -> {
			List<Offer> updatedOffers = new ArrayList<>();

			EntityManager entityManager = OffersController.this.db.getEntityManager();
			entityManager.getTransaction().begin();

			OffersController.this.regatta.getOffers().forEach(offer -> {
				AgeClassExt ageClassExt = offer.getAgeClass().getExtension();
				if (ageClassExt != null) {
					short distance = ageClassExt.getDistance();
					if (distance > 0 && distance != offer.getDistance()) {
						offer.setDistance(distance);
						entityManager.merge(offer);
						updatedOffers.add(offer);
					}
				}
			});

			entityManager.getTransaction().commit();

			Platform.runLater(() -> {
				if (updatedOffers.isEmpty()) {
					showDialog("Keine Ausschreibungen geändert.");
				} else {
					refresh();
					showDialog(String.format("%d Ausschreibungen geändert.", updatedOffers.size()));
				}
				disableButtons(false);
			});

			return updatedOffers;
		});
	}

	@FXML
	private void setMastersAgeClasses() {
		disableButtons(true);

		TaskUtils.createAndRunTask(() -> {
			List<Offer> updatedOffers = new ArrayList<>();

			EntityManager entityManager = OffersController.this.db.getEntityManager();
			entityManager.getTransaction().begin();

			OffersController.this.regatta.getOffers().forEach(offer -> {
				AgeClass ageClass = offer.getAgeClass();
				GroupMode mode = offer.getGroupMode();
				if (ageClass.isMasters() && !mode.equals(GroupMode.AGE)) {
					offer.setGroupMode(GroupMode.AGE);
					entityManager.merge(offer);
					updatedOffers.add(offer);
				}
			});
			entityManager.getTransaction().commit();

			Platform.runLater(() -> {
				if (updatedOffers.isEmpty()) {
					showDialog("Keine Masters Rennen geändert.");
				} else {
					refresh();
					showDialog(String.format("%d Masters Rennen geändert.", updatedOffers.size()));
				}
				disableButtons(false);
			});
			return updatedOffers;
		});
	}

	private static void showDialog(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION, null, ButtonType.OK);
		alert.setHeaderText(msg);
		alert.showAndWait();
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setDistancesBtn.setDisable(disabled);
		this.setMastersAgeClassesBtn.setDisable(disabled);
	}
}
