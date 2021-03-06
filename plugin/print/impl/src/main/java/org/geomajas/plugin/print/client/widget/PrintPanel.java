/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2014 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.plugin.print.client.widget;

import org.geomajas.gwt.client.command.AbstractCommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt2.client.map.MapPresenter;
import org.geomajas.plugin.print.client.i18n.PrintMessages;
import org.geomajas.plugin.print.client.template.DefaultTemplateBuilder;
import org.geomajas.plugin.print.client.template.PageSize;
import org.geomajas.plugin.print.client.template.PrintableLayerBuilder;
import org.geomajas.plugin.print.client.template.PrintableMapBuilder;
import org.geomajas.plugin.print.client.util.PrintLayout;
import org.geomajas.plugin.print.client.util.UrlBuilder;
import org.geomajas.plugin.print.command.dto.PrintGetTemplateRequest;
import org.geomajas.plugin.print.command.dto.PrintGetTemplateResponse;
import org.geomajas.plugin.print.command.dto.PrintTemplateInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for choosing print preferences and printing.
 * 
 * @author An Buyle
 * @author Jan De Moerloose
 */
public class PrintPanel extends Composite {

	/**
	 * UI binder definition for the {@link } widget.
	 * 
	 * @author An Buyle
	 */
	interface PrintPanelUiBinder extends UiBinder<Widget, PrintPanel> {
	}

	private final PrintPanelResource resource;

	private final PrintPanelUiBinder uiBinder;

	private static final PrintMessages MESSAGES = GWT.create(PrintMessages.class);

	private static final String SAVE = "save";

	private static final String OPEN = "open";

	// private static final String EXTENSION = ".pdf";
	private static final String URL_PATH = "d/print";

	private static final String URL_DOCUMENT_ID = "documentId";

	private static final String URL_NAME = "name";

	private static final String URL_TOKEN = "userToken";

	private static final String URL_DOWNLOAD = "download";

	private static final String URL_DOWNLOAD_YES = "1";

	private static final String URL_DOWNLOAD_NO = "0";

	@UiField
	protected Button printButton;

	@UiField
	protected TextBox titleTextBox;

	//
	// private TextItem fileNameItem;
	//
	// private SelectItem sizeItem;
	//
	// private RadioGroupItem orientationGroup;
	@UiField
	protected RadioButton optionLandscapeOrientation;

	@UiField
	protected RadioButton optionPortraitOrientation;

	private MapPresenter mapPresenter;

	private String applicationId;

	private PrintableMapBuilder mapBuilder = new PrintableMapBuilder();

	/** Default constructor. Create an instance using the default resource bundle and layout. */
	public PrintPanel(MapPresenter mapPresenter, String applicationId) {
		this((PrintPanelResource) GWT.create(PrintPanelResource.class), mapPresenter, applicationId);
	}

	/**
	 * Create a new instance using a custom resource bundle.
	 * 
	 * @param resource The custom resource bundle to use.
	 */
	public PrintPanel(PrintPanelResource resource, MapPresenter mapPresenter, String applicationId) {
		this(resource, (PrintPanelUiBinder) GWT.create(PrintPanelUiBinder.class), mapPresenter, applicationId);
	}

	/**
	 * Create a new instance using a custom resource bundle and UiBinder construct.
	 * 
	 * @param resource The ustom resource bundle to use.
	 * @param uiBinder The custom UiBinder construct.
	 */
	public PrintPanel(PrintPanelResource resource, PrintPanelUiBinder uiBinder, MapPresenter mapPresenter,
			String applicationId) {
		this.resource = resource;
		this.uiBinder = uiBinder;

		// Inject the CSS and create the GUI:
		this.resource.css().ensureInjected();
		this.uiBinder.createAndBindUi(this);

		initWidget(uiBinder.createAndBindUi(this));

		this.mapPresenter = mapPresenter;
		this.applicationId = applicationId;
		printButton.setEnabled(true);

		titleTextBox.setTitle(MESSAGES.printPrefsTitleTooltip());
		titleTextBox.getElement().setAttribute("placeholder", MESSAGES.printPrefsTitlePlaceholder());

		final ClickHandler orientationOptionClickedHandler = new ClickHandler() {

			public void onClick(ClickEvent event) {
				if (event != null) {
					optionLandscapeOrientation.setValue(event.getSource().equals(optionLandscapeOrientation));
					optionPortraitOrientation.setValue(event.getSource().equals(optionPortraitOrientation));
				}
			}
		};
		optionLandscapeOrientation.addClickHandler(orientationOptionClickedHandler);
		optionPortraitOrientation.addClickHandler(orientationOptionClickedHandler);

		// Defayult = Landscape
		optionLandscapeOrientation.setValue(true);
		optionPortraitOrientation.setValue(false);
	}

	public void registerLayerBuilder(PrintableLayerBuilder layerBuilder) {
		mapBuilder.registerLayerBuilder(layerBuilder);
	}

	@UiHandler("printButton")
	public void onClick(ClickEvent event) {
		print();
	}

	public PrintableMapBuilder getMapBuilder() {
		return mapBuilder;
	}

	public void print() {
		if (this.mapPresenter == null) {
			return; // ABORT
		}
		PrintGetTemplateRequest request = new PrintGetTemplateRequest();

		DefaultTemplateBuilder builder = new DefaultTemplateBuilder(mapBuilder);

		builder.setApplicationId(this.applicationId);
		builder.setMapPresenter(mapPresenter);
		builder.setMarginX((int) PrintLayout.templateMarginX);
		builder.setMarginY((int) PrintLayout.templateMarginY);
		PageSize size = PageSize.A4;

		if (optionLandscapeOrientation.getValue()) {
			builder.setPageHeight(size.getWidth());
			builder.setPageWidth(size.getHeight());
		} else {
			builder.setPageHeight(size.getHeight());
			builder.setPageWidth(size.getWidth());
		}
		String title = titleTextBox.getText().trim();
		if (title.length() == 0) {
			title = MESSAGES.defaultPrintTitle();
		}
		builder.setTitleText(title);
		// TODO: builder.setWithArrow((Boolean) arrowCheckbox.getValue());
		builder.setWithArrow(true);
		// TODO: builder.setWithScaleBar((Boolean) scaleBarCheckbox.getValue());
		builder.setWithScaleBar(true);
		// builder.setRasterDpi((Integer) rasterDpiSlider.getValue());
		builder.setRasterDpi(200);
		PrintTemplateInfo template = builder.buildTemplate();
		request.setTemplate(template);
		final GwtCommand command = new GwtCommand(PrintGetTemplateRequest.COMMAND);
		command.setCommandRequest(request);
		GwtCommandDispatcher.getInstance().execute(command, new AbstractCommandCallback<PrintGetTemplateResponse>() {

			public void execute(PrintGetTemplateResponse response) {
				UrlBuilder url = new UrlBuilder(GWT.getHostPageBaseURL());
				url.addPath(URL_PATH);
				url.addParameter(URL_DOCUMENT_ID, response.getDocumentId());
				// url.addParameter(URL_NAME, (String) fileNameItem.getValue());
				url.addParameter(URL_NAME, "mapPrint.pdf");
				url.addParameter(URL_TOKEN, command.getUserToken());
				// TODO String downloadType = downloadTypeGroup.getValue()
				String downloadType = OPEN;
				if (SAVE.equals(downloadType)) {
					url.addParameter(URL_DOWNLOAD, URL_DOWNLOAD_YES);
					// TODO Converted to pureGWT
					// String encodedUrl = url.toString();
					// // create a hidden iframe to avoid popups ???
					// HTMLPanel hiddenFrame = new HTMLPanel("<iframe src='" + encodedUrl
					// + "'+style='position:absolute;width:0;height:0;border:0'>");
					// hiddenFrame.setVisible(false);
					//
					// addChild(hiddenFrame);
				} else {
					url.addParameter(URL_DOWNLOAD, URL_DOWNLOAD_NO);
					String encodedUrl = url.toString();
					com.google.gwt.user.client.Window.open(encodedUrl, "_blank", null);
				}
			}
		});
	}

}
