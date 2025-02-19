package org.openobservatory.ooniprobe.ui.resultdetails;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.adevinta.android.barista.rule.flaky.AllowFlaky;
import com.adevinta.android.barista.rule.flaky.FlakyTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openobservatory.ooniprobe.R;
import org.openobservatory.ooniprobe.common.OONITests;
import org.openobservatory.ooniprobe.factory.ResultFactory;
import org.openobservatory.ooniprobe.model.database.Measurement;
import org.openobservatory.ooniprobe.model.database.Result;
import org.openobservatory.ooniprobe.model.jsonresult.TestKeys;
import org.openobservatory.ooniprobe.test.test.Dash;
import org.openobservatory.ooniprobe.test.test.HttpHeaderFieldManipulation;
import org.openobservatory.ooniprobe.test.test.HttpInvalidRequestLine;
import org.openobservatory.ooniprobe.test.test.Ndt;
import org.openobservatory.ooniprobe.utils.FormattingUtils;

@RunWith(AndroidJUnit4.class)
public class PerformanceTest extends MeasurementAbstractTest {

    @Rule
    public FlakyTestRule flakyRule = new FlakyTestRule();

    @Test
    @AllowFlaky(attempts = 3)
    public void testHeaderData() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c));
        Measurement dashMeasurement = testResult.getMeasurement(Dash.NAME);
        String videoQuality = getResourceString(dashMeasurement.getTestKeys().getVideoQuality(false));

        // Ndt values
        Measurement ndtMeasurement = testResult.getMeasurement(Ndt.NAME);
        Integer ndtProtocol = ndtMeasurement.getTestKeys().protocol;
        TestKeys.Summary ndtSummary = ndtMeasurement.getTestKeys().summary;
        TestKeys.Simple ndtSimple = ndtMeasurement.getTestKeys().simple;

        String download = FormattingUtils.getDownload(ndtProtocol, ndtSummary, ndtSimple);
        String downloadUnity = getResourceString(ndtMeasurement.getTestKeys().getDownloadUnit());

        String upload = FormattingUtils.getUpload(ndtProtocol, ndtSummary, ndtSimple);
        String uploadUnity = getResourceString(ndtMeasurement.getTestKeys().getUploadUnit());

        String ping = FormattingUtils.getPing(ndtProtocol, ndtSummary, ndtSimple);

        // Act
        launchDetails(testResult.id);

        // Assert
        // Page 1
        onView(withId(R.id.ping)).check(matches(withText(ping)));
        onView(withId(R.id.video)).check(matches(withText(videoQuality)));
        onView(withId(R.id.downloadUnit)).check(matches(withText(downloadUnity)));
        onView(allOf(withId(R.id.download), withText(download))).check(matches(isDisplayed()));
        onView(withId(R.id.uploadUnit)).check(matches(withText(uploadUnity)));
        onView(allOf(withId(R.id.upload), withText(upload))).check(matches(isDisplayed()));

        // Page 2
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.startTime)).check(matches(withText(FormattingUtils.formatStartTime(testResult.start_time))));
        onView(allOf(withId(R.id.download), withText(testResult.getFormattedDataUsageDown()))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.upload), withText(testResult.getFormattedDataUsageUp()))).check(matches(isDisplayed()));
        onView(withId(R.id.runtime)).check(matches(withText(FormattingUtils.formatRunTime(testResult.getRuntime()))));

        // Page 3
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withText(testResult.network.country_code)).check(matches(isDisplayed()));
        onView(withText(containsString(testResult.network.network_name))).check(matches(isDisplayed()));
        onView(withText(containsString(testResult.network.asn))).check(matches(isDisplayed()));
    }

    @Test
    public void testListOfMeasurements() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c));
        Measurement dashMeasurement = testResult.getMeasurement(Dash.NAME);
        String videoQuality = getResourceString(dashMeasurement.getTestKeys().getVideoQuality(true));
        String notDetected = getResourceString(R.string.TestResults_Overview_MiddleBoxes_NotFound);

        // Ndt values
        Measurement ndtMeasurement = testResult.getMeasurement(Ndt.NAME);
        Integer ndtProtocol = ndtMeasurement.getTestKeys().protocol;
        TestKeys.Summary ndtSummary = ndtMeasurement.getTestKeys().summary;
        TestKeys.Simple ndtSimple = ndtMeasurement.getTestKeys().simple;


        String download = FormattingUtils.getDownload(ndtProtocol, ndtSummary, ndtSimple);
        String downloadUnity = getResourceString(ndtMeasurement.getTestKeys().getDownloadUnit());

        String upload = FormattingUtils.getUpload(ndtProtocol, ndtSummary, ndtSimple);
        String uploadUnity = getResourceString(ndtMeasurement.getTestKeys().getUploadUnit());

        // Act
        launchDetails(testResult.id);

        // Assert
        onData(anything())
            .inAdapterView(withId(R.id.recyclerView))
            .atPosition(0)
            .onChildView(withId(R.id.data1))
            .check(ViewAssertions.matches(withText(download + " " + downloadUnity)));

        onData(anything())
            .inAdapterView(withId(R.id.recyclerView))
            .atPosition(0)
            .onChildView(withId(R.id.data2))
            .check(matches(withText(upload + " " + uploadUnity)));

        onData(anything())
            .inAdapterView(withId(R.id.recyclerView))
            .atPosition(1)
            .onChildView(withId(R.id.data1))
                .check(matches(withText(videoQuality)));

        onData(anything())
            .inAdapterView(withId(R.id.recyclerView))
            .atPosition(2)
            .onChildView(withId(R.id.data1))
                .check(matches(withText(notDetected)));

        onData(anything())
            .inAdapterView(withId(R.id.recyclerView))
            .atPosition(3)
            .onChildView(withId(R.id.data1))
                .check(matches(withText(notDetected)));
    }

    @Test
    public void testNDT() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c));
        Measurement ndtMeasurement = testResult.getMeasurement(Ndt.NAME);
        Integer ndtProtocol = ndtMeasurement.getTestKeys().protocol;
        TestKeys.Summary ndtSummary = ndtMeasurement.getTestKeys().summary;
        TestKeys.Simple ndtSimple = ndtMeasurement.getTestKeys().simple;
        TestKeys.Advanced ndtAdvanced = ndtMeasurement.getTestKeys().advanced;

        String server = ndtMeasurement.getTestKeys().server_name +
                        " - " +
                        ndtMeasurement.getTestKeys().server_country;


        String download = FormattingUtils.getDownload(ndtProtocol, ndtSummary, ndtSimple);
        String downloadUnity = getResourceString(ndtMeasurement.getTestKeys().getDownloadUnit());

        String upload = FormattingUtils.getUpload(ndtProtocol, ndtSummary, ndtSimple);
        String uploadUnity = getResourceString(ndtMeasurement.getTestKeys().getUploadUnit());

        String ping = FormattingUtils.getPing(ndtProtocol, ndtSummary, ndtSimple);

        String averagePing = FormattingUtils.getAveragePing(ndtProtocol, ndtSummary, ndtAdvanced);
        String maxPing = FormattingUtils.getMaxPing(ndtProtocol, ndtSummary, ndtAdvanced);
        String packetLoss = FormattingUtils.getPacketLoss(ndtProtocol, ndtSummary, ndtAdvanced);
        String mss = FormattingUtils.getMss(ndtProtocol, ndtSummary, ndtAdvanced);

        // Act
        launchDetails(testResult.id);
        onData(anything()).inAdapterView(withId(R.id.recyclerView)).atPosition(0).perform(click());

        // Assert
        onView(withText(download + downloadUnity)).check(matches(isDisplayed()));
        onView(withText(upload + uploadUnity)).check(matches(isDisplayed()));
        onView(withText(ping + "ms")).check(matches(isDisplayed()));
        onView(withText(server)).check(matches(isDisplayed()));

        onView(withText(averagePing)).check(matches(isDisplayed()));
        onView(withText(maxPing)).check(matches(isDisplayed()));
        onView(withText(packetLoss)).check(matches(isDisplayed()));
        onView(withText(mss)).check(matches(isDisplayed()));

        assertMeasurementRuntimeAndNetwork(testResult.getMeasurement(Ndt.NAME), testResult.network);
    }

    @Test
    public void testStreaming() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c));
        Measurement dashMeasurement = testResult.getMeasurement(Dash.NAME);
        String videoQuality = getResourceString(dashMeasurement.getTestKeys().getVideoQuality(true));
        String bitrateUnit = getResourceString(dashMeasurement.getTestKeys().getMedianBitrateUnit());
        String bitrate = FormattingUtils.getBitrate(dashMeasurement.getTestKeys().simple);
        String playout = FormattingUtils.getPlayoutDelay(dashMeasurement.getTestKeys().simple);

        // Act
        launchDetails(testResult.id);
        onData(anything()).inAdapterView(withId(R.id.recyclerView)).atPosition(1).perform(click());

        // Assert
        onView(withText(containsString(videoQuality))).check(matches(isDisplayed()));
        onView(withText(bitrate + bitrateUnit)).check(matches(isDisplayed()));
        onView(withText(playout)).check(matches(isDisplayed()));
        assertMeasurementRuntimeAndNetwork(dashMeasurement, testResult.network);
    }

    @Test
    public void testRequestLine() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c));
        Measurement invalidRequest = testResult.getMeasurement(HttpInvalidRequestLine.NAME);

        // Act
        launchDetails(testResult.id);
        onData(anything()).inAdapterView(withId(R.id.recyclerView)).atPosition(2).perform(click());

        // Assert
        onView(withText(getResourceString(R.string.TestResults_Details_Middleboxes_HTTPInvalidRequestLine_NotFound_Hero_Title))).check(matches(isDisplayed()));
        assertMeasurementRuntimeAndNetwork(invalidRequest, testResult.network);
    }

    @Test
    public void testRequestLineDetection() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c), 2, 2);
        Measurement invalidRequest = testResult.getMeasurement(HttpInvalidRequestLine.NAME);

        // Act
        launchDetails(testResult.id);
        onData(anything()).inAdapterView(withId(R.id.recyclerView)).atPosition(2).perform(click());

        // Assert
        onView(withText(getResourceString(R.string.TestResults_Details_Middleboxes_HTTPInvalidRequestLine_Found_Hero_Title))).check(matches(isDisplayed()));
        assertMeasurementRuntimeAndNetwork(invalidRequest, testResult.network);
    }

    @Test
    public void testFieldManipulation() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c));
        Measurement fieldManipulation = testResult.getMeasurement(HttpHeaderFieldManipulation.NAME);

        // Act
        launchDetails(testResult.id);
        onData(anything()).inAdapterView(withId(R.id.recyclerView)).atPosition(3).perform(click());

        // Assert
        onView(withText(getResourceString(R.string.TestResults_Details_Middleboxes_HTTPHeaderFieldManipulation_NotFound_Hero_Title))).check(matches(isDisplayed()));
        assertMeasurementRuntimeAndNetwork(fieldManipulation, testResult.network);
    }

    @Test
    public void testFieldManipulationDetection() {
        // Arrange
        Result testResult = ResultFactory.createAndSave(OONITests.PERFORMANCE.toOONIDescriptor(c), 2, 2);
        Measurement fieldManipulation = testResult.getMeasurement(HttpHeaderFieldManipulation.NAME);

        // Act
        launchDetails(testResult.id);
        onData(anything()).inAdapterView(withId(R.id.recyclerView)).atPosition(3).perform(click());

        // Assert
        onView(withText(getResourceString(R.string.TestResults_Details_Middleboxes_HTTPHeaderFieldManipulation_Found_Hero_Title))).check(matches(isDisplayed()));
        assertMeasurementRuntimeAndNetwork(fieldManipulation, testResult.network);
    }

}
