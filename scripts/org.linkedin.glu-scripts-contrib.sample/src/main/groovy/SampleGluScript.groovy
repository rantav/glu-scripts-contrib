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

/**
 * This is a sample glu script. The purpose of this script is to demonstrates some of the features
 * that can be used in a glu script.
 */
class SampleGluScript
{
  // this is how you express a dependency on a given agent version (it is a min requirement, meaning
  // if the agent is at least v3.1.0 then this glu script can run in it
  static requires = {
    agent(version: '3.1.0')
  }

  /**
   * The version of *this* glu script
   */
  def version = '1.0.0'

  /**
   * This field is 'exported' to ZooKeeper
   */
  def field1

  /**
   * This field is NOT 'exported' to ZooKeeper because it is marked transient
   */
  transient def field2

  /**
   * This field is 'exported' to ZooKeeper
   */
  def field3

  /**
   * The root directory where the script will write some files (will be 'exported')
   */
  def rootDir

  /**
   * Counter for the monitor
   */
  int monitorCounter = 0

  /**
   * install action
   */
  def install = {
    // log allows you to log any log message (shows in the agent log file)
    log.info "installing..."

    // params is the map that contains all initParameters
    field1 = "from install [exported] [${params.field1Value}]"
    field2 = "from install [not exported]"

    // the mountPoint variable points to where the script was 'mounted' in the agent
    // since mountPoint is unique, then it is a good place to use as the root directory
    rootDir = shell.toResource(mountPoint)

    log.info "install completed."
  }

  /**
   * configure action
   * In this action we are demonstrating the use of arguments provided to the specific action (which
   * may be <code>null</code>!)
   */
  def configure = { args ->

    // args are specific to a given action only (see unit test)

    // params is accessible in ALL actions
    field1 = "from configure [exported] [${params.field1Value}]"
    field2 = "from configure [not exported]"

    if(args?.failMessage)
      // if something goes wrong, you can simply 'fail' your script by calling shell.fail(...)
      shell.fail(args.failMessage)

    if(args?.exceptionMessage)
      // this also simulates what happens if <any> exception is thrown
      throw new Exception(args.exceptionMessage)

    field3 = args?.field3Value

    // if there is a readme message, use the convenient shell.saveContent call to generate a file
    if(params.readmeMessage)
      shell.saveContent(rootDir.'readme.txt', params.readmeMessage)

    // note: in glu the Resource type is 'enhanced' and the notation <resource>."xxx" will create
    // a relative resource and is 100%  equivalent to calling resource.createRelative("xxx")
  }

  /**
   * start action
   */
  def start = {
    field1 = "from start [exported]"
    field2 = "from start [not exported]"

    // conditionnally starting a timer
    if(params.monitorRepeatFrequency)
    {
      monitorCounter = 0 // resetting the monitor counter
      timers.schedule(timer: monitor, repeatFrequency: params.monitorRepeatFrequency)
    }
  }

  /**
   * stop action
   */
  def stop = {
    field1 = "from stop [exported]"
    field2 = "from stop [not exported]"

    // no need to repeat the test... it will cancel the timer whether there was one or not
    timers.cancel(timer: monitor)
  }

  /**
   * unconfigure action
   */
  def unconfigure = {
    field1 = "from unconfigure [exported]"
    field2 = "from unconfigure [not exported]"
  }

  /**
   * uninstall action
   */
  def uninstall = {
    field1 = "from uninstall [exported]"
    field2 = "from uninstall [not exported]"
  }

  /**
   * A monitor which will run on a regular basis. The implementation will simply increment a counter
   * every time the monitor runs and when it reaches a given limit, it will force an error
   */
  def monitor = {
    // using ?: groovy shortcut notation to define a default value in case none is provided
    def limit = params.monitorCounterLimit ?: Integer.MAX_VALUE

    if(!state.error)
    {
      if(monitorCounter > limit)
        return

      monitorCounter++

      if(monitorCounter == limit)
      {
        log.warn "monitor limit reached... forcing error state"
        stateManager.forceChangeState(state.currentState, 'monitor limit reached')
      }
      else
      {
        log.info "Running monitor for the ${monitorCounter} time."
      }
    }
  }
}