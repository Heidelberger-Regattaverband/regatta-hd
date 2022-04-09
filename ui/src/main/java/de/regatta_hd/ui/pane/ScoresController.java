package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.commons.fx.stage.WindowManager;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ScoresController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(ScoresController.class.getName());

	@Inject
	private WindowManager windowManager;

	@FXML
	private Button refreshBtn;
	@FXML
	private Button calculateBtn;
	@FXML
	private Button printBtn;
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

		loadScores(false);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadScores(true);
		} else {
			this.scoresList.clear();
			disableButtons(true);
		}
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("PrimaryView.scoresMitm.text") + " - " + activeRegatta.getTitle()
				: getText("PrimaryView.scoresMitm.text");
	}

	@FXML
	void handleRefreshOnAction() {
		loadScores(true);
	}

	@FXML
	void handleCalculateOnAction() {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		this.scoresList.clear();

		super.dbTaskRunner.runInTransaction(progress -> this.regattaDAO.calculateScores(), scores -> {
			try {
				this.scoresList.setAll(scores.getResult());
				this.scoresTbl.sort();
				FxUtils.autoResizeColumns(this.scoresTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				if (this.scoresList.isEmpty()) {
					updatePlaceholder(getText("common.noDataAvailable"));
				}
				disableButtons(false);
			}
		});
	}

	@FXML
	void handlePrintOnAction() {
		openStage("PrintView.fxml", getText("common.print"));
	}

	private void loadScores(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		this.scoresList.clear();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getScores();
		}, scores -> {
			try {
				this.scoresList.setAll(scores.getResult());
				this.scoresTbl.sort();
				FxUtils.autoResizeColumns(this.scoresTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	private Stage openStage(String resource, String title) {
		return this.windowManager.newStage(getClass().getResource(resource), title, this.resources, Modality.APPLICATION_MODAL);
	}

	private void updatePlaceholder(String text) {
		((Label) this.scoresTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.calculateBtn.setDisable(disabled);
		this.printBtn.setDisable(disabled);
	}

}
