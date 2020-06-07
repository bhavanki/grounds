# Creates a chat system. This includes:
# - the chat system extension
# - the chatbot player with ADEPT role
# - the $chat scripted command
# - the $chatadmin scripted command
#
# Run as GOD in the ORIGIN of a universe. Adjust the paths to YAML files as
# necessary for them to load.

build extension chat_system
change_policy chat_system w-dBA
build player chatbot adept
set_attr chat_system owner[THING]=chatbot
set_attr chat_system $chat[ATTRLIST]=@etc/ext/chat/chat.yaml
set_attr chat_system $chatadmin[ATTRLIST]=@etc/ext/chat/chatadmin.yaml

say Chat system installed.