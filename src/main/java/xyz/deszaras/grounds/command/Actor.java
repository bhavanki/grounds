package xyz.deszaras.grounds.command;

import java.util.LinkedList;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;

public class Actor {

  private final String username;
  private final LinkedList<String> messages;

  private Player currentPlayer;

  public Actor(String username) {
    this.username = username;
    messages = new LinkedList<>();
  }

  public String getUsername() {
    return username;
  }

  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  public void setCurrentPlayer(Player player) {
    currentPlayer = player;
  }

  public void sendMessage(String message) {
    messages.add(Objects.requireNonNull(message));
  }

  public String getNextMessage() {
    return messages.poll();
  }
}
