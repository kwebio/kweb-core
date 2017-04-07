---
layout: page
title: "Live coding"
category: use
order: 3
date: 2017-03-09 10:04:11
---

Wouldn't it be great if, when building a website, you could modify your code and immediately see the change reflected
in your web browser?  Great news, Kweb can do this!

#### Step 1 : Install Dynamic Code Evolution VM (DCEVM) Java patch

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

When the installation is complete you can quit the DCEVM installer.

#### Step 2 : Download HotSwapAgent

Go [here](https://github.com/HotswapProjects/HotswapAgent/releases) and download the latest hotswap-agent .jar,
([hotswap-agent-1.1.0-SNAPSHOT.jar](https://github.com/HotswapProjects/HotswapAgent/releases/download/1.1.0-SNAPSHOT/hotswap-agent-1.1.0-SNAPSHOT.jar)
at the time of writing).

Save it somewhere appropriate, perhaps in a directory called `javalibs` in your home directory (it doesn't really 
matter where you put it but you'll need the full path and filename later).

#### Step 3 : Modify your project's run configuration

In intelliJ select `Edit Configurations...` under the `Run` menu, and paste the following into the `VM Options` field:

`-XXaltjvm="dcevm" -javaagent:PATH_TO/hotswap-agent.jar=disablePlugin=AnonymousClassPatch`

Be sure to edit `PATH_TO/hotswap-agent.jar` to be the full path and name of the hotswap-agent .jar file you downloaded
in Step 2.

Note that `=disablePlugin=AnonymousClassPatch` was necessary at the time of writing to prevent a non-fatal 
error, but this bug in HotSwapAgent has been reported and may be fixed by now.

#### Step 4 : Configure Kweb to refresh webpages automatically

Set the `refreshPageOnHotswap` Kweb constructor parameter to `true`:

```kotlin
    Kweb(port = 1234, refreshPageOnHotswap = true) {
```

#### Step 5 : Run your code

Now run your project in debug mode (the button from the green bug), you should see something like this:

```
HOTSWAP AGENT: 19:01:57.851 INFO (org.hotswap.agent.HotswapAgent) - Loading Hotswap agent {1.1.0-SNAPSHOT} - unlimited runtime class redefinition.
HOTSWAP AGENT: 19:01:58.178 INFO (org.hotswap.agent.config.PluginRegistry) - Discovered plugins: [KwebHotswapPlugin]
HOTSWAP AGENT: 19:01:59.145 INFO (org.hotswap.agent.config.PluginRegistry) - Discovered plugins: [Hotswapper, WatchResources, ClassInitPlugin, Hibernate, Hibernate3JPA, Hibernate3, Spring, Jersey1, Jersey2, Jetty, Tomcat, ZK, Logback, Log4j2, MyFaces, Mojarra, Seam, ELResolver, WildFlyELResolver, OsgiEquinox, Owb, Proxy, WebObjects, Weld, JBossModules, ResteasyRegistry, Deltaspike, JavaBeans]
HOTSWAP AGENT: 19:01:59.231 INFO (org.hotswap.agent.config.PluginRegistry) - Discovered plugins: []
HOTSWAP AGENT: 19:02:00.009 INFO (org.hotswap.agent.config.PluginRegistry) - Plugin 'org.hotswap.agent.plugin.logback.LogbackPlugin' initialized in ClassLoader 'sun.misc.Launcher$AppClassLoader@18b4aac2'.
```

This indicates that HotSwapAgent is working, and in particular look out for `Discovered plugins: [KwebHotswapPlugin]` as this will indicate 
that Kweb is aware of HotSwapAgent (this is necessary for the automatic refresh).

#### Step 6 : Try it!

Launch a web browser and point it at your Kweb instance.  Now make a change to your code, and rebuild the file you've 
modified (Shift+Command+F9 on a Mac), in a few seconds the browser window should automatically refresh to reflect
the change.

Note that HotSwapAgent doesn't work from every code modification, for example if you make a change to the configuration
of your Kweb object it will not pick this up.

----------
**Next: [More examples]({{ site.baseurl }}{% post_url 2017-03-11-more-examples %}) >>>>**
