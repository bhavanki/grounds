package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

/**
 * A JLine completer for player command input.
 */
public class CommandCompleter implements Completer {

  private static final Set<Candidate> COMMAND_CANDIDATES =
      CommandExecutor.COMMANDS.keySet().stream()
          .map(s -> new Candidate(s.toLowerCase()))
          .collect(Collectors.toSet());
  private static final Set<Candidate> ACTOR_COMMAND_CANDIDATES =
      ActorCommand.ACTOR_COMMANDS.keySet().stream()
          .map(s -> new Candidate(s.toLowerCase()))
          .collect(Collectors.toSet());
  private static final Set<Candidate> COMBAT_COMMAND_CANDIDATES =
      CombatCommand.COMBAT_COMMANDS.keySet().stream()
          .map(s -> new Candidate(s.toLowerCase()))
          .collect(Collectors.toSet());
  private static final Set<Candidate> MAIL_COMMAND_CANDIDATES =
      MailCommand.MAIL_COMMANDS.keySet().stream()
          .map(s -> new Candidate(s.toLowerCase()))
          .collect(Collectors.toSet());
  private static final Set<Candidate> ROLE_COMMAND_CANDIDATES =
      RoleCommand.ROLE_COMMANDS.keySet().stream()
          .map(s -> new Candidate(s.toLowerCase()))
          .collect(Collectors.toSet());

  @Override
  public void complete(LineReader lineReader, ParsedLine line,
                       List<Candidate> candidates) {
    switch (line.wordIndex()) {
      case 0:
        candidates.addAll(COMMAND_CANDIDATES);
        return;
      case 1:
        switch (line.words().get(0).toLowerCase()) {
          case "actor":
            candidates.addAll(ACTOR_COMMAND_CANDIDATES);
            return;
          case "c":
          case "combat":
            candidates.addAll(COMBAT_COMMAND_CANDIDATES);
            return;
          case "mail":
            candidates.addAll(MAIL_COMMAND_CANDIDATES);
            return;
          case "role":
            candidates.addAll(ROLE_COMMAND_CANDIDATES);
            return;
        }
        return;
    }
  }
}
