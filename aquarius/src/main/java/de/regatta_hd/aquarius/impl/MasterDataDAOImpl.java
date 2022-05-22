package de.regatta_hd.aquarius.impl;

import java.io.Reader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.aquarius.model.Referee;
import de.rudern.schemas.service.wettkampfrichter._2017.Liste;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

@Singleton
public class MasterDataDAOImpl extends AbstractDAOImpl implements MasterDataDAO {

	@Override
	public List<AgeClass> getAgeClasses() {
		return getEntities(AgeClass.class);
	}

	@Override
	public List<BoatClass> getBoatClasses() {
		return getEntities(BoatClass.class);
	}

	@Override
	public List<Club> getClubs() {
		return getEntities(Club.class);
	}

	@Override
	public AgeClass getAgeClass(int id) {
		return super.db.getEntityManager().find(AgeClass.class, Integer.valueOf(id));
	}

	@Override
	public BoatClass getBoatClass(int id) {
		return super.db.getEntityManager().find(BoatClass.class, Integer.valueOf(id));
	}

	@Override
	public List<LogRecord> getLogRecords(String hostName) {
		return super.db.getEntityManager()
				.createQuery("SELECT lr FROM LogRecord lr WHERE lr.hostName = :hostName ORDER BY lr.instant DESC",
						LogRecord.class)
				.setParameter("hostName", hostName).getResultList();
	}

	@Override
	public List<String> getHostNames() {
		return super.db.getEntityManager().createQuery("SELECT DISTINCT lr.hostName FROM LogRecord lr", String.class)
				.getResultList();
	}

	@Override
	public String getAquariusVersion() {
		return super.db.getVersion();
	}

	@Override
	public List<Referee> getReferees() {
		return getEntities(Referee.class);
	}

	@Override
	public int updateAllRefereesLicenceState(boolean licenceState) {
		return super.db.getEntityManager().createQuery("UPDATE Referee SET licenceState = :licenceState")
				.setParameter("licenceState", Boolean.valueOf(licenceState)).executeUpdate();
	}

	@Override
	public void importReferees(Reader reader) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Liste.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			saxFactory.setNamespaceAware(true);
			saxFactory.setValidating(false);
			XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
			Source source = new SAXSource(xmlReader, new InputSource(reader));

			Liste referees = (Liste) unmarshaller.unmarshal(source);

			if (referees != null) {
				referees.getWettkampfrichter().forEach(referee -> {
					Referee ref = Referee.builder().city(referee.getOrt()).externID(referee.getLizenznummer())
							.firstName(referee.getVorname()).lastName(referee.getVorname()).build();
					super.db.getEntityManager().merge(ref);
				});
			}
		} catch (SAXException | ParserConfigurationException | JAXBException e) {
			e.printStackTrace();
		}
	}
}
