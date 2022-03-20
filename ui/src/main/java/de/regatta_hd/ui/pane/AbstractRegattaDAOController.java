package de.regatta_hd.ui.pane;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.common.ListenerManager;

class AbstractRegattaDAOController extends AbstractBaseController {

	@Inject
	protected RegattaDAO regattaDAO;

	@Inject
	protected ListenerManager listenerManager;
}
