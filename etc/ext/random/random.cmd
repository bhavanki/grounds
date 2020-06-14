# Creates an extension with some random commands. This includes:
# - the random system extension
# - the $roll scripted command
# - the $coinflip scripted command
#
# Run as GOD in the ORIGIN of a universe. Adjust the paths to YAML files as
# necessary for them to load.

build extension random
change_policy random w-dBA
set_attr random owner[THING]=GOD
set_attr random $roll[ATTRLIST]=@etc/ext/random/roll.yaml
set_attr random $coinflip[ATTRLIST]=@etc/ext/random/coinflip.yaml
set_attr random $8ball[ATTRLIST]=@etc/ext/random/8ball.yaml

say Random commands installed.
