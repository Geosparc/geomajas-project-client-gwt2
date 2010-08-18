/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geomajas.plugin.printing.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.geomajas.plugin.printing.PrintingException;
import org.geomajas.plugin.printing.component.impl.PageComponentImpl;
import org.geomajas.plugin.printing.component.service.PrintConfigurationService;
import org.geomajas.plugin.printing.configuration.PrintTemplate;
import org.geomajas.plugin.printing.configuration.PrintTemplateDao;
import org.geomajas.plugin.printing.document.Document;
import org.geomajas.plugin.printing.document.SinglePageDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Print service implementation based on iText. Persistence is configured through a DAO implementation of choice.
 * 
 * @author Jan De Moerloose
 * 
 */
@Component
@Transactional
public class PrintServiceImpl implements PrintService {

	private Logger log = LoggerFactory.getLogger(PrintServiceImpl.class);

	@Autowired
	private PrintTemplateDao printTemplateDao;

	@Autowired
	private PrintConfigurationService configurationService;

	@Autowired
	@Qualifier("printMarshaller")
	private Marshaller marshaller;

	@Autowired
	@Qualifier("printMarshaller")
	private Unmarshaller unMarshaller;

	private Map<String, Document> documentMap = Collections.synchronizedMap(new HashMap<String, Document>());

	public List<PrintTemplate> getAllTemplates() throws PrintingException {
		List<PrintTemplate> allTemplates = new ArrayList<PrintTemplate>();
		if (printTemplateDao != null) {
			try {
				allTemplates = printTemplateDao.findAll();
			} catch (IOException e) {
				log.warn("could not access templates in dao", e);
			}
		}
		allTemplates.addAll(getDefaults());
		List<PrintTemplate> badTemplates = new ArrayList<PrintTemplate>();
		for (PrintTemplate printTemplate : allTemplates) {
			if (printTemplate.getPage() == null) {
				try {
					Object o = unMarshaller.unmarshal(new StreamSource(new StringReader(printTemplate.getPageXml())));
					printTemplate.setPage((PageComponentImpl) o);
				} catch (Exception e) {
					badTemplates.add(printTemplate);
				}
			}
		}
		allTemplates.removeAll(badTemplates);
		Collections.sort(allTemplates, new Comparator<PrintTemplate>() {

			public int compare(PrintTemplate p1, PrintTemplate p2) {
				return p1.getName().compareTo(p2.getName());
			}
		});
		return allTemplates;
	}

	public void saveOrUpdateTemplate(PrintTemplate template) throws PrintingException {
		StringWriter sw = new StringWriter();
		try {
			marshaller.marshal(template.getPage(), new StreamResult(sw));
			template.setPageXml(sw.toString());
			printTemplateDao.merge(template);
		} catch (XmlMappingException e) {
			throw new PrintingException(PrintingException.PRINT_TEMPLATE_XML_PROBLEM);
		} catch (IOException e) {
			throw new PrintingException(PrintingException.PRINT_TEMPLATE_PERSIST_PROBLEM);
		}
	}

	/**
	 * Puts a new document in the service. The generate key is globally unique.
	 * 
	 * @param document
	 * @return key unique key to reference the document
	 */
	public String putDocument(Document document) {
		String key = UUID.randomUUID().toString();
		documentMap.put(key, document);
		return key;
	}

	/**
	 * Gets a document from the service.
	 * 
	 * @param key
	 *            unique key to reference the document
	 * @return the document or null if no such document
	 */
	public Document removeDocument(String key) throws PrintingException {
		if (documentMap.containsKey(key)) {
			return documentMap.remove(key);
		} else {
			throw new PrintingException(PrintingException.DOCUMENT_NOT_FOUND, key);
		}
	}

	private List<PrintTemplate> getDefaults() {
		List<PrintTemplate> allTemplates = new ArrayList<PrintTemplate>();
		allTemplates.add(createDefault("A4", true));
		allTemplates.add(createDefault("A3", true));
		allTemplates.add(createDefault("A2", true));
		allTemplates.add(createDefault("A0", true));
		allTemplates.add(createDefault("A1", true));
		allTemplates.add(createDefault("A4", false));
		allTemplates.add(createDefault("A3", false));
		allTemplates.add(createDefault("A2", false));
		allTemplates.add(createDefault("A1", false));
		allTemplates.add(createDefault("A0", false));
		return allTemplates;
	}

	private PrintTemplate createDefault(String pagesize, boolean landscape) {
		PrintTemplate template = PrintTemplate.createDefaultTemplate(pagesize, landscape, configurationService);
		// calculate the sizes (if not already calculated !)
		SinglePageDocument document = new SinglePageDocument(template.getPage(), null);
		try {
			document.layout();
		} catch (IOException e) {
			// should not happen !
			log.warn("Unexpected problem while laying out default print template", e);
		}
		return template;
	}

}
