package com.joe.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.joe.entity.Account;
import com.joe.entity.Product;
import com.joe.entity.Shop;

public class DataHandler {
	
	//	R409					Causeway Bay
	//	R485					Festival Walk
	//	R428					ifc mall
	// "MGAF2ZP/A"	: 6 Plus Gold		128
	// "MGAC2ZP/A"	: 6 Plus Black		128
	// "MGAE2ZP/A"	: 6 Plus Silver		128
	// "MGAK2ZP/A"	: 6 Plus Gold		64
	// "MGAH2ZP/A"	: 6 Plus Black		64
	// "MGAJ2ZP/A"	: 6 Plus Silver		64
	// "MGAA2ZP/A"	: 6 Plus Gold		16
	// "MGA82ZP/A"	: 6 Plus Black		16
	// "MGA92ZP/A"	: 6 Plus Silver		16
	// "MG4E2ZP/A"	: 6 		 Gold		128
	// "MG4A2ZP/A"	: 6 		 Black		128
	// "MG4C2ZP/A"	: 6 		 Silver	128
	// "MG4J2ZP/A"	: 6 		 Gold		64
	// "MG4F2ZP/A"	: 6 		 Black		64
	// "MG4H2ZP/A"	: 6 		 Silver	64
	// "MG492ZP/A"	: 6 		 Gold		16
	// "MG472ZP/A"	: 6 		 Black		16
	// "MG482ZP/A"	: 6 		 Silver	16
	//	idHongkong
	//	homeReturnCard
	//	passport

	private final Logger logger = Logger.getLogger(DataHandler.class);

	private GUI gui;
	private Account account;
	private HttpClientContext context;
	private CloseableHttpClient httpclient;
	private List<URI> redirectLocations;
	private URI lastURI;
	private ExecutorService es = Executors.newFixedThreadPool(1);
	
	private Map<String, Product> productMap = new HashMap<>();
	private Map<String, String> pVars = new HashMap<>();
	private Map<String, Shop> shopMap = new HashMap<>();
	
	private ExecutorService avaES = Executors.newFixedThreadPool(1);
	
	private final int CONNTECTION_TIMEOUT_MS = 10000;
	private final int CONNECTION_REQUEST_TIMEOUT_MS = 10000;
	private final int SOCKET_TIMEOUT_MS = 10000;
	
	private boolean isCaptchaPreload = false;

	public DataHandler(GUI gui) {
		this.gui = gui;
		account = gui.getAccount();
		
		// Load product data
		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream("./product.json"), "UTF8");
			Gson gson = new GsonBuilder().create();
			Product[] products =  gson.fromJson(reader, Product[].class);
			for(Product p : products){
				productMap.put(p.getPartNumber(), p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Init shop
		shopMap.put("R409", new Shop());
		shopMap.put("R485", new Shop());
		shopMap.put("R428", new Shop());
	}
	
	public void preloadCaptcha() {
		Runnable runnable = new Runnable() {
			public void run() {
				while(true){
					logger.info("---------preloadCaptcha Started---------");
					try {
						//Reset all
						redirectLocations = new ArrayList<URI>();
						if(httpclient != null)
							httpclient.close();
						context = HttpClientContext.create();
						
						RequestConfig.Builder requestBuilder = RequestConfig.custom()
								.setConnectTimeout(CONNTECTION_TIMEOUT_MS)
								.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
								.setSocketTimeout(SOCKET_TIMEOUT_MS);
						if(gui.useProxy()){
							HttpHost proxy = new HttpHost(gui.getProxyURL(), gui.getProxyPort(), "http");
							requestBuilder.setProxy(proxy);
						}
						
						httpclient = HttpClientBuilder.create()
								.setRedirectStrategy(new LaxRedirectStrategy())
								.setDefaultRequestConfig(requestBuilder.build())
								.build();
						
						HttpGet httpget = new HttpGet("https://reserve-ca.apple.com/CA/en_CA/reserve/iPhone");
						CloseableHttpResponse response = httpclient.execute(httpget, context);
						EntityUtils.consume(response.getEntity());
						List<URI> redirectList = context.getRedirectLocations();
						if(redirectList != null){
							if(redirectList.size() == 1){
								logger.info("Redirect location == 1, check if IP is banned");
								return;
							}
			
							// Get captcha
							httpget = new HttpGet("https://signin.apple.com/IDMSWebAuth/imageCaptcha/942#"+System.currentTimeMillis());
							CloseableHttpResponse captchaResponse = httpclient.execute(httpget, context);
							gui.setCaptchaImage(captchaResponse.getEntity().getContent());
							isCaptchaPreload = true;
							
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.MINUTE, 2);
					    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
							logger.info("---------preloadCaptcha Ended, captcha expires at: " + sdf.format(cal.getTime()) + "---------");
							return;
						}else{
							logger.info("CA Store not ready");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		es.execute(runnable);
	}

	public void getLoginPage() {
		Runnable runnable = new Runnable() {
			public void run() {
				while(true){
					logger.info("---------getLoginPage Started---------");
					try {
						//Reset all
						redirectLocations = new ArrayList<URI>();
						if(!isCaptchaPreload){
							if(httpclient != null)
								httpclient.close();
							context = HttpClientContext.create();
							
							RequestConfig.Builder requestBuilder = RequestConfig.custom()
									.setConnectTimeout(CONNTECTION_TIMEOUT_MS)
									.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
									.setSocketTimeout(SOCKET_TIMEOUT_MS);
							if(gui.useProxy()){
								HttpHost proxy = new HttpHost(gui.getProxyURL(), gui.getProxyPort(), "http");
								requestBuilder.setProxy(proxy);
							}
							
							httpclient = HttpClientBuilder.create()
									.setRedirectStrategy(new LaxRedirectStrategy())
									.setDefaultRequestConfig(requestBuilder.build())
									.build();
						}
						
						HttpGet httpget = new HttpGet("https://reserve-hk.apple.com/HK/zh_HK/reserve/iPhone");
						CloseableHttpResponse response = httpclient.execute(httpget, context);
						EntityUtils.consume(response.getEntity());
						List<URI> redirectList = context.getRedirectLocations();
						if(redirectList != null && redirectList.size() > 0){
							if(redirectList.size() == 1){
								logger.info("Redirect location == 1, check if IP is banned");
								return;
							}
							
							redirectLocations.addAll(redirectList);
							
							List<NameValuePair> paramsList = URLEncodedUtils.parse(redirectLocations.get(1), "UTF8");
							for(NameValuePair valuePairs : paramsList){
								pVars.put(valuePairs.getName(), valuePairs.getValue());
							}
			
							if(!isCaptchaPreload){
								// Get captcha
								httpget = new HttpGet("https://signin.apple.com/IDMSWebAuth/imageCaptcha/942#"+System.currentTimeMillis());
								CloseableHttpResponse captchaResponse = httpclient.execute(httpget, context);
								gui.setCaptchaImage(captchaResponse.getEntity().getContent());
								logger.info("---------getLoginPage Ended---------");
							}else{
								logger.info("---------getLoginPage Ended---------");
								
								String captchaInput = gui.getCaptchaText();
								logger.info("---------Auto login with preCaptcha " + captchaInput + "---------");
								loginWithCaptcha(captchaInput);
							}
							return;
						}else{
							logger.info("Store not ready");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		es.execute(runnable);
	}
	
	public void loginWithCaptcha(final String captcha){
		Runnable runnable = new Runnable() {
			public void run() {
				logger.info("---------loginWithCaptcha Started---------");
				
				try {
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					formparams.add(new BasicNameValuePair("Env", "PROD"));
					formparams.add(new BasicNameValuePair("accountPassword", account.getPassword()));
					formparams.add(new BasicNameValuePair("appleId",  account.getId()));
					formparams.add(new BasicNameValuePair("captchaAudioInput", ""));
					formparams.add(new BasicNameValuePair("captchaInput", captcha));
					formparams.add(new BasicNameValuePair("captchaToken", ""));
					formparams.add(new BasicNameValuePair("captchaType", "image"));
					formparams.add(new BasicNameValuePair("openiForgotInNewWindow", "true"));
					formparams.add(new BasicNameValuePair("appIdKey", pVars.get("appIdKey")));
					formparams.add(new BasicNameValuePair("language", pVars.get("language")));
					formparams.add(new BasicNameValuePair("path", pVars.get("path")));
					formparams.add(new BasicNameValuePair("rv", pVars.get("rv")));
					formparams.add(new BasicNameValuePair("sslEnabled", "true"));
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
					
					Cookie JSESSIONID = getCookie("JSESSIONID", "signin.apple.com", "/IDMSWebAuth/");
					HttpPost httpPost = new HttpPost("https://signin.apple.com/IDMSWebAuth/authenticate;jsessionid="+JSESSIONID.getValue());
					httpPost.setEntity(entity);
					CloseableHttpResponse response = httpclient.execute(httpPost, context);
					EntityUtils.consume(response.getEntity());
					List<URI> redirectList = context.getRedirectLocations();
					if(redirectList != null){
						redirectLocations.addAll(redirectList);
						lastURI = redirectLocations.get(redirectLocations.size()-1);
						List<NameValuePair> paramsList = URLEncodedUtils.parse(lastURI, "UTF8");
						for(NameValuePair valuePairs : paramsList){
							pVars.put(valuePairs.getName(), valuePairs.getValue());
						}
					}
					
					// Get Keyword
					HttpGet httpget = new HttpGet(lastURI+"&ajaxSource=true&_eventId=context");
					CloseableHttpResponse keywordResponse = httpclient.execute(httpget, context);
					
					String jsonStr = EntityUtils.toString(keywordResponse.getEntity(), Consts.UTF_8);
					Type type = new TypeToken<Map<String, String>>(){}.getType();
					try{
						Map<String,String> keywordMap = new Gson().fromJson(jsonStr, type);
						if(keywordMap != null){
							pVars.putAll(keywordMap);	//here put p_ie in pVars
							String keywordValString = pVars.get("keyword");
							
							for(Map.Entry<String, String> entry : pVars.entrySet()){
								if(entry.getKey().startsWith("IRSV")){
									logger.info("Found "+entry.getKey()+", replacing keyword");
									keywordValString = entry.getValue();
								}
							}
							
							if(keywordValString.indexOf("base64,") != -1){
								String base64Str = keywordValString.substring(keywordValString.indexOf("base64,")+7);
								gui.setSendSMSCodeLabel(base64Str);
							}else{
								gui.setSendSMSCode(keywordValString);
							}
						}else{
							logger.info("keywordMap is null, connect too frequent?");
							httpclient.close();
						}
					}catch(JsonSyntaxException exception){
						logger.info("Fail to parse keyword JSON");
						httpclient.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				logger.info("---------loginWithCaptcha Ended---------");
			}
		};
		
		es.execute(runnable);
	}
	
	public void verifySMS(final String reservationCode){
		Runnable runnable = new Runnable() {
			public void run() {
				logger.info("---------verifySMS Started---------");
				
				try {
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					formparams.add(new BasicNameValuePair("_eventId", "next"));
					formparams.add(new BasicNameValuePair("_flowExecutionKey", pVars.get("_flowExecutionKey")));
					formparams.add(new BasicNameValuePair("p_ie", pVars.get("p_ie")));
					formparams.add(new BasicNameValuePair("phoneNumber",  account.getPhoneNumber()));
					formparams.add(new BasicNameValuePair("reservationCode", reservationCode));
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);

					HttpPost httpPost = new HttpPost(lastURI);
					httpPost.setEntity(entity);
					CloseableHttpResponse response = httpclient.execute(httpPost, context);
					List<URI> redirectList = context.getRedirectLocations();
					if(redirectList != null){
						redirectLocations.addAll(redirectList);
						lastURI = redirectLocations.get(redirectLocations.size()-1);
						List<NameValuePair> paramsList = URLEncodedUtils.parse(lastURI, "UTF8");
						for(NameValuePair valuePairs : paramsList){
							pVars.put(valuePairs.getName(), valuePairs.getValue());
						}
					}
					EntityUtils.toString(response.getEntity(), Consts.UTF_8);	//MUST GET CONTENT BEFORE SENDING ANOTHER RQ

					
					// Get shop list
//					HttpGet httpget = new HttpGet("https://reserve-hk.apple.com/HK/zh_HK/reserve/iPhone?execution=e1s3&ajaxSource=true&_eventId=context");
//					CloseableHttpResponse shopListResponse = httpclient.execute(httpget, context);
//					JsonParser parser = new JsonParser();
//					String shopListJsonStr = EntityUtils.toString(shopListResponse.getEntity(), Consts.UTF_8);
//					JsonObject shopListJson = (JsonObject)parser.parse(shopListJsonStr);
//					logger.info(shopListJson);
					//TBD Set shop enable
					

					
					avaES.execute(new checkAvailable());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				logger.info("---------verifySMS Ended---------");
			}
		};
		
		es.execute(runnable);
	}
	

	
	private JsonObject getTimeslot(String shop){
		logger.info("getting " + shop + " timeslot");
		
		String timeslotJsonStr = "";
		try{
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("_eventId", "timeslots"));
			formparams.add(new BasicNameValuePair("ajaxSource", "true"));
			formparams.add(new BasicNameValuePair("p_ie", pVars.get("p_ie")));
			formparams.add(new BasicNameValuePair("storeNumber",  shop));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);

			HttpPost httpPost = new HttpPost(lastURI);
			httpPost.setEntity(entity);
			CloseableHttpResponse timeslotResponse = httpclient.execute(httpPost, context);
			timeslotJsonStr = EntityUtils.toString(timeslotResponse.getEntity(), Consts.UTF_8);
			logger.info(timeslotJsonStr);
			JsonParser parser = new JsonParser();
			JsonObject timeslotJson = (JsonObject)parser.parse(timeslotJsonStr);
			JsonArray timeslotsAry = timeslotJson.get("timeslots").getAsJsonArray();
			Map<String, String> timeslotsMap = new HashMap<>();
			for(JsonElement ele : timeslotsAry){
				JsonObject object = ele.getAsJsonObject();
				String timeSlotId = object.get("timeSlotId").getAsString();
				String formattedTime = object.get("formattedTime").getAsString();
				timeslotsMap.put(formattedTime, timeSlotId);
			}
			shopMap.get(shop).setTimeslots(timeslotsMap);
		}catch(MalformedJsonException me){
			logger.info("getTimeslot - MalformedJsonException");
			logger.info(timeslotJsonStr);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private JsonObject getAvailability(String shop){
		logger.info("getting " + shop + " product availability");
		
		String availbilityStr = null;
		try{
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("_eventId", "availability"));
			formparams.add(new BasicNameValuePair("ajaxSource", "true"));
			formparams.add(new BasicNameValuePair("p_ie", pVars.get("p_ie")));
			formparams.add(new BasicNameValuePair("partNumbers",  account.getTargetProductListStr()));
			formparams.add(new BasicNameValuePair("selectedContractType", "UNLOCKED"));
			formparams.add(new BasicNameValuePair("storeNumber", shop));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
	
			HttpPost httpPost = new HttpPost(lastURI);
			httpPost.setEntity(entity);
			CloseableHttpResponse availabilityResponse = httpclient.execute(httpPost, context);
			List<URI> redirectList = context.getRedirectLocations();
			if(redirectList != null){
				if(redirectList.size() == 1){
					logger.info("Redirect location == 1, check if IP is banned");
					return null;
				}
			}
			availbilityStr = EntityUtils.toString(availabilityResponse.getEntity(), Consts.UTF_8);
			logger.info(availbilityStr);
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(availbilityStr);
			if(element instanceof JsonObject){
				return (JsonObject)element;
			}else{
				logger.info("availbilityStr cannot parse to JsonObject: not JsonObject"); 
			}
		}catch(JsonSyntaxException e){
			logger.info("availbilityStr cannot parse to JsonObject: JsonSyntaxException"); 		
		}catch(MalformedJsonException e){
			logger.info("availbilityStr cannot parse to JsonObject: MalformedJsonException"); 
		}catch(Exception e){
			logger.info("availbilityStr cannot parse to JsonObject: Exception"); 		
		}
		return null;
	}
	
	private boolean order(String shop, String partNumber){
		try{
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("_eventId", "next"));
			formparams.add(new BasicNameValuePair("_flowExecutionKey", pVars.get("execution")));
			formparams.add(new BasicNameValuePair("color", productMap.get(partNumber).getColor()));
			formparams.add(new BasicNameValuePair("email", account.getId()));
			formparams.add(new BasicNameValuePair("firstName", account.getFirstName()));
			formparams.add(new BasicNameValuePair("govtId", account.getGovtId()));
			formparams.add(new BasicNameValuePair("lastName", account.getLastName()));
			formparams.add(new BasicNameValuePair("p_ie", pVars.get("p_ie")));
			formparams.add(new BasicNameValuePair("product", productMap.get(partNumber).getProduct()));
			formparams.add(new BasicNameValuePair("selectedContractType", "UNLOCKED"));
			formparams.add(new BasicNameValuePair("selectedGovtIdType", account.getGovtIdType()));
			formparams.add(new BasicNameValuePair("selectedPartNumber",  partNumber));
			formparams.add(new BasicNameValuePair("selectedQuantity",  "2"));
			formparams.add(new BasicNameValuePair("selectedStoreNumber", shop));
			String timeslotId = null;
			if(gui.specificTimeslotOnly())
				timeslotId = shopMap.get(shop).getTimeslots().get(account.getTimeslot());
			else {
				Map<String, String> timeslotMap = shopMap.get(shop).getTimeslots();
				for(Map.Entry<String, String> entry : timeslotMap.entrySet()){
					timeslotId = entry.getValue();
					break;
				}
			}
			formparams.add(new BasicNameValuePair("selectedTimeSlotId", timeslotId));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
			logger.info("ordering " +  partNumber + ", timeslot: " + timeslotId + ", shop: " + shop + ", _flowExecutionKey: " + pVars.get("execution"));
			
			HttpPost httpPost = new HttpPost(lastURI);
			httpPost.setEntity(entity);
			CloseableHttpResponse orderResponse = httpclient.execute(httpPost, context);
			List<URI> redirectList = context.getRedirectLocations();
			if(redirectList != null){
				redirectLocations.addAll(redirectList);
				lastURI = redirectLocations.get(redirectLocations.size()-1);
				List<NameValuePair> paramsList = URLEncodedUtils.parse(lastURI, "UTF8");
				for(NameValuePair valuePairs : paramsList){
					pVars.put(valuePairs.getName(), valuePairs.getValue());
				}
			}
			String orderResult = EntityUtils.toString(orderResponse.getEntity());
			if(orderResult.indexOf("confirmation") == -1){
				logger.info("confirmation string not found, reserve failed");
				return false;
			}else{
				logger.info("reserve success");
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	

	
	
	//	1.		Get product availability from specific shop
	//	2a.		If available			-	Get time slot (once only)
	//	2a.								-	Place order
	//	2b.		If not available 	-	Go to 1
	class checkAvailable implements Runnable{
		
		int round = 0;

		@Override
		public void run() {
			List<String> shopList = account.getTargetShopList();
			for(int i=0; i<shopList.size(); i++){
				String shop = shopList.get(i);
			
				JsonObject availabilityJson = getAvailability(shop);
				if(availabilityJson == null){
					logger.info("Error: availabilityJson is null, exiting");
					return;
				}
				Map<String, Boolean> invMap = shopMap.get(shop).getInventories();
				
				JsonArray invArray = availabilityJson.getAsJsonArray("inventories");
				
				if(invArray == null){
					logger.info("inventories return null, exiting");
					return;
				}
				
				for(JsonElement ele : invArray){
					JsonObject object = ele.getAsJsonObject();
					String partNumber = object.get("partNumber").getAsString();
					Boolean available = object.get("available").getAsBoolean();
					invMap.put(partNumber, available);
				}
				
				List<String> targetList = account.getTargetProductList();
				for(String partNumber : targetList){
					if(invMap.get(partNumber).equals(true)){
						logger.info("Target available: " + partNumber);
						boolean success = false;
						if(shopMap.get(shop).getTimeslots() == null || gui.alwaysRequestTimeslot()){
							getTimeslot(shop);
							success = order(shop, partNumber);
						}else{
							success = order(shop, partNumber);
						}
						if(success)
							return;
					}
				}
				
				if(i == shopList.size()-1){
					i = -1;
					round++;
				}
				
				try {
					if(round == 0){
						Thread.sleep(gui.getFRPollInterval());
					}else{
						Thread.sleep(gui.getSRPollInterval());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void close(){
		try {
			if(httpclient != null)
				httpclient.close();
			logger.info("Close connection");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printVars(CloseableHttpResponse response){
		try{
			for(Map.Entry<String, String> entry : pVars.entrySet()){
				logger.info("Key : " + entry.getKey() + " Value : " + entry.getValue());
			}
			
			List<Cookie> cookies = context.getCookieStore().getCookies();
			for (Cookie cookie : cookies) {
				logger.info(" Cookie: - " + cookie);
			}
			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				logger.info(" Header: - " + header);
			}
			logger.info(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Cookie getCookie(String name, String domain, String path){
		List<Cookie> cookies = context.getCookieStore().getCookies();
		for(Cookie cookie : cookies){
			if(cookie.getName().equals(name) && cookie.getDomain().equals(domain) && cookie.getPath().equals(path))
				return cookie;
		}
		return null;
	}
}
