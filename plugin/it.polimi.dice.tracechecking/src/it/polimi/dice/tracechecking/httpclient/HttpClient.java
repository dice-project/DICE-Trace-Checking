package it.polimi.dice.tracechecking.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class HttpClient {

	private URL serverEndpoint;
	private URL taskLocation;
//	private VerificationTask taskStatus;

	public HttpClient(){
		
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

/*	public VerificationTask getTaskStatus() {
		return taskStatus;
	}
*/
/*	public void setTaskStatus(VerificationTask taskStatus) {
		this.taskStatus = taskStatus;
	}
*/
	public boolean postJSONRequest(String urlString, String request) {

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
			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED
					&& conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

//			CHECK TC-SERVICE			
//			String location = conn.getHeaderField("location");
//			System.out.println("Location: \n" + location);
//			this.setTaskLocation(new URL(location));

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
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
	        		dialog.setMessage(e.getMessage()+"!!\n Please verify that the url is reachable ("+ urlString +")");

	        		// open dialog and await user selection
	        		dialog.open(); 

			    }
			});
			
			return false;
		}
		return true; 

	}

	public void getTaskStatusUpdatesFromServer() {

		try {

			URL url = this.getTaskLocation();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

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
			
			
			// getting task status from json response
			Gson gson = new GsonBuilder().create();
//			this.setTaskStatus(gson.fromJson(responseString, VerificationTask.class));
						
			
			
			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (NullPointerException e){
			e.printStackTrace();
		};

	}

	// http://localhost:8080/RESTfulExample/json/product/post
	public static void main(String[] args) {
		String myUrl = "http://localhost:5000/longtasks";
		String jsonRequest = "{\"title\":\"pinellaxJAVA\",\"json_context\":{\"verification_params\": {\"base_quantity\": 10, \"periodic_queues\": [\"expander\"], \"num_steps\": 20, \"max_time\": 20000, \"plugin\": [\"ae2bvzot\", \"ae2sbvzot\"]}, \"version\": \"0.1\", \"app_name\": \"SIMPLIFIED FOCUSED CRAWLER\", \"topology\": {\"bolts\": [{\"d\": 0.0, \"parallelism\": 4, \"min_ttf\": 1000, \"alpha\": 0.5, \"sigma\": 2.0, \"id\": \"WpDeserializer\", \"subs\": [\"wpSpout\"]}, {\"d\": 0.0, \"parallelism\": 8, \"min_ttf\": 1000, \"alpha\": 3.0, \"sigma\": 0.75, \"id\": \"expander\", \"subs\": [\"WpDeserializer\"]}, {\"d\": 0.0, \"parallelism\": 1, \"min_ttf\": 1000, \"alpha\": 1.0, \"sigma\": 1.0, \"id\": \"articleExtraction\", \"subs\": [\"expander\"]}, {\"d\": 0.0, \"parallelism\": 1, \"min_ttf\": 1000, \"alpha\": 1.0, \"sigma\": 1.0, \"id\": \"mediaExtraction\", \"subs\": [\"expander\"]}], \"init_queues\": 4, \"max_reboot_time\": 100, \"max_idle_time\": 1.0, \"min_reboot_time\": 10, \"spouts\": [{\"avg_emit_rate\": 4.0, \"id\": \"wpSpout\"}], \"queue_threshold\": 0}, \"description\": \"\"}}";
		HttpClient nc = new HttpClient();
		nc.postJSONRequest(myUrl, jsonRequest);
		try {
		    Thread.sleep(5000);                 
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		nc.getTaskStatusUpdatesFromServer();
		
	}

}