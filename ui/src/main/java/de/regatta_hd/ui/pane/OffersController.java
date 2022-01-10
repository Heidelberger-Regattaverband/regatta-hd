package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.ui.util.FxUtils;
import de.regatta_hd.ui.util.GroupModeStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class OffersController extends AbstractBaseController {

	// DB components
	@Inject
	private RegattaDAO regattaDao;

	// UI Controls
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

	// fields
	private final ObservableList<Race> racesObservableList = FXCollections.observableArrayList();

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

	@FXML
	private void refresh() {
		loadRaces();
	}

	@FXML
	private void setDistances() {
		disableButtons(true);

		this.dbTask.runInTransaction(this.regattaDao::setDistances, dbResult -> {
			try {
				List<Race> races = dbResult.getResult();
				if (races.isEmpty()) {
					FxUtils.showInfoDialog("Keine Rennen geändert.");
				} else {
					refresh();
					FxUtils.showInfoDialog(String.format("%d Rennen geändert.", Integer.valueOf(races.size())));
				}
			} catch (Exception e) {
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	private void setMastersAgeClasses() {
		disableButtons(true);

		this.dbTask.runInTransaction(this.regattaDao::enableMastersAgeClasses, dbResult -> {
			try {
				List<Race> races = dbResult.getResult();
				if (races.isEmpty()) {
					FxUtils.showInfoDialog("Keine Masters Rennen geändert.");
				} else {
					refresh();
					FxUtils.showInfoDialog(String.format("%d Masters Rennen geändert.", Integer.valueOf(races.size())));
				}
			} catch (Exception e) {
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void loadRaces() {
		disableButtons(true);
		this.racesObservableList.clear();

		this.dbTask.run(() -> {
			this.db.getEntityManager().clear();
			return this.regattaDao.getRaces();
		}, dbResult -> {
			try {
				this.racesObservableList.setAll(dbResult.getResult());
				FxUtils.autoResizeColumns(this.racesTbl);
			} catch (Exception e) {
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setDistancesBtn.setDisable(disabled);
		this.setMastersAgeClassesBtn.setDisable(disabled);
	}
}
