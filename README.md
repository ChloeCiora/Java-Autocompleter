# Java-Autocompleter

This program, when ran, gives suggestions for what a user is typing. Enter each character one at a time, and enter "!" at any point to exit, or "$" at any point to end a word. Enter a number 1-5 corresponding to the predictions of the program in order to choose a selection. The user history will be saved to a file called user_history.txt and will be loaded back into the program at the start of each execution. This autocompleter uses a DLB to predict what words could follow next. More information about implementation can be found in approach.txt.

Here is an example run of the program:

```
$ java AutoComplete

Enter your character.
J
[1]: J   [2]: J's   [3]: JCS   [4]: JD   [5]: JFK
(270725 nanoseconds)

Enter your character.
a
[1]: Jack   [2]: Jaclyn   [3]: Jacob   [4]: Jacquard   [5]: Jacqueline
(376448 nanoseconds)

Enter your character.
v
[1]: Java   [2]: Javier
(157829 nanoseconds)

Enter your character.
1
Your word is: Java

Enter a new word.
Enter your character.
!
Average time: 268334 nanoseconds.
Goodbye!
```


