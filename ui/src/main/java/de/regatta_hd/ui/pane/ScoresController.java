package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class ScoresController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(ScoresController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private Button calculateBtn;
	@FXML
	private TableView<Score> scoreTbl;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		handleRefresh();
	}

	@FXML
	void handleRefresh() {
		disableButtons(true);
		setScores(Collections.emptyList());
		this.dbTask.run(() -> this.regattaDAO.getScores(), scores -> {
			try {
				setScores(scores.getResult());
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
		setScores(Collections.emptyList());
		this.dbTask.runInTransaction(() -> this.regattaDAO.calculateScores(), scores -> {
			try {
				setScores(scores.getResult());
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void setScores(List<Score> scores) {
		this.scoreTbl.setItems(FXCollections.observableArrayList(scores));
		if (!scores.isEmpty()) {
			FxUtils.autoResizeColumns(this.scoreTbl);
		}
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.calculateBtn.setDisable(disabled);
	}
}
