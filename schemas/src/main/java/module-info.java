module de.regatta_hd.schemas {

	requires java.xml;

	requires transitive jakarta.xml.bind;

	requires org.apache.commons.lang3;

	exports de.regatta_hd.schemas.xml;
	exports de.rudern.schemas.service.wettkampfrichter._2017;
	exports de.rudern.schemas.service.meldungen._2010;

	opens de.rudern.schemas.service.wettkampfrichter._2017 to jakarta.xml.bind;
	opens de.rudern.schemas.service.meldungen._2010 to jakarta.xml.bind;
}