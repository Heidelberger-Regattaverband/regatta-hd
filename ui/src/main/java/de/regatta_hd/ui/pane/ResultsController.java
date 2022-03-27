package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.ResultEntry;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ResultsController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(ResultsController.class.getName());

	@FXML
	private Button refreshBtn;
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

		super.listenerManager.addListener(RegattaDAO.RegattaChangedEventListener.class, event -> {
			if (event.getActiveRegatta() != null) {
				loadResults(true);
			} else {
				this.resultsList.clear();
				disableButtons(true);
			}
		});
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("common.results") + " - " + activeRegatta.getTitle()
				: getText("common.results");
	}

	private void loadResults(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
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
				updatePlaceholder(getText("common.noDataAvailable"));
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

	private void updatePlaceholder(String text) {
		((Label) this.resultsTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
