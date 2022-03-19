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
import javafx.stage.Window;

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

		super.dbTaskRunner.run(progress -> {
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
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	void handleSetPointsOnAction() {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(progress -> {
			this.resultsList.forEach(resultEntry -> {
				Race race = resultEntry.getHeat().getRace();
				int maxPoints = race.getRaceMode().getLaneCount() + 1;
				byte numRowers = race.getBoatClass().getNumRowers();

				List<HeatRegistration> heatRegs = resultEntry.getHeat().getEntriesSortedByRank();
				for (HeatRegistration heatReg : heatRegs) {
					Result result = heatReg.getFinalResult();
					int pointsBoat = 0;
					if (result.getRank() > 0) {
						// 1.: 5 - 1 + 4 = 8
						// 2.: 5 - 2 + 4 = 7
						pointsBoat = maxPoints - result.getRank() + numRowers;
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
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	void handleRefreshOnAction() {
		disableButtons(true);

		loadResults(true);

		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setPointsBtn.setDisable(disabled);
	}

	private Window getWindow() {
		return this.refreshBtn.getScene().getWindow();
	}

}
