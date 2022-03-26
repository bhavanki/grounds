# Creates a events system. This includes:
# - the events system extension
# - the $event scripted command
#
# Run as GOD in the ORIGIN of a universe. Adjust the paths to YAML files as
# necessary for them to load.
#
# To uninstall:
# destroy events_system

build extension events_system
change_policy events_system w+d
set_attr events_system $event[ATTRLIST]=@plugins/events/events.yaml
role add bard events_system

say Event system installed.
