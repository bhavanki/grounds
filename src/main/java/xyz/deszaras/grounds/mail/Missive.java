package xyz.deszaras.grounds.mail;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

  /**
   * Creates a new missive with the given attribute values. A new thing for the
   * missive is created and wrapped.
   *
   * @param  sender     message sender name
   * @param  subject    message subject
   * @param  recipients list of message recipient names (may be empty)
   * @param  timestamp  message timestamp
   * @param  body       message body (may be null)
   * @throws NullPointerException if sender, subject, recipients, or timestamp
   *                              is null
   */
  public Missive(String sender, String subject, List<String> recipients,
                 Instant timestamp, String body) {
    t = new Thing(Objects.requireNonNull(subject));

    t.setAttr(SENDER, Objects.requireNonNull(sender));
    t.setAttr(TIMESTAMP, Objects.requireNonNull(timestamp));
    t.setAttr(READ, false);
    if (recipients != null && !recipients.isEmpty()) {
      t.setAttr(RECIPIENTS, RECIPIENTS_JOINER.join(recipients));
    }
    if (body != null && body.length() > 0) {
      t.setAttr(BODY, body);
    }
  }

  /**
   * Creates a new missive wrapping the given thing. This does not check if
   * the thing has the necessary attributes to serve as a missive.
   *
   * @param  t thing to wrap as missive
   * @throws NullPointerException if t is null
   */
  public Missive(Thing t) {
    this.t = Objects.requireNonNull(t);
  }

  /**
   * Creates a new missive wrapping the given thing. This does not check if
   * the thing has the necessary attributes to serve as a missive.
   *
   * @param  t thing to wrap as missive
   * @return new missive
   * @throws NullPointerException if t is null
   */
  public static Missive of(Thing t) {
    return new Missive(t);
  }

  /**
   * Gets the thing this missive wraps.
   *
   * @return thing
   */
  public Thing getThing() {
    return t;
  }

  /**
   * Gets the sender.
   *
   * @return sender
   * @throws IllegalStateException if the sender is missing
   */
  public String getSender() {
    Attr senderAttr = t.getAttr(SENDER, Attr.Type.STRING)
        .orElseThrow(() -> new IllegalStateException("Missive is missing a sender"));
    return senderAttr.getValue();
  }

  /**
   * Gets the subject.
   *
   * @return subject
   */
  public String getSubject() {
    return t.getName();
  }

  /**
   * Gets the recipients.
   *
   * @return recipients
   */
  public List<String> getRecipients() {
    return t.getAttr(RECIPIENTS, Attr.Type.STRING)
        .map(Attr::getValue)
        .map(s -> RECIPIENTS_SPLITTER.splitToList(s))
        .orElse(ImmutableList.<String>of());
  }

  /**
   * Gets the timestamp.
   *
   * @return sender
   * @throws IllegalStateException if the timestamp is missing
   */
  public Instant getTimestamp() {
    Attr tsAttr = t.getAttr(TIMESTAMP, Attr.Type.TIMESTAMP)
        .orElseThrow(() -> new IllegalStateException("Missive is missing a timestamp"));
    return tsAttr.getInstantValue();
  }

  /**
   * Gets the body.
   *
   * @return body
   */
  public Optional<String> getBody() {
    return t.getAttr(BODY, Attr.Type.STRING).map(Attr::getValue);
  }

  /**
   * Gets the read flag.
   *
   * @return read flag (true == read, false == unread)
   * @throws IllegalStateException if the read flag is missing
   */
  public boolean isRead() {
    Attr readAttr = t.getAttr(READ, Attr.Type.BOOLEAN)
        .orElseThrow(() -> new IllegalStateException("Missive is missing a read flag"));
    return readAttr.getBooleanValue();
  }

  /**
   * Sets the read flag.
   *
   * @param read read flag (true == read, false == unread)
   */
  public void setRead(boolean read) {
    t.setAttr(READ, read);
  }

  /**
   * A comparator for missives that sorts in reverse chronological order.
   * The comparator does not accept nulls.
   *
   * @return reverse chronological order comparator
   */
  public static Comparator<Missive> reverseChronoOrder() {
    return new Comparator<Missive>() {
      @Override
      public int compare(Missive a, Missive b) {
        return b.getTimestamp().compareTo(a.getTimestamp());
      }
    };
  }
}
