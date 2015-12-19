# Single Page Application

Let's look at how a typical app lifecycle works, starting with rendering the initial response on the backend.

## Backend Rendering

A minimal response is rendered on the backend and sent down to the
client, setting up things like assets (js, css, fonts, etc), and
configuration data (passed forward to ClojureScript via serialized
[Transit]().

NSFW provides functions for generating responses in a composbile way,
including passing forward configuration data to your cljs:

[:spa/render-spec-basic]()

The code above will:

* Include `/css/app.css`, and `/cljs/app.js`
* Embed `{:js-entry :main}` into the page where it can be accessed by
  your cljs

Use `:env` as a way to set up any data you'll need to set up and
render your page should passed forward in this manner, including user
data and API keys. Don't be lazy and fire off an ajax call from the
frontend to grab data for that initial render, that makes for a poor
user experience.



# Scratch
  ---------------------------------
  v                               ^
Event -> State (Sync / Async) -> Side Effects -> Render

Rendering is taken care of by React, so really, we're


State -> State, then multiple application functions. Should be a


How to do async?


## Assumptions

* One atom representing app state
* Context for things like connections / env info
* State is updated based on previous state and context (for things like config, connections, etc). State can be synchronously calculated (returns a map), or asynchronously calculated (returns a channel).
* Handler Types: Sync, Asyc, Side Effectful

* Sync -> map
* Async -> channel
* Se -> nil (args are different)


Sync / Async params - [state params ctx

SE params - [!state params ctx
