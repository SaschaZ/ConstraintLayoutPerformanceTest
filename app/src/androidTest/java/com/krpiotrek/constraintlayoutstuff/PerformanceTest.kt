package com.krpiotrek.constraintlayoutstuff

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.krpiotrek.constraintlayoutstuff.PerformanceTestActivity.Companion.DEFAULT_TEST_RUNS_PER_LAYOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PerformanceTest {

    @Test
    fun test() = runBlocking {
        val channel = Channel<Any>(RENDEZVOUS)
        launch(PerformanceTestActivity::class.java).onActivity {
            CoroutineScope(Dispatchers.Main).launch {
                it.runTests(DEFAULT_TEST_RUNS_PER_LAYOUT)
                channel.send(true)
            }
        }
        channel.receive()
        Unit
    }
}
