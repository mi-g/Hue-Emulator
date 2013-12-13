package com.hueemulator.server.handlers;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hueemulator.emulator.Controller;
import com.hueemulator.model.PHBridgeConfiguration;
import com.hueemulator.model.PHLight;
import com.hueemulator.model.PHLightState;
import com.hueemulator.utils.PHUtilitiesHelper;
import com.hueemulator.utils.PointF;
import com.hueemulator.utils.Utils;

public class LightsAPI {

    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=
    //  1.1  GET ALL LIGHTS
 //  http://developers.meethue.com/1_lightsapi.html   1.1. Get all lights
    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=    
 public void getAllLights_1_1(PHBridgeConfiguration bridgeConfiguration, OutputStream responseBody, Controller controller) throws JsonParseException, IOException {    
          Map <String, PHLight> lightsMap = bridgeConfiguration.getLights();
          
          Iterator it = lightsMap.entrySet().iterator();
         
          JSONObject lightsJson = new JSONObject();
          
          while (it.hasNext()) {
              Map.Entry <String, PHLight> entry = (Map.Entry) it.next();
              String identifier = (String)entry.getKey();
              PHLight light = (PHLight) entry.getValue();
              
              JSONObject lightJson = new JSONObject();
              lightJson.putOpt("name", light.getName());
              
             
              lightsJson.putOpt(identifier, lightJson);
          }
          
          responseBody.write(lightsJson.toString().getBytes());
          responseBody.close();
 }


    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=
    //  1.4  GET LIGHT ATTRIBUTES AND STATE
    //  http://developers.meethue.com/1_lightsapi.html   1.4. Get light attributes and state
    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=
 public void getLightAttributes_1_4(ObjectMapper mapper, PHBridgeConfiguration bridgeConfiguration, OutputStream responseBody, Controller controller, String lightIdentifier) throws JsonParseException, IOException {
  
   if (bridgeConfiguration.getLights() == null || bridgeConfiguration.getLights().get(lightIdentifier) == null) {
          sendErrorResponse(lightIdentifier, "3", responseBody);
   }
   else {
   mapper.writeValue(responseBody, bridgeConfiguration.getLights().get(lightIdentifier));   // Write to the response.
   controller.addTextToConsole(mapper.writeValueAsString(bridgeConfiguration.getLights().get(lightIdentifier)),Color.WHITE); 
   }

 }
 
    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=
    //  1.5  SET LIGHT ATTRIBUTES
 //  http://developers.meethue.com/1_lightsapi.html   1.5. Set light attributes (rename)
    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*= 
 public void setLightAttributes_1_5(ObjectMapper mapper, String jSONString, PHBridgeConfiguration bridgeConfiguration, OutputStream responseBody, Controller controller, String light) throws JsonParseException, IOException {    
    PHLight lightObject = bridgeConfiguration.getLights().get(light);
 
    String responseBase = "/lights/" + light + "/";
    String resourceUrl="";
 
    JSONArray responseArray = new JSONArray();
    
         JSONObject jObject = new JSONObject(jSONString);
         if (jObject != null) {
             JSONArray names = jObject.names();
           
             for (int i=0; i<names.length(); i++) {
                 JSONObject successObject = new JSONObject();
                 boolean isSuccess=true;  // Success is returned for a valid fieldname, if a field name is invalid then an "error" is returned.
                 responseArray.put(successObject);
                 
                 JSONObject successLine = new JSONObject();
                 String name = names.getString(i);
                 resourceUrl = responseBase + name;
                 
                 if (name.equals("name")) {
                    String lightName = jObject.optString("name");
                    
                    if (lightName == null || lightName.length() > 32) {
                     isSuccess=false;
                    }
                    else {
                        successLine.putOpt(resourceUrl, lightName);
                        lightObject.setName(lightName);                     
                    }
                 }
                 
                 if (!isSuccess)  {   // Handle errors,  i.e.  Non Supported fields
                     JSONObject errorLine = new JSONObject();
                     errorLine.putOpt("type", 7);
                     errorLine.putOpt("address", resourceUrl);
                     errorLine.putOpt("description", "invalid value, " + Utils.chopName(jObject.optString(name)) + ", for parameter, name");
                     successObject.putOpt("error", errorLine);
                    }
                 
                 
                 if (isSuccess) { 
                     successObject.putOpt("success", successLine);
                 }
             }
         }

         responseBody.write(responseArray.toString().getBytes());
         responseBody.close();
         bridgeConfiguration.getLights().put(light, lightObject);  
         controller.addTextToConsole(responseArray.toString(), Color.WHITE); 
 }
 
    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=
    //  1.6  SET LIGHT STATE
    //  http://developers.meethue.com/1_lightsapi.html   1.6. Set light state
    // *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*= 
 public void setLightState_1_6(ObjectMapper mapper, String jSONString, PHBridgeConfiguration bridgeConfiguration, OutputStream responseBody, Controller controller, String lightIdentifier) throws JsonParseException, IOException {
     
     String resourceUrl = "/lights/" + lightIdentifier + "/state/";
     
        if (bridgeConfiguration.getLights() == null || bridgeConfiguration.getLights().get(lightIdentifier) == null) {
            sendErrorResponse(lightIdentifier, "3", responseBody);
         }
       
        PHLightState ls = bridgeConfiguration.getLights().get(lightIdentifier).getState();
  
        JSONArray responseArray = new JSONArray();

        setLightState(resourceUrl,ls, responseArray, jSONString);

        responseBody.write(responseArray.toString().getBytes());
        responseBody.close();
        
        bridgeConfiguration.getLights().get(lightIdentifier).setState(ls);
        controller.addTextToConsole(responseArray.toString(), Color.WHITE);     
 }
 
 
 public void setLightState(String baseUrl, PHLightState ls,  JSONArray responseArray, String jSONString) {
        JSONObject jObject = new JSONObject(jSONString);
        if (jObject != null) {
            JSONArray names = jObject.names();
          
            for (int i=0; i<names.length(); i++) {
                JSONObject successObject = new JSONObject();
                
                JSONObject successLine = new JSONObject();
                String name = names.getString(i);
                String errorDescription="";
                int errorType=0;
                
                String resourceUrl=baseUrl + name;
                responseArray.put(successObject);
                
                boolean isSuccess=true;  // Success is returned for a valid fieldname, if a field name is invalid then an "error" is returned.
                boolean isOn=ls.getOn();       // An attempt to modify the state of a bulb which is off results in a bridge error.
                
                if (name.equals("on")) {
                   
                    successLine.putOpt(resourceUrl, jObject.optBoolean(name));
                    ls.setOn(jObject.optBoolean(name)); 
                }
                else if (name.equals("hue")) {
                    int val = jObject.optInt(name);
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, hue, is not modifiable. Device is set to off.";
                    }
                    else if (Utils.isInRange(val, 0, 65535)) {
                        successLine.putOpt(resourceUrl, val);
                        ls.setHue(val); 
                    }
                    else {
                        isSuccess=false;
                        errorDescription = "invalid value, " + val + " , for parameter, hue";
                        errorType=7;
                    }
                }
                else if (name.equals("bri")) {
                    int val = jObject.optInt(name);
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, bri, is not modifiable. Device is set to off.";
                    }
                    else if (Utils.isInRange(val, 0, 255)) {
                        successLine.putOpt(resourceUrl, val);
                        ls.setBri(val); 
                    }
                    else {
                        isSuccess=false;
                        errorDescription = "invalid value, " + val + " , for parameter, bri";
                        errorType=7;
                    }
                }
                else if (name.equals("sat")) {
                    int val = jObject.optInt(name);
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, sat, is not modifiable. Device is set to off.";
                    }
                    else if (Utils.isInRange(val, 0, 255)) {
                        successLine.putOpt(resourceUrl, val);
                        ls.setSat(val); 
                    }
                    else {
                        isSuccess=false;
                        errorDescription = "invalid value, " + val + " , for parameter, sat";
                        errorType=7;
                    }
                }
                else if (name.equals("ct")) {
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, hue, is not modifiable. Device is set to off.";
                    }
                    else {
                        successLine.putOpt(resourceUrl, jObject.optInt(name));
                        ls.setCt(jObject.optInt(name)); 
                    }

                }
                else if (name.equals("xy")) {
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, hue, is not modifiable. Device is set to off.";
                    }
                    else {
                        JSONArray xyArray = jObject.optJSONArray("xy");
                        successLine.putOpt(resourceUrl, xyArray);
                        
                        String model="LCT001";
                        float point1 = Float.valueOf(xyArray.get(0).toString());
                        float point2 = Float.valueOf(xyArray.get(1).toString());
                        PointF xy = new PointF(point1,point2);
                        xy = PHUtilitiesHelper.fixIfOutOfRange(xy, model);  // If the sent x/y values are out of range, the find the closest point.
                        float[] xyFloatArray = {xy.x, xy.y};
                        int colour = PHUtilitiesHelper.colorFromXY(xyFloatArray, model);
    
                        Color col = new Color(colour);
                        int r = col.getRed();
                        int g = col.getGreen();
                        int b = col.getBlue();
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(r,g,b,hsv);
    
                        // Recalculate Hue
                        ls.setHue((int) (hsv[0] * 65535));
                        List<Double> xyList = new ArrayList();
                        xyList.add((double) xy.x); 
                        xyList.add((double) xy.y);
                        ls.setXy(xyList);
                    }
                }
                else if (name.equals("effect")) {
                 String effect = jObject.optString(name);
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, hue, is not modifiable. Device is set to off.";
                    }
                    else if (effect==null || (!effect.equals("none") && !effect.equals("colorloop"))) {
                        isSuccess=false;
                        errorDescription = "invalid value, " + effect + " , for parameter, effect";
                        errorType=7;
                 }
                 else {
                        successLine.putOpt(resourceUrl, effect);
                        ls.setEffect(effect); 
                 }
                }
                else if (name.equals("alert")) {
                 String alert = jObject.optString(name);
                    if (!isOn) {
                        isSuccess=false; errorType=201;  errorDescription = "parameter, hue, is not modifiable. Device is set to off.";
                    }
                    else if (alert==null || (!alert.equals("none") && !alert.equals("select") && !alert.equals("lselect"))) {
                        isSuccess=false;
                        errorDescription = "invalid value, " + alert + " , for parameter, alert";
                        errorType=7;
                 }
                 else {
                        successLine.putOpt(resourceUrl, alert);
                        ls.setAlert(alert); 
                 }
 
                }
                else if (name.equals("reachable")) {
                    successLine.putOpt(resourceUrl, jObject.optBoolean(name));
                    ls.setReachable(jObject.optBoolean(name)); 
                }
                else {
                  isSuccess=false;     // Handle errors,  i.e.  Non Supported fields
                  errorDescription = "parameter, " + name + ", not available";
                  errorType=6;
                }
                
                if (!isSuccess) {   
                 isSuccess=false;
                 JSONObject errorLine = new JSONObject();
                 errorLine.putOpt("type", errorType);
                 errorLine.putOpt("address", resourceUrl);
                 errorLine.putOpt("description", errorDescription);
                 successObject.putOpt("error", errorLine);
                }
                
                if (isSuccess) { 
                    successObject.putOpt("success", successLine);
                }
            }  // End of for loop.
        }
 }
 
   public void sendErrorResponse(String lightIdentifier, String type, OutputStream responseBody) throws IOException {
       JSONArray responseArray = new JSONArray();
       JSONObject errorObject = new JSONObject();
       JSONObject errorLine = new JSONObject();
       errorLine.putOpt("type", type);
       errorLine.putOpt("address", "/lights/" + lightIdentifier);
       errorLine.putOpt("description", "resource, /lights/" + lightIdentifier + ", not available");
       errorObject.putOpt("error", errorLine);
       responseArray.put(errorObject);
       responseBody.write(responseArray.toString().getBytes());
       responseBody.close();
   }
 
}