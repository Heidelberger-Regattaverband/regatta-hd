package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxConstants;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.UIModule;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class RegattasController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(RegattasController.class.getName());

	// toolbar
	@FXML
	private Button refreshBtn;

	// regattas table
	@FXML
	private TableView<Regatta> regattasTbl;
	@FXML
	private TableColumn<Regatta, Integer> idCol;
	@FXML
	private TableColumn<Regatta, Boolean> activeCol;
	@FXML
	private TableColumn<Regatta, String> titleCol;
	@FXML
	private TableColumn<Regatta, Instant> beginCol;
	@FXML
	private TableColumn<Regatta, Instant> endCol;

	// injections
	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;

	private final ObservableList<Regatta> regattasList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.idCol.visibleProperty().addListener((obs, newVal, oldVal) -> {
			DoubleBinding usedWidth = this.activeCol.widthProperty().add(this.beginCol.widthProperty())
					.add(this.endCol.widthProperty());
			if (this.idCol.isVisible()) {
				usedWidth = usedWidth.add(this.idCol.widthProperty());
			}

			this.titleCol.prefWidthProperty().bind(
					this.regattasTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));
		});
		this.idCol.visibleProperty().bind(this.showIdColumn);

		this.regattasTbl.setItems(this.regattasList);
		this.regattasTbl.getSortOrder().add(this.beginCol);

		loadRegattas(false);
	}

	@FXML
	void handleRefreshOnAction() {
		loadRegattas(true);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadRegattas(true);
		} else {
			this.regattasList.clear();
			disableButtons(true);
		}
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("PrimaryView.regattasMitm.text") + " - " + activeRegatta.getTitle()
				: getText("PrimaryView.regattasMitm.text");
	}

	private void loadRegattas(boolean refresh) {
		disableButtons(true);

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getRegattas();
		}, dbResult -> {
			try {
				this.regattasList.setAll(dbResult.getResult());
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
		this.regattasTbl.setDisable(disabled);
	}

}
