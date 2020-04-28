package org.openobservatory.ooniprobe.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.openobservatory.ooniprobe.BuildConfig;
import org.openobservatory.ooniprobe.client.OONIAPIClient;
import org.openobservatory.ooniprobe.client.OONIOrchestraClient;
import org.openobservatory.ooniprobe.model.jsonresult.TestKeys;

import java.io.IOException;
import java.util.Date;

import ly.count.android.sdk.Countly;
import ly.count.android.sdk.CountlyConfig;
import ly.count.android.sdk.DeviceId;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Application extends android.app.Application {

	static {
		System.loadLibrary("measurement_kit");
	}

	private PreferenceManager preferenceManager;
	private Gson gson;
	private boolean testRunning;
	private OkHttpClient okHttpClient;
	private OONIOrchestraClient orchestraClient;
	private OONIAPIClient apiClient;

	@Override public void onCreate() {
		super.onCreate();

		/*
		    private final String[] validFeatureNames = new String[]{
            CountlyFeatureNames.sessions,
            CountlyFeatureNames.events,
            CountlyFeatureNames.views,
            CountlyFeatureNames.location,
            CountlyFeatureNames.crashes,
            CountlyFeatureNames.attribution,
            CountlyFeatureNames.users,
            CountlyFeatureNames.push,
            CountlyFeatureNames.starRating};
		 */
		//TODO prepare features that should be added to the group
		String[] groupFeatures = new String[]{
				Countly.CountlyFeatureNames.sessions,
				Countly.CountlyFeatureNames.views,
				Countly.CountlyFeatureNames.crashes,
				Countly.CountlyFeatureNames.push,
				Countly.CountlyFeatureNames.events };
		//TODO check for consent
		//Countly.sharedInstance().setRequiresConsent(true);
		CountlyConfig config = new CountlyConfig()
				.setAppKey("146836f41172f9e3287cab6f2cc347de3f5ddf3b")
				.setContext(this)
				//.setDeviceId("lorenzo")
				//.setDeviceId(null)
				//it won't work with fdroid
				.setIdMode(DeviceId.Type.ADVERTISING_ID)
				//.setIdMode(DeviceId.Type.OPEN_UDID)
				//.setRequiresConsent(true)
				.setConsentEnabled(groupFeatures)
				.setServerURL("https://countly.ooni.io")
				//.setLoggingEnabled(!BuildConfig.DEBUG)
				.setLoggingEnabled(true)
				.setViewTracking(true)
				.setHttpPostForced(true)
				.enableCrashReporting();
		Countly.sharedInstance().init(config);

		preferenceManager = new PreferenceManager(this);
		gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateAdapter()).registerTypeAdapter(TestKeys.Tampering.class, new TamperingJsonDeserializer()).create();
		FlavorApplication.onCreate(this, preferenceManager.isSendCrash());
		if (BuildConfig.DEBUG)
			FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
		// Code commented to prevent callling API on app start
		//if (preferenceManager.canCallDeleteJson())
		//	Measurement.deleteUploadedJsons(this);

		FlowManager.init(this);
	}

	public OkHttpClient getOkHttpClient() {
		if (okHttpClient == null) {
			HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
			logging.level(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);
			okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
					.addInterceptor(logging)
					.addInterceptor(new Interceptor() {
						@Override
						public Response intercept(Chain chain) throws IOException {
							Request request = chain.request().newBuilder().addHeader("User-Agent", "ooniprobe-android/" + BuildConfig.VERSION_NAME).build();
							return chain.proceed(request);
						}
					})
                    .build();
		}
		return okHttpClient;
	}

	public OONIOrchestraClient getOrchestraClient() {
		if (orchestraClient == null) {
			orchestraClient = new Retrofit.Builder()
					.baseUrl(BuildConfig.OONI_ORCHESTRATE_BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.client(getOkHttpClient())
					.build().create(OONIOrchestraClient.class);
		}
		return orchestraClient;
	}

	public OONIAPIClient getApiClient() {
		if (apiClient == null) {
			apiClient = new Retrofit.Builder()
					.baseUrl(BuildConfig.OONI_API_BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.client(getOkHttpClient())
					.build().create(OONIAPIClient.class);
		}
		return apiClient;
	}

	public PreferenceManager getPreferenceManager() {
		return preferenceManager;
	}

	public Gson getGson() {
		return gson;
	}

	public boolean isTestRunning() {
		return testRunning;
	}

	public void setTestRunning(boolean testRunning) {
		this.testRunning = testRunning;
	}
}
