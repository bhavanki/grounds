# Creates an extension with some random commands. This includes:
# - the random system extension
# - the $roll plugin call command
# - the $coinflip plugin call command
# - the $8ball plugin call command
#
# Run as GOD in the ORIGIN of a universe. Adjust the paths to YAML files as
# necessary for them to load.

build extension random
change_policy random w-dBA

set_attr random $roll[ATTRLIST]=@etc/ext/random/roll.yaml
set_attr random $coinflip[ATTRLIST]=@etc/ext/random/coinflip.yaml
set_attr random $8ball[ATTRLIST]=@etc/ext/random/8ball.yaml

say Random plugin installed.
