/*
 *
 *  Copyright 2015 Robert Winkler
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
package io.github.robwin.circuitbreaker.event;

/**
 * A CircuitBreakerEvent which informs that an error has been recorded
 */
public class CircuitBreakerOnErrorEvent extends AbstractCircuitBreakerEvent{

    private final Throwable throwable;

    public CircuitBreakerOnErrorEvent(String circuitBreakerName, Throwable throwable) {
        super(circuitBreakerName);
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public Type getEventType() {
        return Type.ERROR;
    }

    @Override
    public String toString(){
        return String.format("%s: CircuitBreaker '%s' recorded an error: '%s'", getCreationTime(), getCircuitBreakerName(), throwable.toString());
    }
}