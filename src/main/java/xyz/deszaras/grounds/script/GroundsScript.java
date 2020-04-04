package xyz.deszaras.grounds.script;

import groovy.lang.Script;
import java.util.List;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.model.Player;

public abstract class GroundsScript extends Script {

  private Actor actor;
  private Player player;

  public void setActor(Actor actor) {
    this.actor = actor;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void sendMessageToCaller(String message) {
    player.sendMessage(message);
  }

  public CommandResult executeCommand(List<String> commandLine) {
    return new CommandCallable(actor, player, commandLine,
                               CommandExecutor.INSTANCE.getCommandFactory()).call();
  }
}
