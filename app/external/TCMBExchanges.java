/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package external;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utils.DateUtils;
import external.model.TCMBRate;

/**
 * @author mdpinar
*/
public class TCMBExchanges {

	private final static Logger log = LoggerFactory.getLogger(TCMBExchanges.class);

	public static List<TCMBRate> getRates() {
		List<TCMBRate> result = new ArrayList<TCMBRate>();

		try {
			log.info("TC Merkez Bankası xml service connection is trying...");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setIgnoringElementContentWhitespace(true);
		   
			DocumentBuilder builder = dbf.newDocumentBuilder();
			URL u = new URL("http://www.tcmb.gov.tr/kurlar/today.xml");
			Document doc = builder.parse(u.openStream());

			NodeList nodesForFirstDate = doc.getElementsByTagName("Tarih_Date");
			Element elementForFirstDate = (Element) nodesForFirstDate.item(0);
			Date firstDate = DateUtils.parse(elementForFirstDate.getAttribute("Tarih"), "dd.MM.yyyy");

			NodeList nodes = doc.getElementsByTagName("Currency");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				if (getDouble(getElementValue(element, "ForexBuying")) == 0d 
					|| getDouble(getElementValue(element, "ForexSelling")) == 0d
					|| getDouble(getElementValue(element, "BanknoteBuying")) == 0d
					|| getDouble(getElementValue(element, "BanknoteSelling")) == 0d) continue;

				TCMBRate rate = new TCMBRate();

				if (i == 0) rate.setDate(firstDate);

				rate.setCode(element.getAttribute("Kod"));
				rate.setName(getElementValue(element, "Isim"));
				rate.setExcBuying(getDouble(getElementValue(element, "ForexBuying")));
				rate.setExcSelling(getDouble(getElementValue(element, "ForexSelling")));
				rate.setEffBuying(getDouble(getElementValue(element, "BanknoteBuying")));
				rate.setEffSelling(getDouble(getElementValue(element, "BanknoteSelling")));

				result.add(rate);
			}
			log.info("xml service connection was successfuly done.");

		} catch (Exception e) {
			log.error("ERROR", e);
		}

		return result;
	}

	private static String getCharacterDataFromElement(Element el) {
		if (el == null) return null;
		try {
			Node child = el.getFirstChild();
			if (child instanceof CharacterData) {
				CharacterData cd = (CharacterData) child;
				return cd.getData();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return "";
	}

	private static double getDouble(String value) {
		if (value != null && !value.equals("")) {
			return Double.parseDouble(value);
		}
		return 0;	   
	}

	private static String getElementValue(Element parent, String label) {
		return getCharacterDataFromElement((Element) parent
				.getElementsByTagName(label).item(0));
	}

}
