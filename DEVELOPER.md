# Developer Notes

## Recommended setup

### Git hooks

We recommend using this for `.git/hooks/pre-commit`:

```bash
#!/bin/bash

./gradlew test
```

## apiDump

Kweb's external API will be checked against `api/kweb-core.api` and the build will fail if it changes. This will
often have false-positives so to update the API dump, run `./gradlew apiDump` and commit the new dump file
with your other changes.

## Updating the user manual

You can build the user manual locally by [installing mdBook](https://rust-lang.github.io/mdBook/guide/installation.html)
and running `mdbook serve` in the `docs/` directory. You can then view the documentation at http://localhost:3000/,
changes will be automatically reloaded.