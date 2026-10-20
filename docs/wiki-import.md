# Importing the screenshot guide into a GitHub wiki

The `wiki/` directory contains the complete guide: Markdown pages, `Home.md`, a custom `_Sidebar.md`, and its own `images/` directory. It does not depend on images in the main repository. Internal page links use GitHub wiki syntax.

The live guide is at [Butter Launcher Wiki](https://github.com/trevor-orbit-sling/butter-launcher/wiki). To publish or update the guide:

1. Enable the repository's Wiki and create its initial page on GitHub.
2. Clone the separate wiki repository:

   ```bash
   git clone https://github.com/trevor-orbit-sling/butter-launcher.wiki.git /path/to/butter-wiki
   ```

3. From the Butter Launcher source checkout, copy the guide into that clone:

   ```bash
   cp -R wiki/. /path/to/butter-wiki/
   ```

4. Review the copied pages and screenshots, commit them in the wiki clone, and push its default branch when ready. The main repository's Git history is separate from the wiki's history.

For later updates, refresh screenshots in both `docs/screenshots/` and `wiki/images/`, then copy the updated guide into the wiki clone again.

See GitHub's instructions for [editing wiki pages locally](https://docs.github.com/en/communities/documenting-your-project-with-wikis/adding-or-editing-wiki-pages) and [custom sidebar files](https://docs.github.com/en/communities/documenting-your-project-with-wikis/creating-a-footer-or-sidebar-for-your-wiki).
