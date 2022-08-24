package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.util.AlternativeRegistration;
import de.regatta_hd.ui.util.RegistrationUtils;
import de.rudern.schemas.service.meldungen._2010.TRennen;
import jakarta.xml.bind.JAXBException;
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
	@FXML
	private Button importBtn;

	// alt. registrations table
	@FXML
	private TableView<AlternativeRegistration> altRegsTbl;
	@FXML
	private TableColumn<AlternativeRegistration, String> altRaceNumberCol;

	// injections
	@Inject
	private MasterDataDAO masterDAO;

	// fields
	private final ObservableList<AlternativeRegistration> altRegsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.altRegsTbl.setItems(this.altRegsList);
		this.altRegsTbl.getSortOrder().add(this.altRaceNumberCol);
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
				List<TRennen> altRegs = RegistrationUtils.getAlternativeRegistrations(regsFile);
				if (!altRegs.isEmpty()) {
					disableButtons(true);

					super.dbTaskRunner.run(progMon -> {
						return altRegs.stream().flatMap(tRennen -> tRennen.getMeldung().stream().map(tMeldung -> {
							Club club = this.masterDAO.getClubViaExternalId(tMeldung.getVerein());
							Race primaryRace = super.regattaDAO.getRace(tMeldung.getAlternativeZu(), null);
							Race alternativeRace = super.regattaDAO.getRace(tRennen.getNummer(), null);

							AlternativeRegistration altReg = AlternativeRegistration.builder()
									.alternativeRace(alternativeRace).primaryRace(primaryRace).registration(tMeldung)
									.club(club).build();
							altReg.getImportProperty().set(primaryRace.isCancelled());
							return altReg;
						})).collect(Collectors.toList());
					}, dbResult -> {
						try {
							this.altRegsList.setAll(dbResult.getResult());
							this.altRegsTbl.sort();
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
							FxUtils.showErrorMessage(getWindow(), e);
						} finally {
							disableButtons(false);
						}
					});
				} else {
					FxUtils.showInfoDialog(getWindow(), getText("altRegs.notFound"));
				}
			} catch (IOException | JAXBException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			}
		}
	}

	@FXML
	void handleImportBtnOnAction() {
		disableButtons(true);

		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.openAltRegsBtn.setDisable(disabled);
		this.importBtn.setDisable(disabled);
		this.altRegsTbl.setDisable(disabled);
	}

}
