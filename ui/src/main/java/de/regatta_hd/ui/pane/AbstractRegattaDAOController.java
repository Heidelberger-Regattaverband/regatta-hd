package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.RegattaDAO.RegattaChangedEventListener;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.fx.stage.PaneController;
import de.regatta_hd.commons.fx.util.FxUtils;

abstract class AbstractRegattaDAOController extends PaneController {
	private static final Logger logger = Logger.getLogger(AbstractRegattaDAOController.class.getName());

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

		if (this.db.isOpen()) {
			super.dbTaskRunner.run(progress -> this.regattaDAO.getActiveRegatta(), dbResult -> {
				try {
					Regatta activeRegatta = dbResult.getResult();
					setTitle(getTitle(activeRegatta));
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				}
			});
		}else {
			setTitle(getTitle(null));
		}
	}

	@Override
	public void shutdown() {
		this.listenerManager.removeListener(RegattaChangedEventListener.class, this.regattaChangedEventListener);
	}

	protected abstract String getTitle(Regatta activeRegatta);

	protected abstract void onActiveRegattaChanged(Regatta activeRegatta);
}
