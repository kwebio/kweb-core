---
layout: page
title: "Live coding"
category: use
order: 3
date: 2017-03-09 10:04:11
---

### What is live coding?

Wouldn't it be great if, when building a website, you could modify your code and immediately see the change reflected
in your web browser?  Great news, Kweb can do this!

### Step 1 : Install Dynamic Code Evolution VM (DCEVM) Java patch

Go to [https://github.com/dcevm/dcevm/releases](https://github.com/dcevm/dcevm/releases) and download the latest
DCEVM light installer 
([DCEVM-light-8u112-installer.jar](https://github.com/dcevm/dcevm/releases/download/light-jdk8u112%2B8/DCEVM-light-8u112-installer.jar) 
at the time of writing).

Run the installer by double-clicking on it.

*Note for Mac users*: You may get an error like `“DCEVM-light-8u112-installer.jar” can’t be opened because it is from an unidentified 
developer`, if so you should control-click the file and select "Open" from the drop-down menu, then click "Open".

You must now select your Java Development Kit directory.  If it isn't already listed click **Add installation directory**,
for example on a Mac it may be `/Library/Java/JavaVirtualMachines/jdk1.7.0_121.jdk/Contents/Home`.

Click **Install DCEVM as altjvm**.

You can now quit the DCEVM installer.

### Step 2 : Download HotSwapAgent

