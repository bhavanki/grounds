package xyz.deszaras.grounds.mail;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.Comparator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Thing;

/**
 * A missive is a mail message. This class is a convenience wrapper for a
 * {@link Thing} which holds missive data.
 */
public class Missive {

  private static final String SENDER = "sender";
  private static final String RECIPIENTS = "recipients";
  private static final String TIMESTAMP = "timestamp";
  private static final String BODY = "body";
  private static final String READ = "read";

  private static final Joiner RECIPIENTS_JOINER = Joiner.on(",").skipNulls();
  private static final Splitter RECIPIENTS_SPLITTER = Splitter.on(",").omitEmptyStrings();

  private final Thing t;

  Missive(String sender, String subject, List<String> recipients,
          Instant timestamp, String body) {
    t = new Thing(Objects.requireNonNull(subject));

    t.setAttr(SENDER, Objects.requireNonNull(sender));
    t.setAttr(TIMESTAMP, Objects.requireNonNull(timestamp));
    t.setAttr(READ, false);
    if (recipients != null && !recipients.isEmpty()) {
      t.setAttr(RECIPIENTS, RECIPIENTS_JOINER.join(recipients));
    }
    if (body != null) {
      t.setAttr(BODY, body);
    }
  }

  public Missive(Thing t) {
    this.t = Objects.requireNonNull(t);
  }

  public static Missive of(Thing t) {
    return new Missive(t);
  }

  Thing getThing() {
    return t;
  }

  public String getSender() {
    Attr senderAttr = t.getAttr(SENDER, Attr.Type.STRING)
        .orElseThrow(() -> new IllegalStateException("Missive is missing a sender"));
    return senderAttr.getValue();
  }

  public String getSubject() {
    return t.getName();
  }

  public Optional<List<String>> getRecipients() {
    return t.getAttr(RECIPIENTS, Attr.Type.STRING)
        .map(Attr::getValue)
        .map(s -> RECIPIENTS_SPLITTER.splitToList(s));
  }

  public Instant getTimestamp() {
    Attr tsAttr = t.getAttr(TIMESTAMP, Attr.Type.TIMESTAMP)
        .orElseThrow(() -> new IllegalStateException("Missive is missing a timestamp"));
    return tsAttr.getInstantValue();
  }

  public Optional<String> getBody() {
    return t.getAttr(BODY, Attr.Type.STRING).map(Attr::getValue);
  }

  public boolean isRead() {
    Attr readAttr = t.getAttr(READ, Attr.Type.BOOLEAN)
        .orElseThrow(() -> new IllegalStateException("Missive is missing a read flag"));
    return readAttr.getBooleanValue();
  }

  public void setRead(boolean read) {
    t.setAttr(READ, read);
  }

  public static Comparator<Missive> reverseChronoOrder() {
    return new Comparator<Missive>() {
      @Override
      public int compare(Missive a, Missive b) {
        return b.getTimestamp().compareTo(a.getTimestamp());
      }
    };
  }
}
