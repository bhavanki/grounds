package xyz.deszaras.grounds.mail;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * A mailbox holds missives. This class is a convenience wrapper for a
 * {@link Thing} which holds mailbox data.
 */
public class Mailbox {

  private final Thing t;

  /**
   * Creates a new mailbox wrapping the given thing.
   *
   * @param  t thing to wrap as mailbox
   * @throws NullPointerException if t is null
   */
  public Mailbox(Thing t) {
    this.t = Objects.requireNonNull(t);
  }

  /**
   * Delivers a new message to this mailbox.
   *
   * @param  missive message to deliver
   * @return         true if delivered, false if already present
   */
  public boolean deliver(Missive missive) {
    return t.giveIfNotPresent(Objects.requireNonNull(missive).getThing());
  }

  /**
   * Deletes an existing message from this mailbox.
   *
   * @param  missive message to delete
   * @return         true if deleted, false if not present
   */
  public boolean delete(Missive missive) {
    return t.takeIfPresent(Objects.requireNonNull(missive).getThing());
  }

  /**
   * Gets the size (number of messages) in this mailbox.
   *
   * @return mailbox size
   */
  public int size() {
    return t.getContents().size();
  }

  /**
   * Gets the message at the given index number, where messages are indexed in
   * reverse chronological order.
   *
   * @param  num message index number
   * @return     message, or empty if there are not enough messages
   * @throws IllegalArgumentException if num is non-positive
   */
  public Optional<Missive> get(int num) {
    checkArgument(num >= 1, "Number must be positive");
    List<Missive> missives = getAllInReverseChronoOrder();
    if (missives.size() < num) {
      return Optional.empty();
    }
    return Optional.of(missives.get(num - 1));
  }

  /**
   * Deletes the message at the given index number, where messages are indexed
   * in reverse chronological order.
   *
   * @param  num message index number
   * @throws IllegalArgumentException if num is non-positive or is larger than
   *         the number of available messages
   */
  public void delete(int num) {
    checkArgument(num >= 1, "Number must be positive");
    checkArgument(num <= size(), "Number must be no more than " + size());
    List<Missive> missives = getAllInReverseChronoOrder();
    delete(missives.get(num - 1));
  }

  /**
   * Gets all messages in this mailbox in reverse chronological order.
   *
   * @return messages in reverse chronological order
   */
  public List<Missive> getAllInReverseChronoOrder() {
    Universe currentUniverse = Universe.getCurrent();
    // this assumes that all missive things are in the universe ...
    return t.getContents().stream()
        .map(id -> currentUniverse.getThing(id).get())
        .map(Missive::of)
        .sorted(Missive.reverseChronoOrder())
        .collect(Collectors.toList());
  }
}
