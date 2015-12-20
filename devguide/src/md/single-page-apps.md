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
