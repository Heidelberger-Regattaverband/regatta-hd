package de.regatta_hd.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.regatta_hd.schemas.xml.XMLDataLoader;
import de.rudern.schemas.service.meldungen._2010.RegattaMeldungen;
import de.rudern.schemas.service.meldungen._2010.TMeldung;
import de.rudern.schemas.service.meldungen._2010.TRennen;
import jakarta.xml.bind.JAXBException;

public class RegistrationUtils {

	private RegistrationUtils() {
		// avoid instances
	}

	public static List<TRennen> getAlternativeRegistrations(File registrationFile) throws IOException, JAXBException {
		try (FileInputStream fileIn = new FileInputStream(registrationFile)) {
			RegattaMeldungen regattaMeldungen = XMLDataLoader.loadRegattaMeldungen(fileIn);

			List<TRennen> tRennenList = regattaMeldungen.getMeldungen().getRennen();

			for (int i = tRennenList.size() - 1; i >= 0; i--) {
				TRennen tRennen = tRennenList.get(i);
				List<TMeldung> tMeldungen = tRennen.getMeldung();

				for (int j = tMeldungen.size() - 1; j >= 0; j--) {
					TMeldung tMeldung = tMeldungen.get(j);
					if (StringUtils.isBlank(tMeldung.getAlternativeZu())) {
						tMeldungen.remove(j);
					}
				}

				if (tMeldungen.isEmpty()) {
					tRennenList.remove(i);
				}
			}

			return tRennenList;
		}
	}
}
