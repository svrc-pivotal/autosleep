/*
 * Autosleep
 * Copyright (C) 2016 Orange
 * Authors: Benjamin Einaudi   benjamin.einaudi@orange.com
 *          Arnaud Ruffin      arnaud.ruffin@orange.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.autosleep.ui.servicebroker.service.parameters;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.cloudfoundry.autosleep.config.Config;
import org.cloudfoundry.autosleep.config.Config.ServiceInstanceParameters;
import org.cloudfoundry.autosleep.config.Config.ServiceInstanceParameters.Enrollment;
import org.cloudfoundry.autosleep.config.EnrollmentConfig;
import org.cloudfoundry.autosleep.config.EnrollmentConfig.EnrollmentParameters.EnrollmentState;
import org.cloudfoundry.autosleep.ui.servicebroker.service.InvalidParameterException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ParameterReaderFactory {

    @Bean(name = Config.ServiceInstanceParameters.AUTO_ENROLLMENT)
    public ParameterReader<Enrollment> buildAutoEnrollmentReader() {
        return new ParameterReader<Enrollment>() {

            @Override
            public String getParameterName() {
                return Config.ServiceInstanceParameters.AUTO_ENROLLMENT;
            }

            @Override
            public Enrollment readParameter(Object parameter,
                                                                             boolean withDefault)
                    throws InvalidParameterException {
                if (parameter != null) {
                    String autoEnrollment = (String) parameter;
                    log.debug("forcedAutoEnrollment " + autoEnrollment);
                    try {
                        return Enrollment.valueOf(autoEnrollment);
                    } catch (IllegalArgumentException i) {
                        String availableValues = String.join(", ",
                                Arrays.asList(Enrollment.values()).stream()
                                        .map(Enrollment::name)
                                        .collect(Collectors.toList()));
                        log.error("Wrong value for auto enrollment  - choose one between [{}]", availableValues);
                        throw new InvalidParameterException(Config.ServiceInstanceParameters.AUTO_ENROLLMENT,
                                "choose one between: " + availableValues);
                    }
                } else if (withDefault) {
                    return Enrollment.standard;
                } else {
                    return null;
                }
            }
        };
    }

    @Bean(name = Config.ServiceInstanceParameters.EXCLUDE_FROM_AUTO_ENROLLMENT)
    public ParameterReader<Pattern> buildExcludeFromAutoEnrollmentReader() {
        return new ParameterReader<Pattern>() {

            @Override
            public String getParameterName() {
                return Config.ServiceInstanceParameters.EXCLUDE_FROM_AUTO_ENROLLMENT;
            }

            @Override
            public Pattern readParameter(Object parameter, boolean withDefault) throws
                    InvalidParameterException {
                if (parameter != null) {
                    String excludeNamesStr = (String) parameter;
                    if (!excludeNamesStr.trim().equals("")) {
                        log.debug("excludeFromAutoEnrollment " + excludeNamesStr);
                        try {
                            return Pattern.compile(excludeNamesStr);
                        } catch (PatternSyntaxException p) {
                            log.error("Wrong format for exclusion  - format cannot be compiled to a valid regexp");
                            throw new InvalidParameterException(
                                    Config.ServiceInstanceParameters.EXCLUDE_FROM_AUTO_ENROLLMENT,
                                    "should be a valid regexp");
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
    }

    @Bean(name = Config.ServiceInstanceParameters.IDLE_DURATION)
    public ParameterReader<Duration> buildIdleDurationReader() {
        return new ParameterReader<Duration>() {

            @Override
            public String getParameterName() {
                return Config.ServiceInstanceParameters.IDLE_DURATION;
            }

            @Override
            public Duration readParameter(Object parameter, boolean withDefault) throws
                    InvalidParameterException {
                if (parameter != null) {
                    String inactivityPattern = (String) parameter;
                    log.debug("pattern " + inactivityPattern);
                    try {
                        return Duration.parse(inactivityPattern);
                    } catch (DateTimeParseException e) {
                        log.error("Wrong format for inactivity duration - format should respect ISO-8601 duration "
                                + "format "
                                + "PnDTnHnMn");
                        throw new InvalidParameterException(Config.ServiceInstanceParameters.IDLE_DURATION,
                                "param badly formatted (ISO-8601). Example: \"PT15M\" for 15mn");
                    }
                } else if (withDefault) {
                    return Config.DEFAULT_INACTIVITY_PERIOD;
                } else {
                    return null;
                }
            }

        };
    }

    @Bean(name = ServiceInstanceParameters.IGNORE_ROUTE_SERVICE_ERROR)
    public ParameterReader<Boolean> buildIgnoreRouteServiceErrorReader() {
        return new ParameterReader<Boolean>() {

            @Override
            public String getParameterName() {
                return Config.ServiceInstanceParameters.IGNORE_ROUTE_SERVICE_ERROR;
            }

            @Override
            public Boolean readParameter(Object parameter, boolean withDefault)
                    throws InvalidParameterException {
                if (parameter != null) {
                    String ignoreRouteError = ((String) parameter).toLowerCase();
                    log.debug("ignoreRouteServiceError " + ignoreRouteError);
                    if (parameter.equals(Boolean.TRUE.toString().toLowerCase())) {
                        return Boolean.TRUE;
                    } else if (parameter.equals(Boolean.FALSE.toString().toLowerCase())) {
                        return Boolean.FALSE;
                    } else {
                        throw new InvalidParameterException(Config.ServiceInstanceParameters.IGNORE_ROUTE_SERVICE_ERROR,
                                "bad parameter value. Must be a boolean value.");
                    }
                } else {
                    return Config.DEFAULT_IGNORE_SERVICE_ERROR;
                }
            }
        };
    }

    @Bean(name = Config.ServiceInstanceParameters.SECRET)
    public ParameterReader<String> buildSecretReader() {

        return new ParameterReader<String>() {

            @Override
            public String getParameterName() {
                return Config.ServiceInstanceParameters.SECRET;
            }

            @Override
            public String readParameter(Object parameter, boolean withDefault) throws
                    InvalidParameterException {
                if (parameter != null) {
                    return String.class.cast(parameter);
                } else {
                    return null;
                }
            }
        };
    }
    
    @Bean(name = EnrollmentConfig.EnrollmentParameters.STATE)
    public ParameterReader<EnrollmentState> buildEnrollmentStateReader() {
        return new ParameterReader<EnrollmentState>() {

            @Override
            public String getParameterName() {
                return EnrollmentConfig.EnrollmentParameters.STATE;
            }

            @Override
            public EnrollmentState readParameter(Object parameter,
                                                                             boolean withDefault)
                    throws InvalidParameterException {
                if (parameter != null) {
                    String state = (String) parameter;
                    log.debug("state " + state);
                    try {
                        return EnrollmentState.valueOf(state);
                    } catch (IllegalArgumentException i) {
                        String availableValues = String.join(", ",
                                Arrays.asList(EnrollmentState.values()).stream()
                                        .map(EnrollmentState::name)
                                        .collect(Collectors.toList()));
                        log.error("Wrong value for state  - choose one between [{}]", availableValues);
                        throw new InvalidParameterException(EnrollmentConfig.EnrollmentParameters.STATE,
                                "choose one between: " + availableValues);
                    }
                } else if (withDefault) {
                    return EnrollmentState.enrolled;
                } else {
                    return null;
                }
            }
        };
    }

}
