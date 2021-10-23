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
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Race.GroupMode;
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
	private TableView<Race> offersTbl;
	@FXML
	private TableColumn<Race, Race.GroupMode> groupModeCol;
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
		this.offersTbl.setItems(FXCollections.observableArrayList(this.regatta.getRaces()));
	}

	@FXML
	private void refresh() {
		this.db.getEntityManager().clear();
		this.offersTbl.setItems(FXCollections.observableArrayList(this.regatta.getRaces()));
	}

	@FXML
	private void setDistances() {
		disableButtons(true);

		TaskUtils.createAndRunTask(() -> {
			List<Race> races = new ArrayList<>();

			EntityManager entityManager = OffersController.this.db.getEntityManager();
			entityManager.getTransaction().begin();

			OffersController.this.regatta.getRaces().forEach(race -> {
				AgeClassExt ageClassExt = race.getAgeClass().getExtension();
				if (ageClassExt != null) {
					short distance = ageClassExt.getDistance();
					if (distance > 0 && distance != race.getDistance()) {
						race.setDistance(distance);
						entityManager.merge(race);
						races.add(race);
					}
				}
			});

			entityManager.getTransaction().commit();

			Platform.runLater(() -> {
				if (races.isEmpty()) {
					showDialog("Keine Rennen geändert.");
				} else {
					refresh();
					showDialog(String.format("%d Rennen geändert.", races.size()));
				}
				disableButtons(false);
			});

			return races;
		});
	}

	@FXML
	private void setMastersAgeClasses() {
		disableButtons(true);

		TaskUtils.createAndRunTask(() -> {
			List<Race> races = new ArrayList<>();

			EntityManager entityManager = OffersController.this.db.getEntityManager();
			entityManager.getTransaction().begin();

			OffersController.this.regatta.getRaces().forEach(race -> {
				AgeClass ageClass = race.getAgeClass();
				GroupMode mode = race.getGroupMode();
				if (ageClass.isMasters() && !mode.equals(GroupMode.AGE)) {
					race.setGroupMode(GroupMode.AGE);
					entityManager.merge(race);
					races.add(race);
				}
			});
			entityManager.getTransaction().commit();

			Platform.runLater(() -> {
				if (races.isEmpty()) {
					showDialog("Keine Masters Rennen geändert.");
				} else {
					refresh();
					showDialog(String.format("%d Masters Rennen geändert.", races.size()));
				}
				disableButtons(false);
			});
			return races;
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
