package xyz.deszaras.grounds.command;

import java.util.LinkedList;
import java.util.Objects;

public class Actor {

  private final LinkedList<String> messages;

  public Actor() {
    messages = new LinkedList<>();
  }

  public void sendMessage(String message) {
    messages.add(Objects.requireNonNull(message));
  }

  public String getNextMessage() {
    return messages.poll();
  }
}
