# Towards Intentful Machines - Building the next generation of APIs.

When there were no computers, we didn’t have _Application_ Programming Interfaces (APIs), but we had _Human_ Programming Interfaces called _natural languages._ As we struggle as a species today training ourselves on how to talk to machines efficiently, there remains a lot to learn from human to human interaction.

Most readers might assume that _Intentful_ is a proper English word, or at least understand the import of the word -- THAT is the beauty of natural languages. What's important is the _intention_ \-\- the semantics do not matter much. Normally, machines would reject a word simply because it's not in their dictionary, yet thoughtful English teachers like [Marlene](http://redefinedagain.blogspot.com/2011/01/intentful.html) find creative reasons to invent it, and other humans find it easy to understand it. It is time to make machines behave like humans and be aware of the intent behind what they do. **It is time for machines to become Intentful.**

Let’s consider you are a rich person (you made your money building the transcontinental railroad) and you have a fleet of servants. These are a set of servants who do exactly what you want and when you want — “iron my tux”, “fix my bed”, “get my horse”, etc. Yeah, 
know you have a car and you feel good about it, but is yours self-driving like a horse? _Think about it!_ — but don’t think too much yet -- that's for another day -- let’s continue with the story. These servants are what we would like to call the “procedural servants”. They will do what you want and when you want, and no more. They don’t know what is your grand plan for the day. That's still in your head. You are the one responsible for getting things done in the right order and with the right urgency. But, you are rich! So, let’s hire a Butler for you. According to the _International Guild of Butlers_:

 “A Butler typically: **Oversees the household staff** usually of one residence. Understands concepts like being **anticipatory,** friendly not familiar, privacy and confidentiality, **invisible** and **available.**”

> You express your intentions to the Butler, and the Butler takes care of the mundane “programming” of the servants for you.

The butler is still a Programming Interface, but one that is anticipatory, takes away the burden of programming, reducing the size of the API (you just interact with the butler instead of the fleet of servants), and hides the implementation details (you don’t need to know whether the bed was made first, or the clothes were done first, or in parallel, or if one of the staff was sick).

> You can have a conversation around the ‘what’ and don’t need to know about the ‘how’. Being able to express Intent and see things happen is what delights humans.

We posit this to be the **true north** for all machine APIs directly consumed by humans.

The world of computers is evolving, and the ‘staff’ of machines is growing rapidly around us, and we so desperately need a butler. 

> Humans are good at managing humans, and only machines are good at managing machines.

Machines never sleep, so their manager should not sleep either. The modern data center is the busiest place where machines come together and is the perfect place for this revolution to begin. We need to let the IT admins sleep (repeat after me: let the IT admins sleep!) and delegate the sleepless tasks to the relentless machines.

We are witnessing this great transformation of data center APIs from being merely _Procedural_ to becoming more and more _Intentful_. Apple’s Siri, Google’s assistant, Amazon’s Alexa, Tesla’s cars are all making the transition from _"Tell me what to do"_ to _"Tell me what you want"_.

So, what is an Intentful API?
-----------------------------

![](https://media.licdn.com/dms/image/C5112AQFrUqgjcveK4w/article-inline_image-shrink_1500_2232/0?e=1540425600&v=beta&t=w70V1uX3dhnTGJ6b7Oflk_JkrmsB8DkMfexG8lTnYT4)

Consider the McDonald’s menu from its early days. We can say that the menu is an "API". It is a contract between the chef and the customer. The chef can change the ingredients, evolve from using ovens to microwaves, decide how long to cook the burger — because the menu does not specify any of that. The contract essentially says:

"A Pure Beef Hamburger at 15c"

Everything else can change over the years in order to delight the customer. This is the essence of an Intentful API.

A Procedural API would have exposed the following primitives:

#define MCDONALDS
﻿
﻿int put\_burger\_in_oven(); // returns error if oven is not working
int cook_burger(int temperature, int cook_time); // returns error if params are not valid
int remove\_burger\_from_oven(); // returns error if oven is empty

Procedural APIs are far more expressive than Intentful APIs as they provide for customization. However, customization is a good thing only when the consumer is both _knowledgeable_ and _cares to be prescriptive_. Procedural APIs start to become burdensome when the programmer stops caring for the customizations, as that is not where the programmer wants to invest his/her time.

In computer science, we have seen this evolution happen many times over: (i) from assembly language to higher level languages, (ii) from custom build scripts to Makefiles, (iii) from custom syntax parsers to [Lex/Yacc](http://dinosaur.compilertools.net/), (iv) from carefully laid data on disks to [SQL](https://en.wikipedia.org/wiki/SQL), and (v) from python scripts to [Ansible](https://www.ansible.com/how-ansible-works) and [SaltStack](https://docs.saltstack.com/en/latest/topics/). As computer programmers focus on higher layers of the stack, they want the lower layers to become Intentful and hence, machine-programmed.

> The best way to make programmers more efficient is to make them not program. Intentful APIs move programming from the users to the machines.

So, assume there is an application stack (_app1_) running in the data center. In an Intentful API, changing the number of replicas and providing data protection parameters for the app would be as simple as modifying three properties in a YAML file and resubmitting to the system via a [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) API:

```
app1:
  ...
  replicas: 3
  rpo: 60s
  rto: 0s
  ...
```

![](https://media.licdn.com/dms/image/C5112AQHQMfbHL2e0ng/article-inline_image-shrink_1000_1488/0?e=1540425600&v=beta&t=r9Xmuf6WfHPg7VE3rCG2eQG9t4ojtM0ef-f-9Gp5Vf4)

In the above diagram, the user specifies the desired state and waits for the system's current state to reach the desired state. The machine automatically figures out the steps required to reach the end state. And just like a GPS-based navigation system, the Intentful system is also able to react appropriately to ongoing obstacles and failures. Allowing the machine to know what the desired end state is, makes the system more robust and opens the door to building intentful machines.

How big should my API be ?
--------------------------

> APIs are like promises - don't plan on breaking them. At the same time, don't make too many of them.

The smaller the surface area of the API, the easier it is to evolve the two sides of the API while not breaking functionality; the producer of the API has more flexibility to evolve the implementation of the API; and the consumer does not have too many points of dependencies.

However, how do you make an API smaller without reducing its capabilities?The answer is to allow the expression of the intent (what), and not the procedure (how).

The intent is the purest and simplest form of interface between the producer and consumer of a service. The above _app1_ example will look like:

```
app1:
  availability: 99.9999
```
> Once the API is reduced to just the Intent, it becomes delightful.

To make the process of building intentful APIs as easy as possible, we have launched this Papiea project. 
