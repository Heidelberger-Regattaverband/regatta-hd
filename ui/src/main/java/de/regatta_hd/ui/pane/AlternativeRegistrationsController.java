package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxConstants;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.UIModule;
import de.regatta_hd.ui.util.RegistrationUtils;
import de.rudern.schemas.service.meldungen._2010.TRennen;
import jakarta.xml.bind.JAXBException;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AlternativeRegistrationsController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(AlternativeRegistrationsController.class.getName());

	// toolbar
	@FXML
	private Button openAltRegsBtn;

	// regattas table
	@FXML
	private TableView<Regatta> altRegsTbl;
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

	// fields
	private final ObservableList<Regatta> altRegsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.idCol.visibleProperty().addListener((obs, newVal, oldVal) -> {
			DoubleBinding usedWidth = this.activeCol.widthProperty().add(this.beginCol.widthProperty())
					.add(this.endCol.widthProperty());
			if (this.idCol.isVisible()) {
				usedWidth = usedWidth.add(this.idCol.widthProperty());
			}

			this.titleCol.prefWidthProperty()
					.bind(this.altRegsTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));
		});
		this.idCol.visibleProperty().bind(this.showIdColumn);

		this.altRegsTbl.setItems(this.altRegsList);
		this.altRegsTbl.getSortOrder().add(this.beginCol);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			disableButtons(false);
		} else {
			this.altRegsList.clear();
			disableButtons(true);
		}
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("altRegs.title") + " - " + activeRegatta.getTitle()
				: getText("altRegs.title");
	}

	@FXML
	void handleOpenAltRegsBtnOnAction() {
		File regsFile = FxUtils.showOpenDialog(getWindow(), null, "Meldungen XML Datei", "*.xml");

		if (regsFile != null) {
			try {
				disableButtons(true);

				List<TRennen> altRegs = RegistrationUtils.getAlternativeRegistrations(regsFile);
				if (!altRegs.isEmpty()) {
					// TODO show in table
				} else {
					FxUtils.showInfoDialog(getWindow(), getText("altRegs.notFound"));
				}
			} catch (IOException | JAXBException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		}
	}

	private void disableButtons(boolean disabled) {
		this.openAltRegsBtn.setDisable(disabled);
		this.altRegsTbl.setDisable(disabled);
	}

}
