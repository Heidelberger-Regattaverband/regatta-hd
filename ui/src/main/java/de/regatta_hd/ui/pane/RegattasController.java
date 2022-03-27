package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

	private final RegattaDAO.RegattaChangedEventListener regattaChangedEventListener = event -> {
		if (event.getActiveRegatta() != null) {
			loadRegattas(true);
		} else {
			this.regattasList.clear();
			disableButtons(true);
		}
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.regattasTbl.setItems(this.regattasList);
		this.regattasTbl.getSortOrder().add(this.idCol);

		loadRegattas(false);

		super.listenerManager.addListener(RegattaDAO.RegattaChangedEventListener.class,
				this.regattaChangedEventListener);
	}

	@Override
	protected void shutdown() {
		super.listenerManager.removeListener(RegattaDAO.RegattaChangedEventListener.class,
				this.regattaChangedEventListener);
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("PrimaryView.regattasMitm.text") + " - " + activeRegatta.getTitle()
				: getText("PrimaryView.regattasMitm.text");
	}

	@FXML
	public void handleRefreshOnAction() {
		loadRegattas(true);
	}

	private void loadRegattas(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
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
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	private void updatePlaceholder(String text) {
		((Label) this.regattasTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
