package xyz.deszaras.grounds.script;

import groovy.lang.Script;
import xyz.deszaras.grounds.model.Thing;

public abstract class GroundsScript extends Script {

  private Thing caller;

  public void setCaller(Thing caller) {
    this.caller = caller;
  }

  public void sendMessageToCaller(String message) {
    caller.sendMessage(message);
  }

}
