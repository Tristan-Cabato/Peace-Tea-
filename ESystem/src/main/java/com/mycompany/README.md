Exer4

------

Finally figured it out after 7 commits and resisting a certain commit irl. Issues faced in current exer: 
	- Main issue was the overcomplication of backtick usage and abusing try catch in the delete method (Made grants harder than it should've been)
    - Pom mismatch, this can easily be fixed by clean and rebuild
    - Detecting the appropriate user typed in login
    - Deleting the user from the table instead of dropping them (Its still there, no, clearing the database is not a shortcut)