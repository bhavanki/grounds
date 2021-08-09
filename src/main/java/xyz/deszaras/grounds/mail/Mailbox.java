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

  public Mailbox(Thing t) {
    this.t = Objects.requireNonNull(t);
  }

  public boolean deliver(Missive missive) {
    return t.giveIfNotPresent(Objects.requireNonNull(missive).getThing());
  }

  public boolean delete(Missive missive) {
    return t.takeIfPresent(Objects.requireNonNull(missive).getThing());
  }

  public Optional<Missive> get(int num) {
    checkArgument(num >= 1, "Number must be positive");
    List<Missive> missives = getAllInReverseChronoOrder();
    if (missives.size() > num) {
      return Optional.empty();
    }
    return Optional.of(missives.get(num - 1));
  }

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
