package org.openobservatory.ooniprobe.common

import android.content.Context
import org.openobservatory.engine.BaseNettest
import org.openobservatory.engine.LoggerArray
import org.openobservatory.engine.OONIRunDescriptor
import org.openobservatory.engine.OONIRunFetchResponse
import org.openobservatory.ooniprobe.BuildConfig
import org.openobservatory.ooniprobe.test.EngineProvider
import org.openobservatory.ooniprobe.test.suite.DynamicTestSuite
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is responsible for managing the test descriptors
 */
@Singleton
class TestDescriptorManager @Inject constructor(private val context: Context) {
    private val descriptors: List<OONIDescriptor<BaseNettest>> = ooniDescriptors(context)

    fun getDescriptors(): List<OONIDescriptor<BaseNettest>> {
        return descriptors
    }

    fun getDescriptorByName(name: String): OONIDescriptor<BaseNettest>? {
        return descriptors.find { it.name == name }
    }

    fun getTestByDescriptorName(name: String): DynamicTestSuite? {
        return getDescriptorByName(name)?.getTest(context)
    }

    /**
     * Fetches the descriptor from the ooni server using the run id.
     * @param runId the run id of the descriptor to fetch
     * @param context the context to use for the request
     */
    fun fetchDescriptorFromRunId(runId: Long, context: Context): OONIRunDescriptor {
        val session = EngineProvider.get().newSession(
            EngineProvider.get().getDefaultSessionConfig(
                context,
                BuildConfig.SOFTWARE_NAME,
                BuildConfig.VERSION_NAME,
                LoggerArray(),
                (context.applicationContext as Application).preferenceManager.proxyURL
            )
        )
        val ooniContext = session.newContextWithTimeout(300)

        val response: OONIRunFetchResponse = session.ooniRunFetch(ooniContext, runId)
        return response.descriptor
    }

    fun addDescriptor(descriptor: OONIRunDescriptor, automatedUpdates: Boolean): Boolean {
        // TODO(aanorbel): persist to database
        return false
    }
}
