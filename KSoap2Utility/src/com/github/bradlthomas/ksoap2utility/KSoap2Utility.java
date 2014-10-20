package com.github.bradlthomas.ksoap2utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.util.Log;

public class KSoap2Utility {
	private String serverNamespace = "";
	private String serverAddress = "";
	private ArrayList<KSoap2ErrorDetail> errors = new ArrayList<KSoap2ErrorDetail>();
	private KSoap2LoggingLevel loggingLevel = KSoap2LoggingLevel.SILENT;
	private final String logTag = "KSOAP2Utility";
	
//<-- Begin Constructors
	/* KSoapUtility construct(serverNamespace, serverAddress, loggingLevel)
	 * Description: Class constructor including loggingLevel setting
	 * Parameters:
	 * 		> serverNamespace: namespace used by the ASP.NET web services
	 * 		> serverAddress: URI location of web service host
	 * 		> loggingLevel: the amount of logging desired
	 * Output: n/a
	 * Change log:
	 *      > 1.0: added to this utility
	 */
	public KSoap2Utility(String serverNamespace, String serverAddress, KSoap2LoggingLevel loggingLevel) {
		this.serverNamespace = serverNamespace;
		this.serverAddress = serverAddress;
		this.loggingLevel = loggingLevel;
	}
	
	/* KSoapUtility construct(serverNamespace, serverAddress)
	 * Description: Class constructor using default logging level (none)
	 * Parameters:
	 * 		> serverNamespace: namespace used by the ASP.NET web services
	 * 		> serverAddress: URI location of web service host
	 * Output: n/a
	 * Change log:
	 *      > 1.0: added to this utility
	 */
	public KSoap2Utility(String serverNamespace, String serverAddress) {
		this.serverNamespace = serverNamespace;
		this.serverAddress = serverAddress;
	}
// End Constructors -->

	
//<-- Begin Error reporting
	/* GetErrorList()
	 * Description: Returns any errors encountered since the last time ClearErrorList() was called
	 * Parameters: n/a
	 * Output: ArrayList of KSoapErrorDetail objects
	 * Change log:
	 *      > 1.0: added to this utility
	 */
	public ArrayList<KSoap2ErrorDetail> GetErrorList() {
		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Entering GetErrorList");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, "Error count: " + errors.size());
				int counter = 1;
				for (KSoap2ErrorDetail error : errors) {
					Log.e(logTag, "  Error " + String.valueOf(counter) + ": " + error.ErrorMessage); //TODO: finish
					counter += 1;
				}
			}
		}
		
		return errors;
	}
	
	/* ClearErrorList()
	 * Description: Clears the ArrayList of errors
	 * Parameters: n/a
	 * Output: n/a
	 * Change log:
	 *      > 1.0: added to this utility
	 */
	public void ClearErrorList() {
		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Entering ClearErrorList");
		}

		errors.clear();

		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Errors cleared successfully");
		}
	}
// End Error reporting -->
	
	
// Begin Web Service interactions -->	
	/* ExecuteOnServer(serviceName, methodName, parameters)
	 * Description: Intended to be called when a web service is being called that does not return a data set.
	 * Parameters:
	 * 		> serviceName: web service name (example: Authenticate.asmx)
	 * 		> methodName: method to call
	 * 		> parameters: parameters to be passed to web service
	 * Output: boolean value. True if the web service was called without an exception. False if a problem occurred.
	 * Change log:
	 *      > 1.0: added to this utility
	 */
	public boolean ExecuteOnServer(final String serviceName, final String methodName, final HashMap<String, String> parameters) {
		boolean result = false;
		
		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Entering ExecuteOnServer");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, " > Service Name: " + serviceName);
				Log.e(logTag, " > Method Name: " + methodName);
				Log.e(logTag, " > Parameters: " + parameters.toString());
			}
		}
		
		try {
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Instantiating SoapObject: " + serverNamespace + " " + methodName);
			SoapObject request = new SoapObject(serverNamespace, methodName);
			
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Adding parameters");
			if (parameters != null) {
				for (Map.Entry<String, String> entry: parameters.entrySet()) {
					request.addProperty(entry.getKey(), entry.getValue());
				}
			}

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Instantiating HttpTransportSE: " + serverAddress + serviceName);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(serverAddress + serviceName);
			
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Calling SOAP service");
			androidHttpTransport.call(serverNamespace + methodName, envelope);
			
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Confirmation from SOAP received");
			result = true;
		}
		catch (Exception exception) {
			if (loggingLevel != KSoap2LoggingLevel.SILENT) {
				logException(exception, "ExecuteOnServer", null);
				if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
					exception.printStackTrace();
				}
			}

			KSoap2ErrorDetail error = new KSoap2ErrorDetail();
			error.ErrorMessage = exception.getMessage();
			errors.add(error);
			
			result = false;
		}
				
		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Exiting ExecuteOnServer.");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, " > Result: " + result);
			}
		}
		
		return result;
	}
	
	/* GetServiceAnswer(serviceName, methodName, parameters)
	 * Description: Calls a web service and responds with a String value.
	 * Parameters:
	 * 		> serviceName: web service name (example: Authenticate.asmx)
	 * 		> methodName: method to call
	 * 		> parameters: parameters to be passed to web service
	 * Output: String response from web service (NOTE: response is not formatted in JSON, XML, etc)
	 * Change log:
	 *      > 1.0: added to this utility
	 */	
	public String GetServiceAnswer(String serviceName, String methodName, HashMap<String, String> parameters) {
		String result = null;
		
		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Entering GetServiceAnswer (String return)");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, " > Service Name: " + serviceName);
				Log.e(logTag, " > Method Name: " + methodName);
				Log.e(logTag, " > Parameters: " + parameters.toString());
			}
		}

		try {
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Instantiating SoapObject: " + serverNamespace + " " + methodName);
            SoapObject request = new SoapObject(serverNamespace, methodName);  
            
    		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Adding parameters");
    		if (parameters != null) {
    			for (Map.Entry<String, String> entry: parameters.entrySet()) {
    				request.addProperty(entry.getKey(), entry.getValue());
    			}
    		}
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

    		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Instantiating HttpTransportSE: " + serverAddress + serviceName);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(serverAddress + serviceName);

    		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Calling SOAP service");
            androidHttpTransport.call(serverNamespace + methodName, envelope);
            SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
            if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Answer from SOAP received.");

            result = response.toString();
        } 
        catch (Exception exception) {
    		if (loggingLevel != KSoap2LoggingLevel.SILENT) {
    			logException(exception, "GetServiceAnswer (String return)", null);
    			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
    				exception.printStackTrace();
    			}
    		}

    		KSoap2ErrorDetail error = new KSoap2ErrorDetail();
    		error.ErrorMessage = exception.getMessage();
    		errors.add(error);

    		result = "";
        }		

		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Exiting GetServiceAnswer (String return).");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, " > Result: " + ((result != null) ? result : "<null>"));
			}
		}

		return result;
	}
	
	/* GetServiceAnswer(serviceName, methodName, parameters)
	 * Description: Calls a web service and responds with a JSON object.
	 * Parameters:
	 * 		> serviceName: web service name (example: Authenticate.asmx)
	 * 		> methodName: method to call
	 * 		> parameters: parameters to be passed to web service
	 * Output: JSON output from web service
	 * Change log:
	 *      > 1.0: added to this utility
	 */	
	public JSONArray GetServiceArrayAnswer(String serviceName, String methodName, HashMap<String, String> parameters) {
		JSONArray result = null;

		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Entering GetServiceAnswer (JSON return)");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, " > Service Name: " + serviceName);
				Log.e(logTag, " > Method Name: " + methodName);
				Log.e(logTag, " > Parameters: " + parameters.toString());
			}
		}

		try {
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Instantiating SoapObject: " + serverNamespace + " " + methodName);
            SoapObject request = new SoapObject(serverNamespace, methodName);
            
    		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Adding parameters");
    		if (parameters != null) {
    			for (Map.Entry<String, String> entry: parameters.entrySet()) {
    				request.addProperty(entry.getKey(), entry.getValue());
    			}
    		}
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

    		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Instantiating HttpTransportSE: " + serverAddress + serviceName);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(serverAddress + serviceName);

    		if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Calling SOAP service");
            androidHttpTransport.call(serverNamespace + methodName, envelope);
            SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
            if (loggingLevel == KSoap2LoggingLevel.VERBOSE) Log.e(logTag, "Answer from SOAP received.");

			result = new JSONArray(response.toString());
		}
		catch (JSONException exception) {
    		if (loggingLevel != KSoap2LoggingLevel.SILENT) {
    			logException(exception, "GetServiceAnswer (JSON return)", null);
    			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
    				exception.printStackTrace();
    			}
    		}

    		KSoap2ErrorDetail error = new KSoap2ErrorDetail();
    		error.ErrorMessage = "JSONException: " + exception.getMessage();
    		errors.add(error);
		}
		catch (Exception exception) {
    		if (loggingLevel != KSoap2LoggingLevel.SILENT) {
    			logException(exception, "GetServiceAnswer (JSON return)", null);
    			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
    				exception.printStackTrace();
    			}
    		}

    		KSoap2ErrorDetail error = new KSoap2ErrorDetail();
    		error.ErrorMessage = exception.getMessage();
    		errors.add(error);
		}

		if (loggingLevel == KSoap2LoggingLevel.MEDIUM || loggingLevel == KSoap2LoggingLevel.VERBOSE) {
			Log.e(logTag, "Exiting GetServiceAnswer (JSON return).");
			if (loggingLevel == KSoap2LoggingLevel.VERBOSE) {
				Log.e(logTag, " > Result: " + ((result != null) ? result.toString() : "<null>"));
			}
		}

		return result;
	}	
	
	/* logException(exception, procedure, moreInformation)
	 * Description: Internal, private function for logging exceptions
	 * Parameters:
	 * 		> exception: the Exception being raised
	 * 		> procedure: name of procedure where the Exception was encountered
	 * 		> moreInformation: any additional information
	 * Output: n/a
	 * Change log:
	 *      > 1.0: added to this utility
	 */	
	private void logException(Exception exception, String procedure, String moreInformation) {
		Log.e(logTag, "Exception raised in ExecuteOnServer");
		Log.e(logTag, " > Message: " + exception.getMessage());
		if (moreInformation != null) {
			Log.e(logTag, " > More information: " + moreInformation);
		}

	}
	
}
