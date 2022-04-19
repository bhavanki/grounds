# Mail

"Every program attempts to expand until it can read mail. Those programs which cannot so expand are replaced by ones which can." - Jamie Zawinski

The Grounds mail system lets players send mail messages to each other. Messages are delivered to each player's mailbox.

## Sending Mail

Every mail message must have a subject and a body, and must be sent to at least one recipient player (which can be the sending player). A recipient does not need to be in the same location as the sending player.

```
$ mail send Ahalish "RP Tue?" "Would you like to RP with me next week?"
Sent to 1 recipients
```

Note that you can use single or double quotation marks for the subject and body, so that it is possible to include spaces. You can even include line breaks, so that message bodies can be multiple lines.

```
$ mail send Ahalish "RP Tuesday?" "Dear Ahalish,
dquote>
dquote> I have free time Tuesday to RP, if you're available. Let me know!
dquote>
dquote> -R"
Sent to 1 recipients
```

You may also use [markup](markup.md) to format your message text.

When a mail message is sent, each recipient gets a message notifying them.

```
You have new mail.
```

### Muting

When you mute another player, you never receive mail messages from them. Their messages do not appear in your mailbox and you never get notifications about them.

If another player has muted you, you can send mail messages to them, but they are silently dropped, even though Grounds states that your mail was sent. When you send a message to multiple recipients, it is delivered to those who have not muted you, but not delivered to those who have.

## Managing Mail

Every mail message gets a timestamp for when it was sent. Mail in a player's mailbox is listed by timestamp in reverse chronological order. Each timestamp is formatted in the player's own timezone, as set in their preferences.

```
$ mail list
R   # SENT             FROM                 SUBJECT
-   - ----             ----                 -------
*   1 2021-11-05 22:47 Rehtaoh              RP Tuesday?
*   2 2021-11-05 22:45 Rehtaoh              RP Tue?
```

Read each message by its ID number, as listed in the mailbox.

```
$ mail get 1
From:    Rehtaoh
To:      Ahalish
Sent:    Nov 05, 2021 10:47:55 PM
Subject: RP Tuesday?

Dear Ahalish,

I have free time Tuesday to RP, if you're available. Let me know!

-R
```

Once a message has been read, its read flag, indicated by an asterisk in the mailbox listing, is cleared.

Delete a message also by its ID number. ID numbers are associated with different messages if any message except for the last one is deleted.

```
$ mail delete 1
Deleted.
$ mail list
R   # SENT             FROM                 SUBJECT
-   - ----             ----                 -------
*   1 2021-11-05 22:45 Rehtaoh              RP Tue?
```
