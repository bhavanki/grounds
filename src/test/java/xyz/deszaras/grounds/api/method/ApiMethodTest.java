package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

@SuppressWarnings({"PMD.TooManyStaticImports", "PMD.EmptyCatchBlock"})
class ApiMethodTest {

  protected ApiMethodContext ctx;
  protected JsonRpcRequest request;
  protected JsonRpcResponse response;

  protected Universe testUniverse;
  protected Actor actor;
  protected Player caller;
  protected Extension extension;
  protected CommandExecutor commandExecutor;

  protected void setUp() {
    testUniverse = new Universe("test");
    Universe.setCurrent(testUniverse);

    actor = new Actor("actor1");
    caller = new Player("caller");
    testUniverse.addThing(caller);
    extension = new Extension("extension1");
    testUniverse.addThing(extension);
    commandExecutor = mock(CommandExecutor.class, RETURNS_DEEP_STUBS);

    ctx = new ApiMethodContext(actor, caller, extension, commandExecutor);
  }

  protected <R> Command<R> mockSuccessfulCommand(Player caller, R result) {
    Command<R> command = mock(Command.class);
    try {
      when(commandExecutor.getCommandFactory()
           .getCommand(eq(actor), eq(caller), any(List.class)))
        .thenReturn(command);
      when(command.execute()).thenReturn(result);
      when(command.getEvents()).thenReturn(Set.<Event>of());
    } catch (Exception e) {
      // won't happen - it's a side effect of Mockito
    }
    return command;
  }

  protected <R> Command<R> mockFactoryFailedCommand(Player caller, String message) {
    Command<R> command = mock(Command.class);
    try {
      when(commandExecutor.getCommandFactory()
           .getCommand(eq(actor), eq(caller), any(List.class)))
        .thenThrow(new CommandFactoryException(message));
      when(command.getEvents()).thenReturn(Set.<Event>of());
    } catch (Exception e) {
      // won't happen - it's a side effect of Mockito
    }
    return command;
  }

  protected <R> Command<R> mockFailedCommand(Player caller, String message) {
    Command<R> command = mock(Command.class);
    try {
      when(commandExecutor.getCommandFactory()
           .getCommand(eq(actor), eq(caller), any(List.class)))
        .thenReturn(command);
      when(command.execute()).thenThrow(new CommandException(message));
      when(command.getEvents()).thenReturn(Set.<Event>of());
    } catch (Exception e) {
      // won't happen - it's a side effect of Mockito
    }
    return command;
  }

  protected Object verifySuccessfulResult() {
    assertEquals(request.getId(), response.getId());
    assertTrue(response.isSuccessful());
    return response.getResult();
  }

  protected JsonRpcResponse.ErrorObject verifyError(int code) {
    assertEquals(request.getId(), response.getId());
    assertFalse(response.isSuccessful());
    assertEquals(code, response.getError().getCode());
    return response.getError();
  }

}
