package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class RegattasController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(RegattasController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private TableView<Regatta> regattasTbl;
	@FXML
	private TableColumn<Regatta, Integer> idCol;

	private final ObservableList<Regatta> regattasList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.regattasTbl.setItems(this.regattasList);
		this.regattasTbl.getSortOrder().add(this.idCol);

		loadRegattas(false);
	}

	@FXML
	public void handleRefreshOnAction() {
		loadRegattas(true);
	}

	@FXML
	private void handleSelectRegattaOnAction() {
		Regatta regatta = this.regattasTbl.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);
	}

	private void loadRegattas(boolean refresh) {
		disableButtons(true);
		this.regattasList.clear();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getRegattas();
		}, dbResult -> {
			try {
				this.regattasList.setAll(dbResult.getResult());
				FxUtils.autoResizeColumns(this.regattasTbl);
				this.regattasTbl.sort();
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
