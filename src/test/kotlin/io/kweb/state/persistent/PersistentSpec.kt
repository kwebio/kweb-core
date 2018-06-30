package io.kweb.state.persistent

/**
 * Created by ian on 6/29/17.

object PersistentSpec : Spek({
    val webClient: WebClient = autoClose(ACWebClient())

    init {
        // htmlUnitInit(webClient)

        "Given a shoebox and a ordered set of tan colored dogs" - {
            data class Dog(val name: String, val color: String, val breed: String)

            val dogs = Shoebox<Dog>()
            listOf(
                    Dog(name = "hot dog", color = "tan", breed = "dachshund"),
                    Dog(name = "toby", color = "tan", breed = "labrador")
            ).forEach { dogs[it.name] = it }

            val viewByColor = dogs.view("dogsByColor", Dog::color)
            val tanDogs = viewByColor.orderedSet("tan", compareBy(Dog::name))
            tanDogs.entries.size shouldBe 2
            tanDogs.entries[0] shouldBe Dog(name = "hot dog", color = "tan", breed = "dachshund")
            tanDogs.entries[1] shouldBe Dog(name = "toby", color = "tan", breed = "labrador")


            "Given a Kweb instance serving a simple website displaying the tan dogs in a list" - {
                Kweb(port = 5424) {
                    doc.body.new {
                        ul().new {
                            renderEach(tanDogs) { tanDog ->
                                li().text(tanDog.invoke { "${it.name} is a ${it.color} ${it.breed}" })
                            }
                        }
                        a().setAttribute("name", "add fred").text("Add fred").on.click {
                            dogs["fred"] = Dog(name = "fred", color = "tan", breed = "beagle")
                        }
                        a().setAttribute("name", "remove toby").text("Remove toby").on.click {
                            dogs.remove("toby")
                        }
                        a().setAttribute("name", "bleach hot dog").text("Bleach hot dog").on.click {
                            dogs.modify("hot dog") { it.copy(color = "white") }
                        }
                        a().setAttribute("name", "make fred lab").text("Make fred a labrador").on.click {
                            dogs.modify("fred") { it.copy(breed = "labrador") }
                        }
                    }
                }

                //Thread.sleep(5000)

                //dogs["fred"] = Dog(name = "fred", color = "tan", breed = "beagle")

                //Thread.sleep(100000)

                "Visiting the page" - {
                    val page = webClient.getPage<HtmlPage>("http://127.0.0.1:5424/")
                    "should respond with a 200 code" {
                        page.webResponse.statusCode shouldBe 200
                    }
                    "should list the dogs in the correct order" {
                        pollFor(5.seconds) {
                            page.getElementsByTagName("li").let { listItems ->
                                listItems.size shouldEqual 2
                                listItems[0].textContent shouldEqual "hot dog is a tan dachshund"
                                listItems[1].textContent shouldEqual "toby is a tan labrador"
                            }
                        }
                    }
                    "clicking button should add a new dog called fred" {
                        val clickedPage = page.getElementByName<HtmlAnchor>("add fred").click<HtmlPage>()
                        pollFor(5.seconds) {
                            val listItems = clickedPage.getElementsByTagName("li")
                            listItems.size shouldEqual 3
                            listItems[0].textContent shouldEqual "fred is a tan beagle"
                            listItems[1].textContent shouldEqual "hot dog is a tan dachshund"
                            listItems[2].textContent shouldEqual "toby is a tan labrador"
                        }
                    }
                    "clicking button should remove dog called toby" {
                        val clickedPage = page.getElementByName<HtmlAnchor>("remove toby").click<HtmlPage>()
                        pollFor(5.seconds) {
                            val listItems = clickedPage.getElementsByTagName("li")
                            listItems.size shouldEqual 2
                            listItems[0].textContent shouldEqual "fred is a tan beagle"
                            listItems[1].textContent shouldEqual "hot dog is a tan dachshund"
                        }
                    }
                    "clicking button should bleach hot dog and remove from list" {
                        val clickedPage = page.getElementByName<HtmlAnchor>("bleach hot dog").click<HtmlPage>()
                        pollFor(5.seconds) {
                            val listItems = clickedPage.getElementsByTagName("li")
                            listItems.size shouldEqual 1
                            listItems[0].textContent shouldEqual "fred is a tan beagle"
                        }
                    }
                    "clicking button should make fred a labrador" {
                    val clickedPage = page.getElementByName<HtmlAnchor>("make fred lab").click<HtmlPage>()
                    pollFor(5.seconds) {
                        val listItems = clickedPage.getElementsByTagName("li")
                        listItems.size shouldEqual 1
                        listItems[0].textContent shouldEqual "fred is a tan labrador"
                    }
                }
                }
            }
        }
    }
})

*/