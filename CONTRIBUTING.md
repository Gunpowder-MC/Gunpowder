Contributing
============

Thank you for your interest in contributing to our mod. We appricate the effort but to make the inclusion
of your features or bug fixes a smooth process, please make note of these guidelines.

* **Please write javadoc.** This only strictly applies to public methods in the API and make sure that your
`@param` and `@return` fields are not just blank.
* **Please write succinct code.** If you have to copy the same code multiple times for a some slight tweaks, 
you may want to consider some abstraction.
* **Test your code.** For obvious reasons we aren't looking for broken code.
* **Please limit the length of commit descriptions.** If you must write more, 
double space and start clacking!
* **API belongs in the API and implementation belongs in implementation.** Please keep API and implementation code seperated and
in their proper categories
* **Design your code as if you are running a modded environment.** We would like to avoid creating more mod incompatability issues
because of hardcoded logic or magic values. But please make sure your code still works in a vanilla scenario.
* **Launch the game.** For example, launch the client and play around with the feature or verify the bug has been fixed for a few minutes. 
You may find issues in that little bit of time that you may not have known of.
* **Please don't bump versions in your commits.** Unless this is to bump the version of a dependency which is related to your issue,
let the maintainers bump versions when publishing updates to the mod.
* **Check if other Pull Requests or issues exist related to your inclusions.** All of us would like to avoid tripping over eachother 
because of merge conflicts.
* **Ask questions if you are confused.** If you don't know, ask! It may be worth your time to ask so you don't stumble around the place.

Checklist
---------

Ready to submit? Perform the checklist below:

1. Have I launched the Client and Server both in development and in production to make sure the game actually loads?
2. Have all tabs been replaced into four spaces? Are indentations 4-space wide?
3. Have I made sure my commits do not introduce much noise. This includes reordering methods
4. Have I written proper Javadocs for my public methods? Are the @param and
   @return fields actually filled out?
5. Have I `git rebase`d my pull request to the latest commit of the target
   branch?
6. Have I combined my commits into a reasonably small number (if not one)
   commit using `git rebase`?
7. Have I made my pull request too large? Pull requests should introduce
   small sets of changes at a time. Major changes should be discussed and it is reccomended to 
   [open an issue](https://github.com/Gunpowder-MC/Gunpowder/issues/new/choose) prior to starting work.
8. Are my commit messages descriptive?

You should be aware of [`git rebase`](https://learn.github.com/p/rebasing.html).
It allows you to modify existing commit messages, and combine, break apart, or
adjust past changes.

For maintainers
---------------

If you have forgetten small things like license headers or a formatting issue after a commit has been pushed, and
if no history exists past the commit, please rebase and re-force push the commit in order to keep the history clean.
