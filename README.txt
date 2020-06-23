This is a forge mod that implements client side /ignore functionality.

It currently supports Minewind with general chat and public chat.
However, private message functionality is NOT implemented because of the /r command being commonly used.
It could easily lead to sending private messages to the wrong person if you get a message from someone who is blocked.
Also, Minewind has built-in ability to ignore private messages.

Functional break-down / Commands list
/ignorehelp -> Prints this help message
/ignoreuser username -> Adds a username to the ignore list
/unignoreuser username -> Removes a username from the ignore list
/ignoreword word -> Adds a word to the ignore list
/unignoreword word -> Removes a word from the word list
/ignorelist -> Prints all ignored usernames and words
/ignoreuserlist -> Prints all ignored usernames
/ignorewordlist -> Prints all ignored words
