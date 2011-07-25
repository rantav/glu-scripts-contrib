/*
 * Copyright (c) 2011 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

spec = [
    name: 'glu-scripts-contrib',
    group: 'org.linkedin',
    version: '1.0.0',

    versions: [
      glu: '3.1.0',
      groovy: '1.7.5',
      linkedinUtils: '1.7.0'
    ],

    // information about the build framework itself
    build: [
        type: "gradle",
        version: "0.9",
        uri: "http://gradle.artifactoryonline.com/gradle/distributions/gradle-0.9-all.zip",
        commands: [
            "snapshot": "gradle release",
            "release": "gradle -Prelease=true release"
        ]
    ]
]

spec.scmUrl = "git@github.com:linkedin/${spec.name}.git"

/**
 * External dependencies
 */
spec.external = [
  gluAgentAPI: "org.linkedin:org.linkedin.glu.agent-api:${spec.versions.glu}",
  gluScriptsTestFwk: "org.linkedin:org.linkedin.glu.scripts-test-fwk:${spec.versions.glu}",
  groovy: "org.codehaus.groovy:groovy:${spec.versions.groovy}",
  junit: 'junit:junit:4.4',
  linkedinUtilsCore: "org.linkedin:org.linkedin.util-core:${spec.versions.linkedinUtils}",
  linkedinUtilsGroovy: "org.linkedin:org.linkedin.util-groovy:${spec.versions.linkedinUtils}"
]
