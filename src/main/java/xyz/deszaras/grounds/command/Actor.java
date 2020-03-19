package xyz.deszaras.grounds.command;

import java.util.LinkedList;
import java.util.Objects;

public class Actor {

  private final String username;
  private final LinkedList<String> messages;

  public Actor(String username) {
    this.username = username;
    messages = new LinkedList<>();
  }

  public String getUsername() {
    return username;
  }

  public void sendMessage(String message) {
    messages.add(Objects.requireNonNull(message));
  }

  public String getNextMessage() {
    return messages.poll();
  }
}
