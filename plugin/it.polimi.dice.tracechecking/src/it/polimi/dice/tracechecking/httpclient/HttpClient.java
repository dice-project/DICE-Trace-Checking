package it.polimi.dice.tracechecking.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import it.polimi.dice.tracechecking.TraceCheckingPlugin;
import it.polimi.dice.tracechecking.core.logger.DiceLogger;
import it.polimi.dice.tracechecking.core.ui.dialogs.DialogUtils;
import it.polimi.dice.tracechecking.uml2json.json.StormTCOutcome;

public class HttpClient {

	private URL serverEndpoint;
	private URL taskLocation;

	public HttpClient() {

	}

	public URL getServerEndpoint() {
		return serverEndpoint;
	}

	public void setServerEndpoint(URL serverEndpoint) {
		this.serverEndpoint = serverEndpoint;
	}

	public URL getTaskLocation() {
		return taskLocation;
	}

	public void setTaskLocation(URL taskLocation) {
		this.taskLocation = taskLocation;
	}

	public boolean postJSONRequest(String urlString, String request) {
		boolean success = false;
		try {

			URL url = new URL(urlString);
			this.setServerEndpoint(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(request.getBytes());
			os.flush();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				DialogUtils.getWarningDialog(null, "Connection Error - HTTP response code: "+conn.getResponseCode(), conn.getResponseMessage());
			}else{

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String outputLine;
				String responseString = "";
				System.out.println("Output from Server .... \n");
				while ((outputLine = br.readLine()) != null) {
					responseString += outputLine;
					System.out.println(outputLine);
				}
				

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(responseString);
				Type listType = new TypeToken<ArrayList<StormTCOutcome>>(){}.getType();
				List<StormTCOutcome> responseList = gson.fromJson(je, listType);
				String dialogString = "";
				int i = 1;
				for (StormTCOutcome tcOutcome : responseList) {
					dialogString += "Property " + i + ":\n" + 
							"\t"+tcOutcome.getProperty().getMethod()+"("+tcOutcome.getProperty().getParameter()+")" + "[0,"+tcOutcome.getProperty().getTimeWindow()+"] " +  
							tcOutcome.getProperty().getRelation().getStringRep() + " "+tcOutcome.getProperty().getDesignValue() +
							"\n" +
							"\tResult: " + tcOutcome.isResult() + " (Metric value: "+tcOutcome.getMetricValue()+")\n\n";
					i++;
				}
				
				DialogUtils.getWarningDialog(null, "TraceCheckingOutput", dialogString);
				success = true;
			}

			conn.disconnect();

			
		} catch (MalformedURLException e) {

			e.printStackTrace();
			return false;

		} catch (IOException e) {

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageBox dialog = new MessageBox(activeShell, SWT.ICON_WARNING | SWT.OK);
					dialog.setText(e.getMessage());
					dialog.setMessage(
							e.getMessage() + "!!\n Please verify that the url is reachable (" + urlString + ")");

					// open dialog and wait user selection
					dialog.open();

				}
			});

			return false;
		}
		return success;

	}

	
	private static void openUrl(URL url, String browserId) {
	    IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
	    IWebBrowser browser;
	    try {
	        browser = support.createBrowser(TraceCheckingPlugin.PLUGIN_ID + "_" + browserId);
	        browser.openURL(url);
	    } catch (PartInitException e) {
	        DiceLogger.logException(TraceCheckingPlugin.getDefault(), e);
	    }
	}
	 
	
	public static void openNewBrowserTab(URL url, String browserId){
		Display.getDefault().syncExec(new Runnable() { 
			@Override
			public void run() { 
					openUrl(url, browserId);
				} 
			}); 
	}
	
}