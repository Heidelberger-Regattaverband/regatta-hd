package de.regatta_hd.aquarius.util;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Label;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.RegistrationLabel;

public class ModelUtils {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("aquarius_messages", Locale.GERMANY);

	public static final short FINAL_ROUND = 64;

	private ModelUtils() {
		// avoid instances
	}

	public static String getBoatLabel(HeatRegistration heatReg) {
		return getBoatLabel(heatReg.getRegistration(), heatReg.getHeat().getRound());
	}

	public static String getBoatLabel(Registration registration) {
		return getBoatLabel(registration, (short) 0);
	}

	private static String getBoatLabel(Registration registration, short round) {
		Optional<RegistrationLabel> boatLabelOpt = registration.getLabel(round);
		String boatLabel = boatLabelOpt.isPresent() ? boatLabelOpt.get().getLabel().getLabelShort()
				: registration.getClub().getAbbreviation();

		Short boatNumber = registration.getBoatNumber();
		if (registration.getBoatNumber() != null) {
			boatLabel += " - " + bundle.getString("registration.boatLabel") + " " + boatNumber;
		}

		return boatLabel;
	}

	public static boolean isEqualCrews(Registration reg1, Registration reg2) {
		// remove cox from comparison
		List<Crew> crews1 = reg1.getFinalCrews().stream().filter(crew -> !crew.isCox()).sorted(ModelUtils::compare)
				.toList();
		List<Crew> crews2 = reg2.getFinalCrews().stream().filter(crew -> !crew.isCox()).sorted(ModelUtils::compare)
				.toList();

		if (crews1.size() != crews2.size()) {
			return false;
		}

		for (int i = 0; i < crews1.size(); i++) {
			if (crews1.get(i).getAthlet().getId() != crews2.get(i).getAthlet().getId()) {
				return false;
			}
		}
		return true;
	}

	private static Stream<Club> getClubs(Registration registration) {
		return registration.getFinalCrews().stream().map(Crew::getClub).distinct();
	}

	public static Label createLabel(Registration registration) {
		boolean community = isCommunity(registration);

		String longLabel;
		String shortLabel;
		if (community) {
			List<String> clubAbbrs = getClubs(registration).map(Club::getAbbreviation).toList();
			List<String> clubNames = getClubs(registration).map(Club::getName).toList();
			longLabel = "Rgm. " + String.join(" / ", clubNames);
			shortLabel = "Rgm. " + String.join(" / ", clubAbbrs);
		} else {
			longLabel = registration.getClub().getName();
			shortLabel = registration.getClub().getAbbreviation();
		}

		return Label.builder().club(registration.getClub()).labelLong(longLabel).labelShort(shortLabel)
				.community(Boolean.valueOf(community)).build();
	}

	private static boolean isCommunity(Registration registration) {
		long count = getClubs(registration).count();
		return count != 1;
	}

	private static int compare(Crew crew1, Crew crew2) {
		if (crew1.getAthlet().getId() == crew2.getAthlet().getId()) {
			return 0;
		}
		return crew1.getAthlet().getId() > crew2.getAthlet().getId() ? 1 : -1;
	}

}
