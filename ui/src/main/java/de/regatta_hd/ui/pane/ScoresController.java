package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ScoresController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(ScoresController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private Button calculateBtn;
	@FXML
	private TableView<Score> scoresTbl;
	@FXML
	private TableColumn<Score, Integer> rankCol;

	private final ObservableList<Score> scoresList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.scoresTbl.setItems(this.scoresList);
		this.scoresTbl.getSortOrder().add(this.rankCol);

		handleRefresh();
	}

	@FXML
	void handleRefresh() {
		disableButtons(true);
		this.scoresList.clear();

		this.dbTask.run(progress -> this.regattaDAO.getScores(), scores -> {
			try {
				this.scoresList.setAll(scores.getResult());
				this.scoresTbl.sort();
				FxUtils.autoResizeColumns(this.scoresTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	void handleCalculate() {
		disableButtons(true);
		this.scoresList.clear();

		this.dbTask.runInTransaction(progress -> this.regattaDAO.calculateScores(), scores -> {
			try {
				this.scoresList.setAll(scores.getResult());
				this.scoresTbl.sort();
				FxUtils.autoResizeColumns(this.scoresTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.calculateBtn.setDisable(disabled);
	}
}
