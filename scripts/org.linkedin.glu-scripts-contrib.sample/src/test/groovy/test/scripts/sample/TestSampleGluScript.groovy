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
 * @author yan@pongasoft.com */
public class TestSampleGluScript extends GluScriptBaseTest
{
  @Override
  protected void setUp()
  {
    super.setUp()
    initParameters = [
      field1Value: 'f1',
      readmeMessage: 'configure readme message'
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
    assertNull(getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2'))
    assertNull(getExportedScriptFieldValue('field3'))
    assertNull(getExportedScriptFieldValue('rootDir'))
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('NONE', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 2. run install action
    install()
    assertEquals("from install [exported] [f1]", getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2')) // never exported
    assertNull(getExportedScriptFieldValue('field3'))
    Resource rootDir = getExportedScriptFieldValue('rootDir')
    assertNotNull(rootDir)
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('installed', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 3. run configure action
    actionArgs.configure = [field3Value: 'f3']
    assertFalse(rootDir.'readme.txt'.exists()) // before executing the action, the file has not been created
    configure()
    assertEquals("from configure [exported] [f1]", getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2')) // never exported
    assertEquals('f3', getExportedScriptFieldValue('field3'))
    assertEquals('configure readme message', rootDir.'readme.txt'.file.text)
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('stopped', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 4. run the start action
    start()
    assertEquals("from start [exported]", getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2')) // never exported
    assertEquals('f3', getExportedScriptFieldValue('field3'))
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('running', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 5. run the stop action
    stop()
    assertEquals("from stop [exported]", getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2')) // never exported
    assertEquals('f3', getExportedScriptFieldValue('field3'))
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('stopped', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 6. run the unconfigure action
    unconfigure()
    assertEquals("from unconfigure [exported]", getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2')) // never exported
    assertEquals('f3', getExportedScriptFieldValue('field3'))
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('installed', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 7. run the uninstall action
    uninstall()
    assertEquals("from uninstall [exported]", getExportedScriptFieldValue('field1'))
    assertNull(getExportedScriptFieldValue('field2')) // never exported
    assertEquals('f3', getExportedScriptFieldValue('field3'))
    assertEquals(0, getExportedScriptFieldValue('monitorCounter'))
    assertEquals('NONE', stateMachineState.currentState)
    assertNull(stateMachineState.error)

    // 8. uninstall the script
    assertTrue(rootDir.'readme.txt'.exists()) // before executing the action, the file should still exist
    uninstallScript()
    assertFalse(rootDir.'readme.txt'.exists()) // cleaned automatically by glu
  }

  /**
   * Demonstrate a test failure and how to test for it
   */
  public void testFailure()
  {
    installScript()
    install()

    // 1. force the script to fail (shell.fail)
    actionArgs.configure = [failMessage: 'should fail!']
    ScriptExecutionCauseException cause = scriptShouldFail {
      configure()
    }
    assertEquals("[org.linkedin.glu.agent.api.ScriptFailedException]: should fail!", cause.message)

    // the state should have remained 'installed'
    assertEquals('installed', stateMachineState.currentState)
    // but the state machine is now in error
    assertEquals("[org.linkedin.glu.agent.api.ScriptFailedException]: should fail!",
                 stateMachineState.error.cause.message)

    // we are clearing the error (required to move forward!)
    clearError()
    assertNull(stateMachineState.error)

    // 2. force the script to throw an exception
    actionArgs.configure = [exceptionMessage: 'should throw exception!']
    cause = scriptShouldFail {
      configure()
    }
    assertEquals("[java.lang.Exception]: should throw exception!", cause.message)
    // the state should have remained 'installed'
    assertEquals('installed', stateMachineState.currentState)
    // but the state machine is now in error
    assertEquals("[java.lang.Exception]: should throw exception!",
                 stateMachineState.error.cause.message)

    // we are clearing the error (required to move forward!)
    clearError()
    assertNull(stateMachineState.error)

    // this time everything should be back to normal
    actionArgs.configure = [:]
    configure()
    assertEquals('stopped', stateMachineState.currentState)
    assertNull(stateMachineState.error)
  }

  /**
   * Demonstrate the monitor feature
   */
  public void testMonitor()
  {
    initParameters.monitorRepeatFrequency = '250' // 250ms
    initParameters.monitorCounterLimit = 4 // (monitor will run 4 times until it raises an error)

    installScript()
    install()
    configure()
    start()

    // at this stage the monitor should be running... we are going to wait until it triggers an
    // error
    GroovyConcurrentUtils.waitForCondition(clock, '10s', '50') {
      stateMachineState.error != null
    }

    // the monitor did not change the state... only added an error
    assertEquals('running', stateMachineState.currentState)
    assertEquals('monitor limit reached', stateMachineState.error)

    // at this time the counter should be set to 4
    assertEquals(4, getExportedScriptFieldValue('monitorCounter'))

    clearError()
    stop()
    unconfigure()
    uninstall()
    uninstallScript()
  }
}