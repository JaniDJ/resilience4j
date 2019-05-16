/*
 * Copyright 2019 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.circuitbreaker.operator;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.internal.disposables.EmptyDisposable;

class CompletableCircuitBreaker extends Completable {

    private final Completable upstream;
    private final CircuitBreaker circuitBreaker;

    CompletableCircuitBreaker(Completable upstream, CircuitBreaker circuitBreaker) {
        this.upstream = upstream;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    protected void subscribeActual(CompletableObserver downstream) {
        if(circuitBreaker.tryAcquirePermission()){
            upstream.subscribe(new CircuitBreakerCompletableObserver(downstream));
        }else{
            downstream.onSubscribe(EmptyDisposable.INSTANCE);
            downstream.onError(new CallNotPermittedException(circuitBreaker));
        }
    }

    class CircuitBreakerCompletableObserver extends BaseCircuitBreakerObserver implements CompletableObserver {

        private final CompletableObserver downstreamObserver;

        CircuitBreakerCompletableObserver(CompletableObserver downstreamObserver) {
            super(circuitBreaker);
            this.downstreamObserver = downstreamObserver;
        }

        @Override
        protected void hookOnSubscribe() {
            downstreamObserver.onSubscribe(this);
        }

        @Override
        public void onError(Throwable e) {
            whenNotCompleted(() -> {
                super.onError(e);
                downstreamObserver.onError(e);
            });
        }

        @Override
        public void onComplete() {
            whenNotCompleted(() -> {
                super.onSuccess();
                downstreamObserver.onComplete();
            });
        }
    }

}