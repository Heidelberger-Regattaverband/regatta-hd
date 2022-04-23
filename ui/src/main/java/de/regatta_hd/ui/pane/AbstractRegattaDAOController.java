package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.RegattaDAO.RegattaChangedEventListener;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.core.ListenerManager;
import javafx.application.Platform;

abstract class AbstractRegattaDAOController extends AbstractBaseController {

	@Inject
	protected RegattaDAO regattaDAO;

	@Inject
	protected ListenerManager listenerManager;

	private final RegattaChangedEventListener regattaChangedEventListener = event -> {
		setTitle(getTitle(event.getActiveRegatta()));
		onActiveRegattaChanged(event.getActiveRegatta());
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.listenerManager.addListener(RegattaChangedEventListener.class, this.regattaChangedEventListener);

		Platform.runLater(() -> {
			setTitle(getTitle(this.db.isOpen() ? this.regattaDAO.getActiveRegatta() : null));
		});
	}

	@Override
	public void shutdown() {
		this.listenerManager.removeListener(RegattaChangedEventListener.class, this.regattaChangedEventListener);
	}

	protected abstract String getTitle(Regatta activeRegatta);

	protected abstract void onActiveRegattaChanged(Regatta activeRegatta);
}
