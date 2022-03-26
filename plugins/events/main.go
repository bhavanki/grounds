package main

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"os"
	"os/signal"
	"runtime"
	"sort"
	"strconv"
	"syscall"
	"time"

	"github.com/araddon/dateparse"
	"github.com/bhavanki/grounds-events/pkg/api"
	"github.com/urfave/cli/v2"
	"golang.org/x/exp/jsonrpc2"
)

func getEventName(n string) string {
	return fmt.Sprintf("#%s", n)
}

type event struct {
	name string
	owner string
	description string
	startTime time.Time
	location string
}

func (e *event) fillFromAttr(eventAttr api.Attr, tzLocation *time.Location) error {
	as := make([]api.Attr, 0)
	err := json.Unmarshal([]byte(eventAttr.Value), &as)
	if err != nil {
		return err
	}
	for _, a := range as {
		switch a.Name {
		case "startTimestamp":
			startTimeSec, err := strconv.ParseInt(a.Value, 10, 64)
			if err != nil {
				return err
			}
			e.startTime = time.Unix(startTimeSec, 0).In(tzLocation)
		case "owner":
			e.owner = a.Value
		case "description":
			e.description = a.Value
		case "location":
			e.location = a.Value
		}
	}
	return nil
}

func handleCreateEvent(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 5); argErr != nil {
		return nil, argErr
	}

	eventName := getEventName(call.Arguments[1])
	_, err := api.GetAttr(ctx, call.ExtensionId, eventName, true)
	if err == nil {
		return nil, fmt.Errorf("Event '%s' already exists", eventName)
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	callerTZ, err := api.GetCallerTZ(ctx)
	if err != nil {
		return nil, err
	}
	tzLocation, err := time.LoadLocation(callerTZ)
	if err != nil {
		tzLocation = time.UTC
	}
	startTime, err := dateparse.ParseIn(call.Arguments[3], tzLocation)
	if err != nil {
		return nil, err
	}

	newEvent := event{
		name: eventName,
		owner: callerName,
		description: call.Arguments[2],
		startTime: startTime,
		location: call.Arguments[4],  // fixme should be Place / Thing
	}
	eventAttr, err := api.NewAttrListAttr(
		newEvent.name,
		[]api.Attr{
			api.NewStringAttr("description", newEvent.description),
			api.NewTimestampAttr("startTimestamp", newEvent.startTime),
			api.NewStringAttr("location", newEvent.location),
			api.NewStringAttr("owner", callerName),
		})
	if err != nil {
		return nil, err
	}

	err = api.SetAttr(ctx, call.ExtensionId, eventAttr, true)
	if err != nil {
		return nil, err
	}

	err = api.SendMessageToCaller(ctx, fmt.Sprintf("Created event '%s'", call.Arguments[1]))
	if err != nil {
		return nil, err
	}

	return "", nil
}

func handleListEvents(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 1); argErr != nil {
		return nil, argErr
	}

	callerTZ, err := api.GetCallerTZ(ctx)
	if err != nil {
		return nil, err
	}
	tzLocation, err := time.LoadLocation(callerTZ)
	if err != nil {
		tzLocation = time.UTC
	}

	attrNames, err := api.GetAttrNames(ctx, call.ExtensionId, true)
	if err != nil {
		return nil, err
	}
	var eventAttrNames []string
	for _, n := range attrNames {
		if n[0] == '#' {
			eventAttrNames = append(eventAttrNames, n)
		}
	}
	if len(eventAttrNames) == 0 {
		err = api.SendMessageToCaller(ctx, "No events found")
		if err != nil {
			return nil, err
		}
		return "", nil
	}

	events := make([]event, 0, len(eventAttrNames))
	for _, eventAttrName := range eventAttrNames {
		eventAttr, err := api.GetAttr(ctx, call.ExtensionId, eventAttrName, true)
		if err != nil {
			return nil, err
		}
		foundEvent := event{
			name: eventAttrName[1:],
		}
		err = foundEvent.fillFromAttr(eventAttr, tzLocation)
		if err != nil {
			return nil, err
		}
		events = append(events, foundEvent)
	}
	sort.Slice(events, func(i, j int) bool {
		return events[j].startTime.After(events[i].startTime)
	})

	table := map[string][][]string{
		"columns": [][]string{
			[]string{
				"NAME",
				"%-40.40s",
			},
			[]string{
				"TIME",
				"%s",
			},
		},
	}
	rows := make([][]string, 0, len(events))
	for _, e := range events {
		rows = append(rows, []string{
			e.name,
			e.startTime.Format(`Jan 2, 2006 3:04:05 PM`),
		})
	}

	table["rows"] = rows
	err = api.SendTableToCaller(ctx, table, "Events:")
	if err != nil {
		return nil, err
	}

	return "", nil
}

func handleGetEvent(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	eventAttr, err := api.GetAttr(ctx, call.ExtensionId, getEventName(call.Arguments[1]), true)
	if err != nil {
		return nil, err
	}
	callerTZ, err := api.GetCallerTZ(ctx)
	if err != nil {
		return nil, err
	}
	tzLocation, err := time.LoadLocation(callerTZ)
	if err != nil {
		tzLocation = time.UTC
	}

	foundEvent := event{
		name: call.Arguments[1],
	}
	err = foundEvent.fillFromAttr(eventAttr, tzLocation)
	if err != nil {
		return nil, err
	}

	record := map[string][]string{
		"keys": []string{
			"Name",
			"Time",
			"Location",
			"Organizer",
			"",
			"",
		},
		"values": []string{
			call.Arguments[1],
			foundEvent.startTime.Format(`Jan 2, 2006 3:04:05 PM`),
			foundEvent.location,
			foundEvent.owner,
			"",
			foundEvent.description,
		},
	}
	err = api.SendRecordToCaller(ctx, record, "Event details:")
	if err != nil {
		return nil, err
	}

	return "", nil
}

func handleDeleteEvent(ctx context.Context, call *api.PluginCall) (interface{}, error) {
	if argErr := api.CheckArgumentCount(call, 2); argErr != nil {
		return nil, argErr
	}

	eventName := getEventName(call.Arguments[1])
	eventAttr, err := api.GetAttr(ctx, call.ExtensionId, eventName, true)
	if err != nil {
		return nil, fmt.Errorf("Event '%s' does not exist", eventName)
	}

	callerName, err := api.GetCallerName(ctx)
	if err != nil {
		return nil, err
	}
	eventAttrValues, err := eventAttr.GetAttrListValue()
	if err != nil {
		return nil, err
	}
	owner, ok := eventAttrValues["owner"]
	if !ok {
		return nil, errors.New("Event is missing an owner")
	}
	if callerName != owner.Value {
		return nil, errors.New("You are not the owner")
	}

	err = api.RemoveAttr(ctx, call.ExtensionId, eventName, true)
	if err != nil {
		return nil, err
	}
	err = api.SendMessageToCaller(ctx, fmt.Sprintf("Deleted event '%s'", call.Arguments[1]))
	if err != nil {
		return nil, err
	}
	return "", nil
}


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
		Name:  "grounds-events",
		Usage: "A Grounds plugin for an event calendar",
		Action: func(c *cli.Context) error {
			handler := api.PluginHandler{
				Method: "event",
				Dispatcher: func(ctx context.Context, call api.PluginCall) api.SubcommandHandler {
					subcommand := call.Arguments[0]
					switch subcommand {
					case "create":
						return handleCreateEvent
					case "list":
						return handleListEvents
					case "get":
						return handleGetEvent
					case "delete":
						return handleDeleteEvent
					default:
						return nil
					}
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

			res := handler.Handle(ctx, req)

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
