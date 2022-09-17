#!/bin/bash

CURRENT_RELEASE=$(gh release list -L 1 | cut -f3)
echo "Previous Release: $CURRENT_RELEASE"
read -pr "New Release: " NEW_RELEASE
gh release create "$NEW_RELEASE" -p