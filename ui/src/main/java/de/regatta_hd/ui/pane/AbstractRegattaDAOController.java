package de.regatta_hd.ui.pane;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;

class AbstractRegattaDAOController extends AbstractBaseController {

	@Inject
	protected RegattaDAO regattaDAO;

}
