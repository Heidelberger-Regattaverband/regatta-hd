package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Race.GroupMode;
import de.regatta_hd.ui.util.DBTask;
import de.regatta_hd.ui.util.FxUtils;
import de.regatta_hd.ui.util.GroupModeStringConverter;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class OffersController extends AbstractBaseController {

	private final ObservableList<Race> racesObservableList = FXCollections.observableArrayList();
	@FXML
	private TableView<Race> racesTbl;
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

	@Inject
	private DBTask dbTask;

	// needs to be a public getter, otherwise items are not bound
	public ObservableList<Race> getRacesObservableList() {
		return this.racesObservableList;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.groupModeCol.setCellFactory(TextFieldTableCell.forTableColumn(new GroupModeStringConverter()));

		loadRaces();
	}

	private void loadRaces() {
		disableButtons(true);

		this.dbTask.run(this.regatta::getRaces, races -> {
			this.racesObservableList.setAll(races);
			FxUtils.autoResizeColumns(this.racesTbl);
			disableButtons(false);
		});
	}

	@FXML
	private void refresh() {
		this.dbTask.run(() -> {
			this.db.getEntityManager().clear();
			return null;
		}, result -> {
			this.racesObservableList.clear();
			loadRaces();
		});
	}

	@FXML
	private void setDistances() {
		disableButtons(true);

		this.dbTask.runInTransaction(() -> {
			List<Race> races = new ArrayList<>();

			EntityManager entityManager = OffersController.this.db.getEntityManager();

			OffersController.this.regatta.getRaces().forEach(race -> {
				short distance = race.getAgeClass().getDistance();
				if (distance != race.getDistance()) {
					race.setDistance(distance);
					entityManager.merge(race);
					races.add(race);
				}
			});

			return races;
		}, races -> {
			if (races.isEmpty()) {
				FxUtils.showInfoDialog("Keine Rennen geändert.");
			} else {
				refresh();
				FxUtils.showInfoDialog(String.format("%d Rennen geändert.", Integer.valueOf(races.size())));
			}
			disableButtons(false);
		});
	}

	@FXML
	private void setMastersAgeClasses() {
		disableButtons(true);

		this.dbTask.runInTransaction(() -> {
			List<Race> races = new ArrayList<>();

			EntityManager entityManager = OffersController.this.db.getEntityManager();

			OffersController.this.regatta.getRaces().forEach(race -> {
				AgeClass ageClass = race.getAgeClass();
				GroupMode mode = race.getGroupMode();
				if (ageClass.isMasters() && !mode.equals(GroupMode.AGE)) {
					race.setGroupMode(GroupMode.AGE);
					entityManager.merge(race);
					races.add(race);
				}
			});

			return races;
		}, races -> {
			if (races.isEmpty()) {
				FxUtils.showInfoDialog("Keine Masters Rennen geändert.");
			} else {
				refresh();
				FxUtils.showInfoDialog(String.format("%d Masters Rennen geändert.", Integer.valueOf(races.size())));
			}
			disableButtons(false);
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setDistancesBtn.setDisable(disabled);
		this.setMastersAgeClassesBtn.setDisable(disabled);
	}

}
