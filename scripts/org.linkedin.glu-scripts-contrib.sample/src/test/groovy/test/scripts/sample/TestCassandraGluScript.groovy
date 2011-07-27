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

package test.scripts.sample

import org.linkedin.glu.scripts.testFwk.GluScriptBaseTest
import org.linkedin.util.io.resource.Resource
import org.linkedin.glu.agent.api.ScriptExecutionCauseException
import org.linkedin.groovy.util.concurrent.GroovyConcurrentUtils

/**
 * @author rantav@gmail 
 */
public class TestCassandraGluScript extends GluScriptBaseTest
{
  @Override
  protected void setUp()
  {
    super.setUp()
    initParameters = [
      "skeleton": "http://archive.apache.org/dist/cassandra/0.8.2/apache-cassandra-0.8.2-bin.tar.gz"

      // for local testing (saves download time)
      //"skeleton": "file:///tmp/apache-cassandra-0.8.2-bin.tar.gz"
    ]
  }

  /**
   * Simple happy path test: deploy and undeploy...
   */
  public void testHappyPath()
  {
    deploy()
    undeploy()
  }

  /**
   * Testing step by step
   */
  public void testStepByStep()
  {
    // 1. install the script (do not run the install action!)
    installScript()

    // script was installed... everything should have default values
    assertNull(getExportedScriptFieldValue('serverRoot'))
    assertNull(getExportedScriptFieldValue('logsDir'))
    assertNull(getExportedScriptFieldValue('serverLog'))
    assertNull(getExportedScriptFieldValue('pid'))
    assertNull(getExportedScriptFieldValue('pidFile'))
    assertEquals('NONE', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 2. run install action
    install()
    assertEquals('installed', stateMachineState.currentState)
    assertNull(stateMachineState.error)
    assertNotNull(getExportedScriptFieldValue('serverRoot'))
    assertEquals("all logs", "/var/log/cassandra/", getExportedScriptFieldValue('logsDir'))
    String serverLog = getExportedScriptFieldValue('serverLog')
    assertEquals("log location", "/var/log/cassandra/system.log", serverLog)

    // 3. run configure action
    //actionArgs.configure = [field3Value: 'f3']
    configure()
    assertEquals('stopped', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 4. run the start action
    start()
    assertEquals('running', stateMachineState.currentState)
    assertNull(stateMachineState.error)
    assertNotNull(getExportedScriptFieldValue('pid'))
    assertNotNull(getExportedScriptFieldValue('pidFile'))
    
    // 5. run the stop action
    stop()
    assertEquals('stopped', stateMachineState.currentState)
    assertNull(stateMachineState.error)
    assertNull(getExportedScriptFieldValue('pid'))

    // 6. run the unconfigure action
    unconfigure()
    assertEquals('installed', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 7. run the uninstall action
    uninstall()
    assertEquals('NONE', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 8. uninstall the script
    uninstallScript()
  }
}
