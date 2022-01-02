package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class ScoreController extends AbstractBaseController {

	@Inject
	private RegattaDAO regattaDAO;

	@FXML
	private TableView<Score> scoreTbl;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.dbTask.run(() -> this.regattaDAO.getScores(), scores -> {
			this.scoreTbl.setItems(FXCollections.observableArrayList(scores));
			FxUtils.autoResizeColumns(this.scoreTbl);
		});
	}

}
