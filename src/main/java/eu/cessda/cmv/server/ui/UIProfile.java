package eu.cessda.cmv.server.ui;

/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2026 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import eu.cessda.cmv.core.Profile;
import org.springframework.core.io.Resource;

/**
 * Container to hold the parsed profile and its source resource.
 *
 * @param profile the profile.
 * @param resource the resource.
 */
public record UIProfile(Profile profile, Resource resource) {
}
