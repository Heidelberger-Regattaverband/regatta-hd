package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.ResultEntry;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ResultsController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(ResultsController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private Button setPointsBtn;
	@FXML
	private TableView<ResultEntry> resultsTbl;
	@FXML
	private TableColumn<ResultEntry, String> numberCol;

	private final ObservableList<ResultEntry> resultsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.resultsTbl.setItems(this.resultsList);
		this.resultsTbl.getSortOrder().add(this.numberCol);

		loadResults(false);
	}

	private void loadResults(boolean refresh) {
		disableButtons(true);
		this.resultsList.clear();

		this.dbTask.run(() -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getOfficialResults();
		}, dbResult -> {
			try {
				this.resultsList.setAll(dbResult.getResult());
				this.resultsTbl.sort();
				FxUtils.autoResizeColumns(this.resultsTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	public void handleSetPointsOnAction() {
		disableButtons(true);
		this.resultsList.clear();

		this.dbTask.runInTransaction(() -> {
			this.resultsList.forEach(resultEntry -> {
				Race race = resultEntry.getHeat().getRace();
				short laneCount = race.getRaceMode().getLaneCount();
				byte numRowers = race.getBoatClass().getNumRowers();

				List<HeatRegistration> heatRegs = resultEntry.getHeat().getHeatRegistrationsOrderedByRank();
				for (HeatRegistration heatReg : heatRegs) {
					Result result = heatReg.getFinalResult();
					float pointsBoat = 0;
					if (result.getRank() > 0) {
						pointsBoat = (numRowers * (laneCount + 1 - result.getRank()));
					}
					result.setPoints(Float.valueOf(pointsBoat));
					this.db.getEntityManager().merge(result);
				}
			});
			this.db.getEntityManager().flush();
			return this.regattaDAO.getOfficialResults();
		}, dbResult -> {
			try {
				this.resultsList.setAll(dbResult.getResult());
				this.resultsTbl.sort();
				FxUtils.autoResizeColumns(this.resultsTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	public void handleRefreshOnAction() {
		disableButtons(true);

		loadResults(true);

		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setPointsBtn.setDisable(disabled);
	}
}
