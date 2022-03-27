package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.ListenerManager;
import javafx.application.Platform;

abstract class AbstractRegattaDAOController extends AbstractBaseController {

	@Inject
	protected RegattaDAO regattaDAO;

	@Inject
	protected ListenerManager listenerManager;

	protected abstract String getTitle(Regatta activeRegatta);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.listenerManager.addListener(RegattaDAO.RegattaChangedEventListener.class, event -> {
			setTitle(getTitle(event.getActiveRegatta()));
		});

		Platform.runLater(() -> {
			setTitle(getTitle(this.db.isOpen() ? this.regattaDAO.getActiveRegatta() : null));
		});
	}

}
