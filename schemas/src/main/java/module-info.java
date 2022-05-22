module de.regatta_hd.schemas {

	requires java.xml;

	requires transitive jakarta.xml.bind;

	exports de.rudern.schemas.service.wettkampfrichter._2017;

	opens de.rudern.schemas.service.wettkampfrichter._2017 to jakarta.xml.bind;
}