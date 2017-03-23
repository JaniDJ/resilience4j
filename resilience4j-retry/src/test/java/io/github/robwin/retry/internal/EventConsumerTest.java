/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.retry.internal;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import javax.xml.ws.WebServiceException;

import io.github.robwin.retry.Retry;
import io.github.robwin.retry.RetryConfig;
import io.github.robwin.retry.event.RetryEvent;
import io.github.robwin.test.HelloWorldService;
import io.reactivex.subscribers.TestSubscriber;
import javaslang.control.Try;

public class EventConsumerTest {


    private HelloWorldService helloWorldService;
    private long sleptTime = 0L;

    @Before
    public void setUp(){
        helloWorldService = Mockito.mock(HelloWorldService.class);
        RetryContext.sleepFunction = sleep -> sleptTime += sleep;
    }

    @Test
    public void shouldReturnAfterThreeAttempts() {
        // Given the HelloWorldService throws an exception
        BDDMockito.willThrow(new WebServiceException("BAM!")).given(helloWorldService).sayHelloWorld();

        // Create a Retry with default configuration
        RetryContext retryContext = (RetryContext) Retry.ofDefaults("id");
        TestSubscriber<RetryEvent.Type> testSubscriber = retryContext.getEventStream()
                .map(RetryEvent::getEventType)
                .test();
        // Decorate the invocation of the HelloWorldService
        Try.CheckedRunnable retryableRunnable = Retry.decorateCheckedRunnable(retryContext, helloWorldService::sayHelloWorld);

        // When
        Try<Void> result = Try.run(retryableRunnable);

        // Then the helloWorldService should be invoked 3 times
        BDDMockito.then(helloWorldService).should(Mockito.times(3)).sayHelloWorld();
        // and the result should be a failure
        Assertions.assertThat(result.isFailure()).isTrue();
        // and the returned exception should be of type RuntimeException
        Assertions.assertThat(result.failed().get()).isInstanceOf(WebServiceException.class);
        Assertions.assertThat(sleptTime).isEqualTo(RetryConfig.DEFAULT_WAIT_DURATION*2);

        testSubscriber.assertValueCount(1).assertValues(RetryEvent.Type.ERROR);
    }

    @Test
    public void shouldReturnAfterTwoAttempts() {
        // Given the HelloWorldService throws an exception
        BDDMockito.willThrow(new WebServiceException("BAM!")).willNothing().given(helloWorldService).sayHelloWorld();

        // Create a Retry with default configuration
        RetryContext retryContext = (RetryContext) Retry.ofDefaults("id");
        TestSubscriber<RetryEvent.Type> testSubscriber = retryContext.getEventStream()
                .map(RetryEvent::getEventType)
                .test();
        // Decorate the invocation of the HelloWorldService
        Try.CheckedRunnable retryableRunnable = Retry.decorateCheckedRunnable(retryContext, helloWorldService::sayHelloWorld);

        // When
        Try<Void> result = Try.run(retryableRunnable);

        // Then the helloWorldService should be invoked 2 times
        BDDMockito.then(helloWorldService).should(Mockito.times(2)).sayHelloWorld();
        // and the result should be a sucess
        Assertions.assertThat(result.isSuccess()).isTrue();
        Assertions.assertThat(sleptTime).isEqualTo(RetryConfig.DEFAULT_WAIT_DURATION);

        testSubscriber.assertValueCount(1).assertValues(RetryEvent.Type.SUCCESS);
    }
}