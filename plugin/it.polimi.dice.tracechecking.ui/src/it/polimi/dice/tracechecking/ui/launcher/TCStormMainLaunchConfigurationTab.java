package it.polimi.dice.tracechecking.ui.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.dice.tracechecking.config.TraceCheckingToolSerializer;
import it.polimi.dice.tracechecking.contentproviders.BoltsContentProvider;
import it.polimi.dice.tracechecking.contentproviders.SpoutsContentProvider;
import it.polimi.dice.tracechecking.core.logger.DiceLogger;
import it.polimi.dice.tracechecking.core.ui.dialogs.DialogUtils;
import it.polimi.dice.tracechecking.core.ui.dialogs.FileSelectionDialog;
import it.polimi.dice.tracechecking.launcher.TraceCheckingLaunchConfigurationAttributes;
import it.polimi.dice.tracechecking.ui.TraceCheckingUI;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.DesignValueEditingSupport;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.MethodEditingSupport;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.ParameterEditingSupport;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.RelationEditingSupport;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.SelectedEditingSupport;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.TimeSubWindowEditingSupport;
import it.polimi.dice.tracechecking.ui.launcher.editingsupports.TimeWindowEditingSupport;
import it.polimi.dice.tracechecking.uml.diagrams.classdiagram.BoltClass;
import it.polimi.dice.tracechecking.uml.diagrams.classdiagram.SpoutClass;
import it.polimi.dice.tracechecking.uml.helpers.UML2ModelHelper;
import it.polimi.dice.tracechecking.uml2json.json.Method;
import it.polimi.dice.tracechecking.uml2json.json.MonitoringStatus;
import it.polimi.dice.tracechecking.uml2json.json.NodeParameter;
import it.polimi.dice.tracechecking.uml2json.json.NodeType;
import it.polimi.dice.tracechecking.uml2json.json.Relation;
import it.polimi.dice.tracechecking.uml2json.json.StormTopology;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class TCStormMainLaunchConfigurationTab extends AbstractLaunchConfigurationTab implements DirtableTab {

	private static final Image CHECKED = TraceCheckingUI.getDefault().getImageRegistry()
			.get(TraceCheckingUI.IMG_CHECKED);;
	private static final Image UNCHECKED = TraceCheckingUI.getDefault().getImageRegistry()
			.get(TraceCheckingUI.IMG_UNCHECKED);

	public class FormData {
		private String inputFile;
		// private boolean keepIntermediateFiles;
		// private List<TopologyNodeFormula> nodesFormulae = new ArrayList<>();
		private List<TopologyNodeFormula> spoutsFormulae = new ArrayList<>();
		private List<TopologyNodeFormula> boltsFormulae = new ArrayList<>();
		private String tcHostAddress;
		private String tcPortNumber;
		private String monitoringHostAddress;
		private String monitoringPortNumber;

		public String getInputFile() {
			return inputFile;
		}

		protected void setInputFile(String inputFile) {
			// DiceLogger.logInfo(TraceCheckingUI.getDefault(), "setInputFile("
			// + inputFile + ")");
			boolean forceReset = inputFile != this.inputFile;
			this.inputFile = inputFile;
			String readableInputFile = toReadableString(inputFile);
			inputFileText.setText(readableInputFile != null ? readableInputFile : StringUtils.EMPTY);
			resetNodesIfNecessary(inputFile, forceReset);
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		private void resetNodesIfNecessary(String inputFile, boolean override) {
			StormTopology topology = getVariablesFromUmlModel(new File(URI.create(inputFile)));
			boolean necessary = false;
			if (boltsFormulae.size() != topology.getBolts().size()
					|| spoutsFormulae.size() != topology.getSpouts().size())
				necessary = true;
			else {
				for (TopologyNodeFormula tnfBolt : boltsFormulae)
					if (!topology.getBoltsNames().contains(tnfBolt.getName()))
						necessary = true;
				for (TopologyNodeFormula tnfSpout : spoutsFormulae)
					if (!topology.getSpoutsNames().contains(tnfSpout.getName()))
						necessary = true;
			}
			if (necessary || override) {
				boltsFormulae.clear();
				spoutsFormulae.clear();
				for (BoltClass b : topology.getBolts()) {
					// populate list of bolts
					boltsFormulae.add(new TopologyNodeFormula(b.getName(), NodeType.BOLT, NodeParameter.SIGMA,
							new Float(0.0), new Float(0.0), new Float(0.0), Method.COUNTING, Relation.LT,
							new Float(b.getSigma())));
				}
				for (SpoutClass s : topology.getSpouts()) {
					// populate list of spouts
					spoutsFormulae.add(new TopologyNodeFormula(s.getName(), NodeType.SPOUT, NodeParameter.AVG_EMIT_RATE,
							new Float(0.0), new Float(0.0), new Float(0.0), Method.COUNTING, Relation.LT,
							new Float(s.getAverageEmitRate())));
				}
			}
			boltsViewer.refresh();
			spoutsViewer.refresh();
		}

		public List<TopologyNodeFormula> getSpoutsFormulae() {
			return spoutsFormulae;
		}

		public List<TopologyNodeFormula> getBoltsFormulae() {
			return boltsFormulae;
		}

		public void setSpoutsFormulae(List<TopologyNodeFormula> spoutsFormulae) {
			this.spoutsFormulae = spoutsFormulae;
			if (spoutsViewer != null) {
				spoutsViewer.setInput(data);
			}
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		public void setBoltsFormulae(List<TopologyNodeFormula> boltsFormulae) {
			this.boltsFormulae = boltsFormulae;
			if (boltsViewer != null) {
				boltsViewer.setInput(data);
			}
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		public void setMonitoringHostAddress(String hostAddress, boolean refresh) {
			this.monitoringHostAddress = hostAddress;
			if (refresh)
				monitoringHostText.setText(hostAddress);
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		public String getMonitoringHostAddress() {
			return monitoringHostAddress;
		}

		public void setMonitoringPortNumber(String portNumber, boolean refresh) {
			this.monitoringPortNumber = portNumber;
			if (refresh)
				monitoringPortText.setText(portNumber);
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		public String getMonitoringPortNumber() {
			return monitoringPortNumber;
		}

		public void setTcHostAddress(String hostAddress, boolean refresh) {
			this.tcHostAddress = hostAddress;
			if (refresh)
				hostText.setText(hostAddress);
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		public String getTcHostAddress() {
			return tcHostAddress;
		}

		public void setTcPortNumber(String portNumber, boolean refresh) {
			this.tcPortNumber = portNumber;
			if (refresh)
				portText.setText(portNumber);
			setDirty(true);
			updateLaunchConfigurationDialog();
		}

		public String getTcPortNumber() {
			return tcPortNumber;
		}

		private String toReadableString(String fileUriString) {
			URI uri = URI.create(fileUriString);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if (new File(uri).isFile()) {
				IFile[] files = root.findFilesForLocationURI(uri);
				if (files.length > 0) {
					return org.eclipse.emf.common.util.URI
							.createPlatformResourceURI(files[0].getFullPath().toString(), false).toString();
				}
			} else {
				IContainer[] containers = root.findContainersForLocationURI(uri);
				if (containers.length > 0) {
					return org.eclipse.emf.common.util.URI
							.createPlatformResourceURI(containers[0].getFullPath().toString(), false).toString();
				}
			}
			return null;
		}

	}

	public void updateMonitoringStatusFields() {
		monitoringStatusText.setText(MessageFormat
				.format(Messages.MainLaunchConfigurationTab_displayMonitoringStatusLabel, monitoringStatus.isAlive()));
		Device device = Display.getCurrent();
		Color red = new Color(device, 255, 0, 0);
		Color green = device.getSystemColor(SWT.COLOR_GREEN);// new
																// Color(device,
																// 0, 255, 0);
		if (monitoringStatus.isAlive())
			monitoringStatusText.setBackground(green);
		else
			monitoringStatusText.setBackground(red);
		activateButton.setEnabled(!monitoringStatus.isAlive());
		deactivateButton.setEnabled(monitoringStatus.isAlive());
	}

	protected Button verifyButton, activateButton, deactivateButton;
	protected Text inputFileText;
	protected Button browseIntermediateFilesDirButton;
	protected TableViewer boltsViewer, spoutsViewer;
	protected TableViewerColumn boltSelectedColumn, boltNameColumn, parameterColumn, methodColumn, timeWindowCloumn,
			timeSubWindowColumn, relationColumn, designValueColumn;
	protected Text hostText, portText;
	protected Text monitoringHostText, monitoringPortText;

	protected TopologyNodeFormulaComparator comparator;
	protected FormData data = new FormData();
	protected Text monitoringStatusText;

	protected MonitoringStatus monitoringStatus = new MonitoringStatus("", false);

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(Composite parent) {

		DiceLogger.logInfo(TraceCheckingUI.getDefault(), "createControl()");
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));

		GridData buttonsGridDataInput = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		buttonsGridDataInput.widthHint = 100;

		{ // Monitoring connection group
			Group group = new Group(comp, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			group.setLayout(new GridLayout(4, true));
			group.setText("Monitoring connection");

			Label hostLabel = new Label(group, SWT.BORDER);
			hostLabel.setText(Messages.MainLaunchConfigurationTab_hostAddressLabel);
			hostLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			monitoringHostText = new Text(group, SWT.BORDER);
			monitoringHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			monitoringHostText.setEditable(true);
			monitoringHostText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					// Get the widget whose text was modified
					Text text = (Text) event.widget;
					data.setMonitoringHostAddress(text.getText(), false);
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			});

			Label portLabel = new Label(group, SWT.BORDER);
			portLabel.setText(Messages.MainLaunchConfigurationTab_portNumberLabel);
			portLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			monitoringPortText = new Text(group, SWT.BORDER);
			monitoringPortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			monitoringPortText.setEditable(true);
			monitoringPortText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					// Get the widget whose text was modified
					Text text = (Text) event.widget;
					data.setMonitoringPortNumber(text.getText(), false);
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			});

			// String statusURL =
			// "http://109.231.122.169:5001/dmon/v1/overlord/storm/logs/active";
			verifyButton = new Button(group, SWT.NONE);
			verifyButton.setText("Check");
			GridData checkButtonLayout = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			checkButtonLayout.widthHint = 150;
			verifyButton.setLayoutData(checkButtonLayout);
			verifyButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String statusURL = "http://" + data.getMonitoringHostAddress()
							.replaceFirst("^(http(?>s)://www\\.|http(?>s)://|www\\.|http(?s)://)", "") + ":"
							+ data.getMonitoringPortNumber() + "/dmon/v1/overlord/storm/logs/active";
					try {
						URL url = new URL(statusURL);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Accept", "application/json");
						conn.setReadTimeout(3000);

						if (conn.getResponseCode() != 200) {
							throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
						}

						BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

						String outputLine;
						String responseString = "";
						System.out.println("Output from Server .... \n");
						while ((outputLine = br.readLine()) != null) {
							responseString += outputLine;
							System.out.println(outputLine);
						}
						Gson gson = new GsonBuilder().create();
						monitoringStatus = gson.fromJson(responseString, MonitoringStatus.class);
						updateMonitoringStatusFields();
						updateLaunchConfigurationDialog();
					} catch (MalformedURLException e0) {
						monitoringStatus.setAlive(false);
						updateMonitoringStatusFields();
						DialogUtils.getWarningDialog(e0, "Malformed URL",
								"Please verify that the specified URL is correct (" + statusURL + ")");
					} catch (IOException e1) {
						monitoringStatus.setAlive(false);
						updateMonitoringStatusFields();
						DialogUtils.getWarningDialog(e1, "Connection Error",
								"Please verify that the URL is reachable (" + statusURL + ")");
					}
				}

			});

			monitoringStatusText = new Text(group, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
			monitoringStatusText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			monitoringStatusText.setEnabled(false);

			activateButton = new Button(group, SWT.NONE);
			activateButton.setText("Activate");
			activateButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			activateButton.setEnabled(false);

			deactivateButton = new Button(group, SWT.NONE);
			deactivateButton.setText("Deactivate");
			deactivateButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			deactivateButton.setEnabled(false);

			// updateMonitoringStatusFields();
		}

		{ // Model Group
			Group group = new Group(comp, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			group.setLayout(new GridLayout(2, false));
			group.setText(Messages.MainLaunchConfigurationTab_modelLabel);

			inputFileText = new Text(group, SWT.BORDER);
			inputFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			inputFileText.setEditable(false);

			Button fileButton = new Button(group, SWT.NONE);
			fileButton.setText(Messages.MainLaunchConfigurationTab_browsLabel);
			fileButton.setLayoutData(buttonsGridDataInput);

			fileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileSelectionDialog dialog = new FileSelectionDialog(getShell());
					dialog.setValidator(dialog.new IsFileStatusValidator() {
						@Override
						public IStatus validate(Object[] selection) {
							IStatus superResult = super.validate(selection);
							if (!superResult.isOK()) {
								return superResult;
							} else {
								IFile file = (IFile) selection[0];
								if (isUmlModel(new File(file.getLocationURI()))) {
									return new Status(IStatus.OK, TraceCheckingUI.PLUGIN_ID, StringUtils.EMPTY);
								} else {
									return new Status(IStatus.ERROR, TraceCheckingUI.PLUGIN_ID,
											Messages.MainLaunchConfigurationTab_invalidUmlFileError);
								}
							}
						}
					});
					if (data.getInputFile() != null) {
						IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
								.findFilesForLocationURI(URI.create(data.getInputFile()));
						dialog.setInitialSelection(files);
					}
					if (dialog.open() == Dialog.OK) {
						data.setInputFile(dialog.getFile().getLocationURI().toString());
					}
				}
			});
		}

		{ // Configuration Group - Set Monitored Bolts
			Group group = new Group(comp, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			group.setLayout(new GridLayout(1, false));
			group.setText(Messages.MainLaunchConfigurationTab_monitoredBoltsLabel);

			Composite tableComposite = new Composite(group, SWT.NONE);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			boltsViewer = new TableViewer(tableComposite,
					SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			boltsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			boltsViewer.getTable().setLinesVisible(true);
			boltsViewer.getTable().setHeaderVisible(true);
			String[] headers = { "", "Bolt Name", "Parameter", "Method", "Window", "SubWindow", "Relation", "Value" };
			createColumns(tableComposite, boltsViewer, headers);

			boltsViewer.setContentProvider(new BoltsContentProvider());

			boltsViewer.setInput(data);

			comparator = new TopologyNodeFormulaComparator();

			boltsViewer.setComparator(comparator);

		}

		{ // Configuration Group - Set Monitored Spouts
			Group group = new Group(comp, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			group.setLayout(new GridLayout(1, false));
			group.setText(Messages.MainLaunchConfigurationTab_monitoredSpoutsLabel);

			Composite tableComposite = new Composite(group, SWT.NONE);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			spoutsViewer = new TableViewer(tableComposite,
					SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			spoutsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			spoutsViewer.getTable().setLinesVisible(true);
			spoutsViewer.getTable().setHeaderVisible(true);
			String[] headers = { "", "Spout Name", "Parameter", "Method", "Window", "SubWindow", "Relation", "Value" };
			createColumns(tableComposite, spoutsViewer, headers);

			spoutsViewer.setContentProvider(new SpoutsContentProvider());

			spoutsViewer.setInput(data);

			// comparator = new TopologyNodeFormulaComparator();

			spoutsViewer.setComparator(comparator);

		}

		{ // Connection group
			Group group = new Group(comp, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			group.setLayout(new GridLayout(4, false));
			group.setText(Messages.MainLaunchConfigurationTab_connectionLabel);

			Label hostLabel = new Label(group, SWT.BORDER);
			hostLabel.setText(Messages.MainLaunchConfigurationTab_hostAddressLabel);
			hostLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			hostText = new Text(group, SWT.BORDER);
			hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			hostText.setEditable(true);
			hostText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					// Get the widget whose text was modified
					Text text = (Text) event.widget;
					data.setTcHostAddress(text.getText(), false);
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			});

			Label portLabel = new Label(group, SWT.BORDER);
			portLabel.setText(Messages.MainLaunchConfigurationTab_portNumberLabel);
			portLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			portText = new Text(group, SWT.BORDER);
			portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			portText.setEditable(true);
			portText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					// Get the widget whose text was modified
					Text text = (Text) event.widget;
					data.setTcPortNumber(text.getText(), false);
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			});

		}

		setControl(comp);

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// DiceLogger.logInfo(TraceCheckingUI.getDefault(), "setDefaults()");
		configuration.removeAttribute(TraceCheckingLaunchConfigurationAttributes.INPUT_FILE);
		configuration.removeAttribute(TraceCheckingLaunchConfigurationAttributes.BOLTS_FORMULAE);
		configuration.removeAttribute(TraceCheckingLaunchConfigurationAttributes.SPOUTS_FORMULAE);
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.TC_HOST_ADDRESS,
				TraceCheckingUI.getDefault().getPreferenceStore()
						.getString(it.polimi.dice.tracechecking.ui.preferences.PreferenceConstants.TC_HOST.getName()));
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.TC_PORT_NUMBER,
				TraceCheckingUI.getDefault().getPreferenceStore()
						.getString(it.polimi.dice.tracechecking.ui.preferences.PreferenceConstants.TC_PORT.getName()));
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_HOST_ADDRESS,
				TraceCheckingUI.getDefault().getPreferenceStore().getString(
						it.polimi.dice.tracechecking.ui.preferences.PreferenceConstants.MONITORING_HOST.getName()));
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_PORT_NUMBER,
				TraceCheckingUI.getDefault().getPreferenceStore().getString(
						it.polimi.dice.tracechecking.ui.preferences.PreferenceConstants.MONITORING_PORT.getName()));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// DiceLogger.logInfo(TraceCheckingUI.getDefault(), "initializeFrom()");
		try {
			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.INPUT_FILE)) {
				data.setInputFile(configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.INPUT_FILE,
						StringUtils.EMPTY));
			}
			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.BOLTS_FORMULAE)) {
				String serializedBoltsFormulae = configuration
						.getAttribute(TraceCheckingLaunchConfigurationAttributes.BOLTS_FORMULAE, StringUtils.EMPTY);
				try {
					data.setBoltsFormulae(TraceCheckingToolSerializer.deserialize(serializedBoltsFormulae));
				} catch (IOException e) {
					DiceLogger.logException(TraceCheckingUI.getDefault(), MessageFormat
							.format(Messages.MainLaunchConfigurationTab_unableParserError, serializedBoltsFormulae), e);
				}
			}

			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.SPOUTS_FORMULAE)) {
				String serializedSpoutsFormulae = configuration
						.getAttribute(TraceCheckingLaunchConfigurationAttributes.SPOUTS_FORMULAE, StringUtils.EMPTY);
				try {
					data.setSpoutsFormulae(TraceCheckingToolSerializer.deserialize(serializedSpoutsFormulae));
				} catch (IOException e) {
					DiceLogger.logException(TraceCheckingUI.getDefault(), MessageFormat.format(
							Messages.MainLaunchConfigurationTab_unableParserError, serializedSpoutsFormulae), e);
				}
			}

			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.TC_HOST_ADDRESS)) {
				data.setTcHostAddress(
						configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.TC_HOST_ADDRESS,
								TraceCheckingLaunchConfigurationAttributes.DEFAULT_TC_HOST_ADDRESS),
						true);
			}
			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.TC_PORT_NUMBER)) {
				data.setTcPortNumber(
						configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.TC_PORT_NUMBER,
								TraceCheckingLaunchConfigurationAttributes.DEFAULT_TC_PORT_NUMBER),
						true);
			}
			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_HOST_ADDRESS)) {
				data.setMonitoringHostAddress(
						configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_HOST_ADDRESS,
								TraceCheckingLaunchConfigurationAttributes.DEFAULT_MONITORING_HOST_ADDRESS),
						true);
			}
			if (configuration.hasAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_PORT_NUMBER)) {
				data.setMonitoringPortNumber(
						configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_PORT_NUMBER,
								TraceCheckingLaunchConfigurationAttributes.DEFAULT_MONITORING_PORT_NUMBER),
						true);
			}
		} catch (CoreException e) {
			DiceLogger.logException(TraceCheckingUI.getDefault(), e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// DiceLogger.logInfo(TraceCheckingUI.getDefault(), "performApply(" +
		// data.getInputFile() + ")");
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.INPUT_FILE, data.getInputFile());
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.BOLTS_FORMULAE,
				TraceCheckingToolSerializer.serialize(data.getBoltsFormulae()));
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.SPOUTS_FORMULAE,
				TraceCheckingToolSerializer.serialize(data.getSpoutsFormulae()));
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.TC_HOST_ADDRESS, data.getTcHostAddress());
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.TC_PORT_NUMBER, data.getTcPortNumber());
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_HOST_ADDRESS,
				data.getMonitoringHostAddress());
		configuration.setAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_PORT_NUMBER,
				data.getMonitoringPortNumber());
	}

	@Override
	public String getName() {
		return Messages.MainLaunchConfigurationTab_mainTabTitle;
	}

	protected boolean isUmlModel(File file) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = null;
		try {
			resource = resourceSet.getResource(org.eclipse.emf.common.util.URI.createFileURI(file.getAbsolutePath()),
					true);
			EObject eObject = resource.getContents().get(0);
			if (UMLPackage.eINSTANCE.getNsURI().equals(eObject.eClass().getEPackage().getNsURI())) {
				return true;
			} else {
				return false;
			}
		} catch (Throwable t) {
			// Unable to get the first root element
			// The file is not a valid EMF resource
		}
		return false;
	}

	protected StormTopology getVariablesFromUmlModel(File file) {
		Set<String> vars = new HashSet<>();
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = null;
		StormTopology topology = new StormTopology();
		List<SpoutClass> spouts = new ArrayList<>();
		List<BoltClass> bolts = new ArrayList<>();
		try {
			resource = resourceSet.getResource(org.eclipse.emf.common.util.URI.createFileURI(file.getAbsolutePath()),
					true);
			for (Iterator<EObject> it = resource.getAllContents(); it.hasNext();) {
				EObject eObject = it.next();
				if (eObject instanceof org.eclipse.uml2.uml.Class) {
					Element element = (Element) eObject;
					if (UML2ModelHelper.isSpout(element)) {
						SpoutClass sc = new SpoutClass((org.eclipse.uml2.uml.Class) eObject);
						spouts.add(sc);
					} else if (UML2ModelHelper.isBolt(element)) {
						BoltClass bc = new BoltClass((org.eclipse.uml2.uml.Class) eObject);
						vars.add(bc.getId());
						bolts.add(bc);
					}
					topology.setBolts(bolts);
					topology.setSpouts(spouts);
				}
			}

		} catch (Throwable t) {
			DiceLogger.logError(TraceCheckingUI.getDefault(), t);
		}
		return topology;
	}

	// create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer, String[] titles) {

		int[] bounds = { 20, 10, 5, 10, 10, 10, 10, 20 };

		// boltSelectedColumn
		TableViewerColumn boltSelectedColumn = createTableViewerColumn(viewer, titles[0], bounds[0], 0);
		boltSelectedColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (((TopologyNodeFormula) element).isSelected()) {
					return CHECKED;
				} else {
					return UNCHECKED;
				}
			}
		});
		boltSelectedColumn.setEditingSupport(new SelectedEditingSupport(viewer, getShell(), this));
		// boltName column
		TableViewerColumn boltNameColumn = createTableViewerColumn(viewer, titles[1], bounds[1], 1);
		boltNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return tnf.getName();
			}
		});

		// parameter column
		TableViewerColumn parameterColumn = createTableViewerColumn(viewer, titles[2], bounds[2], 2);
		parameterColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return tnf.getParameter().getParameterName();
			}
		});
		parameterColumn.setEditingSupport(new ParameterEditingSupport(viewer, this));
		// method column
		TableViewerColumn methodColumn = createTableViewerColumn(viewer, titles[3], bounds[3], 3);
		methodColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return tnf.getMethod().getMethodName();
			}
		});
		methodColumn.setEditingSupport(new MethodEditingSupport(viewer, this));

		// Time window column
		TableViewerColumn timeWindowCloumn = createTableViewerColumn(viewer, titles[4], bounds[4], 4);
		timeWindowCloumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return tnf.getTimeWindow().toString();
			}
		});
		timeWindowCloumn.setEditingSupport(
				new TimeWindowEditingSupport(viewer, /* getShell(), */ this));

		// Sub Time Window column
		TableViewerColumn timeSubWindowColumn = createTableViewerColumn(viewer, titles[5], bounds[5], 5);
		timeSubWindowColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return tnf.getTimeSubWindow().toString();
			}
		});
		timeSubWindowColumn.setEditingSupport(new TimeSubWindowEditingSupport(viewer, this));

		// relation column
		TableViewerColumn relationColumn = createTableViewerColumn(viewer, titles[6], bounds[6], 6);
		relationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return tnf.getRelation().getStringRep();
			}
		});
		relationColumn.setEditingSupport(new RelationEditingSupport(viewer, this));

		// designValue column
		TableViewerColumn designValueColumn = createTableViewerColumn(viewer, titles[titles.length - 1],
				bounds[titles.length - 1], titles.length - 1);
		designValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TopologyNodeFormula tnf = (TopologyNodeFormula) element;
				return Double.toString(tnf.getDesignValue());
			}
		});
		designValueColumn.setEditingSupport(new DesignValueEditingSupport(viewer, this));

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(boltSelectedColumn.getColumn(), new ColumnWeightData(1));
		tableLayout.setColumnData(boltNameColumn.getColumn(), new ColumnWeightData(5));
		tableLayout.setColumnData(parameterColumn.getColumn(), new ColumnWeightData(4));
		tableLayout.setColumnData(methodColumn.getColumn(), new ColumnWeightData(4));
		tableLayout.setColumnData(timeWindowCloumn.getColumn(), new ColumnWeightData(2));
		tableLayout.setColumnData(timeSubWindowColumn.getColumn(), new ColumnWeightData(2));
		tableLayout.setColumnData(relationColumn.getColumn(), new ColumnWeightData(2));
		tableLayout.setColumnData(designValueColumn.getColumn(), new ColumnWeightData(2));
		parent.setLayout(tableLayout);

	}

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(viewer, column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(TableViewer viewer, final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	@Override
	public Shell getTabShell() {
		return getShell();
	}

	public void setTabDirty(boolean dirty) {
		setDirty(dirty);
		updateLaunchConfigurationDialog();
	}

}
