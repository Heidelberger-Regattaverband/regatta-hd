package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.Athlet;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.Label;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.RegistrationLabel;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.UIModule;
import de.regatta_hd.ui.util.AlternativeRegistration;
import de.regatta_hd.ui.util.RegistrationUtils;
import de.rudern.schemas.service.meldungen._2010.TBootsPosition;
import de.rudern.schemas.service.meldungen._2010.TRennen;
import jakarta.persistence.EntityManager;
import jakarta.xml.bind.JAXBException;
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
	@FXML
	private Button deselectAllBtn;
	@FXML
	private Button importBtn;

	// alt. registrations table
	@FXML
	private TableView<AlternativeRegistration> altRegsTbl;
	@FXML
	private TableColumn<AlternativeRegistration, Integer> extIdCol;
	@FXML
	private TableColumn<AlternativeRegistration, String> altRaceNumberCol;

	// injections
	@Inject
	private MasterDataDAO masterDAO;
	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;

	// fields
	private final ObservableList<AlternativeRegistration> altRegsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.altRegsTbl.setItems(this.altRegsList);
		this.altRegsTbl.getSortOrder().add(this.altRaceNumberCol);

		this.extIdCol.visibleProperty().bind(this.showIdColumn);
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
	void handleDeselectAllOnHandler() {
		disableButtons(true);

		this.altRegsList.forEach(altReg -> altReg.importProperty().set(false));

		disableButtons(false);
	}

	@FXML
	void handleImportBtnOnAction() {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(progMon -> {
			return this.altRegsList.stream().filter(altReg -> altReg.importProperty().get()).map(altReg -> {
				final EntityManager em = super.db.getEntityManager();

				List<TBootsPosition> mannschaft = altReg.getRegistration().getMannschaft().getPosition();

				String comment = getText("altRegs.comment", altReg.getPrimaryRaceNumber());
				// create new alternative registration
				final Registration registration = em.merge(Registration.builder().club(altReg.getClub())
						.race(altReg.getAlternativeRace()).regatta(altReg.getAlternativeRace().getRegatta())
						.externalId(Integer.valueOf(altReg.getRegistration().getId())).comment(comment).build());

				// create crew to registration
				Set<Crew> crews = mannschaft.stream().map(pos -> {
					Athlet athlet = this.masterDAO.getAthletViaExternalId(pos.getAthlet().getId());
					return em.merge(Crew.builder().athlet(athlet).pos((byte) pos.getNr()).club(athlet.getClub())
							.registration(registration).build());
				}).collect(Collectors.toSet());
				registration.setCrews(crews);

				Label label = Label.builder().club(altReg.getClub()).labelLong(altReg.getClub().getName())
						.labelShort(altReg.getClub().getAbbreviation()).build();
				label = em.merge(label);

				RegistrationLabel regLabel = RegistrationLabel.builder().registration(registration).roundFrom((short) 0)
						.roundTo((short) 64).label(label).build();
				em.merge(regLabel);

				return registration;
			}).collect(Collectors.toList());
		}, dbResult -> {
			try {
				List<Registration> importedRegs = dbResult.getResult();

				FxUtils.showInfoDialog(getWindow(), getText("altRegs.import.succeeded", Integer.valueOf(importedRegs.size())));
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			}
		});

		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.openAltRegsBtn.setDisable(disabled);
		this.deselectAllBtn.setDisable(disabled || this.altRegsList.isEmpty());
		this.importBtn.setDisable(disabled || this.altRegsList.isEmpty());
		this.altRegsTbl.setDisable(disabled);
	}

}
