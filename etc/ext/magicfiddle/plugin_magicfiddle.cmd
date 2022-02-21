# Creates an extension representing a magic fiddle that greets arriving players.
# This includes:
# - the magic_fiddle_ext extension
# - the ^welcome listener attribute
# - the proxy magic fiddle thing

# Run as GOD where you want the magic fiddle to live. Adjust the paths to YAML
# files as necessary for them to load.

build extension pg_magic_fiddle_ext
yoink pg_magic_fiddle_ext here
set_attr pg_magic_fiddle_ext ^welcome[ATTRLIST]=@etc/ext/magicfiddle/magicfiddle.yaml
role add bard pg_magic_fiddle_ext

build thing "pg magic fiddle"
# stop anyone from picking it up
change_policy "pg magic fiddle" g-d
describe "pg magic fiddle" "This faintly sparkling, faintly golden musical instrument hovers steadily in the corner, its bow trained across its strings."

say Magic fiddle installed.
