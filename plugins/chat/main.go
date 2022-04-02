package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"os"
	"os/signal"
	"runtime"
	"sort"
	"strings"
	"syscall"

	api "github.com/bhavanki/groundsapi"
	"github.com/urfave/cli/v2"
	"golang.org/x/exp/jsonrpc2"
)

const (
	chatMethod              = "chat"
	chatAdminMethod         = "chatadmin"
	chatGuestAutojoinMethod = "chatguestautojoin"
)

type channel struct {
	name      string
	members   []string
	visRoles  []string
	joinRoles []string
}

func (c *channel) fillFromAttr(channelAttr api.Attr) error {
	membersAttr, err := channelAttr.GetAttrInAttrListValue("members")
	if err != nil {
		return err
	}
	if membersAttr.Value != "" {
		c.members = strings.Split(membersAttr.Value, ",")
	}

	visRolesAttr, err := channelAttr.GetAttrInAttrListValue("visRoles")
	if err != nil {
		return err
	}
	if visRolesAttr.Value != "" {
		c.visRoles = strings.Split(visRolesAttr.Value, ",")
	}

	joinRolesAttr, err := channelAttr.GetAttrInAttrListValue("joinRoles")
	if err != nil {
		return err
	}
	if joinRolesAttr.Value != "" {
		c.joinRoles = strings.Split(joinRolesAttr.Value, ",")
	}

	return nil
}

func (c channel) isMember(name string) bool {
	for _, member := range c.members {
		if member == name {
			return true
		}
	}
	return false
}

func (c channel) maySee(name string) bool {
	// TBD: check player roles vs. channel visRoles
	return true
}

func (c channel) mayJoin(name string) bool {
	// TBD: check player roles vs. channel visRoles
	return true
}

func (c *channel) addMember(ctx context.Context, name string, extensionId string, asExtension bool) error {
	c.members = append(c.members, name)
	membersAttr := api.NewStringAttr("members", strings.Join(c.members, ","))
	return api.SetAttrInAttrListValue(ctx, extensionId, c.name, membersAttr, asExtension)
}

func (c *channel) removeMember(ctx context.Context, name string, extensionId string, asExtension bool) error {
	for i, m := range c.members {
		if m == name {
			c.members = append(c.members[:i], c.members[i+1:]...)
			break
		}
	}
	membersAttr := api.NewStringAttr("members", strings.Join(c.members, ","))
	return api.SetAttrInAttrListValue(ctx, extensionId, c.name, membersAttr, asExtension)
}

func channelNotFoundError(channelName string) error {
	return fmt.Errorf("Channel %s not found", channelName)
}

func channelOffLimitsError(channelName string) error {
	return fmt.Errorf("You may not join %s", channelName)
}

func hasChannel(ctx context.Context, channelName string, extensionId string) (bool, error) {
	if _, err := getChannel(ctx, channelName, extensionId); err != nil {
		if err.Error() == channelNotFoundError(channelName).Error() {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

func getChannel(ctx context.Context, channelName string, extensionId string) (*channel, error) {
	if channelName[0] != '#' {
		return nil, errors.New("That is not a valid channel name")
	}
	channelAttr, err := api.GetAttr(ctx, extensionId, channelName, true)
	if err != nil {
		if api.ErrorCode(err) == api.ErrCodeNotFound {
			return nil, channelNotFoundError(channelName)
		}
		return nil, err
	}
	foundChannel := &channel{
		name: channelName,
	}
	err = foundChannel.fillFromAttr(channelAttr)
	if err != nil {
		return nil, err
	}
	return foundChannel, nil
}

func listChannelNames(ctx context.Context, extensionId string) ([]string, error) {
	names, err := api.GetAttrNames(ctx, extensionId, true)
	if err != nil {
		return nil, err
	}
	channelNames := make([]string, 0, len(names))
	for _, n := range names {
		if n[0] == '#' {
			channelNames = append(channelNames, n)
		}
	}
	return channelNames, nil
}

func listVisibleChannelNames(ctx context.Context, callerName string, extensionId string) ([]string, error) {
	channelNames, err := listChannelNames(ctx, extensionId)
	if err != nil {
		return nil, err
	}

	visibleChannelNames := make([]string, 0, len(channelNames))
	for _, channelName := range channelNames {
		channel, err := getChannel(ctx, channelName, extensionId)
		if err != nil {
			continue
		}
		if !channel.maySee(callerName) {
			continue
		}
		visibleChannelNames = append(visibleChannelNames, channelName)
	}

	return visibleChannelNames, nil
}

func handleSay(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 3); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[1]
	channel, err := getChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	if !channel.maySee(callerName) {
		return nil, channelNotFoundError(channelName)
	}
	if !channel.isMember(callerName) {
		return nil, fmt.Errorf("You do not belong to %s", channelName)
	}

	chatMessage := fmt.Sprintf("[%s] %s: %s", channelName, callerName, call.Arguments[2])
	for _, member := range channel.members {
		api.SendMessage(ctx, member, chatMessage)
	}

	return "", nil
}

func handleJoin(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[1]
	channel, err := getChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	if !channel.maySee(callerName) {
		return nil, channelNotFoundError(channelName)
	}
	if !channel.mayJoin(callerName) {
		return nil, channelOffLimitsError(channelName)
	}
	if channel.isMember(callerName) {
		api.SendMessageToCaller(ctx, fmt.Sprintf("You are already a member of %s", channelName))
		return "", nil
	}

	err = channel.addMember(ctx, callerName, call.ExtensionId, true)
	if err != nil {
		return nil, err
	}
	return "", nil
}

func handleLeave(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[1]
	channel, err := getChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	if !channel.maySee(callerName) {
		return nil, channelNotFoundError(channelName)
	}
	if !channel.isMember(callerName) {
		api.SendMessageToCaller(ctx, fmt.Sprintf("You are not a member of %s", channelName))
		return "", nil
	}

	err = channel.removeMember(ctx, callerName, call.ExtensionId, true)
	if err != nil {
		return nil, err
	}
	return "", nil
}

func handleList(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 1); argErr != nil {
		return nil, argErr
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	channelNames, err := listVisibleChannelNames(ctx, callerName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	if len(channelNames) == 0 {
		api.SendMessageToCaller(ctx, "No channels found")
		return "", nil
	}
	sort.Slice(channelNames, func(i, j int) bool {
		return channelNames[i] < channelNames[j]
	})

	err = api.SendMessageWithHeaderToCaller(ctx, strings.Join(channelNames, "\n"), "Channels:")
	if err != nil {
		return nil, err
	}
	return "", nil
}

func handleMine(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 1); argErr != nil {
		return nil, argErr
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	channelNames, err := listVisibleChannelNames(ctx, callerName, call.ExtensionId)
	if err != nil {
		return nil, err
	}
	if len(channelNames) == 0 {
		api.SendMessageToCaller(ctx, "No channels found")
		return "", nil
	}

	myChannelNames := make([]string, 0, len(channelNames))
	for _, n := range channelNames {
		channel, err := getChannel(ctx, n, call.ExtensionId)
		if err != nil {
			return nil, err
		}
		if channel.isMember(callerName) {
			myChannelNames = append(myChannelNames, n)
		}
	}

	sort.Slice(myChannelNames, func(i, j int) bool {
		return myChannelNames[i] < myChannelNames[j]
	})

	err = api.SendMessageWithHeaderToCaller(ctx, strings.Join(myChannelNames, "\n"), "Your channels:")
	if err != nil {
		return nil, err
	}
	return "", nil
}

func handleMembers(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[1]
	channel, err := getChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	if !channel.maySee(callerName) {
		return nil, channelNotFoundError(channelName)
	}
	if !channel.isMember(callerName) {
		api.SendMessageToCaller(ctx, fmt.Sprintf("You are not a member of %s", channelName))
		return "", nil
	}

	if len(channel.members) == 0 {
		err = api.SendMessageToCaller(ctx, "No members found")
	} else {
		err = api.SendMessageWithHeaderToCaller(ctx, strings.Join(channel.members, "\n"),
			fmt.Sprintf("Members of %s", channelName))
	}
	if err != nil {
		return nil, err
	}
	return "", nil
}

// --- ADMIN COMMANDS ---

func handleCreate(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[1]
	channelExists, err := hasChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}
	if channelExists {
		return nil, fmt.Errorf("Channel %s already exists", channelName)
	}

	membersAttr := api.NewStringAttr("members", "")
	visRolesAttr := api.NewStringAttr("visRoles", "")
	joinRolesAttr := api.NewStringAttr("joinRoles", "")
	channel, err := api.NewAttrListAttr(
		channelName,
		[]api.Attr{
			membersAttr,
			visRolesAttr,
			joinRolesAttr,
		},
	)
	if err != nil {
		return nil, err
	}
	err = api.SetAttr(ctx, call.ExtensionId, channel, false)
	if err != nil {
		return nil, err
	}
	api.SendMessageToCaller(ctx, fmt.Sprintf("Created channel %s", channelName))
	return "", nil
}

func handleDelete(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[1]
	channelExists, err := hasChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}
	if !channelExists {
		return nil, fmt.Errorf("Channel %s does not exist", channelName)
	}

	err = api.RemoveAttr(ctx, call.ExtensionId, channelName, false)
	if err != nil {
		return nil, err
	}
	api.SendMessageToCaller(ctx, fmt.Sprintf("Deleted channel %s", channelName))
	return "", nil
}

func handleAddMember(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 3); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[2]
	channel, err := getChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	member := call.Arguments[1]
	if channel.isMember(member) {
		api.SendMessageToCaller(ctx, fmt.Sprintf("%s is already a member of %s", member, channelName))
		return "", nil
	}

	err = channel.addMember(ctx, member, call.ExtensionId, false)
	if err != nil {
		return nil, err
	}
	return "", nil
}

func handleRemoveMember(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 3); argErr != nil {
		return nil, argErr
	}

	channelName := call.Arguments[2]
	channel, err := getChannel(ctx, channelName, call.ExtensionId)
	if err != nil {
		return nil, err
	}

	member := call.Arguments[1]
	if !channel.isMember(member) {
		api.SendMessageToCaller(ctx, fmt.Sprintf("%s is not a member of %s", member, channelName))
		return "", nil
	}

	err = channel.removeMember(ctx, member, call.ExtensionId, false)
	if err != nil {
		return nil, err
	}
	return "", nil
}

// TBD: setVisibility, setJoinability

// --- GUESTAUTOJOIN COMMANDS ---

func handleGuestAutojoin(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 1); argErr != nil {
		return nil, argErr
	}

	payload := make(map[string]interface{})
	err := api.UnmarshalEventPayload(call.Arguments[0], &payload)
	if err != nil {
		return nil, err
	}

	yoinkedThingName := payload["yoinkedThingName"].(string)
	if !strings.HasPrefix(yoinkedThingName, "guest") || payload["yoinkedThingType"] != "Player" {
		return "", nil
	}

	guestChannel, err := getChannel(ctx, "#guest", call.ExtensionId)
	if err != nil {
		return nil, err
	}
	if !guestChannel.isMember(yoinkedThingName) {
		err = guestChannel.addMember(ctx, yoinkedThingName, call.ExtensionId, true)
		if err != nil {
			return nil, err
		}
	}
	return "", nil
}

// TBD: guestautoleave

func main() {
	go func() {
		sigs := make(chan os.Signal, 1)
		signal.Notify(sigs, syscall.SIGQUIT)
		buf := make([]byte, 1<<20)
		for {
			<-sigs
			stacklen := runtime.Stack(buf, true)
			log.Printf("=== received SIGQUIT ===\n*** goroutine dump...\n%s\n*** end\n", buf[:stacklen])
		}
	}()

	ctx := context.Background()

	app := &cli.App{
		Name:  "grounds-chat",
		Usage: "A Grounds plugin for a chat system",
		Action: func(c *cli.Context) error {
			chatHandler := api.PluginHandler{
				Method: chatMethod,
				Dispatcher: func(ctx context.Context, call api.PluginCall) api.SubcommandHandler {
					subcommand := call.Arguments[0]
					switch subcommand {
					case "say":
						return handleSay
					case "join":
						return handleJoin
					case "leave":
						return handleLeave
					case "list":
						return handleList
					case "mine":
						return handleMine
					case "members":
						return handleMembers
					default:
						return nil
					}
				},
			}
			chatadminHandler := api.PluginHandler{
				Method: chatAdminMethod,
				Dispatcher: func(ctx context.Context, call api.PluginCall) api.SubcommandHandler {
					subcommand := call.Arguments[0]
					switch subcommand {
					case "create":
						return handleCreate
					case "delete":
						return handleDelete
					case "add_member":
						return handleAddMember
					case "remove_member":
						return handleRemoveMember
					default:
						return nil
					}
				},
			}
			chatguestautojoinHandler := api.PluginHandler{
				Method: chatGuestAutojoinMethod,
				Dispatcher: func(ctx context.Context, call api.PluginCall) api.SubcommandHandler {
					return handleGuestAutojoin
				},
			}

			// future: server mode
			// conn, err := newConn(ctx, handler)
			// if err != nil {
			// 	log.Fatal(err)
			// }

			req, err := api.ParseJsonRpcRequest(os.Stdin)
			if err != nil {
				return err
			}

			var res *jsonrpc2.Response
			switch req.Method {
			case chatGuestAutojoinMethod:
				res = chatguestautojoinHandler.Handle(ctx, req)
			case chatAdminMethod:
				res = chatadminHandler.Handle(ctx, req)
			default:
				res = chatHandler.Handle(ctx, req)
			}

			b, err := jsonrpc2.EncodeMessage(res)
			if err != nil {
				return err
			}
			// future: server mode
			// if conn != nil {
			// 	// write response to conn
			// } else {
			fmt.Printf("%s", string(b))
			// }
			return nil
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
